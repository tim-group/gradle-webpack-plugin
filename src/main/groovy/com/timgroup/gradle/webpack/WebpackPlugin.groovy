package com.timgroup.gradle.webpack

import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.annotation.Nonnull

class WebpackPlugin implements Plugin<Project> {
    @Override
    void apply(@Nonnull Project project) {
        def extension = project.extensions.create("webpackPlugin", WebpackPluginExtension, project)

        project.pluginManager.apply("base")
        project.pluginManager.apply("com.moowork.node")

        project.tasks.getByName("clean").delete("node_modules")

        def webpackTask = project.tasks.create("webpack", WebpackTask)
        webpackTask.dependsOn.add("npmInstall")
        webpackTask.output = "build/site"
        webpackTask.configFile = "webpack.config.js"
        webpackTask.sources = "src/main/javascript"
        webpackTask.options = ["-p", "--devtool", "source-map"]
        webpackTask.manifestDigest = "SHA-256"
        webpackTask.generateManifest = true
        webpackTask.gzipResources = true
        webpackTask.nodeVersion = extension.nodeVersion
        webpackTask.group = "compile"
        webpackTask.description = "Runs Webpack to produce bundle files"

        project.tasks.getByName("assemble").dependsOn(webpackTask)

        def mochaTestTask = project.tasks.create("mochaTest", MochaTestTask)
        mochaTestTask.dependsOn("npmInstall")
        mochaTestTask.mainFiles = "src/main/javascript"
        mochaTestTask.testFiles = "src/test/javascript"
        mochaTestTask.testOutput = "build/test-results/mochaTest/test-reports.xml"
        mochaTestTask.mochaOptionsFile = "mocha.opts"
        mochaTestTask.group = "verification"
        mochaTestTask.description = "Runs the Mocha (JavaScript) tests"

        project.tasks.getByName("check").dependsOn(mochaTestTask)
    }
}
