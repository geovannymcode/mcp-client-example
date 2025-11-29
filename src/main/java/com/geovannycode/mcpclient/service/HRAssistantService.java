package com.geovannycode.mcpclient.service;

import com.geovannycode.mcpclient.model.QueryRequest;
import com.geovannycode.mcpclient.model.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HRAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(HRAssistantService.class);

    private static final String SYSTEM_MESSAGE = """
        Eres un asistente de Recursos Humanos profesional y amable.
        Tu objetivo es ayudar a los empleados con:
        - Consultas sobre políticas de la empresa
        - Información sobre beneficios y compensaciones
        - Solicitudes de vacaciones y permisos
        - Procedimientos administrativos
        - Resolución de problemas de RRHH
        
        Siempre mantén un tono profesional pero cercano.
        Si no tienes información suficiente, solicita más detalles.
        Si la consulta requiere aprobación de un superior, indícalo claramente.
        """;

    private static final List<String> DEFAULT_TOOLS = List.of(
            "employeeData",
            "companyPolicies",
            "benefitsSystem"
    );

    private final ChatService chatService;

    public HRAssistantService(ChatService chatService) {
        this.chatService = chatService;
        logger.info("HR Assistant Service initialized");
    }

    public Optional<QueryResponse> processQuery(QueryRequest request) {
        var context = buildContext(request);

        return chatService.processMessageWithContext(request.query(), context)
                .map(response -> buildSuccessResponse(response, request.employeeId()));
    }

    public Optional<Map<String, Object>> getPolicyInformation(String policyName) {
        var query = String.format(
                "¿Puedes explicarme la política de la empresa sobre: %s?",
                policyName
        );

        return chatService.processMessageWithSystemPrompt(query, SYSTEM_MESSAGE)
                .map(information -> buildPolicyResponse(policyName, information));
    }

    public Optional<Map<String, Object>> getEmployeeInformation(String employeeId) {
        var query = String.format(
                "¿Puedes proporcionarme un resumen de la información del empleado con ID: %s?",
                employeeId
        );

        var context = Map.of("employeeId", (Object) employeeId);

        return chatService.processMessageWithContext(query, context)
                .map(summary -> buildEmployeeResponse(employeeId, summary));
    }

    public Optional<Map<String, Object>> getBenefitsInformation(String employeeId) {
        var query = employeeId != null
                ? String.format("¿Qué beneficios están disponibles para el empleado %s?", employeeId)
                : "¿Cuáles son los beneficios disponibles en la empresa?";

        var context = employeeId != null
                ? Map.of("employeeId", (Object) employeeId)
                : Map.<String, Object>of();

        return chatService.processMessageWithContext(query, context)
                .map(this::buildBenefitsResponse);
    }

    private Map<String, Object> buildContext(QueryRequest request) {
        var context = new HashMap<String, Object>();

        if (request.employeeId() != null) {
            context.put("employeeId", request.employeeId());
        }

        if (request.context() != null) {
            context.put("additionalContext", request.context());
        }

        return context;
    }

    private QueryResponse buildSuccessResponse(String response, String employeeId) {
        return QueryResponse.builder()
                .response(response)
                .timestamp(LocalDateTime.now())
                .toolsUsed(DEFAULT_TOOLS)
                .metadata(Map.of("employeeId", employeeId))
                .build();
    }

    private Map<String, Object> buildPolicyResponse(String policyName, String information) {
        var response = new HashMap<String, Object>();
        response.put("policyName", policyName);
        response.put("information", information);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    private Map<String, Object> buildEmployeeResponse(String employeeId, String summary) {
        var response = new HashMap<String, Object>();
        response.put("employeeId", employeeId);
        response.put("summary", summary);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    private Map<String, Object> buildBenefitsResponse(String benefits) {
        var response = new HashMap<String, Object>();
        response.put("benefits", benefits);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
