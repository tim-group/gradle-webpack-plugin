package com.timgroup.gradle.webpack

import com.moowork.gradle.node.NodeExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CopyNvmInstallTask : DefaultTask() {
    init {
        group = "Node"
        description = "Copy Node installation from where NVM downloads them"
        enabled = false
    }

    @TaskAction
    fun copyFiles() {
        val nodeExtension = NodeExtension.get(project)

        val nvmDir = File("${System.getProperty("user.home")}/.nvm/versions/node/v${nodeExtension.version}")
        logger.info("Looking for NVM-install NodeJS in $nvmDir")
        if (!nvmDir.isDirectory) return

        val nodeDir = nodeExtension.variant.nodeDir

        logger.info("Symlinking NodeJS in NVM directory $nvmDir to $nodeDir")

        nodeDir.parentFile.mkdirs()

        project.exec {
            commandLine = listOf("ln", "-sfvn", nvmDir.toString(), nodeDir.toString())
        }
    }
}
