package com.sheahorn.llmtoolbox.basics.memory;

import java.time.Instant;

public class MemoryEntryResponse {
    public Long id;
    public Instant timestamp;
    public String content;

    public static MemoryEntryResponse from(MemoryEntry entry) {
        MemoryEntryResponse r = new MemoryEntryResponse();
        r.id = entry.id;
        r.timestamp = entry.timestamp;
        r.content = entry.content;
        return r;
    }
}
