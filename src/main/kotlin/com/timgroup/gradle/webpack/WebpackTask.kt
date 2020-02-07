package com.timgroup.gradle.webpack

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.ExecActionFactory
import java.nio.file.Files
import java.security.MessageDigest
import javax.inject.Inject

abstract class WebpackTask : DefaultTask() {
    init {
        group = "compile"
        description = "Runs Webpack to produce bundle files"
    }

    @get:InputDirectory
    val sources = project.objects.directoryProperty()
    @get:InputFile
    val configFile = project.objects.fileProperty()
    @get:Input
    val options = project.objects.listProperty(String::class.java).convention(listOf())
    @get:OutputDirectory
    val output = project.objects.fileProperty()
    @get:Input
    val generateManifest = project.objects.property(Boolean::class.java).convention(true)
    @get:Input
    val gzipResources = project.objects.property(Boolean::class.java).convention(true)
    @get:Input
    val manifestDigest = project.objects.property(String::class.java).convention("SHA256")

    @get:Inject
    protected abstract val execActionFactory: ExecActionFactory

    @TaskAction
    fun runWebpack() {
        project.execNode {
            arguments = mutableListOf<String>().apply {
                add("node_modules/.bin/webpack")
                addAll(options.get())
                add("--config")
                add(configFile.get().asFile.toString())
            }
            putenv("NODE_ENV", "production")
        }

        if (gzipResources.get())
            doGzipResources()
        if (generateManifest.get())
            doGenerateManifest()
    }

    private fun doGzipResources() {
        val fileTree = project.fileTree(output.get()) {
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
        val sha1 = MessageDigest.getInstance(manifestDigest.get())
        val manifest = StringBuilder()
        val fileTree = project.fileTree(output.get()) {
            exclude(".MANIFEST")
        }
        fileTree.forEach { file ->
            val relativeName = file.toString().substring(output.get().asFile.toString().length + 1)

            val digest = sha1.digest().encodeHex()
            val lastModifiedTime = file.lastModified()
            val fileSize = file.length()
            manifest.append("$digest $relativeName $fileSize ${lastModifiedTime}\n")
        }
        val manifestFile = output.get().asFile.resolve(".MANIFEST")
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
