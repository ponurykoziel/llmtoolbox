package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CryptResource {

    private final CryptCalculator calc = new CryptCalculator();

    @Operation(operationId = "calculator_crypt", summary = "Hash/encode/decode: md2, md5, sha1, sha224, sha256, sha384, sha512, sha512_224, sha512_256, sha3_224, sha3_256, sha3_384, sha3_512, base64_encode, base64_decode, base64url_encode, base64url_decode")
    @POST @Path("/crypt")
    public TextResponse crypt(CryptRequestDto req) {
        if (req == null || req.input == null || req.algo == null || req.algo.isBlank())
            throw new IllegalArgumentException("input and algo are required");
        TextResponse r = new TextResponse();
        r.operation = "crypt";
        r.result = calc.crypt(req.input, req.algo);
        return r;
    }

    @Operation(operationId = "calculator_hmac", summary = "HMAC: hmac_md5, hmac_sha1, hmac_sha224, hmac_sha256, hmac_sha384, hmac_sha512, hmac_sha512_224, hmac_sha512_256")
    @POST @Path("/hmac")
    public TextResponse hmac(HmacRequestDto req) {
        if (req == null || req.input == null || req.key == null || req.key.isBlank() || req.algo == null || req.algo.isBlank())
            throw new IllegalArgumentException("input, key, and algo are required");
        TextResponse r = new TextResponse();
        r.operation = "hmac";
        r.result = calc.hmac(req.input, req.key, req.algo);
        return r;
    }

    @Operation(operationId = "calculator_checksum", summary = "Checksum: crc32, crc32c, adler32, fnv1a_32, joaat")
    @POST @Path("/checksum")
    public TextResponse checksum(ChecksumRequestDto req) {
        if (req == null || req.input == null || req.algo == null || req.algo.isBlank())
            throw new IllegalArgumentException("input and algo are required");
        TextResponse r = new TextResponse();
        r.operation = "checksum";
        r.result = calc.checksum(req.input, req.algo);
        return r;
    }

    @Operation(operationId = "calculator_hex_encode", summary = "Encode text to hex string")
    @POST @Path("/hex-encode")
    public TextResponse hexEncode(HexRequestDto req) {
        if (req == null || req.input == null)
            throw new IllegalArgumentException("input is required");
        TextResponse r = new TextResponse();
        r.operation = "hex_encode";
        r.result = calc.hexEncode(req.input);
        return r;
    }

    @Operation(operationId = "calculator_hex_decode", summary = "Decode hex string to text")
    @POST @Path("/hex-decode")
    public TextResponse hexDecode(HexRequestDto req) {
        if (req == null || req.input == null)
            throw new IllegalArgumentException("input is required");
        TextResponse r = new TextResponse();
        r.operation = "hex_decode";
        r.result = calc.hexDecode(req.input);
        return r;
    }

}
