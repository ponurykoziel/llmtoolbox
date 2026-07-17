package com.sheahorn.llmtoolbox.buildtools.git;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitRequestDtoTest {

    @Test
    void testDefaultConstructor() {
        GitRequestDto dto = new GitRequestDto();
        assertNull(dto.path);
        assertNull(dto.remote);
        assertNull(dto.branch);
        assertNull(dto.remoteBranch);
        assertNull(dto.message);
        assertNull(dto.files);
        assertNull(dto.url);
        assertNull(dto.commit);
        assertNull(dto.n);
        assertNull(dto.confirm);
    }

    @Test
    void testFieldAssignment() {
        GitRequestDto dto = new GitRequestDto();
        dto.path = "/tmp/repo";
        dto.remote = "origin";
        dto.branch = "main";
        dto.remoteBranch = "feature-x";
        dto.message = "commit msg";
        dto.files = "README.md";
        dto.url = "https://github.com/foo/bar.git";
        dto.commit = "abc1234";
        dto.n = 10;
        dto.confirm = true;

        assertEquals("/tmp/repo", dto.path);
        assertEquals("origin", dto.remote);
        assertEquals("main", dto.branch);
        assertEquals("feature-x", dto.remoteBranch);
        assertEquals("commit msg", dto.message);
        assertEquals("README.md", dto.files);
        assertEquals("https://github.com/foo/bar.git", dto.url);
        assertEquals("abc1234", dto.commit);
        assertEquals(10, dto.n);
        assertTrue(dto.confirm);
    }
}
