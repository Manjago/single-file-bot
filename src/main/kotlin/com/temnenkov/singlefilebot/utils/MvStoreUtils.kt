package com.temnenkov.singlefilebot.utils

import org.h2.mvstore.MVStore
import org.h2.mvstore.tx.Transaction
import org.h2.mvstore.tx.TransactionStore

fun <T> runInTransaction(dbFile: String?, block: (TransactionStore, Transaction) -> T): Result<T> {
    MVStore.open(dbFile).use { store: MVStore ->
        val ts = TransactionStore(store)
        val transaction = ts.begin()
        return runCatching {
            block(ts, transaction)
        }.onSuccess {
            transaction.commit()
        }.onFailure { transaction.rollback() }
    }

}