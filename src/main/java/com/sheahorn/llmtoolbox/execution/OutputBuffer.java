package com.sheahorn.llmtoolbox.execution;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Bounded output collector for one process stream.
 *
 * Keeps the LAST {@code cap} bytes — a build log is most useful at its end; the first
 * megabytes are noise for an LLM. {@code cap <= 0} means unbounded (old behaviour).
 *
 * Thread-safety: pumps for stdout and stderr each own an instance, but the accessors are
 * called from the calling thread, so everything is synchronized.
 */
public final class OutputBuffer {

    private final int cap;
    private final byte[] ring;
    private final ByteArrayOutputStream loose;

    private int writePos;
    private boolean wrapped;
    private long total;

    public OutputBuffer(long cap) {
        if (cap > 0 && cap <= Integer.MAX_VALUE) {
            this.cap = (int) cap;
            this.ring = new byte[this.cap];
            this.loose = null;
        } else {
            this.cap = 0;
            this.ring = null;
            this.loose = new ByteArrayOutputStream();
        }
    }

    public synchronized void write(byte[] src, int off, int len) {
        if (len <= 0) {
            return;
        }
        total += len;

        if (loose != null) {
            loose.write(src, off, len);
            return;
        }

        // No special-casing for over-sized chunks: the loop below just wraps as often as
        // needed, which naturally keeps only the last `cap` bytes of the chunk.
        while (len > 0) {
            int n = Math.min(cap - writePos, len);
            System.arraycopy(src, off, ring, writePos, n);
            writePos += n;
            if (writePos == cap) {
                writePos = 0;
                wrapped = true;
            }
            off += n;
            len -= n;
        }
    }

    /** Bytes produced by the stream, whether kept or not. */
    public synchronized long total() {
        return total;
    }

    /** Bytes actually retained. */
    public synchronized int kept() {
        return loose != null ? loose.size() : (wrapped ? cap : writePos);
    }

    public synchronized boolean truncated() {
        return total > kept();
    }

    /**
     * Decoded content. When truncated, a marker line states how much was dropped — the
     * caller (and the LLM reading the tool result) must not mistake the tail for the whole
     * output.
     */
    public synchronized String text() {
        byte[] bytes = snapshot();
        String body = new String(bytes, StandardCharsets.UTF_8);
        if (!truncated()) {
            return body;
        }
        return "[... output truncated, kept last " + bytes.length + " of " + total + " bytes ...]\n" + body;
    }

    private byte[] snapshot() {
        if (loose != null) {
            return loose.toByteArray();
        }
        if (!wrapped) {
            return Arrays.copyOf(ring, writePos);
        }
        byte[] out = new byte[cap];
        System.arraycopy(ring, writePos, out, 0, cap - writePos);
        System.arraycopy(ring, 0, out, cap - writePos, writePos);
        return out;
    }
}
