package org.example.modules.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_responses")
public class UserResponse {

    @Id
    @Column(name = "user_id")
    private Long chatId;

    @Column(name = "user_name")
    private String name;

    @Column(name = "user_email")
    private String email;

    @Column(name = "user_score")
    private int score;

    @Column(name = "created_time")
    private LocalDateTime createdTime;
}