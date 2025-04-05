package io.mem.kek.memtg.domain.ya

import io.mem.kek.memtg.domain.ya.model.request.YaBody
import io.mem.kek.memtg.domain.ya.model.request.YaOptions
import io.mem.kek.memtg.domain.ya.model.response.Message
import io.mem.kek.memtg.domain.ya.model.response.YaResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.time.measureTime

@Service
class YaOneExpertService(
    @Value("\${ya.folder-id}") val folderId: String,
    @Value("\${ya.model}") val model: String,
    private val yaClient: YaClient
) {

    private val messageHistory: MutableList<Message> = mutableListOf()

    fun resetContext() = messageHistory.clear().also { println("Контекст сброшен.") }

    fun generateAndInitializeContext(userPrompt: String) {
        val systemPrompt = Message(
            role = "system",
            text = "Пользователь пришлет свой запрос, тебе нужно будет сгенерировать промпт, дополняющий" +
                    " и дополнительно описывающий его запрос для получения максимально релевантной информации"
        )

        val initMessage = Message(role = "user", text = userPrompt)

        val body = YaBody(
            modelUri = "gpt://$folderId$model",
            completionOptions = YaOptions(),
            messages = mutableListOf(systemPrompt, initMessage)
        )

        val systemMessage = call(body) { }

        val newSystemPrompt = Message(role = "system", text = systemMessage.text)
        messageHistory.add(newSystemPrompt)

        println("Сгенерированный system-промпт: ${systemMessage.text}")
    }

    fun sendMessage(userText: String): String {
        if (messageHistory.isEmpty()) {
            generateAndInitializeContext(userText)
        }

        messageHistory.add(Message(role = "user", text = userText))

        val body = YaBody(
            modelUri = "gpt://$folderId$model",
            completionOptions = YaOptions(),
            messages = messageHistory
        )

        val responseMessage = call(body) { message ->
            messageHistory.add(message)
        }

        return responseMessage.text
    }

    private fun call(body: YaBody, saver: (message: Message) -> Unit): Message {
        println("Sending message to model")
        val result: YaResult

        measureTime { result = yaClient.call(body) }
            .also { println("Request time: $it") }

        val message = result.alternatives[0].message
        saver(message)

        println("Бот: ${message.text}\n")
        return message
    }
}