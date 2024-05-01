package com.temnenkov.singlefilebot.utils

import java.time.Instant

interface NowProvider {
    fun now(): Instant
}
