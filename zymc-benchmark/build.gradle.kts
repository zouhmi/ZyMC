plugins {
    application
}

dependencies {
    implementation(project(":zymc-server"))
    implementation(project(":zymc-network"))
}

application {
    mainClass.set("com.zouhmi.zymc.benchmark.BenchmarkHarness")
}
