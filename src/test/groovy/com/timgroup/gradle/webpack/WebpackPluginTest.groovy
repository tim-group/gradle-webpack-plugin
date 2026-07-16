package com.timgroup.gradle.webpack

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

class WebpackPluginTest extends Specification {
    @TempDir
    Path testProjectDir

    def "adds npmInstall as a default dependency"() {
        given:
        testProjectDir.resolve("build.gradle") << """
plugins {
  id 'com.timgroup.webpack'
}

tasks.webpack.enabled = false
tasks.npmInstall.enabled = false
"""
        testProjectDir.resolve("package.json") << """
{
}
"""
        testProjectDir.resolve("package-lock.json") << "{}"

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
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
        testProjectDir.resolve("build.gradle") << """
plugins {
  id 'com.timgroup.webpack'
}

tasks.webpack.enabled = false
tasks.yarn.enabled = false
"""
        testProjectDir.resolve("package.json") << """
{
}
"""
        testProjectDir.resolve("package-lock.json") << "{}"
        testProjectDir.resolve("yarn.lock") << """
"""

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("webpack")
                .withPluginClasspath()
                .build()

        then:
        result.task(":webpack")?.outcome == TaskOutcome.SKIPPED
        result.task(":npmInstall")?.outcome == null
        result.task(":yarn")?.outcome == TaskOutcome.SKIPPED
    }
}
