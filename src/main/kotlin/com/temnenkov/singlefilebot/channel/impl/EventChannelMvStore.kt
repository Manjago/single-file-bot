package com.temnenkov.singlefilebot.channel.impl

import com.temnenkov.singlefilebot.channel.EventChannel
import com.temnenkov.singlefilebot.utils.MvStoreWrapper
import com.temnenkov.singlefilebot.utils.NowProvider
import mu.KotlinLogging
import org.h2.mvstore.tx.Transaction
import org.h2.mvstore.tx.TransactionMap
import java.util.concurrent.atomic.AtomicLong

class EventChannelMvStore(
    val mvStoreWrapper: MvStoreWrapper,
    val nowProvider: NowProvider,
) : EventChannel {
    private val counter = AtomicLong(0)

    override fun push(eventsProducingAction: (EventChannel.Db) -> List<EventChannel.StoredEvent>) {
        val stored: Result<List<Pair<DbKey, EventChannel.StoredEvent>>> =
            mvStoreWrapper.runInTransaction { transaction ->
                val maps: MutableMap<String, TransactionMap<DbKey, String>> = mutableMapOf()
                eventsProducingAction(DbImpl(transaction)).map {
                    store(maps, it, transaction)
                }.toList()
            }
        logger.debug { "Pushed $stored" }
        stored.onFailure {
            logger.error(it) { "Fail push" }
            throw it
        }
    }

    override fun pull(
        eventType: String,
        eventsProduvingAction: (EventChannel.StoredEvent, EventChannel.Db) -> List<EventChannel.StoredEvent>?,
    ) {
        mvStoreWrapper.runInTransaction { transaction ->
            val mvMap: TransactionMap<DbKey, String> = transaction.openMap(eventType)
            val keyIterator = mvMap.keyIterator(null)
            if (keyIterator.hasNext()) {
                val dbKey = keyIterator.next()
                if (dbKey.fireDate <= nowProvider.now()) {
                    val maps: MutableMap<String, TransactionMap<DbKey, String>> = mutableMapOf()
                    eventsProduvingAction(
                        EventChannel.StoredEvent(eventType, dbKey.fireDate, mvMap[dbKey]!!),
                        DbImpl(transaction),
                    )!!.forEach {
                        store(maps, it, transaction)
                    }
                    mvMap.remove(dbKey)
                } else {
                    logger.debug { "Too early ${dbKey.fireDate}" }
                }
            }
        }
    }

    override fun doInTransaction(action: (EventChannel.Db) -> String): String {
        return mvStoreWrapper.runInTransaction { transaction ->
            val db = DbImpl(transaction)
            action(db)
        }.getOrThrow()
    }

    private fun store(
        maps: MutableMap<String, TransactionMap<DbKey, String>>,
        storedEvent: EventChannel.StoredEvent,
        transaction: Transaction,
    ): Pair<DbKey, EventChannel.StoredEvent> {
        if (!maps.containsKey(storedEvent.eventType)) {
            maps[storedEvent.eventType] = transaction.openMap(storedEvent.eventType)
        }
        val dbKey = DbKey(storedEvent.fireDate, counter.getAndIncrement())
        maps[storedEvent.eventType]!![dbKey] = storedEvent.payload
        return dbKey to storedEvent
    }

    class DbImpl(private val transaction: Transaction) : EventChannel.Db {
        override fun loadValue(
            collection: String,
            key: String,
        ): String? {
            val map: TransactionMap<String, String> = transaction.openMap(collection)
            return map[key]
        }

        override fun storeValue(
            collection: String,
            key: String,
            value: String,
        ) {
            val map: TransactionMap<String, String> = transaction.openMap(collection)
            map[key] = value
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
