package com.openclaw.relay.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPayload {

    private String method;

    private String path;

    private Map<String, String> query;

    private Map<String, String> headers;

    private String body;

    private String bodyBase64;

    private Boolean isBase64;
}
