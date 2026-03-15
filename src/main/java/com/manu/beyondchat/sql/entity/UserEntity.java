package com.manu.beyondchat.sql.entity;

import com.manu.beyondchat.sql.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_phone", columnList = "phone_number")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phone_number")
        }
)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String username;

    @Column(nullable = false, length = 512)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String firstName;

    @Column(length = 20)
    private String lastName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(nullable = false)
    private String countryCode;

    @Column(nullable = false)
    private LocalDate dateOfJoining;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
}

