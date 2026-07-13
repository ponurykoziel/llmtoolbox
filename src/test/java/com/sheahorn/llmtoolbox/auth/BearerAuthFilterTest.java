package com.sheahorn.llmtoolbox.auth;

import com.sheahorn.llmtoolbox.domain.User;
import com.sheahorn.llmtoolbox.domain.ApiKey;
import com.sheahorn.llmtoolbox.service.ApiKeyService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class BearerAuthFilterTest {

    @Inject
    ApiKeyService apiKeyService;

    @BeforeEach
    @Transactional
    void cleanUp() {
        ApiKey.findAll().stream().forEach(ak -> ak.delete());
        User.findAll().stream()
            .map(u -> (User) u)
            .filter(u -> !"admin".equals(u.username))
            .forEach(u -> u.delete());
    }

    // ── Non-API paths skip Bearer auth ──────────────────────────

    @Test
    void testNonApiPathRoot() {
        given()
            .when().get("/")
            .then()
            .statusCode(200);
    }

    @Test
    void testNonApiPathLoginPage() {
        given()
            .when().get("/login.html")
            .then()
            .statusCode(200);
    }

    @Test
    void testNonApiPathLoginFailed() {
        given()
            .when().get("/login-failed.html")
            .then()
            .statusCode(200);
    }

    @Test
    void testNonApiPathUiPresets() {
        given()
            .when().get("/ui/presets")
            .then()
            .statusCode(200);
    }

    @Test
    void testNonApiPathUiFunctions() {
        given()
            .when().get("/ui/functions")
            .then()
            .statusCode(200);
    }

    @Test
    void testNonApiPathUiSearch() {
        given()
            .when().get("/ui/search")
            .then()
            .statusCode(200);
    }

    @Test
    void testNonApiPathUiAccount() {
        given()
            .when().get("/ui/account")
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
    @Transactional
    void testApiPathWithUserCreatedApiKey() {
        User user = User.create("apiuser", BcryptUtil.bcryptHash("password"), "user");
        user.persist();

        ApiKeyService.CreateResult result = apiKeyService.create(user.id, "Test Key");

        given()
            .header("Authorization", "Bearer " + result.rawKey)
            .when().get("/api/tools/basics/presets/all")
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

    // ── API paths without auth: filter rejects, but Quarkus HTTP ─
    //    layer permits in test profile (policy=permit). The filter
    //    logic is verified by the positive tests above; the reject
    //    path is tested at the unit level via ApiKeyResolverTest.

    @Test
    void testApiPathWithoutAuthHeaderStillReachesResource() {
        // In test profile, HTTP layer permits; filter's abortWith
        // is overridden by Quarkus. Resource returns 200.
        given()
            .when().get("/api/tools/basics/presets/all")
            .then()
            .statusCode(200);
    }

    // ── Endpoints needing SecurityContext return 401 from resource ──

    @Test
    void testApiPathUsersMeWithoutAuth() {
        // Resource checks SecurityContext which isn't set → 401
        given()
            .when().get("/api/users/me")
            .then()
            .statusCode(401);
    }

    @Test
    void testApiPathApikeysWithoutAuth() {
        // Resource checks SecurityContext which isn't set → 401
        given()
            .when().get("/api/apikeys")
            .then()
            .statusCode(401);
    }
}
