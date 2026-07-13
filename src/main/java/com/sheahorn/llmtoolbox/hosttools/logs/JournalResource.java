package com.sheahorn.llmtoolbox.hosttools.logs;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/host/logs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JournalResource {

    private static final int DEFAULT_LINES = 200;
    private static final int MAX_LINES = 5000;

    @Inject
    Executor executor;

    @Operation(
            operationId = "host_logs_journal_unit",
            summary = "Tails the systemd journal for a given unit (default 200, max 5000 lines)"
    )
    @POST
    @Path("/journal/unit")
    public ExecutionResponse journalUnit(JournalRequestDto request) {
        validateUnit(request);

        int lines = clampLines(request.lines);
        String unit = ToolSupport.shellQuote(ToolSupport.sanitizeSafeChars(request.unit));

        String command = "journalctl -u " + unit
                + " -n " + lines
                + " --no-pager";

        return executor.execute(command);
    }

    @Operation(
            operationId = "host_logs_journal_user",
            summary = "Tails the systemd user journal for a given user unit (default 200, max 5000 lines)"
    )
    @POST
    @Path("/journal/user")
    public ExecutionResponse journalUser(JournalRequestDto request) {
        validateUnit(request);

        int lines = clampLines(request.lines);
        String unit = ToolSupport.shellQuote(ToolSupport.sanitizeSafeChars(request.unit));

        String command = "journalctl --user -u " + unit
                + " -n " + lines
                + " --no-pager";

        return executor.execute(command);
    }

    @Operation(
            operationId = "host_logs_journal_failed",
            summary = "Tails the systemd journal filtered to error+ priority from the current boot (default 200, max 5000 lines)"
    )
    @POST
    @Path("/journal/failed")
    public ExecutionResponse journalFailed(JournalRequestDto request) {
        int lines = clampLines(request.lines);

        String command = "journalctl -p err -b -n " + lines + " --no-pager";

        return executor.execute(command);
    }

    @Operation(
            operationId = "host_logs_dmesg",
            summary = "Tails the kernel ring buffer with human-readable timestamps (default 200, max 5000 lines)"
    )
    @POST
    @Path("/dmesg")
    public ExecutionResponse dmesg(JournalRequestDto request) {
        int lines = clampLines(request.lines);

        String command = "dmesg -T | tail -n " + lines;

        return executor.execute(command);
    }

    private int clampLines(Integer lines) {
        int n = lines == null ? DEFAULT_LINES : lines;
        return Math.max(1, Math.min(MAX_LINES, n));
    }

    private void validateUnit(JournalRequestDto request) {
        if (request == null || request.unit == null || request.unit.isBlank()) {
            throw new IllegalArgumentException("unit is required");
        }
    }
}
