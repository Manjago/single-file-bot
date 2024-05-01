package com.temnenkov.singlefilebot.channel.impl

import com.temnenkov.singlefilebot.channel.EventChannel
import com.temnenkov.singlefilebot.utils.NowProvider
import com.temnenkov.singlefilebot.utils.runInTransaction
import org.h2.mvstore.tx.Transaction
import org.h2.mvstore.tx.TransactionMap
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class EventChannelMvStore(val nowProvider: NowProvider) : EventChannel {
    private val counter = AtomicLong(0)

    override fun push(
        storeId: String?,
        eventsProducingAction: () -> List<EventChannel.StoredEvent>,
    ) {
        runInTransaction(
            storeId,
        ) { ts, transaction ->
            val maps: MutableMap<String, TransactionMap<DbKey, String>> = mutableMapOf()
            eventsProducingAction().forEach {
                store(maps, it, transaction)
            }
        }
    }

    override fun pull(
        storeId: String?,
        eventType: String,
        arg: (EventChannel.StoredEvent) -> List<EventChannel.StoredEvent>?,
    ) {
        runInTransaction(
            storeId,
        ) { ts, transaction ->
            val mvMap: TransactionMap<DbKey, String> = transaction.openMap(eventType)
            val keyIterator = mvMap.keyIterator(null)
            if (keyIterator.hasNext()) {
                val dbKey = keyIterator.next()
                if (dbKey.fireDate < nowProvider.now()) {
                    val maps: MutableMap<String, TransactionMap<DbKey, String>> = mutableMapOf()
                    arg(EventChannel.StoredEvent(eventType, mvMap[dbKey]!!))!!.forEach {
                        store(maps, it, transaction)
                    }
                    mvMap.remove(dbKey)
                }
            }
        }
    }

    private fun store(
        maps: MutableMap<String, TransactionMap<DbKey, String>>,
        storedEvent: EventChannel.StoredEvent,
        transaction: Transaction,
    ) {
        if (!maps.containsKey(storedEvent.eventType)) {
            maps[storedEvent.eventType] = transaction.openMap(storedEvent.eventType)
        }
        maps[storedEvent.eventType]!![DbKey(nowProvider.now(), counter.getAndIncrement())] = storedEvent.payload
    }
}
