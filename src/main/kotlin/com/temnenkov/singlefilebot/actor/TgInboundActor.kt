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
            val offset =
                eventChannel.doInTransaction { db ->
                    db.loadValue(STORE_NAME, STORE_KEY) ?: "-1"
                }.toLong()

            val (messages, maxOffset) = telegramBot.pull(TelegramBot.PullRequest(offset + 1))

            if (messages.isEmpty() || maxOffset == null) {
                return
            }

            eventChannel.push { db ->

                val newOffset = maxOffset.value
                db.storeValue(STORE_NAME, STORE_KEY, newOffset.toString())

                messages.asSequence().map {
                    EventChannel.StoredEvent(
                        TgOutboundActor.INBOUND_CHANNEL,
                        Instant.now(),
                        it.toJson(),
                    )
                }.toList().also {
                    logger.info { "Store inbound event: $it with offset $newOffset" }
                }
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
