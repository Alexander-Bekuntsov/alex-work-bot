package org.example.modules.services;

import lombok.RequiredArgsConstructor;
import org.example.modules.dao.UserResponseService;
import org.example.modules.domain.UserResponse;
import org.example.utils.locate.TelegramBotLocate;
import org.example.utils.message.TelegramBotMessage;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FormService {

    private final StateService state;
    private final UserResponseService dao;
    private final TelegramClient client;

    private enum Step {
        STEP_NAME, STEP_EMAIL, STEP_SCORE
    }

    public void start(long userId, Message message) {
        state.setState(userId, Step.STEP_NAME.name());
        send(message, TelegramBotLocate.get("form_step_name"));
    }

    public void handleStep(long userId, Message message) {
        String text = message.getText();
        String current = state.getState(userId);

        if (current == null) {
            send(message, TelegramBotLocate.get("und_command"));
            return;
        }

        switch (Step.valueOf(current)) {
            case STEP_NAME -> handleNameStep(userId, message, text);
            case STEP_EMAIL -> handleEmailStep(userId, message, text);
            case STEP_SCORE -> handleScoreStep(userId, message, text);
        }
    }

    private void handleNameStep(long userId, Message message, String text) {
        if (text.length() > 20) {
            send(message, TelegramBotLocate.get("form_format_name_len_error"));
            return;
        }

        if (!text.matches("^[A-Za-zА-Яа-яЁё\\s'-]{2,50}$")) {
            send(message, TelegramBotLocate.get("form_format_name_error"));
            return;
        }

        state.setTemp(userId, "user_name", text);
        state.setState(userId, Step.STEP_EMAIL.name());
        send(message, TelegramBotLocate.get("form_step_email"));
    }

    private void handleEmailStep(long userId, Message message, String text) {
        if (text.length() > 50) {
            send(message, TelegramBotLocate.get("form_format_email_len_error"));
            return;
        }

        if (!text.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            send(message, TelegramBotLocate.get("form_format_email_error"));
            return;
        }

        state.setTemp(userId, "user_email", text);
        state.setState(userId, Step.STEP_SCORE.name());
        send(message, TelegramBotLocate.get("form_step_est"));
    }

    private void handleScoreStep(long userId, Message message, String text) {
        try {
            int score = Integer.parseInt(text);
            if (score < 1 || score > 10) throw new NumberFormatException();

            UserResponse response = new UserResponse(
                    userId,
                    state.getField(userId, "user_name"),
                    state.getField(userId, "user_email"),
                    score,
                    LocalDateTime.now()
            );

            dao.save(response);
            send(message, TelegramBotLocate.get("form_success"));
            state.clear(userId);

        } catch (NumberFormatException e) {
            send(message, TelegramBotLocate.get("form_format_est_error"));
        }
    }

    private void send(Message message, String text) {
        TelegramBotMessage.send(client, message.getChatId(), text);
    }
}
