package io.mem.kek.memtg.domain.ya.model.request

data class YaMessage(
    val role: String = "user",
    val text: String
)