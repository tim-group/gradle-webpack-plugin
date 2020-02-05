package com.timgroup.gradle.webpack

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.present
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class JestTestTaskTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `runs Jest tests and produces JUnit-style output with jest-junit 7`() {
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
              id "com.timgroup.webpack"
            }
        """.trimIndent())

        testProjectDir.newFolder("src", "main", "javascript")
        testProjectDir.newFolder("src", "test", "javascript")

        testProjectDir.newFile("package.json").writeText("""
            {
              "devDependencies": {
                "jest": "24.9.0",
                "jest-junit": "7.0.0"
              }
            }
        """.trimIndent())

        testProjectDir.newFile("package-lock.json").writeText("{}")

        testProjectDir.newFile("src/main/javascript/Thing.js").writeText("""
            
        """.trimIndent());

        testProjectDir.newFile("src/test/javascript/Thing.test.js").writeText("""
            describe("a thing", () => {
                it("works like this", () => {
                    expect(true).toBe(true);
                });
            });
        """.trimIndent());

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("check")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":jestTest"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SUCCESS))))
        val junitXml = testProjectDir.root.resolve("build/test-results/jestTest/test-reports.xml").readText()
        assertThat(junitXml, containsSubstring("a thing"))
        assertThat(junitXml, containsSubstring("works like this"))
        assertThat(junitXml, containsSubstring("tests=\"1\""))
        assertThat(junitXml, containsSubstring("failures=\"0\""))
    }

    @Test
    fun `runs Jest tests and produces JUnit-style output with jest-junit 8`() {
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
              id "com.timgroup.webpack"
            }
        """.trimIndent())

        testProjectDir.newFolder("src", "main", "javascript")
        testProjectDir.newFolder("src", "test", "javascript")

        testProjectDir.newFile("package.json").writeText("""
            {
              "devDependencies": {
                "jest": "24.9.0",
                "jest-junit": "8.0.0"
              }
            }
        """.trimIndent())

        testProjectDir.newFile("package-lock.json").writeText("{}")

        testProjectDir.newFile("src/main/javascript/Thing.js").writeText("""
            
        """.trimIndent());

        testProjectDir.newFile("src/test/javascript/Thing.test.js").writeText("""
            describe("a thing", () => {
                it("works like this", () => {
                    expect(true).toBe(true);
                });
            });
        """.trimIndent());

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("check")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":jestTest"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SUCCESS))))
        val junitXml = testProjectDir.root.resolve("build/test-results/jestTest/test-reports.xml").readText()
        assertThat(junitXml, containsSubstring("a thing"))
        assertThat(junitXml, containsSubstring("works like this"))
        assertThat(junitXml, containsSubstring("tests=\"1\""))
        assertThat(junitXml, containsSubstring("failures=\"0\""))
    }
}
