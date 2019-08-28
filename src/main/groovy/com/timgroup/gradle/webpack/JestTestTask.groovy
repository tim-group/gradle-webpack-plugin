package com.timgroup.gradle.webpack

import com.moowork.gradle.node.exec.NodeExecRunner
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult

class JestTestTask extends DefaultTask implements VerificationTask {
    @InputDirectory
    def mainFiles
    @InputDirectory @SkipWhenEmpty
    def testFiles
    @OutputFile
    def testOutput
    @Input
    boolean ignoreFailures
    @Input
    String suiteName = "jest tests"
    @Internal
    ExecResult result

    File getMainFiles() {
        return project.file(mainFiles)
    }
    File getTestFiles() {
        return project.file(testFiles)
    }
    File getTestOutput() {
        return project.file(testOutput)
    }

    @TaskAction
    void runTests() {
        def runner = new NodeExecRunner( this.project )
        def execArgs = ["node_modules/jest/bin/jest", "--ci", "--reporters=default", "--reporters=jest-junit", "--roots=" + testFiles.toString()]
        runner.arguments = execArgs
        // jest-junit 7
        runner.environment.put("JEST_JUNIT_OUTPUT", testOutput.toString())
        // jest-junit 8
        runner.environment.put("JEST_JUNIT_OUTPUT_DIR", getTestOutput().parent)
        runner.environment.put("JEST_JUNIT_OUTPUT_NAME", getTestOutput().name)
        if (suiteName != null)
            runner.environment.put("JEST_SUITE_NAME", suiteName)
        runner.ignoreExitValue = ignoreFailures
        result = runner.execute()
    }
}
