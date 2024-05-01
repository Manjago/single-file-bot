package com.temnenkov.singlefilebot.channel

import java.io.Serializable
import java.time.Instant

interface EventChannel {
    fun push(eventsProducingAction: () -> List<StoredEvent>)

    fun pull(
        eventType: String,
        arg: (StoredEvent) -> List<StoredEvent>?,
    )

    data class StoredEvent(
        val eventType: String,
        val fireDate: Instant,
        val payload: String,
    ) : Serializable
}
