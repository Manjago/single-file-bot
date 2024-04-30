package com.temnenkov.singlefilebot.telegram.impl.model

data class IncomingMessage(val messageId: Long, val from: Long, val updateId: Long, val text: String)
