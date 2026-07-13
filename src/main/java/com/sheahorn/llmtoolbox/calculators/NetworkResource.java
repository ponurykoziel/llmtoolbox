package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NetworkResource {

    private final NetworkCalculator calc = new NetworkCalculator();

    @Operation(operationId = "calculator_cidr", summary = "Parse CIDR notation: network address, netmask, wildcard, broadcast, first/last host, count")
    @POST @Path("/cidr")
    public TextResponse cidr(CidrRequestDto req) {
        if (req == null || req.cidr == null || req.cidr.isBlank())
            throw new IllegalArgumentException("cidr is required");
        TextResponse r = new TextResponse();
        r.operation = "cidr";
        r.result = calc.cidr(req.cidr);
        return r;
    }

    @Operation(operationId = "calculator_subnet_info", summary = "Subnet info given IP and mask (prefix like /16, integer like 16, or dotted like 255.255.0.0)")
    @POST @Path("/subnet-info")
    public TextResponse subnetInfo(IpMaskRequestDto req) {
        if (req == null || req.ip == null || req.ip.isBlank() || req.mask == null || req.mask.isBlank())
            throw new IllegalArgumentException("ip and mask are required");
        TextResponse r = new TextResponse();
        r.operation = "subnet_info";
        r.result = calc.subnetInfo(req.ip, req.mask);
        return r;
    }

    @Operation(operationId = "calculator_ip_in_cidr", summary = "Check if an IP address belongs to a CIDR range")
    @POST @Path("/ip-in-cidr")
    public TextResponse ipInCidr(IpCidrRequestDto req) {
        if (req == null || req.ip == null || req.ip.isBlank() || req.cidr == null || req.cidr.isBlank())
            throw new IllegalArgumentException("ip and cidr are required");
        TextResponse r = new TextResponse();
        r.operation = "ip_in_cidr";
        r.result = String.valueOf(calc.ipInCidr(req.ip, req.cidr));
        return r;
    }

    @Operation(operationId = "calculator_subnet_split", summary = "Split a CIDR into smaller subnets with a longer prefix")
    @POST @Path("/subnet-split")
    public TextResponse subnetSplit(SubnetRequestDto req) {
        if (req == null || req.cidr == null || req.cidr.isBlank() || req.newPrefix == null)
            throw new IllegalArgumentException("cidr and newPrefix are required");
        TextResponse r = new TextResponse();
        r.operation = "subnet_split";
        r.result = calc.subnetSplit(req.cidr, req.newPrefix);
        return r;
    }

    @Operation(operationId = "calculator_ip_to_long", summary = "Convert an IPv4 address to its 32-bit unsigned integer representation")
    @POST @Path("/ip-to-long")
    public TextResponse ipToLong(IpRequestDto req) {
        if (req == null || req.ip == null || req.ip.isBlank())
            throw new IllegalArgumentException("ip is required");
        TextResponse r = new TextResponse();
        r.operation = "ip_to_long";
        r.result = calc.ipToLongStr(req.ip);
        return r;
    }

    @Operation(operationId = "calculator_long_to_ip", summary = "Convert a 32-bit unsigned integer to an IPv4 address")
    @POST @Path("/long-to-ip")
    public TextResponse longToIp(IpRequestDto req) {
        if (req == null || req.ip == null || req.ip.isBlank())
            throw new IllegalArgumentException("ip (as long value) is required");
        TextResponse r = new TextResponse();
        r.operation = "long_to_ip";
        r.result = calc.longToIpStr(Long.parseLong(req.ip));
        return r;
    }

    @Operation(operationId = "calculator_ip_validate", summary = "Validate an IPv4 address: returns true or false")
    @POST @Path("/ip-validate")
    public TextResponse ipValidate(IpRequestDto req) {
        if (req == null || req.ip == null || req.ip.isBlank())
            throw new IllegalArgumentException("ip is required");
        TextResponse r = new TextResponse();
        r.operation = "ip_validate";
        r.result = String.valueOf(calc.ipValidate(req.ip));
        return r;
    }

    @Operation(operationId = "calculator_cidr_summarize", summary = "Summarize multiple CIDR ranges into the smallest covering CIDR")
    @POST @Path("/cidr-summarize")
    public TextResponse cidrSummarize(CidrSummarizeRequestDto req) {
        if (req == null || req.cidrs == null || req.cidrs.length == 0)
            throw new IllegalArgumentException("cidrs array is required and must not be empty");
        TextResponse r = new TextResponse();
        r.operation = "cidr_summarize";
        r.result = calc.cidrSummarize(req.cidrs);
        return r;
    }
}
