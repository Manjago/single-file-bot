package com.temnenkov.singlefilebot.config

data class TgParameters(val httpClientConnectTimeout: Long, val token: String, val longPollingTimeout: Long)
