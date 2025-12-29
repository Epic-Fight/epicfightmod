plugins {
    id("multiloader-base")
    id("me.modmuss50.mod-publish-plugin")
}

val commonProject: Project = project(":common")

dependencies {
    compileOnly(commonProject)
}

// Includes the "common" project's sources and processed resources in this platform project

tasks.compileJava { source(commonProject.sourceSets.main.get().allSource) }

val commonProcessResources = commonProject.tasks.processResources

tasks.processResources {
    dependsOn(commonProcessResources)
    from(commonProcessResources.map { it.outputs.files })
}
