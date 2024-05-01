package com.temnenkov.singlefilebot.channel

import java.io.Serializable

interface EventChannel {
    fun push(
        storeId: String?,
        eventsProducingAction: () -> List<StoredEvent>,
    )

    fun pull(
        storeId: String?,
        eventType: String,
        arg: (StoredEvent) -> List<StoredEvent>?,
    )

    data class StoredEvent(
        val eventType: String,
        val payload: String,
    ) : Serializable
}
