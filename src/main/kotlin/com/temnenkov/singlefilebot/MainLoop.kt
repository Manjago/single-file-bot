package com.temnenkov.singlefilebot

import com.temnenkov.singlefilebot.actor.TgInboundActor
import com.temnenkov.singlefilebot.channel.impl.EventChannelMvStore
import com.temnenkov.singlefilebot.config.TgParameters
import com.temnenkov.singlefilebot.telegram.impl.TelegramBotImpl
import com.temnenkov.singlefilebot.utils.CustomThreadFactory
import com.temnenkov.singlefilebot.utils.MvStoreWrapper
import com.temnenkov.singlefilebot.utils.NowProvider
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private val customThreadFactory = CustomThreadFactory("sfb-thread-")
private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(1, customThreadFactory)

fun mainLoop() {
    val tgParameters = TgParameters(5L, "test", 10L, 5L)
    val mvStoreWrapper = MvStoreWrapper("tgdb")
    val nowProvider =
        object : NowProvider {
            override fun now(): Instant = Instant.now()
        }

    executor.scheduleWithFixedDelay(
        TgInboundActor(
            telegramBot = TelegramBotImpl(tgParameters),
            mvStoreWrapper = mvStoreWrapper,
            eventChannel = EventChannelMvStore(mvStoreWrapper, nowProvider),
        ),
        500L,
        1000,
        TimeUnit.MILLISECONDS,
    )
}
