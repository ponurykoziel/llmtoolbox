package com.sheahorn.llmtoolbox.nettools.dig;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

@Path("/api/tools/net/dig")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DigController {

    private static final List<DigRecordType> DEFAULT_TYPES = List.of(
            DigRecordType.A,
            DigRecordType.AAAA,
            DigRecordType.CNAME,
            DigRecordType.MX,
            DigRecordType.TXT,
            DigRecordType.NS,
            DigRecordType.SOA,
            DigRecordType.SRV,
            DigRecordType.CAA,
            DigRecordType.DNSKEY,
            DigRecordType.DS,
            DigRecordType.NAPTR,
            DigRecordType.TLSA,
            DigRecordType.SVCB,
            DigRecordType.HTTPS
    );

    @Inject
    Executor executor;

    @Operation(
            operationId = "net_dig",
            summary = "Resolves DNS records for a given name using the system dig binary"
    )
    @POST
    @Path("/execute")
    public ExecutionResponse execute(DigRequestDto request) {
        validate(request);

        String command = buildDigCommand(request);

        return executor.execute(command);
    }

    private String buildDigCommand(DigRequestDto request) {
        List<DigRecordType> types = request.types == null || request.types.isEmpty()
                ? DEFAULT_TYPES
                : request.types;

        StringBuilder command = new StringBuilder();

        boolean first = true;

        for (DigRecordType type : types) {
            if (!first) {
                command.append("; ");
            }

            command.append("echo ")
                    .append(ToolSupport.shellQuote("\n===== " + type.name() + " ====="));

            command.append("; ");

            command.append("dig")
                    .append(" +time=5")
                    .append(" +tries=1")
                    .append(" ")
                    .append(ToolSupport.shellQuote(request.name))
                    .append(" ")
                    .append(ToolSupport.shellQuote(type.name()));

            first = false;
        }

        return command.toString();
    }

    private void validate(DigRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        request.name = ToolSupport.sanitizeSafeChars(request.name);

        if (request.name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        if (request.name.length() > 253) {
            throw new IllegalArgumentException("name is too long");
        }

        if (!isSafeDnsName(request.name)) {
            throw new IllegalArgumentException("name contains invalid characters");
        }
    }

    private boolean isSafeDnsName(String value) {
        return value.matches("[a-zA-Z0-9._*:\\-]+");
    }
}
