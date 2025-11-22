package com.geovannycode.mcpclient.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import org.springframework.ai.model.Content;


@Service
public final class ChatService {

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
            var content = chatClient.prompt(message).content();
            var responseText = extractText(content);
            logger.debug("Respuesta generada: {}", responseText);
            return responseText;
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
            var userMessage = new UserMessage(message, context);
            var prompt = new Prompt(userMessage);
            var content = chatClient.prompt(prompt).content();
            var responseText = extractText(content);
            logger.debug("Respuesta generada con contexto: {}", responseText);
            return responseText;
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
            var content = chatClient.prompt()
                    .system(systemMessage)
                    .user(message)
                    .content();

            var responseText = extractText(content);
            logger.debug("Respuesta generada con system prompt: {}", responseText);
            return responseText;
        } catch (Exception e) {
            logger.error("Error al procesar mensaje con system prompt: {}", message, e);
            return Optional.empty();
        }
    }


    private Optional<String> extractText(Content content) {
        if (content == null) {
            return Optional.empty();
        }

        var text = content.text();
        return Optional.ofNullable(text)
                .filter(t -> !t.isBlank());
    }
}





