package com.temnenkov.singlefilebot.channel.impl

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import com.temnenkov.singlefilebot.channel.EventChannel
import com.temnenkov.singlefilebot.utils.NowProvider
import com.temnenkov.singlefilebot.utils.runInTransaction
import org.h2.mvstore.tx.TransactionMap
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EventChannelMvStoreTest {
    private lateinit var eventChannelMvStore: EventChannelMvStore
    private lateinit var nowAnswers: MutableList<Instant>

    @BeforeEach
    fun setUp() {
        nowAnswers = mutableListOf()
        eventChannelMvStore = EventChannelMvStore(TestNowProvider(nowAnswers))
    }

    @Test
    fun push() {
        val now = Instant.now()
        nowAnswers.add(now)
        val dbName = "target/${NanoIdUtils.randomNanoId()}"
        eventChannelMvStore.push(dbName) {
            listOf(EventChannel.StoredEvent("ev1", "test1"))
        }

        val map = runInTransaction(dbName){ _, t ->
            val openMap: TransactionMap<DbKey, String> = t.openMap("ev1")
            openMap
        }.getOrNull()

        assertTrue(map is TransactionMap)
        assertEquals(1, map.size)
        val dbKey = DbKey(now, 0L)
        assertEquals(dbKey, map.keys.firstOrNull())
        assertEquals(EventChannel.StoredEvent("ev1", "test1"), EventChannel.StoredEvent("ev1", map[dbKey]!!))
    }

    class TestNowProvider(val answers: MutableList<Instant>) : NowProvider {
        private val index = AtomicInteger(0)
        override fun now(): Instant = answers.get(index.getAndIncrement())
    }
}
