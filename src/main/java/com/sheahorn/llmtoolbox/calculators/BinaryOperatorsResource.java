package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BinaryOperatorsResource {

    private final BinaryCalculator calc;

    public BinaryOperatorsResource(
            @ConfigProperty(name = "mfop.calculator.rounding-digits", defaultValue = "8") int roundingDigits) {
        this.calc = new BinaryCalculator(roundingDigits);
    }

    @Operation(operationId = "calculator_add", summary = "Adds two numbers")
    @POST @Path("/add")
    public CalculationResponse add(CalculatorRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "add";
        r.a = request.a;
        r.b = request.b;
        r.result = calc.add(request.a, request.b);
        return r;
    }

    @Operation(operationId = "calculator_subtract", summary = "Subtracts b from a")
    @POST @Path("/subtract")
    public CalculationResponse subtract(CalculatorRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "subtract";
        r.a = request.a;
        r.b = request.b;
        r.result = calc.subtract(request.a, request.b);
        return r;
    }

    @Operation(operationId = "calculator_multiply", summary = "Multiplies two numbers")
    @POST @Path("/multiply")
    public CalculationResponse multiply(CalculatorRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "multiply";
        r.a = request.a;
        r.b = request.b;
        r.result = calc.multiply(request.a, request.b);
        return r;
    }

    @Operation(operationId = "calculator_divide", summary = "Divides a by b")
    @POST @Path("/divide")
    public CalculationResponse divide(CalculatorRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "divide";
        r.a = request.a;
        r.b = request.b;
        r.result = calc.divide(request.a, request.b);
        return r;
    }

    @Operation(operationId = "calculator_power", summary = "Raises a to the power of b")
    @POST @Path("/power")
    public CalculationResponse power(CalculatorRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "power";
        r.a = request.a;
        r.b = request.b;
        r.result = calc.power(request.a, request.b);
        return r;
    }

    @Operation(operationId = "calculator_modulo_division", summary = "True modulo (always non-negative for positive divisor)")
    @POST @Path("/modulo-division")
    public CalculationResponse moduloDivision(CalculatorRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "modulo_division";
        r.a = request.a;
        r.b = request.b;
        r.result = calc.moduloDivision(request.a, request.b);
        return r;
    }

    @Operation(operationId = "calculator_division_remainder", summary = "Remainder of a divided by b (sign follows a)")
    @POST @Path("/division-remainder")
    public CalculationResponse divisionRemainder(CalculatorRequestDto request) {
        validate(request);
        CalculationResponse r = new CalculationResponse();
        r.operation = "division_remainder";
        r.a = request.a;
        r.b = request.b;
        r.result = calc.divisionRemainder(request.a, request.b);
        return r;
    }

    @Operation(operationId = "calculator_round_to", summary = "Rounds a to the specified number of decimal digits")
    @POST @Path("/round-to")
    public CalculationResponse roundTo(CalculatorRequestDto request) {
        validate(request);
        if (request.b == null || request.b % 1 != 0) {
            throw new IllegalArgumentException("b (digits) must be an integer");
        }
        CalculationResponse r = new CalculationResponse();
        r.operation = "round_to";
        r.a = request.a;
        r.b = request.b;
        r.result = calc.roundTo(request.a, request.b.intValue());
        return r;
    }

    private void validate(CalculatorRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        if (request.a == null || request.b == null) {
            throw new IllegalArgumentException("Both 'a' and 'b' are required");
        }
    }
}
