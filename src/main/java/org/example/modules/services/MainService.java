package org.example.modules.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.utils.locate.TelegramBotLocate;
import org.example.utils.message.TelegramBotMessage;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainService {

    private final TelegramClient client;

    public void start(Message message) {
        TelegramBotMessage.send(client, message.getChatId(), TelegramBotLocate.get("welcome"));
    }
}