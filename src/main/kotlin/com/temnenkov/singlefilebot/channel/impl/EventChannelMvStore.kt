package com.temnenkov.singlefilebot.channel.impl

import com.temnenkov.singlefilebot.channel.EventChannel
import com.temnenkov.singlefilebot.channel.EventChannel.EventType.TG_INBOUND
import com.temnenkov.singlefilebot.channel.EventChannel.EventType.TG_OUTBOUND
import com.temnenkov.singlefilebot.utils.runInTransaction
import com.temnenkov.singlefilebot.utils.toJson
import org.h2.mvstore.MVStore
import org.h2.mvstore.tx.Transaction
import org.h2.mvstore.tx.TransactionMap
import org.h2.mvstore.tx.TransactionStore
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class EventChannelMvStore : EventChannel {
    private val counter = AtomicLong(0)

    override fun push(storeId: String?, eventsProducingAction: () -> List<EventChannel.StoredEvent>) {
        runInTransaction(storeId, fun(ts: TransactionStore, transaction: Transaction) {

            val maps: MutableMap<EventChannel.EventType, TransactionMap<DbKey, String>> = mutableMapOf()
            eventsProducingAction().forEach {
                if (!maps.containsKey(it.eventType)) {
                    maps[it.eventType] = transaction.openMap(eventTypeToString(it.eventType))
                }
                maps[it.eventType]!![DbKey(Instant.now(), counter.getAndIncrement())] = it.toJson()
            }

        })
    }

    override fun pull(
        storeId: String?,
        eventType: EventChannel.EventType,
        arg: (EventChannel.StoredEvent) -> List<EventChannel.StoredEvent>?,
    ) {
        MVStore.open(storeId).use { store: MVStore ->
            val ts = TransactionStore(store)
            val transaction = ts.begin()
            runCatching {
                val mvMap: TransactionMap<DbKey, String> = transaction.openMap(eventTypeToString(eventType))
                val keyIterator = mvMap.keyIterator(null)
                if (keyIterator.hasNext()) {
                    val dbKey = keyIterator.next()
                    if (dbKey.fireDate < Instant.now()) {
                    }
                }
            }.onSuccess { transaction.commit() }.onFailure { transaction.rollback() }
        }
    }

    companion object {
        fun eventTypeToString(eventType: EventChannel.EventType) =
            when (eventType) {
                TG_INBOUND -> "0"
                TG_OUTBOUND -> "1"
            }
    }

}
