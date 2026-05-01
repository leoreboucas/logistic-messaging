package com.github.leoreboucas.logisticmessaging.conversation;

import com.github.leoreboucas.logisticmessaging.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "user1")
    private User user1;
    @ManyToOne
    @JoinColumn(name = "user2")
    private User user2;
    @Column(unique = true)
    private String channelId;
    @Column(name = "current_session_id")
    private UUID currentSessionId;
    @Enumerated(value = EnumType.STRING)
    private ConversationStatus status;
    @Column(name = "contact_reason")
    private String contactReason;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void PrePersist() {
        this.createdAt = LocalDateTime.now();
        this.channelId = user1.getId() + "_" + user2.getId();
        this.currentSessionId = UUID.randomUUID();
        this.status = ConversationStatus.ABERTO;
    }
}
