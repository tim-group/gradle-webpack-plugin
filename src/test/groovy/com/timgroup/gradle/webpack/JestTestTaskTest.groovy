package com.timgroup.gradle.webpack

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

class JestTestTaskTest extends Specification {
    @TempDir
    Path testProjectDir

    def "runs Jest tests and produces JUnit-style output with jest-junit 7"() {
        given:
        testProjectDir.resolve("build.gradle") << """
plugins {
  id 'com.timgroup.webpack'
}
"""

        Files.createDirectories(testProjectDir.resolve("src/main/javascript"))
        Files.createDirectories(testProjectDir.resolve("src/test/javascript"))

        testProjectDir.resolve("package.json") << """
{
  "devDependencies": {
    "jest": "24.9.0",
    "jest-junit": "7.0.0"
  }
}
"""

        testProjectDir.resolve("package-lock.json") << "{}"

        testProjectDir.resolve("src/main/javascript/Thing.js") << """
"""
        testProjectDir.resolve("src/test/javascript/Thing.test.js") << """
describe("a thing", () => {
    it("works like this", () => {
        expect(true).toBe(true);
    });
});
"""

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("check")
                .withPluginClasspath()
                .build()

        then:
        result.task(":jestTest").outcome == TaskOutcome.SUCCESS
        testProjectDir.resolve("build/test-results/jestTest/test-reports.xml").text.contains("a thing")
        testProjectDir.resolve("build/test-results/jestTest/test-reports.xml").text.contains("works like this")
        testProjectDir.resolve("build/test-results/jestTest/test-reports.xml").text.contains("tests=\"1\"")
        testProjectDir.resolve("build/test-results/jestTest/test-reports.xml").text.contains("failures=\"0\"")
    }

    def "runs Jest tests and produces JUnit-style output with jest-junit 8"() {
        given:
        testProjectDir.resolve("build.gradle") << """
plugins {
  id 'com.timgroup.webpack'
}
"""

        Files.createDirectories(testProjectDir.resolve("src/main/javascript"))
        Files.createDirectories(testProjectDir.resolve("src/test/javascript"))

        testProjectDir.resolve("package.json") << """
{
  "devDependencies": {
    "jest": "24.9.0",
    "jest-junit": "8.0.0"
  }
}
"""

        testProjectDir.resolve("package-lock.json") << "{}"

        testProjectDir.resolve("src/main/javascript/Thing.js") << """
"""
        testProjectDir.resolve("src/test/javascript/Thing.test.js") << """
describe("a thing", () => {
    it("works like this", () => {
        expect(true).toBe(true);
    });
});
"""

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("check")
                .withPluginClasspath()
                .build()

        then:
        result.task(":jestTest").outcome == TaskOutcome.SUCCESS
        testProjectDir.resolve("build/test-results/jestTest/test-reports.xml").text.contains("a thing")
        testProjectDir.resolve("build/test-results/jestTest/test-reports.xml").text.contains("works like this")
        testProjectDir.resolve("build/test-results/jestTest/test-reports.xml").text.contains("tests=\"1\"")
        testProjectDir.resolve("build/test-results/jestTest/test-reports.xml").text.contains("failures=\"0\"")
    }
}
