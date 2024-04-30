package com.temnenkov.singlefilebot.utils

import com.google.gson.GsonBuilder

private val gson =
    GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()

fun Any.toJson(): String = gson.toJson(this)

fun <T> String.fromJson(clazz: Class<T>): T = gson.fromJson(this, clazz)
