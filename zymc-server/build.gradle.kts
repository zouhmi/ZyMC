plugins {
    application
}

dependencies {
    implementation(project(":zymc-plugins"))

    val nettyVersion = "4.2.18.Final"
    implementation("io.netty:netty-transport:$nettyVersion")
    implementation("io.netty:netty-transport-native-unix-common:$nettyVersion")
    implementation("io.netty:netty-handler:$nettyVersion")
    implementation("io.netty:netty-codec:$nettyVersion")
}

application {
    mainClass.set("com.zouhmi.zymc.server.ZyMCServer")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Main-Class" to "com.zouhmi.zymc.server.ZyMCServer")
    }
}
