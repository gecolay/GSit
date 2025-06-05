rootProject.name = "GSit"

include(":core")

listOf(
    "v1_17_1",
    "v1_18", "v1_18_2",
    "v1_19", "v1_19_1", "v1_19_3", "v1_19_4",
    "v1_20", "v1_20_2", "v1_20_3", "v1_20_5",
    "v1_21", "v1_21_2", "v1_21_4", "v1_21_5", "v1_21_6"
).forEach {
    include(":$it")
    project(":$it").projectDir = file("mcv/$it")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}