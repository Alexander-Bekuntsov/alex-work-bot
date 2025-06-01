package ru.alex.tg.modules.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.alex.tg.modules.services.FormService;
import ru.alex.tg.modules.services.MainService;
import ru.alex.tg.modules.services.ReportService;

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
