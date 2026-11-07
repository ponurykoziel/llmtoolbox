package com.sheahorn.llmtoolbox.buildtools;

import com.sheahorn.llmtoolbox.buildtools.cargo.CargoLockService;
import com.sheahorn.llmtoolbox.buildtools.docker.DockerLockService;
import com.sheahorn.llmtoolbox.buildtools.git.GitLockService;
import com.sheahorn.llmtoolbox.buildtools.gradle.GradleLockService;
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
    GradleLockService gradleLock;

    @Inject
    GitLockService gitLock;

    @Inject
    DockerLockService dockerLock;

    @Inject
    CargoLockService cargoLock;

    @Inject
    ToolCallInFlightService inFlightService;

    @Inject
    ToolCallHistoryService history;

    @Operation(
            operationId = "build_locks_status",
            summary = "Returns the current lock state for mvn, gradle, git, docker, and cargo, plus in-flight calls and recent history"
    )
    @GET
    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        // One switch, one mode for every family — reported once, not per lock.
        result.put("enabled", mvnLock.isEnabled());
        result.put("mode", mvnLock.getMode());
        result.put("locks", locks());
        result.put("inFlight", inFlightService.getAll().values());
        result.put("history", history.recent(20));
        return result;
    }

    private Map<String, LockInfo> locks() {
        Map<String, LockInfo> result = new LinkedHashMap<>();
        result.put("mvn", new LockInfo(mvnLock));
        result.put("gradle", new LockInfo(gradleLock));
        result.put("git", new LockInfo(gitLock));
        result.put("docker", new LockInfo(dockerLock));
        result.put("cargo", new LockInfo(cargoLock));
        return result;
    }

    public static class LockInfo {
        public String family;
        public boolean locked;
        public Instant since;
        public String operation;
        /** True while a worker is really inside the call — a locked flag without this is stale. */
        public boolean heldByWorker;

        public LockInfo() {
        }

        public LockInfo(AbstractFamilyLockService lock) {
            this.family = lock.getFamily();
            this.locked = lock.isLocked();
            this.since = lock.getSince();
            this.operation = lock.getOperation();
            this.heldByWorker = lock.isHeldByWorker();
        }
    }
}
