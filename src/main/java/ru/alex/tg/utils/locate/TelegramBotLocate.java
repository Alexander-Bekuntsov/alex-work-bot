package ru.alex.tg.utils.locate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Slf4j
@Configuration
public class TelegramBotLocate {

    private static final Map<String, String> MESSAGES = new ConcurrentHashMap<>();

    public TelegramBotLocate(
            ObjectMapper objectMapper,
            @Value("classpath:messages.json") Resource messagesFile
    ) {
        try {
            Map<String, String> loaded = objectMapper.readValue(
                    messagesFile.getInputStream(),
                    new TypeReference<>() {
                    }
            );
            MESSAGES.clear();
            MESSAGES.putAll(loaded);
            log.info("Загружены {} локализованные сообщения из {}", loaded.size(), messagesFile.getFilename());
        } catch (IOException e) {
            log.error("Не удалось загрузить messages.json", e);
            throw new IllegalStateException("Ошибка загрузки messages.json", e);
        }
    }

    public static String get(String key) {
        return MESSAGES.getOrDefault(key, "???" + key + "???");
    }
}
