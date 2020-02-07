package com.timgroup.gradle.webpack

import com.moowork.gradle.node.exec.NodeExecRunner

@Suppress("UNCHECKED_CAST")
internal fun NodeExecRunner.putenv(key: String, value: Any?) {
    if (value != null) {
        (environment as MutableMap<String, String>)[key] = value.toString()
    }
}
