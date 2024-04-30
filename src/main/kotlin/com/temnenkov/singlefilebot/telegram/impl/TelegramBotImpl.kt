package com.temnenkov.singlefilebot.telegram.impl

import com.temnenkov.singlefilebot.config.TgParameters
import com.temnenkov.singlefilebot.telegram.TelegramBot
import java.net.http.HttpClient
import java.time.Duration

class TelegramBotImpl(val config: TgParameters) : TelegramBot {
    private val httpClient: HttpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(config.httpClientConnectTimeout))
            .version(HttpClient.Version.HTTP_1_1).build()

    override fun pull(request: TelegramBot.PullRequest): TelegramBot.PullResponse {
        TODO("Not yet implemented")
    }

    override fun push(request: TelegramBot.PushRequest) {
        TODO("Not yet implemented")
    }
}
