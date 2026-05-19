import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.4.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://jitpack.io/")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
    }
}

dependencies {
    api(project(":core"))
    api(project(":v1_17_1", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_18", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_18_2", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_19", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_19_1", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_19_3", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_19_4", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_20", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_20_2", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_20_3", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_20_5", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_2", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_4", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_5", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_6", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_9", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_11", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v26_1", "default"))
}

tasks {
    compileJava {
        options.release = 16
    }

    shadowJar {
        enabled = false
    }

    jar {
        enabled = false
    }

    val sources = mapOf(
        "dev" to mapOf("website" to "https://github.com/gecolay/GSit"),
        "github" to mapOf("website" to "https://github.com/gecolay/GSit"),
        "modrinth" to mapOf("website" to "https://modrinth.com/plugin/gsit"),
        "spigot" to mapOf("website" to "https://www.spigotmc.org/resources/GSit.62325"),
        "paper" to mapOf("website" to "https://hangar.papermc.io/gecolay/GSit")
    )

    val resourceTasks = sources.mapValues { (sourceName, sourceProps) ->
        register<ProcessResources>("processResources${sourceName.replaceFirstChar { it.uppercase() }}") {
            from("resources")
            into(layout.buildDirectory.dir("generated/resources/$sourceName"))

            val baseProps = project.properties.filterValues { it is String || it is Number || it is Boolean }.mapValues { it.value.toString() }
            val props = baseProps + sourceProps + mapOf(
                "source" to sourceName,
                "main" to "${project.group}.${project.name}Main"
            )

            inputs.property("source", sourceName)
            inputs.properties(sourceProps)

            expand(props)
        }
    }

    val jarTasks = sources.keys.associateWith { sourceName ->
        register<ShadowJar>("shadowJar${sourceName.replaceFirstChar { it.uppercase() }}") {
            group = "build"

            val resourceTask = resourceTasks.getValue(sourceName)

            dependsOn(resourceTask)

            archiveClassifier.set("")
            destinationDirectory.set(layout.buildDirectory.dir(if (sourceName == "dev") "libs" else "libs/$sourceName"))

            from(sourceSets.main.get().output)
            from(resourceTask)

            configurations = listOf(project.configurations.runtimeClasspath.get())

            minimize()

            manifest {
                attributes["paperweight-mappings-namespace"] = io.papermc.paperweight.util.constants.SPIGOT_NAMESPACE
            }
        }
    }

    build {
        dependsOn(jarTasks.values)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks.named("shadowJarDev"))
        }
    }
}