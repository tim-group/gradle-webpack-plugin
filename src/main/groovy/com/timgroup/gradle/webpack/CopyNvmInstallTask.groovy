package com.timgroup.gradle.webpack

import com.moowork.gradle.node.NodeExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CopyNvmInstallTask extends DefaultTask {
    CopyNvmInstallTask() {
        this.group = 'Node'
        this.description = 'Copy Node installation from where NVM downloads them'

        this.enabled = false
    }

    @TaskAction
    void copyFiles() {
        def nodeExtension = NodeExtension.get(project)

        def nvmDir = new File("${System.getProperty("user.home")}/.nvm/versions/node/v${nodeExtension.version}")
        logger.info("Looking for NVM-installed NodeJS in ${nvmDir}")
        if (!nvmDir.directory) return

        def nodeDir = nodeExtension.variant.nodeDir

        logger.info("Symlinking NodeJS in NVM directory ${nvmDir} to ${nodeDir}")

        nodeDir.parentFile.mkdirs()

        project.exec {
            commandLine = ["ln", "-sfvn", nvmDir, nodeDir]
        }
    }
}
