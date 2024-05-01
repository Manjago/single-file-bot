package com.temnenkov.singlefilebot.utils

import java.io.FileInputStream
import java.util.Properties

fun enrichSystemProperties(filePath: String) {
    val appProps = Properties()
    appProps.load(FileInputStream(filePath))
    val systemProperties = System.getProperties()
    appProps.forEach {
        systemProperties.setProperty(it.key.toString(), it.value.toString())
    }
}
