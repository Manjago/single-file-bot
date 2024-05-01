package com.temnenkov.singlefilebot.channel.impl

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import com.temnenkov.singlefilebot.channel.EventChannel
import com.temnenkov.singlefilebot.utils.MvStoreWrapper
import com.temnenkov.singlefilebot.utils.NowProvider
import mu.KotlinLogging
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
        val fireDate = Instant.now()
        eventChannelMvStore.push {
            listOf(EventChannel.StoredEvent("ev0", fireDate, "test0"))
        }

        val map =
            mvStoreWrapper.runInTransaction { t ->
                val openMap: TransactionMap<DbKey, String> = t.openMap("ev0")
                openMap
            }.getOrNull()

        assertTrue(map is TransactionMap)
        assertEquals(1, map.size)
        val dbKey = DbKey(fireDate, 0L)
        assertEquals(dbKey, map.keys.firstOrNull())
        assertEquals(EventChannel.StoredEvent("ev0", fireDate, "test0"), EventChannel.StoredEvent("ev0", dbKey.fireDate, map[dbKey]!!))
    }

    @Test
    fun pull() {
        val now = Instant.now()
        nowAnswers.add(now)
        nowAnswers.add(now)

        eventChannelMvStore.push {
            listOf(EventChannel.StoredEvent("ev1", now, "test1"))
        }

        eventChannelMvStore.pull("ev1") { it, _ ->
            if (it.payload == "test1") {
                listOf(EventChannel.StoredEvent("ev2", now, "test2"), EventChannel.StoredEvent("ev3", now, "test3"))
            } else {
                null
            }
        }

        val map1 =
            mvStoreWrapper.runInTransaction { t ->
                val openMap: TransactionMap<DbKey, String> = t.openMap("ev1")
                openMap
            }.getOrNull()
        val map2 =
            mvStoreWrapper.runInTransaction { t ->
                val openMap: TransactionMap<DbKey, String> = t.openMap("ev2")
                openMap
            }.getOrNull()
        val map3 =
            mvStoreWrapper.runInTransaction { t ->
                val openMap: TransactionMap<DbKey, String> = t.openMap("ev3")
                openMap
            }.getOrNull()

        assertNotNull(map1)
        assertTrue { map1.isEmpty() }
        assertNotNull(map2)
        assertEquals(1, map2.size)
        val dbKey2 = DbKey(now, 1L)
        assertEquals(dbKey2, map2.keys.firstOrNull())
        assertEquals(EventChannel.StoredEvent("ev2", now, "test2"), EventChannel.StoredEvent("ev2", now, map2[dbKey2]!!))

        assertNotNull(map3)
        assertEquals(1, map3.size)
        val dbKey3 = DbKey(now, 2L)
        assertEquals(dbKey3, map3.keys.firstOrNull())
        assertEquals(EventChannel.StoredEvent("ev3", now, "test3"), EventChannel.StoredEvent("ev3", now, map3[dbKey3]!!))
    }

    @Test
    fun pullToEarly() {
        val now = Instant.now()
        nowAnswers.add(now)
        nowAnswers.add(now)

        val tooLateNow = now.plusSeconds(10)
        val futureDate = now.plusSeconds(100)

        eventChannelMvStore.push {
            listOf(
                EventChannel.StoredEvent("ev1", tooLateNow, "test1"),
                EventChannel.StoredEvent("ev1", now, "test2"),
            )
        }

        eventChannelMvStore.pull("ev1") { it, _ ->
            if (it.payload == "test2") {
                listOf(EventChannel.StoredEvent("ev2", futureDate, "ok"))
            } else {
                null
            }
        }

        val mapSource =
            mvStoreWrapper.runInTransaction { t ->
                val openMap: TransactionMap<DbKey, String> = t.openMap("ev1")
                openMap
            }.getOrNull()

        val mapTarget =
            mvStoreWrapper.runInTransaction { t ->
                val openMap: TransactionMap<DbKey, String> = t.openMap("ev2")
                openMap
            }.getOrNull()

        println(mapSource)
        println(mapTarget)

        assertNotNull(mapSource)
        assertEquals(1, mapSource.size)
        val dbKeySource = DbKey(tooLateNow, 0L)
        assertEquals(dbKeySource, mapSource.keys.firstOrNull())
        assertEquals(
            EventChannel.StoredEvent("ev1", tooLateNow, "test1"),
            EventChannel.StoredEvent("ev1", dbKeySource.fireDate, mapSource[dbKeySource]!!),
        )

        assertNotNull(mapTarget)
        assertEquals(1, mapTarget.size)
        val dbKeyTarget = DbKey(futureDate, 2L)
        assertEquals(dbKeyTarget, mapTarget.keys.firstOrNull())
        assertEquals(
            EventChannel.StoredEvent("ev2", futureDate, "ok"),
            EventChannel.StoredEvent("ev2", dbKeyTarget.fireDate, mapTarget[dbKeyTarget]!!),
        )

        eventChannelMvStore.pull("ev1") { _, _ ->
            throw IllegalStateException("must not enter")
        }
    }

    class TestNowProvider(private val answers: MutableList<Instant>) : NowProvider {
        private val index = AtomicInteger(0)

        override fun now(): Instant =
            answers.get(index.getAndIncrement()).also {
                logger.debug { "Check now $it" }
            }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
