
plugins {
    kotlin("jvm")
    application
}

group = "com.sanop"
version = "0.1-dev"


dependencies {
    val ktor_version = "2.0.0"

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("io.ktor:ktor-network:2.0.0")
    implementation("io.ktor:ktor-server-core:2.0.0") // 기본 Ktor 서버 기능
    implementation("io.ktor:ktor-server-netty:2.0.0") // Netty 엔진
    implementation("io.ktor:ktor-serialization:2.0.0")
    implementation("io.ktor:ktor-serialization-jackson:2.0.0")
    implementation("io.ktor:ktor-server-content-negotiation:2.0.0")
    implementation("io.ktor:ktor-server-status-pages:2.0.0")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
    implementation("io.ktor:ktor-client-serialization:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
}


application {
    mainClass.set("MainKt")
}