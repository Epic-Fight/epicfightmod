// Shared utilities between all Gradle projects or plugins.

import me.modmuss50.mpp.ModPublishExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.provider.Provider
import java.net.URI

// Note: Prefer "strictMaven()" over "maven { ... }" to reduce unnecessary repository lookups (i.e., reduce network calls)
fun RepositoryHandler.strictMaven(name: String, url: String, vararg includeGroups: String) {
    exclusiveContent {
        forRepository { maven { this.name = name; this.url = URI(url) } }
        filter { includeGroups.forEach { includeGroup(it) } }
    }
}

const val generationTaskGroup = "generation"

private val Project.versionCatalog: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

private fun Project.catalogVersion(name: String): String = versionCatalog.findVersion(name).get().toString()

private fun Project.gradleProperty(name: String): String {
    return this.providers.gradleProperty(name).get()
}

val Project.modVersion: String
    get() = catalogVersion("mod")

private val Project.modLicense: String
    get() = gradleProperty("license")

val Project.modId: String
    get() = gradleProperty("mod_id")

val Project.mcVersion: String
    get() = catalogVersion("minecraft")

val Project.parchmentMc: String
    get() = catalogVersion("parchmentMappings")

val Project.javaVersion: Int
    get() = catalogVersion("java").toInt()

private val Project.modIssueTracker: String
    get() = gradleProperty("issue_tracker")

private val Project.modDisplayName: String
    get() = gradleProperty("display_name")

private val Project.modDisplayUrl: String
    get() = gradleProperty("display_url")

private val Project.modSourceCode: String
    get() = gradleProperty("source_code")

private val Project.modDescription: String
    get() = gradleProperty("description")

private val Project.modCredits: String
    get() = gradleProperty("credits")

private val Project.modAuthors: String
    get() = gradleProperty("authors")

val Project.groupId: String
    get() = gradleProperty("group_id")

private val Project.neoForgeVersion: String
    get() = catalogVersion("neoforge")

/**
 * Configures the JAR file names to be unique, clear, and consistent.
 */
fun Project.configureBaseArchive(variant: String) {
    extensions.getByType(BasePluginExtension::class.java).apply {
        // "epic-fight" is intentionally being used instead of the mod ID (i.e., modId),
        // to keep the JAR file name consistent with the mod project slug URL,
        // and therefore Modrinth will automatically download the sources JAR file: https://support.modrinth.com/en/articles/8801191-modrinth-maven#h_1b24106498
        // This workaround is not needed if the mod ID matches the project slug.
        archivesName.set("epic-fight")
        version = getFullModVersion(variant)
    }
}

private fun Project.getFullModVersion(variant: String): String = "${modVersion}-mc${mcVersion}-$variant"

enum class ModLoader(val conventionalName: String) {
    NeoForge("neoforge"),
    ;

    val isForgeLike: Boolean
        get() = this == NeoForge
}

private fun Project.extractCurrentVersionChangelog(): String? {
    val changelogFile = rootProject.file("CHANGELOG.md")
    val fullChangelogText = changelogFile.readText()

    // Extracts the current version changelog without the version heading 2 and "For Devs" heading 3.
    val versionSectionRegex =
        "(?s)## \\[$modVersion\\] - \\d{4}-\\d{2}-\\d{2}\\R(.*?)(?=\\R### For Devs|\\R## \\[.*?\\] |\\Z)"
    val matcher = Regex(versionSectionRegex).find(fullChangelogText) ?: return null

    val versionChangelog = matcher.groupValues[1]
    return versionChangelog
}

private fun Project.buildReleaseChangelog(
    currentVersionChangelog: String,
    modLoader: ModLoader,
): String {
    return buildString {
        append(currentVersionChangelog)
        appendLine(); appendLine()

        val modLoaderName = modLoader.name
        val modLoaderVersion: String = when (modLoader) {
            ModLoader.NeoForge -> neoForgeVersion
        }

        append(
            """
    ### **Tested against:**
    - **Minecraft:** $mcVersion
    - **$modLoaderName:** $modLoaderVersion
    """.trimIndent()
        )
    }
}

/**
 * Configures the mod publishing to mod sites (e.g., Modrinth, CurseForge).
 */
fun Project.configureModPublish(
    modLoader: ModLoader,
    jarFile: () -> Provider<RegularFile>,
) {
    val project = this
    project.tasks.named("publishMods") {
        doLast {
            if (extractCurrentVersionChangelog() == null) {
                throw GradleException("Cannot run publishMods task: No changelog found for version $modVersion in CHANGELOG.md file")
            }
        }
    }
    extensions.getByType(ModPublishExtension::class.java).apply {
        // Assumes `java { withSourcesJar() }` is called.
        val sourcesJar = project.tasks.named("sourcesJar")

        dryRun.set(false)

        val latestChangelog = extractCurrentVersionChangelog() ?: run {
            logger.warn("Skipping the publishMods configuration: No changelog found for version $modVersion in CHANGELOG.md file")
            return@apply
        }
        val releaseChangelog = project.buildReleaseChangelog(latestChangelog, modLoader)
        changelog.set(releaseChangelog)

        modLoaders.add(modLoader.conventionalName)
        type.set(STABLE)
        displayName.set(project.getFullModVersion(modLoader.conventionalName))
        file.set(jarFile())
        additionalFiles.from(sourcesJar)

        val requiredDependencies = emptyList<String>()
        val optionalDependencies = emptyList<String>()

        curseforge {
            accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
            projectId.set("405076")
            minecraftVersions.add(mcVersion)
            projectSlug.set("epic-fight-mod")

            requiredDependencies.forEach { requires(it) }
            optionalDependencies.forEach { optional(it) }
        }

        modrinth {
            accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
            projectId.set("vu3NZ5Ma")
            minecraftVersions.add(mcVersion)

            requiredDependencies.forEach { requires(it) }
            optionalDependencies.forEach { optional(it) }
        }

        discord {
            webhookUrl.set(providers.environmentVariable("DISCORD_WEBHOOK"))
            dryRunWebhookUrl.set(providers.environmentVariable("DRY_RUN_DISCORD_WEBHOOK"))
            username.set("Update Notification")
            avatarUrl.set("https://i.imgur.com/FrxDviN.png")
            content.set(
                changelog.map {
                    buildString {
                        appendLine("<@&1074034800849059930>")
                        appendLine("# Epic Fight $modVersion is out!")
                        appendLine(releaseChangelog)
                    }
                }
            )

            style {
                look.set("MODERN")
                link.set("EMBED")
                thumbnailUrl.set("https://i.imgur.com/nI8xOCy.png")
            }
        }
    }
}

/**
 * The placeholders to be replaced with in NeoForge mod metadata (neoforge.mods.toml).
 *
 * ### **Example:**
 *
 * ```toml
 * modId = "${modId}"
 * ```
 *
 * Gradle projects are expected to use this in `tasks.processResources {}` configuration,
 * to replace the properties.
 *
 * This is done to avoid hardcoding the values directly in these resource files, and allows sharing them
 * in "gradle.properties".
 */
val Project.modPlatformMetadataReplaceProperties: Map<String, Any>
    get() = mapOf(
        "modId" to modId,
        "modVersion" to modVersion,
        "license" to modLicense,
        "issueTracker" to modIssueTracker,
        "displayName" to modDisplayName,
        "displayUrl" to modDisplayUrl,
        "description" to modDescription,
        "authors" to modAuthors,
        "credits" to modCredits,
        "sourceCode" to modSourceCode,
        "minecraft" to mcVersion,
        "neoforgeVersion" to neoForgeVersion,
        "javaVersion" to javaVersion,
        "forgeconfigapiportVersion" to catalogVersion("forgeconfigapiport"),
    )
