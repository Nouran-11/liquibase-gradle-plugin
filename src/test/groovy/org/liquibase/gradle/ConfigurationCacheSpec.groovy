package org.liquibase.gradle

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

class ConfigurationCacheSpec extends Specification {
    @TempDir
    File testProjectDir

    def "plugin should be compatible with configuration cache"() {
        given:
        def changelogFile = new File(testProjectDir, "changelog.yml")
        def buildFile = new File(testProjectDir, "build.gradle")
        buildFile.text = """
            plugins {
                id 'org.liquibase.gradle'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                liquibaseRuntime 'org.liquibase:liquibase-core:4.26.0'
                liquibaseRuntime 'info.picocli:picocli:4.7.5'
                liquibaseRuntime 'com.h2database:h2:2.2.224'
            }

            liquibase {
                activities {
                    main {
                        changelogFile 'changelog.yml'
                        url 'jdbc:h2:mem:testdb'
                        username 'sa'
                        password '1234'
                        searchPath '${testProjectDir}'
                    }
                }
                runList = 'main'
            }
        """

        changelogFile.text = """
            databaseChangeLog:
              - changeSet:
                  id: 1
                  author: your.name
                  changes:
                    - createTable:
                        tableName: person
                        columns:
                          - column:
                              name: id
                              type: int
                              autoIncrement: true
                              constraints:
                                primaryKey: true
                                nullable: false
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .withArguments("update", "--configuration-cache","-s")
                .forwardOutput()
                .build()

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.output.contains("Configuration cache entry stored.")
    }
}
