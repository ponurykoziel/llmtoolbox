package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BaseResource {

    private final BaseCalculator calc = new BaseCalculator();

    @Operation(operationId = "calculator_base_conversion", summary = "Convert a number between bases (2-36)")
    @POST @Path("/base-conversion")
    public TextResponse baseConversion(BaseConversionRequestDto req) {
        if (req == null || req.value == null || req.fromBase == null || req.toBase == null)
            throw new IllegalArgumentException("value, fromBase, and toBase are required");
        TextResponse r = new TextResponse();
        r.operation = "base_conversion";
        r.result = calc.baseConversion(req.value, req.fromBase, req.toBase);
        return r;
    }

    @Operation(operationId = "calculator_decompose_bytes", summary = "Decompose a long into 4 unsigned bytes (big-endian)")
    @POST @Path("/decompose-bytes")
    public TextResponse decomposeBytes(ByteDecomposeRequestDto req) {
        if (req == null || req.value == null)
            throw new IllegalArgumentException("value is required");
        TextResponse r = new TextResponse();
        r.operation = "decompose_bytes";
        long[] bytes = calc.decomposeBytes(req.value);
        r.result = bytes[0] + ", " + bytes[1] + ", " + bytes[2] + ", " + bytes[3];
        return r;
    }

    @Operation(operationId = "calculator_compose_bytes", summary = "Compose 4 unsigned bytes into a long (big-endian)")
    @POST @Path("/compose-bytes")
    public TextResponse composeBytes(ByteComposeRequestDto req) {
        if (req == null || req.b0 == null || req.b1 == null || req.b2 == null || req.b3 == null)
            throw new IllegalArgumentException("b0, b1, b2, b3 are required");
        TextResponse r = new TextResponse();
        r.operation = "compose_bytes";
        r.result = String.valueOf(calc.composeBytes(req.b0, req.b1, req.b2, req.b3));
        return r;
    }

    @Operation(operationId = "calculator_bitwise_ops", summary = "Bitwise operations: and, or, xor, not, shl, shr")
    @POST @Path("/bitwise-ops")
    public TextResponse bitwiseOps(BitwiseRequestDto req) {
        if (req == null || req.op == null || req.op.isBlank())
            throw new IllegalArgumentException("op is required");
        TextResponse r = new TextResponse();
        r.operation = "bitwise_ops";
        r.result = calc.bitwiseOps(req.op, req.a, req.b);
        return r;
    }
}
