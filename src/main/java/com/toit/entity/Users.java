package com.toit.entity;

import com.toit.enums.AuthProvider;
import com.toit.enums.EntityStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "users")
public class Users {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usersId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = true)
    private String bio;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    private Long providerUsersId;

    @Enumerated(EnumType.STRING)
    private EntityStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

}
