package ru.alex.tg.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alex.tg.modules.repository.domain.UserState;

public interface UserStateRepository extends JpaRepository<UserState, Long> {
} 
