package com.timgroup.gradle.webpack

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.contains
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.present
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WebpackTaskTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `compiles javascript files with webpack, compresses outputs and produces manifest with SHA-256 digests`() {
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
              id "com.timgroup.webpack"
            }
        """.trimIndent())

        typicalJavascriptSetup()

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("assemble")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":webpack"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SUCCESS))))
        assertThat(testProjectDir.root.resolve("build/site").list()?.toSet(), equalTo(setOf("main.js", "main.js.map", "main.js.gz", "main.js.map.gz", ".MANIFEST")))
        assertThat(testProjectDir.root.resolve("build/site/.MANIFEST").readText(), contains(Regex("^[0-9a-f]{64} main\\.js \\d+ \\d+", RegexOption.MULTILINE)))
    }

    @Test
    fun `resource gzipping can be disabled`() {
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
              id "com.timgroup.webpack"
            }
            
            webpack {
              gzipResources = false
            }
        """.trimIndent())

        typicalJavascriptSetup()

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("assemble")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":webpack"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SUCCESS))))
        assertThat(testProjectDir.root.resolve("build/site").list()?.toSet(), present(!hasElement("main.js.gz")))
    }

    @Test
    fun `manifest generation can be disabled`() {
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
              id "com.timgroup.webpack"
            }
            
            webpack {
              generateManifest = false
            }
        """.trimIndent())

        typicalJavascriptSetup()

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("assemble")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":webpack"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SUCCESS))))
        assertThat(testProjectDir.root.resolve("build/site").list()?.toSet(), present(!hasElement(".MANIFEST")))
    }

    @Test
    fun `build fails if webpack config file is missing`() {
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
              id "com.timgroup.webpack"
            }
        """.trimIndent())

        typicalJavascriptSetup()
        testProjectDir.root.resolve("webpack.config.js").delete()

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("assemble")
                .withPluginClasspath()
                .buildAndFail()

        assertThat(result.task(":webpack"), present(has(BuildTask::outcome, equalTo(TaskOutcome.FAILED))))
    }

    @Test
    fun `task uses alternative configuration file when specified`() {
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
              id "com.timgroup.webpack"
            }
            
            webpack {
              configFile = file("webpack.alt.config.js")
            }
        """.trimIndent())

        typicalJavascriptSetup()
        testProjectDir.root.resolve("webpack.config.js").renameTo(testProjectDir.root.resolve("webpack.alt.config.js"))

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("assemble")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":webpack"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SUCCESS))))
    }

    private fun typicalJavascriptSetup() {
        testProjectDir.newFolder("src", "main", "javascript")

        testProjectDir.newFile("package.json").writeText("""
            {
              "dependencies": {
                "webpack": "3.6.0"
              }
            }
        """.trimIndent())

        testProjectDir.newFile("package-lock.json").writeText("{}")

        testProjectDir.newFile("webpack.config.js").writeText("""
            var webpack = require("webpack");
            var path = require('path');
            function relative(suffix) {
                return path.resolve(__dirname, suffix);
            }
            module.exports = {
              context: relative("src/main/javascript"),
              output: {
                filename: "[name].js",
                path: relative("build/site")
              },
              entry: "./init"
            };
        """.trimIndent())

        testProjectDir.newFile("src/main/javascript/init.js").writeText("""
            // this is init.js
        """.trimIndent())
    }
}
