plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}

dependencies {
    api(project(":core"))
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}