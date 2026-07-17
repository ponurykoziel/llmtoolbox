package com.sheahorn.llmtoolbox.buildtools.docker;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/devops/docker")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DockerResource {

    @Inject
    Executor executor;

    @Inject
    DockerLockService lock;

    // ── info ─────────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_version",
            summary = "Runs `docker version`"
    )
    @POST
    @Path("/version")
    public ExecutionResponse version(DockerRequestDto request) throws Exception {
        return run("docker version");
    }

    @Operation(
            operationId = "devops_docker_info",
            summary = "Runs `docker info`"
    )
    @POST
    @Path("/info")
    public ExecutionResponse info(DockerRequestDto request) throws Exception {
        return run("docker info");
    }

    // ── containers: list / inspect ────────────────────────────

    @Operation(
            operationId = "devops_docker_ps",
            summary = "Runs `docker ps -a` to list all containers"
    )
    @POST
    @Path("/ps")
    public ExecutionResponse ps(DockerRequestDto request) throws Exception {
        return run("docker ps -a");
    }

    @Operation(
            operationId = "devops_docker_ps_running",
            summary = "Runs `docker ps` to list running containers only"
    )
    @POST
    @Path("/ps-running")
    public ExecutionResponse psRunning(DockerRequestDto request) throws Exception {
        return run("docker ps");
    }

    @Operation(
            operationId = "devops_docker_inspect",
            summary = "Runs `docker inspect <container>`"
    )
    @POST
    @Path("/inspect")
    public ExecutionResponse inspect(DockerRequestDto request) throws Exception {
        validateContainer(request);
        return run("docker inspect " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_top",
            summary = "Runs `docker top <container>` to show processes"
    )
    @POST
    @Path("/top")
    public ExecutionResponse top(DockerRequestDto request) throws Exception {
        validateContainer(request);
        return run("docker top " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_stats",
            summary = "Runs `docker stats --no-stream` (one-shot) for all containers, or a single container if specified"
    )
    @POST
    @Path("/stats")
    public ExecutionResponse stats(DockerRequestDto request) throws Exception {
        if (request != null && request.container != null && !request.container.isBlank()) {
            return run("docker stats --no-stream " + ToolSupport.shellQuote(request.container));
        }
        return run("docker stats --no-stream");
    }

    @Operation(
            operationId = "devops_docker_port",
            summary = "Runs `docker port <container>` to show port mappings"
    )
    @POST
    @Path("/port")
    public ExecutionResponse port(DockerRequestDto request) throws Exception {
        validateContainer(request);
        return run("docker port " + ToolSupport.shellQuote(request.container));
    }

    // ── logs ─────────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_logs",
            summary = "Runs `docker logs <container>` — requires container field"
    )
    @POST
    @Path("/logs")
    public ExecutionResponse logs(DockerRequestDto request) throws Exception {
        validateContainer(request);
        StringBuilder cmd = new StringBuilder("docker logs");
        if (request != null && request.tail != null && !request.tail.isBlank()) {
            cmd.append(" --tail ").append(ToolSupport.shellQuote(request.tail));
        }
        cmd.append(" ").append(ToolSupport.shellQuote(request.container));
        return run(cmd.toString());
    }

    // ── lifecycle ────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_run",
            summary = "Runs `docker run -d <image>` with optional flags (name, port, env, volume, network, hostname, restart, rm)"
    )
    @POST
    @Path("/run")
    public ExecutionResponse runContainer(DockerRequestDto request) throws Exception {
        if (request == null || request.image == null || request.image.isBlank()) {
            throw new IllegalArgumentException("image is required");
        }
        StringBuilder cmd = new StringBuilder("docker run -d");
        if (request.name != null && !request.name.isBlank()) {
            cmd.append(" --name ").append(ToolSupport.shellQuote(request.name));
        }
        if (request.port != null && !request.port.isBlank()) {
            cmd.append(" -p ").append(ToolSupport.shellQuote(request.port));
        }
        if (request.env != null && !request.env.isBlank()) {
            cmd.append(" -e ").append(ToolSupport.shellQuote(request.env));
        }
        if (request.volume != null && !request.volume.isBlank()) {
            cmd.append(" -v ").append(ToolSupport.shellQuote(request.volume));
        }
        if (request.network != null && !request.network.isBlank()) {
            cmd.append(" --network ").append(ToolSupport.shellQuote(request.network));
        }
        if (request.hostname != null && !request.hostname.isBlank()) {
            cmd.append(" --hostname ").append(ToolSupport.shellQuote(request.hostname));
        }
        if (request.restart != null && !request.restart.isBlank()) {
            cmd.append(" --restart ").append(ToolSupport.shellQuote(request.restart));
        }
        if (Boolean.TRUE.equals(request.rm)) {
            cmd.append(" --rm");
        }
        cmd.append(" ").append(ToolSupport.shellQuote(request.image));
        if (request.command != null && !request.command.isBlank()) {
            cmd.append(" ").append(request.command);
        }
        return run(cmd.toString());
    }

    @Operation(
            operationId = "devops_docker_start",
            summary = "Runs `docker start <container>`"
    )
    @POST
    @Path("/start")
    public ExecutionResponse start(DockerRequestDto request) throws Exception {
        validateContainer(request);
        return run("docker start " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_stop",
            summary = "Runs `docker stop <container>`"
    )
    @POST
    @Path("/stop")
    public ExecutionResponse stop(DockerRequestDto request) throws Exception {
        validateContainer(request);
        return run("docker stop " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_restart",
            summary = "Runs `docker restart <container>`"
    )
    @POST
    @Path("/restart")
    public ExecutionResponse restart(DockerRequestDto request) throws Exception {
        validateContainer(request);
        return run("docker restart " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_pause",
            summary = "Runs `docker pause <container>`"
    )
    @POST
    @Path("/pause")
    public ExecutionResponse pause(DockerRequestDto request) throws Exception {
        validateContainer(request);
        return run("docker pause " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_unpause",
            summary = "Runs `docker unpause <container>`"
    )
    @POST
    @Path("/unpause")
    public ExecutionResponse unpause(DockerRequestDto request) throws Exception {
        validateContainer(request);
        return run("docker unpause " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_rename",
            summary = "Runs `docker rename <container> <name>` — container is old name, name is new name"
    )
    @POST
    @Path("/rename")
    public ExecutionResponse rename(DockerRequestDto request) throws Exception {
        validateContainer(request);
        if (request.name == null || request.name.isBlank()) {
            throw new IllegalArgumentException("name (new name) is required");
        }
        return run("docker rename " + ToolSupport.shellQuote(request.container)
                + " " + ToolSupport.shellQuote(request.name));
    }

    // ── removal ──────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_rm",
            summary = "Runs `docker rm <container>`"
    )
    @POST
    @Path("/rm")
    public ExecutionResponse rm(DockerRequestDto request) throws Exception {
        validateContainer(request);
        String flag = Boolean.TRUE.equals(request.force) ? " -f" : "";
        return run("docker rm" + flag + " " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_rmi",
            summary = "Runs `docker rmi <image>`"
    )
    @POST
    @Path("/rmi")
    public ExecutionResponse rmi(DockerRequestDto request) throws Exception {
        if (request == null || request.image == null || request.image.isBlank()) {
            throw new IllegalArgumentException("image is required");
        }
        String flag = Boolean.TRUE.equals(request.force) ? " -f" : "";
        return run("docker rmi" + flag + " " + ToolSupport.shellQuote(request.image));
    }

    @Operation(
            operationId = "devops_docker_system_prune",
            summary = "Runs `docker system prune -f` to remove unused data"
    )
    @POST
    @Path("/system-prune")
    public ExecutionResponse systemPrune(DockerRequestDto request) throws Exception {
        return run("docker system prune -f");
    }

    // ── images ───────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_images",
            summary = "Runs `docker images` to list all images"
    )
    @POST
    @Path("/images")
    public ExecutionResponse images(DockerRequestDto request) throws Exception {
        return run("docker images");
    }

    @Operation(
            operationId = "devops_docker_pull",
            summary = "Runs `docker pull <image>`"
    )
    @POST
    @Path("/pull")
    public ExecutionResponse pull(DockerRequestDto request) throws Exception {
        if (request == null || request.image == null || request.image.isBlank()) {
            throw new IllegalArgumentException("image is required");
        }
        return run("docker pull " + ToolSupport.shellQuote(request.image));
    }

    @Operation(
            operationId = "devops_docker_push",
            summary = "Runs `docker push <image>`"
    )
    @POST
    @Path("/push")
    public ExecutionResponse push(DockerRequestDto request) throws Exception {
        if (request == null || request.image == null || request.image.isBlank()) {
            throw new IllegalArgumentException("image is required");
        }
        return run("docker push " + ToolSupport.shellQuote(request.image));
    }

    @Operation(
            operationId = "devops_docker_build",
            summary = "Runs `docker build -t <name> .` — name is required"
    )
    @POST
    @Path("/build")
    public ExecutionResponse build(DockerRequestDto request) throws Exception {
        if (request == null || request.name == null || request.name.isBlank()) {
            throw new IllegalArgumentException("name (tag) is required");
        }
        return run("docker build -t " + ToolSupport.shellQuote(request.name) + " .");
    }

    @Operation(
            operationId = "devops_docker_tag",
            summary = "Runs `docker tag <source> <target>` — source and target fields required"
    )
    @POST
    @Path("/tag")
    public ExecutionResponse tag(DockerRequestDto request) throws Exception {
        if (request == null || request.source == null || request.source.isBlank()) {
            throw new IllegalArgumentException("source is required");
        }
        if (request.target == null || request.target.isBlank()) {
            throw new IllegalArgumentException("target is required");
        }
        return run("docker tag " + ToolSupport.shellQuote(request.source)
                + " " + ToolSupport.shellQuote(request.target));
    }

    @Operation(
            operationId = "devops_docker_history",
            summary = "Runs `docker history <image>`"
    )
    @POST
    @Path("/history")
    public ExecutionResponse history(DockerRequestDto request) throws Exception {
        if (request == null || request.image == null || request.image.isBlank()) {
            throw new IllegalArgumentException("image is required");
        }
        return run("docker history " + ToolSupport.shellQuote(request.image));
    }

    // ── exec / cp ────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_exec",
            summary = "Runs `docker exec <container> <command>` — container and command required"
    )
    @POST
    @Path("/exec")
    public ExecutionResponse exec(DockerRequestDto request) throws Exception {
        validateContainer(request);
        if (request.command == null || request.command.isBlank()) {
            throw new IllegalArgumentException("command is required");
        }
        return run("docker exec " + ToolSupport.shellQuote(request.container)
                + " " + request.command);
    }

    @Operation(
            operationId = "devops_docker_cp",
            summary = "Runs `docker cp <source> <target>` — source and target fields required (container paths use <container>:<path> syntax)"
    )
    @POST
    @Path("/cp")
    public ExecutionResponse cp(DockerRequestDto request) throws Exception {
        if (request == null || request.source == null || request.source.isBlank()) {
            throw new IllegalArgumentException("source is required");
        }
        if (request.target == null || request.target.isBlank()) {
            throw new IllegalArgumentException("target is required");
        }
        return run("docker cp " + ToolSupport.shellQuote(request.source)
                + " " + ToolSupport.shellQuote(request.target));
    }

    // ── networks ─────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_network_ls",
            summary = "Runs `docker network ls`"
    )
    @POST
    @Path("/network-ls")
    public ExecutionResponse networkLs(DockerRequestDto request) throws Exception {
        return run("docker network ls");
    }

    @Operation(
            operationId = "devops_docker_network_inspect",
            summary = "Runs `docker network inspect <network>` — network field required"
    )
    @POST
    @Path("/network-inspect")
    public ExecutionResponse networkInspect(DockerRequestDto request) throws Exception {
        if (request == null || request.network == null || request.network.isBlank()) {
            throw new IllegalArgumentException("network is required");
        }
        return run("docker network inspect " + ToolSupport.shellQuote(request.network));
    }

    // ── volumes ──────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_volume_ls",
            summary = "Runs `docker volume ls`"
    )
    @POST
    @Path("/volume-ls")
    public ExecutionResponse volumeLs(DockerRequestDto request) throws Exception {
        return run("docker volume ls");
    }

    @Operation(
            operationId = "devops_docker_volume_inspect",
            summary = "Runs `docker volume inspect <volume>` — volume field required"
    )
    @POST
    @Path("/volume-inspect")
    public ExecutionResponse volumeInspect(DockerRequestDto request) throws Exception {
        if (request == null || request.volume == null || request.volume.isBlank()) {
            throw new IllegalArgumentException("volume is required");
        }
        return run("docker volume inspect " + ToolSupport.shellQuote(request.volume));
    }

    // ── compose ──────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_compose_up",
            summary = "Runs `docker compose up -d` — optional composeFile path and composeService name"
    )
    @POST
    @Path("/compose-up")
    public ExecutionResponse composeUp(DockerRequestDto request) throws Exception {
        StringBuilder cmd = composeBase(request);
        cmd.append(" up -d");
        if (request != null && request.composeService != null && !request.composeService.isBlank()) {
            cmd.append(" ").append(ToolSupport.shellQuote(request.composeService));
        }
        return run(cmd.toString());
    }

    @Operation(
            operationId = "devops_docker_compose_down",
            summary = "Runs `docker compose down` — optional composeFile path"
    )
    @POST
    @Path("/compose-down")
    public ExecutionResponse composeDown(DockerRequestDto request) throws Exception {
        StringBuilder cmd = composeBase(request);
        cmd.append(" down");
        return run(cmd.toString());
    }

    @Operation(
            operationId = "devops_docker_compose_ps",
            summary = "Runs `docker compose ps` — optional composeFile path"
    )
    @POST
    @Path("/compose-ps")
    public ExecutionResponse composePs(DockerRequestDto request) throws Exception {
        StringBuilder cmd = composeBase(request);
        cmd.append(" ps");
        return run(cmd.toString());
    }

    @Operation(
            operationId = "devops_docker_compose_logs",
            summary = "Runs `docker compose logs` — optional composeFile path and composeService name"
    )
    @POST
    @Path("/compose-logs")
    public ExecutionResponse composeLogs(DockerRequestDto request) throws Exception {
        StringBuilder cmd = composeBase(request);
        cmd.append(" logs");
        if (request != null && request.tail != null && !request.tail.isBlank()) {
            cmd.append(" --tail ").append(ToolSupport.shellQuote(request.tail));
        }
        if (request != null && request.composeService != null && !request.composeService.isBlank()) {
            cmd.append(" ").append(ToolSupport.shellQuote(request.composeService));
        }
        return run(cmd.toString());
    }

    @Operation(
            operationId = "devops_docker_compose_restart",
            summary = "Runs `docker compose restart` — optional composeFile path and composeService name"
    )
    @POST
    @Path("/compose-restart")
    public ExecutionResponse composeRestart(DockerRequestDto request) throws Exception {
        StringBuilder cmd = composeBase(request);
        cmd.append(" restart");
        if (request != null && request.composeService != null && !request.composeService.isBlank()) {
            cmd.append(" ").append(ToolSupport.shellQuote(request.composeService));
        }
        return run(cmd.toString());
    }

    @Operation(
            operationId = "devops_docker_compose_build",
            summary = "Runs `docker compose build` — optional composeFile path and composeService name"
    )
    @POST
    @Path("/compose-build")
    public ExecutionResponse composeBuild(DockerRequestDto request) throws Exception {
        StringBuilder cmd = composeBase(request);
        cmd.append(" build");
        if (request != null && request.composeService != null && !request.composeService.isBlank()) {
            cmd.append(" ").append(ToolSupport.shellQuote(request.composeService));
        }
        return run(cmd.toString());
    }

    @Operation(
            operationId = "devops_docker_compose_pull",
            summary = "Runs `docker compose pull` — optional composeFile path and composeService name"
    )
    @POST
    @Path("/compose-pull")
    public ExecutionResponse composePull(DockerRequestDto request) throws Exception {
        StringBuilder cmd = composeBase(request);
        cmd.append(" pull");
        if (request != null && request.composeService != null && !request.composeService.isBlank()) {
            cmd.append(" ").append(ToolSupport.shellQuote(request.composeService));
        }
        return run(cmd.toString());
    }

    // ── helpers ──────────────────────────────────────────────

    private StringBuilder composeBase(DockerRequestDto request) {
        StringBuilder cmd = new StringBuilder("docker compose");
        if (request != null && request.composeFile != null && !request.composeFile.isBlank()) {
            cmd.append(" -f ").append(ToolSupport.shellQuote(request.composeFile));
        }
        return cmd;
    }

    private ExecutionResponse run(String command) throws Exception {
        return lock.runLocked(() -> executor.execute(command));
    }

    private void validateContainer(DockerRequestDto request) {
        if (request == null || request.container == null || request.container.isBlank()) {
            throw new IllegalArgumentException("container is required");
        }
    }
}
