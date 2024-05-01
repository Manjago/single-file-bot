package com.temnenkov.singlefilebot.utils

import mu.KotlinLogging
import org.h2.mvstore.MVStore
import org.h2.mvstore.tx.Transaction
import org.h2.mvstore.tx.TransactionStore

private val logger = KotlinLogging.logger {}

fun <T> runInTransaction(
    dbFile: String?,
    block: (TransactionStore, Transaction) -> T,
): Result<T> {
    MVStore.open(dbFile).use { store: MVStore ->
        val ts = TransactionStore(store)
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
}
