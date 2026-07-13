package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class Vector3DResource {

    private final Vector3DCalculator calc;

    public Vector3DResource(
            @ConfigProperty(name = "mfop.calculator.rounding-digits", defaultValue = "8") int roundingDigits) {
        this.calc = new Vector3DCalculator(roundingDigits);
    }

    @Operation(operationId = "calculator_vector3d_dot_product", summary = "Dot product of two 3D vectors")
    @POST @Path("/vector3d-dot-product")
    public AggregateResponse dotProduct(Vector3DPairRequestDto req) {
        validatePair(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "vector3d_dot_product";
        r.result = calc.dotProduct(req.x1, req.y1, req.z1, req.x2, req.y2, req.z2);
        return r;
    }

    @Operation(operationId = "calculator_vector3d_cross_product", summary = "Cross product of two 3D vectors")
    @POST @Path("/vector3d-cross-product")
    public Vector3DResponse crossProduct(Vector3DPairRequestDto req) {
        validatePair(req);
        Vector3DResponse r = new Vector3DResponse();
        r.operation = "vector3d_cross_product";
        double[] result = calc.crossProduct(req.x1, req.y1, req.z1, req.x2, req.y2, req.z2);
        r.x = result[0];
        r.y = result[1];
        r.z = result[2];
        return r;
    }

    @Operation(operationId = "calculator_vector3d_magnitude", summary = "Magnitude (length) of a 3D vector")
    @POST @Path("/vector3d-magnitude")
    public AggregateResponse magnitude(Vector3DRequestDto req) {
        validateSingle(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "vector3d_magnitude";
        r.result = calc.magnitude(req.x, req.y, req.z);
        return r;
    }

    @Operation(operationId = "calculator_vector3d_normalize", summary = "Normalize a 3D vector to unit length")
    @POST @Path("/vector3d-normalize")
    public Vector3DResponse normalize(Vector3DRequestDto req) {
        validateSingle(req);
        Vector3DResponse r = new Vector3DResponse();
        r.operation = "vector3d_normalize";
        double[] result = calc.normalize(req.x, req.y, req.z);
        r.x = result[0];
        r.y = result[1];
        r.z = result[2];
        return r;
    }

    @Operation(operationId = "calculator_vector3d_angle", summary = "Angle between two 3D vectors (radians)")
    @POST @Path("/vector3d-angle")
    public AggregateResponse angle(Vector3DPairRequestDto req) {
        validatePair(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "vector3d_angle";
        r.result = calc.angle(req.x1, req.y1, req.z1, req.x2, req.y2, req.z2);
        return r;
    }

    @Operation(operationId = "calculator_vector3d_add", summary = "Add two 3D vectors")
    @POST @Path("/vector3d-add")
    public Vector3DResponse add(Vector3DPairRequestDto req) {
        validatePair(req);
        Vector3DResponse r = new Vector3DResponse();
        r.operation = "vector3d_add";
        double[] result = calc.add(req.x1, req.y1, req.z1, req.x2, req.y2, req.z2);
        r.x = result[0];
        r.y = result[1];
        r.z = result[2];
        return r;
    }

    @Operation(operationId = "calculator_vector3d_subtract", summary = "Subtract second 3D vector from first")
    @POST @Path("/vector3d-subtract")
    public Vector3DResponse subtract(Vector3DPairRequestDto req) {
        validatePair(req);
        Vector3DResponse r = new Vector3DResponse();
        r.operation = "vector3d_subtract";
        double[] result = calc.subtract(req.x1, req.y1, req.z1, req.x2, req.y2, req.z2);
        r.x = result[0];
        r.y = result[1];
        r.z = result[2];
        return r;
    }

    @Operation(operationId = "calculator_vector3d_scale", summary = "Scale a 3D vector by a scalar")
    @POST @Path("/vector3d-scale")
    public Vector3DResponse scale(Vector3DScaleRequestDto req) {
        if (req == null || req.x == null || req.y == null || req.z == null || req.scalar == null)
            throw new IllegalArgumentException("x, y, z, and scalar are required");
        Vector3DResponse r = new Vector3DResponse();
        r.operation = "vector3d_scale";
        double[] result = calc.scale(req.x, req.y, req.z, req.scalar);
        r.x = result[0];
        r.y = result[1];
        r.z = result[2];
        return r;
    }

    @Operation(operationId = "calculator_vector3d_distance", summary = "Euclidean distance between two 3D points")
    @POST @Path("/vector3d-distance")
    public AggregateResponse distance(Point3DPairRequestDto req) {
        if (req == null || req.x1 == null || req.y1 == null || req.z1 == null
                || req.x2 == null || req.y2 == null || req.z2 == null)
            throw new IllegalArgumentException("x1, y1, z1, x2, y2, z2 are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "vector3d_distance";
        r.result = calc.distance(req.x1, req.y1, req.z1, req.x2, req.y2, req.z2);
        return r;
    }

    @Operation(operationId = "calculator_vector3d_project", summary = "Project first 3D vector onto second")
    @POST @Path("/vector3d-project")
    public Vector3DResponse project(Vector3DPairRequestDto req) {
        validatePair(req);
        Vector3DResponse r = new Vector3DResponse();
        r.operation = "vector3d_project";
        double[] result = calc.project(req.x1, req.y1, req.z1, req.x2, req.y2, req.z2);
        r.x = result[0];
        r.y = result[1];
        r.z = result[2];
        return r;
    }

    @Operation(operationId = "calculator_vector3d_scalar_triple_product", summary = "Scalar triple product a·(b×c)")
    @POST @Path("/vector3d-scalar-triple-product")
    public AggregateResponse scalarTripleProduct(Vector3DTripleRequestDto req) {
        validateTriple(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "vector3d_scalar_triple_product";
        r.result = calc.scalarTripleProduct(req.x1, req.y1, req.z1, req.x2, req.y2, req.z2, req.x3, req.y3, req.z3);
        return r;
    }

    @Operation(operationId = "calculator_vector3d_vector_triple_product", summary = "Vector triple product a×(b×c)")
    @POST @Path("/vector3d-vector-triple-product")
    public Vector3DResponse vectorTripleProduct(Vector3DTripleRequestDto req) {
        validateTriple(req);
        Vector3DResponse r = new Vector3DResponse();
        r.operation = "vector3d_vector_triple_product";
        double[] result = calc.vectorTripleProduct(req.x1, req.y1, req.z1, req.x2, req.y2, req.z2, req.x3, req.y3, req.z3);
        r.x = result[0];
        r.y = result[1];
        r.z = result[2];
        return r;
    }

    @Operation(operationId = "calculator_vector3d_cosine_similarity", summary = "Cosine similarity of two 3D vectors")
    @POST @Path("/vector3d-cosine-similarity")
    public AggregateResponse cosineSimilarity(Vector3DPairRequestDto req) {
        validatePair(req);
        AggregateResponse r = new AggregateResponse();
        r.operation = "vector3d_cosine_similarity";
        r.result = calc.cosineSimilarity(req.x1, req.y1, req.z1, req.x2, req.y2, req.z2);
        return r;
    }

    private void validateSingle(Vector3DRequestDto req) {
        if (req == null || req.x == null || req.y == null || req.z == null)
            throw new IllegalArgumentException("x, y, and z are required");
    }

    private void validatePair(Vector3DPairRequestDto req) {
        if (req == null || req.x1 == null || req.y1 == null || req.z1 == null
                || req.x2 == null || req.y2 == null || req.z2 == null)
            throw new IllegalArgumentException("x1, y1, z1, x2, y2, z2 are required");
    }

    private void validateTriple(Vector3DTripleRequestDto req) {
        if (req == null || req.x1 == null || req.y1 == null || req.z1 == null
                || req.x2 == null || req.y2 == null || req.z2 == null
                || req.x3 == null || req.y3 == null || req.z3 == null)
            throw new IllegalArgumentException("x1, y1, z1, x2, y2, z2, x3, y3, z3 are required");
    }
}
