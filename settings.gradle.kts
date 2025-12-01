pluginManagement {
    repositories {
        gradlePluginPortal()
        // Temporary: required because this plugin is not yet published on the Gradle Plugin Portal
        //  https://github.com/EchoEllet/minecraft-safe-resources-gradle/issues/1
        maven { url = uri("https://echoellet.github.io/maven-repo/") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}

rootProject.name = "Epic Fight"
