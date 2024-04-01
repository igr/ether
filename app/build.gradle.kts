plugins {
    id("dev.oblac.tudu.kotlin-application-conventions")
}

dependencies {
    implementation(project(":ether:ether-api"))
    implementation(project(":ether:ether-nats"))

    implementation("io.vertx:vertx-core:4.5.7")
    implementation("io.vertx:vertx-web:4.5.7")
    implementation("io.vertx:vertx-lang-kotlin:4.5.7")
    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.68.Final:osx-aarch_64")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.12")
}

val mainVerticleName = "dev.oblac.tudu.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

application {
    mainClass.set(launcherClassName)
}


tasks.withType<JavaExec> {
    args = listOf("run", mainVerticleName, "--launcher-class=$launcherClassName")
}
