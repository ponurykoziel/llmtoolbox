package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegexResource {

    private final RegexCalculator calc = new RegexCalculator();

    @Operation(operationId = "calculator_regex_tester", summary = "Test a regex pattern against text, returns matches with positions and groups")
    @POST @Path("/regex-tester")
    public TextResponse regexTester(RegexRequestDto req) {
        if (req == null || req.pattern == null || req.pattern.isBlank())
            throw new IllegalArgumentException("pattern is required");
        if (req.text == null)
            throw new IllegalArgumentException("text is required");
        TextResponse r = new TextResponse();
        r.operation = "regex_tester";
        r.result = calc.regexTester(req.pattern, req.text, req.flags);
        return r;
    }

    @Operation(operationId = "calculator_regex_replace", summary = "Replace regex matches in text: replace first or all")
    @POST @Path("/regex-replace")
    public TextResponse regexReplace(RegexReplaceRequestDto req) {
        if (req == null || req.pattern == null || req.pattern.isBlank())
            throw new IllegalArgumentException("pattern is required");
        if (req.text == null)
            throw new IllegalArgumentException("text is required");
        TextResponse r = new TextResponse();
        r.operation = "regex_replace";
        r.result = calc.regexReplace(req.pattern, req.text, req.replacement, req.flags, true);
        return r;
    }

    @Operation(operationId = "calculator_regex_replace_first", summary = "Replace first regex match in text")
    @POST @Path("/regex-replace-first")
    public TextResponse regexReplaceFirst(RegexReplaceRequestDto req) {
        if (req == null || req.pattern == null || req.pattern.isBlank())
            throw new IllegalArgumentException("pattern is required");
        if (req.text == null)
            throw new IllegalArgumentException("text is required");
        TextResponse r = new TextResponse();
        r.operation = "regex_replace_first";
        r.result = calc.regexReplace(req.pattern, req.text, req.replacement, req.flags, false);
        return r;
    }

    @Operation(operationId = "calculator_regex_validate", summary = "Validate a regex pattern: returns 'valid' or error message")
    @POST @Path("/regex-validate")
    public TextResponse regexValidate(RegexValidateRequestDto req) {
        if (req == null || req.pattern == null || req.pattern.isBlank())
            throw new IllegalArgumentException("pattern is required");
        TextResponse r = new TextResponse();
        r.operation = "regex_validate";
        r.result = calc.regexValidate(req.pattern, req.flags);
        return r;
    }

    @Operation(operationId = "calculator_regex_split", summary = "Split text by regex pattern, returns indexed parts; optional limit controls max splits")
    @POST @Path("/regex-split")
    public TextResponse regexSplit(RegexSplitRequestDto req) {
        if (req == null || req.pattern == null || req.pattern.isBlank())
            throw new IllegalArgumentException("pattern is required");
        if (req.text == null)
            throw new IllegalArgumentException("text is required");
        TextResponse r = new TextResponse();
        r.operation = "regex_split";
        r.result = calc.regexSplit(req.pattern, req.text, req.flags, req.limit);
        return r;
    }

    @Operation(operationId = "calculator_regex_escape", summary = "Escape literal text for use as a regex pattern")
    @POST @Path("/regex-escape")
    public TextResponse regexEscape(RegexRequestDto req) {
        if (req == null || req.text == null)
            throw new IllegalArgumentException("text is required");
        TextResponse r = new TextResponse();
        r.operation = "regex_escape";
        r.result = calc.regexEscape(req.text);
        return r;
    }
}
