package com.sheahorn.llmtoolbox.hosttools.audio;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/host/audio")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AudioResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "host_audio_volume_set",
            summary = "Sets the default sink volume to an absolute percent (0-100, clamped)"
    )
    @POST
    @Path("/volume/set")
    public ExecutionResponse setVolume(VolumeSetRequestDto request) {
        if (request == null || request.volume == null) {
            throw new IllegalArgumentException("volume is required");
        }

        int vol = Math.max(0, Math.min(100, request.volume));

        return executor.execute(
                "pactl set-sink-volume @DEFAULT_SINK@ " + vol + "%"
        );
    }

    @Operation(
            operationId = "host_audio_volume_up",
            summary = "Raises the default sink volume by a percent (default 5, clamped 1-100)"
    )
    @POST
    @Path("/volume/up")
    public ExecutionResponse volumeUp(VolumeChangeRequestDto request) {
        int percent = request == null || request.percent == null ? 5 : request.percent;
        percent = Math.max(1, Math.min(100, percent));

        return executor.execute(
                "pactl set-sink-volume @DEFAULT_SINK@ +" + percent + "%"
        );
    }

    @Operation(
            operationId = "host_audio_volume_down",
            summary = "Lowers the default sink volume by a percent (default 5, clamped 1-100)"
    )
    @POST
    @Path("/volume/down")
    public ExecutionResponse volumeDown(VolumeChangeRequestDto request) {
        int percent = request == null || request.percent == null ? 5 : request.percent;
        percent = Math.max(1, Math.min(100, percent));

        return executor.execute(
                "pactl set-sink-volume @DEFAULT_SINK@ -" + percent + "%"
        );
    }

    @Operation(
            operationId = "host_audio_mute",
            summary = "Toggles mute on the default sink"
    )
    @POST
    @Path("/mute")
    public ExecutionResponse mute(AudioRequestDto request) {
        return executor.execute("pactl set-sink-mute @DEFAULT_SINK@ toggle");
    }

    @Operation(
            operationId = "host_audio_play_pause",
            summary = "Toggles play/pause on the active media player via playerctl"
    )
    @POST
    @Path("/play-pause")
    public ExecutionResponse playPause(AudioRequestDto request) {
        return executor.execute("playerctl play-pause");
    }

    @Operation(
            operationId = "host_audio_next",
            summary = "Skips to the next track on the active media player"
    )
    @POST
    @Path("/next")
    public ExecutionResponse next(AudioRequestDto request) {
        return executor.execute("playerctl next");
    }

    @Operation(
            operationId = "host_audio_previous",
            summary = "Returns to the previous track on the active media player"
    )
    @POST
    @Path("/previous")
    public ExecutionResponse previous(AudioRequestDto request) {
        return executor.execute("playerctl previous");
    }
}
