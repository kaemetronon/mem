package io.mem.kek.memtg.domain.ya

import com.fasterxml.jackson.databind.ObjectMapper
import io.mem.kek.memtg.domain.ya.model.request.YaBody
import io.mem.kek.memtg.domain.ya.model.request.YaOptions
import io.mem.kek.memtg.domain.ya.model.response.Message
import io.mem.kek.memtg.domain.ya.model.response.YaResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class YaService(
    @Value("\${ya.host}") val host: String,
    @Value("\${ya.token}") val token: String,
    @Value("\${ya.folder-id}") val folderId: String,
    private val objectMapper: ObjectMapper = ObjectMapper(),
    private val client: WebClient = WebClient.create(host)
) {

    private val messageHistory: MutableList<Message> = mutableListOf()

    fun sendMessage(text: String): String {

        val message = Message(text = text)
        messageHistory.add(message)
        val body = YaBody(
            modelUri = "gpt://$folderId/yandexgpt/rc",
            completionOptions = YaOptions(),
            messages = messageHistory
        )

        val response = client.post()
            .header("Authorization", "Bearer $token")
            .header("x-folder-id", folderId)
            .bodyValue(objectMapper.writeValueAsString(body))
            .retrieve()
            .bodyToMono(YaResponse::class.java)
            .block()!!.result.alternatives[0].message
        messageHistory.add(response)

        return response.text
    }
}