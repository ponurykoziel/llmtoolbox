package com.sheahorn.llmtoolbox.browser;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Immutable screenshot transformation settings: target format, compression
 * level, and an optional hard byte cap. Provides the transform function that
 * converts a lossless PNG capture into the configured output.
 *
 * <p>Compression ranges:
 * <ul>
 *   <li>jpg: quality 1-100</li>
 *   <li>png: quality 0-9 (0 = uncompressed)</li>
 * </ul>
 * <p>{@code maxBytes} is a hard cap in bytes; 0 means no limit.
 */
public class ScreenshotSettings {

    public static final String FORMAT_JPG = "jpg";
    public static final String FORMAT_PNG = "png";

    private static final int MAX_DOWNSCALE_ITERATIONS = 30;
    private static final double DOWNSCALE_STEP = 0.85;

    private final String format;
    private final int compression;
    private final long maxBytes;

    public ScreenshotSettings(String format, int compression, long maxBytes) {
        this.format = normalizeFormat(format);
        this.compression = compression;
        this.maxBytes = maxBytes;
        validate();
    }

    public String getFormat() {
        return format;
    }

    public int getCompression() {
        return compression;
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    /**
     * Transforms a lossless PNG capture into the configured format/compression,
     * downscaling as needed to honor the byte cap (0 = no limit).
     */
    public byte[] transform(byte[] pngBytes) throws IOException {
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(pngBytes));
        if (source == null) {
            throw new IOException("Failed to decode screenshot PNG bytes");
        }

        BufferedImage img = source;
        byte[] out = encode(img);
        if (maxBytes <= 0) {
            return out;
        }

        double scale = 1.0;
        int iterations = 0;
        while (out.length > maxBytes && iterations < MAX_DOWNSCALE_ITERATIONS) {
            scale *= DOWNSCALE_STEP;
            int w = Math.max(1, (int) Math.round(source.getWidth() * scale));
            int h = Math.max(1, (int) Math.round(source.getHeight() * scale));
            if (w == img.getWidth() && h == img.getHeight()) {
                break; // cannot shrink further
            }
            img = downscale(source, w, h);
            out = encode(img);
            iterations++;
        }
        return out;
    }

    private byte[] encode(BufferedImage img) throws IOException {
        BufferedImage toWrite = img;
        if (FORMAT_JPG.equals(format) && img.getColorModel().hasAlpha()) {
            toWrite = flattenAlpha(img);
        }

        String writerFormat = FORMAT_JPG.equals(format) ? "jpeg" : "png";
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(writerFormat);
        if (!writers.hasNext()) {
            throw new IOException("No ImageWriter available for format: " + format);
        }
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(bos)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(compressionQuality());
            }
            writer.write(null, new IIOImage(toWrite, null, null), param);
            ios.flush();
            return bos.toByteArray();
        } finally {
            writer.dispose();
        }
    }

    private float compressionQuality() {
        if (FORMAT_JPG.equals(format)) {
            return compression / 100.0f;
        }
        return compression / 9.0f;
    }

    private BufferedImage flattenAlpha(BufferedImage img) {
        BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return rgb;
    }

    private BufferedImage downscale(BufferedImage src, int w, int h) {
        int type = src.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_ARGB : src.getType();
        BufferedImage dst = new BufferedImage(w, h, type);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }

    private static String normalizeFormat(String format) {
        if (format == null || format.isBlank()) {
            return FORMAT_PNG;
        }
        String f = format.trim().toLowerCase();
        if ("jpeg".equals(f)) {
            return FORMAT_JPG;
        }
        return f;
    }

    private void validate() {
        if (!FORMAT_JPG.equals(format) && !FORMAT_PNG.equals(format)) {
            throw new IllegalArgumentException("format must be 'jpg' or 'png', got: " + format);
        }
        if (FORMAT_JPG.equals(format)) {
            if (compression < 1 || compression > 100) {
                throw new IllegalArgumentException("jpg compression must be 1-100, got: " + compression);
            }
        } else {
            if (compression < 0 || compression > 9) {
                throw new IllegalArgumentException("png compression must be 0-9, got: " + compression);
            }
        }
        if (maxBytes < 0) {
            throw new IllegalArgumentException("maxBytes must be >= 0, got: " + maxBytes);
        }
    }
}
