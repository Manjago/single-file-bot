package com.temnenkov.singlefilebot.telegram.impl

import com.temnenkov.singlefilebot.config.TgParameters
import com.temnenkov.singlefilebot.telegram.TelegramBot
import com.temnenkov.singlefilebot.telegram.TelegramBot.TgMessage
import com.temnenkov.singlefilebot.telegram.impl.model.GetUpdatesResponse
import com.temnenkov.singlefilebot.telegram.impl.model.GetUpdatestRequest
import com.temnenkov.singlefilebot.telegram.impl.model.SendMessageRequest
import com.temnenkov.singlefilebot.telegram.impl.model.SendMessageResponse
import com.temnenkov.singlefilebot.utils.fromJson
import com.temnenkov.singlefilebot.utils.toJson
import mu.KotlinLogging
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
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

        val httpResponse =
            httpClient.send(
                requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString(),
            )

        val (messages, maxOffset) =
            if (httpResponse.statusCode() == 200) {
                logger.info { "got telegram response ${httpResponse.body()}" }
                val response = httpResponse.body().fromJson(GetUpdatesResponse::class.java)
                response.result.asSequence().filter {
                    it.message?.from != null && it.message.text != null
                }.map {
                    TgMessage(
                        TelegramBot.MessageId(it.message!!.messageId),
                        TelegramBot.Address(it.message.from!!.id),
                        TelegramBot.MessageText(it.message.text!!),
                    )
                }.toList() to response.maxOffset()
            } else {
                logger.error { "bad status code ${httpResponse.statusCode()} text ${httpResponse.body()}" }
                listOf<TgMessage>() to null
            }

        return TelegramBot.PullResponse(messages, maxOffset?.let { TelegramBot.Offset(it) })
    }

    override fun push(request: TelegramBot.PushRequest): TelegramBot.PushResponse {
        val httpRequest =
            with(request) {
                SendMessageRequest(addressTO.value, messageText.value)
            }

        val requestBuilder =
            HttpRequest.newBuilder(URI.create("https://api.telegram.org/${config.token}/sendMessage"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(config.pushTimeout))
                .POST(HttpRequest.BodyPublishers.ofString(httpRequest.toJson()))

        val httpResponse =
            httpClient.send(
                requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString(),
            )

        val ok =
            if (httpResponse.statusCode() != 200) {
                logger.error { "bad status code ${httpResponse.statusCode()} text ${httpResponse.body()}" }
                null
            } else {
                logger.info { "got telegram response ${httpResponse.body()}" }
                httpResponse.body().fromJson(SendMessageResponse::class.java).ok
            }
        return TelegramBot.PushResponse(ok)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
