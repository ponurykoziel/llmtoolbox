package com.sheahorn.llmtoolbox.fstools.info;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Path;

public abstract class FsResourceSupport {

    @ConfigProperty(name = "llmtoolbox.files.allowed-root", defaultValue = "/")
    String allowedRoot;

    /**
     * Validates and resolves a raw path string against the allowed root.
     * Relative paths are resolved against the root. Symlink escapes are
     * defeated via a real-path check on the parent directory.
     *
     * @return the validated, absolute, normalized Path
     * @throws IllegalArgumentException if the path is outside the allowed root
     */
    protected Path resolvePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new IllegalArgumentException("path is required");
        }

        Path root = Path.of(allowedRoot).toAbsolutePath().normalize();
        Path path = Path.of(rawPath);

        if (!path.isAbsolute()) {
            path = root.resolve(path);
        }

        path = path.toAbsolutePath().normalize();

        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("path is outside allowed root");
        }

        // Real-path parent check — defeats symlink escapes
        // (e.g. /opt/app/link -> /etc, then /opt/app/link/passwd)
        try {
            Path rootReal = root.toRealPath();
            Path parent = path.getParent();
            Path parentReal = (parent == null) ? rootReal : parent.toRealPath();

            if (!parentReal.startsWith(rootReal)) {
                throw new IllegalArgumentException("path parent is outside allowed root");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot resolve path: " + e.getMessage());
        }

        return path;
    }

    protected String normalizePath(String rawPath) {
        return resolvePath(rawPath).toString();
    }
}
