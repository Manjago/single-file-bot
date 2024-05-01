package com.temnenkov.singlefilebot.channel

import java.io.Serializable

interface EventChannel {
    fun push(eventsProducingAction: () -> List<StoredEvent>)

    fun pull(
        eventType: EventType,
        arg: (StoredEvent) -> List<StoredEvent>?,
    )

    interface StoredEvent : Serializable {
        val eventType: EventType
    }

    enum class EventType {
        TG_INBOUND,
        TG_OUTBOUND,
    }
}
