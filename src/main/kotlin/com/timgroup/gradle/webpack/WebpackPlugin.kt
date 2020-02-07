package com.timgroup.gradle.webpack

import com.moowork.gradle.node.NodeExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.existing
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import java.io.File

open class WebpackPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            pluginManager.apply("base")
            pluginManager.apply("com.github.node-gradle.node")

            val clean by tasks.existing(Delete::class)
            val assemble by tasks.existing
            val check by tasks.existing
            val nodeExtension by lazy { NodeExtension.get(project) }

            clean.configure {
                delete("node_modules")
            }

            val webpack by tasks.registering(WebpackTask::class) {
                output.set(file("build/site"))
                configFile.set(file("webpack.config.js"))
                sources.set(file("src/main/javascript"))
                options.set(listOf("-p", "--devtool", "source-map"))
                manifestDigest.set("SHA-256")
                generateManifest.set(true)
                gzipResources.set(true)
            }

            assemble.configure { dependsOn(webpack) }

            val mochaTest by tasks.registering(MochaTestTask::class) {
                mainFiles.set(file("src/main/javascript"))
                testFiles.set(file("src/test/javascript"))
                testOutput.set(file("build/test-results/mochaTest/test-reports.xml"))
                mochaOptionsFile.set(file("mocha.opts"))
            }

            val jestTest by tasks.registering(JestTestTask::class) {
                mainFiles.set(file("src/main/javascript"))
                testFiles.set(file("src/test/javascript"))
                testOutput.set(file("build/test-results/jestTest/test-reports.xml"))
            }

            val copyNvmInstall by tasks.registering(CopyNvmInstallTask::class)

            afterEvaluate {
                val installTaskName = if (file("yarn.lock").exists()) "yarn" else "npmInstall"

                mochaTest.configure { dependsOn(installTaskName) }
                jestTest.configure { dependsOn(installTaskName) }
                webpack.configure { dependsOn(installTaskName) }

                val packageJson = slurpJson(file("package.json"))
                if (packageJson.hasContentAt("/devDependencies/mocha") || packageJson.hasContentAt("/depdendencies/mocha")) {
                    check.configure { dependsOn(mochaTest) }
                }
                if (packageJson.hasContentAt("/devDependencies/jest") || packageJson.hasContentAt("/depdendencies/jest")) {
                    check.configure { dependsOn(jestTest) }
                }

                if (nodeExtension.isDownload) {
                    val nodeSetup by tasks.existing
                    val nvmDir = File("${System.getProperty("user.home")}/.nvm/versions/node/v${nodeExtension.version}")

                    nodeSetup.configure {
                        dependsOn(copyNvmInstall)
                        enabled = !nvmDir.isDirectory
                    }

                    copyNvmInstall.configure {
                        enabled = nvmDir.isDirectory
                    }
                }
            }
        }
    }
}
