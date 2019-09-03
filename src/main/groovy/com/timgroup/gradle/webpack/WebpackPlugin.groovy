package com.timgroup.gradle.webpack

import com.moowork.gradle.node.NodeExtension
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.annotation.Nonnull

class WebpackPlugin implements Plugin<Project> {
    @Override
    void apply(@Nonnull Project project) {
        def extension = project.extensions.create("webpackPlugin", WebpackPluginExtension, project)

        project.pluginManager.apply("base")
        project.pluginManager.apply("com.moowork.node")

        project.tasks.named("clean").configure { task ->
            task.delete("node_modules");
        };

        def webpackTask = project.tasks.create("webpack", WebpackTask)
        webpackTask.output = "build/site"
        webpackTask.configFile = "webpack.config.js"
        webpackTask.sources = "src/main/javascript"
        webpackTask.options = ["-p", "--devtool", "source-map"]
        webpackTask.manifestDigest = "SHA-256"
        webpackTask.generateManifest = true
        webpackTask.gzipResources = true
        webpackTask.group = "compile"
        webpackTask.description = "Runs Webpack to produce bundle files"

        project.tasks.getByName("assemble").dependsOn(webpackTask)

        def mochaTestTask = project.tasks.create("mochaTest", MochaTestTask)
        mochaTestTask.mainFiles = "src/main/javascript"
        mochaTestTask.testFiles = "src/test/javascript"
        mochaTestTask.testOutput = "build/test-results/mochaTest/test-reports.xml"
        mochaTestTask.mochaOptionsFile = "mocha.opts"
        mochaTestTask.group = "verification"
        mochaTestTask.description = "Runs the Mocha (JavaScript) tests"

        def jestTestTask = project.tasks.create("jestTest", JestTestTask)
        jestTestTask.mainFiles = "src/main/javascript"
        jestTestTask.testFiles = "src/test/javascript"
        jestTestTask.testOutput = "build/test-results/jestTest/test-reports.xml"
        jestTestTask.group = "verification"
        jestTestTask.description = "Runs the Jest (JavaScript) tests"

        def copyNvmInstall = project.tasks.create("copyNvmInstall", CopyNvmInstallTask)

        project.afterEvaluate {
            def installTask
            if (project.file("yarn.lock").exists()) {
                installTask = "yarn"
            }
            else {
                installTask = "npmInstall"
            }
            mochaTestTask.dependsOn.add(installTask)
            jestTestTask.dependsOn.add(installTask)
            webpackTask.dependsOn.add(installTask)

            def packageJson = new JsonSlurper().parse(project.file("package.json"))
            if ((packageJson.devDependencies && packageJson.devDependencies.mocha) || (packageJson.dependencies && packageJson.dependencies.mocha)) {
                project.tasks.getByName("check").dependsOn(mochaTestTask)
            }
            if ((packageJson.devDependencies && packageJson.devDependencies.jest) || (packageJson.dependencies && packageJson.dependencies.jest)) {
                project.tasks.getByName("check").dependsOn(jestTestTask)
            }

            def nodeExtension = NodeExtension.get(project)
            if (nodeExtension.download) {
                def nodeSetup = project.tasks.getByName("nodeSetup")
                def nvmDir = new File("${System.getProperty("user.home")}/.nvm/versions/node/v${nodeExtension.version}")

                nodeSetup.dependsOn(copyNvmInstall)

                nodeSetup.enabled = !nvmDir.directory
                copyNvmInstall.enabled = nvmDir.directory
            }
        }
    }
}
