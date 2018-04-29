package com.timgroup.gradle.webpack

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.internal.ExecActionFactory

import javax.inject.Inject

class MochaTestTask extends DefaultTask implements VerificationTask {
    @InputDirectory
    def mainFiles
    @InputDirectory @SkipWhenEmpty
    def testFiles
    @InputFile
    def mochaOptionsFile
    @OutputFile
    def testOutput
    @Input
    boolean ignoreFailures
    @Input @Optional
    final Property<String> nodeVersion = project.objects.property(String)

    File getMainFiles() {
        return project.file(mainFiles)
    }
    File getTestFiles() {
        return project.file(testFiles)
    }
    File getMochaOptionsFile() {
        return project.file(mochaOptionsFile)
    }
    File getTestOutput() {
        return project.file(testOutput)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException()
    }

    @TaskAction
    void runTests() {
        def execAction = execActionFactory.newExecAction()
        execAction.executable = new NodeVersion(nodeVersion, project, execActionFactory).nodeExecutable
        execAction.environment("JUNIT_REPORT_PATH", testOutput.toString())
        execAction.environment("JUNIT_REPORT_STACK", "1")
        execAction.args = ["node_modules/mocha/bin/mocha", "--reporter", "mocha-jenkins-reporter", "--opts", mochaOptionsFile.toString(), "--recursive", testFiles.toString()]
        execAction.ignoreExitValue = ignoreFailures
        execAction.execute()
    }
}
