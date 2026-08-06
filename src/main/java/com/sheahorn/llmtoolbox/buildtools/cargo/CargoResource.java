package com.sheahorn.llmtoolbox.buildtools.cargo;

import com.sheahorn.llmtoolbox.buildtools.LogToolCall;
import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import com.sheahorn.llmtoolbox.fstools.info.FsResourceSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/build/cargo")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@LogToolCall
public class CargoResource extends FsResourceSupport {

    @Inject
    Executor executor;

    @Inject
    CargoLockService lock;

    @Operation(
            operationId = "build_cargo_build",
            summary = "Runs `cargo build` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/build")
    public ExecutionResponse build(CargoRequestDto request) throws Exception {
        return run(request, "cargo build");
    }

    @Operation(
            operationId = "build_cargo_test",
            summary = "Runs `cargo test` inside the project path; may exceed the default 30s timeout"
    )
    @POST
    @Path("/test")
    public ExecutionResponse test(CargoRequestDto request) throws Exception {
        return run(request, "cargo test");
    }

    @Operation(
            operationId = "build_cargo_test_one",
            summary = "Runs a single test via `cargo test <name>`; may exceed the default 30s timeout"
    )
    @POST
    @Path("/test-one")
    public ExecutionResponse testOne(CargoRequestDto request) throws Exception {
        if (request == null || request.testClass == null || request.testClass.isBlank()) {
            throw new IllegalArgumentException("testClass is required");
        }

        return run(request, "cargo test " + ToolSupport.shellQuote(request.testClass));
    }

    @Operation(
            operationId = "build_cargo_check",
            summary = "Runs `cargo check` inside the project path (fast compile check, no binary output)"
    )
    @POST
    @Path("/check")
    public ExecutionResponse check(CargoRequestDto request) throws Exception {
        return run(request, "cargo check");
    }

    @Operation(
            operationId = "build_cargo_clean",
            summary = "Runs `cargo clean` inside the project path"
    )
    @POST
    @Path("/clean")
    public ExecutionResponse clean(CargoRequestDto request) throws Exception {
        return run(request, "cargo clean");
    }

    @Operation(
            operationId = "build_cargo_tree",
            summary = "Runs `cargo tree` inside the project path (dependency tree)"
    )
    @POST
    @Path("/tree")
    public ExecutionResponse tree(CargoRequestDto request) throws Exception {
        return run(request, "cargo tree");
    }

    @Operation(
            operationId = "build_cargo_metadata",
            summary = "Runs `cargo metadata` inside the project path (JSON project metadata)"
    )
    @POST
    @Path("/metadata")
    public ExecutionResponse metadata(CargoRequestDto request) throws Exception {
        return run(request, "cargo metadata");
    }

    private ExecutionResponse run(CargoRequestDto request, String baseCommand) throws Exception {
        validateBase(request);

        String path = normalizePath(request.path);
        StringBuilder command = new StringBuilder();
        command.append("cd -- ").append(ToolSupport.shellQuote(path));
        command.append(" && ").append(baseCommand);

        if (request != null && Boolean.TRUE.equals(request.release)
                && baseCommand.equals("cargo build")) {
            command.append(" --release");
        }

        if (request != null && Boolean.TRUE.equals(request.offline)) {
            command.append(" --offline");
        }

        final String finalCommand = command.toString();

        return lock.runLocked(() -> executor.execute(finalCommand));
    }

    private void validateBase(CargoRequestDto request) {
        if (request == null || request.path == null || request.path.isBlank()) {
            throw new IllegalArgumentException("path is required");
        }
    }
}
