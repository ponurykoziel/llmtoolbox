package com.sheahorn.llmtoolbox.buildtools.gradle;

import com.sheahorn.llmtoolbox.buildtools.LogToolCall;
import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import com.sheahorn.llmtoolbox.fstools.info.FsResourceSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/build/gradle")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@LogToolCall
public class GradleResource extends FsResourceSupport {

    @Inject
    Executor executor;

    @Inject
    GradleLockService lock;

    @Operation(
            operationId = "build_gradle_clean",
            summary = "Runs `gradle --no-daemon clean` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/clean")
    public ExecutionResponse clean(GradleRequestDto request) throws Exception {
        return run(request, "gradle --no-daemon clean");
    }

    @Operation(
            operationId = "build_gradle_compile",
            summary = "Runs `gradle --no-daemon clean compileJava` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/compile")
    public ExecutionResponse compile(GradleRequestDto request) throws Exception {
        return run(request, "gradle --no-daemon clean compileJava");
    }

    @Operation(
            operationId = "build_gradle_test",
            summary = "Runs `gradle --no-daemon test` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/test")
    public ExecutionResponse test(GradleRequestDto request) throws Exception {
        return run(request, "gradle --no-daemon test");
    }

    @Operation(
            operationId = "build_gradle_test_one",
            summary = "Runs a single test class or pattern via `gradle --no-daemon test --tests <value>`; may exceed the default 30s timeout"
    )
    @POST
    @Path("/test-one")
    public ExecutionResponse testOne(GradleRequestDto request) throws Exception {
        if (request == null || request.testClass == null || request.testClass.isBlank()) {
            throw new IllegalArgumentException("testClass is required");
        }

        return run(request, "gradle --no-daemon test --tests " + ToolSupport.shellQuote(request.testClass));
    }

    @Operation(
            operationId = "build_gradle_check",
            summary = "Runs `gradle --no-daemon check` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/check")
    public ExecutionResponse check(GradleRequestDto request) throws Exception {
        return run(request, "gradle --no-daemon check");
    }

    @Operation(
            operationId = "build_gradle_build",
            summary = "Runs `gradle --no-daemon clean build` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/build")
    public ExecutionResponse build(GradleRequestDto request) throws Exception {
        return run(request, "gradle --no-daemon clean build");
    }

    @Operation(
            operationId = "build_gradle_build_skip_tests",
            summary = "Runs `gradle --no-daemon clean build -x test` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/build-skip-tests")
    public ExecutionResponse buildSkipTests(GradleRequestDto request) throws Exception {
        return run(request, "gradle --no-daemon clean build -x test");
    }

    @Operation(
            operationId = "build_gradle_dependencies",
            summary = "Runs `gradle --no-daemon dependencies` inside the project path"
    )
    @POST
    @Path("/dependencies")
    public ExecutionResponse dependencies(GradleRequestDto request) throws Exception {
        return run(request, "gradle --no-daemon dependencies");
    }

    @Operation(
            operationId = "build_gradle_properties",
            summary = "Runs `gradle --no-daemon properties` inside the project path"
    )
    @POST
    @Path("/properties")
    public ExecutionResponse properties(GradleRequestDto request) throws Exception {
        return run(request, "gradle --no-daemon properties");
    }

    @Operation(
            operationId = "build_gradle_build_environment",
            summary = "Runs `gradle --no-daemon buildEnvironment` inside the project path"
    )
    @POST
    @Path("/build-environment")
    public ExecutionResponse buildEnvironment(GradleRequestDto request) throws Exception {
        return run(request, "gradle --no-daemon buildEnvironment");
    }

    @Operation(
            operationId = "build_gradle_dependency_updates",
            summary = "Runs `gradle --no-daemon dependencyUpdates` inside the project path (requires com.github.ben-manes.versions plugin)"
    )
    @POST
    @Path("/dependency-updates")
    public ExecutionResponse dependencyUpdates(GradleRequestDto request) throws Exception {
        return run(request, "gradle --no-daemon dependencyUpdates");
    }

    private ExecutionResponse run(GradleRequestDto request, String baseCommand) throws Exception {
        validateBase(request);

        String path = normalizePath(request.path);
        StringBuilder command = new StringBuilder();
        command.append("cd -- ").append(ToolSupport.shellQuote(path));
        command.append(" && ").append(baseCommand);

        if (request != null && Boolean.TRUE.equals(request.offline)) {
            command.append(" --offline");
        }

        if (request != null
                && request.profile != null
                && !request.profile.isBlank()
                && !baseCommand.contains(" --tests ")) {
            command.append(" -P").append(ToolSupport.shellQuote(request.profile));
        }

        if (request != null
                && Boolean.TRUE.equals(request.skipTests)
                && (baseCommand.equals("gradle --no-daemon clean build")
                    || baseCommand.equals("gradle --no-daemon clean compileJava")
                    || baseCommand.equals("gradle --no-daemon check"))) {
            command.append(" -x test");
        }

        final String finalCommand = command.toString();

        return lock.runLocked(() -> executor.execute(finalCommand));
    }

    private void validateBase(GradleRequestDto request) {
        if (request == null || request.path == null || request.path.isBlank()) {
            throw new IllegalArgumentException("path is required");
        }
    }
}
