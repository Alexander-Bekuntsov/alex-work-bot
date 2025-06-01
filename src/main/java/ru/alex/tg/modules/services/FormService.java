package ru.alex.tg.modules.services;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.alex.tg.modules.dao.UserResponseService;
import ru.alex.tg.modules.repository.domain.UserResponse;
import ru.alex.tg.modules.statemachine.FormEvents;
import ru.alex.tg.modules.statemachine.FormStates;
import ru.alex.tg.utils.locate.TelegramBotLocate;
import ru.alex.tg.utils.message.TelegramBotMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormService {

    private final StateMachineService stateMachineService;
    private final UserResponseService userResponseService;
    private final TelegramClient telegramClient;

    @Transactional
    public void start(long userId, Message message) {
        stateMachineService.clearUserData(userId);
        stateMachineService.sendEvent(userId, FormEvents.START_FORM);
        send(message, TelegramBotLocate.get("form_step_name"));
    }

    @Transactional
    public void handleStep(long userId, Message message) {
        String text = message.getText();
        FormStates currentState = stateMachineService.getCurrentState(userId);

        if (currentState == null || currentState == FormStates.IDLE) {
            send(message, TelegramBotLocate.get("und_command"));
            return;
        }

        switch (currentState) {
            case WAITING_NAME -> handleNameStep(userId, message, text);
            case WAITING_EMAIL -> handleEmailStep(userId, message, text);
            case WAITING_SCORE -> handleScoreStep(userId, message, text);
            case COMPLETED -> handleCompleted(userId, message);
            default -> send(message, TelegramBotLocate.get("und_command"));
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

        stateMachineService.saveFormData(userId, "user_name", text);
        stateMachineService.sendEvent(userId, FormEvents.NAME_PROVIDED);
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

        stateMachineService.saveFormData(userId, "user_email", text);
        stateMachineService.sendEvent(userId, FormEvents.EMAIL_PROVIDED);
        send(message, TelegramBotLocate.get("form_step_est"));
    }

    private void handleScoreStep(long userId, Message message, String text) {
        try {
            int score = Integer.parseInt(text);
            if (score < 1 || score > 10) throw new NumberFormatException();

            // Сохраняем данные в основную БД
            UserResponse response = new UserResponse(
                    userId,
                    stateMachineService.getFormData(userId, "user_name"),
                    stateMachineService.getFormData(userId, "user_email"),
                    score,
                    LocalDateTime.now()
            );

            userResponseService.save(response);
            stateMachineService.sendEvent(userId, FormEvents.SCORE_PROVIDED);
            send(message, TelegramBotLocate.get("form_success"));

            stateMachineService.clearUserData(userId);
            stateMachineService.sendEvent(userId, FormEvents.RESET);

        } catch (NumberFormatException e) {
            send(message, TelegramBotLocate.get("form_format_est_error"));
        }
    }

    private void handleCompleted(long userId, Message message) {
        send(message, TelegramBotLocate.get("form_success"));
        stateMachineService.clearUserData(userId);
        stateMachineService.sendEvent(userId, FormEvents.RESET);
    }

    private void send(Message message, String text) {
        TelegramBotMessage.send(telegramClient, message.getChatId(), text);
    }
}
