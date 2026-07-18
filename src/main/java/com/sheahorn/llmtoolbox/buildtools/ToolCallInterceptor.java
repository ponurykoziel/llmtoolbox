package com.sheahorn.llmtoolbox.buildtools;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.time.Instant;

@LogToolCall
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class ToolCallInterceptor {

    @Inject
    ToolCallHistoryService history;

    @Inject
    ToolCallInFlightService inFlightService;

    @AroundInvoke
    public Object log(InvocationContext ctx) throws Exception {
        String operationId = ctx.getMethod().getAnnotation(org.eclipse.microprofile.openapi.annotations.Operation.class) != null
                ? ctx.getMethod().getAnnotation(org.eclipse.microprofile.openapi.annotations.Operation.class).operationId()
                : ctx.getMethod().getName();

        String tool = toolFromOperationId(operationId);

        long start = System.currentTimeMillis();
        inFlightService.start(operationId, tool);

        boolean success = true;
        String error = null;
        try {
            return ctx.proceed();
        } catch (Exception e) {
            success = false;
            error = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;
            inFlightService.finish(operationId);
            history.record(tool, operationId, duration, success, error);
        }
    }

    private String toolFromOperationId(String opId) {
        if (opId == null) return "unknown";
        if (opId.startsWith("build_mvn_")) return "mvn";
        if (opId.startsWith("devops_git_")) return "git";
        if (opId.startsWith("devops_docker_")) return "docker";
        return "other";
    }

    public static class InFlight {
        public String operationId;
        public String tool;
        public Instant startedAt;
    }
}
