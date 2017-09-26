package com.timgroup.gradle.webpack

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class MochaTestTaskTest extends Specification {
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

        testProjectDir.newFolder("src", "main", "javascript")
        testProjectDir.newFolder("src", "test", "javascript")

        testProjectDir.newFile("package.json") << """
{
  "dependencies": {
    "mocha": "2.3.2",
    "mocha-jenkins-reporter": "0.1.9",
    "must": "0.13.1"
  }
}
"""
        testProjectDir.newFile("mocha.opts") << """
"""
        testProjectDir.newFile("src/main/javascript/Thing.js") << """
"""
        testProjectDir.newFile("src/test/javascript/ThingTest.js") << """
var expect = require("must");

describe("a thing", () => {
    it("works like this", () => {
        expect(true).to.eql(true);
    });
});
"""

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("check")
            .withPluginClasspath()
            .build()

        then:
        result.task(":mochaTest").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/test-results/mochaTest/test-reports.xml").text.contains("a thing")
        new File(testProjectDir.root, "build/test-results/mochaTest/test-reports.xml").text.contains("works like this")
        new File(testProjectDir.root, "build/test-results/mochaTest/test-reports.xml").text.contains("tests=\"1\"")
        new File(testProjectDir.root, "build/test-results/mochaTest/test-reports.xml").text.contains("failures=\"0\"")
    }

    def "fails the build if a Mocha test fails, but still produces JUnit output"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}
"""

        testProjectDir.newFolder("src", "main", "javascript")
        testProjectDir.newFolder("src", "test", "javascript")

        testProjectDir.newFile("package.json") << """
{
  "dependencies": {
    "mocha": "2.3.2",
    "mocha-jenkins-reporter": "0.1.9",
    "must": "0.13.1"
  }
}
"""
        testProjectDir.newFile("mocha.opts") << """
"""
        testProjectDir.newFile("src/main/javascript/Thing.js") << """
"""
        testProjectDir.newFile("src/test/javascript/ThingTest.js") << """
var expect = require("must");

describe("a thing", () => {
    it("works like this", () => {
        expect(true).to.eql(false);
    });
});
"""

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("check")
            .withPluginClasspath()
            .buildAndFail()

        then:
        result.task(":mochaTest").outcome == TaskOutcome.FAILED
        new File(testProjectDir.root, "build/test-results/mochaTest/test-reports.xml").text.contains("a thing")
        new File(testProjectDir.root, "build/test-results/mochaTest/test-reports.xml").text.contains("works like this")
        new File(testProjectDir.root, "build/test-results/mochaTest/test-reports.xml").text.contains("tests=\"1\"")
        new File(testProjectDir.root, "build/test-results/mochaTest/test-reports.xml").text.contains("failures=\"1\"")
        new File(testProjectDir.root, "build/test-results/mochaTest/test-reports.xml").text.contains("true must be equivalent to false")
    }
}
