package com.temnenkov.singlefilebot.telegram.impl

import com.temnenkov.singlefilebot.config.TgParameters
import com.temnenkov.singlefilebot.telegram.TelegramBot
import com.temnenkov.singlefilebot.telegram.impl.model.GetUpdatestRequest
import com.temnenkov.singlefilebot.utils.toJson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.time.Duration

class TelegramBotImpl(val config: TgParameters) : TelegramBot {
    private val httpClient: HttpClient =
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(config.httpClientConnectTimeout))
            .version(HttpClient.Version.HTTP_1_1).build()

    override fun pull(request: TelegramBot.PullRequest): TelegramBot.PullResponse {
        val requestBuilder =
            HttpRequest.newBuilder(URI.create("https://api.telegram.org/${config.token}/getUpdates"))
                .header("Content-Type", "application/json").timeout(Duration.ofSeconds(config.longPollingTimeout))
                .POST(HttpRequest.BodyPublishers.ofString(GetUpdatestRequest(request.offset).toJson()))
        TODO("Not yet implemented")
    }

    override fun push(request: TelegramBot.PushRequest) {
        TODO("Not yet implemented")
    }
}
