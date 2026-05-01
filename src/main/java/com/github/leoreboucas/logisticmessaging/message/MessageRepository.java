package com.github.leoreboucas.logisticmessaging.message;

import com.github.leoreboucas.logisticmessaging.conversation.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationAndSessionId(Conversation conversation, UUID sessionId);
}
