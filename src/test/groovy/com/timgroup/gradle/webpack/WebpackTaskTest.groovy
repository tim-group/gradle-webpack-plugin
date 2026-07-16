package com.timgroup.gradle.webpack

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.GZIPInputStream

import static java.util.stream.Collectors.toSet

class WebpackTaskTest extends Specification {
    @TempDir
    Path testProjectDir

    private static def filesIn(Path dir) {
        return Files.list(dir).withCloseable { it.map { it.getFileName().toString() }.collect(toSet()) }
    }

    private static def ungzippedBytes(Path file) {
        return new GZIPInputStream(Files.newInputStream(file)).bytes
    }

    def "compiles javascript files with webpack, compresses outputs and produces manifest with SHA-256 digests"() {
        given:
        testProjectDir.resolve("build.gradle") << """
plugins {
  id 'com.timgroup.webpack'
}
"""
        typicalJavascriptSetup()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments("assemble")
            .withPluginClasspath()
            .build()

        then:
        result.task(":webpack").outcome == TaskOutcome.SUCCESS
        filesIn(testProjectDir.resolve("build/site")) == ["main.js", "main.js.map", "main.js.gz", "main.js.map.gz", ".MANIFEST"] as Set
        (testProjectDir.resolve("build/site/.MANIFEST").text =~ /(?m)^[0-9a-f]{64} main.js \d+ \d+$/).find()
        testProjectDir.resolve("build/site/main.js").bytes == ungzippedBytes(testProjectDir.resolve("build/site/main.js.gz"))
    }

    def "manifest digest can be specified"() {
        given:
        testProjectDir.resolve("build.gradle") << """
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
            .withProjectDir(testProjectDir.toFile())
            .withArguments("assemble")
            .withPluginClasspath()
            .build()

        then:
        result.task(":webpack").outcome == TaskOutcome.SUCCESS
        filesIn(testProjectDir.resolve("build/site")) == ["main.js", "main.js.map", "main.js.gz", "main.js.map.gz", ".MANIFEST"] as Set
        (testProjectDir.resolve("build/site/.MANIFEST").text =~ /(?m)^[0-9a-f]{40} main.js \d+ \d+$/).find()
        testProjectDir.resolve("build/site/main.js").bytes == ungzippedBytes(testProjectDir.resolve("build/site/main.js.gz"))
    }

    def "resource gzipping can be disabled"() {
        given:
        testProjectDir.resolve("build.gradle") << """
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
            .withProjectDir(testProjectDir.toFile())
            .withArguments("assemble")
            .withPluginClasspath()
            .build()

        then:
        result.task(":webpack").outcome == TaskOutcome.SUCCESS
        filesIn(testProjectDir.resolve("build/site")) == ["main.js", "main.js.map", ".MANIFEST"] as Set
    }

    def "manifest generation can be disabled"() {
        given:
        testProjectDir.resolve("build.gradle") << """
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
            .withProjectDir(testProjectDir.toFile())
            .withArguments("assemble")
            .withPluginClasspath()
            .build()

        then:
        result.task(":webpack").outcome == TaskOutcome.SUCCESS
        filesIn(testProjectDir.resolve("build/site")) == ["main.js", "main.js.map", "main.js.gz", "main.js.map.gz"] as Set
    }

    def "build fails if webpack config file is missing"() {
        given:
        testProjectDir.resolve("build.gradle") << """
plugins {
  id 'com.timgroup.webpack'
}
"""
        typicalJavascriptSetup()
        Files.delete(testProjectDir.resolve("webpack.config.js"))

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments("assemble")
            .withPluginClasspath()
            .buildAndFail()

        then:
        result.task(":webpack").outcome == TaskOutcome.FAILED
    }

    def "task uses alternative configuration file when specified"() {
        given:
        testProjectDir.resolve("build.gradle") << """
plugins {
  id 'com.timgroup.webpack'
}

webpack {
  configFile = "webpack.alt.config.js"
}
"""
        typicalJavascriptSetup()
        Files.move(testProjectDir.resolve("webpack.config.js"), testProjectDir.resolve("webpack.alt.config.js"))

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments("assemble")
            .withPluginClasspath()
            .build()

        then:
        result.task(":webpack").outcome == TaskOutcome.SUCCESS
    }

    private def typicalJavascriptSetup() {
        Files.createDirectories(testProjectDir.resolve("src/main/javascript"))
        testProjectDir.resolve("package.json") << """
{
  "dependencies": {
    "webpack": "3.6.0"
  }
}
"""
        testProjectDir.resolve("package-lock.json") << "{}"
        testProjectDir.resolve("webpack.config.js") << """
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
        testProjectDir.resolve("src/main/javascript/init.js") << """
// this is init.js
"""
    }
}
