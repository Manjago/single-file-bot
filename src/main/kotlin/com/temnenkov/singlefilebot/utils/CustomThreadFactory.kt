package com.temnenkov.singlefilebot.utils

import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

class CustomThreadFactory(private val threadPrefix: String) : ThreadFactory {
    private val logger = KotlinLogging.logger {}
    private val backingThreadFactory = Executors.defaultThreadFactory()
    private val counter = AtomicLong(0)

    override fun newThread(r: Runnable): Thread {
        val newThread = backingThreadFactory.newThread(r)
        newThread.name = "$threadPrefix${counter.getAndIncrement()}"
        newThread.setUncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
            logger.error(throwable) { "thread ${thread.name} threw exception ${throwable.message}" }
        }
        return newThread
    }
}
