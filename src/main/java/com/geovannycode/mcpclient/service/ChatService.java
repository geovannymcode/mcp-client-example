package com.geovannycode.mcpclient.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }


    public Optional<String> processMessage(String message) {
        if (message == null || message.isBlank()) {
            logger.warn("Intento de procesar mensaje vacío o nulo");
            return Optional.empty();
        }

        logger.debug("Procesando mensaje: {}", message);

        try {
            var responseText = chatClient
                    .prompt(message)
                    .call()
                    .content();

            logger.debug("Respuesta generada: {}", responseText);
            return Optional.ofNullable(responseText)
                    .filter(text -> !text.isBlank());
        } catch (Exception e) {
            logger.error("Error al procesar mensaje: {}", message, e);
            return Optional.empty();
        }
    }


    public Optional<String> processMessageWithContext(String message, Map<String, Object> context) {
        if (message == null || message.isBlank()) {
            logger.warn("Intento de procesar mensaje vacío o nulo");
            return Optional.empty();
        }

        logger.debug("Procesando mensaje con contexto: {} - {}", message, context);

        try {
            // Construir el mensaje enriquecido con contexto
            var enrichedMessage = buildEnrichedMessage(message, context);
            var userMessage = new UserMessage(enrichedMessage);
            var prompt = new Prompt(List.of(userMessage));

            var responseText = chatClient
                    .prompt(prompt)
                    .call()
                    .content();

            logger.debug("Respuesta generada con contexto: {}", responseText);
            return Optional.ofNullable(responseText)
                    .filter(text -> !text.isBlank());
        } catch (Exception e) {
            logger.error("Error al procesar mensaje con contexto: {}", message, e);
            return Optional.empty();
        }
    }

    public Optional<String> processMessageWithSystemPrompt(String message, String systemMessage) {
        if (message == null || message.isBlank()) {
            logger.warn("Intento de procesar mensaje vacío o nulo");
            return Optional.empty();
        }

        logger.debug("Procesando mensaje con system prompt: {}", message);

        try {
            var responseText = chatClient.prompt()
                    .system(systemMessage)
                    .user(message)
                    .call()
                    .content();

            logger.debug("Respuesta generada con system prompt: {}", responseText);
            return Optional.ofNullable(responseText)
                    .filter(text -> !text.isBlank());
        } catch (Exception e) {
            logger.error("Error al procesar mensaje con system prompt: {}", message, e);
            return Optional.empty();
        }
    }


    private String buildEnrichedMessage(String message, Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return message;
        }

        var contextInfo = new StringBuilder();
        contextInfo.append("Contexto: ");

        context.forEach((key, value) ->
                contextInfo.append(key).append("=").append(value).append(", ")
        );

        // Remover la última coma y espacio
        if (contextInfo.length() > 10) {
            contextInfo.setLength(contextInfo.length() - 2);
        }

        return contextInfo + "\n\nConsulta: " + message;
    }


    public Map<String, Object> createSuccessResponse(String response) {
        return Map.of(
                "response", response,
                "status", "success",
                "timestamp", System.currentTimeMillis()
        );
    }


    public Map<String, Object> createErrorResponse(String errorMessage) {
        return Map.of(
                "status", "error",
                "message", errorMessage,
                "timestamp", System.currentTimeMillis()
        );
    }
}
