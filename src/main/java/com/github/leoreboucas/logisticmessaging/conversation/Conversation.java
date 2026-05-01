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
    @JoinColumn(name = "user1_id")
    private User user1Id;
    @ManyToOne
    @JoinColumn(name = "user2_id")
    private User user2Id;
    @Column(unique = true)
    private String channelId;
    @Enumerated(value = EnumType.STRING)
    private ConversationStatus status;
    @Column(name = "contact_reason", nullable = false)
    private String contactReason;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void PrePersist() {
        this.createdAt = LocalDateTime.now();
        this.channelId = user1Id.getId() + "_" + user2Id.getId();
    }
}
