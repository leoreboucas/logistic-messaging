package com.github.leoreboucas.logisticmessaging.redis;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void onMessage(@NonNull Message message, byte @Nullable [] pattern) {
        byte[] channelBytes = message.getChannel();
        String channelName = new String(channelBytes, StandardCharsets.UTF_8);
        
        simpMessagingTemplate.convertAndSend("/topic/conversations/" + channelName, new String(message.getBody()));
    }
}
