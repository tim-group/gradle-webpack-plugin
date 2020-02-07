plugins {
    `java-gradle-plugin`
    `maven-publish`
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "0.9.9"
    id("com.timgroup.jarmangit") version "1.1.86"
}

val repoUrl: String? by project
val repoUsername: String? by project
val repoPassword: String? by project

val buildNumber: String? by extra { System.getenv("ORIGINAL_BUILD_NUMBER") ?: System.getenv("BUILD_NUMBER") }
val githubUrl by extra("https://github.com/tim-group/gradle-webpack-plugin")

group = "com.timgroup"
if (buildNumber != null) version = "1.0.$buildNumber"
description = "Build Javascript sources with Webpack and test with Mocha"

repositories {
    mavenCentral()
    jcenter()
    gradlePluginPortal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

dependencies {
    implementation(gradleApi())
    implementation("com.github.node-gradle:gradle-node-plugin:2.2.0")

    testImplementation("junit:junit:4.12")
    testImplementation("com.natpryce:hamkrest:1.7.0.0")
}

tasks {
    "test"(Test::class) {
        maxParallelForks = 4
    }
}

gradlePlugin {
    plugins {
        create("webpack") {
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
        create("webpack") {
            id = "com.timgroup.webpack"
            displayName = "Webpack / Mocha plugin"
            tags = setOf("webpack", "mocha", "jest", "nodejs")
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
}
