package com.sheahorn.llmtoolbox.basics.notes;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/tools/basics/notes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NoteResource {

    @Operation(
            operationId = "notes_create",
            summary = "Creates a new note (title max 1024, content max 65536 characters)"
    )
    @POST
    @Path("/create")
    @Transactional
    public NoteResponse create(NoteRequest request) {
        if (request == null || request.title == null || request.title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }

        if (request.title.length() > 1024) {
            throw new IllegalArgumentException("title exceeds maximum length of 1024 characters");
        }

        if (request.content != null && request.content.length() > 65536) {
            throw new IllegalArgumentException("content exceeds maximum length of 65536 characters");
        }

        Note note = new Note();
        note.title = request.title;
        note.content = request.content == null ? "" : request.content;
        note.persist();
        return NoteResponse.from(note);
    }

    @Operation(
            operationId = "notes_read",
            summary = "Returns a single note by its ID"
    )
    @GET
    @Path("/{id}")
    public NoteResponse read(@PathParam("id") Long id) {
        Note note = Note.findById(id);
        if (note == null) {
            throw new NotFoundException("note not found: " + id);
        }
        return NoteResponse.from(note);
    }

    @Operation(
            operationId = "notes_update",
            summary = "Updates an existing note's title and/or content"
    )
    @PUT
    @Path("/{id}")
    @Transactional
    public NoteResponse update(@PathParam("id") Long id, NoteRequest request) {
        Note note = Note.findById(id);
        if (note == null) {
            throw new NotFoundException("note not found: " + id);
        }

        if (request.title != null && !request.title.isBlank()) {
            if (request.title.length() > 1024) {
                throw new IllegalArgumentException("title exceeds maximum length of 1024 characters");
            }
            note.title = request.title;
        }

        if (request.content != null) {
            if (request.content.length() > 65536) {
                throw new IllegalArgumentException("content exceeds maximum length of 65536 characters");
            }
            note.content = request.content;
        }

        note.persist();
        return NoteResponse.from(note);
    }

    @Operation(
            operationId = "notes_delete",
            summary = "Deletes a note by its ID"
    )
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = Note.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("note not found: " + id);
        }
        return Response.ok("{\"deleted\":" + id + "}").build();
    }

    @Operation(
            operationId = "notes_find",
            summary = "Finds notes whose title or content contains the query string (case-insensitive)"
    )
    @POST
    @Path("/find")
    public List<NoteResponse> find(NoteFindRequest request) {
        if (request == null || request.query == null || request.query.isBlank()) {
            throw new IllegalArgumentException("query is required");
        }

        String pattern = "%" + request.query.toLowerCase() + "%";
        return Note.find("LOWER(title) LIKE ?1 OR LOWER(content) LIKE ?1 ORDER BY id DESC", pattern)
                .<Note>stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    @Operation(
            operationId = "notes_list_all",
            summary = "Returns all notes, newest first"
    )
    @GET
    @Path("/all")
    public List<NoteResponse> listAll() {
        return Note.findAll()
                .<Note>stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }
}
