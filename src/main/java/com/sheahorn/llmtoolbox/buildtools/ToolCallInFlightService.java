package com.sheahorn.llmtoolbox.buildtools;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ToolCallInFlightService {

    private final Map<String, ToolCallInterceptor.InFlight> inFlight = new ConcurrentHashMap<>();

    public void start(String operationId, String tool) {
        ToolCallInterceptor.InFlight f = new ToolCallInterceptor.InFlight();
        f.operationId = operationId;
        f.tool = tool;
        f.startedAt = Instant.now();
        inFlight.put(operationId, f);
    }

    public void finish(String operationId) {
        inFlight.remove(operationId);
    }

    public Map<String, ToolCallInterceptor.InFlight> getAll() {
        return Map.copyOf(inFlight);
    }
}
