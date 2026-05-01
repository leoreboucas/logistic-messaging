package com.github.leoreboucas.logisticmessaging.message;

import com.github.leoreboucas.logisticmessaging.conversation.Conversation;
import com.github.leoreboucas.logisticmessaging.conversation.ConversationRepository;
import com.github.leoreboucas.logisticmessaging.user.User;
import com.github.leoreboucas.logisticmessaging.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Message saveMessage(String conversationId, String senderDocument, String content) {

        Conversation conversation = conversationRepository.findById(UUID.fromString(conversationId))
                .orElseThrow(() -> new IllegalArgumentException("Conversa não encontrada"));

        User user = Optional.ofNullable(userRepository.findByDocument(senderDocument))
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));


        if(content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Conteúdo da mensagem não pode ser vazio");
        }

        Message message = new Message();
        message.setContent(content);
        message.setSender(user);
        message.setConversation(conversation);

        messageRepository.save(message);

        return message;
    }
}
