package com.timgroup.gradle.webpack

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class JestTestTaskTest extends Specification {
    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "runs Jest tests and produces JUnit-style output with jest-junit 7"() {
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
  "devDependencies": {
    "jest": "24.9.0",
    "jest-junit": "7.0.0"
  }
}
"""

        testProjectDir.newFile("package-lock.json") << "{}"

        testProjectDir.newFile("src/main/javascript/Thing.js") << """
"""
        testProjectDir.newFile("src/test/javascript/Thing.test.js") << """
describe("a thing", () => {
    it("works like this", () => {
        expect(true).toBe(true);
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
        result.task(":jestTest").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/test-results/jestTest/test-reports.xml").text.contains("a thing")
        new File(testProjectDir.root, "build/test-results/jestTest/test-reports.xml").text.contains("works like this")
        new File(testProjectDir.root, "build/test-results/jestTest/test-reports.xml").text.contains("tests=\"1\"")
        new File(testProjectDir.root, "build/test-results/jestTest/test-reports.xml").text.contains("failures=\"0\"")
    }

    def "runs Jest tests and produces JUnit-style output with jest-junit 8"() {
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
  "devDependencies": {
    "jest": "24.9.0",
    "jest-junit": "8.0.0"
  }
}
"""

        testProjectDir.newFile("package-lock.json") << "{}"

        testProjectDir.newFile("src/main/javascript/Thing.js") << """
"""
        testProjectDir.newFile("src/test/javascript/Thing.test.js") << """
describe("a thing", () => {
    it("works like this", () => {
        expect(true).toBe(true);
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
        result.task(":jestTest").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/test-results/jestTest/test-reports.xml").text.contains("a thing")
        new File(testProjectDir.root, "build/test-results/jestTest/test-reports.xml").text.contains("works like this")
        new File(testProjectDir.root, "build/test-results/jestTest/test-reports.xml").text.contains("tests=\"1\"")
        new File(testProjectDir.root, "build/test-results/jestTest/test-reports.xml").text.contains("failures=\"0\"")
    }
}
