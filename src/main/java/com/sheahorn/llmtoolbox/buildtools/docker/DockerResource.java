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

    // ── print ─────────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_print_version",
            summary = "Runs `docker version`"
    )
    @POST
    @Path("/print-version")
    public ExecutionResponse printVersion(DockerEmptyRequest request) throws Exception {
        return run("docker version");
    }

    @Operation(
            operationId = "devops_docker_print_info",
            summary = "Runs `docker info`"
    )
    @POST
    @Path("/print-info")
    public ExecutionResponse printInfo(DockerEmptyRequest request) throws Exception {
        return run("docker info");
    }

    @Operation(
            operationId = "devops_docker_print_stats",
            summary = "Runs `docker stats --no-stream` (one-shot) for all containers"
    )
    @POST
    @Path("/print-stats")
    public ExecutionResponse printStats(DockerEmptyRequest request) throws Exception {
        return run("docker stats --no-stream");
    }

    // ── containers ───────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_container_list",
            summary = "Runs `docker ps -a` to list all containers"
    )
    @POST
    @Path("/container-list")
    public ExecutionResponse containerList(DockerEmptyRequest request) throws Exception {
        return run("docker ps -a");
    }

    @Operation(
            operationId = "devops_docker_container_inspect",
            summary = "Runs `docker inspect <container>`"
    )
    @POST
    @Path("/container-inspect")
    public ExecutionResponse containerInspect(DockerContainerRequest request) throws Exception {
        validateContainer(request);
        return run("docker inspect " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_container_top",
            summary = "Runs `docker top <container>` to show processes"
    )
    @POST
    @Path("/container-top")
    public ExecutionResponse containerTop(DockerContainerRequest request) throws Exception {
        validateContainer(request);
        return run("docker top " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_container_ports",
            summary = "Runs `docker port <container>` to show port mappings"
    )
    @POST
    @Path("/container-ports")
    public ExecutionResponse containerPorts(DockerContainerRequest request) throws Exception {
        validateContainer(request);
        return run("docker port " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_container_logs_full",
            summary = "Runs `docker logs <container>` (full log output)"
    )
    @POST
    @Path("/container-logs-full")
    public ExecutionResponse containerLogsFull(DockerContainerRequest request) throws Exception {
        validateContainer(request);
        return run("docker logs " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_container_logs_tail",
            summary = "Runs `docker logs --tail <n> <container>` — n is required"
    )
    @POST
    @Path("/container-logs-tail")
    public ExecutionResponse containerLogsTail(DockerContainerLogsTailRequest request) throws Exception {
        if (request == null || request.container == null || request.container.isBlank()) {
            throw new IllegalArgumentException("container is required");
        }
        if (request.n == null || request.n <= 0) {
            throw new IllegalArgumentException("n is required and must be positive");
        }
        return run("docker logs --tail " + request.n + " " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_container_start",
            summary = "Runs `docker start <container>`"
    )
    @POST
    @Path("/container-start")
    public ExecutionResponse containerStart(DockerContainerRequest request) throws Exception {
        validateContainer(request);
        return run("docker start " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_container_stop",
            summary = "Runs `docker stop <container>`"
    )
    @POST
    @Path("/container-stop")
    public ExecutionResponse containerStop(DockerContainerRequest request) throws Exception {
        validateContainer(request);
        return run("docker stop " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_container_restart",
            summary = "Runs `docker restart <container>`"
    )
    @POST
    @Path("/container-restart")
    public ExecutionResponse containerRestart(DockerContainerRequest request) throws Exception {
        validateContainer(request);
        return run("docker restart " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_container_rename",
            summary = "Runs `docker rename <container> <name>` — container is old name, name is new name"
    )
    @POST
    @Path("/container-rename")
    public ExecutionResponse containerRename(DockerContainerRenameRequest request) throws Exception {
        if (request == null || request.container == null || request.container.isBlank()) {
            throw new IllegalArgumentException("container is required");
        }
        if (request.name == null || request.name.isBlank()) {
            throw new IllegalArgumentException("name (new name) is required");
        }
        return run("docker rename " + ToolSupport.shellQuote(request.container)
                + " " + ToolSupport.shellQuote(request.name));
    }

    @Operation(
            operationId = "devops_docker_container_remove",
            summary = "Runs `docker rm <container>`"
    )
    @POST
    @Path("/container-remove")
    public ExecutionResponse containerRemove(DockerContainerRequest request) throws Exception {
        validateContainer(request);
        return run("docker rm " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_container_retrieve_file",
            summary = "Runs `docker cp <source> <target>` — source and target fields required (container paths use <container>:<path> syntax)"
    )
    @POST
    @Path("/container-retrieve-file")
    public ExecutionResponse containerRetrieveFile(DockerCpRequest request) throws Exception {
        if (request == null || request.source == null || request.source.isBlank()) {
            throw new IllegalArgumentException("source is required");
        }
        if (request.target == null || request.target.isBlank()) {
            throw new IllegalArgumentException("target is required");
        }
        return run("docker cp " + ToolSupport.shellQuote(request.source)
                + " " + ToolSupport.shellQuote(request.target));
    }

    @Operation(
            operationId = "devops_docker_container_connect_to_network",
            summary = "Runs `docker network connect <network> <container>`"
    )
    @POST
    @Path("/container-connect-to-network")
    public ExecutionResponse containerConnectToNetwork(DockerContainerNetworkRequest request) throws Exception {
        if (request == null || request.container == null || request.container.isBlank()) {
            throw new IllegalArgumentException("container is required");
        }
        if (request.network == null || request.network.isBlank()) {
            throw new IllegalArgumentException("network is required");
        }
        return run("docker network connect " + ToolSupport.shellQuote(request.network)
                + " " + ToolSupport.shellQuote(request.container));
    }

    @Operation(
            operationId = "devops_docker_container_disconnect_from_network",
            summary = "Runs `docker network disconnect <network> <container>`"
    )
    @POST
    @Path("/container-disconnect-from-network")
    public ExecutionResponse containerDisconnectFromNetwork(DockerContainerNetworkRequest request) throws Exception {
        if (request == null || request.container == null || request.container.isBlank()) {
            throw new IllegalArgumentException("container is required");
        }
        if (request.network == null || request.network.isBlank()) {
            throw new IllegalArgumentException("network is required");
        }
        return run("docker network disconnect " + ToolSupport.shellQuote(request.network)
                + " " + ToolSupport.shellQuote(request.container));
    }

    // ── images ───────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_image_list",
            summary = "Runs `docker images` to list all images"
    )
    @POST
    @Path("/image-list")
    public ExecutionResponse imageList(DockerEmptyRequest request) throws Exception {
        return run("docker images");
    }

    @Operation(
            operationId = "devops_docker_image_pull",
            summary = "Runs `docker pull <image>`"
    )
    @POST
    @Path("/image-pull")
    public ExecutionResponse imagePull(DockerImageRequest request) throws Exception {
        if (request == null || request.image == null || request.image.isBlank()) {
            throw new IllegalArgumentException("image is required");
        }
        return run("docker pull " + ToolSupport.shellQuote(request.image));
    }

    @Operation(
            operationId = "devops_docker_image_push",
            summary = "Runs `docker push <image>`"
    )
    @POST
    @Path("/image-push")
    public ExecutionResponse imagePush(DockerImageRequest request) throws Exception {
        if (request == null || request.image == null || request.image.isBlank()) {
            throw new IllegalArgumentException("image is required");
        }
        return run("docker push " + ToolSupport.shellQuote(request.image));
    }

    @Operation(
            operationId = "devops_docker_image_remove",
            summary = "Runs `docker rmi <image>`"
    )
    @POST
    @Path("/image-remove")
    public ExecutionResponse imageRemove(DockerImageRequest request) throws Exception {
        if (request == null || request.image == null || request.image.isBlank()) {
            throw new IllegalArgumentException("image is required");
        }
        return run("docker rmi " + ToolSupport.shellQuote(request.image));
    }

    @Operation(
            operationId = "devops_docker_image_prune_all",
            summary = "Runs `docker image prune -a` to remove all unused images"
    )
    @POST
    @Path("/image-prune-all")
    public ExecutionResponse imagePruneAll(DockerEmptyRequest request) throws Exception {
        return run("docker image prune -a");
    }

    // ── networks ─────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_network_list",
            summary = "Runs `docker network ls`"
    )
    @POST
    @Path("/network-list")
    public ExecutionResponse networkList(DockerEmptyRequest request) throws Exception {
        return run("docker network ls");
    }

    @Operation(
            operationId = "devops_docker_network_inspect",
            summary = "Runs `docker network inspect <network>`"
    )
    @POST
    @Path("/network-inspect")
    public ExecutionResponse networkInspect(DockerNetworkRequest request) throws Exception {
        if (request == null || request.network == null || request.network.isBlank()) {
            throw new IllegalArgumentException("network is required");
        }
        return run("docker network inspect " + ToolSupport.shellQuote(request.network));
    }

    @Operation(
            operationId = "devops_docker_network_create",
            summary = "Runs `docker network create <name>`"
    )
    @POST
    @Path("/network-create")
    public ExecutionResponse networkCreate(DockerNameRequest request) throws Exception {
        if (request == null || request.name == null || request.name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        return run("docker network create " + ToolSupport.shellQuote(request.name));
    }

    @Operation(
            operationId = "devops_docker_network_remove",
            summary = "Runs `docker network rm <network>`"
    )
    @POST
    @Path("/network-remove")
    public ExecutionResponse networkRemove(DockerNetworkRequest request) throws Exception {
        if (request == null || request.network == null || request.network.isBlank()) {
            throw new IllegalArgumentException("network is required");
        }
        return run("docker network rm " + ToolSupport.shellQuote(request.network));
    }

    // ── volumes ──────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_volume_list",
            summary = "Runs `docker volume ls`"
    )
    @POST
    @Path("/volume-list")
    public ExecutionResponse volumeList(DockerEmptyRequest request) throws Exception {
        return run("docker volume ls");
    }

    @Operation(
            operationId = "devops_docker_volume_inspect",
            summary = "Runs `docker volume inspect <volume>`"
    )
    @POST
    @Path("/volume-inspect")
    public ExecutionResponse volumeInspect(DockerVolumeRequest request) throws Exception {
        if (request == null || request.volume == null || request.volume.isBlank()) {
            throw new IllegalArgumentException("volume is required");
        }
        return run("docker volume inspect " + ToolSupport.shellQuote(request.volume));
    }

    // ── build / history / exec ───────────────────────────────

    @Operation(
            operationId = "devops_docker_build",
            summary = "Runs `docker build -t <name> .` — name (tag) is required"
    )
    @POST
    @Path("/build")
    public ExecutionResponse build(DockerNameRequest request) throws Exception {
        if (request == null || request.name == null || request.name.isBlank()) {
            throw new IllegalArgumentException("name (tag) is required");
        }
        return run("docker build -t " + ToolSupport.shellQuote(request.name) + " .");
    }

    @Operation(
            operationId = "devops_docker_history",
            summary = "Runs `docker history <image>`"
    )
    @POST
    @Path("/history")
    public ExecutionResponse history(DockerImageRequest request) throws Exception {
        if (request == null || request.image == null || request.image.isBlank()) {
            throw new IllegalArgumentException("image is required");
        }
        return run("docker history " + ToolSupport.shellQuote(request.image));
    }

    @Operation(
            operationId = "devops_docker_exec",
            summary = "Runs `docker exec <container> <command>` — container and command required"
    )
    @POST
    @Path("/exec")
    public ExecutionResponse exec(DockerExecRequest request) throws Exception {
        if (request == null || request.container == null || request.container.isBlank()) {
            throw new IllegalArgumentException("container is required");
        }
        if (request.command == null || request.command.isBlank()) {
            throw new IllegalArgumentException("command is required");
        }
        return run("docker exec " + ToolSupport.shellQuote(request.container)
                + " " + request.command);
    }

    // ── compose ──────────────────────────────────────────────

    @Operation(
            operationId = "devops_docker_compose_up",
            summary = "Runs `docker compose up -d` — optional composeFile path and composeService name"
    )
    @POST
    @Path("/compose-up")
    public ExecutionResponse composeUp(DockerComposeRequest request) throws Exception {
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
    public ExecutionResponse composeDown(DockerComposeRequest request) throws Exception {
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
    public ExecutionResponse composePs(DockerComposeRequest request) throws Exception {
        StringBuilder cmd = composeBase(request);
        cmd.append(" ps");
        return run(cmd.toString());
    }

    @Operation(
            operationId = "devops_docker_compose_logs",
            summary = "Runs `docker compose logs` — optional composeFile path, composeService name, and n (--tail)"
    )
    @POST
    @Path("/compose-logs")
    public ExecutionResponse composeLogs(DockerComposeRequest request) throws Exception {
        StringBuilder cmd = composeBase(request);
        cmd.append(" logs");
        if (request != null && request.n != null && request.n > 0) {
            cmd.append(" --tail ").append(request.n);
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
    public ExecutionResponse composeRestart(DockerComposeRequest request) throws Exception {
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
    public ExecutionResponse composeBuild(DockerComposeRequest request) throws Exception {
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
    public ExecutionResponse composePull(DockerComposeRequest request) throws Exception {
        StringBuilder cmd = composeBase(request);
        cmd.append(" pull");
        if (request != null && request.composeService != null && !request.composeService.isBlank()) {
            cmd.append(" ").append(ToolSupport.shellQuote(request.composeService));
        }
        return run(cmd.toString());
    }

    // ── helpers ──────────────────────────────────────────────

    private StringBuilder composeBase(DockerComposeRequest request) {
        StringBuilder cmd = new StringBuilder("docker compose");
        if (request != null && request.composeFile != null && !request.composeFile.isBlank()) {
            cmd.append(" -f ").append(ToolSupport.shellQuote(request.composeFile));
        }
        return cmd;
    }

    private ExecutionResponse run(String command) throws Exception {
        return lock.runLocked(() -> executor.execute(command));
    }

    private void validateContainer(DockerContainerRequest request) {
        if (request == null || request.container == null || request.container.isBlank()) {
            throw new IllegalArgumentException("container is required");
        }
    }
}
