package com.timgroup.gradle.webpack;

import java.io.File;
import java.util.Arrays;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.VerificationTask;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;

public class MochaTestTask extends DefaultTask implements VerificationTask {
    private File mainFiles;
    private File testFiles;
    private File testOutput;
    private File mochaOptionsFile;
    private boolean ignoreFailures;

    @InputDirectory
    public File getMainFiles() {
        return mainFiles;
    }

    public void setMainFiles(Object mainFiles) {
        this.mainFiles = getProject().file(mainFiles);
    }

    @SkipWhenEmpty
    @InputDirectory
    public File getTestFiles() {
        return testFiles;
    }

    public void setTestFiles(Object testFiles) {
        this.testFiles = getProject().file(testFiles);
    }

    @InputFile
    public File getMochaOptionsFile() {
        return mochaOptionsFile;
    }

    public void setMochaOptionsFile(Object mochaOptionsFile) {
        this.mochaOptionsFile = getProject().file(mochaOptionsFile);
    }

    @OutputFile
    public File getTestOutput() {
        return testOutput;
    }

    public void setTestOutput(Object testOutput) {
        this.testOutput = getProject().file(testOutput);
    }

    @Override
    @Input
    public boolean getIgnoreFailures() {
        return ignoreFailures;
    }

    @Override
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
    }

    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException();
    }

    @TaskAction
    public void runTests() {
        ExecAction execAction = getExecActionFactory().newExecAction();
        execAction.setExecutable("node_modules/mocha/bin/mocha");
        execAction.environment("JUNIT_REPORT_PATH", testOutput.toString());
        execAction.environment("JUNIT_REPORT_STACK", "1");
        execAction.setArgs(Arrays.asList("--reporter", "mocha-jenkins-reporter", "--opts", mochaOptionsFile, "--recursive", testFiles));
        execAction.setIgnoreExitValue(ignoreFailures);
        execAction.execute();
    }
}
