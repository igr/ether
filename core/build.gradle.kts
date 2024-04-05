plugins {
    id("dev.oblac.tudu.kotlin-library-conventions")
}

dependencies {
    implementation(project(":ether:ether-api"))
    implementation(project(":ether:ether-nats"))
    runtimeOnly("org.slf4j:slf4j-simple:2.0.12")
}
