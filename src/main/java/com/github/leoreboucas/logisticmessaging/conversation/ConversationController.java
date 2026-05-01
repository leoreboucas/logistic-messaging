package com.github.leoreboucas.logisticmessaging.conversation;

import com.github.leoreboucas.logisticmessaging.conversation.DTO.ConversationResponseDTO;
import com.github.leoreboucas.logisticmessaging.conversation.DTO.UserRecipientDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;

    @PostMapping
    public ConversationResponseDTO createConversation (@RequestBody UserRecipientDTO userRecipientDTO, Principal principal) {
        Conversation conversation = conversationService.createConversation(userRecipientDTO, principal.getName());
        return new ConversationResponseDTO(conversation.getChannelId());
    }
}
