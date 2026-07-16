package com.timgroup.gradle.webpack

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

class MochaTestTaskTest extends Specification {
    @TempDir
    Path testProjectDir

    def "runs Mocha tests and produces JUnit-style output"() {
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
    "mocha": "2.3.2",
    "mocha-jenkins-reporter": "0.1.9",
    "must": "0.13.1"
  }
}
"""
        testProjectDir.resolve("package-lock.json") << "{}"
        testProjectDir.resolve("mocha.opts") << """
"""
        testProjectDir.resolve("src/main/javascript/Thing.js") << """
"""
        testProjectDir.resolve("src/test/javascript/ThingTest.js") << """
var expect = require("must");

describe("a thing", () => {
    it("works like this", () => {
        expect(true).to.eql(true);
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
        result.task(":mochaTest").outcome == TaskOutcome.SUCCESS
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("a thing")
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("works like this")
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("tests=\"1\"")
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("failures=\"0\"")
    }

    def "fails the build if a Mocha test fails, but still produces JUnit output"() {
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
    "mocha": "2.3.2",
    "mocha-jenkins-reporter": "0.1.9",
    "must": "0.13.1"
  }
}
"""
        testProjectDir.resolve("package-lock.json") << "{}"
        testProjectDir.resolve("mocha.opts") << """
"""
        testProjectDir.resolve("src/main/javascript/Thing.js") << """
"""
        testProjectDir.resolve("src/test/javascript/ThingTest.js") << """
var expect = require("must");

describe("a thing", () => {
    it("works like this", () => {
        expect(true).to.eql(false);
    });
});
"""

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments("check")
            .withPluginClasspath()
            .buildAndFail()

        then:
        result.task(":mochaTest").outcome == TaskOutcome.FAILED
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("a thing")
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("works like this")
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("tests=\"1\"")
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("failures=\"1\"")
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("true must be equivalent to false")
    }

    def "ignores failing tests if so configured"() {
        given:
        testProjectDir.resolve("build.gradle") << """
plugins {
  id 'com.timgroup.webpack'
}

mochaTest {
  ignoreFailures = true
}
"""

        Files.createDirectories(testProjectDir.resolve("src/main/javascript"))
        Files.createDirectories(testProjectDir.resolve("src/test/javascript"))

        testProjectDir.resolve("package.json") << """
{
  "devDependencies": {
    "mocha": "2.3.2",
    "mocha-jenkins-reporter": "0.1.9",
    "must": "0.13.1"
  }
}
"""
        testProjectDir.resolve("package-lock.json") << "{}"
        testProjectDir.resolve("mocha.opts") << """
"""
        testProjectDir.resolve("src/main/javascript/Thing.js") << """
"""
        testProjectDir.resolve("src/test/javascript/ThingTest.js") << """
var expect = require("must");

describe("a thing", () => {
    it("works like this", () => {
        expect(true).to.eql(false);
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
        result.task(":mochaTest").outcome == TaskOutcome.SUCCESS
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("a thing")
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("works like this")
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("tests=\"1\"")
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("failures=\"1\"")
        testProjectDir.resolve("build/test-results/mochaTest/test-reports.xml").text.contains("true must be equivalent to false")
    }

    def "skips running tests if source directory does not exist"() {
        given:
        testProjectDir.resolve("build.gradle") << """
plugins {
  id 'com.timgroup.webpack'
}
"""

        Files.createDirectories(testProjectDir.resolve("src/main/javascript"))

        testProjectDir.resolve("package.json") << """
{
  "devDependencies": {
    "mocha": "2.3.2",
    "mocha-jenkins-reporter": "0.1.9",
    "must": "0.13.1"
  }
}
"""
        testProjectDir.resolve("package-lock.json") << "{}"
        testProjectDir.resolve("mocha.opts") << """
"""
        testProjectDir.resolve("src/main/javascript/Thing.js") << """
"""

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("check")
                .withPluginClasspath()
                .build()

        then:
        result.task(":mochaTest").outcome == TaskOutcome.NO_SOURCE
    }

    def "skips running tests if source directory contains no files"() {
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
    "mocha": "2.3.2",
    "mocha-jenkins-reporter": "0.1.9",
    "must": "0.13.1"
  }
}
"""
        testProjectDir.resolve("package-lock.json") << "{}"
        testProjectDir.resolve("mocha.opts") << """
"""
        testProjectDir.resolve("src/main/javascript/Thing.js") << """
"""

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("check")
                .withPluginClasspath()
                .build()

        then:
        result.task(":mochaTest").outcome == TaskOutcome.NO_SOURCE
    }
}
