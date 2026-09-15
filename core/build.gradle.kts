plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.18") {
        exclude("com.google.guava", "guava")
        exclude("com.google.code.gson", "gson")
        exclude("it.unimi.dsi", "fastutil")
    }
    compileOnly("me.clip:placeholderapi:2.12.3")
    compileOnly("com.github.GriefPrevention:GriefPrevention:18.0.0")
    compileOnly("com.intellectualsites.plotsquared:plotsquared-core:7.6.0")
}

java {
    disableAutoTargetJvm()
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.compileJava {
    options.release = 16
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(project.components["java"])
        }
    }
}