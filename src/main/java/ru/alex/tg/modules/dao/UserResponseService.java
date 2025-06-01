package ru.alex.tg.modules.dao;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.alex.tg.modules.repository.UserResponseRepository;
import ru.alex.tg.modules.repository.domain.UserResponse;

@Service
@RequiredArgsConstructor
public class UserResponseService {

    private final UserResponseRepository repository;

    public void save(UserResponse response) {
        repository.save(response);
    }

    public List<UserResponse> findAll() {
        return repository.findAll();
    }
}
