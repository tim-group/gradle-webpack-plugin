package com.timgroup.gradle.webpack

import com.moowork.gradle.node.exec.NodeExecRunner
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import java.io.File

open class JestTestTask : DefaultTask() {
    @get:InputDirectory
    var mainFiles: File? = null
    @get:InputDirectory
    @get:SkipWhenEmpty
    var testFiles: File? = null
    @get:OutputFile
    var testOutput: File? = null
    @get:Input
    var suiteName: String = "jest tests"
    @get:Internal
    var result: ExecResult? = null
    @get:Input
    var ignoreFailures: Boolean = false

    @TaskAction
    fun runTests() {
        val runner = NodeExecRunner(project).apply {
            arguments = listOf("node_modules/jest/bin/jest",
                    "--ci",
                    "--reporters=default",
                    "--reporters=jest-junit",
                    "--roots=$testFiles")

            // jest-junit 7
            putenv("JEST_JUNIT_OUTPUT", testOutput)
            // jest-junit 8
            putenv("JEST_JUNIT_OUTPUT_DIR", testOutput?.parent)
            putenv("JEST_JUNIT_OUTPUT_NAME", testOutput?.name)

            putenv("JEST_SUITE_NAME", suiteName)

            ignoreExitValue = ignoreFailures
        }

        result = runner.execute()
    }
}
