package com.timgroup.gradle.webpack

import com.moowork.gradle.node.exec.NodeExecRunner
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import java.io.File

open class MochaTestTask : DefaultTask() {
    @get:InputDirectory
    var mainFiles: File? = null
    @get:InputDirectory
    @get:SkipWhenEmpty
    var testFiles: File? = null
    @get:InputFile
    var mochaOptionsFile: File? = null
    @get:OutputFile
    var testOutput: File? = null
    @get:Internal
    var result: ExecResult? = null
    @get:Input
    var ignoreFailures: Boolean = false

    @TaskAction
    fun runTests() {
        val runner = NodeExecRunner(project).apply {
            arguments = listOf("node_modules/mocha/bin/mocha",
                    "--reporter=mocha-jenkins-reporter",
                    "--opts",
                    mochaOptionsFile.toString(),
                    "--recursive",
                    testFiles.toString())

            putenv("JUNIT_REPORT_PATH", testOutput)
            putenv("JUNIT_REPORT_STACK", "1")

            ignoreExitValue = ignoreFailures
        }

        result = runner.execute()
    }
}
