package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.common.Calculator;

public class ColorCalculator implements Calculator {

    // ==================== linear-RGB operations (lossless, no gamma clamp) ====================

    /** Invert in linear RGB: (1 - R, 1 - G, 1 - B) */
    public double[] invertLinear(double[] linear) {
        return new double[]{ 1.0 - linear[0], 1.0 - linear[1], 1.0 - linear[2] };
    }

    /** Adjust contrast in linear RGB by scaling around 0.5. f = 1 + amount. */
    public double[] adjustContrastLinear(double[] linear, double amount) {
        double f = 1.0 + amount;
        return new double[]{
            clamp01((linear[0] - 0.5) * f + 0.5),
            clamp01((linear[1] - 0.5) * f + 0.5),
            clamp01((linear[2] - 0.5) * f + 0.5)
        };
    }

    /** Level stretch in linear RGB: remap [low, high] to [0, 1] with gamma. */
    public double[] levelStretchLinear(double[] linear, double blackDelta, double whiteDelta, double grayDelta) {
        double low = clamp01(blackDelta / 255.0);
        double high = 1.0 - clamp01(whiteDelta / 255.0);
        double range = high - low;
        if (range <= 0) range = 1.0;
        return new double[]{
            stretchChannelLinear(linear[0], low, range, grayDelta),
            stretchChannelLinear(linear[1], low, range, grayDelta),
            stretchChannelLinear(linear[2], low, range, grayDelta)
        };
    }

    private double stretchChannelLinear(double v, double low, double range, double gamma) {
        double t = (v - low) / range;
        t = clamp01(t);
        t = Math.pow(t, gamma);
        return t;
    }

    /** Level squeeze in linear RGB: remap [0, 1] to [low, high] with gamma. */
    public double[] levelSqueezeLinear(double[] linear, double blackDelta, double whiteDelta, double grayDelta) {
        double low = clamp01(blackDelta / 255.0);
        double high = 1.0 - clamp01(whiteDelta / 255.0);
        double range = high - low;
        return new double[]{
            squeezeChannelLinear(linear[0], low, range, grayDelta),
            squeezeChannelLinear(linear[1], low, range, grayDelta),
            squeezeChannelLinear(linear[2], low, range, grayDelta)
        };
    }

    private double squeezeChannelLinear(double v, double low, double range, double gamma) {
        double t = clamp01(v);
        t = Math.pow(t, gamma);
        return low + t * range;
    }

    /** Offset red channel in linear RGB. */
    public double[] adjustRedLinear(double[] linear, double amount) {
        return new double[]{ linear[0] + amount / 255.0, linear[1], linear[2] };
    }

    /** Offset green channel in linear RGB. */
    public double[] adjustGreenLinear(double[] linear, double amount) {
        return new double[]{ linear[0], linear[1] + amount / 255.0, linear[2] };
    }

    /** Offset blue channel in linear RGB. */
    public double[] adjustBlueLinear(double[] linear, double amount) {
        return new double[]{ linear[0], linear[1], linear[2] + amount / 255.0 };
    }

    /** Additive brightness in linear RGB. */
    public double[] adjustBrightnessLinear(double[] linear, double amount) {
        double da = amount / 255.0;
        return new double[]{
            linear[0] + da,
            linear[1] + da,
            linear[2] + da
        };
    }

    /** Multiplicative luminance in linear RGB. */
    public double[] adjustLuminanceLinear(double[] linear, double amount) {
        double factor = 1.0 + amount;
        return new double[]{
            linear[0] * factor,
            linear[1] * factor,
            linear[2] * factor
        };
    }

    /** Interpolate toward luminance gray in linear RGB. */
    public double[] adjustLuminanceSaturationLinear(double[] linear, double amount) {
        double gray = 0.2126 * linear[0] + 0.7152 * linear[1] + 0.0722 * linear[2];
        return new double[]{
            linear[0] + amount * (gray - linear[0]),
            linear[1] + amount * (gray - linear[1]),
            linear[2] + amount * (gray - linear[2])
        };
    }

    /** Grayscale via relative luminance in linear RGB. */
    public double[] grayscaleLinear(double[] linear) {
        double gray = 0.2126 * linear[0] + 0.7152 * linear[1] + 0.0722 * linear[2];
        return new double[]{ gray, gray, gray };
    }

    /** Mix two colors in linear RGB by ratio (0 = from, 1 = to). */
    public double[] mixLinear(double[] from, double[] to, double ratio) {
        double r = clamp01(ratio);
        return new double[]{
            from[0] + r * (to[0] - from[0]),
            from[1] + r * (to[1] - from[1]),
            from[2] + r * (to[2] - from[2])
        };
    }

    // ==================== sRGB-path operations (for WCAG contrast, backward compat) ====================

    public RgbColorDto invert(RgbColorDto color) {
        return new RgbColorDto(
            255 - color.r,
            255 - color.g,
            255 - color.b
        );
    }

    public RgbColorDto adjustContrast(RgbColorDto color, double amount) {
        double f = 1.0 + amount;
        return new RgbColorDto(
            clamp(contrastChannel(color.r, f)),
            clamp(contrastChannel(color.g, f)),
            clamp(contrastChannel(color.b, f))
        );
    }

    private int contrastChannel(int v, double f) {
        return (int) Math.round((v - 128) * f + 128);
    }

    public RgbColorDto levelStretch(RgbColorDto color, double blackDelta, double whiteDelta, double grayDelta) {
        double low = clampDelta(blackDelta);
        double high = 255.0 - clampDelta(whiteDelta);
        double range = high - low;
        if (range <= 0) range = 1.0;

        return new RgbColorDto(
            stretchChannel(color.r, low, range, grayDelta),
            stretchChannel(color.g, low, range, grayDelta),
            stretchChannel(color.b, low, range, grayDelta)
        );
    }

    private int stretchChannel(int v, double low, double range, double gamma) {
        double t = (v - low) / range;
        t = clamp01(t);
        t = Math.pow(t, gamma);
        return clamp((int) Math.round(t * 255.0));
    }

    public RgbColorDto levelSqueeze(RgbColorDto color, double blackDelta, double whiteDelta, double grayDelta) {
        double low = clampDelta(blackDelta);
        double high = 255.0 - clampDelta(whiteDelta);
        double range = high - low;

        return new RgbColorDto(
            squeezeChannel(color.r, low, range, grayDelta),
            squeezeChannel(color.g, low, range, grayDelta),
            squeezeChannel(color.b, low, range, grayDelta)
        );
    }

    private int squeezeChannel(int v, double low, double range, double gamma) {
        double t = v / 255.0;
        t = clamp01(t);
        t = Math.pow(t, gamma);
        return clamp((int) Math.round(low + t * range));
    }

    public RgbColorDto adjustRed(RgbColorDto color, double amount) {
        return new RgbColorDto(clamp(color.r + (int) Math.round(amount)), color.g, color.b);
    }

    public RgbColorDto adjustGreen(RgbColorDto color, double amount) {
        return new RgbColorDto(color.r, clamp(color.g + (int) Math.round(amount)), color.b);
    }

    public RgbColorDto adjustBlue(RgbColorDto color, double amount) {
        return new RgbColorDto(color.r, color.g, clamp(color.b + (int) Math.round(amount)));
    }

    public RgbColorDto adjustBrightness(RgbColorDto color, double amount) {
        int dr = (int) Math.round(amount);
        return new RgbColorDto(
            clamp(color.r + dr),
            clamp(color.g + dr),
            clamp(color.b + dr)
        );
    }

    public RgbColorDto adjustLuminance(RgbColorDto color, double amount) {
        double factor = 1.0 + amount;
        return new RgbColorDto(
            clamp((int) Math.round(color.r * factor)),
            clamp((int) Math.round(color.g * factor)),
            clamp((int) Math.round(color.b * factor))
        );
    }

    public RgbColorDto adjustLuminanceSaturation(RgbColorDto color, double amount) {
        int gray = luminanceGray(color);
        return new RgbColorDto(
            clamp((int) Math.round(color.r + amount * (gray - color.r))),
            clamp((int) Math.round(color.g + amount * (gray - color.g))),
            clamp((int) Math.round(color.b + amount * (gray - color.b)))
        );
    }

    public RgbColorDto grayscale(RgbColorDto color) {
        int gray = luminanceGray(color);
        return new RgbColorDto(gray, gray, gray);
    }

    private int luminanceGray(RgbColorDto color) {
        return clamp((int) Math.round(0.299 * color.r + 0.587 * color.g + 0.114 * color.b));
    }

    // --- HSL operations ---

    public HslColorDto adjustHue(HslColorDto hsl, double amount) {
        double h = (hsl.h + amount) % 360.0;
        if (h < 0) h += 360.0;
        return new HslColorDto(h, hsl.s, hsl.l);
    }

    public HslColorDto adjustSaturation(HslColorDto hsl, double amount) {
        double s = clampRange(hsl.s * (1.0 + amount), 0.0, 100.0);
        return new HslColorDto(hsl.h, s, hsl.l);
    }

    public HslColorDto adjustLightness(HslColorDto hsl, double amount) {
        double l = clampRange(hsl.l * (1.0 + amount), 0.0, 100.0);
        return new HslColorDto(hsl.h, hsl.s, l);
    }

    // --- HSV operations ---

    public HsvColorDto adjustHue(HsvColorDto hsv, double amount) {
        double h = (hsv.h + amount) % 360.0;
        if (h < 0) h += 360.0;
        return new HsvColorDto(h, hsv.s, hsv.v);
    }

    public HsvColorDto adjustSaturation(HsvColorDto hsv, double amount) {
        double s = clampRange(hsv.s * (1.0 + amount), 0.0, 100.0);
        return new HsvColorDto(hsv.h, s, hsv.v);
    }

    public HsvColorDto adjustValue(HsvColorDto hsv, double amount) {
        double v = clampRange(hsv.v * (1.0 + amount), 0.0, 100.0);
        return new HsvColorDto(hsv.h, hsv.s, v);
    }

    // --- CIELAB operations ---

    public LabColorDto adjustLightness(LabColorDto lab, double amount) {
        double L = clampRange(lab.L * (1.0 + amount), 0.0, 100.0);
        return new LabColorDto(L, lab.a, lab.b);
    }

    public LabColorDto adjustA(LabColorDto lab, double amount) {
        return new LabColorDto(lab.L, lab.a + amount, lab.b);
    }

    public LabColorDto adjustB(LabColorDto lab, double amount) {
        return new LabColorDto(lab.L, lab.a, lab.b + amount);
    }

    // --- CIELCh operations ---

    public LchColorDto adjustLightness(LchColorDto lch, double amount) {
        double L = clampRange(lch.L * (1.0 + amount), 0.0, 100.0);
        return new LchColorDto(L, lch.C, lch.h);
    }

    public LchColorDto adjustChroma(LchColorDto lch, double amount) {
        double C = Math.max(0.0, lch.C * (1.0 + amount));
        return new LchColorDto(lch.L, C, lch.h);
    }

    public LchColorDto adjustHue(LchColorDto lch, double amount) {
        double h = (lch.h + amount) % 360.0;
        if (h < 0) h += 360.0;
        return new LchColorDto(lch.L, lch.C, h);
    }

    // --- OKLab operations ---

    public OkLabColorDto adjustLightness(OkLabColorDto oklab, double amount) {
        double L = clampRange(oklab.L * (1.0 + amount), 0.0, 1.0);
        return new OkLabColorDto(L, oklab.a, oklab.b);
    }

    public OkLabColorDto adjustA(OkLabColorDto oklab, double amount) {
        return new OkLabColorDto(oklab.L, oklab.a + amount, oklab.b);
    }

    public OkLabColorDto adjustB(OkLabColorDto oklab, double amount) {
        return new OkLabColorDto(oklab.L, oklab.a, oklab.b + amount);
    }

    // --- OKLCh operations ---

    public OkLchColorDto adjustLightness(OkLchColorDto oklch, double amount) {
        double L = clampRange(oklch.L * (1.0 + amount), 0.0, 1.0);
        return new OkLchColorDto(L, oklch.C, oklch.h);
    }

    public OkLchColorDto adjustChroma(OkLchColorDto oklch, double amount) {
        double C = Math.max(0.0, oklch.C * (1.0 + amount));
        return new OkLchColorDto(oklch.L, C, oklch.h);
    }

    public OkLchColorDto adjustHue(OkLchColorDto oklch, double amount) {
        double h = (oklch.h + amount) % 360.0;
        if (h < 0) h += 360.0;
        return new OkLchColorDto(oklch.L, oklch.C, h);
    }

    // --- HWB operations ---

    public HwbColorDto adjustHue(HwbColorDto hwb, double amount) {
        double h = (hwb.h + amount) % 360.0;
        if (h < 0) h += 360.0;
        return new HwbColorDto(h, hwb.w, hwb.b);
    }

    public HwbColorDto adjustWhiteness(HwbColorDto hwb, double amount) {
        double w = clampRange(hwb.w * (1.0 + amount), 0.0, 100.0);
        return new HwbColorDto(hwb.h, w, hwb.b);
    }

    public HwbColorDto adjustBlackness(HwbColorDto hwb, double amount) {
        double b = clampRange(hwb.b * (1.0 + amount), 0.0, 100.0);
        return new HwbColorDto(hwb.h, hwb.w, b);
    }

    // --- YCbCr operations ---

    public YcbcrColorDto adjustLuma(YcbcrColorDto ycbcr, double amount) {
        double y = clampRange(ycbcr.y * (1.0 + amount), 0.0, 1.0);
        return new YcbcrColorDto(y, ycbcr.cb, ycbcr.cr);
    }

    public YcbcrColorDto adjustBlueChroma(YcbcrColorDto ycbcr, double amount) {
        double cb = clampRange(ycbcr.cb + amount, 0.0, 1.0);
        return new YcbcrColorDto(ycbcr.y, cb, ycbcr.cr);
    }

    public YcbcrColorDto adjustRedChroma(YcbcrColorDto ycbcr, double amount) {
        double cr = clampRange(ycbcr.cr + amount, 0.0, 1.0);
        return new YcbcrColorDto(ycbcr.y, ycbcr.cb, cr);
    }

    // --- Delta-E ---

    public double deltaE(LabColorDto lab1, LabColorDto lab2, String formula) {
        return switch (formula.toUpperCase().trim()) {
            case "CIE76" -> deltaE76(lab1, lab2);
            case "CIE94" -> deltaE94(lab1, lab2);
            case "CIEDE2000" -> deltaE2000(lab1, lab2);
            default -> throw new IllegalArgumentException(
                "Unknown delta-E formula: " + formula + ". Use CIE76, CIE94, or CIEDE2000.");
        };
    }

    private double deltaE76(LabColorDto lab1, LabColorDto lab2) {
        double dL = lab1.L - lab2.L;
        double da = lab1.a - lab2.a;
        double db = lab1.b - lab2.b;
        return Math.sqrt(dL * dL + da * da + db * db);
    }

    private double deltaE94(LabColorDto lab1, LabColorDto lab2) {
        double dL = lab1.L - lab2.L;
        double C1 = Math.sqrt(lab1.a * lab1.a + lab1.b * lab1.b);
        double C2 = Math.sqrt(lab2.a * lab2.a + lab2.b * lab2.b);
        double dC = C1 - C2;
        double da = lab1.a - lab2.a;
        double db = lab1.b - lab2.b;
        double dH = Math.sqrt(Math.max(0.0, da * da + db * db - dC * dC));
        double kL = 1.0, kC = 1.0, kH = 1.0; // graphic arts
        double sL = 1.0;
        double sC = 1.0 + 0.045 * C1;
        double sH = 1.0 + 0.015 * C1;
        return Math.sqrt(
            (dL / (kL * sL)) * (dL / (kL * sL)) +
            (dC / (kC * sC)) * (dC / (kC * sC)) +
            (dH / (kH * sH)) * (dH / (kH * sH))
        );
    }

    private double deltaE2000(LabColorDto lab1, LabColorDto lab2) {
        double L1 = lab1.L, a1 = lab1.a, b1 = lab1.b;
        double L2 = lab2.L, a2 = lab2.a, b2 = lab2.b;

        double C1 = Math.sqrt(a1 * a1 + b1 * b1);
        double C2 = Math.sqrt(a2 * a2 + b2 * b2);
        double Cbar = (C1 + C2) / 2.0;

        double G = 0.5 * (1.0 - Math.sqrt(Math.pow(Cbar, 7) / (Math.pow(Cbar, 7) + Math.pow(25, 7))));
        double a1p = a1 * (1.0 + G);
        double a2p = a2 * (1.0 + G);

        double C1p = Math.sqrt(a1p * a1p + b1 * b1);
        double C2p = Math.sqrt(a2p * a2p + b2 * b2);
        double Cbarp = (C1p + C2p) / 2.0;

        double h1p = Math.toDegrees(Math.atan2(b1, a1p));
        if (h1p < 0) h1p += 360.0;
        double h2p = Math.toDegrees(Math.atan2(b2, a2p));
        if (h2p < 0) h2p += 360.0;

        double Hbarp;
        double dh;
        if (Math.abs(h1p - h2p) <= 180.0) {
            Hbarp = (h1p + h2p) / 2.0;
            dh = h2p - h1p;
        } else if (h1p + h2p < 360.0) {
            Hbarp = (h1p + h2p + 360.0) / 2.0;
            dh = h2p - h1p + 360.0;
        } else {
            Hbarp = (h1p + h2p - 360.0) / 2.0;
            dh = h2p - h1p - 360.0;
        }

        double dLp = L2 - L1;
        double dCp = C2p - C1p;
        double dHp = 2.0 * Math.sqrt(C1p * C2p) * Math.sin(Math.toRadians(dh / 2.0));

        double T = 1.0 - 0.17 * Math.cos(Math.toRadians(Hbarp - 30.0))
                     + 0.24 * Math.cos(Math.toRadians(2.0 * Hbarp))
                     + 0.32 * Math.cos(Math.toRadians(3.0 * Hbarp + 6.0))
                     - 0.20 * Math.cos(Math.toRadians(4.0 * Hbarp - 63.0));

        double SL = 1.0 + (0.015 * (L1 + L2 - 100.0) * (L1 + L2 - 100.0))
                     / Math.sqrt(20.0 + (L1 + L2 - 100.0) * (L1 + L2 - 100.0));
        double SC = 1.0 + 0.045 * Cbarp;
        double SH = 1.0 + 0.015 * Cbarp * T;

        double RT = -2.0 * Math.sqrt(Math.pow(Cbarp, 7) / (Math.pow(Cbarp, 7) + Math.pow(25, 7)))
                    * Math.sin(Math.toRadians(60.0 * Math.exp(-((Hbarp - 275.0) / 25.0) * ((Hbarp - 275.0) / 25.0))));

        double kL = 1.0, kC = 1.0, kH = 1.0;

        return Math.sqrt(
            (dLp / (kL * SL)) * (dLp / (kL * SL)) +
            (dCp / (kC * SC)) * (dCp / (kC * SC)) +
            (dHp / (kH * SH)) * (dHp / (kH * SH)) +
            RT * (dCp / (kC * SC)) * (dHp / (kH * SH))
        );
    }

    // --- WCAG contrast ratio (must use sRGB path per spec) ---

    public double wcagContrastRatio(RgbColorDto color1, RgbColorDto color2) {
        double l1 = relativeLuminance(color1);
        double l2 = relativeLuminance(color2);
        double lighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);
        return (lighter + 0.05) / (darker + 0.05);
    }

    private double relativeLuminance(RgbColorDto color) {
        double r = srgbToLinearForLuminance(color.r / 255.0);
        double g = srgbToLinearForLuminance(color.g / 255.0);
        double b = srgbToLinearForLuminance(color.b / 255.0);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private double srgbToLinearForLuminance(double c) {
        if (c <= 0.04045) return c / 12.92;
        return Math.pow((c + 0.055) / 1.055, 2.4);
    }

    // --- Mix (sRGB path, kept for backward compat) ---

    public RgbColorDto mix(RgbColorDto from, RgbColorDto to, double ratio) {
        double r = clamp01(ratio);
        return new RgbColorDto(
            clamp((int) Math.round(from.r + r * (to.r - from.r))),
            clamp((int) Math.round(from.g + r * (to.g - from.g))),
            clamp((int) Math.round(from.b + r * (to.b - from.b)))
        );
    }

    // --- Temperature (Kelvin → RGB) ---

    public RgbColorDto temperatureToRgb(double kelvin) {
        double t = kelvin / 100.0;
        double r, g, b;

        if (t <= 66.0) {
            r = 255.0;
            g = 99.4708025861 * Math.log(t) - 161.1195681661;
            if (t <= 19.0) {
                b = 0.0;
            } else {
                b = 138.5177312231 * Math.log(t - 10.0) - 305.0447927307;
            }
        } else {
            r = 329.698727446 * Math.pow(t - 60.0, -0.1332047592);
            g = 288.1221695283 * Math.pow(t - 60.0, -0.0755148492);
            b = 255.0;
        }

        return new RgbColorDto(
            clamp((int) Math.round(r)),
            clamp((int) Math.round(g)),
            clamp((int) Math.round(b))
        );
    }

    // --- helpers ---

    private int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private double clampRange(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double clampDelta(double d) {
        return Math.max(0.0, Math.min(255.0, d));
    }
}
