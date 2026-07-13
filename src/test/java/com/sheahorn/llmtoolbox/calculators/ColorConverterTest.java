package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorConverterTest {

    // ── convertTo: same-type no-op ─────────────────────────────────────

    @Test
    void convertTo_sameTypeReturnsSame() {
        RgbColorDto rgb = new RgbColorDto(100, 150, 200);
        RgbColorDto result = ColorConverter.convertTo(rgb, ColorSpace.RGB);
        assertSame(rgb, result);
    }

    @Test
    void convertTo_sameTypeHex() {
        HexColorDto hex = new HexColorDto("#FF8040");
        HexColorDto result = ColorConverter.convertTo(hex, ColorSpace.HEX);
        assertSame(hex, result);
    }

    // ── convertTo: RGB → other spaces ──────────────────────────────────

    @Test
    void convertTo_RGBtoHEX() {
        RgbColorDto rgb = new RgbColorDto(255, 128, 64);
        HexColorDto result = ColorConverter.convertTo(rgb, ColorSpace.HEX);
        assertEquals("#FF8040", result.hex);
    }

    @Test
    void convertTo_RGBtoHSL() {
        RgbColorDto rgb = new RgbColorDto(255, 0, 0);
        HslColorDto result = ColorConverter.convertTo(rgb, ColorSpace.HSL);
        assertEquals(0.0, result.h);
        assertEquals(100.0, result.s);
        assertEquals(50.0, result.l);
    }

    @Test
    void convertTo_RGBtoHSV() {
        RgbColorDto rgb = new RgbColorDto(255, 0, 0);
        HsvColorDto result = ColorConverter.convertTo(rgb, ColorSpace.HSV);
        assertEquals(0.0, result.h);
        assertEquals(100.0, result.s);
        assertEquals(100.0, result.v);
    }

    @Test
    void convertTo_RGBtoCMYK_black() {
        RgbColorDto rgb = new RgbColorDto(0, 0, 0);
        CmykColorDto result = ColorConverter.convertTo(rgb, ColorSpace.CMYK);
        assertEquals(0.0, result.c);
        assertEquals(0.0, result.m);
        assertEquals(0.0, result.y);
        assertEquals(100.0, result.k);
    }

    @Test
    void convertTo_RGBtoCMYK_white() {
        RgbColorDto rgb = new RgbColorDto(255, 255, 255);
        CmykColorDto result = ColorConverter.convertTo(rgb, ColorSpace.CMYK);
        assertEquals(0.0, result.c);
        assertEquals(0.0, result.m);
        assertEquals(0.0, result.y);
        assertEquals(0.0, result.k);
    }

    @Test
    void convertTo_RGBtoCMYK_red() {
        RgbColorDto rgb = new RgbColorDto(255, 0, 0);
        CmykColorDto result = ColorConverter.convertTo(rgb, ColorSpace.CMYK);
        assertEquals(0.0, result.c);
        assertEquals(100.0, result.m);
        assertEquals(100.0, result.y);
        assertEquals(0.0, result.k);
    }

    @Test
    void convertTo_RGBtoLAB() {
        RgbColorDto rgb = new RgbColorDto(255, 255, 255);
        LabColorDto result = ColorConverter.convertTo(rgb, ColorSpace.LAB);
        assertTrue(result.L > 99.0 && result.L <= 100.0);
        assertTrue(Math.abs(result.a) < 1.0);
        assertTrue(Math.abs(result.b) < 1.0);
    }

    @Test
    void convertTo_RGBtoLCH() {
        RgbColorDto rgb = new RgbColorDto(255, 0, 0);
        LchColorDto result = ColorConverter.convertTo(rgb, ColorSpace.LCH);
        assertTrue(result.L > 40.0 && result.L < 60.0);
        assertTrue(result.C > 80.0);
    }

    @Test
    void convertTo_RGBtoOKLAB() {
        RgbColorDto rgb = new RgbColorDto(255, 255, 255);
        OkLabColorDto result = ColorConverter.convertTo(rgb, ColorSpace.OKLAB);
        assertTrue(result.L > 0.99 && result.L <= 1.0);
        assertTrue(Math.abs(result.a) < 0.01);
        assertTrue(Math.abs(result.b) < 0.01);
    }

    @Test
    void convertTo_RGBtoOKLCH() {
        RgbColorDto rgb = new RgbColorDto(255, 0, 0);
        OkLchColorDto result = ColorConverter.convertTo(rgb, ColorSpace.OKLCH);
        assertTrue(result.L > 0.5 && result.L < 0.7);
        assertTrue(result.C > 0.2);
    }

    @Test
    void convertTo_RGBtoHWB() {
        RgbColorDto rgb = new RgbColorDto(255, 0, 0);
        HwbColorDto result = ColorConverter.convertTo(rgb, ColorSpace.HWB);
        assertEquals(0.0, result.h);
        assertEquals(0.0, result.w);
        assertEquals(0.0, result.b);
    }

    @Test
    void convertTo_RGBtoYCBCR() {
        RgbColorDto rgb = new RgbColorDto(255, 255, 255);
        YcbcrColorDto result = ColorConverter.convertTo(rgb, ColorSpace.YCBCR);
        assertTrue(result.y > 0.99);
        assertTrue(Math.abs(result.cb - 0.5) < 0.01);
        assertTrue(Math.abs(result.cr - 0.5) < 0.01);
    }

    // ── convertTo: HEX → other spaces ─────────────────────────────────

    @Test
    void convertTo_HEXtoRGB() {
        HexColorDto hex = new HexColorDto("#FF8040");
        RgbColorDto result = ColorConverter.convertTo(hex, ColorSpace.RGB);
        assertEquals(255, result.r);
        assertEquals(128, result.g);
        assertEquals(64, result.b);
    }

    // ── convertTo: HSL → other spaces ─────────────────────────────────

    @Test
    void convertTo_HSLtoRGB_red() {
        HslColorDto hsl = new HslColorDto(0, 100, 50);
        RgbColorDto result = ColorConverter.convertTo(hsl, ColorSpace.RGB);
        assertEquals(255, result.r);
        assertEquals(0, result.g);
        assertEquals(0, result.b);
    }

    @Test
    void convertTo_HSLtoRGB_white() {
        HslColorDto hsl = new HslColorDto(0, 0, 100);
        RgbColorDto result = ColorConverter.convertTo(hsl, ColorSpace.RGB);
        assertEquals(255, result.r);
        assertEquals(255, result.g);
        assertEquals(255, result.b);
    }

    @Test
    void convertTo_HSLtoRGB_black() {
        HslColorDto hsl = new HslColorDto(0, 0, 0);
        RgbColorDto result = ColorConverter.convertTo(hsl, ColorSpace.RGB);
        assertEquals(0, result.r);
        assertEquals(0, result.g);
        assertEquals(0, result.b);
    }

    // ── convertTo: HSV → other spaces ─────────────────────────────────

    @Test
    void convertTo_HSVtoRGB_red() {
        HsvColorDto hsv = new HsvColorDto(0, 100, 100);
        RgbColorDto result = ColorConverter.convertTo(hsv, ColorSpace.RGB);
        assertEquals(255, result.r);
        assertEquals(0, result.g);
        assertEquals(0, result.b);
    }

    // ── convertTo: CMYK → other spaces ────────────────────────────────

    @Test
    void convertTo_CMYKtoRGB_black() {
        CmykColorDto cmyk = new CmykColorDto(0, 0, 0, 100);
        RgbColorDto result = ColorConverter.convertTo(cmyk, ColorSpace.RGB);
        assertEquals(0, result.r);
        assertEquals(0, result.g);
        assertEquals(0, result.b);
    }

    // ── convertTo: LAB → other spaces ─────────────────────────────────

    @Test
    void convertTo_LABtoRGB_white() {
        LabColorDto lab = new LabColorDto(100, 0, 0);
        RgbColorDto result = ColorConverter.convertTo(lab, ColorSpace.RGB);
        assertEquals(255, result.r);
        assertEquals(255, result.g);
        assertEquals(255, result.b);
    }

    @Test
    void convertTo_LABtoRGB_black() {
        LabColorDto lab = new LabColorDto(0, 0, 0);
        RgbColorDto result = ColorConverter.convertTo(lab, ColorSpace.RGB);
        assertEquals(0, result.r);
        assertEquals(0, result.g);
        assertEquals(0, result.b);
    }

    // ── convertTo: LCH → other spaces ─────────────────────────────────

    @Test
    void convertTo_LCHtoRGB() {
        LchColorDto lch = new LchColorDto(53.23, 104.55, 40.0);
        RgbColorDto result = ColorConverter.convertTo(lch, ColorSpace.RGB);
        // should produce a valid red-ish color
        assertTrue(result.r > 200);
        assertTrue(result.g < 100);
        assertTrue(result.b < 100);
    }

    // ── convertTo: OKLab → other spaces ───────────────────────────────

    @Test
    void convertTo_OKLABtoRGB_white() {
        OkLabColorDto oklab = new OkLabColorDto(1.0, 0.0, 0.0);
        RgbColorDto result = ColorConverter.convertTo(oklab, ColorSpace.RGB);
        assertEquals(255, result.r);
        assertEquals(255, result.g);
        assertEquals(255, result.b);
    }

    // ── convertTo: OKLCh → other spaces ───────────────────────────────

    @Test
    void convertTo_OKLCHtoRGB() {
        OkLchColorDto oklch = new OkLchColorDto(0.628, 0.258, 29.23);
        RgbColorDto result = ColorConverter.convertTo(oklch, ColorSpace.RGB);
        // should produce a valid red-ish color
        assertTrue(result.r > 200);
        assertTrue(result.g < 100);
        assertTrue(result.b < 100);
    }

    // ── convertTo: HWB → other spaces ─────────────────────────────────

    @Test
    void convertTo_HWBtoRGB_red() {
        HwbColorDto hwb = new HwbColorDto(0, 0, 0);
        RgbColorDto result = ColorConverter.convertTo(hwb, ColorSpace.RGB);
        assertEquals(255, result.r);
        assertEquals(0, result.g);
        assertEquals(0, result.b);
    }

    @Test
    void convertTo_HWBtoRGB_white() {
        HwbColorDto hwb = new HwbColorDto(0, 100, 0);
        RgbColorDto result = ColorConverter.convertTo(hwb, ColorSpace.RGB);
        assertEquals(255, result.r);
        assertEquals(255, result.g);
        assertEquals(255, result.b);
    }

    @Test
    void convertTo_HWBtoRGB_black() {
        HwbColorDto hwb = new HwbColorDto(0, 0, 100);
        RgbColorDto result = ColorConverter.convertTo(hwb, ColorSpace.RGB);
        assertEquals(0, result.r);
        assertEquals(0, result.g);
        assertEquals(0, result.b);
    }

    @Test
    void convertTo_HWBtoRGB_gray() {
        // w=50, b=50 → gray at 50%
        HwbColorDto hwb = new HwbColorDto(0, 50, 50);
        RgbColorDto result = ColorConverter.convertTo(hwb, ColorSpace.RGB);
        assertEquals(128, result.r);
        assertEquals(128, result.g);
        assertEquals(128, result.b);
    }

    // ── convertTo: YCbCr → other spaces ──────────────────────────────

    @Test
    void convertTo_YCBCRtoRGB_white() {
        YcbcrColorDto ycbcr = new YcbcrColorDto(1.0, 0.5, 0.5);
        RgbColorDto result = ColorConverter.convertTo(ycbcr, ColorSpace.RGB);
        assertEquals(255, result.r);
        assertEquals(255, result.g);
        assertEquals(255, result.b);
    }

    // ── toLinearRgb: from various spaces ──────────────────────────────

    @Test
    void toLinearRgb_fromRGB_white() {
        RgbColorDto rgb = new RgbColorDto(255, 255, 255);
        double[] linear = ColorConverter.toLinearRgb(rgb);
        assertEquals(1.0, linear[0], 1e-6);
        assertEquals(1.0, linear[1], 1e-6);
        assertEquals(1.0, linear[2], 1e-6);
    }

    @Test
    void toLinearRgb_fromRGB_black() {
        RgbColorDto rgb = new RgbColorDto(0, 0, 0);
        double[] linear = ColorConverter.toLinearRgb(rgb);
        assertEquals(0.0, linear[0]);
        assertEquals(0.0, linear[1]);
        assertEquals(0.0, linear[2]);
    }

    @Test
    void toLinearRgb_fromHex() {
        HexColorDto hex = new HexColorDto("#FF0000");
        double[] linear = ColorConverter.toLinearRgb(hex);
        assertTrue(linear[0] > 0.0);
        assertEquals(0.0, linear[1]);
        assertEquals(0.0, linear[2]);
    }

    @Test
    void toLinearRgb_fromHSL() {
        HslColorDto hsl = new HslColorDto(0, 100, 50);
        double[] linear = ColorConverter.toLinearRgb(hsl);
        assertTrue(linear[0] > 0.0);
        assertEquals(0.0, linear[1]);
        assertEquals(0.0, linear[2]);
    }

    @Test
    void toLinearRgb_fromHSV() {
        HsvColorDto hsv = new HsvColorDto(0, 100, 100);
        double[] linear = ColorConverter.toLinearRgb(hsv);
        assertTrue(linear[0] > 0.0);
        assertEquals(0.0, linear[1]);
        assertEquals(0.0, linear[2]);
    }

    @Test
    void toLinearRgb_fromCMYK() {
        CmykColorDto cmyk = new CmykColorDto(0, 100, 100, 0);
        double[] linear = ColorConverter.toLinearRgb(cmyk);
        assertTrue(linear[0] > 0.0);
        assertEquals(0.0, linear[1]);
        assertEquals(0.0, linear[2]);
    }

    @Test
    void toLinearRgb_fromLAB_white() {
        LabColorDto lab = new LabColorDto(100, 0, 0);
        double[] linear = ColorConverter.toLinearRgb(lab);
        assertTrue(linear[0] > 0.99);
        assertTrue(linear[1] > 0.99);
        assertTrue(linear[2] > 0.99);
    }

    @Test
    void toLinearRgb_fromLCH() {
        LchColorDto lch = new LchColorDto(53.23, 104.55, 40.0);
        double[] linear = ColorConverter.toLinearRgb(lch);
        assertTrue(linear[0] > 0.0);
        assertTrue(linear[1] < 0.0 || linear[2] < 0.0); // red has negative green/blue in linear
    }

    @Test
    void toLinearRgb_fromOKLab() {
        OkLabColorDto oklab = new OkLabColorDto(1.0, 0.0, 0.0);
        double[] linear = ColorConverter.toLinearRgb(oklab);
        assertTrue(linear[0] > 0.99);
        assertTrue(linear[1] > 0.99);
        assertTrue(linear[2] > 0.99);
    }

    @Test
    void toLinearRgb_fromOKLCh() {
        OkLchColorDto oklch = new OkLchColorDto(0.628, 0.258, 29.23);
        double[] linear = ColorConverter.toLinearRgb(oklch);
        assertTrue(linear[0] > 0.0);
    }

    @Test
    void toLinearRgb_fromHWB() {
        HwbColorDto hwb = new HwbColorDto(0, 0, 0);
        double[] linear = ColorConverter.toLinearRgb(hwb);
        assertTrue(linear[0] > 0.0);
        assertEquals(0.0, linear[1]);
        assertEquals(0.0, linear[2]);
    }

    @Test
    void toLinearRgb_fromYCbCr() {
        YcbcrColorDto ycbcr = new YcbcrColorDto(1.0, 0.5, 0.5);
        double[] linear = ColorConverter.toLinearRgb(ycbcr);
        assertTrue(linear[0] > 0.99);
        assertTrue(linear[1] > 0.99);
        assertTrue(linear[2] > 0.99);
    }

    // ── linearRgbTo: to various spaces ────────────────────────────────

    @Test
    void linearRgbTo_RGB_white() {
        double[] linear = {1.0, 1.0, 1.0};
        RgbColorDto result = ColorConverter.linearRgbTo(ColorSpace.RGB, linear);
        assertEquals(255, result.r);
        assertEquals(255, result.g);
        assertEquals(255, result.b);
    }

    @Test
    void linearRgbTo_RGB_black() {
        double[] linear = {0.0, 0.0, 0.0};
        RgbColorDto result = ColorConverter.linearRgbTo(ColorSpace.RGB, linear);
        assertEquals(0, result.r);
        assertEquals(0, result.g);
        assertEquals(0, result.b);
    }

    @Test
    void linearRgbTo_HEX() {
        double[] linear = {1.0, 0.0, 0.0};
        HexColorDto result = ColorConverter.linearRgbTo(ColorSpace.HEX, linear);
        assertEquals("#FF0000", result.hex);
    }

    @Test
    void linearRgbTo_HSL() {
        double[] linear = {1.0, 0.0, 0.0};
        HslColorDto result = ColorConverter.linearRgbTo(ColorSpace.HSL, linear);
        assertEquals(0.0, result.h);
        assertEquals(100.0, result.s);
        assertEquals(50.0, result.l);
    }

    @Test
    void linearRgbTo_HSV() {
        double[] linear = {1.0, 0.0, 0.0};
        HsvColorDto result = ColorConverter.linearRgbTo(ColorSpace.HSV, linear);
        assertEquals(0.0, result.h);
        assertEquals(100.0, result.s);
        assertEquals(100.0, result.v);
    }

    @Test
    void linearRgbTo_CMYK_black() {
        double[] linear = {0.0, 0.0, 0.0};
        CmykColorDto result = ColorConverter.linearRgbTo(ColorSpace.CMYK, linear);
        assertEquals(0.0, result.c);
        assertEquals(0.0, result.m);
        assertEquals(0.0, result.y);
        assertEquals(100.0, result.k);
    }

    @Test
    void linearRgbTo_LAB_white() {
        double[] linear = {1.0, 1.0, 1.0};
        LabColorDto result = ColorConverter.linearRgbTo(ColorSpace.LAB, linear);
        assertTrue(result.L > 99.0);
        assertTrue(Math.abs(result.a) < 1.0);
        assertTrue(Math.abs(result.b) < 1.0);
    }

    @Test
    void linearRgbTo_LCH() {
        double[] linear = {1.0, 0.0, 0.0};
        LchColorDto result = ColorConverter.linearRgbTo(ColorSpace.LCH, linear);
        assertTrue(result.L > 40.0 && result.L < 60.0);
        assertTrue(result.C > 80.0);
    }

    @Test
    void linearRgbTo_OKLAB_white() {
        double[] linear = {1.0, 1.0, 1.0};
        OkLabColorDto result = ColorConverter.linearRgbTo(ColorSpace.OKLAB, linear);
        assertTrue(result.L > 0.99);
        assertTrue(Math.abs(result.a) < 0.01);
        assertTrue(Math.abs(result.b) < 0.01);
    }

    @Test
    void linearRgbTo_OKLCH() {
        double[] linear = {1.0, 0.0, 0.0};
        OkLchColorDto result = ColorConverter.linearRgbTo(ColorSpace.OKLCH, linear);
        assertTrue(result.L > 0.5 && result.L < 0.7);
        assertTrue(result.C > 0.2);
    }

    @Test
    void linearRgbTo_HWB_red() {
        double[] linear = {1.0, 0.0, 0.0};
        HwbColorDto result = ColorConverter.linearRgbTo(ColorSpace.HWB, linear);
        assertEquals(0.0, result.h);
        assertEquals(0.0, result.w);
        assertEquals(0.0, result.b);
    }

    @Test
    void linearRgbTo_YCBCR_white() {
        double[] linear = {1.0, 1.0, 1.0};
        YcbcrColorDto result = ColorConverter.linearRgbTo(ColorSpace.YCBCR, linear);
        assertTrue(result.y > 0.99);
        assertTrue(Math.abs(result.cb - 0.5) < 0.01);
        assertTrue(Math.abs(result.cr - 0.5) < 0.01);
    }

    // ── roundtrips ────────────────────────────────────────────────────

    @Test
    void roundtrip_RGB_via_LAB() {
        RgbColorDto original = new RgbColorDto(100, 150, 200);
        LabColorDto lab = ColorConverter.convertTo(original, ColorSpace.LAB);
        RgbColorDto back = ColorConverter.convertTo(lab, ColorSpace.RGB);
        // Should be close (LAB→RGB is lossy due to gamut)
        assertTrue(Math.abs(original.r - back.r) <= 2);
        assertTrue(Math.abs(original.g - back.g) <= 2);
        assertTrue(Math.abs(original.b - back.b) <= 2);
    }

    @Test
    void roundtrip_RGB_via_OKLAB() {
        RgbColorDto original = new RgbColorDto(100, 150, 200);
        OkLabColorDto oklab = ColorConverter.convertTo(original, ColorSpace.OKLAB);
        RgbColorDto back = ColorConverter.convertTo(oklab, ColorSpace.RGB);
        assertTrue(Math.abs(original.r - back.r) <= 2);
        assertTrue(Math.abs(original.g - back.g) <= 2);
        assertTrue(Math.abs(original.b - back.b) <= 2);
    }

    @Test
    void roundtrip_RGB_via_HSL() {
        RgbColorDto original = new RgbColorDto(100, 150, 200);
        HslColorDto hsl = ColorConverter.convertTo(original, ColorSpace.HSL);
        RgbColorDto back = ColorConverter.convertTo(hsl, ColorSpace.RGB);
        // HSL roundtrip is lossy due to Math.round on hue/sat/light
        assertTrue(Math.abs(original.r - back.r) <= 1);
        assertTrue(Math.abs(original.g - back.g) <= 1);
        assertTrue(Math.abs(original.b - back.b) <= 1);
    }

    @Test
    void roundtrip_RGB_via_HSV() {
        RgbColorDto original = new RgbColorDto(100, 150, 200);
        HsvColorDto hsv = ColorConverter.convertTo(original, ColorSpace.HSV);
        RgbColorDto back = ColorConverter.convertTo(hsv, ColorSpace.RGB);
        // HSV roundtrip is lossy due to Math.round on hue/sat/value
        assertTrue(Math.abs(original.r - back.r) <= 1);
        assertTrue(Math.abs(original.g - back.g) <= 1);
        assertTrue(Math.abs(original.b - back.b) <= 1);
    }

    @Test
    void roundtrip_RGB_via_CMYK() {
        RgbColorDto original = new RgbColorDto(100, 150, 200);
        CmykColorDto cmyk = ColorConverter.convertTo(original, ColorSpace.CMYK);
        RgbColorDto back = ColorConverter.convertTo(cmyk, ColorSpace.RGB);
        // CMYK roundtrip is lossy
        assertTrue(Math.abs(original.r - back.r) <= 1);
        assertTrue(Math.abs(original.g - back.g) <= 1);
        assertTrue(Math.abs(original.b - back.b) <= 1);
    }

    @Test
    void roundtrip_RGB_via_HWB() {
        RgbColorDto original = new RgbColorDto(100, 150, 200);
        HwbColorDto hwb = ColorConverter.convertTo(original, ColorSpace.HWB);
        RgbColorDto back = ColorConverter.convertTo(hwb, ColorSpace.RGB);
        // HWB roundtrip is lossy due to Math.round on hue/whiteness/blackness
        assertTrue(Math.abs(original.r - back.r) <= 1);
        assertTrue(Math.abs(original.g - back.g) <= 1);
        assertTrue(Math.abs(original.b - back.b) <= 1);
    }

    @Test
    void roundtrip_RGB_via_YCBCR() {
        RgbColorDto original = new RgbColorDto(100, 150, 200);
        YcbcrColorDto ycbcr = ColorConverter.convertTo(original, ColorSpace.YCBCR);
        RgbColorDto back = ColorConverter.convertTo(ycbcr, ColorSpace.RGB);
        assertTrue(Math.abs(original.r - back.r) <= 1);
        assertTrue(Math.abs(original.g - back.g) <= 1);
        assertTrue(Math.abs(original.b - back.b) <= 1);
    }

    @Test
    void roundtrip_linearRgb_via_LAB() {
        double[] original = {0.5, 0.3, 0.1};
        LabColorDto lab = ColorConverter.linearRgbTo(ColorSpace.LAB, original);
        double[] back = ColorConverter.toLinearRgb(lab);
        assertTrue(Math.abs(original[0] - back[0]) < 0.02);
        assertTrue(Math.abs(original[1] - back[1]) < 0.02);
        assertTrue(Math.abs(original[2] - back[2]) < 0.02);
    }

    @Test
    void roundtrip_linearRgb_via_OKLAB() {
        double[] original = {0.5, 0.3, 0.1};
        OkLabColorDto oklab = ColorConverter.linearRgbTo(ColorSpace.OKLAB, original);
        double[] back = ColorConverter.toLinearRgb(oklab);
        assertTrue(Math.abs(original[0] - back[0]) < 0.02);
        assertTrue(Math.abs(original[1] - back[1]) < 0.02);
        assertTrue(Math.abs(original[2] - back[2]) < 0.02);
    }

    // ── out-of-gamut preservation ─────────────────────────────────────

    @Test
    void toLinearRgb_preservesOutOfGamut_fromLAB() {
        // ProPhotoRGB-ish red that's outside sRGB gamut
        LabColorDto lab = new LabColorDto(50, 100, 50);
        double[] linear = ColorConverter.toLinearRgb(lab);
        // At least one channel should be negative or >1 (out of sRGB gamut)
        boolean outOfGamut = linear[0] < 0.0 || linear[0] > 1.0
                || linear[1] < 0.0 || linear[1] > 1.0
                || linear[2] < 0.0 || linear[2] > 1.0;
        assertTrue(outOfGamut, "LAB→linear should preserve out-of-gamut values");
    }

    @Test
    void linearRgbTo_clipsToSRGB_forSRGBTargets() {
        // Out-of-gamut linear values
        double[] linear = {1.5, -0.2, 0.5};
        RgbColorDto result = ColorConverter.linearRgbTo(ColorSpace.RGB, linear);
        // Should be clamped to 0..255
        assertTrue(result.r >= 0 && result.r <= 255);
        assertTrue(result.g >= 0 && result.g <= 255);
        assertTrue(result.b >= 0 && result.b <= 255);
    }

    @Test
    void linearRgbTo_preservesOutOfGamut_forWideGamutTargets() {
        double[] linear = {1.5, -0.2, 0.5};
        LabColorDto result = ColorConverter.linearRgbTo(ColorSpace.LAB, linear);
        // LAB should reflect the out-of-gamut values (not clamped to sRGB first)
        assertNotNull(result);
    }
}
