package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "reset_password_tokens")
@Table(schema = "fraude_management",name = "reset_password_tokens")
@EntityListeners(AuditLogListener.class)
public class ResetPasswordToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String token;
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    @Column(nullable = false)
    private boolean used;
    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    public ResetPasswordToken(String token, LocalDateTime expiryDate, User user) {
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
        this.used = false;
    }
}
