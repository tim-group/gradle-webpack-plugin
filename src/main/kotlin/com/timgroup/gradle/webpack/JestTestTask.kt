package com.timgroup.gradle.webpack

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult

open class JestTestTask : DefaultTask() {
    init {
        group = "verification"
        description = "Runs ths Jest (JavaScript) tests"
    }

    @get:InputDirectory
    val mainFiles = project.objects.directoryProperty()
    @get:InputDirectory
    @get:SkipWhenEmpty
    val testFiles = project.objects.directoryProperty()
    @get:OutputFile
    val testOutput = project.objects.fileProperty()
    @get:Input
    val suiteName = project.objects.property(String::class.java).convention("jest tests")
    @get:Internal
    var result: ExecResult? = null
    @get:Input
    val ignoreFailures = project.objects.property(Boolean::class.java).convention(false)

    @TaskAction
    fun runTests() {
        result = project.execNode {
            arguments = listOf("node_modules/jest/bin/jest",
                    "--ci",
                    "--reporters=default",
                    "--reporters=jest-junit",
                    "--roots=${testFiles.get().asFile}")

            // jest-junit 7
            putenv("JEST_JUNIT_OUTPUT", testOutput.get().asFile)
            // jest-junit 8
            putenv("JEST_JUNIT_OUTPUT_DIR", testOutput.get().asFile.parent)
            putenv("JEST_JUNIT_OUTPUT_NAME", testOutput.get().asFile.name)

            putenv("JEST_SUITE_NAME", suiteName.get())

            ignoreExitValue = ignoreFailures.get()
        }
    }
}
