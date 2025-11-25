package com.geovannycode.mcpclient.controller;

import com.geovannycode.mcpclient.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/chat")
public final class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private static final String ERROR_MESSAGE = "Error al procesar el mensaje";
    private static final String EMPTY_MESSAGE_ERROR = "El mensaje no puede estar vacío";

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<String> chatGet(@RequestParam String message) {
        logger.info("GET request recibido: {}", message);

        return chatService.processMessage(message)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .internalServerError()
                        .body(ERROR_MESSAGE));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> chatPost(@RequestBody Map<String, String> request) {
        var message = request.get("message");
        logger.info("POST request recibido: {}", message);

        if (message == null || message.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(chatService.createErrorResponse(EMPTY_MESSAGE_ERROR));
        }

        return chatService.processMessage(message)
                .map(chatService::createSuccessResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .internalServerError()
                        .body(chatService.createErrorResponse(ERROR_MESSAGE)));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        var status = Map.of(
                "status", "UP",
                "service", "MCP Chat Client",
                "version", "1.0.0"
        );
        return ResponseEntity.ok(status);
    }
}