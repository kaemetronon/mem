package io.mem.kek.memtg.domain.ya.model.response

data class YaResult(
    val alternatives: List<Alternative>,
    val usage: Usage,
    val modelVersion: String
)
