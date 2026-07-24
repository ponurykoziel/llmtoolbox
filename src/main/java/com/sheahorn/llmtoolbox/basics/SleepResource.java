package com.sheahorn.llmtoolbox.basics;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path("/api/tools/basics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SleepResource {

    @Operation(
            operationId = "sleep",
            summary = "Sleeps for the given number of seconds, returning the from/to timestamps"
    )
    @POST
    @Path("/sleep")
    public SleepResponse sleep(
            @Parameter(description = "Number of seconds to sleep", required = true)
            @QueryParam("seconds") int seconds) {

        ZonedDateTime from = ZonedDateTime.now(ZoneOffset.UTC);

        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ZonedDateTime to = ZonedDateTime.now(ZoneOffset.UTC);

        SleepResponse r = new SleepResponse();
        r.from = from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        r.to = to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return r;
    }
}
