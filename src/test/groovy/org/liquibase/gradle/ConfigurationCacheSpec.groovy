package org.liquibase.gradle

import spock.lang.TempDir
import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification


class ConfigurationCacheSpec extends Specification {

    def "plugin should be compatible with configuration cache"() {
        given:
        def testProjectDir = File.createTempDir()
        def buildFile = new File(testProjectDir, "build.gradle")
        buildFile.text = """
            plugins {
                id 'org.liquibase.gradle'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                liquibaseRuntime 'org.liquibase:liquibase-core:4.31.1'
                liquibaseRuntime 'info.picocli:picocli:4.7.5'
                liquibaseRuntime 'org.liquibase:liquibase-groovy-dsl:3.0.3'
                liquibaseRuntime 'com.h2database:h2:2.2.224'
            }

            liquibase {
                activities {
                    main {
                        changelogFile 'src/main/db/changelog.yml'
                        url 'jdbc:h2:mem:testdb'
                        username 'sa'
                        password '1234'
                    }
                }
                runList = 'main'
            }
        """

        def sourceDir = new File(testProjectDir, "src/main/db")
        sourceDir.mkdirs()

        def changelogFile = new File(sourceDir, "changelog.yml")
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
