package com.temnenkov.singlefilebot.telegram.impl.model

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("message_id") val messageId: Long,
    val from: User? = null,
    val text: String? = null,
)
