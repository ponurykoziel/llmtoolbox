package com.sheahorn.llmtoolbox.fstools.ls;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.fstools.info.FsResourceSupport;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@jakarta.ws.rs.Path("/pack-folder")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PackFolderResource extends FsResourceSupport {

    @ConfigProperty(name = "llmtoolbox.files.max-pack-total-bytes", defaultValue = "2097152")
    long maxTotalBytes;

    @ConfigProperty(name = "llmtoolbox.files.max-pack-file-bytes", defaultValue = "262144")
    long maxFileBytes;

    @ConfigProperty(name = "llmtoolbox.files.max-pack-files", defaultValue = "200")
    int maxFiles;

    @Operation(
            operationId = "fs_pack_folder",
            summary = "Concatenates a directory's files into a single text bundle, bounded by size and file-count caps"
    )
    @POST
    @jakarta.ws.rs.Path("/execute")
    public ExecutionResponse execute(PackFolderRequestDto request) {
        return wrap("pack-folder " + quote(request == null ? null : request.path), () -> {
            if (request == null) {
                throw new IllegalArgumentException("Request is required");
            }

            Path root = existingDirectory(request.path);

            List<Path> files = collectFiles(root);

            StringBuilder output = new StringBuilder();
            long totalBytes = 0;

            for (Path file : files) {
                long size = Files.size(file);

                if (size > maxFileBytes) {
                    appendSkipped(output, root, file, "file too large: " + size + " bytes");
                    continue;
                }

                if (totalBytes + size > maxTotalBytes) {
                    appendSkipped(output, root, file, "total pack limit reached");
                    break;
                }

                String relative = root.relativize(file).toString();

                output.append("===== FILE: ")
                        .append(relative)
                        .append(" =====\n");

                try {
                    output.append(Files.readString(file, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    output.append("[[SKIPPED: not valid UTF-8 or unreadable: ")
                            .append(e.getMessage())
                            .append("]]");
                }

                output.append("\n===== END FILE: ")
                        .append(relative)
                        .append(" =====\n\n");

                totalBytes += size;
            }

            return output.toString();
        });
    }

    private List<Path> collectFiles(Path root) throws IOException {
        List<Path> files = new ArrayList<>();

        Files.walkFileTree(root, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (Files.isSymbolicLink(dir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                if (!dir.equals(root) && isHiddenName(dir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (files.size() >= maxFiles) {
                    return FileVisitResult.TERMINATE;
                }

                if (Files.isSymbolicLink(file)) {
                    return FileVisitResult.CONTINUE;
                }

                if (isHiddenName(file)) {
                    return FileVisitResult.CONTINUE;
                }

                if (attrs.isRegularFile()) {
                    files.add(file);
                }

                return FileVisitResult.CONTINUE;
            }
        });

        files.sort(Comparator.comparing(path -> root.relativize(path).toString()));

        return files;
    }

    private void appendSkipped(StringBuilder output, Path root, Path file, String reason) {
        String relative = root.relativize(file).toString();

        output.append("===== FILE: ")
                .append(relative)
                .append(" =====\n")
                .append("[[SKIPPED: ")
                .append(reason)
                .append("]]\n")
                .append("===== END FILE: ")
                .append(relative)
                .append(" =====\n\n");
    }

    private Path existingDirectory(String rawPath) throws IOException {
        Path path = resolvePath(rawPath);

        if (Files.isSymbolicLink(path)) {
            throw new IllegalArgumentException("symlinks are not supported");
        }

        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("directory does not exist");
        }

        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("path is not a directory");
        }

        return path;
    }

    private boolean isHiddenName(Path path) {
        Path fileName = path.getFileName();
        return fileName != null && fileName.toString().startsWith(".");
    }

    private ExecutionResponse wrap(String command, FileAction action) {
        Instant start = Instant.now();

        ExecutionResponse response = new ExecutionResponse();
        response.command = command;
        response.timedOut = false;

        try {
            response.stdout = action.run();
            response.stderr = "";
            response.exitCode = 0;
        } catch (Exception e) {
            response.stdout = "";
            response.stderr = e.getMessage();
            response.exitCode = 1;
        } finally {
            response.durationMs = Duration.between(start, Instant.now()).toMillis();
        }

        return response;
    }

    private String quote(String value) {
        if (value == null) {
            return "''";
        }

        return "'" + value.replace("'", "'\\''") + "'";
    }

    @FunctionalInterface
    private interface FileAction {
        String run() throws Exception;
    }
}
