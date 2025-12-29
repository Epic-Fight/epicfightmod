plugins {
    id("multiloader-platform")
    alias(libs.plugins.fabric.loom)
}

configureBaseArchive("fabric")

repositories {
    parchmentMcRepository()
    terraformersRepository()
}

dependencies {
    minecraft(libs.fabric.minecraft)
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${mcVersion}:${parchmentMc}@zip")
    })
    modImplementation(libs.fabric.loader)

    // Fabric API
    modImplementation(libs.fabric.api)
    modImplementation(libs.modmenu.fabric)

    modImplementation(libs.forgeconfigapiport.fabric) {
        exclude(group = libs.fabric.loader.get().group)
        exclude(group = libs.fabric.api.get().group)
    }
}

loom {
    // Removed due to lack of NeoForge support: https://github.com/neoforged/ModDevGradle/issues/3 and https://neoforged.net/news/21.5release/#split-sourcesets
//    splitEnvironmentSourceSets()

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
            // Removed due to lack of NeoForge support: https://github.com/neoforged/ModDevGradle/issues/3 and https://neoforged.net/news/21.5release/#split-sourcesets
//            sourceSet(sourceSets.named("client").get())
        }
    }
}

tasks.processResources {
    val replaceProperties = modPlatformMetadataReplaceProperties

    inputs.properties(replaceProperties)

    filesMatching("fabric.mod.json") {
        expand(replaceProperties)
    }
}

configureModPublish(ModLoader.Fabric) { tasks.remapJar.get().archiveFile }
