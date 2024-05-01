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
        eventChannelMvStore.push("target/db123") {
            listOf(EventChannel.StoredEvent("ev1", "test1"))
        }
        runInTransaction("target/db123") { ts, t ->
            val openMap: TransactionMap<DbKey, String> = t.openMap("ev1")
            println(openMap)
        }
    }
}
