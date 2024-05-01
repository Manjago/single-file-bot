package com.temnenkov.singlefilebot.channel

import java.io.Serializable

interface EventChannel {
    fun push(storeId: String?, eventsProducingAction: () -> List<StoredEvent>)

    fun pull(
        storeId: String?,
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
