package com.github.leoreboucas.logisticmessaging.conversation;

import com.github.leoreboucas.logisticmessaging.conversation.DTO.UserRecipientDTO;
import com.github.leoreboucas.logisticmessaging.infra.bot.BotPrompts;
import com.github.leoreboucas.logisticmessaging.infra.client.DTO.DeliveryManOrdersDTO;
import com.github.leoreboucas.logisticmessaging.infra.client.DTO.OrdersDTO;
import com.github.leoreboucas.logisticmessaging.infra.client.LogisticClient;
import com.github.leoreboucas.logisticmessaging.user.User;
import com.github.leoreboucas.logisticmessaging.user.UserRepository;
import com.github.leoreboucas.logisticmessaging.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final LogisticClient logisticClient;

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

    public String buildSystemPrompt(Conversation conversation, String userDocument) {
        User user = Optional.ofNullable(userRepository.findByDocument(userDocument))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        String basePrompt = BotPrompts.BASE_PROMPT;
        basePrompt = basePrompt.replace("{ROLE}", user.getRole().toString());
        basePrompt = basePrompt.replace("{DOCUMENT}", user.getDocument());
        switch (user.getRole()) {
            case CUSTOMER -> {
                List<OrdersDTO> orders = logisticClient.getOrdersForCostumer(userDocument);
                String ordersFormatted = orders.stream()
                        .map(o -> "- Pedido %s | Status: %s | %s/%s".formatted(o.trackingCode(), o.status(), o.city(), o.state()))
                        .collect(Collectors.joining("\n"));
                String customerPrompt = BotPrompts.CUSTOMER_PROMPT;
                customerPrompt = customerPrompt.replace("{LISTA_PEDIDOS}", ordersFormatted);
                basePrompt = basePrompt + customerPrompt;
            }
            case DELIVERY_MAN -> {
                DeliveryManOrdersDTO deliveryManOrders = logisticClient.getOrdersForDeliveryMan(userDocument);
                String parciaisFormatted = deliveryManOrders.partialDelivery().stream()
                        .map(p -> "- Pedido %s | De %s para %s | Saída: %s".formatted(p.trackingCode(), p.originCenter(), p.destinationCenter(), p.departureDate()))
                        .collect(Collectors.joining("\n"));

                String finaisFormatted = deliveryManOrders.finalDelivery().stream()
                        .map(f -> "- Pedido %s | Destino: %s - %s/%s | Saída: %s".formatted(f.trackingCode(), f.customerCompleteName(), f.city(), f.state(), f.departureDate()))
                        .collect(Collectors.joining("\n"));

                String deliveryManPrompt = BotPrompts.DELIVERY_MAN_PROMPT;
                
                deliveryManPrompt = deliveryManPrompt.replace("{LISTA_PARCIAIS}", parciaisFormatted);
                deliveryManPrompt = deliveryManPrompt.replace("{LISTA_FINAIS}", finaisFormatted);
                basePrompt = basePrompt + deliveryManPrompt;
            }
            default -> {
                basePrompt = basePrompt + BotPrompts.ENTERPRISE_PROMPT;
            }
        }
        return basePrompt;
}}
