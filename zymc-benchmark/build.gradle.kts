plugins {
    application
}

dependencies {
    implementation(project(":zymc-server"))
    implementation(project(":zymc-network"))
    implementation(project(":zymc-world"))
    implementation(project(":zmc-core"))

    val nettyVersion = "4.2.18.Final"
    implementation("io.netty:netty-buffer:$nettyVersion")
    implementation("io.netty:netty-transport:$nettyVersion")
    implementation("io.netty:netty-transport-native-unix-common:$nettyVersion")
    implementation("io.netty:netty-handler:$nettyVersion")
    implementation("io.netty:netty-codec:$nettyVersion")
}

application {
    mainClass.set("com.zouhmi.zymc.benchmark.BenchmarkHarness")
}
