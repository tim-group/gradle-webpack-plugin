package com.timgroup.gradle.webpack

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.present
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WebpackPluginTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `adds npmInstall as a default dependency`() {
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
              id "com.timgroup.webpack"
            }
            
            tasks.webpack.enabled = false
            tasks.npmInstall.enabled = false
        """.trimIndent())

        testProjectDir.newFile("package.json").writeText("{}")
        testProjectDir.newFile("package-lock.json").writeText("{}")

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("webpack")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":webpack"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SKIPPED))))
        assertThat(result.task(":npmInstall"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SKIPPED))))
        assertThat(result.task(":yarn"), absent())
    }

    @Test
    fun `adds yarn as a dependency if yarn lockfile exists`() {
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
              id "com.timgroup.webpack"
            }
            
            tasks.webpack.enabled = false
            tasks.yarn.enabled = false
        """.trimIndent())

        testProjectDir.newFile("package.json").writeText("{}")
        testProjectDir.newFile("package-lock.json").writeText("{}")
        testProjectDir.newFile("yarn.lock").writeText("""
        """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("webpack")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":webpack"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SKIPPED))))
        assertThat(result.task(":npmInstall"), absent())
        assertThat(result.task(":yarn"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SKIPPED))))
    }
}
