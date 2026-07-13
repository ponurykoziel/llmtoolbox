package com.sheahorn.llmtoolbox.hosttools.hardware;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/host/hardware")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HardwareResource {

    @Inject
    Executor executor;

    @Operation(
            operationId = "host_hardware_lscpu",
            summary = "Shows CPU architecture and topology info via `lscpu`"
    )
    @POST
    @Path("/lscpu")
    public ExecutionResponse lscpu(HardwareRequestDto request) {
        return executor.execute("lscpu");
    }

    @Operation(
            operationId = "host_hardware_lsmem",
            summary = "Shows memory hierarchy/topology info via `lsmem`"
    )
    @POST
    @Path("/lsmem")
    public ExecutionResponse lsmem(HardwareRequestDto request) {
        return executor.execute("lsmem");
    }

    @Operation(
            operationId = "host_hardware_lsusb",
            summary = "Lists USB devices via `lsusb`"
    )
    @POST
    @Path("/lsusb")
    public ExecutionResponse lsusb(HardwareRequestDto request) {
        return executor.execute("lsusb");
    }

    @Operation(
            operationId = "host_hardware_lspci",
            summary = "Lists PCI devices via `lspci`"
    )
    @POST
    @Path("/lspci")
    public ExecutionResponse lspci(HardwareRequestDto request) {
        return executor.execute("lspci");
    }

    @Operation(
            operationId = "host_hardware_sensors",
            summary = "Reads temperatures, voltages, and fan speeds via `sensors` (lm-sensors)"
    )
    @POST
    @Path("/sensors")
    public ExecutionResponse sensors(HardwareRequestDto request) {
        return executor.execute(
                "if command -v sensors >/dev/null 2>&1; then "
                        + "sensors; "
                        + "else "
                        + "echo 'lm-sensors not installed'; "
                        + "fi"
        );
    }

    @Operation(
            operationId = "host_hardware_nvidia_smi",
            summary = "Dumps GPU status for all NVIDIA devices via `nvidia-smi`"
    )
    @POST
    @Path("/nvidia-smi")
    public ExecutionResponse nvidiaSmi(HardwareRequestDto request) {
        return executor.execute("nvidia-smi");
    }
}
