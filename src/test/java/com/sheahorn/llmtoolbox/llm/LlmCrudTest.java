package com.sheahorn.llmtoolbox.llm;

import com.sheahorn.llmtoolbox.domain.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(LlmCrudTest.Profile.class)
class LlmCrudTest {

    public static class Profile implements io.quarkus.test.junit.QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                "llmtoolbox.auth.token", "test-bearer-token",
                "llmtoolbox.auth.seed-on-startup", "false",
                "llmtoolbox.auth.admin.username", "admin",
                "llmtoolbox.auth.admin.password", "admin",
                "llmtoolbox.api-key.pepper", "test-pepper"
            );
        }
    }

    private static final String AUTH = "Bearer test-bearer-token";

    @BeforeEach
    @Transactional
    void cleanUp() {
        Agent.findAll().stream().forEach(a -> a.delete());
        Model.findAll().stream().forEach(m -> m.delete());
        Personality.findAll().stream().forEach(p -> p.delete());
        Provider.findAll().stream().forEach(p -> p.delete());
    }

    // ── PROVIDER ───────────────────────────────────────────────

    @Test
    void providerCreateAndGet() {
        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"test-prov","baseUrl":"http://localhost:11434","apiType":"ollama"}
                    """)
                .when().post("/api/llm/providers")
                .then().statusCode(200)
                .body("name", equalTo("test-prov"))
                .body("baseUrl", equalTo("http://localhost:11434"))
                .body("apiType", equalTo("ollama"))
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"id\":\"" + id + "\"}")
                .when().post("/api/llm/providers/get")
                .then().statusCode(200)
                .body("name", equalTo("test-prov"));
    }

    @Test
    void providerCreateDuplicateName() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"dup","baseUrl":"http://a","apiType":"ollama"}
                    """)
                .when().post("/api/llm/providers")
                .then().statusCode(200);

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"dup","baseUrl":"http://b","apiType":"openai"}
                    """)
                .when().post("/api/llm/providers")
                .then().statusCode(409)
                .body("error", equalTo("provider name already exists"));
    }

    @Test
    void providerCreateMissingName() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"baseUrl":"http://a","apiType":"ollama"}
                    """)
                .when().post("/api/llm/providers")
                .then().statusCode(400)
                .body("error", equalTo("name is required"));
    }

    @Test
    void providerCreateMissingBaseUrl() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"x","apiType":"ollama"}
                    """)
                .when().post("/api/llm/providers")
                .then().statusCode(400)
                .body("error", equalTo("baseUrl is required"));
    }

    @Test
    void providerCreateMissingApiType() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"x","baseUrl":"http://a"}
                    """)
                .when().post("/api/llm/providers")
                .then().statusCode(400)
                .body("error", equalTo("apiType is required (ollama, openai, or openwebui)"));
    }

    @Test
    void providerCreateBlankName() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"  ","baseUrl":"http://a","apiType":"ollama"}
                    """)
                .when().post("/api/llm/providers")
                .then().statusCode(400)
                .body("error", equalTo("name is required"));
    }

    @Test
    void providerPatch() {
        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"orig","baseUrl":"http://a","apiType":"ollama"}
                    """)
                .when().post("/api/llm/providers")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"id\":\"" + id + "\",\"name\":\"renamed\",\"baseUrl\":\"http://b\",\"apiType\":\"openai\"}")
                .when().post("/api/llm/providers/patch")
                .then().statusCode(200)
                .body("name", equalTo("renamed"))
                .body("baseUrl", equalTo("http://b"))
                .body("apiType", equalTo("openai"));
    }

    @Test
    void providerPatchBlankName() {
        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"orig","baseUrl":"http://a","apiType":"ollama"}
                    """)
                .when().post("/api/llm/providers")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"id\":\"" + id + "\",\"name\":\"  \"}")
                .when().post("/api/llm/providers/patch")
                .then().statusCode(400)
                .body("error", equalTo("name must not be blank"));
    }

    @Test
    void providerPatchDuplicateName() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"a","baseUrl":"http://a","apiType":"ollama"}
                    """)
                .when().post("/api/llm/providers");

        String idB = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"b","baseUrl":"http://b","apiType":"ollama"}
                    """)
                .when().post("/api/llm/providers")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"id\":\"" + idB + "\",\"name\":\"a\"}")
                .when().post("/api/llm/providers/patch")
                .then().statusCode(409)
                .body("error", equalTo("provider name already exists"));
    }

    @Test
    void providerDelete() {
        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"del","baseUrl":"http://a","apiType":"ollama"}
                    """)
                .when().post("/api/llm/providers")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"id\":\"" + id + "\"}")
                .when().post("/api/llm/providers/delete")
                .then().statusCode(204);

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"id\":\"" + id + "\"}")
                .when().post("/api/llm/providers/get")
                .then().statusCode(404);
    }

    @Test
    void providerGetNotFound() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"id\":\"nonexistent\"}")
                .when().post("/api/llm/providers/get")
                .then().statusCode(404)
                .body("error", equalTo("provider not found"));
    }

    @Test
    void providerListEmpty() {
        given()
                .header("Authorization", AUTH)
                .when().get("/api/llm/providers")
                .then().statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void providerListMultiple() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"p1","baseUrl":"http://a","apiType":"ollama"}
                    """)
                .when().post("/api/llm/providers");
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"name":"p2","baseUrl":"http://b","apiType":"openai"}
                    """)
                .when().post("/api/llm/providers");

        given()
                .header("Authorization", AUTH)
                .when().get("/api/llm/providers")
                .then().statusCode(200)
                .body("size()", equalTo(2));
    }

    // ── MODEL ──────────────────────────────────────────────────

    private String createProvider(String name) {
        return given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"name\":\"" + name + "\",\"baseUrl\":\"http://x\",\"apiType\":\"ollama\"}")
                .when().post("/api/llm/providers")
                .then().statusCode(200)
                .extract().path("id");
    }

    @Test
    void modelCreateAndGet() {
        String provId = createProvider("prov1");

        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"gpt-4o\",\"providerName\":\"gpt-4o-2024\",\"providerId\":\"" + provId + "\"}")
                .when().post("/api/llm/models")
                .then().statusCode(200)
                .body("niceName", equalTo("gpt-4o"))
                .body("providerName", equalTo("gpt-4o-2024"))
                .body("providerId", equalTo(provId))
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .when().get("/api/llm/models/" + id)
                .then().statusCode(200)
                .body("niceName", equalTo("gpt-4o"));
    }

    @Test
    void modelCreateDuplicateNiceName() {
        String provId = createProvider("prov1");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"dup\",\"providerName\":\"a\",\"providerId\":\"" + provId + "\"}")
                .when().post("/api/llm/models")
                .then().statusCode(200);

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"dup\",\"providerName\":\"b\",\"providerId\":\"" + provId + "\"}")
                .when().post("/api/llm/models")
                .then().statusCode(409)
                .body("error", equalTo("model niceName already exists"));
    }

    @Test
    void modelCreateMissingFields() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"providerName\":\"x\",\"providerId\":\"x\"}")
                .when().post("/api/llm/models")
                .then().statusCode(400)
                .body("error", equalTo("niceName is required"));
    }

    @Test
    void modelCreateNonexistentProvider() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"m\",\"providerName\":\"x\",\"providerId\":\"nonexistent\"}")
                .when().post("/api/llm/models")
                .then().statusCode(400)
                .body("error", equalTo("provider not found"));
    }

    @Test
    void modelPatch() {
        String provId = createProvider("prov1");
        String provId2 = createProvider("prov2");

        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"orig\",\"providerName\":\"orig-pn\",\"providerId\":\"" + provId + "\"}")
                .when().post("/api/llm/models")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"renamed\",\"providerName\":\"new-pn\",\"providerId\":\"" + provId2 + "\"}")
                .when().patch("/api/llm/models/" + id)
                .then().statusCode(200)
                .body("niceName", equalTo("renamed"))
                .body("providerName", equalTo("new-pn"))
                .body("providerId", equalTo(provId2));
    }

    @Test
    void modelDelete() {
        String provId = createProvider("prov1");
        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"del\",\"providerName\":\"x\",\"providerId\":\"" + provId + "\"}")
                .when().post("/api/llm/models")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .when().delete("/api/llm/models/" + id).then().statusCode(204);
        given()
                .header("Authorization", AUTH)
                .when().get("/api/llm/models/" + id).then().statusCode(404);
    }

    @Test
    void modelGetNotFound() {
        given()
                .header("Authorization", AUTH)
                .when().get("/api/llm/models/nonexistent")
                .then().statusCode(404)
                .body("error", equalTo("model not found"));
    }

    // ── PERSONALITY ────────────────────────────────────────────

    @Test
    void personalityCreateAndGet() {
        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"niceName":"helpful","systemPrompt":"You are helpful.","temperature":0.7,"topP":0.9}
                    """)
                .when().post("/api/llm/personalities")
                .then().statusCode(200)
                .body("niceName", equalTo("helpful"))
                .body("systemPrompt", equalTo("You are helpful."))
                .body("temperature", equalTo(0.7F))
                .body("topP", equalTo(0.9F))
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .when().get("/api/llm/personalities/" + id)
                .then().statusCode(200)
                .body("niceName", equalTo("helpful"));
    }

    @Test
    void personalityCreateDuplicateNiceName() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"dup\"}")
                .when().post("/api/llm/personalities")
                .then().statusCode(200);

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"dup\"}")
                .when().post("/api/llm/personalities")
                .then().statusCode(409)
                .body("error", equalTo("personality niceName already exists"));
    }

    @Test
    void personalityCreateMissingNiceName() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"temperature\":0.5}")
                .when().post("/api/llm/personalities")
                .then().statusCode(400)
                .body("error", equalTo("niceName is required"));
    }

    @Test
    void personalityDefaults() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"defaults\"}")
                .when().post("/api/llm/personalities")
                .then().statusCode(200)
                .body("temperature", equalTo(-1.0F))
                .body("minP", equalTo(-1.0F))
                .body("topP", equalTo(-1.0F))
                .body("topK", equalTo(-1.0F))
                .body("frequencyPenalty", equalTo(-1.0F))
                .body("presencePenalty", equalTo(-1.0F));
    }

    @Test
    void personalitySanitizeNegativeToMinusOne() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"niceName":"neg","temperature":-5.0,"topP":-0.01}
                    """)
                .when().post("/api/llm/personalities")
                .then().statusCode(200)
                .body("temperature", equalTo(-1.0F))
                .body("topP", equalTo(-1.0F));
    }

    @Test
    void personalitySanitizeRounding() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"niceName":"round","temperature":0.12345,"topP":0.99999}
                    """)
                .when().post("/api/llm/personalities")
                .then().statusCode(200)
                .body("temperature", equalTo(0.12F))
                .body("topP", equalTo(1.0F));
    }

    @Test
    void personalitySanitizeNullToMinusOne() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"niceName":"nulls","temperature":null,"topP":null}
                    """)
                .when().post("/api/llm/personalities")
                .then().statusCode(200)
                .body("temperature", equalTo(-1.0F))
                .body("topP", equalTo(-1.0F));
    }

    @Test
    void personalityPatch() {
        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"orig\"}")
                .when().post("/api/llm/personalities")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {"niceName":"patched","systemPrompt":"Be concise.","temperature":0.3,"reasoningEffort":"low"}
                    """)
                .when().patch("/api/llm/personalities/" + id)
                .then().statusCode(200)
                .body("niceName", equalTo("patched"))
                .body("systemPrompt", equalTo("Be concise."))
                .body("temperature", equalTo(0.3F))
                .body("reasoningEffort", equalTo("low"));
    }

    @Test
    void personalityPatchDuplicateName() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"a\"}")
                .when().post("/api/llm/personalities");

        String idB = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"b\"}")
                .when().post("/api/llm/personalities")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"a\"}")
                .when().patch("/api/llm/personalities/" + idB)
                .then().statusCode(409)
                .body("error", equalTo("personality niceName already exists"));
    }

    @Test
    void personalityDelete() {
        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"del\"}")
                .when().post("/api/llm/personalities")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .when().delete("/api/llm/personalities/" + id).then().statusCode(204);
        given()
                .header("Authorization", AUTH)
                .when().get("/api/llm/personalities/" + id).then().statusCode(404);
    }

    @Test
    void personalityGetNotFound() {
        given()
                .header("Authorization", AUTH)
                .when().get("/api/llm/personalities/nonexistent")
                .then().statusCode(404)
                .body("error", equalTo("personality not found"));
    }

    @Test
    void personalityAllFields() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "niceName":"full",
                        "systemPrompt":"You are a pirate.",
                        "temperature":0.8,
                        "minP":0.05,
                        "topP":0.95,
                        "topK":40.0,
                        "frequencyPenalty":0.3,
                        "presencePenalty":0.1,
                        "reasoningEffort":"high"
                    }
                    """)
                .when().post("/api/llm/personalities")
                .then().statusCode(200)
                .body("niceName", equalTo("full"))
                .body("systemPrompt", equalTo("You are a pirate."))
                .body("temperature", equalTo(0.8F))
                .body("minP", equalTo(0.05F))
                .body("topP", equalTo(0.95F))
                .body("topK", equalTo(40.0F))
                .body("frequencyPenalty", equalTo(0.3F))
                .body("presencePenalty", equalTo(0.1F))
                .body("reasoningEffort", equalTo("high"));
    }

    // ── AGENT ──────────────────────────────────────────────────

    private String createPersonality(String name) {
        return given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"" + name + "\"}")
                .when().post("/api/llm/personalities")
                .then().statusCode(200)
                .extract().path("id");
    }

    private String createModel(String niceName, String providerId) {
        return given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"" + niceName + "\",\"providerName\":\"pn\",\"providerId\":\"" + providerId + "\"}")
                .when().post("/api/llm/models")
                .then().statusCode(200)
                .extract().path("id");
    }

    @Test
    void agentCreateAndGet() {
        String provId = createProvider("prov1");
        String modelId = createModel("m1", provId);
        String persId = createPersonality("p1");

        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"my-agent\",\"providerId\":\"" + provId + "\",\"modelId\":\"" + modelId + "\",\"personalityId\":\"" + persId + "\",\"toolPreset\":\"basics\"}")
                .when().post("/api/llm/agents")
                .then().statusCode(200)
                .body("niceName", equalTo("my-agent"))
                .body("providerId", equalTo(provId))
                .body("modelId", equalTo(modelId))
                .body("personalityId", equalTo(persId))
                .body("toolPreset", equalTo("basics"))
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .when().get("/api/llm/agents/" + id)
                .then().statusCode(200)
                .body("niceName", equalTo("my-agent"));
    }

    @Test
    void agentCreateDuplicateNiceName() {
        String provId = createProvider("prov1");
        String modelId = createModel("m1", provId);
        String persId = createPersonality("p1");

        String body = "{\"niceName\":\"dup\",\"providerId\":\"" + provId + "\",\"modelId\":\"" + modelId + "\",\"personalityId\":\"" + persId + "\"}";

        given().header("Authorization", AUTH).contentType(ContentType.JSON).body(body)
                .when().post("/api/llm/agents").then().statusCode(200);

        given().header("Authorization", AUTH).contentType(ContentType.JSON).body(body)
                .when().post("/api/llm/agents")
                .then().statusCode(409)
                .body("error", equalTo("agent niceName already exists"));
    }

    @Test
    void agentCreateMissingFields() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"providerId\":\"x\",\"modelId\":\"x\",\"personalityId\":\"x\"}")
                .when().post("/api/llm/agents")
                .then().statusCode(400)
                .body("error", equalTo("niceName is required"));

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"x\",\"modelId\":\"x\",\"personalityId\":\"x\"}")
                .when().post("/api/llm/agents")
                .then().statusCode(400)
                .body("error", equalTo("providerId is required"));

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"x\",\"providerId\":\"x\",\"personalityId\":\"x\"}")
                .when().post("/api/llm/agents")
                .then().statusCode(400)
                .body("error", equalTo("modelId is required"));

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"x\",\"providerId\":\"x\",\"modelId\":\"x\"}")
                .when().post("/api/llm/agents")
                .then().statusCode(400)
                .body("error", equalTo("personalityId is required"));
    }

    @Test
    void agentCreateNonexistentFks() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"a\",\"providerId\":\"nonexistent\",\"modelId\":\"nonexistent\",\"personalityId\":\"nonexistent\"}")
                .when().post("/api/llm/agents")
                .then().statusCode(400)
                .body("error", equalTo("provider not found"));
    }

    @Test
    void agentCreateNonexistentModel() {
        String provId = createProvider("prov1");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"a\",\"providerId\":\"" + provId + "\",\"modelId\":\"nonexistent\",\"personalityId\":\"nonexistent\"}")
                .when().post("/api/llm/agents")
                .then().statusCode(400)
                .body("error", equalTo("model not found"));
    }

    @Test
    void agentCreateNonexistentPersonality() {
        String provId = createProvider("prov1");
        String modelId = createModel("m1", provId);

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"a\",\"providerId\":\"" + provId + "\",\"modelId\":\"" + modelId + "\",\"personalityId\":\"nonexistent\"}")
                .when().post("/api/llm/agents")
                .then().statusCode(400)
                .body("error", equalTo("personality not found"));
    }

    @Test
    void agentPatch() {
        String provId = createProvider("prov1");
        String provId2 = createProvider("prov2");
        String modelId = createModel("m1", provId);
        String modelId2 = createModel("m2", provId2);
        String persId = createPersonality("p1");
        String persId2 = createPersonality("p2");

        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"orig\",\"providerId\":\"" + provId + "\",\"modelId\":\"" + modelId + "\",\"personalityId\":\"" + persId + "\"}")
                .when().post("/api/llm/agents")
                .then().statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"renamed\",\"providerId\":\"" + provId2 + "\",\"modelId\":\"" + modelId2 + "\",\"personalityId\":\"" + persId2 + "\",\"toolPreset\":\"fs\"}")
                .when().patch("/api/llm/agents/" + id)
                .then().statusCode(200)
                .body("niceName", equalTo("renamed"))
                .body("providerId", equalTo(provId2))
                .body("modelId", equalTo(modelId2))
                .body("personalityId", equalTo(persId2))
                .body("toolPreset", equalTo("fs"));
    }

    @Test
    void agentDelete() {
        String provId = createProvider("prov1");
        String modelId = createModel("m1", provId);
        String persId = createPersonality("p1");

        String id = given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"del\",\"providerId\":\"" + provId + "\",\"modelId\":\"" + modelId + "\",\"personalityId\":\"" + persId + "\"}")
                .when().post("/api/llm/agents")
                .then().statusCode(200)
                .extract().path("id");

        given().header("Authorization", AUTH).when().delete("/api/llm/agents/" + id).then().statusCode(204);
        given().header("Authorization", AUTH).when().get("/api/llm/agents/" + id).then().statusCode(404);
    }

    @Test
    void agentGetNotFound() {
        given()
                .header("Authorization", AUTH)
                .when().get("/api/llm/agents/nonexistent")
                .then().statusCode(404)
                .body("error", equalTo("agent not found"));
    }

    @Test
    void agentToolPresetOptional() {
        String provId = createProvider("prov1");
        String modelId = createModel("m1", provId);
        String persId = createPersonality("p1");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"no-preset\",\"providerId\":\"" + provId + "\",\"modelId\":\"" + modelId + "\",\"personalityId\":\"" + persId + "\"}")
                .when().post("/api/llm/agents")
                .then().statusCode(200)
                .body("toolPreset", nullValue());
    }

    // ── EXECUTE ENDPOINT ───────────────────────────────────────

    @Test
    void executeMissingAgentName() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"requestPrompt\":\"Hello\"}")
                .when().post("/api/llm/execute")
                .then().statusCode(400)
                .body("error", equalTo("agentName is required"));
    }

    @Test
    void executeMissingRequestPrompt() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"agentName\":\"test\"}")
                .when().post("/api/llm/execute")
                .then().statusCode(400)
                .body("error", equalTo("requestPrompt is required"));
    }

    @Test
    void executeNonexistentAgent() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"agentName\":\"nonexistent\",\"requestPrompt\":\"Hello\"}")
                .when().post("/api/llm/execute")
                .then().statusCode(200)
                .body("agent", equalTo("nonexistent"))
                .body("status", equalTo("error"))
                .body("payload", containsString("Agent not found"));
    }

    @Test
    void executeAgentWithMissingProvider() {
        String persId = createPersonality("p1");
        String provId = createProvider("prov1");
        String modelId = createModel("m1", provId);

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"broken\",\"providerId\":\"" + provId + "\",\"modelId\":\"" + modelId + "\",\"personalityId\":\"" + persId + "\"}")
                .when().post("/api/llm/agents")
                .then().statusCode(200);

        given().header("Authorization", AUTH).contentType(ContentType.JSON)
                .body("{\"id\":\"" + provId + "\"}")
                .when().post("/api/llm/providers/delete");

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"agentName\":\"broken\",\"requestPrompt\":\"Hello\"}")
                .when().post("/api/llm/execute")
                .then().statusCode(200)
                .body("status", equalTo("error"))
                .body("payload", containsString("Provider not found"));
    }

    @Test
    void executeAgentWithMissingModel() {
        String persId = createPersonality("p1");
        String provId = createProvider("prov1");
        String modelId = createModel("m1", provId);

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"broken2\",\"providerId\":\"" + provId + "\",\"modelId\":\"" + modelId + "\",\"personalityId\":\"" + persId + "\"}")
                .when().post("/api/llm/agents");

        given().header("Authorization", AUTH).when().delete("/api/llm/models/" + modelId);

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"agentName\":\"broken2\",\"requestPrompt\":\"Hello\"}")
                .when().post("/api/llm/execute")
                .then().statusCode(200)
                .body("status", equalTo("error"))
                .body("payload", containsString("Model not found"));
    }

    @Test
    void executeAgentWithMissingPersonality() {
        String persId = createPersonality("p1");
        String provId = createProvider("prov1");
        String modelId = createModel("m1", provId);

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"niceName\":\"broken3\",\"providerId\":\"" + provId + "\",\"modelId\":\"" + modelId + "\",\"personalityId\":\"" + persId + "\"}")
                .when().post("/api/llm/agents");

        given().header("Authorization", AUTH).when().delete("/api/llm/personalities/" + persId);

        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{\"agentName\":\"broken3\",\"requestPrompt\":\"Hello\"}")
                .when().post("/api/llm/execute")
                .then().statusCode(200)
                .body("status", equalTo("error"))
                .body("payload", containsString("Personality not found"));
    }

    @Test
    void executeEmptyBody() {
        given()
                .header("Authorization", AUTH)
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/api/llm/execute")
                .then().statusCode(400)
                .body("error", equalTo("agentName is required"));
    }
}
