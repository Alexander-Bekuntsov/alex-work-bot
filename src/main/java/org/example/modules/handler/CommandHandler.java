package org.example.modules.handler;

import lombok.RequiredArgsConstructor;
import org.example.modules.services.FormService;
import org.example.modules.services.MainService;
import org.example.modules.services.ReportService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class CommandHandler {

    private final MainService mainService;
    private final FormService formService;
    private final ReportService reportService;

    public void handle(Message message) {
        String text = message.getText();
        long chatId = message.getFrom().getId();

        switch (text) {
            case "/start" -> mainService.start(message);
            case "/form" -> formService.start(chatId, message);
            case "/report" -> reportService.sendReport(chatId);
            default -> formService.handleStep(chatId, message);
        }
    }
}