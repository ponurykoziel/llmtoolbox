package com.sheahorn.llmtoolbox.buildtools;

import com.sheahorn.llmtoolbox.buildtools.docker.DockerLockService;
import com.sheahorn.llmtoolbox.buildtools.git.GitLockService;
import com.sheahorn.llmtoolbox.buildtools.mvn.MvnLockService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/api/tools/build/locks")
@Produces(MediaType.APPLICATION_JSON)
public class LockStatusResource {

    @Inject
    MvnLockService mvnLock;

    @Inject
    GitLockService gitLock;

    @Inject
    DockerLockService dockerLock;

    @Inject
    ToolCallInFlightService inFlightService;

    @Inject
    ToolCallHistoryService history;

    @Operation(
            operationId = "build_locks_status",
            summary = "Returns the current lock state for mvn, git, and docker, plus in-flight calls and recent history"
    )
    @GET
    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mode", mvnLock.getMode());
        result.put("locks", locks());
        result.put("inFlight", inFlightService.getAll().values());
        result.put("history", history.recent(20));
        return result;
    }

    private Map<String, LockInfo> locks() {
        Map<String, LockInfo> result = new LinkedHashMap<>();
        result.put("mvn", toInfo(mvnLock));
        result.put("git", toInfo(gitLock));
        result.put("docker", toInfo(dockerLock));
        return result;
    }

    private LockInfo toInfo(Object lock) {
        try {
            boolean locked = (boolean) lock.getClass().getMethod("isLocked").invoke(lock);
            Instant since = (Instant) lock.getClass().getMethod("getSince").invoke(lock);
            String op = (String) lock.getClass().getMethod("getOperation").invoke(lock);
            return new LockInfo(locked, since, op);
        } catch (Exception e) {
            return new LockInfo(false, null, null);
        }
    }

    public static class LockInfo {
        public boolean locked;
        public Instant since;
        public String operation;

        public LockInfo() {}
        public LockInfo(boolean locked, Instant since, String operation) {
            this.locked = locked;
            this.since = since;
            this.operation = operation;
        }
    }
}
