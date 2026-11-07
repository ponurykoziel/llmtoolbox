package com.sheahorn.llmtoolbox.buildtools;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Status surface for the build locks, plus the dashboard-only clear endpoint.
 * /ui/locks/clear must NOT be a tool — no OpenAPI operationId, no ToolBean.
 */
@QuarkusTest
class LockStatusResourceTest {

    private static final String AUTH = "Bearer test-bearer-token";

    @Test
    void statusReportsEnabledModeAndEveryFamily() {
        given()
                .header("Authorization", AUTH)
                .when().get("/api/tools/build/locks")
                .then().statusCode(200)
                .body("enabled", is(true))
                .body("mode", is("synchronize"))
                .body("locks.mvn.locked", is(false))
                .body("locks.gradle.locked", is(false))
                .body("locks.git.locked", is(false))
                .body("locks.docker.locked", is(false))
                .body("locks.cargo.locked", is(false))
                .body("locks.mvn.family", is("mvn"));
    }

    @Test
    void clearAllOnAFreeSystemIsANoOp() {
        given()
                .header("Authorization", AUTH)
                .contentType("application/json")
                .body("{}")
                .when().post("/ui/locks/clear")
                .then().statusCode(200)
                .body("enabled", is(true))
                .body("cleared", hasSize(0));
    }

    @Test
    void clearSingleKnownFamilyIsAccepted() {
        given()
                .header("Authorization", AUTH)
                .contentType("application/json")
                .body("{\"family\":\"mvn\"}")
                .when().post("/ui/locks/clear")
                .then().statusCode(200);
    }

    @Test
    void clearUnknownFamilyIs404() {
        given()
                .header("Authorization", AUTH)
                .contentType("application/json")
                .body("{\"family\":\"nope\"}")
                .when().post("/ui/locks/clear")
                .then().statusCode(404);
    }

    @Test
    void clearRequiresAuthentication() {
        given()
                .contentType("application/json")
                .body("{}")
                .when().post("/ui/locks/clear")
                .then().statusCode(401);
    }
}
