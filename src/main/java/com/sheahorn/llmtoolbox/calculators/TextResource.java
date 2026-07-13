package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TextResource {

    private final TextCalculator calc;

    public TextResource(
            @ConfigProperty(name = "mfop.calculator.rounding-digits", defaultValue = "8") int roundingDigits) {
        this.calc = new TextCalculator(roundingDigits);
    }

    @Operation(operationId = "calculator_levenshtein", summary = "Levenshtein edit distance between two strings")
    @POST @Path("/levenshtein")
    public AggregateResponse levenshtein(TextCompareRequestDto req) {
        if (req == null || req.a == null || req.b == null)
            throw new IllegalArgumentException("a and b are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "levenshtein";
        r.result = calc.levenshtein(req.a, req.b, req.normalization);
        return r;
    }

    @Operation(operationId = "calculator_normalized_levenshtein", summary = "Normalized Levenshtein similarity (0.0–1.0): 1 - distance / max(len)")
    @POST @Path("/normalized-levenshtein")
    public AggregateResponse normalizedLevenshtein(TextCompareRequestDto req) {
        if (req == null || req.a == null || req.b == null)
            throw new IllegalArgumentException("a and b are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "normalized_levenshtein";
        r.result = calc.normalizedLevenshtein(req.a, req.b, req.normalization);
        return r;
    }

    @Operation(operationId = "calculator_jaccard", summary = "Jaccard similarity of two strings (character sets)")
    @POST @Path("/jaccard")
    public AggregateResponse jaccard(TextCompareRequestDto req) {
        if (req == null || req.a == null || req.b == null)
            throw new IllegalArgumentException("a and b are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "jaccard";
        r.result = calc.jaccard(req.a, req.b, req.normalization);
        return r;
    }

    @Operation(operationId = "calculator_ngram_overlap", summary = "N-gram overlap similarity between two strings")
    @POST @Path("/ngram-overlap")
    public AggregateResponse ngramOverlap(TextCompareRequestDto req) {
        if (req == null || req.a == null || req.b == null)
            throw new IllegalArgumentException("a and b are required");
        int n = req.n != null ? req.n : 2;
        if (n < 1) throw new IllegalArgumentException("n must be >= 1");
        AggregateResponse r = new AggregateResponse();
        r.operation = "ngram_overlap";
        r.result = calc.ngramOverlap(req.a, req.b, n, req.normalization);
        return r;
    }

    @Operation(operationId = "calculator_fuzzy_match", summary = "Fuzzy match: returns true if Levenshtein distance <= threshold (default 2)")
    @POST @Path("/fuzzy-match")
    public AggregateResponse fuzzyMatch(TextCompareRequestDto req) {
        if (req == null || req.a == null || req.b == null)
            throw new IllegalArgumentException("a and b are required");
        int threshold = req.n != null ? req.n : 2;
        AggregateResponse r = new AggregateResponse();
        r.operation = "fuzzy_match";
        r.result = calc.fuzzyMatch(req.a, req.b, threshold, req.normalization);
        return r;
    }

    @Operation(operationId = "calculator_count_occurrences", summary = "Count non-overlapping occurrences of a substring in text")
    @POST @Path("/count-occurrences")
    public AggregateResponse countOccurrences(TextCompareRequestDto req) {
        if (req == null || req.a == null || req.b == null)
            throw new IllegalArgumentException("a (text) and b (substring) are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "count_occurrences";
        r.result = (double) calc.countOccurrences(req.a, req.b, req.normalization);
        return r;
    }

    @Operation(operationId = "calculator_diff", summary = "Line-by-line diff between two strings with optional normalization. Lines prefixed with '  ' (unchanged), '- ' (removed), '+ ' (added)")
    @POST @Path("/diff")
    public TextResponse diff(TextCompareRequestDto req) {
        if (req == null || req.a == null || req.b == null)
            throw new IllegalArgumentException("a and b are required");
        TextResponse r = new TextResponse();
        r.operation = "diff";
        r.result = calc.diff(req.a, req.b, req.normalization);
        return r;
    }

    @Operation(operationId = "calculator_diff_raw", summary = "Raw line-by-line diff between two strings with no normalization. Lines prefixed with '  ' (unchanged), '- ' (removed), '+ ' (added)")
    @POST @Path("/diff-raw")
    public TextResponse diffRaw(TextCompareRequestDto req) {
        if (req == null || req.a == null || req.b == null)
            throw new IllegalArgumentException("a and b are required");
        TextResponse r = new TextResponse();
        r.operation = "diff_raw";
        r.result = calc.diffRaw(req.a, req.b);
        return r;
    }
}
