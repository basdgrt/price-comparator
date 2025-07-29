package com.github.basdgrt.notifications

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import io.github.oshai.kotlinlogging.KotlinLogging

private const val BOT_USER_NAME = "baby_park_bot"

/**
 * A notifier that sends messages to a Telegram chat using a Telegram bot.
 * 
 * @param botToken The token of the Telegram bot
 * @param chatId The ID of the chat to send messages to
 */
class TelegramNotifier(
    private val botToken: String,
    private val chatId: String
) : TelegramLongPollingBot(botToken) {

    private val logger = KotlinLogging.logger {}

    /**
     * Sends a message to the configured Telegram chat.
     * 
     * @param message The message to send
     * @return true if the message was sent successfully, false otherwise
     */
    fun sendMessage(message: String): Boolean {
        val sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(message)
            .build()

        return try {
            execute(sendMessage)
            true
        } catch (e: TelegramApiException) {
            logger.error(e) { "Failed to send message to Telegram chat $chatId" }
            false
        }
    }

    override fun onUpdateReceived(update: Update) {
        // This bot doesn't process incoming updates
    }

    override fun getBotUsername(): String {
        return BOT_USER_NAME
    }
}