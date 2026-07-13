package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.common.Calculator;

public class BaseCalculator implements Calculator {

    public String baseConversion(String value, int fromBase, int toBase) {
        if (fromBase < 2 || fromBase > 36 || toBase < 2 || toBase > 36) {
            throw new IllegalArgumentException("Bases must be between 2 and 36");
        }
        long decimal = Long.parseLong(value, fromBase);
        return Long.toString(decimal, toBase).toUpperCase();
    }

    public long[] decomposeBytes(long value) {
        return new long[] {
            (value >> 24) & 0xFF,
            (value >> 16) & 0xFF,
            (value >> 8) & 0xFF,
            value & 0xFF
        };
    }

    public long composeBytes(long b0, long b1, long b2, long b3) {
        for (long b : new long[]{b0, b1, b2, b3}) {
            if (b < 0 || b > 255) {
                throw new IllegalArgumentException("Each byte must be 0-255");
            }
        }
        return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
    }

    public String bitwiseOps(String op, Long a, Long b) {
        switch (op.toLowerCase()) {
            case "not":
                if (a == null) throw new IllegalArgumentException("'a' is required for NOT");
                return String.valueOf(~a);
            case "and":
                if (a == null || b == null) throw new IllegalArgumentException("'a' and 'b' are required");
                return String.valueOf(a & b);
            case "or":
                if (a == null || b == null) throw new IllegalArgumentException("'a' and 'b' are required");
                return String.valueOf(a | b);
            case "xor":
                if (a == null || b == null) throw new IllegalArgumentException("'a' and 'b' are required");
                return String.valueOf(a ^ b);
            case "shl":
                if (a == null || b == null) throw new IllegalArgumentException("'a' and 'b' are required");
                return String.valueOf(a << b);
            case "shr":
                if (a == null || b == null) throw new IllegalArgumentException("'a' and 'b' are required");
                return String.valueOf(a >> b);
            default:
                throw new IllegalArgumentException("Unknown op: " + op + ". Use: and, or, xor, not, shl, shr");
        }
    }
}
