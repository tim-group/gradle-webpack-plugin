package com.timgroup.gradle.webpack

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult

open class MochaTestTask : DefaultTask() {
    init {
        group = "verification"
        description = "Runs ths Mocha (JavaScript) tests"
    }

    @get:InputDirectory
    val mainFiles = project.objects.directoryProperty()
    @get:InputDirectory
    @get:SkipWhenEmpty
    val testFiles = project.objects.directoryProperty()
    @get:InputFile
    val mochaOptionsFile = project.objects.fileProperty()
    @get:OutputFile
    val testOutput = project.objects.fileProperty()
    @get:Internal
    var result: ExecResult? = null
    @get:Input
    val ignoreFailures = project.objects.property(Boolean::class.java).convention(false)

    @TaskAction
    fun runTests() {
        result = project.execNode {
            arguments = listOf("node_modules/mocha/bin/mocha",
                    "--reporter=mocha-jenkins-reporter",
                    "--opts",
                    mochaOptionsFile.get().asFile.toString(),
                    "--recursive",
                    testFiles.get().asFile.toString())

            putenv("JUNIT_REPORT_PATH", testOutput.get().asFile)
            putenv("JUNIT_REPORT_STACK", "1")

            ignoreExitValue = ignoreFailures.get()
        }
    }
}
