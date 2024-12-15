description = "Relax with other players on nice seats!"

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.7" apply false
}

allprojects {
    group = "dev.geco.gsit"
    version = "1.12.1"

    repositories {
        mavenLocal()
        mavenCentral()

        maven(url = "https://repo.papermc.io/repository/maven-public/")
        maven(url = "https://hub.spigotmc.org/nexus/content/groups/public/")
        maven(url = "https://maven.enginehub.org/repo/")
        maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven(url = "https://jitpack.io/")
    }

    tasks.withType<JavaCompile>() {
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc>() {
        options.encoding = "UTF-8"
    }
}

dependencies {
    api(project(":core"))
    implementation(project(":v1_17"))
    implementation(project(":v1_17_1"))
    implementation(project(":v1_18"))
    implementation(project(":v1_18_2"))
    implementation(project(":v1_19"))
    implementation(project(":v1_19_1"))
    implementation(project(":v1_19_3"))
    implementation(project(":v1_19_4"))
    implementation(project(":v1_20"))
    implementation(project(":v1_20_2"))
    implementation(project(":v1_20_3"))
    implementation(project(":v1_20_5"))
    implementation(project(":v1_21"))
    implementation(project(":v1_21_2"))
    implementation(project(":v1_21_4"))
}