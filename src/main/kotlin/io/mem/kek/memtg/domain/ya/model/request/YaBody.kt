package io.mem.kek.memtg.domain.ya.model.request

import io.mem.kek.memtg.domain.ya.model.response.Message

data class YaBody(
    val modelUri: String,
    val completionOptions: YaOptions,
    val messages: MutableList<Message>
)
