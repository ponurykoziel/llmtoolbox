package com.sheahorn.llmtoolbox.execution;

public final class ToolSupport {

    private ToolSupport() {}

    public static String shellQuote(String value) {
        if (value == null) {
            return "''";
        }
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    public static void validateHost(String host) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host is required");
        }

        if (host.length() > 253) {
            throw new IllegalArgumentException("host is too long");
        }

        if (!host.matches("[a-zA-Z0-9._:\\-]+")) {
            throw new IllegalArgumentException("host contains invalid characters");
        }
    }

    /**
     * Strips all characters that are not A-Z, a-z, 0-9, hyphen, dot, underscore, @, or colon.
     * Returns the sanitized string, or empty string if null/blank input.
     */
    public static String sanitizeSafeChars(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replaceAll("[^A-Za-z0-9._@:\\-]", "");
    }

    public static void validatePort(Integer port) {
        if (port == null) {
            throw new IllegalArgumentException("port is required");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
    }
}