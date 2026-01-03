plugins {
    id("multiloader-base")
    alias(libs.plugins.moddevgradle)
    alias(libs.plugins.mcSafeResources)
}

configureBaseArchive("common")

// Note: Although this is called "neoforge {}", it does NOT apply NeoForge extensions
// or APIs to this Gradle project.
// This configures the ModDevGradle plugin by NeoForge to enable vanilla-mode (NeoForm),
// which provides access only to vanilla Minecraft classes, which can be reused outside NeoForge.
//
// NeoForge patches vanilla Minecraft classes: https://github.com/neoforged/NeoForge/tree/HEAD/patches
// however, this will compile against the original classes to load on non-Forge-like platforms.
//
// See also:
// * https://github.com/neoforged/ModDevGradle#vanilla-mode
// * https://github.com/neoforged/NeoForm
neoForge {
    neoFormVersion = libs.versions.neoform.get()

    parchment {
        mappingsVersion.set(libs.versions.parchmentMappings.get())
        minecraftVersion.set(libs.versions.minecraft.get())
    }
}

dependencies {
    compileOnly(libs.forgeconfigapiport.common) // Required in non-Forge like platforms
    compileOnly(libs.mixin)
}

mcSafeResources {
    namespace.set(modId)
    outputPackage.set("yesman.${modId}.generated")
}

java.sourceSets.main.get().java.srcDirs(
    tasks.generateLangKeys.map { it.outputs.files.singleFile },
    tasks.generateSoundKeys.map { it.outputs.files.singleFile }
)

tasks.compileJava { dependsOn(tasks.generateLangKeys, tasks.generateSoundKeys) }

// The API JAR file includes only classes from the API package.

val apiPackage = "yesman/epicfight/api/**"
val apiJarClassifier = "api"

val apiJar by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier.set(apiJarClassifier)

    from(sourceSets.main.get().output) { include(apiPackage) }
}

val apiSourcesJar by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier.set("$apiJarClassifier-sources") // Important to use this suffix, to follow standards

    from(sourceSets.main.get().allSource) { include(apiPackage) }
}

artifacts {
    archives(apiJar)
    archives(apiSourcesJar)
}
