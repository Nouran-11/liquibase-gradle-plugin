package org.liquibase.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.Internal

class ProjectInfo {
    List<Activity> activities
    String runList
    List<String> jvmArgs
    Map<String, Object> liquibaseProperties
    File buildDir
    Object logger

    ProjectInfo(List<Activity> activities, String runList, List<String> jvmArgs, Map<String, Object> liquibaseProperties, File buildDir, Object logger) {
        this.activities = activities
        this.runList = runList
        this.jvmArgs = jvmArgs
        this.liquibaseProperties = liquibaseProperties
        this.buildDir = buildDir
        this.logger = logger
    }

    static ProjectInfo fromProject(Project project) {
        def buildDir = project.buildDir
        def logger = project.logger
        def liquibaseProperties = [:]
        project.properties.findAll { key, value ->
            if (!key.startsWith("liquibase")) return false
            if (value != null && LiquibaseTask.class.isAssignableFrom(value.class)) return false

            //XXX: @Nouran Atef - this ends up ignoring `liquibaseChangelogParameters`
            def supported = ArgumentBuilder.allGlobalProperties.contains(key) ||
                    ArgumentBuilder.allCommandProperties.contains(key)
            if (!supported) {
                println("Skipping unsupported: $key")
            }
            return supported
        }.each { key, value ->
            liquibaseProperties[key] = value
        }
        def activities = project.liquibase.activities.toList()
        def runList = project.liquibase.runList
        def jvmArgs = project.liquibase.jvmArgs
        return new ProjectInfo(activities, runList, jvmArgs,
                liquibaseProperties as Map<String, Object>, buildDir, logger)
    }

    @Internal
    Map<String, String> getChangelogParameters() {
        def changelogParameters = [:]
        if (liquibaseProperties.containsKey("liquibaseChangelogParameters")) {
            def paramString = liquibaseProperties.get("liquibaseChangelogParameters")
            if (paramString instanceof String) {
                paramString.split(",").each { param ->
                    def parts = param.split("=", 2)
                    if (parts.length == 2) {
                        changelogParameters[parts[0].trim()] = parts[1].trim()
                    }
                }
            }
        }
        return changelogParameters as Map<String, String>
    }
}
