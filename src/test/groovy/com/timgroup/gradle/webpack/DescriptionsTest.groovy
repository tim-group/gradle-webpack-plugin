package com.timgroup.gradle.webpack

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

class DescriptionsTest extends Specification {
    @TempDir
    Path testProjectDir

    def "tasks show up in 'gradle tasks' output"() {
        given:
        testProjectDir.resolve('build.gradle') << """
plugins {
  id 'com.timgroup.webpack'
}
"""
        testProjectDir.resolve("package.json") << """
{
  "devDependencies": {
    "jest": "23.6.0",
    "jest-junit": "5.2.0"
  }
}
"""

        testProjectDir.resolve("package-lock.json") << "{}"

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
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
