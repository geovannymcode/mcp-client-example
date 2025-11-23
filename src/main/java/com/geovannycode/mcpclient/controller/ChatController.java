package com.geovannycode.mcpclient.controller;

import com.geovannycode.mcpclient.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public final class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;
    private final ChatService chatService;


    public ChatController(
            ChatClient.Builder builder,
            ToolCallbackProvider toolCallbackProvider,
            ChatService chatService) {

        this.chatService = chatService;

        logAvailableTools(toolCallbackProvider);

        this.chatClient = builder
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .build();
    }


    @GetMapping
    public ResponseEntity<String> chatGet(@RequestParam String message) {
        logger.info("GET request recibido: {}", message);

        return chatService.processMessage(message)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.internalServerError()
                        .body("Error al procesar el mensaje"));
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> chatPost(@RequestBody Map<String, String> request) {
        var message = request.get("message");
        logger.info("POST request recibido: {}", message);

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("El mensaje no puede estar vacío"));
        }

        return chatService.processMessage(message)
                .map(response -> ResponseEntity.ok(createSuccessResponse(response)))
                .orElseGet(() -> ResponseEntity.internalServerError()
                        .body(createErrorResponse("Error al procesar el mensaje")));
    }


    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        var status = new HashMap<String, String>();
        status.put("status", "UP");
        status.put("service", "MCP Chat Client");
        status.put("version", "1.0.0");
        return ResponseEntity.ok(status);
    }


    private void logAvailableTools(ToolCallbackProvider toolCallbackProvider) {
        logger.info("=== Herramientas MCP Disponibles ===");
        Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .forEach(tool -> {
                    var definition = tool.getToolDefinition();
                    logger.info("Tool: {} - Description: {}",
                            definition.name(),
                            definition.description());
                });
        logger.info("====================================");
    }


    private Map<String, Object> createSuccessResponse(String response) {
        var result = new HashMap<String, Object>();
        result.put("response", response);
        result.put("status", "success");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }


    private Map<String, Object> createErrorResponse(String message) {
        var error = new HashMap<String, Object>();
        error.put("status", "error");
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}
