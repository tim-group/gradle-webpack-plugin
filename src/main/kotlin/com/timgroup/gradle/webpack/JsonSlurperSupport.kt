package com.timgroup.gradle.webpack

import groovy.json.JsonSlurper
import java.io.File

internal fun slurpJson(file: File) = Slurped(JsonSlurper().parse(file) as Map<*, *>)

private val pattern = Regex("^/([^/]+)")

internal inline class Slurped(private val content: Map<*, *>) {
    fun hasContentAt(jsonPointer: String): Boolean {
        val result = pattern.find(jsonPointer) ?: throw IllegalArgumentException("Invalid JSONPointer: $jsonPointer")
        val nextValue = content[result.groupValues[1]] ?: return false
        if ((result.range.last + 1) == jsonPointer.length)
            return true
        return Slurped(nextValue as Map<*, *>).hasContentAt(jsonPointer.substring(result.range.last + 1))
    }
}
