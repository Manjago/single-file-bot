package com.temnenkov.singlefilebot

import mu.KotlinLogging

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        logger.error("config not found")
        return
    }
    mainLoop(args[0])
}

private val logger = KotlinLogging.logger { }
