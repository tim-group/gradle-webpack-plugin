package com.timgroup.gradle.webpack

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.internal.ExecActionFactory

import javax.inject.Inject
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
    @Input @Optional
    final Property<String> nodeVersion = project.objects.property(String)

    File getSources() {
        return project.file(sources)
    }
    File getConfigFile() {
        return project.file(configFile)
    }
    File getOutput() {
        return project.file(output)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException()
    }

    @TaskAction
    void runWebpack() {
        def execAction = execActionFactory.newExecAction()
        execAction.executable = new NodeVersion(nodeVersion, project, execActionFactory).nodeExecutable
        execAction.args = ["node_modules/.bin/webpack"] + options + ["--config", configFile.toString()]
        execAction.environment("NODE_ENV", "production")
        execAction.execute()

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
