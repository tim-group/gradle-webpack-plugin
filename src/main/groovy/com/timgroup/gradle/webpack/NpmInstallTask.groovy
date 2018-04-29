package com.timgroup.gradle.webpack

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.ExecActionFactory

import javax.inject.Inject

class NpmInstallTask extends DefaultTask {
    @InputFile
    File packageFile = project.file("package.json")
    @OutputDirectory
    File getNodeModulesDirectory = project.file("node_modules")

    void setPackageFile(Object obj) {
        packageFile = project.file(obj)
    }
    void setNodeModulesDirectory(Object obj) {
        nodeModulesDirectory = project.file(obj)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException()
    }

    @TaskAction
    void runNPM() {
        def execAction = execActionFactory.newExecAction()
        execAction.executable = "npm"
        execAction.args = ["install"]
        execAction.execute()
    }
}
