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

class DescriptionsTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `tasks show up in 'gradle tasks' output`() {
        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
              id "com.timgroup.webpack"
            }
        """.trimIndent())

        testProjectDir.newFile("package.json").writeText("""
            {
              "devDependencies": {
                "jest": "23.6.0",
                "jest-junit": "5.2.0"
              }
            }
        """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("tasks")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":tasks"), present(has(BuildTask::outcome, equalTo(TaskOutcome.SUCCESS))))
        assertThat(result.output, containsSubstring("npmInstall"))
        assertThat(result.output, containsSubstring("webpack"))
        assertThat(result.output, containsSubstring("mochaTest"))
        assertThat(result.output, containsSubstring("jestTest"))
    }
}
