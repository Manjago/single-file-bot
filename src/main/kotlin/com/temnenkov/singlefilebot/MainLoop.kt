package com.temnenkov.singlefilebot

import com.temnenkov.singlefilebot.utils.CustomThreadFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

fun mainLoop() {
    val executor: ExecutorService = Executors.newFixedThreadPool(1, CustomThreadFactory("sfb-thread-"))
}