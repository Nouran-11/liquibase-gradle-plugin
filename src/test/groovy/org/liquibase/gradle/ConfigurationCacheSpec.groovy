package org.liquibase.gradle

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
                liquibaseRuntime 'com.h2database:h2:2.2.224'
            }

            liquibase {
                activities {
                    main {
                        changelogFile 'changelog.groovy'
                        url 'jdbc:h2:mem:testdb'
                        username 'sa'
                        password ''
                    }
                }
                runList = 'main'
            }
        """

        new File(testProjectDir, "changelog.groovy").text = """
            databaseChangeLog {
                changeSet(id: '1', author: 'test') {
                    createTable(tableName: 'person') {
                        column(name: 'id', type: 'int')
                    }
                }
            }
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


