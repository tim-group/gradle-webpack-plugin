package com.timgroup.gradle.webpack

import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.annotation.Nonnull

class WebpackPlugin implements Plugin<Project> {
    @Override
    void apply(@Nonnull Project project) {
        project.pluginManager.apply("base")

        project.tasks.getByName("clean").delete("node_modules")

        def npmInstallTask = project.tasks.create("npmInstall", NpmInstallTask)
        npmInstallTask.group = "compile"
        npmInstallTask.description = "Runs NPM to fetch javascript packages into node_modules"

        def webpackTask = project.tasks.create("webpack", WebpackTask)
        webpackTask.dependsOn.add(npmInstallTask)
        webpackTask.output = project.file("build/site")
        webpackTask.configFile = project.file("webpack.config.js")
        webpackTask.sources = project.file("src/main/javascript")
        webpackTask.options = ["-p", "--devtool", "source-map"]
        webpackTask.manifestDigest = "SHA-256"
        webpackTask.generateManifest = true
        webpackTask.gzipResources = true
        webpackTask.group = "compile"
        webpackTask.description = "Runs Webpack to produce bundle files"

        project.tasks.getByName("assemble").dependsOn(webpackTask)

        def mochaTestTask = project.tasks.create("mochaTest", MochaTestTask)
        mochaTestTask.dependsOn(npmInstallTask)
        mochaTestTask.mainFiles = project.file("src/main/javascript")
        mochaTestTask.testFiles = project.file("src/test/javascript")
        mochaTestTask.testOutput = project.file("build/test-results/mochaTest/test-reports.xml")
        mochaTestTask.mochaOptionsFile = project.file("mocha.opts")
        mochaTestTask.group = "verification"
        mochaTestTask.description = "Runs the Mocha (JavaScript) tests"

        project.tasks.getByName("check").dependsOn(mochaTestTask)
    }
}
