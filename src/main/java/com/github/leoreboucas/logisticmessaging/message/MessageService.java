package com.github.leoreboucas.logisticmessaging.message;

import com.github.leoreboucas.logisticmessaging.conversation.Conversation;
import com.github.leoreboucas.logisticmessaging.conversation.ConversationRepository;
import com.github.leoreboucas.logisticmessaging.conversation.ConversationStatus;
import com.github.leoreboucas.logisticmessaging.infra.exception.NotFoundException;
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
    public void saveMessage(String conversationId, String senderDocument, String content, Boolean isBot ) {

        Conversation conversation = conversationRepository.findById(UUID.fromString(conversationId))
                .orElseThrow(() -> new NotFoundException("Conversa não encontrada"));


        if (conversation.getStatus() == ConversationStatus.ABERTO && !isBot) {
            conversation.setStatus(ConversationStatus.TRIAGEM);
            conversationRepository.save(conversation);
        }

        if(content == null || content.trim().isEmpty()) {
            throw new NotFoundException("Conteúdo da mensagem não pode ser vazio");
        }

        Message message = new Message();
        message.setContent(content);
        if (!isBot) {
            User user = Optional.ofNullable(userRepository.findByDocument(senderDocument))
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
            message.setSender(user);
        } else {
            message.setBot(true);
        }
        message.setSessionId(conversation.getCurrentSessionId());
        message.setConversation(conversation);

        messageRepository.save(message);

    }
}
