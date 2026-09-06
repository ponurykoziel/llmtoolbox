package com.sheahorn.llmtoolbox.llm;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.sheahorn.llmtoolbox.domain.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(LlmExecutionServiceTest.Profile.class)
@QuarkusTestResource(WireMockTestResource.class)
class LlmExecutionServiceTest {

    public static class Profile implements io.quarkus.test.junit.QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                "llmtoolbox.auth.token", "test-bearer-token",
                "llmtoolbox.auth.seed-on-startup", "false",
                "llmtoolbox.auth.admin.username", "admin",
                "llmtoolbox.auth.admin.password", "admin",
                "llmtoolbox.api-key.pepper", "test-pepper",
                "llmtoolbox.llm.max-tool-rounds", "3",
                "quarkus.hibernate-orm.database.generation", "drop-and-create",
                "quarkus.datasource.jdbc.url", "jdbc:h2:mem:llmtoolbox-wiremock;DB_CLOSE_DELAY=-1"
            );
        }
    }

    @ConfigProperty(name = "wiremock.port")
    int wireMockPort;

    @Inject
    LlmExecutionService service;

    @InjectMock
    ToolDispatcher toolDispatcher;

    @BeforeEach
    @Transactional
    void setUp() {
        WireMock.configureFor("localhost", wireMockPort);
        WireMock.reset();

        // Default mock: no tools, dispatch returns canned result
        when(toolDispatcher.resolveTools(anyString())).thenReturn(List.of());
        when(toolDispatcher.dispatch(anyString(), anyString())).thenReturn("{\"result\":\"ok\"}");

        // Clean up entities
        Agent.findAll().stream().forEach(a -> a.delete());
        Model.findAll().stream().forEach(m -> m.delete());
        Personality.findAll().stream().forEach(p -> p.delete());
        Provider.findAll().stream().forEach(p -> p.delete());
    }

    // ── helpers (call within @Transactional test methods) ────────

    private Provider createProvider(String name, ApiType apiType, ApiMode apiMode) {
        Provider p = Provider.create(name, "http://localhost:" + wireMockPort, null, apiType, apiMode);
        p.persist();
        return p;
    }

    private Model createModel(String niceName, String providerName, String providerId) {
        Model m = Model.create(providerId, niceName, providerName);
        m.persist();
        return m;
    }

    private Personality createPersonality(String niceName) {
        Personality p = Personality.create(niceName);
        p.persist();
        return p;
    }

    private Agent createAgent(String niceName, String providerId, String modelId, String personalityId, String toolPreset) {
        Agent a = Agent.create(niceName, providerId, modelId, personalityId, toolPreset);
        a.persist();
        return a;
    }

    private LlmExecuteRequest req(String agentName, String prompt) {
        LlmExecuteRequest r = new LlmExecuteRequest();
        r.agentName = agentName;
        r.requestPrompt = prompt;
        return r;
    }

    // ── WireMock stub helpers ────────────────────────────────────

    private void stubOpenAiChat(String content) {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"choices":[{"message":{"role":"assistant","content":"%s"}}]}
                    """.formatted(content))));
    }

    private void stubOpenAiChatMultiRound() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .inScenario("multi-round")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"choices":[{"message":{"role":"assistant","tool_calls":[{"id":"call_1","type":"function","function":{"name":"tool_a","arguments":"{}"}}]}}]}
                    """))
            .willSetStateTo("round-2"));

        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .inScenario("multi-round")
            .whenScenarioStateIs("round-2")
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"choices":[{"message":{"role":"assistant","content":"Final answer after tools."}}]}
                    """)));
    }

    private void stubOpenAiChatAlwaysToolCalls() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"choices":[{"message":{"role":"assistant","tool_calls":[{"id":"call_1","type":"function","function":{"name":"loop_tool","arguments":"{}"}}]}}]}
                    """)));
    }

    private void stubOpenAiChatError() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"error":{"message":"Service Unavailable","code":503}}
                    """)));
    }

    private void stubOpenAiChatMalformed() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody("not valid json at all")));
    }

    private void stubOpenAiChatEmptyChoices() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"choices":[]}
                    """)));
    }

    private void stubOpenAiCompletions(String text) {
        stubFor(post(urlEqualTo("/v1/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"choices":[{"text":"%s"}]}
                    """.formatted(text))));
    }

    private void stubOpenAiCompletionsError() {
        stubFor(post(urlEqualTo("/v1/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"error":{"message":"Model overloaded"}}
                    """)));
    }

    private void stubOpenWebuiChat(String content) {
        stubFor(post(urlEqualTo("/api/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"choices":[{"message":{"role":"assistant","content":"%s"}}]}
                    """.formatted(content))));
    }

    private void stubOllamaChat(String content) {
        stubFor(post(urlEqualTo("/api/chat"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"message":{"role":"assistant","content":"%s"}}
                    """.formatted(content))));
    }

    // ══════════════════════════════════════════════════════════════
    // Chat mode — happy paths
    // ══════════════════════════════════════════════════════════════

    @Test
    @Transactional
    void test1_simpleTextResponse() {
        Provider prov = createProvider("openai-prov", ApiType.openai, ApiMode.chat);
        Model model = createModel("gpt-4", "gpt-4-turbo", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, null);

        stubOpenAiChat("Hello! How can I help you?");

        LlmExecuteResponse resp = service.execute(req("test-agent", "Hi"));

        assertEquals("success", resp.status);
        assertEquals("test-agent", resp.agent);
        assertTrue(resp.payload.contains("Hello! How can I help you?"));
    }

    @Test
    @Transactional
    void test2_singleToolCall() {
        Provider prov = createProvider("openai-prov", ApiType.openai, ApiMode.chat);
        Model model = createModel("gpt-4", "gpt-4-turbo", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, "basics");

        when(toolDispatcher.resolveTools(anyString())).thenReturn(
            List.of(Map.of("type", "function", "function", Map.of("name", "test_tool", "description", "A test tool")))
        );

        // Round 1: tool call, Round 2: final text
        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .inScenario("tool-then-text")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"choices":[{"message":{"role":"assistant","tool_calls":[{"id":"call_1","type":"function","function":{"name":"test_tool","arguments":"{\\"query\\":\\"weather\\"}"}}]}}]}
                    """))
            .willSetStateTo("final"));

        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .inScenario("tool-then-text")
            .whenScenarioStateIs("final")
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"choices":[{"message":{"role":"assistant","content":"The weather is sunny."}}]}
                    """)));

        LlmExecuteResponse resp = service.execute(req("test-agent", "What's the weather?"));

        assertEquals("success", resp.status);
        assertTrue(resp.payload.contains("TOOL CALLED"));
        assertTrue(resp.payload.contains("test_tool"));
        assertTrue(resp.payload.contains("TOOL FINISHED"));
        assertTrue(resp.payload.contains("The weather is sunny."));
    }

    @Test
    @Transactional
    void test3_multipleToolCallsInOneRound() {
        Provider prov = createProvider("openai-prov", ApiType.openai, ApiMode.chat);
        Model model = createModel("gpt-4", "gpt-4-turbo", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, "basics");

        when(toolDispatcher.resolveTools(anyString())).thenReturn(
            List.of(Map.of("type", "function", "function", Map.of("name", "tool_a", "description", "A")))
        );

        // Round 1: two tool calls, Round 2: final text
        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .inScenario("multi-tool")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"choices":[{"message":{"role":"assistant","tool_calls":[{"id":"call_1","type":"function","function":{"name":"tool_a","arguments":"{}"}},{"id":"call_2","type":"function","function":{"name":"tool_b","arguments":"{\\"x\\":1}"}}]}}]}
                    """))
            .willSetStateTo("final"));

        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .inScenario("multi-tool")
            .whenScenarioStateIs("final")
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"choices":[{"message":{"role":"assistant","content":"Done with both tools."}}]}
                    """)));

        LlmExecuteResponse resp = service.execute(req("test-agent", "Do stuff"));

        assertEquals("success", resp.status);
        assertTrue(resp.payload.contains("TOOL CALLED: tool_a"));
        assertTrue(resp.payload.contains("TOOL CALLED: tool_b"));
        assertTrue(resp.payload.contains("Done with both tools."));
    }

    @Test
    @Transactional
    void test4_multiRoundToolLoop() {
        Provider prov = createProvider("openai-prov", ApiType.openai, ApiMode.chat);
        Model model = createModel("gpt-4", "gpt-4-turbo", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, "basics");

        when(toolDispatcher.resolveTools(anyString())).thenReturn(
            List.of(Map.of("type", "function", "function", Map.of("name", "tool_a", "description", "A")))
        );

        stubOpenAiChatMultiRound();

        LlmExecuteResponse resp = service.execute(req("test-agent", "Complex query"));

        assertEquals("success", resp.status);
        assertTrue(resp.payload.contains("TOOL CALLED: tool_a"));
        assertTrue(resp.payload.contains("Final answer after tools."));
    }

    // ══════════════════════════════════════════════════════════════
    // Chat mode — error/edge
    // ══════════════════════════════════════════════════════════════

    @Test
    @Transactional
    void test5_maxRoundsExceeded() {
        Provider prov = createProvider("openai-prov", ApiType.openai, ApiMode.chat);
        Model model = createModel("gpt-4", "gpt-4-turbo", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, "basics");

        when(toolDispatcher.resolveTools(anyString())).thenReturn(
            List.of(Map.of("type", "function", "function", Map.of("name", "loop_tool", "description", "Loops")))
        );

        stubOpenAiChatAlwaysToolCalls();

        LlmExecuteResponse resp = service.execute(req("test-agent", "Loop forever"));

        assertEquals("error", resp.status);
        assertTrue(resp.payload.contains("exceeded maximum tool rounds"));
        int count = resp.payload.split("TOOL CALLED").length - 1;
        assertEquals(3, count);
    }

    @Test
    @Transactional
    void test6_llmReturnsErrorJson() {
        Provider prov = createProvider("openai-prov", ApiType.openai, ApiMode.chat);
        Model model = createModel("gpt-4", "gpt-4-turbo", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, null);

        stubOpenAiChatError();

        LlmExecuteResponse resp = service.execute(req("test-agent", "Hi"));

        assertEquals("error", resp.status);
        assertTrue(resp.payload.contains("Service Unavailable"));
    }

    @Test
    @Transactional
    void test7_malformedJsonResponse() {
        Provider prov = createProvider("openai-prov", ApiType.openai, ApiMode.chat);
        Model model = createModel("gpt-4", "gpt-4-turbo", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, null);

        stubOpenAiChatMalformed();

        LlmExecuteResponse resp = service.execute(req("test-agent", "Hi"));

        assertEquals("error", resp.status);
        assertTrue(resp.payload.contains("Failed to parse LLM response"));
    }

    @Test
    @Transactional
    void test8_emptyChoices() {
        Provider prov = createProvider("openai-prov", ApiType.openai, ApiMode.chat);
        Model model = createModel("gpt-4", "gpt-4-turbo", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, null);

        stubOpenAiChatEmptyChoices();

        LlmExecuteResponse resp = service.execute(req("test-agent", "Hi"));

        assertEquals("error", resp.status);
        assertTrue(resp.payload.contains("No message in LLM response"));
    }

    // ══════════════════════════════════════════════════════════════
    // Completions mode
    // ══════════════════════════════════════════════════════════════

    @Test
    @Transactional
    void test9_successfulCompletion() {
        Provider prov = createProvider("openai-prov", ApiType.openai, ApiMode.completions);
        Model model = createModel("gpt-3", "gpt-3.5-turbo-instruct", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, null);

        stubOpenAiCompletions("Paris is the capital of France.");

        LlmExecuteResponse resp = service.execute(req("test-agent", "What is the capital of France?"));

        assertEquals("success", resp.status);
        assertTrue(resp.payload.contains("Paris is the capital of France."));
    }

    @Test
    @Transactional
    void test10_ollamaCompletionsRejected() {
        Provider prov = createProvider("ollama-prov", ApiType.ollama, ApiMode.completions);
        Model model = createModel("llama3", "llama3:8b", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, null);

        LlmExecuteResponse resp = service.execute(req("test-agent", "Hello"));

        assertEquals("error", resp.status);
        assertTrue(resp.payload.contains("Ollama does not support completions mode"));
    }

    @Test
    @Transactional
    void test11_completionsErrorFromApi() {
        Provider prov = createProvider("openai-prov", ApiType.openai, ApiMode.completions);
        Model model = createModel("gpt-3", "gpt-3.5-turbo-instruct", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, null);

        stubOpenAiCompletionsError();

        LlmExecuteResponse resp = service.execute(req("test-agent", "Hello"));

        assertEquals("error", resp.status);
        assertTrue(resp.payload.contains("Model overloaded"));
    }

    // ══════════════════════════════════════════════════════════════
    // Agent resolution failures
    // ══════════════════════════════════════════════════════════════

    @Test
    @Transactional
    void test12_agentNotFound() {
        LlmExecuteResponse resp = service.execute(req("nonexistent", "Hello"));

        assertEquals("error", resp.status);
        assertEquals("nonexistent", resp.agent);
        assertTrue(resp.payload.contains("Agent not found"));
    }

    @Test
    @Transactional
    void test13_providerNotFound() {
        Provider prov = createProvider("temp-prov", ApiType.openai, ApiMode.chat);
        Model model = createModel("m1", "gpt-4", prov.id);
        Personality pers = createPersonality("p1");
        createAgent("broken", prov.id, model.id, pers.id, null);

        Provider.deleteById(prov.id);

        LlmExecuteResponse resp = service.execute(req("broken", "Hello"));

        assertEquals("error", resp.status);
        assertTrue(resp.payload.contains("Provider not found"));
    }

    @Test
    @Transactional
    void test14_modelNotFound() {
        Provider prov = createProvider("temp-prov", ApiType.openai, ApiMode.chat);
        Model model = createModel("m1", "gpt-4", prov.id);
        Personality pers = createPersonality("p1");
        createAgent("broken", prov.id, model.id, pers.id, null);

        Model.deleteById(model.id);

        LlmExecuteResponse resp = service.execute(req("broken", "Hello"));

        assertEquals("error", resp.status);
        assertTrue(resp.payload.contains("Model not found"));
    }

    @Test
    @Transactional
    void test15_personalityNotFound() {
        Provider prov = createProvider("temp-prov", ApiType.openai, ApiMode.chat);
        Model model = createModel("m1", "gpt-4", prov.id);
        Personality pers = createPersonality("p1");
        createAgent("broken", prov.id, model.id, pers.id, null);

        Personality.deleteById(pers.id);

        LlmExecuteResponse resp = service.execute(req("broken", "Hello"));

        assertEquals("error", resp.status);
        assertTrue(resp.payload.contains("Personality not found"));
    }

    // ══════════════════════════════════════════════════════════════
    // Provider type URL verification
    // ══════════════════════════════════════════════════════════════

    @Test
    @Transactional
    void test16_openAiChatUrl() {
        Provider prov = createProvider("openai-prov", ApiType.openai, ApiMode.chat);
        Model model = createModel("gpt-4", "gpt-4-turbo", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, null);

        stubOpenAiChat("Hi there");

        service.execute(req("test-agent", "Hello"));

        verify(postRequestedFor(urlEqualTo("/v1/chat/completions")));
    }

    @Test
    @Transactional
    void test17_openWebuiChatUrl() {
        Provider prov = createProvider("openwebui-prov", ApiType.openwebui, ApiMode.chat);
        Model model = createModel("llama3", "llama3:8b", prov.id);
        Personality pers = createPersonality("helpful");
        createAgent("test-agent", prov.id, model.id, pers.id, null);

        stubOpenWebuiChat("Hello from OpenWebUI");

        service.execute(req("test-agent", "Hello"));

        verify(postRequestedFor(urlEqualTo("/api/chat/completions")));
    }

    @Test
    @Transactional
    void test18_ollamaChatUrlAndBody() {
        Provider prov = createProvider("ollama-prov", ApiType.ollama, ApiMode.chat);
        Model model = createModel("llama3", "llama3:8b", prov.id);
        Personality pers = createPersonality("helpful");
        pers.temperature = 0.7;
        createAgent("test-agent", prov.id, model.id, pers.id, null);

        stubOllamaChat("Hello from Ollama");

        service.execute(req("test-agent", "Hello"));

        verify(postRequestedFor(urlEqualTo("/api/chat"))
            .withRequestBody(matchingJsonPath("$.stream", equalTo("false")))
            .withRequestBody(matchingJsonPath("$.options.temperature", equalTo("0.7")))
            .withRequestBody(matchingJsonPath("$.model", equalTo("llama3:8b"))));
    }
}
