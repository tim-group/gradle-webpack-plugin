package com.timgroup.gradle.webpack

import com.moowork.gradle.node.NodeExtension
import org.gradle.api.Project

class WebpackPluginExtension {
    private final Project project

    String getNodeVersion() {
        return null
    }

    void setNodeVersion(String nodeVersion) {
        def nodeExtension = NodeExtension.get(project)
        nodeExtension.version = nodeVersion
        nodeExtension.download = true
    }

    WebpackPluginExtension(Project project) {
        this.project = project
    }
}
