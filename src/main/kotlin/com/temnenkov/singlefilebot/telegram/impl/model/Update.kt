package com.temnenkov.singlefilebot.telegram.impl.model

import com.google.gson.annotations.SerializedName

data class Update(
    @SerializedName("update_id") val updateId: Long,
    val message: Message? = null,
)
