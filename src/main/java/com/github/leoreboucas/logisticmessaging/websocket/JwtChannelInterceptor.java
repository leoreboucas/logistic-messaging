package com.github.leoreboucas.logisticmessaging.websocket;

import com.github.leoreboucas.logisticmessaging.infra.exception.BusinessException;
import com.github.leoreboucas.logisticmessaging.infra.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;

    @Override
    public Message<?> preSend (@NonNull Message<?> message, @NonNull MessageChannel channel) {
        try {
            StompHeaderAccessor accessor = (StompHeaderAccessor) MessageHeaderAccessor.getMutableAccessor(message);

            if(accessor.getCommand() == StompCommand.CONNECT) {
                String bearerToken = accessor.getFirstNativeHeader("Authorization");

                if(bearerToken == null) {
                    throw new BusinessException("Token de autenticação ausente.");
                }

                String token = bearerToken.split(" ")[1];

                if (jwtService.isTokenValid(token)) {
                    String subject = jwtService.extractSubject(token);
                    accessor.setUser(() -> subject); // seta o Principal
                    return message;
                } else {
                    throw new BusinessException("Token inválido ou expirado.");
                }
            }
            return message;
        } catch (Exception e) {
            throw new BusinessException("Token inválido ou expirado.");
        }
    }
}
