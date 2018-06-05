package com.timgroup.gradle.webpack

import com.moowork.gradle.node.exec.NodeExecRunner
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributeView
import java.security.MessageDigest

class WebpackTask extends DefaultTask {
    @InputDirectory
    def sources
    @InputFile
    def configFile
    @Input
    List<String> options
    @OutputDirectory
    def output
    @Input
    boolean generateManifest
    @Input
    boolean gzipResources
    @Input
    String manifestDigest

    File getSources() {
        return project.file(sources)
    }
    File getConfigFile() {
        return project.file(configFile)
    }
    File getOutput() {
        return project.file(output)
    }

    @TaskAction
    void runWebpack() {
        def runner = new NodeExecRunner( this.project )
        runner.arguments = ["node_modules/.bin/webpack"] + options + ["--config", configFile.toString()]
        runner.environment.put("NODE_ENV", "production")
        runner.execute()

        if (gzipResources)
            doGzipResources()
        if (generateManifest)
            doGenerateManifest()
    }

    private void doGzipResources() {
        def fileTree = project.fileTree(output) {
            includes = ["**/*.html", "**/*.js", "**/*.map"]
        }
        fileTree.forEach { file ->
            project.logger.info("Gzipping $file")
            def inputPath = file.toPath()
            def outputPath = inputPath.resolveSibling("${inputPath.fileName}.gz")

            def execAction = execActionFactory.newExecAction()
            execAction.executable = "gzip"
            execAction.args = ["-9c"]
            execAction.standardInput = Files.newInputStream(inputPath)
            execAction.standardOutput = Files.newOutputStream(outputPath)
            execAction.execute()
        }
    }

    private void doGenerateManifest() {
        def sha1 = MessageDigest.getInstance(manifestDigest)
        def manifest = new StringBuilder()
        def fileTree = project.fileTree(output) {
            excludes = [".MANIFEST"]
        }
        fileTree.forEach { file ->
            def relativeName = file.toString().substring(getOutput().toString().length() + 1)
            file.withInputStream { input ->
                def buf = new byte[8192]
                int got
                while ((got = input.read(buf)) > 0) {
                    sha1.update(buf, 0, got)
                }
            }
            def digest = sha1.digest().encodeHex()
            def fileAttributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView).readAttributes()
            def lastModifiedTime = fileAttributes.lastModifiedTime()
            def fileSize = fileAttributes.size()
            manifest.append("$digest $relativeName $fileSize ${lastModifiedTime.toInstant().toEpochMilli()}\n")
        }
        def manifestPath = getOutput().toPath().resolve(".MANIFEST")
        project.logger.info("Writing $manifestPath")
        manifestPath.toFile().setText(manifest.toString(), "UTF-8")
    }
}
