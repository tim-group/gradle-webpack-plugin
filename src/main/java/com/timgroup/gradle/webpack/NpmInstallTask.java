package com.timgroup.gradle.webpack;

import java.io.File;
import java.util.Collections;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;

public class NpmInstallTask extends DefaultTask {
    @InputFile
    public File getPackageFile() {
        return getProject().file("package.json");
    }

    @OutputDirectory
    public File getNodeModulesDirectory() {
        return getProject().file("node_modules");
    }

    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException();
    }

    @TaskAction
    public void runNPM() {
        ExecAction execAction = getExecActionFactory().newExecAction();
        execAction.setExecutable("npm");
        execAction.setArgs(Collections.singletonList("install"));
        execAction.execute();
    }
}
