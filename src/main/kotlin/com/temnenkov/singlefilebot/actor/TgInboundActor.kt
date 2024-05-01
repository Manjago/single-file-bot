package com.temnenkov.singlefilebot.actor

import com.temnenkov.singlefilebot.channel.EventChannel
import com.temnenkov.singlefilebot.telegram.TelegramBot
import com.temnenkov.singlefilebot.utils.MvStoreWrapper
import mu.KotlinLogging
import org.h2.mvstore.tx.TransactionMap

class TgInboundActor(
    private val telegramBot: TelegramBot,
    private val mvStoreWrapper: MvStoreWrapper,
    private val eventChannel: EventChannel,
) : Runnable {
    override fun run() {
        try {
            val offset =
                requireNotNull(
                    mvStoreWrapper.runInTransaction { transaction ->
                        val map: TransactionMap<String, Long> = transaction.openMap(STORE_NAME)
                        map[STORE_KEY]?.let { 0L }
                    }.getOrElse {
                        logger.error(it) { "Fail get offset" }
                        throw IllegalStateException("Fail get offset", it)
                    },
                ) { "Offset must be not null" }

            val (messages, maxOffset) = telegramBot.pull(TelegramBot.PullRequest(offset))

            eventChannel.push {
                listOf()
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
