package com.github.leoreboucas.logisticmessaging.websocket;

import com.github.leoreboucas.logisticmessaging.bot.BotService;
import com.github.leoreboucas.logisticmessaging.conversation.Conversation;
import com.github.leoreboucas.logisticmessaging.conversation.ConversationRepository;
import com.github.leoreboucas.logisticmessaging.conversation.ConversationStatus;
import com.github.leoreboucas.logisticmessaging.infra.exception.BusinessException;
import com.github.leoreboucas.logisticmessaging.message.MessageService;
import com.github.leoreboucas.logisticmessaging.redis.RedisMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final RedisMessagePublisher redisMessagePublisher;
    private final MessageService messageService;
    private final ConversationRepository conversationRepository;
    private final BotService botService;

    @MessageMapping("/conversation/{conversationId}")
    public void receiveMessage (@DestinationVariable String conversationId,  String message, Principal principal) {
        messageService.saveMessage(conversationId, principal.getName(), message, false);

        Conversation conversation =conversationRepository.findById(UUID.fromString(conversationId))
                .orElseThrow(() -> new BusinessException("Conversa não encontrada."));

        redisMessagePublisher.publish(conversationId, message);
        if (conversation.getStatus() == ConversationStatus.TRIAGEM) {
            String botResponse = botService.processMessage(conversation, principal.getName());
            messageService.saveMessage(conversationId, null, botResponse, true);
            redisMessagePublisher.publish(conversationId, botResponse);
        }

    }
}
