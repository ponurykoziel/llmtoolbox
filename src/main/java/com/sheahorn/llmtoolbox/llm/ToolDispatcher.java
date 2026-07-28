package com.sheahorn.llmtoolbox.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheahorn.llmtoolbox.basics.presets.PresetDefaults;
import com.sheahorn.llmtoolbox.openapitools.BuiltinFunctionCache;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@ApplicationScoped
public class ToolDispatcher {

    private static final Logger LOG = Logger.getLogger(ToolDispatcher.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    BuiltinFunctionCache functionCache;

    /** Maps operationId → ToolMethod. Built at startup via CDI scan. */
    private volatile Map<String, ToolMethod> dispatchIndex;

    /**
     * Holds a bean instance, its method, and the parameter-binding strategy
     * needed to invoke it from a JSON arguments object.
     */
    public static class ToolMethod {
        public final Object bean;
        public final Method method;
        public final ParamBinding[] bindings;

        public ToolMethod(Object bean, Method method, ParamBinding[] bindings) {
            this.bean = bean;
            this.method = method;
            this.bindings = bindings;
        }
    }

    /** Describes how to resolve a single method parameter from the JSON arguments. */
    public enum BindingKind { NONE, DTO }

    public static class ParamBinding {
        public final BindingKind kind;
        public final Class<?> type;

        public ParamBinding(BindingKind kind, Class<?> type) {
            this.kind = kind;
            this.type = type;
        }
    }

    // ── startup scan ──────────────────────────────────────────

    void init(@Observes StartupEvent event) {
        buildDispatchIndex();
    }

    private synchronized void buildDispatchIndex() {
        if (dispatchIndex != null) return;

        Map<String, ToolMethod> index = new LinkedHashMap<>();

        for (Object bean : Arc.container().select(ToolBean.class)) {
            // Unwrap CDI proxy to get the real class for method scanning
            Class<?> beanClass = bean.getClass();
            // CDI proxies often have generated names; walk up to the first non-synthetic class
            while (beanClass.isSynthetic() || beanClass.getName().contains("$$")) {
                Class<?> sup = beanClass.getSuperclass();
                if (sup == null || sup == Object.class) break;
                beanClass = sup;
            }

            for (Method method : beanClass.getDeclaredMethods()) {
                Operation op = method.getAnnotation(Operation.class);
                if (op == null || op.operationId() == null || op.operationId().isBlank()) continue;

                String operationId = op.operationId();
                ParamBinding[] bindings = buildBindings(method);

                // Find the actual method on the proxy class (may differ from declared)
                Method proxyMethod;
                try {
                    proxyMethod = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    proxyMethod = method; // fallback
                }

                index.put(operationId, new ToolMethod(bean, proxyMethod, bindings));
            }
        }

        dispatchIndex = index;
        LOG.infof("ToolDispatcher indexed %d dispatchable methods from %d ToolBean instances.",
                index.size(),
                index.values().stream().map(tm -> tm.bean.getClass().getName()).distinct().count());
    }

    private ParamBinding[] buildBindings(Method method) {
        Parameter[] params = method.getParameters();
        if (params.length == 0) return new ParamBinding[0];

        ParamBinding[] bindings = new ParamBinding[params.length];

        for (int i = 0; i < params.length; i++) {
            Parameter p = params[i];
            // All parameters are body DTOs per ADR-0013 rule 4
            bindings[i] = new ParamBinding(BindingKind.DTO, p.getType());
        }

        return bindings;
    }

    // ── resolveTools (unchanged) ──────────────────────────────

    /**
     * Resolves a preset name to a list of tool definitions suitable for the LLM API.
     */
    public List<Map<String, Object>> resolveTools(String presetName) {
        if (presetName == null || presetName.isBlank()) return List.of();

        List<String> prefixes = PresetDefaults.resolve(presetName);
        if (prefixes == null || prefixes.isEmpty()) return List.of();

        Set<String> opIds = new LinkedHashSet<>();
        for (String p : prefixes) {
            if (p.equals("*")) {
                opIds.addAll(functionCache.all().keySet());
            } else if (p.endsWith("*")) {
                String prefix = p.substring(0, p.length() - 1);
                for (String id : functionCache.all().keySet()) {
                    if (id.startsWith(prefix)) opIds.add(id);
                }
            } else {
                opIds.add(p);
            }
        }

        List<Map<String, Object>> tools = new ArrayList<>();
        for (String opId : opIds) {
            String desc = functionCache.all().getOrDefault(opId, "");
            tools.add(buildToolDef(opId, desc));
        }
        return tools;
    }

    // ── dispatch (in-process) ──────────────────────────────────

    /**
     * Dispatches a tool call: looks up the operationId in the CDI-built index,
     * binds JSON arguments to method parameters, invokes the method, and
     * serializes the result to a JSON string.
     */
    public String dispatch(String operationId, String argumentsJson) {
        if (dispatchIndex == null) {
            buildDispatchIndex();
        }

        ToolMethod tm = dispatchIndex.get(operationId);
        if (tm == null) {
            return "[ TOOL ERROR: unknown operationId: " + operationId + " ]";
        }

        try {
            JsonNode argsNode = (argumentsJson != null && !argumentsJson.isBlank())
                    ? MAPPER.readTree(argumentsJson)
                    : MAPPER.createObjectNode();

            Object[] callArgs = bindArguments(tm.bindings, argsNode);
            Object result = tm.method.invoke(tm.bean, callArgs);

            // If the result is a jakarta.ws.rs.core.Response, extract the entity
            if (result instanceof jakarta.ws.rs.core.Response resp) {
                Object entity = resp.getEntity();
                if (entity != null) {
                    return MAPPER.writeValueAsString(entity);
                }
                return "{\"status\":" + resp.getStatus() + "}";
            }

            return MAPPER.writeValueAsString(result);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return "[ TOOL ERROR: " + cause.getClass().getSimpleName() + ": " + cause.getMessage() + " ]";
        }
    }

    private Object[] bindArguments(ParamBinding[] bindings, JsonNode argsNode) throws Exception {
        if (bindings.length == 0) return new Object[0];

        Object[] args = new Object[bindings.length];

        for (int i = 0; i < bindings.length; i++) {
            ParamBinding b = bindings[i];

            switch (b.kind) {
                case NONE:
                    args[i] = null;
                    break;

                case DTO:
                    args[i] = MAPPER.treeToValue(argsNode, b.type);
                    break;
            }
        }

        return args;
    }

    // ── tool definition builder (unchanged) ───────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildToolDef(String operationId, String description) {
        Map<String, Object> func = new LinkedHashMap<>();
        func.put("name", operationId);
        func.put("description", description != null ? description : "");

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "object");
        params.put("additionalProperties", true);
        func.put("parameters", params);

        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("type", "function");
        tool.put("function", func);
        return tool;
    }
}
