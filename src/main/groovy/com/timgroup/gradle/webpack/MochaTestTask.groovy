package com.timgroup.gradle.webpack

import com.moowork.gradle.node.exec.NodeExecRunner
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult

class MochaTestTask extends DefaultTask implements VerificationTask {
    @InputDirectory
    File mainFiles
    @InputDirectory @SkipWhenEmpty
    File testFiles
    @InputFile
    File mochaOptionsFile
    @OutputFile
    File testOutput
    @Input
    boolean ignoreFailures
    @Internal
    ExecResult result

    @TaskAction
    void runTests() {
        def runner = new NodeExecRunner( this.project )
        def execArgs = ["node_modules/mocha/bin/mocha", "--reporter", "mocha-jenkins-reporter", "--opts", mochaOptionsFile.toString(), "--recursive", testFiles.toString()]
        runner.arguments = execArgs
        runner.environment.put("JUNIT_REPORT_PATH", testOutput.toString())
        runner.environment.put("JUNIT_REPORT_STACK", "1")
        runner.ignoreExitValue = ignoreFailures
        result = runner.execute()
    }
}
