package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class Vector2DResource {

    private final Vector2DCalculator calc;

    public Vector2DResource(
            @ConfigProperty(name = "mfop.calculator.rounding-digits", defaultValue = "8") int roundingDigits) {
        this.calc = new Vector2DCalculator(roundingDigits);
    }

    @Operation(operationId = "calculator_vector2d_dot_product", summary = "Dot product of two 2D vectors")
    @POST @Path("/vector2d-dot-product")
    public AggregateResponse dotProduct(Vector2DPairRequestDto req) {
        validatePair(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "vector2d_dot_product";
        r.result = calc.dotProduct(req.x1, req.y1, req.x2, req.y2);
        return r;
    }

    @Operation(operationId = "calculator_vector2d_cross_product", summary = "Cross product (scalar) of two 2D vectors")
    @POST @Path("/vector2d-cross-product")
    public AggregateResponse crossProduct(Vector2DPairRequestDto req) {
        validatePair(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "vector2d_cross_product";
        r.result = calc.crossProduct(req.x1, req.y1, req.x2, req.y2);
        return r;
    }

    @Operation(operationId = "calculator_vector2d_magnitude", summary = "Magnitude (length) of a 2D vector")
    @POST @Path("/vector2d-magnitude")
    public AggregateResponse magnitude(Vector2DRequestDto req) {
        validateSingle(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "vector2d_magnitude";
        r.result = calc.magnitude(req.x, req.y);
        return r;
    }

    @Operation(operationId = "calculator_vector2d_normalize", summary = "Normalize a 2D vector to unit length")
    @POST @Path("/vector2d-normalize")
    public Vector2DResponse normalize(Vector2DRequestDto req) {
        validateSingle(req);
        Vector2DResponse r = new Vector2DResponse();
        r.operation = "vector2d_normalize";
        double[] result = calc.normalize(req.x, req.y);
        r.x = result[0];
        r.y = result[1];
        return r;
    }

    @Operation(operationId = "calculator_vector2d_angle", summary = "Angle between two 2D vectors (radians)")
    @POST @Path("/vector2d-angle")
    public AggregateResponse angle(Vector2DPairRequestDto req) {
        validatePair(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "vector2d_angle";
        r.result = calc.angle(req.x1, req.y1, req.x2, req.y2);
        return r;
    }

    @Operation(operationId = "calculator_vector2d_add", summary = "Add two 2D vectors")
    @POST @Path("/vector2d-add")
    public Vector2DResponse add(Vector2DPairRequestDto req) {
        validatePair(req);
        Vector2DResponse r = new Vector2DResponse();
        r.operation = "vector2d_add";
        double[] result = calc.add(req.x1, req.y1, req.x2, req.y2);
        r.x = result[0];
        r.y = result[1];
        return r;
    }

    @Operation(operationId = "calculator_vector2d_subtract", summary = "Subtract second 2D vector from first")
    @POST @Path("/vector2d-subtract")
    public Vector2DResponse subtract(Vector2DPairRequestDto req) {
        validatePair(req);
        Vector2DResponse r = new Vector2DResponse();
        r.operation = "vector2d_subtract";
        double[] result = calc.subtract(req.x1, req.y1, req.x2, req.y2);
        r.x = result[0];
        r.y = result[1];
        return r;
    }

    @Operation(operationId = "calculator_vector2d_scale", summary = "Scale a 2D vector by a scalar")
    @POST @Path("/vector2d-scale")
    public Vector2DResponse scale(Vector2DScaleRequestDto req) {
        if (req == null || req.x == null || req.y == null || req.scalar == null)
            throw new IllegalArgumentException("x, y, and scalar are required");
        Vector2DResponse r = new Vector2DResponse();
        r.operation = "vector2d_scale";
        double[] result = calc.scale(req.x, req.y, req.scalar);
        r.x = result[0];
        r.y = result[1];
        return r;
    }

    @Operation(operationId = "calculator_vector2d_distance", summary = "Euclidean distance between two 2D points")
    @POST @Path("/vector2d-distance")
    public AggregateResponse distance(Point2DPairRequestDto req) {
        if (req == null || req.x1 == null || req.y1 == null || req.x2 == null || req.y2 == null)
            throw new IllegalArgumentException("x1, y1, x2, y2 are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "vector2d_distance";
        r.result = calc.distance(req.x1, req.y1, req.x2, req.y2);
        return r;
    }

    @Operation(operationId = "calculator_vector2d_project", summary = "Project first 2D vector onto second")
    @POST @Path("/vector2d-project")
    public Vector2DResponse project(Vector2DPairRequestDto req) {
        validatePair(req);
        Vector2DResponse r = new Vector2DResponse();
        r.operation = "vector2d_project";
        double[] result = calc.project(req.x1, req.y1, req.x2, req.y2);
        r.x = result[0];
        r.y = result[1];
        return r;
    }

    @Operation(operationId = "calculator_vector2d_cosine_similarity", summary = "Cosine similarity of two 2D vectors")
    @POST @Path("/vector2d-cosine-similarity")
    public AggregateResponse cosineSimilarity(Vector2DPairRequestDto req) {
        validatePair(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "vector2d_cosine_similarity";
        r.result = calc.cosineSimilarity(req.x1, req.y1, req.x2, req.y2);
        return r;
    }

    private void validateSingle(Vector2DRequestDto req) {
        if (req == null || req.x == null || req.y == null)
            throw new IllegalArgumentException("x and y are required");
    }

    private void validatePair(Vector2DPairRequestDto req) {
        if (req == null || req.x1 == null || req.y1 == null || req.x2 == null || req.y2 == null)
            throw new IllegalArgumentException("x1, y1, x2, y2 are required");
    }
}
