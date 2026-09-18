plugins {
    `java-library`
}

dependencies {
    // no external deps at this stage
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Automatic-Module-Name" to "com.zouhmi.zymc.core")
    }
}
