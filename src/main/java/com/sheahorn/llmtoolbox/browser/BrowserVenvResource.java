package com.sheahorn.llmtoolbox.browser;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

/**
 * Streams browser venv initialization (browser/setup.sh) to the dashboard as
 * Server-Sent Events. The venv is created on user request, not automatically.
 */
@Path("/api/tools/browser/venv")
public class BrowserVenvResource {

    @Inject
    BrowserSidecar sidecar;

    @GET
    @Path("/init")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void init(@Context SseEventSink sink, @Context Sse sse) {
        if (sidecar.isVenvPresent()) {
            send(sink, sse, "done", "venv already present");
            sink.close();
            return;
        }

        if (!sidecar.beginInit()) {
            send(sink, sse, "done", "initialization already in progress");
            sink.close();
            return;
        }

        Thread t = new Thread(() -> {
            try {
                sidecar.initializeVenv(line -> send(sink, sse, "log", line));
                send(sink, sse, "done", sidecar.isReady() ? "venv ready" : "venv created");
            } catch (Exception e) {
                send(sink, sse, "done", "error: " + e.getMessage());
            } finally {
                sink.close();
            }
        }, "browser-venv-init");
        t.setDaemon(true);
        t.start();
    }

    private void send(SseEventSink sink, Sse sse, String name, String data) {
        try {
            sink.send(sse.newEventBuilder().name(name).data(data).build());
        } catch (Exception e) {
            // Client disconnected — ignore
        }
    }
}
