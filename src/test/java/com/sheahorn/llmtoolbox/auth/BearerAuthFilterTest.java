package com.sheahorn.llmtoolbox.auth;

import com.sheahorn.llmtoolbox.domain.ApiKey;
import com.sheahorn.llmtoolbox.domain.User;
import com.sheahorn.llmtoolbox.service.ApiKeyService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestProfile(BearerAuthFilterTest.Profile.class)
class BearerAuthFilterTest {

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

    @Inject
    ApiKeyService apiKeyService;

    private String userApiKey;

    @BeforeEach
    void setUp() {
        QuarkusTransaction.run(() -> {
            User user = User.create("apiuser", BcryptUtil.bcryptHash("password"), "user");
            user.persist();
            ApiKeyService.CreateResult result = apiKeyService.create(user.id, "Test Key");
            userApiKey = result.rawKey;
        });
    }

    @AfterEach
    void tearDown() {
        QuarkusTransaction.run(() -> {
            ApiKey.findAll().stream().forEach(ak -> ak.delete());
            User.findAll().stream()
                .map(u -> (User) u)
                .filter(u -> !"admin".equals(u.username))
                .forEach(u -> u.delete());
        });
    }

    // ── Public paths — no auth required ───────────────────────

    @Test
    void testPublicPathLoginPage() {
        given()
            .when().get("/login.html")
            .then()
            .statusCode(200);
    }

    @Test
    void testPublicPathLoginFailed() {
        given()
            .when().get("/login-failed.html")
            .then()
            .statusCode(200);
    }

    // ── NoBearerAuth endpoints skip auth ────────────────────────

    @Test
    void testNoBearerAuthEndpointBuiltinFunctions() {
        given()
            .when().get("/api/functions/builtin")
            .then()
            .statusCode(200);
    }

    @Test
    void testNoBearerAuthEndpointOpenApiPreset() {
        given()
            .when().get("/api/openapi/preset/all")
            .then()
            .statusCode(200);
    }

    // ── Static config Bearer token ──────────────────────────────

    @Test
    void testApiPathWithValidStaticToken() {
        given()
            .header("Authorization", "Bearer test-bearer-token")
            .when().get("/api/tools/basics/presets/all")
            .then()
            .statusCode(200);
    }

    // ── User-created API key ────────────────────────────────────

    @Test
    void testApiPathWithUserCreatedApiKey() {
        given()
            .header("Authorization", "Bearer " + userApiKey)
            .when().get("/api/tools/basics/presets/all")
            .then()
            .statusCode(200);
    }

    // ── API paths without auth → 401 ───────────────────────────

    @Test
    void testApiPathWithoutAuthHeaderReturns401() {
        given()
            .when().get("/api/tools/basics/presets/all")
            .then()
            .statusCode(401);
    }

    @Test
    void testApiPathWithInvalidTokenReturns401() {
        given()
            .header("Authorization", "Bearer garbage-token")
            .when().get("/api/tools/basics/presets/all")
            .then()
            .statusCode(401);
    }

    @Test
    void testApiPathWithEmptyTokenReturns401() {
        given()
            .header("Authorization", "Bearer ")
            .when().get("/api/tools/basics/presets/all")
            .then()
            .statusCode(401);
    }

    // ── UI paths without auth → 401 (not public) ───────────────

    @Test
    void testUiPathWithoutAuthReturns401() {
        given()
            .when().get("/ui/presets")
            .then()
            .statusCode(401);
    }

    @Test
    void testUiPathAccountWithoutAuthReturns401() {
        given()
            .when().get("/ui/account")
            .then()
            .statusCode(401);
    }

    @Test
    void testRootWithoutAuthReturns401() {
        given()
            .when().get("/")
            .then()
            .statusCode(401);
    }

    // ── Endpoints needing SecurityContext work with Bearer token ──

    @Test
    void testApiPathUsersMeWithValidToken() {
        given()
            .header("Authorization", "Bearer " + userApiKey)
            .when().get("/api/users/me")
            .then()
            .statusCode(200);
    }

    @Test
    void testApiPathApikeysWithValidToken() {
        given()
            .header("Authorization", "Bearer " + userApiKey)
            .when().get("/api/apikeys")
            .then()
            .statusCode(200);
    }

    // ── Endpoints needing SecurityContext return 401 without auth ──

    @Test
    void testApiPathUsersMeWithoutAuth() {
        given()
            .when().get("/api/users/me")
            .then()
            .statusCode(401);
    }

    @Test
    void testApiPathApikeysWithoutAuth() {
        given()
            .when().get("/api/apikeys")
            .then()
            .statusCode(401);
    }
}
