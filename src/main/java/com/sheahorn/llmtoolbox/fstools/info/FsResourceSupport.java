package com.sheahorn.llmtoolbox.fstools.info;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Path;

public abstract class FsResourceSupport {

    @ConfigProperty(name = "llmtoolbox.files.allowed-root", defaultValue = "/")
    String allowedRoot;

    /**
     * Validates and resolves a raw path string against the allowed root.
     *
     * Guards (applied to rawPath as-is):
     * 1. rawPath == allowedRoot           → allow
     * 2. rawPath == allowedRoot + "/"     → allow
     * 3. rawPath does not start with allowedRoot → reject
     * 4. rawPath contains "/.."           → reject
     *
     * After normalization, if the path equals the root it is allowed.
     * Otherwise the parent directory's real path must start with the
     * root's real path (defeats symlink escapes).
     *
     * @return the validated, absolute, normalized Path
     * @throws IllegalArgumentException if the path is outside the allowed root
     */
    protected Path resolvePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new IllegalArgumentException("path is required");
        }

        Path root = Path.of(allowedRoot).toAbsolutePath().normalize();
        String rootStr = root.toString();

        // ── Guard 1 & 2: raw path equals root (with or without trailing slash) ──
        if (rawPath.equals(rootStr) || rawPath.equals(rootStr + "/")) {
            return root;
        }

        // ── Guard 3: raw path must start with allowed root ──
        if (!rawPath.startsWith(rootStr)) {
            throw new IllegalArgumentException(
                "path is outside allowed root: allowed root=" + rootStr + ", actual path=" + rawPath);
        }

        // ── Guard 4: no "/.." segments ──
        if (rawPath.contains("/..")) {
            throw new IllegalArgumentException(
                "path contains parent directory traversal: " + rawPath);
        }

        // ── Resolve and normalize ──
        Path path = Path.of(rawPath).toAbsolutePath().normalize();

        // If normalized path equals root, allow (e.g. /root/. or /root/foo/..)
        if (path.equals(root)) {
            return path;
        }

        // ── Parent real-path check ──
        // Only the parent must exist and be inside the root.
        // The target itself may not exist yet (e.g. fs_files_create, fs_files_mkdir).
        try {
            Path rootReal = root.toRealPath();
            Path parent = path.getParent();
            // parent is never null here: path != root and root is at least "/"
            Path parentReal = parent.toRealPath();
            if (!parentReal.startsWith(rootReal)) {
                throw new IllegalArgumentException(
                    "path parent is outside allowed root: allowed root=" + rootReal
                        + ", actual parent=" + parentReal);
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
