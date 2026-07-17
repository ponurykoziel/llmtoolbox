package com.sheahorn.llmtoolbox.buildtools.cmake;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import com.sheahorn.llmtoolbox.fstools.info.FsResourceSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/api/tools/build/cmake")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CmakeResource extends FsResourceSupport {

    @Inject
    Executor executor;

    @Inject
    CmakeLockService lock;

    // -------------------------------------------------------------------------
    // configure
    // -------------------------------------------------------------------------
    @Operation(
            operationId = "build_cmake_configure",
            summary = "Runs `cmake -S <sourcePath> -B <buildPath>` with optional -D defines and build type"
    )
    @POST
    @Path("/configure")
    public ExecutionResponse configure(CmakeRequestDto request) throws Exception {
        validateConfigure(request);

        String sourcePath = normalizePath(request.sourcePath);
        String buildPath = normalizePath(request.buildPath);

        StringBuilder cmd = new StringBuilder();
        cmd.append("cmake -S ").append(ToolSupport.shellQuote(sourcePath));
        cmd.append(" -B ").append(ToolSupport.shellQuote(buildPath));

        if (request.buildType != null && !request.buildType.isBlank()) {
            cmd.append(" -DCMAKE_BUILD_TYPE=").append(ToolSupport.shellQuote(request.buildType));
        }

        if (request.defines != null) {
            for (var entry : request.defines.entrySet()) {
                cmd.append(" -D").append(ToolSupport.shellQuote(entry.getKey()))
                   .append("=").append(ToolSupport.shellQuote(entry.getValue()));
            }
        }

        return lock.runLocked(() -> executor.execute(cmd.toString()));
    }

    // -------------------------------------------------------------------------
    // read-cache
    // -------------------------------------------------------------------------
    @Operation(
            operationId = "build_cmake_read_cache",
            summary = "Reads CMakeCache.txt from the build directory and returns key variables"
    )
    @POST
    @Path("/read-cache")
    public CmakeCacheResponse readCache(CmakeRequestDto request) throws Exception {
        if (request == null || request.buildPath == null || request.buildPath.isBlank()) {
            throw new IllegalArgumentException("buildPath is required");
        }

        String buildPath = normalizePath(request.buildPath);
        java.nio.file.Path cacheFile = java.nio.file.Path.of(buildPath, "CMakeCache.txt");

        CmakeCacheResponse response = new CmakeCacheResponse();
        response.buildPath = buildPath;

        if (!Files.exists(cacheFile)) {
            return response;
        }

        // Lines of interest: KEY:TYPE=VALUE  (skip comments and blank lines)
        Pattern p = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*):[A-Z]+=(.*)$");

        for (String line : Files.readAllLines(cacheFile)) {
            Matcher m = p.matcher(line);
            if (m.matches()) {
                String key = m.group(1);
                String value = m.group(2);
                // Only surface "interesting" keys — skip internal/advanced noise
                if (isInteresting(key)) {
                    response.entries.put(key, value);
                }
            }
        }

        return response;
    }

    // -------------------------------------------------------------------------
    // build
    // -------------------------------------------------------------------------
    @Operation(
            operationId = "build_cmake_build",
            summary = "Runs `cmake --build <buildPath>` with optional --target, --clean-first, -j, --verbose"
    )
    @POST
    @Path("/build")
    public ExecutionResponse build(CmakeRequestDto request) throws Exception {
        validateBuild(request);

        StringBuilder cmd = new StringBuilder();
        cmd.append("cmake --build ").append(ToolSupport.shellQuote(normalizePath(request.buildPath)));

        if (Boolean.TRUE.equals(request.cleanFirst)) {
            cmd.append(" --clean-first");
        }

        if (request.target != null && !request.target.isBlank()) {
            cmd.append(" --target ").append(ToolSupport.shellQuote(request.target));
        }

        if (request.parallel != null && request.parallel > 0) {
            cmd.append(" -j").append(request.parallel);
        }

        if (Boolean.TRUE.equals(request.verbose)) {
            cmd.append(" --verbose");
        }

        return lock.runLocked(() -> executor.execute(cmd.toString()));
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------
    private void validateConfigure(CmakeRequestDto request) {
        if (request == null || request.sourcePath == null || request.sourcePath.isBlank()) {
            throw new IllegalArgumentException("sourcePath is required");
        }
        if (request.buildPath == null || request.buildPath.isBlank()) {
            throw new IllegalArgumentException("buildPath is required");
        }
    }

    private void validateBuild(CmakeRequestDto request) {
        if (request == null || request.buildPath == null || request.buildPath.isBlank()) {
            throw new IllegalArgumentException("buildPath is required");
        }
    }

    /** Filters cache entries to surface only user-relevant variables. */
    private boolean isInteresting(String key) {
        // Always show project-level and build-type variables
        if (key.startsWith("CMAKE_BUILD_TYPE")) return true;
        if (key.startsWith("CMAKE_C_COMPILER") && !key.contains("_AR") && !key.contains("_RANLIB")) return true;
        if (key.startsWith("CMAKE_CXX_COMPILER") && !key.contains("_AR") && !key.contains("_RANLIB")) return true;
        if (key.startsWith("CMAKE_PROJECT_")) return true;

        // Show user-defined cache variables (not internal CMAKE_*, not pkg-config internals)
        if (key.startsWith("CMAKE_")) return false;
        if (key.startsWith("_")) return false;
        if (key.startsWith("__")) return false;
        if (key.startsWith("pkgcfg_")) return false;
        if (key.startsWith("FIND_PACKAGE_")) return false;
        if (key.startsWith("JUCE_")) return true; // JUCE-specific vars are user-relevant

        // Catch project-specific variables (uppercase, not internal)
        return Character.isUpperCase(key.charAt(0));
    }
}
