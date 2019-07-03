package com.timgroup.gradle.webpack

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.zip.GZIPInputStream

class WebpackTaskTest extends Specification {
    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    private static def filesIn(File dir) {
        return (dir.list() ?: []) as Set
    }

    private static def ungzippedBytes(File file) {
        return new GZIPInputStream(new FileInputStream(file)).bytes
    }

    def "compiles javascript files with webpack, compresses outputs and produces manifest with SHA-256 digests"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}
"""
        typicalJavascriptSetup()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("assemble")
            .withPluginClasspath()
            .build()

        then:
        result.task(":webpack").outcome == TaskOutcome.SUCCESS
        filesIn(new File(testProjectDir.root, "build/site")) == ["main.js", "main.js.map", "main.js.gz", "main.js.map.gz", ".MANIFEST"] as Set
        (new File(testProjectDir.root, "build/site/.MANIFEST").text =~ /(?m)^[0-9a-f]{64} main.js \d+ \d+$/).find()
        new File(testProjectDir.root, "build/site/main.js").bytes == ungzippedBytes(new File(testProjectDir.root, "build/site/main.js.gz"))
    }

    def "manifest digest can be specified"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}

webpack {
  manifestDigest = "SHA-1"
}
"""
        typicalJavascriptSetup()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("assemble")
            .withPluginClasspath()
            .build()

        then:
        result.task(":webpack").outcome == TaskOutcome.SUCCESS
        filesIn(new File(testProjectDir.root, "build/site")) == ["main.js", "main.js.map", "main.js.gz", "main.js.map.gz", ".MANIFEST"] as Set
        (new File(testProjectDir.root, "build/site/.MANIFEST").text =~ /(?m)^[0-9a-f]{40} main.js \d+ \d+$/).find()
        new File(testProjectDir.root, "build/site/main.js").bytes == ungzippedBytes(new File(testProjectDir.root, "build/site/main.js.gz"))
    }

    def "resource gzipping can be disabled"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}

webpack {
  gzipResources = false
}
"""
        typicalJavascriptSetup()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("assemble")
            .withPluginClasspath()
            .build()

        then:
        result.task(":webpack").outcome == TaskOutcome.SUCCESS
        filesIn(new File(testProjectDir.root, "build/site")) == ["main.js", "main.js.map", ".MANIFEST"] as Set
    }

    def "manifest generation can be disabled"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}

webpack {
  generateManifest = false
}
"""
        typicalJavascriptSetup()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("assemble")
            .withPluginClasspath()
            .build()

        then:
        result.task(":webpack").outcome == TaskOutcome.SUCCESS
        filesIn(new File(testProjectDir.root, "build/site")) == ["main.js", "main.js.map", "main.js.gz", "main.js.map.gz"] as Set
    }

    def "build fails if webpack config file is missing"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}
"""
        typicalJavascriptSetup()
        new File(testProjectDir.root, "webpack.config.js").delete()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("assemble")
            .withPluginClasspath()
            .buildAndFail()

        then:
        result.task(":webpack").outcome == TaskOutcome.FAILED
    }

    def "task uses alternative configuration file when specified"() {
        given:
        buildFile << """
plugins {
  id 'com.timgroup.webpack'
}

webpack {
  configFile = "webpack.alt.config.js"
}
"""
        typicalJavascriptSetup()
        new File(testProjectDir.root, "webpack.config.js").renameTo(new File(testProjectDir.root, "webpack.alt.config.js"))

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("assemble")
            .withPluginClasspath()
            .build()

        then:
        result.task(":webpack").outcome == TaskOutcome.SUCCESS
    }

    private def typicalJavascriptSetup() {
        testProjectDir.newFolder("src", "main", "javascript")
        testProjectDir.newFile("package.json") << """
{
  "dependencies": {
    "webpack": "3.6.0"
  }
}
"""
        testProjectDir.newFile("package-lock.json") << "{}"
        testProjectDir.newFile("webpack.config.js") << """
var webpack = require("webpack");
var path = require('path');
function relative(suffix) {
    return path.resolve(__dirname, suffix);
}
module.exports = {
  context: relative("src/main/javascript"),
  output: {
    filename: "[name].js",
    path: relative("build/site")
  },
  entry: "./init"
};
"""
        testProjectDir.newFile("src/main/javascript/init.js") << """
// this is init.js
"""
    }
}
