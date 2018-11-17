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

    def "runs Jest tests and produces JUnit-style output"() {
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
    "jest": "23.6.0",
    "jest-junit": "5.2.0"
  }
}
"""
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
