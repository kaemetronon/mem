package io.mem.kek.memtg.domain.ya.model.response

data class Usage(
    val inputTextTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val completionTokensDetails: CompletionTokensDetails?
)