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
        project.pluginManager.apply("com.github.node-gradle.node")

        project.tasks.named("clean").configure { task ->
            task.delete("node_modules");
        }

        def webpackTaskProvider = project.tasks.register("webpack", WebpackTask) { webpackTask ->
            webpackTask.output = "build/site"
            webpackTask.configFile = "webpack.config.js"
            webpackTask.sources = "src/main/javascript"
            webpackTask.options = ["-p", "--devtool", "source-map"]
            webpackTask.manifestDigest = "SHA-256"
            webpackTask.generateManifest = true
            webpackTask.gzipResources = true
            webpackTask.group = "compile"
            webpackTask.description = "Runs Webpack to produce bundle files"
        }

        project.tasks.named("assemble").configure { it.dependsOn(webpackTaskProvider) }

        def mochaTestTaskProvider = project.tasks.register("mochaTest", MochaTestTask) { mochaTestTask ->
            mochaTestTask.mainFiles = "src/main/javascript"
            mochaTestTask.testFiles = "src/test/javascript"
            mochaTestTask.testOutput = "build/test-results/mochaTest/test-reports.xml"
            mochaTestTask.mochaOptionsFile = "mocha.opts"
            mochaTestTask.group = "verification"
            mochaTestTask.description = "Runs the Mocha (JavaScript) tests"
        }

        def jestTestTaskProvider = project.tasks.register("jestTest", JestTestTask) { jestTestTask ->
            jestTestTask.mainFiles = "src/main/javascript"
            jestTestTask.testFiles = "src/test/javascript"
            jestTestTask.testOutput = "build/test-results/jestTest/test-reports.xml"
            jestTestTask.group = "verification"
            jestTestTask.description = "Runs the Jest (JavaScript) tests"
        }

        def copyNvmInstallTaskProvider = project.tasks.register("copyNvmInstall", CopyNvmInstallTask)

        project.afterEvaluate {
            String installTaskName
            if (project.file("yarn.lock").exists()) {
                installTaskName = "yarn"
            }
            else {
                installTaskName = "npmInstall"
            }
            mochaTestTaskProvider.configure { it.dependsOn(installTaskName) }
            jestTestTaskProvider.configure { it.dependsOn(installTaskName) }
            webpackTaskProvider.configure { it.dependsOn(installTaskName) }

            def packageJson = new JsonSlurper().parse(project.file("package.json"))
            if ((packageJson.devDependencies && packageJson.devDependencies.mocha) || (packageJson.dependencies && packageJson.dependencies.mocha)) {
                project.tasks.named("check").configure { it.dependsOn(mochaTestTaskProvider) }
            }
            if ((packageJson.devDependencies && packageJson.devDependencies.jest) || (packageJson.dependencies && packageJson.dependencies.jest)) {
                project.tasks.named("check").configure { it.dependsOn(jestTestTaskProvider) }
            }

            def nodeExtension = NodeExtension.get(project)
            if (nodeExtension.download) {
                def nodeSetupTaskProvider = project.tasks.named("nodeSetup")
                def nvmDir = new File("${System.getProperty("user.home")}/.nvm/versions/node/v${nodeExtension.version}")

                nodeSetupTaskProvider.configure {
                    it.dependsOn(copyNvmInstallTaskProvider)
                    it.enabled = !nvmDir.directory
                }

                copyNvmInstallTaskProvider.configure {
                    it.enabled = nvmDir.directory
                }
            }
        }
    }
}
