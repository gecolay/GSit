plugins {
    `maven-publish`
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.12")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.GriefPrevention:GriefPrevention:16.18.4")
    compileOnly("com.intellectualsites.plotsquared:plotsquared-core:7.4.0")
}

configurations.all {
    resolutionStrategy {
        // worldguard-bukkit & plotsquared-core require outdated versions compared to paper-api, so we force the new version
        force("com.google.guava:guava:33.3.1-jre")
        force("com.google.code.gson:gson:2.11.0")
        force("it.unimi.dsi:fastutil:8.5.15")
    }
}

java {
    disableAutoTargetJvm()
}

tasks.compileJava {
    options.release = 17
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