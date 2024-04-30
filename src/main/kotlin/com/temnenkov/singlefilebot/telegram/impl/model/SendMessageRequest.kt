package com.temnenkov.singlefilebot.telegram.impl.model

import com.google.gson.annotations.SerializedName

data class SendMessageRequest(
    @SerializedName("chat_id") val chatId: Long,
    val text: String,
    @SerializedName("reply_to_message_id") val replyToMessageId: Long? = null,
)
