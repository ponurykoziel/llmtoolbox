package com.sheahorn.llmtoolbox.fstools.files;

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
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;

@jakarta.ws.rs.Path("/api/tools/fs/files")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FileResource extends FsResourceSupport {

    @ConfigProperty(name = "llmtoolbox.files.max-read-bytes", defaultValue = "1048576")
    long maxReadBytes;

    @Operation(
            operationId = "fs_files_read",
            summary = "Reads a regular file's contents as UTF-8, bounded by a maximum byte cap"
    )
    @POST
    @jakarta.ws.rs.Path("/read")
    public ExecutionResponse read(ReadFileRequestDto request) {
        return wrap("read " + quote(request == null ? null : request.path), () -> {
            if (request == null) {
                throw new IllegalArgumentException("Request is required");
            }

            Path path = existingRegularFile(request.path);

            long size = Files.size(path);
            if (size > maxReadBytes) {
                throw new IllegalArgumentException("file is too large to read");
            }

            return Files.readString(path, StandardCharsets.UTF_8);
        });
    }

    @Operation(
            operationId = "fs_files_create",
            summary = "Creates a new regular file with the given UTF-8 content (refuses if it already exists)"
    )
    @POST
    @jakarta.ws.rs.Path("/create")
    public ExecutionResponse create(CreateFileRequestDto request) {
        return wrap("create " + quote(request == null ? null : request.path), () -> {
            if (request == null) {
                throw new IllegalArgumentException("Request is required");
            }

            Path path = resolvePath(request.path);

            Files.writeString(
                    path,
                    request.content == null ? "" : request.content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );

            return "created\n";
        });
    }

    @Operation(
            operationId = "fs_files_overwrite",
            summary = "Overwrites the entire content of an existing regular file with new UTF-8 content"
    )
    @POST
    @jakarta.ws.rs.Path("/overwrite")
    public ExecutionResponse overwrite(OverwriteFileRequestDto request) {
        return wrap("overwrite " + quote(request == null ? null : request.path), () -> {
            if (request == null) {
                throw new IllegalArgumentException("Request is required");
            }

            Path path = existingRegularFile(request.path);

            Files.writeString(
                    path,
                    request.content == null ? "" : request.content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            return "overwritten\n";
        });
    }

    @Operation(
            operationId = "fs_files_append",
            summary = "Appends UTF-8 content to an existing regular file"
    )
    @POST
    @jakarta.ws.rs.Path("/append")
    public ExecutionResponse append(AppendFileRequestDto request) {
        return wrap("append " + quote(request == null ? null : request.path), () -> {
            if (request == null) {
                throw new IllegalArgumentException("Request is required");
            }

            Path path = existingRegularFile(request.path);

            Files.writeString(
                    path,
                    request.content == null ? "" : request.content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE
            );

            return "appended\n";
        });
    }

    @Operation(
            operationId = "fs_files_replace",
            summary = "Replaces all literal occurrences of a search string with a replacement string in an existing file (not regex)"
    )
    @POST
    @jakarta.ws.rs.Path("/replace")
    public ExecutionResponse replace(ReplaceFileRequestDto request) {
        return wrap("replace " + quote(request == null ? null : request.path), () -> {
            if (request == null) {
                throw new IllegalArgumentException("Request is required");
            }

            if (request.search == null) {
                throw new IllegalArgumentException("search string is required");
            }

            Path path = existingRegularFile(request.path);

            long size = Files.size(path);
            if (size > maxReadBytes) {
                throw new IllegalArgumentException("file is too large to read");
            }

            String content = Files.readString(path, StandardCharsets.UTF_8);
            String replacement = request.replacement == null ? "" : request.replacement;
            String newContent = content.replace(request.search, replacement);

            Files.writeString(
                    path,
                    newContent,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            return "replaced\n";
        });
    }

    @Operation(
            operationId = "fs_files_replace_regex",
            summary = "Replaces all regex pattern matches with a replacement string in an existing file (uses String.replaceAll)"
    )
    @POST
    @jakarta.ws.rs.Path("/replace-regex")
    public ExecutionResponse replaceRegex(ReplaceRegexFileRequestDto request) {
        return wrap("replace-regex " + quote(request == null ? null : request.path), () -> {
            if (request == null) {
                throw new IllegalArgumentException("Request is required");
            }

            if (request.pattern == null) {
                throw new IllegalArgumentException("regex pattern is required");
            }

            Path path = existingRegularFile(request.path);

            long size = Files.size(path);
            if (size > maxReadBytes) {
                throw new IllegalArgumentException("file is too large to read");
            }

            String content = Files.readString(path, StandardCharsets.UTF_8);
            String replacement = request.replacement == null ? "" : request.replacement;
            String newContent = content.replaceAll(request.pattern, replacement);

            Files.writeString(
                    path,
                    newContent,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            return "replaced (regex)\n";
        });
    }

    @Operation(
            operationId = "fs_files_move",
            summary = "Renames a regular file inside the allowed root, refusing if the target exists"
    )
    @POST
    @jakarta.ws.rs.Path("/move")
    public ExecutionResponse move(MoveFileRequestDto request) {
        return wrap("move "
                + quote(request == null ? null : request.sourcePath)
                + " "
                + quote(request == null ? null : request.targetPath), () -> {

            if (request == null) {
                throw new IllegalArgumentException("Request is required");
            }

            Path source = existingRegularFile(request.sourcePath);
            Path target = resolvePath(request.targetPath);

            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                throw new IllegalArgumentException("target already exists");
            }

            Files.move(source, target);

            return "moved\n";
        });
    }

    @Operation(
            operationId = "fs_files_delete",
            summary = "Deletes a regular file inside the allowed root"
    )
    @POST
    @jakarta.ws.rs.Path("/delete")
    public ExecutionResponse delete(DeleteFileRequestDto request) {
        return wrap("delete " + quote(request == null ? null : request.path), () -> {
            if (request == null) {
                throw new IllegalArgumentException("Request is required");
            }

            Path path = existingRegularFile(request.path);

            Files.delete(path);

            return "deleted\n";
        });
    }

    @Operation(
            operationId = "fs_files_mkdir",
            summary = "Creates a new directory inside the allowed root (parent must already exist)"
    )
    @POST
    @jakarta.ws.rs.Path("/mkdir")
    public ExecutionResponse mkdir(MkdirRequestDto request) {
        return wrap("mkdir " + quote(request == null ? null : request.path), () -> {
            if (request == null) {
                throw new IllegalArgumentException("Request is required");
            }

            Path path = resolvePath(request.path);

            Files.createDirectory(path);

            return "directory created\n";
        });
    }

    @Operation(
            operationId = "fs_files_rmdir",
            summary = "Removes an empty directory inside the allowed root"
    )
    @POST
    @jakarta.ws.rs.Path("/rmdir")
    public ExecutionResponse rmdir(RmdirRequestDto request) {
        return wrap("rmdir " + quote(request == null ? null : request.path), () -> {
            if (request == null) {
                throw new IllegalArgumentException("Request is required");
            }

            Path path = existingDirectory(request.path);

            Files.delete(path);

            return "directory removed\n";
        });
    }

    private Path existingRegularFile(String rawPath) throws IOException {
        Path path = resolvePath(rawPath);

        if (Files.isSymbolicLink(path)) {
            throw new IllegalArgumentException("symlinks are not supported");
        }

        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("file does not exist");
        }

        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("path is not a regular file");
        }

        return path;
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
