package org.example.utils.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
public class TelegramBotMessage {

    public static void send(TelegramClient client, Long chatId, String text) {
        if (chatId == null || text == null || text.isBlank()) {
            log.warn("Попытка отправить пустое сообщение или chatId: chatId={}, text='{}'", chatId, text);
            return;
        }

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .build();

        try {
            client.execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в чат [{}]: {}", chatId, e.getMessage(), e);
        }
    }
}
