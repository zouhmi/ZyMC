plugins {
    `java-library`
}

dependencies {
    api(project(":zmc-core"))

    val nettyVersion = "4.2.18.Final"
    implementation("io.netty:netty-buffer:$nettyVersion")
    implementation("io.netty:netty-transport:$nettyVersion")
    implementation("io.netty:netty-transport-native-unix-common:$nettyVersion")
    implementation("io.netty:netty-handler:$nettyVersion")
    implementation("io.netty:netty-codec:$nettyVersion")
}
