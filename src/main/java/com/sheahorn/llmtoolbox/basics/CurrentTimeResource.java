package com.sheahorn.llmtoolbox.basics;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/basics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CurrentTimeResource {

    @Operation(
            operationId = "time_now",
            summary = "Returns the current UTC date and time"
    )
    @GET
    @Path("/current-time")
    public CurrentTimeResponse currentTime() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        CurrentTimeResponse r = new CurrentTimeResponse();
        r.iso = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        r.human = now.format(DateTimeFormatter.ofPattern("dd MM yyyy HH mm ss EEEE"));
        r.year = now.getYear();
        r.month = now.getMonthValue();
        r.day = now.getDayOfMonth();
        r.hour = now.getHour();
        r.minute = now.getMinute();
        r.second = now.getSecond();
        r.dayOfWeek = now.getDayOfWeek().toString();
        r.epochSecond = now.toEpochSecond();
        return r;
    }
}
