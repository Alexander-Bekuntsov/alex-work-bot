package ru.alex.tg.utils.message;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TelegramBotMessage {

    public static void send(TelegramClient client, Long chatId, String text) {
        if (Objects.isNull(chatId) || StringUtils.isBlank(text)) {
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
