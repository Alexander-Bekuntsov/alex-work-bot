package ru.alex.tg.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alex.tg.modules.repository.domain.UserResponse;

public interface UserResponseRepository extends JpaRepository<UserResponse, Long> {
}
