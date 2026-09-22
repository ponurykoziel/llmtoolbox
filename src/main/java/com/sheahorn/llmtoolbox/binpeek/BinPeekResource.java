package com.sheahorn.llmtoolbox.binpeek;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import com.sheahorn.llmtoolbox.fstools.info.FsResourceSupport;
import com.sheahorn.llmtoolbox.llm.ToolBean;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/binpeek")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BinPeekResource extends FsResourceSupport implements ToolBean {

    @Inject
    Executor executor;

    @Operation(
            operationId = "binpeek_ldd",
            summary = "Prints shared object dependencies of a binary via `ldd`"
    )
    @POST
    @Path("/ldd")
    public ExecutionResponse ldd(BinPeekRequestDto request) {
        return run(request, "ldd", false);
    }

    @Operation(
            operationId = "binpeek_nm_dynamic",
            summary = "Lists dynamic symbols of a binary via `nm -D`"
    )
    @POST
    @Path("/nm-dynamic")
    public ExecutionResponse nmDynamic(BinPeekRequestDto request) {
        return run(request, "nm -D", true);
    }

    @Operation(
            operationId = "binpeek_readelf_dynamic",
            summary = "Displays the dynamic section of a binary via `readelf -d`"
    )
    @POST
    @Path("/readelf-dynamic")
    public ExecutionResponse readelfDynamic(BinPeekRequestDto request) {
        return run(request, "readelf -d", true);
    }

    @Operation(
            operationId = "binpeek_readelf_symbols",
            summary = "Displays the symbol table of a binary via `readelf -s`"
    )
    @POST
    @Path("/readelf-symbols")
    public ExecutionResponse readelfSymbols(BinPeekRequestDto request) {
        return run(request, "readelf -s", true);
    }

    @Operation(
            operationId = "binpeek_objdump_dynamic_symbols",
            summary = "Displays the dynamic symbol table of a binary via `objdump -T`"
    )
    @POST
    @Path("/objdump-dynamic-symbols")
    public ExecutionResponse objdumpDynamicSymbols(BinPeekRequestDto request) {
        return run(request, "objdump -T", true);
    }

    @Operation(
            operationId = "binpeek_strings",
            summary = "Prints printable character sequences from a binary via `strings`"
    )
    @POST
    @Path("/strings")
    public ExecutionResponse strings(BinPeekRequestDto request) {
        return run(request, "strings", true);
    }

    @Operation(
            operationId = "binpeek_file",
            summary = "Determines the file type of a binary via `file`"
    )
    @POST
    @Path("/file")
    public ExecutionResponse file(BinPeekRequestDto request) {
        return run(request, "file", true);
    }

    private ExecutionResponse run(BinPeekRequestDto request, String baseCommand, boolean useDoubleDash) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        String path = normalizePath(request.path);
        String quotedPath = ToolSupport.shellQuote(path);

        String command = baseCommand + (useDoubleDash ? " -- " : " ") + quotedPath;

        return executor.execute(command);
    }
}
