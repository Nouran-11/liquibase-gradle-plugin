package org.liquibase.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.assertTrue

class ConfigurationCacheSpec {
    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder()

    @Test
    void pluginShouldBeCompatibleWithConfigurationCache() {
        def projectDir = testProjectDir.root
        def changelogFile = new File(projectDir, "changelog.yml")
        def buildFile = new File(projectDir, "build.gradle")
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
                       searchPath "${projectDir}"
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

        def result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments("update", "--configuration-cache","-s")
                .forwardOutput()
                .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
        assertTrue(result.output.contains("Configuration cache entry stored."))
    }
}
