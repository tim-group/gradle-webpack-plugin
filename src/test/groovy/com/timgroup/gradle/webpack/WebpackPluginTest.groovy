package com.timgroup.gradle.webpack

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class WebpackPluginTest extends Specification {
    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "adds npmInstall as a default dependency"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}

tasks.webpack.enabled = false
tasks.npmInstall.enabled = false
"""
        testProjectDir.newFile("package.json") << """
{
}
"""
        testProjectDir.newFile("package-lock.json") << "{}"

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("webpack")
                .withPluginClasspath()
                .build()

        then:
        result.task(":webpack")?.outcome == TaskOutcome.SKIPPED
        result.task(":npmInstall")?.outcome == TaskOutcome.SKIPPED
        result.task(":yarn")?.outcome == null
    }

    def "adds yarn as a dependency if yarn.lock exists"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}

tasks.webpack.enabled = false
tasks.yarn.enabled = false
"""
        testProjectDir.newFile("package.json") << """
{
}
"""
        testProjectDir.newFile("package-lock.json") << "{}"
        testProjectDir.newFile("yarn.lock") << """
"""

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("webpack")
                .withPluginClasspath()
                .build()

        then:
        result.task(":webpack")?.outcome == TaskOutcome.SKIPPED
        result.task(":npmInstall")?.outcome == null
        result.task(":yarn")?.outcome == TaskOutcome.SKIPPED
    }
}
