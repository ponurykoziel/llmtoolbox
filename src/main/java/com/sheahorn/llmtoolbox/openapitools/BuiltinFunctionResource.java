package com.sheahorn.llmtoolbox.openapitools;

import com.sheahorn.llmtoolbox.auth.NoBearerAuth;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/api/functions/builtin")
@Produces(MediaType.APPLICATION_JSON)
@NoBearerAuth
public class BuiltinFunctionResource {

    @Inject
    BuiltinFunctionCache cache;

    @GET
    public List<Map<String, String>> list() {
        List<Map<String, String>> result = new ArrayList<>();
        cache.all().forEach((opId, desc) -> {
            result.add(Map.of("operationId", opId, "description", desc));
        });
        return result;
    }
}
