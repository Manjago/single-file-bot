package com.temnenkov.singlefilebot.channel

import java.io.Serializable
import java.time.Instant

interface EventChannel {
    fun push(eventsProducingAction: (Db) -> List<StoredEvent>)

    fun pull(
        eventType: String,
        eventsProduvingAction: (StoredEvent, Db) -> List<StoredEvent>?,
    )

    fun doInTransaction(action: (Db) -> String) : String

    data class StoredEvent(
        val eventType: String,
        val fireDate: Instant,
        val payload: String,
    ) : Serializable

    interface Db {
        fun loadValue(
            collection: String,
            key: String,
        ): String?

        fun storeValue(
            collection: String,
            key: String,
            value: String,
        )
    }
}
