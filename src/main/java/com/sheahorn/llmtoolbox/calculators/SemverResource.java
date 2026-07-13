package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SemverResource {

    private final SemverCalculator calc = new SemverCalculator();

    @Operation(operationId = "calculator_semver_parse", summary = "Parse a semver version into components")
    @POST @Path("/semver-parse")
    public SemverParseResponse semverParse(SemverRequestDto req) {
        if (req == null || req.version == null || req.version.isBlank())
            throw new IllegalArgumentException("version is required");
        SemverParseResponse r = new SemverParseResponse();
        r.operation = "semver_parse";
        SemverCalculator.Parsed p = calc.parse(req.version);
        r.major = p.major;
        r.minor = p.minor;
        r.patch = p.patch;
        r.prerelease = p.prerelease;
        r.build = p.build;
        return r;
    }

    @Operation(operationId = "calculator_semver_compare", summary = "Compare two semver versions: returns -1, 0, or 1")
    @POST @Path("/semver-compare")
    public TextResponse semverCompare(SemverCompareRequestDto req) {
        if (req == null || req.v1 == null || req.v1.isBlank() || req.v2 == null || req.v2.isBlank())
            throw new IllegalArgumentException("v1 and v2 are required");
        TextResponse r = new TextResponse();
        r.operation = "semver_compare";
        r.result = String.valueOf(calc.compare(req.v1, req.v2));
        return r;
    }

    @Operation(operationId = "calculator_semver_satisfies", summary = "Check if a version satisfies a range: exact, ^X.Y.Z, ~X.Y.Z, >=, <=, >, <, or compound")
    @POST @Path("/semver-satisfies")
    public TextResponse semverSatisfies(SemverSatisfiesRequestDto req) {
        if (req == null || req.version == null || req.version.isBlank() || req.range == null || req.range.isBlank())
            throw new IllegalArgumentException("version and range are required");
        TextResponse r = new TextResponse();
        r.operation = "semver_satisfies";
        r.result = String.valueOf(calc.satisfies(req.version, req.range));
        return r;
    }

    @Operation(operationId = "calculator_semver_bumper", summary = "Bump a semver version: major, minor, or patch. Resets prerelease and drops build metadata (per semver spec).")
    @POST @Path("/semver-bumper")
    public TextResponse semverBumper(SemverRequestDto req) {
        if (req == null || req.version == null || req.version.isBlank())
            throw new IllegalArgumentException("version is required");
        if (req.bump == null || req.bump.isBlank())
            throw new IllegalArgumentException("bump is required");
        TextResponse r = new TextResponse();
        r.operation = "semver_bumper";
        r.result = calc.bump(req.version, req.bump);
        return r;
    }
}
