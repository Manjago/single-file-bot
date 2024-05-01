package com.temnenkov.singlefilebot.channel

import java.io.Serializable
import java.time.Instant

interface EventChannel {
    fun push(eventsProducingAction: (Db) -> List<StoredEvent>)

    fun pull(
        eventType: String,
        eventsProduvingAction: (StoredEvent, Db) -> List<StoredEvent>?,
    )

    fun getDbForTransaction() : Db

    data class StoredEvent(
        val eventType: String,
        val fireDate: Instant,
        val payload: String,
    ) : Serializable

    interface Db {
        fun loadLongValue(
            collection: String,
            key: String,
        ): Long?

        fun storeLongValue(
            collection: String,
            key: String,
            value: Long,
        )
    }
}
