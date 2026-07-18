package com.sheahorn.llmtoolbox.buildtools.mvn;

import com.sheahorn.llmtoolbox.buildtools.LogToolCall;
import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import com.sheahorn.llmtoolbox.fstools.info.FsResourceSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/build/mvn")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@LogToolCall
public class MvnResource extends FsResourceSupport {

    @Inject
    Executor executor;

    @Inject
    MvnLockService lock;

    @Operation(
            operationId = "build_mvn_clean",
            summary = "Runs `mvn -B clean` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/clean")
    public ExecutionResponse clean(MvnRequestDto request) throws Exception {
        return run(request, "mvn -B clean");
    }

    @Operation(
            operationId = "build_mvn_compile",
            summary = "Runs `mvn -B clean compile` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/compile")
    public ExecutionResponse compile(MvnRequestDto request) throws Exception {
        return run(request, "mvn -B clean compile");
    }

    @Operation(
            operationId = "build_mvn_test",
            summary = "Runs `mvn -B test` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/test")
    public ExecutionResponse test(MvnRequestDto request) throws Exception {
        return run(request, "mvn -B test");
    }

    @Operation(
            operationId = "build_mvn_test_one",
            summary = "Runs a single test class or pattern via `mvn -B test -Dtest=<value>`; may exceed the default 30s timeout"
    )
    @POST
    @Path("/test-one")
    public ExecutionResponse testOne(MvnRequestDto request) throws Exception {
        if (request == null || request.testClass == null || request.testClass.isBlank()) {
            throw new IllegalArgumentException("testClass is required");
        }

        return run(request, "mvn -B test -Dtest=" + ToolSupport.shellQuote(request.testClass));
    }

    @Operation(
            operationId = "build_mvn_verify",
            summary = "Runs `mvn -B verify` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/verify")
    public ExecutionResponse verify(MvnRequestDto request) throws Exception {
        return run(request, "mvn -B verify");
    }

    @Operation(
            operationId = "build_mvn_package",
            summary = "Runs `mvn -B clean package` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/package")
    public ExecutionResponse packageGoal(MvnRequestDto request) throws Exception {
        return run(request, "mvn -B clean package");
    }

    @Operation(
            operationId = "build_mvn_package_skip_tests",
            summary = "Runs `mvn -B clean package -DskipTests` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/package-skip-tests")
    public ExecutionResponse packageSkipTests(MvnRequestDto request) throws Exception {
        return run(request, "mvn -B clean package -DskipTests");
    }

    @Operation(
            operationId = "build_mvn_dependency_tree",
            summary = "Runs `mvn -B dependency:tree` inside the project path"
    )
    @POST
    @Path("/dependency-tree")
    public ExecutionResponse dependencyTree(MvnRequestDto request) throws Exception {
        return run(request, "mvn -B dependency:tree");
    }

    @Operation(
            operationId = "build_mvn_effective_pom",
            summary = "Runs `mvn -B help:effective-pom` inside the project path"
    )
    @POST
    @Path("/effective-pom")
    public ExecutionResponse effectivePom(MvnRequestDto request) throws Exception {
        return run(request, "mvn -B help:effective-pom");
    }

    @Operation(
            operationId = "build_mvn_effective_settings",
            summary = "Runs `mvn -B help:effective-settings` inside the project path"
    )
    @POST
    @Path("/effective-settings")
    public ExecutionResponse effectiveSettings(MvnRequestDto request) throws Exception {
        return run(request, "mvn -B help:effective-settings");
    }

    @Operation(
            operationId = "build_mvn_display_updates",
            summary = "Runs `mvn -B versions:display-dependency-updates` inside the project path"
    )
    @POST
    @Path("/display-updates")
    public ExecutionResponse displayUpdates(MvnRequestDto request) throws Exception {
        return run(request, "mvn -B versions:display-dependency-updates");
    }

    private ExecutionResponse run(MvnRequestDto request, String baseCommand) throws Exception {
        validateBase(request);

        String path = normalizePath(request.path);
        StringBuilder command = new StringBuilder();
        command.append("cd -- ").append(ToolSupport.shellQuote(path));
        command.append(" && ").append(baseCommand);

        if (request != null && Boolean.TRUE.equals(request.offline)) {
            command.append(" -o");
        }

        if (request != null
                && request.profile != null
                && !request.profile.isBlank()
                && !baseCommand.contains(" -Dtest=")) {
            command.append(" -P").append(ToolSupport.shellQuote(request.profile));
        }

        if (request != null
                && Boolean.TRUE.equals(request.skipTests)
                && (baseCommand.equals("mvn -B clean package")
                    || baseCommand.equals("mvn -B clean compile")
                    || baseCommand.equals("mvn -B verify"))) {
            command.append(" -DskipTests");
        }

        final String finalCommand = command.toString();

        return lock.runLocked(() -> executor.execute(finalCommand));
    }

    private void validateBase(MvnRequestDto request) {
        if (request == null || request.path == null || request.path.isBlank()) {
            throw new IllegalArgumentException("path is required");
        }
    }
}
