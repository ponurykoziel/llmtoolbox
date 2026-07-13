package com.sheahorn.llmtoolbox.basics.memory;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "memory_entries")
public class MemoryEntry extends io.quarkus.hibernate.orm.panache.PanacheEntity {

    @Column(nullable = false)
    public Instant timestamp;

    @Column(nullable = false, length = 65536)
    public String content;

    public static MemoryEntry create(String content) {
        MemoryEntry entry = new MemoryEntry();
        entry.timestamp = Instant.now();
        entry.content = content;
        return entry;
    }
}
