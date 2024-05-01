package com.temnenkov.singlefilebot.telegram

import com.temnenkov.singlefilebot.telegram.impl.model.Message

interface TelegramBot {
    fun pull(request: PullRequest): PullResponse

    fun push(request: PushRequest): PushResponse

    @JvmInline
    value class MessageId(val value: Long)

    @JvmInline
    value class Address(val value: Long)

    @JvmInline
    value class Offset(val value: Long)

    @JvmInline
    value class MessageText(val value: String)

    data class PullRequest(val offset: Long)

    data class PullResponse(val messages: List<Message>, val maxOffset: Offset?)

    data class PushRequest(val addressTO: Address, val messageText: MessageText)

    data class PushResponse(val ok: Boolean?)
}
