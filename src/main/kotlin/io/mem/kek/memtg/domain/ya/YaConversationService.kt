package io.mem.kek.memtg.domain.ya

import com.fasterxml.jackson.databind.ObjectMapper
import io.mem.kek.memtg.domain.ya.model.request.YaBody
import io.mem.kek.memtg.domain.ya.model.request.YaOptions
import io.mem.kek.memtg.domain.ya.model.response.Message
import io.mem.kek.memtg.domain.ya.model.response.YaResponse
import io.mem.kek.memtg.domain.ya.model.response.YaResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.TimeUnit
import kotlin.time.measureTime

@Service
class YaConversationService(
    @Value("\${ya.host}") val host: String,
    @Value("\${ya.token}") val token: String,
    @Value("\${ya.folder-id}") val folderId: String,
    @Value("\${ya.model}") val model: String,
    @Value("\${ya.tokens-limit}") val tokensLimit: Int,
    private val objectMapper: ObjectMapper = ObjectMapper(),
    private val client: WebClient = WebClient.create(host)
) {

    private val firstMessageHistory: MutableList<Message> = mutableListOf()
    private val secondMessageHistory: MutableList<Message> = mutableListOf()

    private var inputTokens: Int = 0
    private var outputTokens: Int = 0


    private var callback: (botMsg: String) -> Unit = { _ -> }

    fun sendMessage(text: String, callbackParam: (botMsg: String) -> Unit): String {
        callback = callbackParam
        val init = init()

        val firstResponse = call(init.first, "First") { message ->
            firstMessageHistory.add(message)
            secondMessageHistory.add(message.copy(role = "user"))
        }

        call(
            init.second.apply { this.messages.add(Message(text = firstResponse.text)) },
            "Second"
        ) { message ->
            firstMessageHistory.add(message.copy(role = "user"))
            secondMessageHistory.add(message)
        }

        while (inputTokens + inputTokens < tokensLimit) {
            call(
                YaBody(
                    modelUri = "gpt://$folderId$model",
                    completionOptions = YaOptions(),
                    messages = firstMessageHistory
                ), "First"
            ) { message ->
                firstMessageHistory.add(message)
                secondMessageHistory.add(message.copy(role = "user"))
            }

            call(
                YaBody(
                    modelUri = "gpt://$folderId$model",
                    completionOptions = YaOptions(),
                    messages = secondMessageHistory
                ), "Second"
            ) { message ->
                secondMessageHistory.add(message)
                firstMessageHistory.add(message.copy(role = "user"))
            }

            println("--- tokens used: Input: $inputTokens, Output: $outputTokens, Total: ${inputTokens + outputTokens}")
        }
        return "done"
    }

    private fun init(): Pair<YaBody, YaBody> {
        val firstSystem = Message(
            role = "system",
            text = "промпт для бота 1"
        )
        val firstBody = YaBody(
            modelUri = "gpt://$folderId$model",
            completionOptions = YaOptions(),
            messages = mutableListOf(firstSystem)
        )
        firstMessageHistory.addAll(listOf(firstSystem))
        val secondSystem = Message(
            role = "system",
            text = "промпт для бота 2"
        )
        val secondBody = YaBody(
            modelUri = "gpt://$folderId$model",
            completionOptions = YaOptions(),
            messages = mutableListOf(secondSystem)
        )
        secondMessageHistory.add(secondSystem)
        return Pair(firstBody, secondBody)
    }

    private fun call(body: YaBody, who: String, saver: (message: Message) -> Unit): Message {
        println("Send to $who")
        var result: YaResult
        measureTime {
            result = client.post()
                .header("Authorization", "Bearer $token")
                .header("x-folder-id", folderId)
                .bodyValue(objectMapper.writeValueAsString(body))
                .retrieve()
                .bodyToMono(YaResponse::class.java)
                .block()!!.result
        }.also { println("Request time: $it") }
        TimeUnit.SECONDS.sleep(3)

        inputTokens += result.usage.inputTextTokens
        outputTokens += result.usage.completionTokens
        return result.alternatives[0].message.also {
            saver(it)
            "Модель $who: ${it.text}".also { msg ->
                println(msg)
                callback(msg)
                println("-----------------------------------------------------------------------")
                println()
            }
        }
    }
}