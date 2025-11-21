package com.geovannycode.mcpclient.model;

import jakarta.validation.constraints.NotBlank;

public record QueryRequest(
        @NotBlank(message = "La consulta no puede estar vacía")
        String query,

        String employeeId,

        String context
) {

    public QueryRequest {
        if (query != null) {
            query = query.trim();
        }
    }

    public static QueryRequest of(String query) {
        return new QueryRequest(query, null, null);
    }
    
    public static QueryRequest of(String query, String employeeId) {
        return new QueryRequest(query, employeeId, null);
    }
}
