package com.sheahorn.llmtoolbox.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sheahorn.llmtoolbox.domain.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@ApplicationScoped
public class LlmExecutionService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Inject
    ToolDispatcher toolDispatcher;

    @Inject
    ImageAttachmentHelper imageAttachmentHelper;

    @ConfigProperty(name = "llmtoolbox.llm.max-tool-rounds", defaultValue = "20")
    int maxToolRounds;

    public LlmExecuteResponse execute(LlmExecuteRequest request) {
        LlmExecuteResponse response = new LlmExecuteResponse();

        // 1. Resolve agent
        Agent agent = Agent.find("niceName", request.agentName).firstResult();
        if (agent == null) {
            response.agent = request.agentName;
            response.status = "error";
            response.payload = "Agent not found: " + request.agentName;
            return response;
        }
        response.agent = agent.niceName;

        // 2. Resolve provider, model, personality
        Provider provider = Provider.findById(agent.providerId);
        Model model = Model.findById(agent.modelId);
        Personality personality = Personality.findById(agent.personalityId);

        if (provider == null) {
            response.status = "error";
            response.payload = "Provider not found for agent: " + agent.niceName;
            return response;
        }
        if (model == null) {
            response.status = "error";
            response.payload = "Model not found for agent: " + agent.niceName;
            return response;
        }
        if (personality == null) {
            response.status = "error";
            response.payload = "Personality not found for agent: " + agent.niceName;
            return response;
        }

        // 3. Resolve optional image attachment
        ImageAttachmentHelper.ImageAttachment image = null;
        if (request.imagePath != null && !request.imagePath.isBlank()) {
            if (provider.apiMode == ApiMode.completions) {
                response.status = "error";
                response.payload = "Image attachments are not supported in completions mode. Use chat mode instead.";
                return response;
            }
            try {
                image = imageAttachmentHelper.load(request.imagePath);
            } catch (IllegalArgumentException e) {
                response.status = "error";
                response.payload = e.getMessage();
                return response;
            }
        }

        // 4. Completions mode — single request, no tools
        if (provider.apiMode == ApiMode.completions) {
            return executeCompletions(response, provider, model, personality, request.requestPrompt);
        }

        // 5. Chat mode — tool loop
        return executeChat(response, provider, model, personality, request.requestPrompt, agent.toolPreset, image);
    }

    // ── Completions mode ───────────────────────────────────────

    private LlmExecuteResponse executeCompletions(LlmExecuteResponse response,
                                                   Provider provider, Model model, Personality personality,
                                                   String userPrompt) {
        if (provider.apiType == ApiType.ollama) {
            response.status = "error";
            response.payload = "Ollama does not support completions mode. Use chat mode instead.";
            return response;
        }

        StringBuilder prompt = new StringBuilder();
        if (personality.systemPrompt != null && !personality.systemPrompt.isBlank()) {
            prompt.append(personality.systemPrompt).append("\n\n");
        }
        prompt.append(userPrompt);

        try {
            String body = callCompletions(provider, model, personality, prompt.toString());
            JsonNode json = MAPPER.readTree(body);

            if (json.has("error")) {
                response.status = "error";
                response.payload = json.get("error").toPrettyString();
                return response;
            }

            JsonNode choices = json.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                response.status = "error";
                response.payload = "No choices in LLM response: " + body;
                return response;
            }

            JsonNode text = choices.get(0).get("text");
            response.status = "success";
            response.payload = text != null ? text.asText() : "";
        } catch (Exception e) {
            response.status = "error";
            response.payload = e.getClass().getSimpleName() + ": " + e.getMessage();
        }

        return response;
    }

    // ── Chat mode (tool loop) ──────────────────────────────────

    private LlmExecuteResponse executeChat(LlmExecuteResponse response,
                                            Provider provider, Model model, Personality personality,
                                            String userPrompt, String toolPreset,
                                            ImageAttachmentHelper.ImageAttachment image) {
        List<Map<String, Object>> tools = toolDispatcher.resolveTools(toolPreset);

        List<Map<String, Object>> messages = new ArrayList<>();
        if (personality.systemPrompt != null && !personality.systemPrompt.isBlank()) {
            messages.add(Map.of("role", "system", "content", personality.systemPrompt));
        }
        messages.add(buildUserMessage(provider, userPrompt, image));

        StringBuilder output = new StringBuilder();

        try {
            for (int round = 0; round < maxToolRounds; round++) {
                String llmResponseBody = callChat(provider, model, personality, messages, tools, image);

                JsonNode llmJson;
                try {
                    llmJson = MAPPER.readTree(llmResponseBody);
                } catch (JsonProcessingException e) {
                    response.status = "error";
                    response.payload = "Failed to parse LLM response: " + e.getMessage();
                    return response;
                }

                if (llmJson.has("error")) {
                    response.status = "error";
                    response.payload = llmJson.get("error").toPrettyString();
                    return response;
                }

                JsonNode message = extractMessage(llmJson);
                if (message == null) {
                    response.status = "error";
                    response.payload = "No message in LLM response: " + llmResponseBody;
                    return response;
                }

                JsonNode toolCalls = message.get("tool_calls");
                boolean hasToolCalls = toolCalls != null && toolCalls.isArray() && toolCalls.size() > 0;

                if (hasToolCalls) {
                    messages.add(jsonToMessage(message));

                    for (JsonNode tc : toolCalls) {
                        String toolCallId = tc.has("id") ? tc.get("id").asText() : "call_" + round;
                        JsonNode function = tc.get("function");
                        String funcName = function != null && function.has("name") ? function.get("name").asText() : "unknown";
                        String funcArgs = extractToolArguments(function);

                        output.append("[ TOOL CALLED: ").append(funcName).append(" ").append(funcArgs).append(" ]\n");

                        String toolResult = toolDispatcher.dispatch(funcName, funcArgs);

                        output.append("[ TOOL FINISHED WITH STATUS: ").append(toolResult).append(" ]\n");

                        Map<String, Object> toolMsg = new LinkedHashMap<>();
                        toolMsg.put("role", "tool");
                        toolMsg.put("tool_call_id", toolCallId);
                        toolMsg.put("content", toolResult);
                        messages.add(toolMsg);
                    }
                } else {
                    JsonNode content = message.get("content");
                    String text = content != null ? content.asText() : "";
                    output.append(text);
                    response.status = "success";
                    response.payload = output.toString();
                    return response;
                }
            }

            response.status = "error";
            response.payload = output + "\n[ ERROR: exceeded maximum tool rounds (" + maxToolRounds + ") ]";

        } catch (Exception e) {
            response.status = "error";
            response.payload = output + "\n[ ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage() + " ]";
        }

        return response;
    }

    // ── Chat API calls ─────────────────────────────────────────

    private String callChat(Provider provider, Model model, Personality personality,
                            List<Map<String, Object>> messages,
                            List<Map<String, Object>> tools,
                            ImageAttachmentHelper.ImageAttachment image) throws Exception {
        if (provider.apiType == ApiType.ollama) {
            return callOllamaChat(provider, model, personality, messages, tools, image);
        } else if (provider.apiType == ApiType.openwebui) {
            return callOpenWebuiChat(provider, model, personality, messages, tools);
        } else {
            return callOpenAiChat(provider, model, personality, messages, tools);
        }
    }

    private String callOpenAiChat(Provider provider, Model model, Personality personality,
                                  List<Map<String, Object>> messages,
                                  List<Map<String, Object>> tools) throws Exception {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("model", model.providerName);
        body.set("messages", MAPPER.valueToTree(messages));
        if (tools != null && !tools.isEmpty()) {
            body.set("tools", MAPPER.valueToTree(tools));
        }
        applyPersonality(body, personality);

        String url = stripTrailingSlash(provider.baseUrl) + "/v1/chat/completions";
        return postJson(url, provider.apiKey, body);
    }

    /**
     * OpenWebUI is currently OpenAI-compatible but uses /api/chat/completions
     * instead of /v1/chat/completions. We keep this as a separate method because
     * OpenWebUI may diverge from the OpenAI API in the future.
     */
    private String callOpenWebuiChat(Provider provider, Model model, Personality personality,
                                     List<Map<String, Object>> messages,
                                     List<Map<String, Object>> tools) throws Exception {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("model", model.providerName);
        body.set("messages", MAPPER.valueToTree(messages));
        if (tools != null && !tools.isEmpty()) {
            body.set("tools", MAPPER.valueToTree(tools));
        }
        applyPersonality(body, personality);

        String url = stripTrailingSlash(provider.baseUrl) + "/api/chat/completions";
        return postJson(url, provider.apiKey, body);
    }

    private String callOllamaChat(Provider provider, Model model, Personality personality,
                                  List<Map<String, Object>> messages,
                                  List<Map<String, Object>> tools,
                                  ImageAttachmentHelper.ImageAttachment image) throws Exception {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("model", model.providerName);
        body.set("messages", MAPPER.valueToTree(messages));
        body.put("stream", false);
        if (tools != null && !tools.isEmpty()) {
            body.set("tools", MAPPER.valueToTree(tools));
        }
        if (image != null) {
            ArrayNode images = body.putArray("images");
            images.add(image.base64);
        }

        ObjectNode options = MAPPER.createObjectNode();
        applyPersonalityToOptions(options, personality);
        if (!options.isEmpty()) {
            body.set("options", options);
        }

        String url = stripTrailingSlash(provider.baseUrl) + "/api/chat";
        return postJson(url, provider.apiKey, body);
    }

    // ── Completions API calls ───────────────────────────────────

    private String callCompletions(Provider provider, Model model, Personality personality,
                                   String prompt) throws Exception {
        if (provider.apiType == ApiType.openwebui) {
            return callOpenWebuiCompletions(provider, model, personality, prompt);
        } else {
            return callOpenAiCompletions(provider, model, personality, prompt);
        }
    }

    private String callOpenAiCompletions(Provider provider, Model model, Personality personality,
                                         String prompt) throws Exception {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("model", model.providerName);
        body.put("prompt", prompt);
        applyPersonality(body, personality);

        String url = stripTrailingSlash(provider.baseUrl) + "/v1/completions";
        return postJson(url, provider.apiKey, body);
    }

    /**
     * OpenWebUI completions endpoint. Kept separate from OpenAI for the same
     * reason as the chat variant — OpenWebUI may diverge.
     */
    private String callOpenWebuiCompletions(Provider provider, Model model, Personality personality,
                                            String prompt) throws Exception {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("model", model.providerName);
        body.put("prompt", prompt);
        applyPersonality(body, personality);

        String url = stripTrailingSlash(provider.baseUrl) + "/api/completions";
        return postJson(url, provider.apiKey, body);
    }

    // ── User message construction ──────────────────────────────

    /**
     * Builds the user message content. For OpenAI/OpenWebUI providers the
     * content becomes a multimodal array of parts (text + image_url). For
     * Ollama the content stays a plain text string — the image is attached
     * separately as a top-level {@code images} array in {@link #callOllamaChat}.
     */
    private Map<String, Object> buildUserMessage(Provider provider, String userPrompt,
                                                 ImageAttachmentHelper.ImageAttachment image) {
        if (image == null || provider.apiType == ApiType.ollama) {
            return Map.of("role", "user", "content", userPrompt);
        }

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(Map.of("type", "text", "text", userPrompt));
        parts.add(Map.of(
            "type", "image_url",
            "image_url", Map.of("url", image.dataUrl())
        ));

        return Map.of("role", "user", "content", parts);
    }

    // ── HTTP helper ─────────────────────────────────────────────

    private String postJson(String url, String apiKey, ObjectNode body) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(120));

        if (apiKey != null && !apiKey.isBlank()) {
            req.header("Authorization", "Bearer " + apiKey);
        }

        req.POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)));

        HttpResponse<String> resp = HTTP.send(req.build(), HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    private static String stripTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    // ── Personality → API params ───────────────────────────────

    private void applyPersonality(ObjectNode body, Personality p) {
        if (p.temperature >= 0) body.put("temperature", p.temperature);
        if (p.topP >= 0) body.put("top_p", p.topP);
        if (p.frequencyPenalty >= 0) body.put("frequency_penalty", p.frequencyPenalty);
        if (p.presencePenalty >= 0) body.put("presence_penalty", p.presencePenalty);
        if (p.reasoningEffort != null && !p.reasoningEffort.isBlank()) {
            body.put("reasoning_effort", p.reasoningEffort);
        }
    }

    private void applyPersonalityToOptions(ObjectNode options, Personality p) {
        if (p.temperature >= 0) options.put("temperature", p.temperature);
        if (p.minP >= 0) options.put("min_p", p.minP);
        if (p.topP >= 0) options.put("top_p", p.topP);
        if (p.topK >= 0) options.put("top_k", p.topK);
        if (p.frequencyPenalty >= 0) options.put("frequency_penalty", p.frequencyPenalty);
        if (p.presencePenalty >= 0) options.put("presence_penalty", p.presencePenalty);
    }

    // ── Helpers ─────────────────────────────────────────────────

    /**
     * Extracts the tool-call arguments as a JSON string, tolerating the
     * variations providers emit: {@code arguments} may be a JSON string, an
     * already-parsed object, or (in some Ollama/OpenWebUI responses) nested
     * under a {@code parameters} key. Always returns a JSON object string.
     */
    private String extractToolArguments(JsonNode function) {
        if (function == null) return "{}";

        JsonNode args = function.get("arguments");
        if (args == null || args.isMissingNode() || args.isNull()) {
            args = function.get("parameters");
        }
        if (args == null || args.isMissingNode() || args.isNull()) {
            return "{}";
        }

        if (args.isTextual()) {
            String text = args.asText();
            if (text == null || text.isBlank()) return "{}";
            return text;
        }

        // Already a structured node (object/array) — re-serialize to a string.
        try {
            return MAPPER.writeValueAsString(args);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private JsonNode extractMessage(JsonNode llmJson) {
        if (llmJson.has("message") && llmJson.get("message").isObject()) {
            return llmJson.get("message");
        }
        JsonNode choices = llmJson.get("choices");
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode msg = choices.get(0).get("message");
            if (msg != null && msg.isObject()) {
                return msg;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> jsonToMessage(JsonNode message) {
        return MAPPER.convertValue(message, Map.class);
    }
}
