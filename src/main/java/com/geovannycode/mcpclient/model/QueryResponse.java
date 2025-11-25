package com.geovannycode.mcpclient.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public final class QueryResponse {

    private final String response;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    private final List<String> toolsUsed;


    private final Object metadata;


    public Optional<List<String>> getToolsUsedOptional() {
        return Optional.ofNullable(toolsUsed);
    }


    public Optional<Object> getMetadataOptional() {
        return Optional.ofNullable(metadata);
    }


    public static QueryResponse simple(String response) {
        return QueryResponse.builder()
                .response(response)
                .build();
    }


    public static QueryResponse error(String errorMessage) {
        return QueryResponse.builder()
                .response("Error: " + errorMessage)
                .build();
    }
}
