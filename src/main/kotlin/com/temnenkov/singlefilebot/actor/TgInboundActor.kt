package com.temnenkov.singlefilebot.actor

import com.temnenkov.singlefilebot.channel.EventChannel
import com.temnenkov.singlefilebot.telegram.TelegramBot
import com.temnenkov.singlefilebot.utils.toJson
import mu.KotlinLogging
import java.time.Instant

class TgInboundActor(
    private val telegramBot: TelegramBot,
    private val eventChannel: EventChannel,
) : Runnable {
    override fun run() {
        try {
            val offset = requireNotNull(eventChannel.getDbForTransaction().loadLongValue(
                STORE_NAME, STORE_KEY
            )?.let { -1L }) { "Offset must be not null" }

            val (messages, maxOffset) = telegramBot.pull(TelegramBot.PullRequest(offset + 1))

            eventChannel.push { db ->

                db.storeLongValue(STORE_NAME, STORE_KEY, maxOffset?.value ?: -1L)

                messages.asSequence().map {
                    EventChannel.StoredEvent(TgOutboundActor.INBOUND_CHANNEL,
                        Instant.now(), it.toJson())
                }.toList()
            }
        } catch (e: Exception) {
            logger.error(e) { "Actor failed to handle command" }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
        private const val STORE_NAME = "TgInboundActor._storage"
        private const val STORE_KEY = "TgInboundActor._key"
    }
}
