package com.temnenkov.singlefilebot.actor

import com.temnenkov.singlefilebot.channel.EventChannel
import com.temnenkov.singlefilebot.telegram.TelegramBot
import com.temnenkov.singlefilebot.telegram.impl.model.Message
import com.temnenkov.singlefilebot.utils.fromJson
import mu.KotlinLogging

class TgOutboundActor(
    private val telegramBot: TelegramBot,
    private val eventChannel: EventChannel,
) : Runnable {
    override fun run() {
        eventChannel.pull(INBOUND_CHANNEL) { event, _ ->

            val message = event.payload.fromJson(Message::class.java)

            logger.info { "GHot message $message" }
            val addressTo = message.from?.id

            if (addressTo != null) {
                val request =
                    TelegramBot.PushRequest(
                        TelegramBot.Address(addressTo),
                        TelegramBot.MessageText("Hi ${message.text}"),
                    )
                telegramBot.push(request).also { logger.info { "Sent request $request got response $it" } }
            }
            listOf()
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
        const val INBOUND_CHANNEL = "TgInboundActor._inbound"
    }
}
