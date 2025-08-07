package org.liquibase.gradle

import org.gradle.api.provider.Provider
import org.gradle.api.Project

/**
 * Unified PropertyProvider for both live (Project) and cached (captured values) modes.
 * Ensures configuration cache compatibility and reduces code duplication.
 */
class PropertyProvider {
    // Live mode fields
    private final Project project
    // Cached mode fields
    private final Map<String, Object> capturedLiquibaseProperties
    private final Set<String> allGlobalProperties
    private final Set<String> allCommandProperties
    private final String buildDirPath
    private final Object capturedLogger
    private final boolean isCached

    // Live mode constructor
    PropertyProvider(Project project, Set<String> allGlobalProperties, Set<String> allCommandProperties) {
        this.project = project
        this.allGlobalProperties = allGlobalProperties
        this.allCommandProperties = allCommandProperties
        this.capturedLiquibaseProperties = null
        this.buildDirPath = null
        this.capturedLogger = null
        this.isCached = false
    }

    // Cached mode constructor
    PropertyProvider(Map<String, Object> capturedLiquibaseProperties, Set<String> allGlobalProperties, Set<String> allCommandProperties, String buildDirPath, Object capturedLogger) {
        this.capturedLiquibaseProperties = capturedLiquibaseProperties ?: [:]
        this.allGlobalProperties = allGlobalProperties ?: [] as Set
        this.allCommandProperties = allCommandProperties ?: [] as Set
        this.buildDirPath = buildDirPath
        this.capturedLogger = capturedLogger
        this.project = null
        this.isCached = true
    }

    Provider<Map<String, Object>> getLiquibasePropertiesProvider() {
        if (isCached) {
            return { capturedLiquibaseProperties } as Provider
        } else {
            return project.providers.provider {
                def liquibaseProperties = [:]
                project.properties.findAll { key, value ->
                    if (!key.startsWith("liquibase")) return false
                    if (value != null && LiquibaseTask.class.isAssignableFrom(value.class)) return false
                    return allGlobalProperties.contains(key) || allCommandProperties.contains(key)
                }.each { key, value ->
                    liquibaseProperties[key] = value
                }
                return liquibaseProperties
            }
        }
    }

    Provider<Map<String, String>> getChangelogParametersProvider() {
        if (isCached) {
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
        } else {
            return project.providers.provider {
                def changelogParameters = [:]
                if (project.properties.containsKey("liquibaseChangelogParameters")) {
                    def paramString = project.properties.get("liquibaseChangelogParameters")
                    if (paramString instanceof String) {
                        paramString.split(",").each { param ->
                            def parts = param.split("=", 2)
                            if (parts.length == 2) {
                                changelogParameters[parts[0].trim()] = parts[1].trim()
                            }
                        }
                    }
                }
                return changelogParameters
            }
        }
    }


    def getLogger() {
        if (isCached) {
            return capturedLogger
        }
        return project.logger
    }

    def getBuildDir() {
        if (isCached) return buildDirPath ?: "build"
        return project.buildDir
    }
} 
