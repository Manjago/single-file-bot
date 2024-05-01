package com.temnenkov.singlefilebot.channel.impl

import com.temnenkov.singlefilebot.channel.EventChannel
import com.temnenkov.singlefilebot.utils.runInTransaction
import org.h2.mvstore.tx.TransactionMap
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EventChannelMvStoreTest {
    private lateinit var eventChannelMvStore: EventChannelMvStore

    @BeforeEach
    fun setUp() {
        eventChannelMvStore = EventChannelMvStore()
    }

    @Test
    fun push() {
        eventChannelMvStore.push("123") {
            listOf(TestStoredEvent("test-1"))
        }
        runInTransaction("123") { ts, t ->
            val openMap: TransactionMap<DbKey, String> = t.openMap(EventChannelMvStore.eventTypeToString(EventChannel.EventType.TG_INBOUND))
            println(openMap)
        }
    }

    class TestStoredEvent(val stringPayload: String) : EventChannel.StoredEvent {
        override val eventType: EventChannel.EventType
            get() = EventChannel.EventType.TG_INBOUND
    }
}
