package com.temnenkov.singlefilebot.utils

import mu.KotlinLogging
import org.h2.mvstore.MVStore
import org.h2.mvstore.tx.Transaction
import org.h2.mvstore.tx.TransactionStore

class MvStoreWrapper(dbFile: String) {
    private val store = MVStore.open(dbFile)
    private val ts = TransactionStore(store)

    fun <T> runInTransaction(block: (TransactionStore, Transaction) -> T): Result<T> {
        val transaction = ts.begin()
        return runCatching {
            block(ts, transaction)
        }.onSuccess {
            logger.debug { "Transaction commited for $it" }
            transaction.commit()
        }.onFailure {
            logger.error(it) { "Transaction rollback" }
            transaction.rollback()
        }
    }

    fun release() = store.close()

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
