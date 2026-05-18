# AtlasLang

[![Minecraft](https://img.shields.io/badge/Minecraft-1.16%2B-dark_green.svg)](https://shields.io/)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://shields.io/)
[![JitPack](https://jitpack.io/v/nauticstudios/AtlasLang.svg)](https://jitpack.io/#nauticstudios/AtlasLang)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

Multi-language plugin for Minecraft with PlaceholderAPI integration, GitHub-based synchronization
of language files and a small, professional API so any other plugin can translate its messages
through the player's chosen language.

> [!IMPORTANT]
> Requires Minecraft **1.16 or newer** since the plugin relies on HEX colors (`§x§r§r§g§g§b§b`)
> and MiniMessage rendering. Older versions will not display colors correctly.

> [!CAUTION]
> Only depend on the **API** module from JitPack, never on the full plugin jar. The plugin already
> ships the implementation — shading it inside your own jar will cause class conflicts.

The whole point of the API is to stay simple: one static facade, async by default, a fluent
service if you want to keep a reference, and Bukkit events for reactive integrations. No
configuration, no `onEnable` boilerplate.

### Features

- Per-player language with locale aliases (`en_US`, `es_ES`, `ja_JP`, ...)
- In-memory cache loaded on `AsyncPlayerPreLoginEvent` — no DB hit per placeholder call
- PlaceholderAPI expansions: `%alang_(file)_(key)%`, `%atlaslang_language%`, `%atlaslang_locale%`
- GitHub sync of language files (with Zip-Slip protection and content-hash diffing)
- H2 + MySQL with HikariCP, fully async writes
- MiniMessage + legacy + HEX color support
- Public API with `CompletableFuture`, namespaces and cancellable events

### Getting Started

The API targets **Java 17** and uses `CompletableFuture` for any operation that touches the
database, so calling it from the main thread is safe. You only need to depend on the `API`
module — the implementation lives inside the plugin and registers itself on enable.

You can drop it into your project with JitPack:

#### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>com.github.nauticstudios.AtlasLang</groupId>
    <artifactId>api</artifactId>
    <version>version</version>
    <scope>provided</scope>
</dependency>
```

#### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.nauticstudios.AtlasLang:api:version")
}
```

#### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.nauticstudios.AtlasLang:api:version'
}
```

#### plugin.yml

```yaml
depend: [AtlasLang]
# or, if AtlasLang is optional at runtime:
# softdepend: [AtlasLang]
```

### Usage

The simplest case is translating a message for a player using their saved language:

```java
String msg = AtlasAPI.translate(player, "welcome.message");
player.sendMessage(msg);
```

UUIDs work the same way:

```java
String msg = AtlasAPI.translate(uuid, "welcome.message");
```

If you ship your own language files under a namespace (a folder like
`languages/<lang>/myplugin.yml`), pass it explicitly:

```java
String msg = AtlasAPI.translate(player, "myplugin", "menu.title");
```

To force a specific language regardless of the player's choice:

```java
String msg = AtlasAPI.translateInLang("es_ES", "welcome.message");
```

### Player language

`getLanguage` returns from the in-memory cache, no database hit:

```java
String current = AtlasAPI.getLanguage(player);

AtlasAPI.setLanguageAsync(player.getUniqueId(), "es_ES")
        .thenAccept(ok -> getLogger().info("Switched: " + ok));
```

### Compatibility Detection

If your addon ships translations for a subset of locales, AtlasLang tells you which ones
overlap with the server's registered languages:

```java
Set<String> mine = Set.of("en_US", "es_ES", "jp_JP");
Set<String> compatible = AtlasAPI.api().compatibleLanguages(mine);
```

### Events

`PlayerLanguageChangeEvent` is fired before any switch is persisted and can be cancelled or
rewritten:

```java
@EventHandler
public void onLangChange(PlayerLanguageChangeEvent event) {
    if (event.getNewLanguage().equals("ja_JP") && !hasPermission(event)) {
        event.setCancelled(true);
    }
}
```

### Fluent Style

The static facade is just a wrapper around the registered `AtlasAPI` instance, so you can grab
it once if you prefer the fluent style:

```java
AtlasAPI api = AtlasAPI.api();

String msg  = api.message(player, "welcome.message");
String def  = api.getDefaultLanguage();
Set<String> locales = api.getRegisteredLocales();
```

You can also guard against older AtlasLang versions during your `onEnable`:

```java
AtlasAPI.requireVersion(2);
```

### GitHub Sync

AtlasLang can pull its `languages/` directory from a GitHub repo. Configure `github` in
`config.yml`:

```yaml
github:
  repository:
    name: "owner/repo"
    branch: "main"
  authentication:
    type: "none"      # or "token"
    token: ""
  paths:
    remote-root: "languages"
    local-root: "languages"
  sync:
    create-missing: true
    overwrite-existing: true
    delete-missing: false
    reload-after-sync: true
```

Run `/atlaslang github sync` from console or in-game. Diffs are computed by content hash, so a
sync with no changes returns `NO_CHANGES` instead of touching disk. ZIP entries are validated
against the local root to prevent path-traversal.

### Notes

- All database writes go through an async pool, so calling `setLanguageAsync` from the main thread is safe.
- The cache is keyed by `UUID` and shared across every API caller.
- Locale lookups (`getLocaleOf`, `compatibleLanguages`) are O(1) — backed by a reverse map built at load time.
- Player language is preloaded on `AsyncPlayerPreLoginEvent` and dropped on `PlayerQuitEvent`.
- HEX color support requires Minecraft 1.16+. Use legacy codes (`&a`, `&b`, ...) on older clients.
- Do **not** bundle the API module inside your plugin jar — the implementation is already on the server.

### License

Released under the MIT License. Powered by **Nautic Studios** © 2026.
