package com.github.leoreboucas.logisticmessaging.websocket;

import com.github.leoreboucas.logisticmessaging.message.MessageService;
import com.github.leoreboucas.logisticmessaging.redis.RedisMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final RedisMessagePublisher redisMessagePublisher;
    private final MessageService messageService;

    @MessageMapping("/conversation/{conversationId}")
    public void receiveMessage (@DestinationVariable String conversationId,  String message, Principal principal) {
        messageService.saveMessage(conversationId, principal.getName(), message);
        redisMessagePublisher.publish(conversationId, message);
    }
}
