package com.temnenkov.singlefilebot.channel.impl

import com.temnenkov.singlefilebot.channel.EventChannel
import com.temnenkov.singlefilebot.channel.EventChannel.EventType.*
import org.h2.mvstore.MVStore
import org.h2.mvstore.tx.TransactionMap
import org.h2.mvstore.tx.TransactionStore
import java.time.Instant


class EventChannelMvStore : EventChannel {

    override fun push(arg: () -> List<EventChannel.StoredEvent>) {

        MVStore.open(null).use { store: MVStore ->
            val ts = TransactionStore(store)
            val transaction = ts.begin()
            runCatching {
                val maps: MutableMap<EventChannel.EventType, TransactionMap<Instant, String>> = mutableMapOf()
                arg().forEach {
                   if (!maps.containsKey(it.eventType)) {
                       maps[it.eventType] = transaction.openMap(eventTypeToString(it.eventType))
                   }
                   maps[it.eventType].put(Instant.now())
                }
            }
                .onSuccess { transaction.commit() }
                .onFailure { transaction.rollback() }
        }
        TODO("Not yet implemented")
    }

    override fun pull(
        eventType: EventChannel.EventType,
        arg: (EventChannel.StoredEvent) -> List<EventChannel.StoredEvent>?
    ) {
        TODO("Not yet implemented")
    }

    private fun eventTypeToString(eventType: EventChannel.EventType) = when (eventType) {
        TG_INBOUND -> "0"
        TG_OUTBOUND -> "1"
    }

}