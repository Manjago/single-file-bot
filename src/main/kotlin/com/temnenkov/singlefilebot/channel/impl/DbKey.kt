package com.temnenkov.singlefilebot.channel.impl

import java.time.Instant

class DbKey(val fireDate: Instant, val id: Long) : Comparable<DbKey> {
    override fun compareTo(other: DbKey): Int = COMPARATOR.compare(this, other)

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            javaClass != other?.javaClass -> false
            else -> compareTo(other as DbKey) == 0
        }

    override fun hashCode(): Int {
        var result = fireDate.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "DbKey(fireDate=$fireDate, id=$id)"
    }

    companion object {
        private val COMPARATOR =
            Comparator
                .comparing(DbKey::fireDate)
                .thenComparingLong(DbKey::id)
    }
}
