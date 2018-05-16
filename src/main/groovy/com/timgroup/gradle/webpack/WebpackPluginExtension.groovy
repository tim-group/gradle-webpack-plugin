package com.timgroup.gradle.webpack

import org.gradle.api.Project
import org.gradle.api.provider.Property

class WebpackPluginExtension {
    final Property<String> nodeVersion

    WebpackPluginExtension(Project project) {
        nodeVersion = project.objects.property(String)
    }
}
