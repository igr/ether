plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "tudu"
include(
    "app",
    "ether",
    "ether:ether-api",
    "ether:ether-nats"
)
