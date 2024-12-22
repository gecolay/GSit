plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":core"))
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}