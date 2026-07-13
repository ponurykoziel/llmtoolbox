package com.sheahorn.llmtoolbox.nettools.curl;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Path("/api/tools/net/curl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CurlController {

    @Inject
    Executor executor;

    @ConfigProperty(name = "llmtoolbox.net.curl.block-private-ips", defaultValue = "false")
    boolean blockPrivateIps;

    @Operation(
            operationId = "net_curl",
            summary = "Performs an HTTPS request using the system curl binary"
    )
    @POST
    public ExecutionResponse execute(CurlRequestDto request) {
        validate(request);
        String command = buildCurlCommand(request);
        return executor.execute(command);
    }

    private String buildCurlCommand(CurlRequestDto request) {
        StringBuilder command = new StringBuilder();

        command.append("curl");

        // Always follow redirects
        command.append(" -L");

        // Always 5s curl timeout
        command.append(" --max-time 5");

        // Cleaner output
        command.append(" --silent");
        command.append(" --show-error");

        // Lock down protocols
        command.append(" --proto http,https");
        command.append(" --proto-redir http,https");

        command.append(" -X ")
                .append(ToolSupport.shellQuote(request.verb.name()));

        if (request.contentType != null && !request.contentType.isBlank()) {
            command.append(" -H ")
                    .append(ToolSupport.shellQuote("Content-Type: " + stripCrLf(request.contentType)));
        }

        if (request.accept != null && !request.accept.isBlank()) {
            command.append(" -H ")
                    .append(ToolSupport.shellQuote("Accept: " + stripCrLf(request.accept)));
        }

        if (request.headers != null) {
            for (Map.Entry<String, String> header : request.headers.entrySet()) {
                if (header.getKey() == null || header.getKey().isBlank()) {
                    continue;
                }

                command.append(" -H ")
                        .append(ToolSupport.shellQuote(
                                stripCrLf(header.getKey()) + ": " + stripCrLf(nullToEmpty(header.getValue()))));
            }
        }

        if (request.body != null) {
            command.append(" -d ")
                    .append(ToolSupport.shellQuote(request.body));
        }

        command.append(" -- ")
                .append(ToolSupport.shellQuote(buildUrl(request)));

        return command.toString();
    }

    private String buildUrl(CurlRequestDto request) {
        if (request.queryParams == null || request.queryParams.isEmpty()) {
            return request.url;
        }

        StringBuilder url = new StringBuilder(request.url);

        if (request.url.contains("?")) {
            url.append("&");
        } else {
            url.append("?");
        }

        boolean first = true;

        for (Map.Entry<String, String> entry : request.queryParams.entrySet()) {
            if (!first) {
                url.append("&");
            }

            url.append(encode(entry.getKey()))
                    .append("=")
                    .append(encode(nullToEmpty(entry.getValue())));

            first = false;
        }

        return url.toString();
    }

    private void validate(CurlRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        if (request.verb == null) {
            throw new IllegalArgumentException("verb is required");
        }

        if (request.url == null || request.url.isBlank()) {
            throw new IllegalArgumentException("url is required");
        }

        URI uri = URI.create(request.url);

        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Only HTTPS URLs are allowed");
        }

        if (blockPrivateIps) {
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("URL must have a host");
            }

            String hostLower = host.toLowerCase();
            if (hostLower.equals("localhost")
                    || hostLower.equals("127.0.0.1")
                    || hostLower.equals("0.0.0.0")
                    || hostLower.equals("[::1]")
                    || hostLower.startsWith("169.254.")
                    || hostLower.startsWith("10.")
                    || hostLower.startsWith("172.16.") || hostLower.startsWith("172.17.")
                    || hostLower.startsWith("172.18.") || hostLower.startsWith("172.19.")
                    || hostLower.startsWith("172.20.") || hostLower.startsWith("172.21.")
                    || hostLower.startsWith("172.22.") || hostLower.startsWith("172.23.")
                    || hostLower.startsWith("172.24.") || hostLower.startsWith("172.25.")
                    || hostLower.startsWith("172.26.") || hostLower.startsWith("172.27.")
                    || hostLower.startsWith("172.28.") || hostLower.startsWith("172.29.")
                    || hostLower.startsWith("172.30.") || hostLower.startsWith("172.31.")
                    || hostLower.startsWith("192.168.")
                    || hostLower.startsWith("fc") || hostLower.startsWith("fd")) {
                throw new IllegalArgumentException("URL targets a private/internal network");
            }
        }
    }

    private String stripCrLf(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r", "").replace("\n", "");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

}
