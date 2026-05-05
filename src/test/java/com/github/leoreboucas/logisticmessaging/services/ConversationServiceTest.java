package com.github.leoreboucas.logisticmessaging.services;

import com.github.leoreboucas.logisticmessaging.conversation.Conversation;
import com.github.leoreboucas.logisticmessaging.conversation.ConversationRepository;
import com.github.leoreboucas.logisticmessaging.conversation.ConversationService;
import com.github.leoreboucas.logisticmessaging.conversation.DTO.UserRecipientDTO;
import com.github.leoreboucas.logisticmessaging.infra.client.LogisticClient;
import com.github.leoreboucas.logisticmessaging.infra.exception.BusinessException;
import com.github.leoreboucas.logisticmessaging.infra.exception.NotFoundException;
import com.github.leoreboucas.logisticmessaging.user.User;
import com.github.leoreboucas.logisticmessaging.user.UserRepository;
import com.github.leoreboucas.logisticmessaging.user.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConversationServiceTest {
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LogisticClient logisticClient;

    private final String user1Document = "00000000000";
    private final String user2Document = "11111111111";


    @InjectMocks
    private ConversationService conversationService;

    @Nested
    class CreateConversation {
        @Test
        void user1NotFound () {
            UserRecipientDTO userRecipient = userRecipientDTO();
            when(userRepository.findByDocument(user1Document)).thenReturn(null);

            assertThrows(NotFoundException.class, () -> conversationService.createConversation(userRecipient, user1Document));

            verify(userRepository).findByDocument(user1Document);
        }

        @Test
        void user2NotFound () {
            UserRecipientDTO userRecipient = userRecipientDTO();
            when(userRepository.findByDocument(user1Document)).thenReturn(new User());
            when(userRepository.findByDocument(userRecipient.user2Document())).thenReturn(null);

            assertThrows(NotFoundException.class, () -> conversationService.createConversation(userRecipient, user1Document));

            verify(userRepository).findByDocument(user1Document);
            verify(userRepository).findByDocument(userRecipient.user2Document());
        }

        @Test
        void notEnterpriseRoleOnUsers () {
            User user1 = new User();
            user1.setRole(UserRole.CUSTOMER);
            User user2 = new User();
            user2.setRole(UserRole.DELIVERY_MAN);
            UserRecipientDTO userRecipient = userRecipientDTO();

            when(userRepository.findByDocument(user1Document)).thenReturn(user1);
            when(userRepository.findByDocument(userRecipient.user2Document())).thenReturn(user2);

            assertThrows(BusinessException.class, () -> conversationService.createConversation(userRecipient, user1Document));

            verify(userRepository).findByDocument(user1Document);
            verify(userRepository).findByDocument(userRecipient.user2Document());

        }

        @Test
        void twoEnterpriseTryToInitConversation () {
            User user1 = new User();
            user1.setRole(UserRole.ENTERPRISE);
            User user2 = new User();
            user2.setRole(UserRole.ENTERPRISE);
            UserRecipientDTO userRecipient = userRecipientDTO();

            when(userRepository.findByDocument(user1Document)).thenReturn(user1);
            when(userRepository.findByDocument(userRecipient.user2Document())).thenReturn(user2);

            assertThrows(BusinessException.class, () -> conversationService.createConversation(userRecipient, user1Document));

            verify(userRepository).findByDocument(user1Document);
            verify(userRepository).findByDocument(userRecipient.user2Document());
        }

        @Test
        void conversationAlreadyExist () {
            User user1 = new User();
            user1.setRole(UserRole.CUSTOMER);
            User user2 = new User();
            user2.setRole(UserRole.ENTERPRISE);
            UserRecipientDTO userRecipient = userRecipientDTO();

            when(userRepository.findByDocument(user1Document)).thenReturn(user1);
            when(userRepository.findByDocument(userRecipient.user2Document())).thenReturn(user2);
            when(conversationRepository.findByUser(user1, user2)).thenReturn(new Conversation());

            Conversation result = conversationService.createConversation(userRecipient, user1Document);

            assertEquals(Conversation.class, result.getClass());

            verify(conversationRepository).findByUser(user1, user2);
            verify(userRepository).findByDocument(user1Document);
            verify(userRepository).findByDocument(userRecipient.user2Document());
        }

        @Test
        void successOnCreateConversation () {
            User user1 = new User();
            user1.setRole(UserRole.CUSTOMER);
            User user2 = new User();
            user2.setRole(UserRole.ENTERPRISE);
            UserRecipientDTO userRecipient = userRecipientDTO();

            when(userRepository.findByDocument(user1Document)).thenReturn(user1);
            when(userRepository.findByDocument(userRecipient.user2Document())).thenReturn(user2);
            when(conversationRepository.findByUser(user1, user2)).thenReturn(null);
            when(conversationRepository.save(any(Conversation.class))).thenReturn(null);

            Conversation result = conversationService.createConversation(userRecipient, user1Document);

            assertEquals(Conversation.class, result.getClass());
            assertEquals(user1, result.getUser1());
            assertEquals(user2, result.getUser2());

            verify(conversationRepository).save(any(Conversation.class));
            verify(conversationRepository).findByUser(user1, user2);
            verify(userRepository).findByDocument(user1Document);
            verify(userRepository).findByDocument(userRecipient.user2Document());
        }
    }

    private UserRecipientDTO userRecipientDTO () {
        return new UserRecipientDTO(user2Document);
    }
}
