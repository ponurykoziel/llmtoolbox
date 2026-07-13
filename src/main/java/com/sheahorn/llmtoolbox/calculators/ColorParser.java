package com.sheahorn.llmtoolbox.calculators;

public class ColorParser {

    private ColorParser() {}

    public static ColorDto from(String colorString) {
        if (colorString == null || colorString.isBlank()) {
            throw new IllegalArgumentException("color string is required");
        }

        String trimmed = colorString.trim();

        // CSS named color?
        if (CssColors.isNamedColor(trimmed)) {
            return parseHex(CssColors.hexFor(trimmed));
        }

        if (trimmed.startsWith("#")) {
            return parseHex(trimmed);
        }

        int paren = trimmed.indexOf('(');
        if (paren < 0 || !trimmed.endsWith(")")) {
            throw new IllegalArgumentException(
                "Invalid color format. Use color_space(tuple) e.g. rgb(255,0,0), hex #000000, or CSS name like dodgerblue");
        }

        String space = trimmed.substring(0, paren).trim().toLowerCase();
        String inner = trimmed.substring(paren + 1, trimmed.length() - 1).trim();

        return switch (space) {
            case "rgb" -> parseRgb(inner);
            case "hsl" -> parseHsl(inner);
            case "hsv" -> parseHsv(inner);
            case "cmyk" -> parseCmyk(inner);
            case "lab" -> parseLab(inner);
            case "lch" -> parseLch(inner);
            case "oklab" -> parseOkLab(inner);
            case "oklch" -> parseOkLch(inner);
            case "hwb" -> parseHwb(inner);
            case "ycbcr" -> parseYcbcr(inner);
            default -> throw new IllegalArgumentException(
                "Unknown color space: " + space + ". Use: hex, rgb, hsl, hsv, cmyk, lab, lch, oklab, oklch, hwb, ycbcr, or CSS name");
        };
    }

    public static String to(ColorDto color, ColorSpace targetSpace) {
        ColorDto converted = ColorConverter.convertTo(color, targetSpace);
        return dtoToString(converted);
    }

    /** Format an already-converted ColorDto as a string (no conversion step). */
    public static String dtoToString(ColorDto converted) {
        if (converted instanceof RgbColorDto rgb) {
            return "rgb(" + rgb.r + "," + rgb.g + "," + rgb.b + ")";
        } else if (converted instanceof HslColorDto hsl) {
            return "hsl(" + Math.round(hsl.h) + "," + Math.round(hsl.s) + "," + Math.round(hsl.l) + ")";
        } else if (converted instanceof HsvColorDto hsv) {
            return "hsv(" + Math.round(hsv.h) + "," + Math.round(hsv.s) + "," + Math.round(hsv.v) + ")";
        } else if (converted instanceof CmykColorDto cmyk) {
            return "cmyk(" + Math.round(cmyk.c) + "," + Math.round(cmyk.m) + ","
                         + Math.round(cmyk.y) + "," + Math.round(cmyk.k) + ")";
        } else if (converted instanceof HexColorDto hex) {
            return hex.hex;
        } else if (converted instanceof LabColorDto lab) {
            return "lab(" + round2(lab.L) + "," + round2(lab.a) + "," + round2(lab.b) + ")";
        } else if (converted instanceof LchColorDto lch) {
            return "lch(" + round2(lch.L) + "," + round2(lch.C) + "," + round2(lch.h) + ")";
        } else if (converted instanceof OkLabColorDto oklab) {
            return "oklab(" + round4(oklab.L) + "," + round4(oklab.a) + "," + round4(oklab.b) + ")";
        } else if (converted instanceof OkLchColorDto oklch) {
            return "oklch(" + round4(oklch.L) + "," + round4(oklch.C) + "," + round2(oklch.h) + ")";
        } else if (converted instanceof HwbColorDto hwb) {
            return "hwb(" + Math.round(hwb.h) + "," + Math.round(hwb.w) + "," + Math.round(hwb.b) + ")";
        } else if (converted instanceof YcbcrColorDto ycbcr) {
            return "ycbcr(" + round2(ycbcr.y) + "," + round2(ycbcr.cb) + "," + round2(ycbcr.cr) + ")";
        } else {
            throw new IllegalArgumentException("Unknown ColorDto type");
        }
    }

    // --- private parsers ---

    private static HexColorDto parseHex(String hex) {
        String h = hex.replace("#", "");
        if (h.length() == 3) {
            h = "" + h.charAt(0) + h.charAt(0)
                  + h.charAt(1) + h.charAt(1)
                  + h.charAt(2) + h.charAt(2);
        }
        if (h.length() != 6) {
            throw new IllegalArgumentException("Invalid hex color: " + hex);
        }
        for (char c : h.toCharArray()) {
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                throw new IllegalArgumentException("Invalid hex color: " + hex);
            }
        }
        return new HexColorDto("#" + h.toUpperCase());
    }

    private static RgbColorDto parseRgb(String inner) {
        double[] vals = splitDoubles(inner, 3);
        return new RgbColorDto(clamp255(vals[0]), clamp255(vals[1]), clamp255(vals[2]));
    }

    private static HslColorDto parseHsl(String inner) {
        double[] vals = splitDoubles(inner, 3);
        return new HslColorDto(vals[0], vals[1], vals[2]);
    }

    private static HsvColorDto parseHsv(String inner) {
        double[] vals = splitDoubles(inner, 3);
        return new HsvColorDto(vals[0], vals[1], vals[2]);
    }

    private static CmykColorDto parseCmyk(String inner) {
        double[] vals = splitDoubles(inner, 4);
        return new CmykColorDto(vals[0], vals[1], vals[2], vals[3]);
    }

    private static LabColorDto parseLab(String inner) {
        double[] vals = splitDoubles(inner, 3);
        return new LabColorDto(vals[0], vals[1], vals[2]);
    }

    private static LchColorDto parseLch(String inner) {
        double[] vals = splitDoubles(inner, 3);
        return new LchColorDto(vals[0], vals[1], vals[2]);
    }

    private static OkLabColorDto parseOkLab(String inner) {
        double[] vals = splitDoubles(inner, 3);
        return new OkLabColorDto(vals[0], vals[1], vals[2]);
    }

    private static OkLchColorDto parseOkLch(String inner) {
        double[] vals = splitDoubles(inner, 3);
        return new OkLchColorDto(vals[0], vals[1], vals[2]);
    }

    private static HwbColorDto parseHwb(String inner) {
        double[] vals = splitDoubles(inner, 3);
        return new HwbColorDto(vals[0], vals[1], vals[2]);
    }

    private static YcbcrColorDto parseYcbcr(String inner) {
        double[] vals = splitDoubles(inner, 3);
        return new YcbcrColorDto(vals[0], vals[1], vals[2]);
    }

    private static double[] splitDoubles(String inner, int expected) {
        String[] parts = inner.split(",");
        if (parts.length != expected) {
            throw new IllegalArgumentException(
                "Expected " + expected + " values, got " + parts.length + " in: " + inner);
        }
        double[] vals = new double[expected];
        for (int i = 0; i < expected; i++) {
            vals[i] = Double.parseDouble(parts[i].trim());
        }
        return vals;
    }

    private static int clamp255(double v) {
        return (int) Math.max(0, Math.min(255, Math.round(v)));
    }

    private static String round2(double v) {
        return String.format("%.2f", v);
    }

    private static String round4(double v) {
        return String.format("%.4f", v);
    }
}
