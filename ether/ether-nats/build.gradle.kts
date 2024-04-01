plugins {
    id("dev.oblac.tudu.kotlin-library-conventions")
}

dependencies {
    implementation(project(":ether:ether-api"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    implementation("io.nats:jnats:2.11.0")
    implementation("org.slf4j:slf4j-api:2.0.12")
}
