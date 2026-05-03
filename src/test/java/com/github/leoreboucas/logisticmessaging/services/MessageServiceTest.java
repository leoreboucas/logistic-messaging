package com.github.leoreboucas.logisticmessaging.services;

import com.github.leoreboucas.logisticmessaging.conversation.Conversation;
import com.github.leoreboucas.logisticmessaging.conversation.ConversationRepository;
import com.github.leoreboucas.logisticmessaging.conversation.ConversationStatus;
import com.github.leoreboucas.logisticmessaging.infra.exception.NotFoundException;
import com.github.leoreboucas.logisticmessaging.message.Message;
import com.github.leoreboucas.logisticmessaging.message.MessageRepository;
import com.github.leoreboucas.logisticmessaging.message.MessageService;
import com.github.leoreboucas.logisticmessaging.user.User;
import com.github.leoreboucas.logisticmessaging.user.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

    private final String conversationId = UUID.randomUUID().toString();
    private final String senderDocument = "00000000000";
    private final String content = "content";
    private Boolean isBot = false;

    @Nested
    class saveMessage {
        @Test
        void conversationNotFound () {
            when(conversationRepository.findById(UUID.fromString(conversationId))).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> messageService.saveMessage(conversationId, senderDocument, content, isBot));

            verify(conversationRepository).findById(UUID.fromString(conversationId));
        }

        @Test
        void contentEmpty () {
            when(conversationRepository.findById(UUID.fromString(conversationId))).thenReturn(Optional.of(new Conversation()));

            assertThrows(NotFoundException.class, () -> messageService.saveMessage(conversationId, senderDocument, "", isBot));

            verify(conversationRepository).findById(UUID.fromString(conversationId));
        }

        @Test
        void userNotFound () {
            when(conversationRepository.findById(UUID.fromString(conversationId))).thenReturn(Optional.of(new Conversation()));

            when(userRepository.findByDocument(senderDocument)).thenReturn(null);

            assertThrows(NotFoundException.class, () -> messageService.saveMessage(conversationId, senderDocument, content, isBot));

            verify(conversationRepository).findById(UUID.fromString(conversationId));
            verify(userRepository).findByDocument(senderDocument);
        }

        @Test
        void openToScreeningConversation () {
            Conversation conversation = new Conversation();
            when(conversationRepository.findById(UUID.fromString(conversationId))).thenReturn(Optional.of(conversation));
            conversation.setStatus(ConversationStatus.ABERTO);

            when(conversationRepository.save(conversation)).thenReturn(null);

            User user = new User();
            when(userRepository.findByDocument(senderDocument)).thenReturn(user);

            assertDoesNotThrow(() -> messageService.saveMessage(conversationId, senderDocument, content, isBot));
            assertEquals(ConversationStatus.TRIAGEM, conversation.getStatus());

            verify(conversationRepository).save(any(Conversation.class));
            verify(userRepository).findByDocument(senderDocument);
            verify(conversationRepository).findById(UUID.fromString(conversationId));
        }

        @Test
        void successOnSaveMessageByBot () {
            when(conversationRepository.findById(UUID.fromString(conversationId))).thenReturn(Optional.of(new Conversation()));

            when(messageRepository.save(any(Message.class))).thenReturn(null);


            assertDoesNotThrow(() -> messageService.saveMessage(conversationId, null, content, true));
            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            verify(messageRepository).save(captor.capture());
            Message savedMessage = captor.getValue();
            assertTrue(savedMessage.isBot());
            assertNull(savedMessage.getSender());

            verify(conversationRepository).findById(UUID.fromString(conversationId));
            verify(messageRepository).save(any(Message.class));
        }

        @Test
        void successOnSaveMessageByUser () {
            when(conversationRepository.findById(UUID.fromString(conversationId))).thenReturn(Optional.of(new Conversation()));

            User user = new User();
            when(userRepository.findByDocument(senderDocument)).thenReturn(user);

            when(messageRepository.save(any(Message.class))).thenReturn(null);

            assertDoesNotThrow(() -> messageService.saveMessage(conversationId, senderDocument, content, isBot));

            verify(conversationRepository).findById(UUID.fromString(conversationId));
            verify(userRepository).findByDocument(senderDocument);
            verify(messageRepository).save(any(Message.class));
        }
    }
}
