package com.github.leoreboucas.logisticmessaging.bot;

import com.github.leoreboucas.logisticmessaging.conversation.Conversation;
import com.github.leoreboucas.logisticmessaging.conversation.ConversationRepository;
import com.github.leoreboucas.logisticmessaging.conversation.ConversationService;
import com.github.leoreboucas.logisticmessaging.conversation.ConversationStatus;
import com.github.leoreboucas.logisticmessaging.message.Message;
import com.github.leoreboucas.logisticmessaging.message.MessageRepository;
import com.github.leoreboucas.logisticmessaging.message.MessageService;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BotService {
    private final ConversationService conversationService;
    private final ConversationRepository conversationRepository;
    private  final MessageService messageService;
    private final MessageRepository messageRepository;
    private Client client;

    @Value("${gemini.api_key}")
    private String apiKey;

    public String generateResponse(String systemPrompt, List<Content> history) {
        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.1-flash-lite-preview",
                        history,
                        GenerateContentConfig.builder()
                                .systemInstruction(Content.fromParts(Part.fromText(systemPrompt)))
                                .responseMimeType("application/json")
                                .build());

        return response.text();
    }

    public String processMessage(Conversation conversation, String senderDocument) {
        String systemPrompt = conversationService.buildSystemPrompt(conversation, senderDocument);

        List<Message> messages = messageRepository.findByConversationAndSessionId(conversation, conversation.getCurrentSessionId());
        List<Content> history = messages.stream()
                .map(message -> Content.fromParts(Part.fromText(message.getContent()))
                        .toBuilder().role(message.isBot() ? "model" : "user").build())
                .toList();
        String botResponse = generateResponse(systemPrompt, history);

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode json = mapper.readTree(botResponse);
            if (json.has("encaminhado") && json.get("encaminhado").asBoolean()) {
                conversation.setStatus(ConversationStatus.AGUARDANDO_ATENDIMENTO);
                conversation.setContactReason(json.get("resumo").asString());
            }
        } catch (Exception e) {
            return botResponse;
        }
        conversationRepository.save(conversation);
        return botResponse;
    }

    @PostConstruct
    void init () {
        this.client = new Client.Builder().apiKey(apiKey).build();
    }


}
