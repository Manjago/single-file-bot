package com.temnenkov.singlefilebot.channel.impl

import com.temnenkov.singlefilebot.channel.EventChannel
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
        eventChannelMvStore.push {
            listOf(TestStoredEvent("test-1"))
        }
    }

    class TestStoredEvent(val stringPayload: String) : EventChannel.StoredEvent {
        override val eventType: EventChannel.EventType
            get() = EventChannel.EventType.TG_INBOUND
    }
}
