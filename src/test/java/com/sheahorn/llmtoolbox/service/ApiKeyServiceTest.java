package com.sheahorn.llmtoolbox.service;

import com.sheahorn.llmtoolbox.domain.ApiKey;
import com.sheahorn.llmtoolbox.service.ApiKeyService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ApiKeyServiceTest {

    @Inject
    ApiKeyService apiKeyService;

    @BeforeEach
    @Transactional
    void cleanUp() {
        ApiKey.findAll().stream().forEach(ak -> ak.delete());
    }

    @Test
    @Transactional
    void testCreateApiKey() {
        ApiKeyService.CreateResult result = apiKeyService.create("USER001", "My API Key");
        assertNotNull(result);
        assertNotNull(result.apiKey);
        assertNotNull(result.rawKey);
        assertEquals("USER001", result.apiKey.userId);
        assertEquals("My API Key", result.apiKey.name);
        assertTrue(result.rawKey.startsWith("atk-"));
    }

    @Test
    @Transactional
    void testCreateWithEmptyName() {
        // Service passes name through as-is; defaulting happens in the resource layer
        ApiKeyService.CreateResult result = apiKeyService.create("USER001", "");
        assertEquals("", result.apiKey.name);
    }

    @Test
    @Transactional
    void testCreateWithBlankName() {
        ApiKeyService.CreateResult result = apiKeyService.create("USER001", "   ");
        assertEquals("   ", result.apiKey.name);
    }

    @Test
    @Transactional
    void testFindByUserId() {
        apiKeyService.create("USER001", "Key1");
        apiKeyService.create("USER001", "Key2");
        apiKeyService.create("USER002", "Key3");

        List<ApiKey> user1Keys = apiKeyService.findByUserId("USER001");
        assertEquals(2, user1Keys.size());

        List<ApiKey> user2Keys = apiKeyService.findByUserId("USER002");
        assertEquals(1, user2Keys.size());
    }

    @Test
    @Transactional
    void testDeleteById() {
        ApiKeyService.CreateResult result = apiKeyService.create("USER001", "To Delete");
        assertTrue(apiKeyService.deleteById(result.apiKey.id, "USER001"));
        assertNull(ApiKey.findById(result.apiKey.id));
    }

    @Test
    @Transactional
    void testDeleteByIdWrongUser() {
        ApiKeyService.CreateResult result = apiKeyService.create("USER001", "My Key");
        assertFalse(apiKeyService.deleteById(result.apiKey.id, "USER002"));
    }

    @Test
    @Transactional
    void testDeleteByIdNotFound() {
        assertFalse(apiKeyService.deleteById("NONEXISTENT", "USER001"));
    }

    @Test
    @Transactional
    void testHashKey() {
        String hash1 = apiKeyService.hashKey("test-key-1");
        String hash2 = apiKeyService.hashKey("test-key-1");
        String hash3 = apiKeyService.hashKey("test-key-2");

        assertNotNull(hash1);
        assertEquals(64, hash1.length()); // SHA-256 hex = 64 chars
        assertEquals(hash1, hash2); // Same input = same hash
        assertNotEquals(hash1, hash3); // Different input = different hash
    }

    @Test
    @Transactional
    void testFindByKeyHash() {
        ApiKeyService.CreateResult result = apiKeyService.create("USER001", "Test");
        String keyHash = apiKeyService.hashKey(result.rawKey);

        ApiKey found = apiKeyService.findByKeyHash(keyHash);
        assertNotNull(found);
        assertEquals(result.apiKey.id, found.id);
    }

    @Test
    @Transactional
    void testFindByKeyHashNotFound() {
        ApiKey found = apiKeyService.findByKeyHash("nonexistent-hash");
        assertNull(found);
    }

    @Test
    @Transactional
    void testRawKeyIsDifferentEachTime() {
        ApiKeyService.CreateResult r1 = apiKeyService.create("USER001", "Key1");
        ApiKeyService.CreateResult r2 = apiKeyService.create("USER001", "Key2");

        assertNotEquals(r1.rawKey, r2.rawKey);
        assertNotEquals(r1.apiKey.keyHash, r2.apiKey.keyHash);
    }

    @Test
    @Transactional
    void testKeyHashDoesNotContainRawKey() {
        ApiKeyService.CreateResult result = apiKeyService.create("USER001", "Test");
        assertFalse(result.apiKey.keyHash.contains(result.rawKey));
    }
}
