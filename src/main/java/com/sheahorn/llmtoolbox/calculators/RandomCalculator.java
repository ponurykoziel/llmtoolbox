package com.sheahorn.llmtoolbox.calculators;

import java.security.SecureRandom;
import java.util.UUID;

import com.sheahorn.llmtoolbox.calculators.common.Calculator;

public class RandomCalculator implements Calculator {

    private static final SecureRandom RNG = new SecureRandom();
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String LETTERS_NUMBERS = LETTERS + NUMBERS;

    public String randomValue(String type, Long min, Long max, Double mean, Double stddev) {
        switch (type.toLowerCase()) {
            case "byte":
                byte[] b = new byte[1];
                RNG.nextBytes(b);
                return String.valueOf(b[0]);
            case "boolean":
                return String.valueOf(RNG.nextBoolean());
            case "int":
                if (min != null && max != null) {
                    if (min > max) throw new IllegalArgumentException("min must be <= max");
                    if (max >= Long.MAX_VALUE - 1) throw new IllegalArgumentException("max too large for bounded range");
                    return String.valueOf(RNG.nextLong(min, max + 1));
                } else if (min != null || max != null) {
                    throw new IllegalArgumentException("Both min and max are required for bounded int generation");
                } else {
                    return String.valueOf(RNG.nextInt());
                }
            case "long":
                return String.valueOf(RNG.nextLong());
            case "float":
                return String.valueOf(RNG.nextFloat());
            case "double":
                return String.valueOf(RNG.nextDouble());
            case "gaussian":
                double m = mean != null ? mean : 0.0;
                double s = stddev != null ? stddev : 1.0;
                if (s < 0.0) throw new IllegalArgumentException("stddev must be non-negative");
                return String.valueOf(m + s * RNG.nextGaussian());
            case "uuid":
                return UUID.randomUUID().toString();
            default:
                throw new IllegalArgumentException("Unknown type: " + type + ". Use: byte, boolean, int, long, float, double, gaussian, uuid");
        }
    }

    public String randomText(int length, String charset, String customChars) {
        if (length < 1 || length > 10000) {
            throw new IllegalArgumentException("length must be 1-10000");
        }

        String chars;
        if (charset == null || charset.isBlank()) {
            chars = LETTERS_NUMBERS;
        } else {
            switch (charset.toLowerCase()) {
                case "letters":
                    chars = LETTERS;
                    break;
                case "numbers":
                    chars = NUMBERS;
                    break;
                case "letters_numbers":
                    chars = LETTERS_NUMBERS;
                    break;
                case "custom":
                    if (customChars == null || customChars.isEmpty()) {
                        throw new IllegalArgumentException("customChars is required for charset=custom");
                    }
                    chars = customChars;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown charset: " + charset + ". Use: letters, numbers, letters_numbers, custom");
            }
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RNG.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
