package org.liquibase.gradle

import org.gradle.api.provider.Provider

class ProjectInfo{
    private final Map<String, Object> capturedLiquibaseProperties
    private final String buildDirPath
    private final Object capturedLogger

    ProjectInfo(Map<String, Object> capturedLiquibaseProperties, String buildDirPath, Object capturedLogger) {
        this.capturedLiquibaseProperties = capturedLiquibaseProperties ?: [:]
        this.buildDirPath = buildDirPath
        this.capturedLogger = capturedLogger
    }

    Provider<Map<String, Object>> getLiquibaseProperties() {
        return { capturedLiquibaseProperties } as Provider
    }

    Provider<Map<String, String>> getChangelogParameters() {
        def changelogParameters = [:]
        if (capturedLiquibaseProperties.containsKey("liquibaseChangelogParameters")) {
            def paramString = capturedLiquibaseProperties.get("liquibaseChangelogParameters")
            if (paramString instanceof String) {
                paramString.split(",").each { param ->
                    def parts = param.split("=", 2)
                    if (parts.length == 2) {
                        changelogParameters[parts[0].trim()] = parts[1].trim()
                    }
                }
            }
        }
        return { changelogParameters } as Provider
    }


    def getLogger() {
        return capturedLogger
    }

    def getBuildDir() {
        return buildDirPath
    }
} 
