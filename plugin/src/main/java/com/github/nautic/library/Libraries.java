package com.github.nautic.library;

import revxrsal.zapper.Dependency;
import revxrsal.zapper.relocation.Relocation;

import java.util.List;

public enum Libraries {

    HIKARICP(
            new Dependency("com.zaxxer", "HikariCP", "6.3.0"),
            relocate("com{}zaxxer{}hikari", "com.github.nautic.libs.hikari")
    ),

    SLF4J_API(
            new Dependency("org.slf4j", "slf4j-api", "1.7.36")
    ),

    SLF4J_SIMPLE(
            new Dependency("org.slf4j", "slf4j-simple", "1.7.36")
    ),

    H2(
            new Dependency("com.h2database", "h2", "2.1.214"),
            relocate("org{}h2", "com.github.nautic.libs.h2")
    ),

    MYSQL(
            new Dependency("com.mysql", "mysql-connector-j", "9.2.0"),
            relocate("com{}mysql", "com.github.nautic.libs.mysql")
    );

    private final Dependency dependency;
    private final List<Relocation> relocations;

    Libraries(Dependency dependency, Relocation... relocations) {
        this.dependency = dependency;
        this.relocations = List.of(relocations);
    }

    public Dependency dependency() {
        return dependency;
    }

    public List<Relocation> relocations() {
        return relocations;
    }

    private static Relocation relocate(String from, String to) {
        return new Relocation(from.replace("{}", "."), to);
    }
}
