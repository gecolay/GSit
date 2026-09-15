plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.6.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23" apply false
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

java {
    disableAutoTargetJvm()
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
    api(project(":v26_2", "default"))
    api(project(":v26_3", "default"))
}

tasks {
    compileJava {
        options.release = 16
    }

    jar {
        enabled = false
    }

    shadowJar {
        group = "build"

        archiveClassifier.set("")
        archiveBaseName.set("${project.name}-base")
        destinationDirectory.set(layout.buildDirectory.dir("generated/shadow-base"))

        from(sourceSets.main.get().output)
        from("resources") {
            exclude("plugin.yml")
        }

        configurations = listOf(project.configurations.runtimeClasspath.get())

        minimize()
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
            from("resources") {
                include("plugin.yml")
            }
            into(layout.buildDirectory.dir("generated/resources/$sourceName"))

            val baseProps = mapOf(
                "name" to project.name,
                "version" to project.version.toString(),
                "description" to project.description.orEmpty()
            )
            val props = baseProps + sourceProps + mapOf(
                "source" to sourceName,
                "main" to "${project.group}.${project.name}Main"
            )

            inputs.property("source", sourceName)
            inputs.properties(props)

            expand(props)
        }
    }

    val jarTasks = sources.keys.associateWith { sourceName ->
        register<Jar>("shadowJar${sourceName.replaceFirstChar { it.uppercase() }}") {
            group = "build"

            val resourceTask = resourceTasks.getValue(sourceName)

            dependsOn(shadowJar, resourceTask)

            archiveClassifier.set("")
            destinationDirectory.set(layout.buildDirectory.dir(if(sourceName == "dev") "libs" else "libs/$sourceName"))

            from(zipTree(shadowJar.get().archiveFile)) {
                exclude("META-INF/MANIFEST.MF")
            }
            from(resourceTask)

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