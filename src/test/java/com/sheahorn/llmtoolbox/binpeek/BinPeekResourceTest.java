package com.sheahorn.llmtoolbox.binpeek;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class BinPeekResourceTest {

    @Inject
    BinPeekResource resource;

    private BinPeekRequestDto req(String path) {
        BinPeekRequestDto dto = new BinPeekRequestDto();
        dto.path = path;
        return dto;
    }

    @Test
    void testNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> resource.file(null));
    }

    @Test
    void testBlankPath() {
        assertThrows(IllegalArgumentException.class, () -> resource.file(req("")));
    }

    @Test
    void testPathOutsideAllowedRoot() {
        assertThrows(IllegalArgumentException.class, () -> resource.file(req("/etc/passwd")));
    }

    @Test
    void testPathTraversal() {
        assertThrows(IllegalArgumentException.class, () -> resource.file(req("/workzone/angelica/../etc/passwd")));
    }
}
