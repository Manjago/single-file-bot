package com.temnenkov.singlefilebot.channel.impl

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import com.temnenkov.singlefilebot.channel.EventChannel
import com.temnenkov.singlefilebot.utils.MvStoreWrapper
import com.temnenkov.singlefilebot.utils.NowProvider
import org.h2.mvstore.tx.TransactionMap
import org.junit.jupiter.api.AfterEach
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
    private lateinit var mvStoreWrapper: MvStoreWrapper

    @BeforeEach
    fun setUp() {
        mvStoreWrapper = MvStoreWrapper("target/{${NanoIdUtils.randomNanoId()}}")
        nowAnswers = mutableListOf()
        eventChannelMvStore = EventChannelMvStore(mvStoreWrapper, TestNowProvider(nowAnswers))
    }

    @AfterEach
    fun tearDown() {
        mvStoreWrapper.release()
    }

    @Test
    fun push() {
        val now = Instant.now()
        nowAnswers.add(now)
        val dbName = "target/${NanoIdUtils.randomNanoId()}"
        eventChannelMvStore.push() {
            listOf(EventChannel.StoredEvent("ev0", "test0"))
        }

        val map =
            mvStoreWrapper.runInTransaction() { _, t ->
                val openMap: TransactionMap<DbKey, String> = t.openMap("ev0")
                openMap
            }.getOrNull()

        assertTrue(map is TransactionMap)
        assertEquals(1, map.size)
        val dbKey = DbKey(now, 0L)
        assertEquals(dbKey, map.keys.firstOrNull())
        assertEquals(EventChannel.StoredEvent("ev0", "test0"), EventChannel.StoredEvent("ev0", map[dbKey]!!))
    }

    @Test
    fun pull() {
        val now = Instant.now()
        nowAnswers.add(now)
        nowAnswers.add(now.plusSeconds(1))
        nowAnswers.add(now.plusSeconds(2))
        nowAnswers.add(now.plusSeconds(3))

        val dbName = "target/${NanoIdUtils.randomNanoId()}"

        eventChannelMvStore.push() {
            listOf(EventChannel.StoredEvent("ev1", "test1"))
        }

        eventChannelMvStore.pull("ev1") {
            if (it.payload == "test1") {
                listOf(EventChannel.StoredEvent("ev2", "test2"), EventChannel.StoredEvent("ev3", "test3"))
            } else {
                null
            }
        }

        val map1 =
            mvStoreWrapper.runInTransaction() { _, t ->
                val openMap: TransactionMap<DbKey, String> = t.openMap("ev1")
                openMap
            }.getOrNull()
        val map2 =
            mvStoreWrapper.runInTransaction() { _, t ->
                val openMap: TransactionMap<DbKey, String> = t.openMap("ev2")
                openMap
            }.getOrNull()
        val map3 =
            mvStoreWrapper.runInTransaction() { _, t ->
                val openMap: TransactionMap<DbKey, String> = t.openMap("ev3")
                openMap
            }.getOrNull()

        assertNotNull(map1)
        assertTrue { map1.isEmpty() }
        assertNotNull(map2)
        assertEquals(1, map2.size)
        val dbKey2 = DbKey(nowAnswers[2], 1L)
        assertEquals(dbKey2, map2.keys.firstOrNull())
        assertEquals(EventChannel.StoredEvent("ev2", "test2"), EventChannel.StoredEvent("ev2", map2[dbKey2]!!))

        assertNotNull(map3)
        assertEquals(1, map3.size)
        val dbKey3 = DbKey(nowAnswers[3], 2L)
        assertEquals(dbKey3, map3.keys.firstOrNull())
        assertEquals(EventChannel.StoredEvent("ev3", "test3"), EventChannel.StoredEvent("ev3", map3[dbKey3]!!))
    }

    class TestNowProvider(private val answers: MutableList<Instant>) : NowProvider {
        private val index = AtomicInteger(0)

        override fun now(): Instant = answers.get(index.getAndIncrement())
    }
}
