package com.sheahorn.llmtoolbox.commtools;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/communication/telegram")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TelegramResource {

    @Inject TelegramApi telegramApi;

    @Operation(
        operationId = "communication_send_message",
        summary = "Sends a message to the configured Telegram chat (max 4096 characters)"
    )
    @POST
    @Transactional
    @Path("/comm/sendmessage")
    public Response messageByPost(MessagePayload p) {
        if (p.message == null || p.message.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"message must not be empty\"}")
                    .build();
        }

        if (p.message.length() > TelegramApi.MAX_MESSAGE_LENGTH) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"message exceeds maximum length of " + TelegramApi.MAX_MESSAGE_LENGTH + " characters\"}")
                    .build();
        }

        try {
            telegramApi.send(p.message);
            return Response.ok(p).build();
        } catch (Exception e) {
            telegramApi.logError(e);
            return Response.status(Response.Status.BAD_GATEWAY)
                    .entity("{\"error\":\"failed to send message\"}")
                    .build();
        }
    }

    public static class MessagePayload {
        public String message;
    }

}
