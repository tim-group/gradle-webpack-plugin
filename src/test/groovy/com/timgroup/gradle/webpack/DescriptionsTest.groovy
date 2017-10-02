package com.timgroup.gradle.webpack

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class DescriptionsTest extends Specification {
    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "runs Mocha tests and produces JUnit-style output"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}
"""

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("tasks")
            .withPluginClasspath()
            .build()

        then:
        result.task(":tasks").outcome == TaskOutcome.SUCCESS
        result.output.contains("npmInstall")
        result.output.contains("webpack")
        result.output.contains("mochaTest")
    }
}
