package com.sheahorn.llmtoolbox.hosttools.netinfo;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import com.sheahorn.llmtoolbox.llm.ToolBean;

@Path("/api/tools/host/netinfo")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WhoisResource implements ToolBean {

    @Inject
    Executor executor;

    @Operation(
            operationId = "host_netinfo_whois",
            summary = "Looks up WHOIS registration info for a host or IP"
    )
    @POST
    @Path("/whois")
    public ExecutionResponse whois(WhoisRequestDto request) {
        if (request == null || request.host == null || request.host.isBlank()) {
            throw new IllegalArgumentException("host is required");
        }

        String host = ToolSupport.sanitizeSafeChars(request.host);
        ToolSupport.validateHost(host);

        return executor.execute("whois " + ToolSupport.shellQuote(host));
    }
}
