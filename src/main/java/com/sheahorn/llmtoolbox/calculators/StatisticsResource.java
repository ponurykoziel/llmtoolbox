package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsResource {

    private final StatisticsCalculator calc;

    public StatisticsResource(
            @ConfigProperty(name = "mfop.calculator.rounding-digits", defaultValue = "8") int roundingDigits) {
        this.calc = new StatisticsCalculator(roundingDigits);
    }

    @Operation(operationId = "calculator_sum", summary = "Sum of an array of numbers")
    @POST @Path("/sum")
    public AggregateResponse sum(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "sum";
        r.result = calc.sum(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_mean", summary = "Arithmetic mean of an array of numbers")
    @POST @Path("/mean")
    public AggregateResponse mean(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "mean";
        r.result = calc.mean(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_geometric_mean", summary = "Geometric mean of an array of positive numbers")
    @POST @Path("/geometric-mean")
    public AggregateResponse geometricMean(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "geometric_mean";
        r.result = calc.geometricMean(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_harmonic_mean", summary = "Harmonic mean of an array of non-zero numbers")
    @POST @Path("/harmonic-mean")
    public AggregateResponse harmonicMean(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "harmonic_mean";
        r.result = calc.harmonicMean(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_median", summary = "Median of an array of numbers")
    @POST @Path("/median")
    public AggregateResponse median(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "median";
        r.result = calc.median(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_mode", summary = "Mode (most frequent value) of an array of numbers")
    @POST @Path("/mode")
    public AggregateResponse mode(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "mode";
        r.result = calc.mode(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_stddev", summary = "Population standard deviation")
    @POST @Path("/stddev")
    public AggregateResponse stddev(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "stddev";
        r.result = calc.stddev(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_variance", summary = "Population variance")
    @POST @Path("/variance")
    public AggregateResponse variance(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "variance";
        r.result = calc.variance(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_skewness", summary = "Skewness of an array of numbers")
    @POST @Path("/skewness")
    public AggregateResponse skewness(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "skewness";
        r.result = calc.skewness(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_kurtosis", summary = "Excess kurtosis of an array of numbers")
    @POST @Path("/kurtosis")
    public AggregateResponse kurtosis(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "kurtosis";
        r.result = calc.kurtosis(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_min", summary = "Minimum value in an array")
    @POST @Path("/min")
    public AggregateResponse min(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "min";
        r.result = calc.min(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_max", summary = "Maximum value in an array")
    @POST @Path("/max")
    public AggregateResponse max(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "max";
        r.result = calc.max(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_quantile", summary = "q-th quantile (0..1) of sorted values")
    @POST @Path("/quantile")
    public AggregateResponse quantile(AggregateRequestDto req) {
        validateAggregate(req);
        if (req.q == null || req.q < 0.0 || req.q > 1.0)
            throw new IllegalArgumentException("q must be between 0 and 1");
        AggregateResponse r = new AggregateResponse();
        r.operation = "quantile";
        r.result = calc.quantile(unbox(req.values), req.q);
        return r;
    }

    @Operation(operationId = "calculator_iqr", summary = "Interquartile range (Q3 - Q1)")
    @POST @Path("/iqr")
    public AggregateResponse iqr(AggregateRequestDto req) {
        validateAggregate(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "iqr";
        r.result = calc.iqr(unbox(req.values));
        return r;
    }

    @Operation(operationId = "calculator_z_score", summary = "Z-score: (value - mean) / stddev")
    @POST @Path("/z-score")
    public AggregateResponse zScore(ZScoreRequestDto req) {
        if (req == null || req.value == null || req.mean == null || req.stddev == null)
            throw new IllegalArgumentException("value, mean, and stddev are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "z_score";
        r.result = calc.zScore(req.value, req.mean, req.stddev);
        return r;
    }

    @Operation(operationId = "calculator_binomial_pmf", summary = "Binomial PMF: P(X=k) for n trials, probability p")
    @POST @Path("/binomial-pmf")
    public AggregateResponse binomialPmf(BinomialRequestDto req) {
        if (req == null || req.n == null || req.p == null || req.k == null)
            throw new IllegalArgumentException("n, p, and k are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "binomial_pmf";
        r.result = calc.binomialPmf(req.n, req.p, req.k);
        return r;
    }

    @Operation(operationId = "calculator_poisson_pmf", summary = "Poisson PMF: P(X=k) for rate lambda")
    @POST @Path("/poisson-pmf")
    public AggregateResponse poissonPmf(PoissonRequestDto req) {
        if (req == null || req.lambda == null || req.k == null)
            throw new IllegalArgumentException("lambda and k are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "poisson_pmf";
        r.result = calc.poissonPmf(req.lambda, req.k);
        return r;
    }

    @Operation(operationId = "calculator_linear_regression", summary = "Simple linear regression: returns slope and intercept")
    @POST @Path("/linear-regression")
    public LinearRegressionResponse linearRegression(LinearRegressionRequestDto req) {
        if (req == null || req.x == null || req.y == null || req.x.length == 0 || req.y.length == 0)
            throw new IllegalArgumentException("x and y arrays are required and must not be empty");
        if (req.x.length != req.y.length)
            throw new IllegalArgumentException("x and y must have the same length");
        LinearRegressionResponse r = new LinearRegressionResponse();
        r.operation = "linear_regression";
        double[] result = calc.linearRegression(unbox(req.x), unbox(req.y));
        r.slope = result[0];
        r.intercept = result[1];
        return r;
    }

    @Operation(operationId = "calculator_clamp", summary = "Clamps value between low and high")
    @POST @Path("/clamp")
    public AggregateResponse clamp(ClampRequestDto req) {
        if (req == null) throw new IllegalArgumentException("Request is required");
        if (req.value == null || req.low == null || req.high == null)
            throw new IllegalArgumentException("value, low, and high are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "clamp";
        r.result = calc.clamp(req.value, req.low, req.high);
        return r;
    }

    @Operation(operationId = "calculator_factorial", summary = "Factorial of a non-negative integer")
    @POST @Path("/factorial")
    public AggregateResponse factorial(UnaryRequestDto req) {
        if (req == null) throw new IllegalArgumentException("Request is required");
        if (req.a == null) throw new IllegalArgumentException("Field 'a' is required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "factorial";
        r.result = calc.factorial(req.a);
        return r;
    }

    @Operation(operationId = "calculator_gcd", summary = "Greatest common divisor of two integers")
    @POST @Path("/gcd")
    public CalculationResponse gcd(CalculatorRequestDto req) {
        validateBinary(req);
        CalculationResponse r = new CalculationResponse();
        r.operation = "gcd";
        r.a = req.a;
        r.b = req.b;
        r.result = calc.gcd(req.a, req.b);
        return r;
    }

    @Operation(operationId = "calculator_lcm", summary = "Least common multiple of two integers")
    @POST @Path("/lcm")
    public CalculationResponse lcm(CalculatorRequestDto req) {
        validateBinary(req);
        CalculationResponse r = new CalculationResponse();
        r.operation = "lcm";
        r.a = req.a;
        r.b = req.b;
        r.result = calc.lcm(req.a, req.b);
        return r;
    }

    @Operation(operationId = "calculator_permute", summary = "Permutations P(n, k) = n! / (n-k)!")
    @POST @Path("/permute")
    public CalculationResponse permute(CalculatorRequestDto req) {
        validateBinary(req);
        CalculationResponse r = new CalculationResponse();
        r.operation = "permute";
        r.a = req.a;
        r.b = req.b;
        r.result = calc.permute(req.a, req.b);
        return r;
    }

    @Operation(operationId = "calculator_combine", summary = "Combinations C(n, k) = n! / (k! * (n-k)!)")
    @POST @Path("/combine")
    public CalculationResponse combine(CalculatorRequestDto req) {
        validateBinary(req);
        CalculationResponse r = new CalculationResponse();
        r.operation = "combine";
        r.a = req.a;
        r.b = req.b;
        r.result = calc.combine(req.a, req.b);
        return r;
    }

    @Operation(operationId = "calculator_a_of_b_to_c_percent", summary = "What percent is a of b? → c%")
    @POST @Path("/a-of-b-to-c-percent")
    public CalculationResponse aOfBToCPercent(CalculatorRequestDto req) {
        validateBinary(req);
        CalculationResponse r = new CalculationResponse();
        r.operation = "a_of_b_to_c_percent";
        r.a = req.a;
        r.b = req.b;
        r.result = calc.aOfBToCPercent(req.a, req.b);
        return r;
    }

    @Operation(operationId = "calculator_a_percent_of_b_to_c", summary = "a% of b → c")
    @POST @Path("/a-percent-of-b-to-c")
    public CalculationResponse aPercentOfBToC(CalculatorRequestDto req) {
        validateBinary(req);
        CalculationResponse r = new CalculationResponse();
        r.operation = "a_percent_of_b_to_c";
        r.a = req.a;
        r.b = req.b;
        r.result = calc.aPercentOfBToC(req.a, req.b);
        return r;
    }

    @Operation(operationId = "calculator_ratio", summary = "Ratio a / b")
    @POST @Path("/ratio")
    public CalculationResponse ratio(CalculatorRequestDto req) {
        validateBinary(req);
        CalculationResponse r = new CalculationResponse();
        r.operation = "ratio";
        r.a = req.a;
        r.b = req.b;
        r.result = calc.ratio(req.a, req.b);
        return r;
    }

    @Operation(operationId = "calculator_linear_interpolate", summary = "Linear interpolation: y at x given (x1,y1) and (x2,y2)")
    @POST @Path("/linear-interpolate")
    public AggregateResponse linearInterpolate(InterpolateRequestDto req) {
        if (req == null) throw new IllegalArgumentException("Request is required");
        if (req.x == null || req.x1 == null || req.y1 == null || req.x2 == null || req.y2 == null)
            throw new IllegalArgumentException("x, x1, y1, x2, y2 are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "linear_interpolate";
        r.result = calc.linearInterpolate(req.x, req.x1, req.y1, req.x2, req.y2);
        return r;
    }

    @Operation(operationId = "calculator_solve_linear_equation", summary = "Solves ax + b = 0 for x")
    @POST @Path("/solve-linear-equation")
    public CalculationResponse solveLinearEquation(CalculatorRequestDto req) {
        validateBinary(req);
        CalculationResponse r = new CalculationResponse();
        r.operation = "solve_linear_equation";
        r.a = req.a;
        r.b = req.b;
        r.result = calc.solveLinearEquation(req.a, req.b);
        return r;
    }

    @Operation(operationId = "calculator_solve_quadric_equation", summary = "Solves ax² + bx + c = 0")
    @POST @Path("/solve-quadric-equation")
    public QuadricResponse solveQuadricEquation(QuadricRequestDto req) {
        if (req == null) throw new IllegalArgumentException("Request is required");
        if (req.a == null || req.b == null || req.c == null)
            throw new IllegalArgumentException("a, b, c are required");
        QuadricResponse r = new QuadricResponse();
        r.operation = "solve_quadric_equation";
        r.a = req.a;
        r.b = req.b;
        r.c = req.c;
        double[] roots = calc.solveQuadricEquation(req.a, req.b, req.c);
        r.root1 = roots[0];
        r.root2 = roots[1];
        return r;
    }

    private double[] unbox(Double[] values) {
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) result[i] = values[i];
        return result;
    }

    private void validateAggregate(AggregateRequestDto req) {
        if (req == null) throw new IllegalArgumentException("Request is required");
        if (req.values == null || req.values.length == 0)
            throw new IllegalArgumentException("values array is required and must not be empty");
    }

    private void validateBinary(CalculatorRequestDto req) {
        if (req == null) throw new IllegalArgumentException("Request is required");
        if (req.a == null || req.b == null)
            throw new IllegalArgumentException("Both 'a' and 'b' are required");
    }
}
