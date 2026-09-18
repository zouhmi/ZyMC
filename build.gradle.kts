allprojects {
    group = "com.zouhmi.zymc"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.withType<JavaPlugin>().configureEach {
        val javaExt = project.the<JavaPluginExtension>()
        javaExt.toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        javaExt.withSourcesJar()
        javaExt.withJavadocJar()

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf(
                "-Xlint:all",
                "-Xlint:-serial",
                "-Xlint:-exports"
            ))
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }

        project.dependencies.add("testImplementation", "org.junit.jupiter:junit-jupiter:5.11.0")
        project.dependencies.add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }
}
