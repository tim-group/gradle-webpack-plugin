package com.timgroup.gradle.webpack

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.ExecActionFactory
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import javax.inject.Inject

abstract class WebpackTask : DefaultTask() {
    @get:InputDirectory
    var sources: File? = null
    @get:InputFile
    var configFile: String? = null
    @get:Input
    var options: List<String> = mutableListOf()
    @get:OutputDirectory
    var output: File? = null
    @get:Input
    var generateManifest: Boolean = true
    @get:Input
    var gzipResources: Boolean = true
    @get:Input
    var manifestDigest: String = "SHA256"

    @get:Inject
    protected abstract val execActionFactory: ExecActionFactory

    @TaskAction
    fun runWebpack() {
        project.execNode {
            arguments = mutableListOf<String>().apply {
                add("node_modules/.bin/webpack")
                addAll(options)
                add("--config")
                add(project.file(configFile!!).toString())
            }
            putenv("NODE_ENV", "production")
        }

        if (gzipResources)
            doGzipResources()
        if (generateManifest)
            doGenerateManifest()
    }

    private fun doGzipResources() {
        val fileTree = project.fileTree(output!!) {
            setIncludes(setOf("**/*.html", "**/*.js", "**/*.map"))
        }
        fileTree.forEach { file ->
            project.logger.info("Gzipping $file")
            val inputPath = file.toPath()
            val outputPath = inputPath.resolveSibling("${inputPath.fileName}.gz")

            execActionFactory.newExecAction().apply {
                executable = "gzip"
                args = listOf("-9c")
                standardInput = Files.newInputStream(inputPath)
                standardOutput = Files.newOutputStream(outputPath)
            }.execute()
        }
    }

    private fun doGenerateManifest() {
        val sha1 = MessageDigest.getInstance(manifestDigest)
        val manifest = StringBuilder()
        val fileTree = project.fileTree(output!!) {
            exclude(".MANIFEST")
        }
        fileTree.forEach { file ->
            val relativeName = file.toString().substring(output.toString().length + 1)

            val digest = sha1.digest().encodeHex()
            val lastModifiedTime = file.lastModified()
            val fileSize = file.length()
            manifest.append("$digest $relativeName $fileSize ${lastModifiedTime}\n")
        }
        val manifestFile = output!!.resolve(".MANIFEST")
        project.logger.info("Writing $manifestFile")
        manifestFile.writeText(manifest.toString())
    }
}

private fun ByteArray.encodeHex(): String {
    val output = CharArray(2 * size)
    val hexdigits = "0123456789abcdef"
    var j = 0
    for (b in this) {
        output[j++] = hexdigits[(b.toInt() shr 4) and 0x0f]
        output[j++] = hexdigits[b.toInt() and 0x0f]
    }
    return String(output)
}
