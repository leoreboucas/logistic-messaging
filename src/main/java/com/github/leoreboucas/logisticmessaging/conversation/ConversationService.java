package com.github.leoreboucas.logisticmessaging.conversation;

import com.github.leoreboucas.logisticmessaging.conversation.DTO.UserRecipientDTO;
import com.github.leoreboucas.logisticmessaging.user.User;
import com.github.leoreboucas.logisticmessaging.user.UserRepository;
import com.github.leoreboucas.logisticmessaging.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public Conversation createConversation (UserRecipientDTO userRecipientDTO, String user1Document) {
        User user1 = Optional.ofNullable(userRepository.findByDocument(user1Document))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        User user2 = Optional.ofNullable(userRepository.findByDocument(userRecipientDTO.user2Document()))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if(user1.getRole() != UserRole.ENTERPRISE && user2.getRole() != UserRole.ENTERPRISE) {
            throw new RuntimeException("Pelo menos um dos usuários deve ser uma empresa.");
        }

        if(user1.getRole() == UserRole.ENTERPRISE && user2.getRole() == UserRole.ENTERPRISE) {
            throw new RuntimeException("Não é permitido criar uma conversa entre duas empresas.");
        }

        Optional<Conversation> conversation = Optional.ofNullable(conversationRepository.findByUser(user1, user2));

        if(conversation.isPresent()) {
            return conversation.get();
        }

        Conversation newConversation = new Conversation();
        newConversation.setUser1(user1);
        newConversation.setUser2(user2);
        conversationRepository.save(newConversation);

        return newConversation;
    }
}
