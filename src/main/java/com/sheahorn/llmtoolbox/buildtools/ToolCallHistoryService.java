package com.sheahorn.llmtoolbox.buildtools;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@ApplicationScoped
public class ToolCallHistoryService {

    private final ConcurrentLinkedDeque<Entry> history = new ConcurrentLinkedDeque<>();
    private static final int MAX = 200;

    public void record(String tool, String operationId, long durationMs, boolean success, String error) {
        Entry e = new Entry();
        e.tool = tool;
        e.operationId = operationId;
        e.startedAt = Instant.now().minusMillis(durationMs);
        e.durationMs = durationMs;
        e.success = success;
        e.error = error;

        history.addFirst(e);
        while (history.size() > MAX) {
            history.pollLast();
        }
    }

    public List<Entry> recent(int limit) {
        List<Entry> list = new ArrayList<>(history);
        if (limit > 0 && limit < list.size()) {
            list = list.subList(0, limit);
        }
        return list;
    }

    public static class Entry {
        public String tool;
        public String operationId;
        public Instant startedAt;
        public long durationMs;
        public boolean success;
        public String error;
    }
}
