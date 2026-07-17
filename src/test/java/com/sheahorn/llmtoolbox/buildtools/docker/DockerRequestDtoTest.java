package com.sheahorn.llmtoolbox.buildtools.docker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DockerRequestDtoTest {

    @Test
    void testDefaultConstructor() {
        DockerRequestDto dto = new DockerRequestDto();
        assertNull(dto.container);
        assertNull(dto.image);
        assertNull(dto.name);
        assertNull(dto.port);
        assertNull(dto.env);
        assertNull(dto.volume);
        assertNull(dto.command);
        assertNull(dto.composeFile);
        assertNull(dto.composeService);
        assertNull(dto.network);
        assertNull(dto.hostname);
        assertNull(dto.restart);
        assertNull(dto.source);
        assertNull(dto.target);
        assertNull(dto.tail);
        assertNull(dto.filter);
        assertNull(dto.force);
        assertNull(dto.rm);
    }

    @Test
    void testFieldAssignment() {
        DockerRequestDto dto = new DockerRequestDto();
        dto.container = "my-container";
        dto.image = "nginx:latest";
        dto.name = "web";
        dto.port = "8080:80";
        dto.env = "FOO=bar";
        dto.volume = "/host:/container";
        dto.command = "echo hello";
        dto.composeFile = "docker-compose.yml";
        dto.composeService = "web";
        dto.network = "bridge";
        dto.hostname = "myhost";
        dto.restart = "unless-stopped";
        dto.source = "/tmp/src";
        dto.target = "/tmp/dst";
        dto.tail = "100";
        dto.filter = "status=running";
        dto.force = true;
        dto.rm = true;

        assertEquals("my-container", dto.container);
        assertEquals("nginx:latest", dto.image);
        assertEquals("web", dto.name);
        assertEquals("8080:80", dto.port);
        assertEquals("FOO=bar", dto.env);
        assertEquals("/host:/container", dto.volume);
        assertEquals("echo hello", dto.command);
        assertEquals("docker-compose.yml", dto.composeFile);
        assertEquals("web", dto.composeService);
        assertEquals("bridge", dto.network);
        assertEquals("myhost", dto.hostname);
        assertEquals("unless-stopped", dto.restart);
        assertEquals("/tmp/src", dto.source);
        assertEquals("/tmp/dst", dto.target);
        assertEquals("100", dto.tail);
        assertEquals("status=running", dto.filter);
        assertTrue(dto.force);
        assertTrue(dto.rm);
    }
}
