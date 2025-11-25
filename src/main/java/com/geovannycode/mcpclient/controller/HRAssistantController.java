package com.geovannycode.mcpclient.controller;

import com.geovannycode.mcpclient.model.QueryRequest;
import com.geovannycode.mcpclient.model.QueryResponse;
import com.geovannycode.mcpclient.service.HRAssistantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/hr-assistant")
public final class HRAssistantController {

    private static final Logger logger = LoggerFactory.getLogger(HRAssistantController.class);

    private final HRAssistantService hrAssistantService;

    public HRAssistantController(HRAssistantService hrAssistantService) {
        this.hrAssistantService = hrAssistantService;
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> handleQuery(@Valid @RequestBody QueryRequest request) {
        logger.info("HR Query received - Employee: {}, Query: {}",
                request.employeeId(), request.query());

        return hrAssistantService.processQuery(request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.internalServerError()
                        .body(QueryResponse.error("Error al procesar la consulta")));
    }


    @GetMapping("/policies")
    public ResponseEntity<Map<String, Object>> getPolicyInfo(@RequestParam String policyName) {
        logger.info("Policy query for: {}", policyName);

        return hrAssistantService.getPolicyInformation(policyName)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.internalServerError()
                        .body(Map.of("error", "Error al consultar la política")));
    }


    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Map<String, Object>> getEmployeeInfo(@PathVariable String employeeId) {
        logger.info("Employee info query for: {}", employeeId);

        return hrAssistantService.getEmployeeInformation(employeeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.internalServerError()
                        .body(Map.of("error", "Error al consultar información del empleado")));
    }


    @GetMapping("/benefits")
    public ResponseEntity<Map<String, Object>> getBenefits(
            @RequestParam(required = false) String employeeId) {
        logger.info("Benefits query for employee: {}", employeeId);

        return hrAssistantService.getBenefitsInformation(employeeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.internalServerError()
                        .body(Map.of("error", "Error al consultar beneficios")));
    }
}