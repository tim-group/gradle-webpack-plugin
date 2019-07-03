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

    def "tasks show up in 'gradle tasks' output"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}
"""
        testProjectDir.newFile("package.json") << """
{
  "devDependencies": {
    "jest": "23.6.0",
    "jest-junit": "5.2.0"
  }
}
"""

        testProjectDir.newFile("package-lock.json") << "{}"

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
        result.output.contains("jestTest")
    }
}
