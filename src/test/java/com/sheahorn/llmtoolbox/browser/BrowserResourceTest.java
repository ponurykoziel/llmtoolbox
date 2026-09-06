package com.sheahorn.llmtoolbox.browser;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(BrowserResourceTest.Profile.class)
@QuarkusTestResource(BrowserWireMockTestResource.class)
class BrowserResourceTest {

    public static class Profile implements io.quarkus.test.junit.QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                "llmtoolbox.auth.token", "test-bearer-token",
                "llmtoolbox.auth.seed-on-startup", "false",
                "quarkus.hibernate-orm.database.generation", "drop-and-create",
                "quarkus.datasource.jdbc.url", "jdbc:h2:mem:llmtoolbox-browser;DB_CLOSE_DELAY=-1"
            );
        }
    }

    @ConfigProperty(name = "llmtoolbox.browser.port")
    int browserPort;

    @InjectMock
    BrowserSidecar sidecar;

    @Inject
    BrowserProfilesResource profilesResource;

    @Inject
    BrowserSessionsResource sessionsResource;

    @Inject
    BrowserInteractResource interactResource;

    @BeforeEach
    void setUp() {
        WireMock.configureFor("localhost", browserPort);
        WireMock.reset();

        // Mock the sidecar as ready (real sidecar won't start in test)
        when(sidecar.isReady()).thenReturn(true);
    }

    // ── Profiles ──────────────────────────────────────────────

    @Test
    void testListProfiles() throws Exception {
        stubFor(get(urlEqualTo("/profiles"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"default\"},{\"name\":\"work\"}]")));

        String result = profilesResource.list();
        assertTrue(result.contains("default"));
        assertTrue(result.contains("work"));
    }

    @Test
    void testCreateProfile() throws Exception {
        stubFor(post(urlEqualTo("/profiles"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"name\":\"new-profile\",\"path\":\"/tmp/profiles/new-profile\"}")));

        var req = new BrowserProfilesResource.ProfileCreateRequest();
        req.name = "new-profile";
        String result = profilesResource.create(req);
        assertTrue(result.contains("new-profile"));
    }

    @Test
    void testCreateProfileMissingName() {
        var req = new BrowserProfilesResource.ProfileCreateRequest();
        assertThrows(IllegalArgumentException.class, () -> profilesResource.create(req));
    }

    @Test
    void testDeleteProfile() throws Exception {
        stubFor(delete(urlEqualTo("/profiles/myprofile"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"deleted\":\"myprofile\"}")));

        String result = profilesResource.delete("myprofile");
        assertTrue(result.contains("myprofile"));
    }

    @Test
    void testDeleteProfileMissingName() {
        assertThrows(IllegalArgumentException.class, () -> profilesResource.delete(""));
    }

    // ── Sessions ──────────────────────────────────────────────

    @Test
    void testListSessions() throws Exception {
        stubFor(get(urlEqualTo("/sessions"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"session_id\":\"abc123\",\"profile\":null}]")));

        String result = sessionsResource.list();
        assertTrue(result.contains("abc123"));
    }

    @Test
    void testOpenSessionAnonymous() throws Exception {
        stubFor(post(urlEqualTo("/sessions"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"session_id\":\"def456\",\"profile\":null}")));

        String result = sessionsResource.open(null);
        assertTrue(result.contains("def456"));
    }

    @Test
    void testOpenSessionWithProfile() throws Exception {
        stubFor(post(urlEqualTo("/sessions"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"session_id\":\"ghi789\",\"profile\":\"default\"}")));

        var req = new BrowserSessionsResource.SessionOpenRequest();
        req.profile = "default";
        String result = sessionsResource.open(req);
        assertTrue(result.contains("ghi789"));
        assertTrue(result.contains("default"));
    }

    @Test
    void testCloseSession() throws Exception {
        stubFor(delete(urlEqualTo("/sessions/abc123"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"closed\":\"abc123\"}")));

        String result = sessionsResource.close("abc123");
        assertTrue(result.contains("abc123"));
    }

    @Test
    void testCloseSessionMissingId() {
        assertThrows(IllegalArgumentException.class, () -> sessionsResource.close(""));
    }

    // ── Interact: navigate ────────────────────────────────────

    @Test
    void testNavigateTo() throws Exception {
        stubFor(post(urlEqualTo("/sessions/s1/goto"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"url\":\"https://example.com\",\"title\":\"Example\"}")));

        var req = new BrowserInteractResource.GotoRequest();
        req.url = "https://example.com";
        String result = interactResource.navigateTo("s1", req);
        assertTrue(result.contains("Example"));
    }

    @Test
    void testNavigateToMissingUrl() {
        var req = new BrowserInteractResource.GotoRequest();
        assertThrows(IllegalArgumentException.class, () -> interactResource.navigateTo("s1", req));
    }

    // ── Interact: get HTML ────────────────────────────────────

    @Test
    void testGetHtml() throws Exception {
        stubFor(get(urlEqualTo("/sessions/s1/content"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"url\":\"https://example.com\",\"html\":\"<html><body>Hi</body></html>\"}")));

        String result = interactResource.getHtml("s1");
        assertTrue(result.contains("<html>"));
    }

    // ── Interact: get visible text ────────────────────────────

    @Test
    void testGetVisibleText() throws Exception {
        stubFor(get(urlEqualTo("/sessions/s1/text"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"url\":\"https://example.com\",\"text\":\"Hello World\"}")));

        String result = interactResource.getVisibleText("s1");
        assertTrue(result.contains("Hello World"));
    }

    // ── Interact: click ──────────────────────────────────────

    @Test
    void testClickElement() throws Exception {
        stubFor(post(urlEqualTo("/sessions/s1/click"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"clicked\":\"#btn\",\"url\":\"https://example.com\"}")));

        var req = new BrowserInteractResource.ClickRequest();
        req.selector = "#btn";
        String result = interactResource.clickElement("s1", req);
        assertTrue(result.contains("#btn"));
    }

    @Test
    void testClickElementMissingSelector() {
        var req = new BrowserInteractResource.ClickRequest();
        assertThrows(IllegalArgumentException.class, () -> interactResource.clickElement("s1", req));
    }

    // ── Interact: screenshot base64 ───────────────────────────

    @Test
    void testScreenshotBase64() throws Exception {
        stubFor(post(urlEqualTo("/sessions/s1/screenshot_base64"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"screenshot_b64\":\"iVBORw0KGgo=\"}")));

        String result = interactResource.captureScreenshotBase64("s1", null);
        assertTrue(result.contains("iVBORw0KGgo="));
    }

    // ── Interact: screenshot png ──────────────────────────────

    @Test
    void testScreenshotPng() throws Exception {
        stubFor(post(urlEqualTo("/sessions/s1/screenshot_png"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"ok\"}")));

        String result = interactResource.captureScreenshotPng("s1", null);
        assertTrue(result.contains("ok"));
    }

    // ── Interact: execute JS ──────────────────────────────────

    @Test
    void testExecuteJavascript() throws Exception {
        stubFor(post(urlEqualTo("/sessions/s1/execute"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":42}")));

        var req = new BrowserInteractResource.ExecuteRequest();
        req.script = "return 42";
        String result = interactResource.executeJavascript("s1", req);
        assertTrue(result.contains("42"));
    }

    @Test
    void testExecuteJavascriptMissingScript() {
        var req = new BrowserInteractResource.ExecuteRequest();
        assertThrows(IllegalArgumentException.class, () -> interactResource.executeJavascript("s1", req));
    }

    // ── Interact: type text ───────────────────────────────────

    @Test
    void testTypeText() throws Exception {
        stubFor(post(urlEqualTo("/sessions/s1/type"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"typed\":\"#input\",\"text\":\"hello\",\"method\":\"type\",\"url\":\"https://example.com\"}")));

        var req = new BrowserInteractResource.TypeRequest();
        req.selector = "#input";
        req.text = "hello";
        req.method = "type";
        String result = interactResource.typeText("s1", req);
        assertTrue(result.contains("hello"));
    }

    @Test
    void testTypeTextMissingSelector() {
        var req = new BrowserInteractResource.TypeRequest();
        req.text = "hello";
        assertThrows(IllegalArgumentException.class, () -> interactResource.typeText("s1", req));
    }

    @Test
    void testTypeTextMissingText() {
        var req = new BrowserInteractResource.TypeRequest();
        req.selector = "#input";
        assertThrows(IllegalArgumentException.class, () -> interactResource.typeText("s1", req));
    }

    // ── Interact: URL print ───────────────────────────────────

    @Test
    void testUrlPrint() throws Exception {
        stubFor(get(urlEqualTo("/sessions/s1/url"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"url\":\"https://example.com/page\"}")));

        String result = interactResource.urlPrint("s1");
        assertTrue(result.contains("https://example.com/page"));
    }

    // ── Error propagation ─────────────────────────────────────

    @Test
    void testSidecarErrorPropagation() throws Exception {
        stubFor(get(urlEqualTo("/profiles"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"detail\":\"Internal server error\"}")));

        assertThrows(RuntimeException.class, () -> profilesResource.list());
    }

    @Test
    void testSidecarNotReady() {
        when(sidecar.isReady()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> profilesResource.list());
    }
}
