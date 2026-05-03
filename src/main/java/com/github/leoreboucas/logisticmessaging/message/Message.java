package com.github.leoreboucas.logisticmessaging.message;

import com.github.leoreboucas.logisticmessaging.conversation.Conversation;
import com.github.leoreboucas.logisticmessaging.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "conversation")
    private Conversation conversation;
    @Column(name = "session_id")
    private UUID sessionId;
    @ManyToOne
    @JoinColumn(name = "sender")
    private User sender;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Column(name = "is_bot")
    private boolean isBot;
    @Column(name = "read_at")
    private LocalDateTime readAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void PrePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
