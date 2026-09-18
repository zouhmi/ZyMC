plugins {
    application
}

dependencies {
    implementation(project(":zymc-plugins"))
}

application {
    mainClass.set("com.zouhmi.zymc.server.ZyMCServer")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Main-Class" to "com.zouhmi.zymc.server.ZyMCServer")
    }
}
