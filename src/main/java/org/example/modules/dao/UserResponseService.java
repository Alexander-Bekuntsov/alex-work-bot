package org.example.modules.dao;

import org.example.modules.domain.UserResponse;
import org.example.modules.repository.UserResponseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserResponseService {

    private final UserResponseRepository repository;

    public UserResponseService(UserResponseRepository repository) {
        this.repository = repository;
    }

    public void save(UserResponse response) {
        repository.save(response);
    }

    public List<UserResponse> findAll() {
        return repository.findAll();
    }
}