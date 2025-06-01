package ru.alex.tg.modules.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.alex.tg.modules.dao.UserResponseService;
import ru.alex.tg.modules.repository.domain.UserResponse;
import ru.alex.tg.utils.locate.TelegramBotLocate;
import ru.alex.tg.utils.message.TelegramBotMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    public static final String FILE_GENERATE_ERROR = "file_generate_error";
    public static final String DB_NO_USERS_ERROR = "db_no_users_error";
    public static final String REPORT_READY = "report_ready";
    public static final String REPORT_DOCX = "report.docx";

    private final UserResponseService userResponseService;
    private final TelegramClient telegramClient;

    public void sendReport(long chatId) {
        CompletableFuture.runAsync(() -> {
            try {
                List<UserResponse> responses = userResponseService.findAll();

                if (responses.isEmpty()) {
                    TelegramBotMessage.send(telegramClient, chatId, TelegramBotLocate.get(DB_NO_USERS_ERROR));
                    return;
                }

                byte[] report = generateReport(responses);
                if (report.length == 0) {
                    TelegramBotMessage.send(telegramClient, chatId, TelegramBotLocate.get(FILE_GENERATE_ERROR));
                    return;
                }

                TelegramBotMessage.send(telegramClient, chatId, TelegramBotLocate.get(REPORT_READY));

                SendDocument sendDocument = SendDocument.builder()
                        .chatId(chatId)
                        .document(new InputFile(new ByteArrayInputStream(report), REPORT_DOCX))
                        .build();

                telegramClient.execute(sendDocument);

            } catch (TelegramApiException e) {
                log.error("Ошибка отправки документа в Telegram: {}", e.getMessage(), e);
                TelegramBotMessage.send(telegramClient, chatId, TelegramBotLocate.get(FILE_GENERATE_ERROR));
            } catch (Exception e) {
                log.error("Ошибка при генерации отчета: {}", e.getMessage(), e);
                TelegramBotMessage.send(telegramClient, chatId, TelegramBotLocate.get(FILE_GENERATE_ERROR));
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
