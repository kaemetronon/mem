package io.mem.kek.memtg.domain.ya.model.request

data class YaOptions(
    val stream: Boolean = false,
    val temperature: Float = 0.3F,
    val maxTokens: Int = 5000
)
