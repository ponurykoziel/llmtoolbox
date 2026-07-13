package com.sheahorn.llmtoolbox.calculators;

public class ColorConverter {

    private ColorConverter() {}

    // ==================== sRGB-path conversion (for color_convert endpoint and WCAG) ====================

    @SuppressWarnings("unchecked")
    public static <T extends ColorDto> T convertTo(ColorDto color, ColorSpace target) {
        if (matches(color, target)) {
            return (T) color;
        }

        double[] rgb = toRgbDoubles(color);

        return (T) switch (target) {
            case RGB -> new RgbColorDto(
                clamp255(rgb[0] * 255), clamp255(rgb[1] * 255), clamp255(rgb[2] * 255));
            case HEX -> {
                int ri = clamp255(rgb[0] * 255);
                int gi = clamp255(rgb[1] * 255);
                int bi = clamp255(rgb[2] * 255);
                yield new HexColorDto(String.format("#%02X%02X%02X", ri, gi, bi));
            }
            case HSL -> {
                double[] hsl = rgbToHsl(rgb[0], rgb[1], rgb[2]);
                yield new HslColorDto(
                    Math.round(hsl[0]), Math.round(hsl[1] * 100), Math.round(hsl[2] * 100));
            }
            case HSV -> {
                double[] hsv = rgbToHsv(rgb[0], rgb[1], rgb[2]);
                yield new HsvColorDto(
                    Math.round(hsv[0]), Math.round(hsv[1] * 100), Math.round(hsv[2] * 100));
            }
            case CMYK -> {
                double k = 1 - Math.max(rgb[0], Math.max(rgb[1], rgb[2]));
                if (k >= 1.0 - 1e-9) {
                    yield new CmykColorDto(0, 0, 0, 100);
                }
                double c = (1 - rgb[0] - k) / (1 - k);
                double m = (1 - rgb[1] - k) / (1 - k);
                double y = (1 - rgb[2] - k) / (1 - k);
                yield new CmykColorDto(
                    Math.round(c * 100), Math.round(m * 100),
                    Math.round(y * 100), Math.round(k * 100));
            }
            case LAB -> {
                double[] xyz = linearRgbToXyz(srgbToLinear(rgb));
                double[] lab = xyzToLab(xyz[0], xyz[1], xyz[2]);
                yield new LabColorDto(
                    Math.round(lab[0] * 100.0) / 100.0,
                    Math.round(lab[1] * 100.0) / 100.0,
                    Math.round(lab[2] * 100.0) / 100.0);
            }
            case LCH -> {
                double[] xyz = linearRgbToXyz(srgbToLinear(rgb));
                double[] lab = xyzToLab(xyz[0], xyz[1], xyz[2]);
                double[] lch = labToLch(lab[0], lab[1], lab[2]);
                yield new LchColorDto(
                    Math.round(lch[0] * 100.0) / 100.0,
                    Math.round(lch[1] * 100.0) / 100.0,
                    Math.round(lch[2] * 100.0) / 100.0);
            }
            case OKLAB -> {
                double[] oklab = linearRgbToOkLab(srgbToLinear(rgb));
                yield new OkLabColorDto(
                    Math.round(oklab[0] * 10000.0) / 10000.0,
                    Math.round(oklab[1] * 10000.0) / 10000.0,
                    Math.round(oklab[2] * 10000.0) / 10000.0);
            }
            case OKLCH -> {
                double[] oklab = linearRgbToOkLab(srgbToLinear(rgb));
                double[] oklch = labToLch(oklab[0], oklab[1], oklab[2]);
                yield new OkLchColorDto(
                    Math.round(oklch[0] * 10000.0) / 10000.0,
                    Math.round(oklch[1] * 10000.0) / 10000.0,
                    Math.round(oklch[2] * 100.0) / 100.0);
            }
            case HWB -> {
                double[] hsv = rgbToHsv(rgb[0], rgb[1], rgb[2]);
                double w = (1 - hsv[1]) * hsv[2];
                double b = 1 - hsv[2];
                yield new HwbColorDto(
                    Math.round(hsv[0]), Math.round(w * 100), Math.round(b * 100));
            }
            case YCBCR -> {
                double[] ycbcr = rgbToYcbcr(rgb[0], rgb[1], rgb[2]);
                yield new YcbcrColorDto(
                    Math.round(ycbcr[0] * 100.0) / 100.0,
                    Math.round(ycbcr[1] * 100.0) / 100.0,
                    Math.round(ycbcr[2] * 100.0) / 100.0);
            }
        };
    }

    // ==================== lossless linear-RGB path (no gamma clamp for wide-gamut spaces) ====================

    /**
     * Convert any ColorDto to linear RGB (unbounded, no gamma compression).
     * For sRGB-native spaces (RGB, Hex, HSL, HSV, CMYK, HWB, YCbCr) this gamma-expands
     * from sRGB. For wide-gamut spaces (LAB, LCH, OKLab, OKLCh) this goes through XYZ
     * directly, preserving out-of-sRGB-gamut values.
     */
    public static double[] toLinearRgb(ColorDto color) {
        if (color instanceof RgbColorDto rgb) {
            return srgbToLinear(new double[]{ rgb.r / 255.0, rgb.g / 255.0, rgb.b / 255.0 });
        } else if (color instanceof HexColorDto hex) {
            String h = hex.hex.replace("#", "");
            return srgbToLinear(new double[]{
                Integer.parseInt(h.substring(0, 2), 16) / 255.0,
                Integer.parseInt(h.substring(2, 4), 16) / 255.0,
                Integer.parseInt(h.substring(4, 6), 16) / 255.0
            });
        } else if (color instanceof HslColorDto hsl) {
            return srgbToLinear(hslToRgb(hsl.h, hsl.s / 100.0, hsl.l / 100.0));
        } else if (color instanceof HsvColorDto hsv) {
            return srgbToLinear(hsvToRgb(hsv.h, hsv.s / 100.0, hsv.v / 100.0));
        } else if (color instanceof CmykColorDto cmyk) {
            double c = cmyk.c / 100.0, m = cmyk.m / 100.0, y = cmyk.y / 100.0, k = cmyk.k / 100.0;
            return srgbToLinear(new double[]{
                (1 - c) * (1 - k), (1 - m) * (1 - k), (1 - y) * (1 - k)
            });
        } else if (color instanceof LabColorDto lab) {
            double[] xyz = labToXyz(lab.L, lab.a, lab.b);
            return xyzToLinearRgb(xyz[0], xyz[1], xyz[2]);
        } else if (color instanceof LchColorDto lch) {
            double[] lab = lchToLab(lch.L, lch.C, lch.h);
            double[] xyz = labToXyz(lab[0], lab[1], lab[2]);
            return xyzToLinearRgb(xyz[0], xyz[1], xyz[2]);
        } else if (color instanceof OkLabColorDto oklab) {
            return okLabToLinearRgb(oklab.L, oklab.a, oklab.b);
        } else if (color instanceof OkLchColorDto oklch) {
            double[] oklab = lchToLab(oklch.L, oklch.C, oklch.h);
            return okLabToLinearRgb(oklab[0], oklab[1], oklab[2]);
        } else if (color instanceof HwbColorDto hwb) {
            double w = hwb.w / 100.0, b = hwb.b / 100.0;
            if (w + b >= 1.0) {
                double gray = w / (w + b);
                return srgbToLinear(new double[]{ gray, gray, gray });
            }
            double v = 1 - b, s = 1 - w / v;
            return srgbToLinear(hsvToRgb(hwb.h, s, v));
        } else if (color instanceof YcbcrColorDto ycbcr) {
            return srgbToLinear(ycbcrToRgb(ycbcr.y, ycbcr.cb, ycbcr.cr));
        }
        throw new IllegalArgumentException("Unknown ColorDto type");
    }

    /**
     * Convert linear RGB (unbounded) to any target ColorSpace.
     * For sRGB-bound targets (RGB, HEX, HSL, HSV, CMYK, HWB, YCbCr) this gamma-compresses
     * first. For wide-gamut targets (LAB, LCH, OKLAB, OKLCH) this goes through XYZ directly,
     * preserving values outside the sRGB gamut.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ColorDto> T linearRgbTo(ColorSpace target, double[] linear) {
        return (T) switch (target) {
            case RGB -> {
                double[] srgb = linearRgbToSrgb(linear);
                yield new RgbColorDto(
                    clamp255(srgb[0] * 255), clamp255(srgb[1] * 255), clamp255(srgb[2] * 255));
            }
            case HEX -> {
                double[] srgb = linearRgbToSrgb(linear);
                int ri = clamp255(srgb[0] * 255);
                int gi = clamp255(srgb[1] * 255);
                int bi = clamp255(srgb[2] * 255);
                yield new HexColorDto(String.format("#%02X%02X%02X", ri, gi, bi));
            }
            case HSL -> {
                double[] srgb = linearRgbToSrgb(linear);
                double[] hsl = rgbToHsl(srgb[0], srgb[1], srgb[2]);
                yield new HslColorDto(
                    Math.round(hsl[0]), Math.round(hsl[1] * 100), Math.round(hsl[2] * 100));
            }
            case HSV -> {
                double[] srgb = linearRgbToSrgb(linear);
                double[] hsv = rgbToHsv(srgb[0], srgb[1], srgb[2]);
                yield new HsvColorDto(
                    Math.round(hsv[0]), Math.round(hsv[1] * 100), Math.round(hsv[2] * 100));
            }
            case CMYK -> {
                double[] srgb = linearRgbToSrgb(linear);
                double k = 1 - Math.max(srgb[0], Math.max(srgb[1], srgb[2]));
                if (k >= 1.0 - 1e-9) {
                    yield new CmykColorDto(0, 0, 0, 100);
                }
                double c = (1 - srgb[0] - k) / (1 - k);
                double m = (1 - srgb[1] - k) / (1 - k);
                double y = (1 - srgb[2] - k) / (1 - k);
                yield new CmykColorDto(
                    Math.round(c * 100), Math.round(m * 100),
                    Math.round(y * 100), Math.round(k * 100));
            }
            case LAB -> {
                double[] xyz = linearRgbToXyz(linear);
                double[] lab = xyzToLab(xyz[0], xyz[1], xyz[2]);
                yield new LabColorDto(
                    Math.round(lab[0] * 100.0) / 100.0,
                    Math.round(lab[1] * 100.0) / 100.0,
                    Math.round(lab[2] * 100.0) / 100.0);
            }
            case LCH -> {
                double[] xyz = linearRgbToXyz(linear);
                double[] lab = xyzToLab(xyz[0], xyz[1], xyz[2]);
                double[] lch = labToLch(lab[0], lab[1], lab[2]);
                yield new LchColorDto(
                    Math.round(lch[0] * 100.0) / 100.0,
                    Math.round(lch[1] * 100.0) / 100.0,
                    Math.round(lch[2] * 100.0) / 100.0);
            }
            case OKLAB -> {
                double[] oklab = linearRgbToOkLab(linear);
                yield new OkLabColorDto(
                    Math.round(oklab[0] * 10000.0) / 10000.0,
                    Math.round(oklab[1] * 10000.0) / 10000.0,
                    Math.round(oklab[2] * 10000.0) / 10000.0);
            }
            case OKLCH -> {
                double[] oklab = linearRgbToOkLab(linear);
                double[] oklch = labToLch(oklab[0], oklab[1], oklab[2]);
                yield new OkLchColorDto(
                    Math.round(oklch[0] * 10000.0) / 10000.0,
                    Math.round(oklch[1] * 10000.0) / 10000.0,
                    Math.round(oklch[2] * 100.0) / 100.0);
            }
            case HWB -> {
                double[] srgb = linearRgbToSrgb(linear);
                double[] hsv = rgbToHsv(srgb[0], srgb[1], srgb[2]);
                double w = (1 - hsv[1]) * hsv[2];
                double b = 1 - hsv[2];
                yield new HwbColorDto(
                    Math.round(hsv[0]), Math.round(w * 100), Math.round(b * 100));
            }
            case YCBCR -> {
                double[] srgb = linearRgbToSrgb(linear);
                double[] ycbcr = rgbToYcbcr(srgb[0], srgb[1], srgb[2]);
                yield new YcbcrColorDto(
                    Math.round(ycbcr[0] * 100.0) / 100.0,
                    Math.round(ycbcr[1] * 100.0) / 100.0,
                    Math.round(ycbcr[2] * 100.0) / 100.0);
            }
        };
    }

    // --- internal RGB extraction (from any DTO, sRGB path) ---

    private static double[] toRgbDoubles(ColorDto color) {
        if (color instanceof RgbColorDto rgb) {
            return new double[]{ rgb.r / 255.0, rgb.g / 255.0, rgb.b / 255.0 };
        } else if (color instanceof HexColorDto hex) {
            String h = hex.hex.replace("#", "");
            return new double[]{
                Integer.parseInt(h.substring(0, 2), 16) / 255.0,
                Integer.parseInt(h.substring(2, 4), 16) / 255.0,
                Integer.parseInt(h.substring(4, 6), 16) / 255.0
            };
        } else if (color instanceof HslColorDto hsl) {
            return hslToRgb(hsl.h, hsl.s / 100.0, hsl.l / 100.0);
        } else if (color instanceof HsvColorDto hsv) {
            return hsvToRgb(hsv.h, hsv.s / 100.0, hsv.v / 100.0);
        } else if (color instanceof CmykColorDto cmyk) {
            double c = cmyk.c / 100.0;
            double m = cmyk.m / 100.0;
            double y = cmyk.y / 100.0;
            double k = cmyk.k / 100.0;
            return new double[]{
                (1 - c) * (1 - k),
                (1 - m) * (1 - k),
                (1 - y) * (1 - k)
            };
        } else if (color instanceof LabColorDto lab) {
            double[] xyz = labToXyz(lab.L, lab.a, lab.b);
            return linearRgbToSrgb(xyzToLinearRgb(xyz[0], xyz[1], xyz[2]));
        } else if (color instanceof LchColorDto lch) {
            double[] lab = lchToLab(lch.L, lch.C, lch.h);
            double[] xyz = labToXyz(lab[0], lab[1], lab[2]);
            return linearRgbToSrgb(xyzToLinearRgb(xyz[0], xyz[1], xyz[2]));
        } else if (color instanceof OkLabColorDto oklab) {
            return linearRgbToSrgb(okLabToLinearRgb(oklab.L, oklab.a, oklab.b));
        } else if (color instanceof OkLchColorDto oklch) {
            double[] oklab = lchToLab(oklch.L, oklch.C, oklch.h);
            return linearRgbToSrgb(okLabToLinearRgb(oklab[0], oklab[1], oklab[2]));
        } else if (color instanceof HwbColorDto hwb) {
            double w = hwb.w / 100.0;
            double b = hwb.b / 100.0;
            if (w + b >= 1.0) {
                double gray = w / (w + b);
                return new double[]{ gray, gray, gray };
            }
            double v = 1 - b;
            double s = 1 - w / v;
            return hsvToRgb(hwb.h, s, v);
        } else if (color instanceof YcbcrColorDto ycbcr) {
            return ycbcrToRgb(ycbcr.y, ycbcr.cb, ycbcr.cr);
        }
        throw new IllegalArgumentException("Unknown ColorDto type");
    }

    private static boolean matches(ColorDto color, ColorSpace target) {
        return switch (target) {
            case RGB -> color instanceof RgbColorDto;
            case HEX -> color instanceof HexColorDto;
            case HSL -> color instanceof HslColorDto;
            case HSV -> color instanceof HsvColorDto;
            case CMYK -> color instanceof CmykColorDto;
            case LAB -> color instanceof LabColorDto;
            case LCH -> color instanceof LchColorDto;
            case OKLAB -> color instanceof OkLabColorDto;
            case OKLCH -> color instanceof OkLchColorDto;
            case HWB -> color instanceof HwbColorDto;
            case YCBCR -> color instanceof YcbcrColorDto;
        };
    }

    // --- math helpers ---

    private static int clamp255(double v) {
        return (int) Math.max(0, Math.min(255, Math.round(v)));
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    // --- sRGB ↔ linear RGB ---

    private static double[] srgbToLinear(double[] srgb) {
        return new double[]{
            srgbGammaExpand(srgb[0]),
            srgbGammaExpand(srgb[1]),
            srgbGammaExpand(srgb[2])
        };
    }

    private static double srgbGammaExpand(double c) {
        if (c <= 0.04045) return c / 12.92;
        return Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static double[] linearRgbToSrgb(double[] linear) {
        return new double[]{
            srgbGammaCompress(linear[0]),
            srgbGammaCompress(linear[1]),
            srgbGammaCompress(linear[2])
        };
    }

    private static double srgbGammaCompress(double c) {
        if (c <= 0.0031308) return c * 12.92;
        return 1.055 * Math.pow(c, 1.0 / 2.4) - 0.055;
    }

    // --- linear RGB ↔ XYZ (sRGB D65) ---

    private static double[] linearRgbToXyz(double[] linear) {
        double x = 0.4124564 * linear[0] + 0.3575761 * linear[1] + 0.1804375 * linear[2];
        double y = 0.2126729 * linear[0] + 0.7151522 * linear[1] + 0.0721750 * linear[2];
        double z = 0.0193339 * linear[0] + 0.1191920 * linear[1] + 0.9503041 * linear[2];
        return new double[]{ x, y, z };
    }

    private static double[] xyzToLinearRgb(double x, double y, double z) {
        double r =  3.2404542 * x - 1.5371385 * y - 0.4985314 * z;
        double g = -0.9692660 * x + 1.8760108 * y + 0.0415560 * z;
        double b =  0.0556434 * x - 0.2040259 * y + 1.0572252 * z;
        return new double[]{ r, g, b };
    }

    // --- XYZ ↔ CIELAB (D65 reference white) ---

    private static final double REF_X = 0.95047;
    private static final double REF_Y = 1.00000;
    private static final double REF_Z = 1.08883;

    private static double[] xyzToLab(double x, double y, double z) {
        double fx = labF(x / REF_X);
        double fy = labF(y / REF_Y);
        double fz = labF(z / REF_Z);
        double L = 116.0 * fy - 16.0;
        double a = 500.0 * (fx - fy);
        double b = 200.0 * (fy - fz);
        return new double[]{ L, a, b };
    }

    private static double[] labToXyz(double L, double a, double b) {
        double fy = (L + 16.0) / 116.0;
        double fx = a / 500.0 + fy;
        double fz = fy - b / 200.0;
        double x = labFInv(fx) * REF_X;
        double y = labFInv(fy) * REF_Y;
        double z = labFInv(fz) * REF_Z;
        return new double[]{ x, y, z };
    }

    private static double labF(double t) {
        double delta = 6.0 / 29.0;
        if (t > delta * delta * delta) return Math.cbrt(t);
        return t / (3.0 * delta * delta) + 4.0 / 29.0;
    }

    private static double labFInv(double t) {
        double delta = 6.0 / 29.0;
        if (t > delta) return t * t * t;
        return 3.0 * delta * delta * (t - 4.0 / 29.0);
    }

    // --- Lab ↔ LCh ---

    private static double[] labToLch(double L, double a, double b) {
        double C = Math.sqrt(a * a + b * b);
        double h = Math.toDegrees(Math.atan2(b, a));
        if (h < 0) h += 360.0;
        return new double[]{ L, C, h };
    }

    private static double[] lchToLab(double L, double C, double h) {
        double hr = Math.toRadians(h);
        double a = C * Math.cos(hr);
        double b = C * Math.sin(hr);
        return new double[]{ L, a, b };
    }

    // --- linear RGB ↔ OKLab ---

    private static double[] linearRgbToOkLab(double[] linear) {
        double l = 0.4122214708 * linear[0] + 0.5363325363 * linear[1] + 0.0514459929 * linear[2];
        double m = 0.2119034982 * linear[0] + 0.6806995451 * linear[1] + 0.1073969566 * linear[2];
        double s = 0.0883024619 * linear[0] + 0.2817188376 * linear[1] + 0.6299787005 * linear[2];
        double lc = Math.cbrt(l);
        double mc = Math.cbrt(m);
        double sc = Math.cbrt(s);
        double L = 0.2104542553 * lc + 0.7936177850 * mc - 0.0040720468 * sc;
        double a = 1.9779984951 * lc - 2.4285922050 * mc + 0.4505937099 * sc;
        double b = 0.0259040371 * lc + 0.7827717662 * mc - 0.8086757660 * sc;
        return new double[]{ L, a, b };
    }

    private static double[] okLabToLinearRgb(double L, double a, double b) {
        double lc = L + 0.3963377774 * a + 0.2158037573 * b;
        double mc = L - 0.1055613458 * a - 0.0638541728 * b;
        double sc = L - 0.0894841775 * a - 1.2914855480 * b;
        double l = lc * lc * lc;
        double m = mc * mc * mc;
        double s = sc * sc * sc;
        double r =  4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s;
        double g = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s;
        double bl = -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s;
        return new double[]{ r, g, bl };
    }

    // --- RGB ↔ YCbCr (full-range / JFIF) ---

    private static double[] rgbToYcbcr(double r, double g, double b) {
        double y  =  0.299 * r + 0.587 * g + 0.114 * b;
        double cb = -0.168736 * r - 0.331264 * g + 0.5 * b + 0.5;
        double cr =  0.5 * r - 0.418688 * g - 0.081312 * b + 0.5;
        return new double[]{ y, cb, cr };
    }

    private static double[] ycbcrToRgb(double y, double cb, double cr) {
        double r = y + 1.402 * (cr - 0.5);
        double g = y - 0.344136 * (cb - 0.5) - 0.714136 * (cr - 0.5);
        double b = y + 1.772 * (cb - 0.5);
        return new double[]{ clamp01(r), clamp01(g), clamp01(b) };
    }

    // --- HSL ---

    private static double[] rgbToHsl(double r, double g, double b) {
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double h, s, l = (max + min) / 2.0;
        double d = max - min;
        if (d == 0) {
            h = 0; s = 0;
        } else {
            s = l > 0.5 ? d / (2.0 - max - min) : d / (max + min);
            if (max == r) h = ((g - b) / d + (g < b ? 6 : 0)) / 6.0;
            else if (max == g) h = ((b - r) / d + 2) / 6.0;
            else h = ((r - g) / d + 4) / 6.0;
        }
        return new double[]{ h * 360, s, l };
    }

    private static double[] hslToRgb(double h, double s, double l) {
        h /= 360.0;
        if (s == 0) return new double[]{ l, l, l };
        double q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        double p = 2 * l - q;
        return new double[]{
            hueToRgb(p, q, h + 1.0 / 3.0),
            hueToRgb(p, q, h),
            hueToRgb(p, q, h - 1.0 / 3.0)
        };
    }

    private static double hueToRgb(double p, double q, double t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1.0 / 6.0) return p + (q - p) * 6 * t;
        if (t < 1.0 / 2.0) return q;
        if (t < 2.0 / 3.0) return p + (q - p) * (2.0 / 3.0 - t) * 6;
        return p;
    }

    // --- HSV ---

    private static double[] rgbToHsv(double r, double g, double b) {
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double h, s, v = max;
        double d = max - min;
        if (d == 0) {
            h = 0; s = 0;
        } else {
            s = d / max;
            if (max == r) h = ((g - b) / d + (g < b ? 6 : 0)) / 6.0;
            else if (max == g) h = ((b - r) / d + 2) / 6.0;
            else h = ((r - g) / d + 4) / 6.0;
        }
        return new double[]{ h * 360, s, v };
    }

    private static double[] hsvToRgb(double h, double s, double v) {
        h /= 360.0;
        if (s == 0) return new double[]{ v, v, v };
        int i = (int) (h * 6);
        double f = h * 6 - i;
        double p = v * (1 - s);
        double q = v * (1 - f * s);
        double t = v * (1 - (1 - f) * s);
        return switch (i % 6) {
            case 0 -> new double[]{ v, t, p };
            case 1 -> new double[]{ q, v, p };
            case 2 -> new double[]{ p, v, t };
            case 3 -> new double[]{ p, q, v };
            case 4 -> new double[]{ t, p, v };
            default -> new double[]{ v, p, q };
        };
    }
}
