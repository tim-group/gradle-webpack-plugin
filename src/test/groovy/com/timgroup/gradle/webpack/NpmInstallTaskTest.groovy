package com.timgroup.gradle.webpack

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification

class NpmInstallTaskTest extends Specification {
    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    private static def filesIn(File dir) {
        return (dir.list() ?: []) as Set
    }

    def "installs node packages"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}
"""

        testProjectDir.newFile("package.json") << """
{
  "dependencies": {
  }
}
"""

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("npmInstall")
            .withPluginClasspath()
            .build()

        then:
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "node_modules").isDirectory()
        filesIn(new File(testProjectDir.root, "node_modules")) == [] as Set
    }

    @Ignore
    def "uses specific node version"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}

webpackPlugin {
  nodeVersion = "8.11.1"
}
"""

        testProjectDir.newFile("package.json") << """
{
  "dependencies": {
  }
}
"""

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("npmInstall")
            .withPluginClasspath()
            .build()

        then:
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "node_modules").isDirectory()
        filesIn(new File(testProjectDir.root, "node_modules")) == [] as Set
    }

    def "build fails if package file is missing"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}
"""

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("npmInstall")
            .withPluginClasspath()
            .buildAndFail()

        then:
        result.task(":npmInstall").outcome == TaskOutcome.FAILED
    }
}
