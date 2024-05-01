package com.temnenkov.singlefilebot.actor

import com.temnenkov.singlefilebot.channel.EventChannel
import com.temnenkov.singlefilebot.telegram.TelegramBot
import com.temnenkov.singlefilebot.telegram.impl.model.Message
import com.temnenkov.singlefilebot.utils.fromJson

class TgOutboundActor(
    private val telegramBot: TelegramBot,
    private val eventChannel: EventChannel,
) : Runnable {
    override fun run() {
        eventChannel.pull(INBOUND_CHANNEL) { event, _ ->

            val message = event.payload.fromJson(Message::class.java)

            val addressTo = message.from?.id

            if (addressTo != null) {
                telegramBot.push(
                    TelegramBot.PushRequest(
                        TelegramBot.Address(addressTo),
                        TelegramBot.MessageText("Hi ${message.text}"),
                    ),
                )
            }
            listOf()
        }
    }

    companion object {
        const val INBOUND_CHANNEL = "TgInboundActor._inbound"
    }
}
