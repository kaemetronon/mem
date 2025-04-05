package io.mem.kek.memtg.domain.config

import io.mem.kek.memtg.domain.MyTelegramBot
import io.mem.kek.memtg.domain.ya.YaService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Configuration
class TelegramBotConfig {

    @Bean
    fun telegramBot(
        myTelegramBot: MyTelegramBot
    ): TelegramBotsApi {
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        telegramBotsApi.registerBot(myTelegramBot)
        return telegramBotsApi
    }
}