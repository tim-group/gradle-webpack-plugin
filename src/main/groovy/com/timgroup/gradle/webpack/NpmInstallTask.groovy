package com.timgroup.gradle.webpack

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.internal.ExecActionFactory

import javax.inject.Inject

class NpmInstallTask extends DefaultTask {
    @InputFile
    def packageFile = "package.json"
    @OutputDirectory
    def nodeModulesDirectory = "node_modules"
    @Input @Optional
    final Property<String> nodeVersion = project.objects.property(String)

    File getPackageFile() {
        return project.file(packageFile)
    }
    File getNodeModulesDirectory() {
        return project.file(nodeModulesDirectory)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException()
    }

    @TaskAction
    void runNPM() {
        def execAction = execActionFactory.newExecAction()
        execAction.executable = new NodeVersion(nodeVersion, project, execActionFactory).npmExecutable
        execAction.args = ["install"]
        execAction.execute()
    }
}
