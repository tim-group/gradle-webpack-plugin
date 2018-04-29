package com.timgroup.gradle.webpack

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.internal.os.OperatingSystem
import org.gradle.process.internal.ExecActionFactory

class NodeVersion {
    private final String version
    private final String architecture
    private final DirectoryProperty buildDirectory
    private final ExecActionFactory execActionFactory
    private final String archiveExtension

    NodeVersion(Provider<String> version, Project project, ExecActionFactory execActionFactory) {
        this.version = version.orNull
        // TODO probe this properly
        if (OperatingSystem.current().isMacOsX()) {
            architecture = "darwin-x64"
            archiveExtension = "tar.gz"
        }
        else if (OperatingSystem.current().isLinux()) {
            architecture = "linux-x64"
            archiveExtension = "tar.xz"
        }
        else {
            throw new UnsupportedOperationException("Unhandled OperatingSystem value: ${OperatingSystem.current()}")
        }
        buildDirectory = project.layout.buildDirectory
        this.execActionFactory = execActionFactory
    }

    String getNodeExecutable() {
        if (version == null) {
            return "node"
        }
        fetchInstallation()
        return new File(getBinDir(), "node")
    }

    String getNpmExecutable() {
        if (version == null) {
            return "npm"
        }
        fetchInstallation()
        return new File(getBinDir(), "npm")
    }

    File getInstallationsHome() {
        return buildDirectory.get().file("node-installations").getAsFile()
    }

    File getInstallationBase() {
        if (version == null) {
            throw new IllegalStateException("No version specified, so installation base is unavailable")
        }

        def nvmInstallDir = new File(System.getProperty("user.home"), ".nvm/versions/node/v$version")
        if (nvmInstallDir.directory && new File(nvmInstallDir, "bin/node").executable) return nvmInstallDir

        return new File(getInstallationsHome(), "node-v${version}-${architecture}")
    }

    File getBinDir() {
        return new File(getInstallationBase(), "bin")
    }

    private void fetchInstallation() {
        if (getBinDir().directory) return

        def archiveName = "node-v$version-${architecture}.$archiveExtension"
        def archiveUri = URI.create("https://nodejs.org/dist/v$version/$archiveName")

        println("Downloading Node from $archiveUri")
        getInstallationsHome().mkdirs()
        def archiveFile = new File(getInstallationsHome(), archiveName)
        archiveFile.withOutputStream { output ->
            archiveUri.toURL().withInputStream { input ->
                output << input
            }
        }

        println("Unpacking $archiveFile")
        def execAction = execActionFactory.newExecAction()
        if (archiveExtension == "tar.gz") {
            execAction.executable = "tar"
            execAction.args = ["xzf", archiveName.toString()]
        }
        else if (archiveExtension == "tar.xz") {
            execAction.executable = "tar"
            execAction.args = ["xJf", archiveName.toString()]
        }
        else {
            throw new IllegalStateException("Unhandled archive etension: $archiveExtension")
        }
        execAction.workingDir = getInstallationsHome()
        execAction.execute()
    }
}
