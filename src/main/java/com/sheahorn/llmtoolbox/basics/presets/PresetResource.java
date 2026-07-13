package com.sheahorn.llmtoolbox.basics.presets;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.*;
import java.util.stream.Collectors;

@Path("/api/tools/basics/presets")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PresetResource {

    @Operation(
            operationId = "presets_list_all",
            summary = "Lists all presets (DB entries shadow hardcoded defaults)"
    )
    @GET
    @Path("/all")
    public List<PresetResponse> listAll() {
        Set<String> names = PresetDefaults.allNames();
        return names.stream()
                .map(name -> {
                    List<String> prefixes = PresetDefaults.resolve(name);
                    return new PresetResponse(name, prefixes);
                })
                .collect(Collectors.toList());
    }

    @Operation(
            operationId = "presets_get",
            summary = "Returns a single preset by name (DB first, then hardcoded default)"
    )
    @GET
    @Path("/{name}")
    public PresetResponse get(@PathParam("name") String name) {
        List<String> prefixes = PresetDefaults.resolve(name);
        if (prefixes == null) {
            throw new NotFoundException("preset not found: " + name);
        }
        return new PresetResponse(name, prefixes);
    }

    @Operation(
            operationId = "presets_create",
            summary = "Creates a new preset in the database (name max 128, prefixes max 4096 characters)"
    )
    @POST
    @Path("/create")
    @Transactional
    public PresetResponse create(PresetRequest request) {
        if (request == null || request.name == null || request.name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        if (request.name.length() > 128) {
            throw new IllegalArgumentException("name exceeds maximum length of 128 characters");
        }

        if (request.prefixes == null || request.prefixes.isBlank()) {
            throw new IllegalArgumentException("prefixes is required");
        }

        if (request.prefixes.length() > 4096) {
            throw new IllegalArgumentException("prefixes exceeds maximum length of 4096 characters");
        }

        if (Preset.byName(request.name).isPresent()) {
            throw new IllegalArgumentException("preset already exists: " + request.name);
        }

        Preset preset = new Preset();
        preset.name = request.name.strip();
        preset.prefixes = request.prefixes.strip();
        preset.persist();
        return new PresetResponse(preset.name, preset.prefixList());
    }

    @Operation(
            operationId = "presets_patch",
            summary = "Patches a preset's name and/or prefixes (DB presets only)"
    )
    @PATCH
    @Path("/{name}")
    @Transactional
    public PresetResponse patch(@PathParam("name") String name, PresetRequest request) {
        Preset preset = Preset.byName(name)
                .orElseThrow(() -> new NotFoundException("preset not found: " + name));

        if (request.name != null && !request.name.isBlank() && !request.name.equals(preset.name)) {
            String newName = request.name.strip();
            if (newName.length() > 128) {
                throw new IllegalArgumentException("name exceeds maximum length of 128 characters");
            }
            if (Preset.byName(newName).isPresent()) {
                throw new IllegalArgumentException("preset already exists: " + newName);
            }
            preset.name = newName;
        }

        if (request.prefixes != null && !request.prefixes.isBlank()) {
            String newPrefixes = request.prefixes.strip();
            if (newPrefixes.length() > 4096) {
                throw new IllegalArgumentException("prefixes exceeds maximum length of 4096 characters");
            }
            preset.prefixes = newPrefixes;
        }

        preset.persist();
        return new PresetResponse(preset.name, preset.prefixList());
    }

    @Operation(
            operationId = "presets_delete",
            summary = "Deletes a preset from the database (hardcoded defaults cannot be deleted)"
    )
    @DELETE
    @Path("/{name}")
    @Transactional
    public Response delete(@PathParam("name") String name) {
        if (PresetDefaults.isHardcoded(name)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"cannot delete hardcoded preset: " + name + "\"}")
                    .build();
        }

        Preset preset = Preset.byName(name)
                .orElseThrow(() -> new NotFoundException("preset not found: " + name));
        preset.delete();
        return Response.ok("{\"deleted\":\"" + name + "\"}").build();
    }
}
