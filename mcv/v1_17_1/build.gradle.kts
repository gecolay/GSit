plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":core"))
    paperweight.paperDevBundle("1.17.1-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(16)
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}