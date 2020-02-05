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

class MochaTestTaskTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `runs Mocha tests and produces JUnit-style output`() {
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
                "mocha": "2.3.2",
                "mocha-jenkins-reporter": "0.1.9",
                "must": "0.13.1"
              }
            }
        """.trimIndent())

        testProjectDir.newFile("package-lock.json").writeText("{}")
        testProjectDir.newFile("mocha.opts").writeText("""
        """.trimIndent())

        testProjectDir.newFile("src/main/javascript/Thing.js").writeText("""
            
        """.trimIndent());

        testProjectDir.newFile("src/test/javascript/Thing.test.js").writeText("""
            var expect = require("must");
            
            describe("a thing", () => {
                it("works like this", () => {
                    expect(true).to.eql(true);
                });
            });
        """.trimIndent());

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("check")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":mochaTest"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SUCCESS))))
        val mochaXml = testProjectDir.root.resolve("build/test-results/mochaTest/test-reports.xml").readText()
        assertThat(mochaXml, containsSubstring("a thing"))
        assertThat(mochaXml, containsSubstring("works like this"))
        assertThat(mochaXml, containsSubstring("tests=\"1\""))
        assertThat(mochaXml, containsSubstring("failures=\"0\""))
    }

    @Test
    fun `fails the build if a Mocha test fails, but still produces JUnit output`() {
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
                "mocha": "2.3.2",
                "mocha-jenkins-reporter": "0.1.9",
                "must": "0.13.1"
              }
            }
        """.trimIndent())

        testProjectDir.newFile("package-lock.json").writeText("{}")
        testProjectDir.newFile("mocha.opts").writeText("""
        """.trimIndent())

        testProjectDir.newFile("src/main/javascript/Thing.js").writeText("""
            
        """.trimIndent());

        testProjectDir.newFile("src/test/javascript/Thing.test.js").writeText("""
            var expect = require("must");
            
            describe("a thing", () => {
                it("works like this", () => {
                    expect(true).to.eql(false);
                });
            });
        """.trimIndent());

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("check")
                .withPluginClasspath()
                .buildAndFail()

        assertThat(result.task(":mochaTest"), present(has(BuildTask::outcome, equalTo(TaskOutcome.FAILED))))
        val mochaXml = testProjectDir.root.resolve("build/test-results/mochaTest/test-reports.xml").readText()
        assertThat(mochaXml, containsSubstring("a thing"))
        assertThat(mochaXml, containsSubstring("works like this"))
        assertThat(mochaXml, containsSubstring("tests=\"1\""))
        assertThat(mochaXml, containsSubstring("failures=\"1\""))
        assertThat(mochaXml, containsSubstring("true must be equivalent to false"))
    }

    @Test
    fun `ignores failing tests if so configured`() {
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
              id "com.timgroup.webpack"
            }

            mochaTest {
              ignoreFailures = true
            }
        """.trimIndent())

        testProjectDir.newFolder("src", "main", "javascript")
        testProjectDir.newFolder("src", "test", "javascript")

        testProjectDir.newFile("package.json").writeText("""
            {
              "devDependencies": {
                "mocha": "2.3.2",
                "mocha-jenkins-reporter": "0.1.9",
                "must": "0.13.1"
              }
            }
        """.trimIndent())

        testProjectDir.newFile("package-lock.json").writeText("{}")
        testProjectDir.newFile("mocha.opts").writeText("""
        """.trimIndent())

        testProjectDir.newFile("src/main/javascript/Thing.js").writeText("""
            
        """.trimIndent());

        testProjectDir.newFile("src/test/javascript/Thing.test.js").writeText("""
            var expect = require("must");
            
            describe("a thing", () => {
                it("works like this", () => {
                    expect(true).to.eql(false);
                });
            });
        """.trimIndent());

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("check")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":mochaTest"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SUCCESS))))
        val mochaXml = testProjectDir.root.resolve("build/test-results/mochaTest/test-reports.xml").readText()
        assertThat(mochaXml, containsSubstring("a thing"))
        assertThat(mochaXml, containsSubstring("works like this"))
        assertThat(mochaXml, containsSubstring("tests=\"1\""))
        assertThat(mochaXml, containsSubstring("failures=\"1\""))
        assertThat(mochaXml, containsSubstring("true must be equivalent to false"))
    }

    @Test
    fun `skips running tests if source directory does not exist`() {
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
              id "com.timgroup.webpack"
            }
        """.trimIndent())

        testProjectDir.newFolder("src", "main", "javascript")

        testProjectDir.newFile("package.json").writeText("""
            {
              "devDependencies": {
                "mocha": "2.3.2",
                "mocha-jenkins-reporter": "0.1.9",
                "must": "0.13.1"
              }
            }
        """.trimIndent())

        testProjectDir.newFile("package-lock.json").writeText("{}")
        testProjectDir.newFile("mocha.opts").writeText("""
        """.trimIndent())

        testProjectDir.newFile("src/main/javascript/Thing.js").writeText("""
            
        """.trimIndent());

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("check")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":mochaTest"), present(has(BuildTask::outcome, equalTo(TaskOutcome.NO_SOURCE))))
    }

    @Test
    fun `skips running tests if source directory contains no files`() {
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
                "mocha": "2.3.2",
                "mocha-jenkins-reporter": "0.1.9",
                "must": "0.13.1"
              }
            }
        """.trimIndent())

        testProjectDir.newFile("package-lock.json").writeText("{}")
        testProjectDir.newFile("mocha.opts").writeText("""
        """.trimIndent())

        testProjectDir.newFile("src/main/javascript/Thing.js").writeText("""
            
        """.trimIndent());

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("check")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":mochaTest"), present(has(BuildTask::outcome, equalTo(TaskOutcome.NO_SOURCE))))
    }
}
