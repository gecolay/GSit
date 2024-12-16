dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.12")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.GriefPrevention:GriefPrevention:16.18.4")
    compileOnly("com.intellectualsites.plotsquared:plotsquared-core:7.4.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    compileJava {
        options.release.set(16)
    }
}