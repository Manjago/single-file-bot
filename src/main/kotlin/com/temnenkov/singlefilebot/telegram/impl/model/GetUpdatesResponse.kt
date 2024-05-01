package com.temnenkov.singlefilebot.telegram.impl.model

data class GetUpdatesResponse(
    val ok: Boolean? = null,
    val result: List<Update> = listOf(),
) {
    fun maxOffset() =
        if (result.isEmpty()) {
            null
        } else {
            result.maxOf { it.updateId }
        }
}
