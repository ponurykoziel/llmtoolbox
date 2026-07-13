package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RandomResource {

    private final RandomCalculator calc = new RandomCalculator();

    @Operation(operationId = "calculator_random_value", summary = "Generate random value: byte, boolean, int, long, float, double, gaussian, uuid")
    @POST @Path("/random-value")
    public TextResponse randomValue(RandomValueRequestDto req) {
        if (req == null || req.type == null || req.type.isBlank())
            throw new IllegalArgumentException("type is required");
        TextResponse r = new TextResponse();
        r.operation = "random_value";
        r.result = calc.randomValue(req.type, req.min, req.max, req.mean, req.stddev);
        return r;
    }

    @Operation(operationId = "calculator_random_text", summary = "Generate random text of given length and charset")
    @POST @Path("/random-text")
    public TextResponse randomText(RandomTextRequestDto req) {
        if (req == null || req.length == null)
            throw new IllegalArgumentException("length is required");
        if (req.length < 1 || req.length > 10000)
            throw new IllegalArgumentException("length must be 1-10000");
        TextResponse r = new TextResponse();
        r.operation = "random_text";
        r.result = calc.randomText(req.length, req.charset, req.customChars);
        return r;
    }
}
