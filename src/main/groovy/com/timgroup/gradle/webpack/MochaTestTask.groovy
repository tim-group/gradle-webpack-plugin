package com.timgroup.gradle.webpack

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.internal.ExecActionFactory

import javax.inject.Inject

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

    void setMainFiles(Object obj) {
        mainFiles = project.file(obj)
    }
    void setTestFiles(Object obj) {
        testFiles = project.file(obj)
    }
    void setMochaOptionsFile(Object obj) {
        mochaOptionsFile = project.file(obj)
    }
    void setTestOutput(Object obj) {
        testOutput = project.file(obj)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException()
    }

    @TaskAction
    void runTests() {
        def execAction = execActionFactory.newExecAction()
        execAction.executable = "node_modules/mocha/bin/mocha"
        execAction.environment("JUNIT_REPORT_PATH", testOutput.toString())
        execAction.environment("JUNIT_REPORT_STACK", "1")
        execAction.args = ["--reporter", "mocha-jenkins-reporter", "--opts", mochaOptionsFile.toString(), "--recursive", testFiles.toString()]
        execAction.ignoreExitValue = ignoreFailures
        execAction.execute()
    }
}
