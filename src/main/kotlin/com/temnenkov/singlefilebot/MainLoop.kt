package com.temnenkov.singlefilebot

import com.temnenkov.singlefilebot.actor.TgInboundActor
import com.temnenkov.singlefilebot.channel.impl.EventChannelMvStore
import com.temnenkov.singlefilebot.config.TgParameters
import com.temnenkov.singlefilebot.telegram.impl.TelegramBotImpl
import com.temnenkov.singlefilebot.utils.CustomThreadFactory
import com.temnenkov.singlefilebot.utils.MvStoreWrapper
import com.temnenkov.singlefilebot.utils.NowProvider
import com.temnenkov.singlefilebot.utils.enrichSystemProperties
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private val customThreadFactory = CustomThreadFactory("sfb-thread-")
private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(1, customThreadFactory)

fun mainLoop(configPath: String) {

    enrichSystemProperties(configPath)

    val tgParameters = TgParameters.loadFromSystemProperties()
    val nowProvider =
        object : NowProvider {
            override fun now(): Instant = Instant.now()
        }

    executor.scheduleWithFixedDelay(
        TgInboundActor(
            telegramBot = TelegramBotImpl(tgParameters),
            eventChannel = EventChannelMvStore(MvStoreWrapper("tgdb"), nowProvider),
        ),
        500L,
        1000,
        TimeUnit.MILLISECONDS,
    )
}
