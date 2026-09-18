pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "zymc"

include("zmc-core")
include("zymc-network")
include("zymc-world")
include("zymc-entity")
include("zymc-game")
include("zymc-plugins")
include("zymc-server")
include("zymc-benchmark")
