plugins {
    `java-gradle-plugin`
    groovy
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.1.1"
    id("com.adarshr.test-logger") version "4.0.0"
}

val repoUrl: String? by project
val repoUsername: String? by project
val repoPassword: String? by project

val buildNumber = providers.environmentVariable("ORIGINAL_BUILD_NUMBER")
    .orElse(providers.environmentVariable("BUILD_NUMBER"))
val githubUrl by extra("https://github.com/tim-group/gradle-webpack-plugin")

group = "com.timgroup"
if (buildNumber.isPresent) version = "1.0.${buildNumber.get()}"
description = "Build Javascript sources with Webpack and test with Mocha / Jest"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withSourcesJar()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("com.github.node-gradle:gradle-node-plugin:2.2.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.spockframework:spock-core:2.4-groovy-3.0") {
        exclude(module = "groovy")
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

gradlePlugin {
    plugins {
        create("webpack") {
            id = "com.timgroup.webpack"
            implementationClass = "com.timgroup.gradle.webpack.WebpackPlugin"
            description = project.description
            displayName = "Webpack / Mocha plugin"
        }
    }
}

pluginBundle {
    website = githubUrl
    vcsUrl = githubUrl
    tags = setOf("webpack", "mocha", "jest", "nodejs")
}

val nexusRepoUrl = providers.gradleProperty("repoUrl")
val nexusRepoUsername = providers.gradleProperty("repoUsername")
val nexusRepoPassword = providers.gradleProperty("repoPassword")
val codeartifactUrl = providers.environmentVariable("CODEARTIFACT_URL")
    .orElse(providers.gradleProperty("codeartifact.url"))
    .orElse("https://timgroup-148217964156.d.codeartifact.eu-west-1.amazonaws.com/maven/jars/")
val codeartifactToken = providers.environmentVariable("CODEARTIFACT_TOKEN")
    .orElse(providers.gradleProperty("codeartifact.token"))

publishing {
    repositories {
        if (nexusRepoUrl.isPresent && nexusRepoUsername.isPresent && nexusRepoPassword.isPresent) {
            maven("${nexusRepoUrl.get()}/repositories/yd-release-candidates") {
                name = "nexus"
                credentials {
                    username = nexusRepoUsername.get()
                    password = nexusRepoPassword.get()
                }
                isAllowInsecureProtocol = true
            }
        }
        if (codeartifactUrl.isPresent && codeartifactToken.isPresent) {
            maven(url = codeartifactUrl.get()) {
                name = "codeartifact"
                credentials {
                    username = "aws"
                    password = codeartifactToken.get()
                }
            }
        }
    }
}
