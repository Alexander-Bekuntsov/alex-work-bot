package org.example.modules.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StateService {
    private final Map<Long, Map<String, String>> formData = new HashMap<>();
    private final Map<Long, String> states = new HashMap<>();

    public void setState(long userId, String step) {
        states.put(userId, step);
        formData.putIfAbsent(userId, new HashMap<>());
    }

    public String getState(long userId) {
        return states.getOrDefault(userId, null);
    }

    public void setTemp(long userId, String key, String value) {
        formData.get(userId).put(key, value);
    }

    public String getField(long userId, String needle_field) {
        Map<String, String> data = formData.get(userId);
        return data.get(needle_field);
    }

    public void clear(long userId) {
        formData.remove(userId);
        states.remove(userId);
    }
}