plugins {
    `java-gradle-plugin`
    groovy
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.9.9"
    id("com.timgroup.jarmangit") version "1.1.86"
}

val repoUrl by project
val repoUsername by project
val repoPassword by project

val buildNumber: String? by extra { System.getenv("ORIGINAL_BUILD_NUMBER") ?: System.getenv("BUILD_NUMBER") }
val githubUrl by extra("https://github.com/tim-group/gradle-webpack-plugin")

group = "com.timgroup"
if (buildNumber != null) version = "1.0.$buildNumber"
description = "Build Javascript sources with Webpack and test with Mocha"

repositories {
    mavenCentral()
}

dependencies {
    compile(gradleApi())
    compile(localGroovy())

    testCompile("junit:junit:4.12")
    testCompile("org.spockframework:spock-core:1.0-groovy-2.4") {
        exclude(module = "groovy-all")
    }
}

tasks {
    "test"(Test::class) {
        maxParallelForks = 4
    }

    val sourcesJar by creating(Jar::class) {
        classifier = "sources"
        from(java.sourceSets["main"].allSource)
    }

    "assemble" {
        dependsOn(sourcesJar)
    }
}

gradlePlugin {
    (plugins) {
        "webpack" {
            id = "com.timgroup.webpack"
            implementationClass = "com.timgroup.gradle.webpack.WebpackPlugin"
        }
    }
}

pluginBundle {
    website = githubUrl
    vcsUrl = githubUrl
    description = project.description

    (plugins) {
        "webpack" {
            id = "com.timgroup.webpack"
            displayName = "Webpack / Mocha plugin"
            tags = setOf("webpack", "mocha", "nodejs")
        }
    }
}

publishing {
    repositories {
        if (project.hasProperty("repoUrl")) {
            maven("$repoUrl/repositories/yd-release-candidates") {
                name = "timgroup"
                credentials {
                    username = repoUsername.toString()
                    password = repoPassword.toString()
                }
            }
        }
    }
    (publications) {
        "pluginMaven"(MavenPublication::class) {
            artifact(tasks["sourcesJar"])
        }
    }
}
