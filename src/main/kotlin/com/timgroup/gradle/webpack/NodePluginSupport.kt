package com.timgroup.gradle.webpack

import com.moowork.gradle.node.exec.NodeExecRunner
import org.gradle.api.Project
import org.gradle.process.ExecResult

@Suppress("UNCHECKED_CAST")
internal fun NodeExecRunner.putenv(key: String, value: Any?) {
    if (value != null) {
        (environment as MutableMap<String, String>)[key] = value.toString()
    }
}

internal fun Project.execNode(builder: NodeExecRunner.() -> Unit): ExecResult {
    return NodeExecRunner(this).apply(builder).execute()
}
