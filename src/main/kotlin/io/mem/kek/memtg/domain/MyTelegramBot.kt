package io.mem.kek.memtg.domain

import io.mem.kek.memtg.domain.ya.YaOneExpertService
import io.mem.kek.memtg.domain.ya.YaService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
class MyTelegramBot(
    @Value("\${bot.username}") private val username: String,
    @Value("\${bot.token}") private val token: String,
    private val yaService: YaService,
//    private val yaConversationService: YaConversationService
    private val yaOneExpertService: YaOneExpertService
) : TelegramLongPollingBot() {

    override fun onUpdateReceived(update: Update) {
        val message: Message = update.message
        val text = message.text
        val response: String
        if (text.contains("reset")) {
            yaOneExpertService.resetContext()
            return
        } else {
            response = yaOneExpertService.sendMessage(text)
        }
//        val response = yaConversationService.sendMessage(update.message.text) { botMsg ->
//            sendResponse(
//                message.chatId,
//                botMsg
//            )
//        }
        sendResponse(message.chatId, response)
    }

    private fun sendResponse(chatId: Long, text: String) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = text
        message.parseMode = ParseMode.MARKDOWN

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun getBotUsername() = username

    override fun getBotToken() = token
}