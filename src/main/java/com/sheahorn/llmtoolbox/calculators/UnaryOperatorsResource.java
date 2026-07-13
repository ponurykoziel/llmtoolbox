package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UnaryOperatorsResource {

    private final UnaryCalculator calc;

    public UnaryOperatorsResource(
            @ConfigProperty(name = "mfop.calculator.rounding-digits", defaultValue = "8") int roundingDigits) {
        this.calc = new UnaryCalculator(roundingDigits);
    }

    @Operation(operationId = "calculator_sqrt", summary = "Square root of a")
    @POST @Path("/sqrt")
    public CalculationResponse sqrt(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "sqrt";
        r.a = request.a;
        r.result = calc.sqrt(request.a);
        return r;
    }

    @Operation(operationId = "calculator_cbrt", summary = "Cube root of a")
    @POST @Path("/cbrt")
    public CalculationResponse cbrt(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "cbrt";
        r.a = request.a;
        r.result = calc.cbrt(request.a);
        return r;
    }

    @Operation(operationId = "calculator_log2", summary = "Base-2 logarithm of a")
    @POST @Path("/log2")
    public CalculationResponse log2(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "log2";
        r.a = request.a;
        r.result = calc.log2(request.a);
        return r;
    }

    @Operation(operationId = "calculator_log10", summary = "Base-10 logarithm of a")
    @POST @Path("/log10")
    public CalculationResponse log10(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "log10";
        r.a = request.a;
        r.result = calc.log10(request.a);
        return r;
    }

    @Operation(operationId = "calculator_ln", summary = "Natural logarithm of a")
    @POST @Path("/ln")
    public CalculationResponse ln(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "ln";
        r.a = request.a;
        r.result = calc.ln(request.a);
        return r;
    }

    @Operation(operationId = "calculator_sin", summary = "Sine of a (radians)")
    @POST @Path("/sin")
    public CalculationResponse sin(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "sin";
        r.a = request.a;
        r.result = calc.sin(request.a);
        return r;
    }

    @Operation(operationId = "calculator_cos", summary = "Cosine of a (radians)")
    @POST @Path("/cos")
    public CalculationResponse cos(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "cos";
        r.a = request.a;
        r.result = calc.cos(request.a);
        return r;
    }

    @Operation(operationId = "calculator_tan", summary = "Tangent of a (radians)")
    @POST @Path("/tan")
    public CalculationResponse tan(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "tan";
        r.a = request.a;
        r.result = calc.tan(request.a);
        return r;
    }

    @Operation(operationId = "calculator_sign", summary = "Sign of a: -1, 0, or 1")
    @POST @Path("/sign")
    public CalculationResponse sign(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "sign";
        r.a = request.a;
        r.result = (double) calc.sign(request.a);
        return r;
    }

    @Operation(operationId = "calculator_abs", summary = "Absolute value of a")
    @POST @Path("/abs")
    public CalculationResponse abs(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "abs";
        r.a = request.a;
        r.result = calc.abs(request.a);
        return r;
    }

    @Operation(operationId = "calculator_magnitude", summary = "Base-10 order of magnitude of a (e.g. 500 → 2, 0.007 → -3)")
    @POST @Path("/magnitude")
    public CalculationResponse magnitude(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "magnitude";
        r.a = request.a;
        r.result = (double) calc.magnitude(request.a);
        return r;
    }

    @Operation(operationId = "calculator_nearest_int", summary = "Rounds a to the nearest integer (half-up)")
    @POST @Path("/nearest-int")
    public CalculationResponse nearestInt(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "nearest_int";
        r.a = request.a;
        r.result = calc.nearestInt(request.a);
        return r;
    }

    @Operation(operationId = "calculator_ceil", summary = "Smallest integer >= a")
    @POST @Path("/ceil")
    public CalculationResponse ceil(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "ceil";
        r.a = request.a;
        r.result = calc.ceil(request.a);
        return r;
    }

    @Operation(operationId = "calculator_floor", summary = "Largest integer <= a")
    @POST @Path("/floor")
    public CalculationResponse floor(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "floor";
        r.a = request.a;
        r.result = calc.floor(request.a);
        return r;
    }

    @Operation(operationId = "calculator_trunc", summary = "Truncates a toward zero")
    @POST @Path("/trunc")
    public CalculationResponse trunc(UnaryRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "trunc";
        r.a = request.a;
        r.result = calc.trunc(request.a);
        return r;
    }

    private void validate(UnaryRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        if (request.a == null) {
            throw new IllegalArgumentException("Field 'a' is required");
        }
    }
}
