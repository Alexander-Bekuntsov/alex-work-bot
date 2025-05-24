package org.example.modules.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.example.modules.dao.UserResponseService;
import org.example.modules.domain.UserResponse;
import org.example.utils.locate.TelegramBotLocate;
import org.example.utils.message.TelegramBotMessage;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserResponseService dao;
    private final TelegramClient client;

    public void sendReport(long chatId) {
        CompletableFuture.runAsync(() -> {
            try {
                List<UserResponse> responses = dao.findAll();

                if (responses.isEmpty()) {
                    TelegramBotMessage.send(client, chatId, TelegramBotLocate.get("db_no_users_error"));
                    return;
                }

                byte[] report = generateReport(responses);
                if (report.length == 0) {
                    TelegramBotMessage.send(client, chatId, TelegramBotLocate.get("file_generate_error"));
                    return;
                }

                TelegramBotMessage.send(client, chatId, TelegramBotLocate.get("report_ready"));

                SendDocument sendDocument = SendDocument.builder()
                        .chatId(chatId)
                        .document(new InputFile(new ByteArrayInputStream(report), "report.docx"))
                        .build();

                client.execute(sendDocument);

            } catch (TelegramApiException e) {
                log.error("Ошибка отправки документа в Telegram: {}", e.getMessage(), e);
                TelegramBotMessage.send(client, chatId, TelegramBotLocate.get("file_generate_error"));
            } catch (Exception e) {
                log.error("Ошибка при генерации отчета: {}", e.getMessage(), e);
                TelegramBotMessage.send(client, chatId, TelegramBotLocate.get("file_generate_error"));
            }
        });
    }

    private byte[] generateReport(List<UserResponse> responses) {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            createTitle(doc);
            createTable(doc, responses);

            doc.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Ошибка генерации Word-документа: {}", e.getMessage(), e);
            return new byte[0];
        }
    }

    private void createTitle(XWPFDocument doc) {
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.LEFT);
        title.setSpacingAfter(200);

        XWPFRun run = title.createRun();
        run.setText(TelegramBotLocate.get("table_title"));
        run.setBold(true);
        run.setFontSize(20);
    }

    private void createTable(XWPFDocument doc, List<UserResponse> responses) {
        XWPFTable table = doc.createTable(responses.size() + 1, 3);

        table.getRow(0).getCell(0).setText(TelegramBotLocate.get("table_title_name"));
        table.getRow(0).getCell(1).setText(TelegramBotLocate.get("table_title_email"));
        table.getRow(0).getCell(2).setText(TelegramBotLocate.get("table_title_est"));

        for (int i = 0; i < responses.size(); i++) {
            UserResponse r = responses.get(i);
            table.getRow(i + 1).getCell(0).setText(r.getName());
            table.getRow(i + 1).getCell(1).setText(r.getEmail());
            table.getRow(i + 1).getCell(2).setText(String.valueOf(r.getScore()));
        }
    }
}
