package com.sheahorn.llmtoolbox.basics.clipboard;

import java.util.concurrent.atomic.AtomicReference;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClipboardCache {
    private final AtomicReference<String> content = new AtomicReference<>("");

    public String read() {
        return content.get();
    }

    public void write(String value) {
        content.set(value == null ? "" : value);
    }

    public String append(String value) {
        if (value == null || value.isEmpty()) {
            return content.get();
        }
        return content.updateAndGet(current -> current + value);
    }
}
