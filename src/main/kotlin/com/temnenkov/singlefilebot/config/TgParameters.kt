package com.temnenkov.singlefilebot.config

data class TgParameters(
    val httpClientConnectTimeout: Long,
    val token: String,
    val longPollingTimeout: Long,
    val pushTimeout: Long
) {
    companion object {
        fun loadFromSystemProperties() : TgParameters {
           val token = System.getProperty("tg.token") ?: throw IllegalStateException("token not defined")
           return TgParameters(
               httpClientConnectTimeout = System.getProperty("tg.connect.timeout", "5").toLong(),
               token = token,
               longPollingTimeout = System.getProperty("tg.longpolling.timeout", "10").toLong(),
               pushTimeout = System.getProperty("tg.push.timeout", "0").toLong()
           )
        }
    }
}
