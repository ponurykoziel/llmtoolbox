package com.sheahorn.llmtoolbox.llm;

import com.sheahorn.llmtoolbox.fstools.info.FsResourceSupport;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Reads a single image file from disk (guarded by the allowed workspace root),
 * detects its MIME type from the file extension, and base64-encodes it for
 * attachment to an LLM request.
 */
@ApplicationScoped
public class ImageAttachmentHelper extends FsResourceSupport {

    public ImageAttachment load(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        Path path = resolvePath(imagePath);

        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("image path is not a regular file: " + imagePath);
        }

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot read image file: " + e.getMessage());
        }

        String mime = detectMime(imagePath);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return new ImageAttachment(mime, base64);
    }

    private String detectMime(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        return "image/png";
    }

    public static class ImageAttachment {
        public final String mime;
        public final String base64;

        public ImageAttachment(String mime, String base64) {
            this.mime = mime;
            this.base64 = base64;
        }

        public String dataUrl() {
            return "data:" + mime + ";base64," + base64;
        }
    }
}
