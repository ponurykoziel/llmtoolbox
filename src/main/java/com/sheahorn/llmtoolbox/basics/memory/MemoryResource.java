package com.sheahorn.llmtoolbox.basics.memory;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/tools/basics/memory")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MemoryResource {

    @Operation(
            operationId = "memory_add",
            summary = "Adds a new memory entry (max 65536 characters)"
    )
    @POST
    @Path("/add")
    @Transactional
    public MemoryEntryResponse add(MemoryAddRequest request) {
        if (request == null || request.content == null || request.content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }

        if (request.content.length() > 65536) {
            throw new IllegalArgumentException("content exceeds maximum length of 65536 characters");
        }

        MemoryEntry entry = MemoryEntry.create(request.content);
        entry.persist();
        return MemoryEntryResponse.from(entry);
    }

    @Operation(
            operationId = "memory_get_by_id",
            summary = "Returns a single memory entry by its ID"
    )
    @GET
    @Path("/{id}")
    public MemoryEntryResponse getById(@PathParam("id") Long id) {
        MemoryEntry entry = MemoryEntry.findById(id);
        if (entry == null) {
            throw new NotFoundException("memory entry not found: " + id);
        }
        return MemoryEntryResponse.from(entry);
    }

    @Operation(
            operationId = "memory_show_all",
            summary = "Returns all memory entries, newest first"
    )
    @GET
    @Path("/all")
    public List<MemoryEntryResponse> showAll() {
        return MemoryEntry.findAll()
                .<MemoryEntry>stream()
                .map(MemoryEntryResponse::from)
                .collect(Collectors.toList());
    }

    @Operation(
            operationId = "memory_find",
            summary = "Finds memory entries whose content contains the given query string (case-insensitive)"
    )
    @POST
    @Path("/find")
    public List<MemoryEntryResponse> find(MemoryFindRequest request) {
        if (request == null || request.query == null || request.query.isBlank()) {
            throw new IllegalArgumentException("query is required");
        }

        return MemoryEntry.find("LOWER(content) LIKE ?1 ORDER BY timestamp DESC",
                        "%" + request.query.toLowerCase() + "%")
                .<MemoryEntry>stream()
                .map(MemoryEntryResponse::from)
                .collect(Collectors.toList());
    }

    @Operation(
            operationId = "memory_remove",
            summary = "Deletes a memory entry by its ID"
    )
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response remove(@PathParam("id") Long id) {
        boolean deleted = MemoryEntry.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("memory entry not found: " + id);
        }
        return Response.ok("{\"deleted\":" + id + "}").build();
    }
}
