package io.mem.kek.memtg.domain.ya

import com.fasterxml.jackson.databind.ObjectMapper
import io.mem.kek.memtg.domain.ya.model.request.YaBody
import io.mem.kek.memtg.domain.ya.model.response.YaResponse
import io.mem.kek.memtg.domain.ya.model.response.YaResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime

@Service
class YaClient(
    @Value("\${ya.host}") val host: String,
    @Value("\${ya.folder-id}") val folderId: String,
    @Value("\${ya.iam-token}") val iamToken: String,
    private val objectMapper: ObjectMapper = ObjectMapper(),
    private val client: WebClient = WebClient.create(host)
) {

    private var yaIamToken: Pair<String, LocalDateTime>? = null

    private fun getToken(): String {
        return if (yaIamToken == null || yaIamToken?.second?.isBefore(LocalDateTime.now()) == true) {
            val response = WebClient.create("https://iam.api.cloud.yandex.net/iam/v1/tokens")
                .post()
                .bodyValue(objectMapper.writeValueAsString(TokenRequest(iamToken)))
                .retrieve()
                .bodyToMono(TokenResponse::class.java)
                .block()!!
            yaIamToken = Pair(response.iamToken, response.expiresAt)
            response.iamToken
        } else {
            (yaIamToken as Pair<String, LocalDateTime>).first
        }
    }

    fun call(body: YaBody): YaResult {
        val token = getToken()
        return client.post()
            .header("Authorization", "Bearer $token")
            .header("x-folder-id", folderId)
            .bodyValue(objectMapper.writeValueAsString(body))
            .retrieve()
            .bodyToMono(YaResponse::class.java)
            .block()!!.result
    }
}

data class TokenRequest(val yandexPassportOauthToken: String)

data class TokenResponse(
    val iamToken: String,
    val expiresAt: LocalDateTime
)