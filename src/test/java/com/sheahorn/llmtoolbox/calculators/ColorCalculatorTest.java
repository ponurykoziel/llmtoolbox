package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorCalculatorTest {

    private ColorCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new ColorCalculator();
    }

    // ── invert ──────────────────────────────────────────────────────────

    @Test
    void invert_black() {
        RgbColorDto result = calc.invert(new RgbColorDto(0, 0, 0));
        assertEquals(255, result.r);
        assertEquals(255, result.g);
        assertEquals(255, result.b);
    }

    @Test
    void invert_white() {
        RgbColorDto result = calc.invert(new RgbColorDto(255, 255, 255));
        assertEquals(0, result.r);
        assertEquals(0, result.g);
        assertEquals(0, result.b);
    }

    @Test
    void invert_midGray() {
        RgbColorDto result = calc.invert(new RgbColorDto(128, 128, 128));
        assertEquals(127, result.r);
        assertEquals(127, result.g);
        assertEquals(127, result.b);
    }

    @Test
    void invert_color() {
        RgbColorDto result = calc.invert(new RgbColorDto(100, 150, 200));
        assertEquals(155, result.r);
        assertEquals(105, result.g);
        assertEquals(55, result.b);
    }

    @Test
    void invert_doubleInvert() {
        RgbColorDto original = new RgbColorDto(50, 100, 150);
        RgbColorDto inverted = calc.invert(original);
        RgbColorDto back = calc.invert(inverted);
        assertEquals(original.r, back.r);
        assertEquals(original.g, back.g);
        assertEquals(original.b, back.b);
    }

    // ── adjustContrast ──────────────────────────────────────────────────

    @Test
    void adjustContrast_positive() {
        RgbColorDto result = calc.adjustContrast(new RgbColorDto(100, 100, 100), 0.5);
        // f=1.5, (100-128)*1.5+128 = -28*1.5+128 = -42+128 = 86
        assertEquals(86, result.r);
        assertEquals(86, result.g);
        assertEquals(86, result.b);
    }

    @Test
    void adjustContrast_negative() {
        RgbColorDto result = calc.adjustContrast(new RgbColorDto(200, 200, 200), -0.5);
        // f=0.5, (200-128)*0.5+128 = 72*0.5+128 = 36+128 = 164
        assertEquals(164, result.r);
        assertEquals(164, result.g);
        assertEquals(164, result.b);
    }

    @Test
    void adjustContrast_zero() {
        RgbColorDto result = calc.adjustContrast(new RgbColorDto(100, 150, 200), 0.0);
        assertEquals(100, result.r);
        assertEquals(150, result.g);
        assertEquals(200, result.b);
    }

    @Test
    void adjustContrast_clamps() {
        // extreme contrast increase should clamp to 0..255
        RgbColorDto result = calc.adjustContrast(new RgbColorDto(0, 128, 255), 10.0);
        assertEquals(0, result.r);
        assertEquals(128, result.g); // 128 stays 128
        assertEquals(255, result.b);
    }

    // ── levelStretch ────────────────────────────────────────────────────

    @Test
    void levelStretch_noChange() {
        RgbColorDto result = calc.levelStretch(new RgbColorDto(100, 150, 200), 0, 0, 1.0);
        assertEquals(100, result.r);
        assertEquals(150, result.g);
        assertEquals(200, result.b);
    }

    @Test
    void levelStretch_blackPoint() {
        // blackDelta=50: low=50, high=255, range=205
        // v=50 → t=0 → 0; v=100 → t=50/205≈0.2439 → 62
        RgbColorDto result = calc.levelStretch(new RgbColorDto(50, 100, 200), 50, 0, 1.0);
        assertEquals(0, result.r);
        assertTrue(result.g > 60 && result.g < 65);
        assertTrue(result.b > 185 && result.b < 195);
    }

    @Test
    void levelStretch_whitePoint() {
        // whiteDelta=55: low=0, high=200, range=200
        // v=200 → t=1 → 255; v=100 → t=0.5 → 128
        RgbColorDto result = calc.levelStretch(new RgbColorDto(100, 200, 50), 0, 55, 1.0);
        assertEquals(128, result.r);
        assertEquals(255, result.g);
        assertEquals(64, result.b);
    }

    @Test
    void levelStretch_gamma() {
        // gamma=2.0 brightens midtones
        RgbColorDto result = calc.levelStretch(new RgbColorDto(128, 128, 128), 0, 0, 2.0);
        // t=128/255≈0.502, t^2≈0.252, *255≈64
        assertEquals(64, result.r);
    }

    @Test
    void levelStretch_fullRange() {
        // blackDelta=255, whiteDelta=255 → low=255, high=0, range=1 (clamped)
        RgbColorDto result = calc.levelStretch(new RgbColorDto(128, 128, 128), 255, 255, 1.0);
        // t=(128-255)/1 = -127, clamped to 0 → 0
        assertEquals(0, result.r);
    }

    // ── levelSqueeze ────────────────────────────────────────────────────

    @Test
    void levelSqueeze_noChange() {
        RgbColorDto result = calc.levelSqueeze(new RgbColorDto(100, 150, 200), 0, 0, 1.0);
        assertEquals(100, result.r);
        assertEquals(150, result.g);
        assertEquals(200, result.b);
    }

    @Test
    void levelSqueeze_blackPoint() {
        // blackDelta=50: low=50, high=255, range=205
        // v=0 → t=0 → 50; v=128 → t≈0.502 → 50+0.502*205≈153
        RgbColorDto result = calc.levelSqueeze(new RgbColorDto(0, 128, 255), 50, 0, 1.0);
        assertEquals(50, result.r);
        assertTrue(result.g > 150 && result.g < 155);
        assertEquals(255, result.b);
    }

    @Test
    void levelSqueeze_whitePoint() {
        // whiteDelta=55: low=0, high=200, range=200
        // v=255 → t=1 → 200; v=128 → t≈0.502 → 100
        RgbColorDto result = calc.levelSqueeze(new RgbColorDto(128, 255, 0), 0, 55, 1.0);
        assertEquals(100, result.r);
        assertEquals(200, result.g);
        assertEquals(0, result.b);
    }

    // ── adjustRed / adjustGreen / adjustBlue ────────────────────────────

    @Test
    void adjustRed_positive() {
        RgbColorDto result = calc.adjustRed(new RgbColorDto(100, 50, 50), 50);
        assertEquals(150, result.r);
        assertEquals(50, result.g);
        assertEquals(50, result.b);
    }

    @Test
    void adjustRed_negative() {
        RgbColorDto result = calc.adjustRed(new RgbColorDto(100, 50, 50), -30);
        assertEquals(70, result.r);
    }

    @Test
    void adjustRed_clamp() {
        RgbColorDto result = calc.adjustRed(new RgbColorDto(200, 50, 50), 100);
        assertEquals(255, result.r);
    }

    @Test
    void adjustGreen_positive() {
        RgbColorDto result = calc.adjustGreen(new RgbColorDto(50, 100, 50), 50);
        assertEquals(150, result.g);
    }

    @Test
    void adjustBlue_positive() {
        RgbColorDto result = calc.adjustBlue(new RgbColorDto(50, 50, 100), 50);
        assertEquals(150, result.b);
    }

    // ── adjustBrightness ────────────────────────────────────────────────

    @Test
    void adjustBrightness_positive() {
        RgbColorDto result = calc.adjustBrightness(new RgbColorDto(100, 100, 100), 50);
        assertEquals(150, result.r);
        assertEquals(150, result.g);
        assertEquals(150, result.b);
    }

    @Test
    void adjustBrightness_negative() {
        RgbColorDto result = calc.adjustBrightness(new RgbColorDto(100, 100, 100), -30);
        assertEquals(70, result.r);
        assertEquals(70, result.g);
        assertEquals(70, result.b);
    }

    @Test
    void adjustBrightness_clampLow() {
        RgbColorDto result = calc.adjustBrightness(new RgbColorDto(10, 10, 10), -20);
        assertEquals(0, result.r);
        assertEquals(0, result.g);
        assertEquals(0, result.b);
    }

    @Test
    void adjustBrightness_clampHigh() {
        RgbColorDto result = calc.adjustBrightness(new RgbColorDto(200, 200, 200), 100);
        assertEquals(255, result.r);
        assertEquals(255, result.g);
        assertEquals(255, result.b);
    }

    // ── adjustLuminance ─────────────────────────────────────────────────

    @Test
    void adjustLuminance_positive() {
        RgbColorDto result = calc.adjustLuminance(new RgbColorDto(100, 100, 100), 0.5);
        // factor=1.5, 100*1.5=150
        assertEquals(150, result.r);
        assertEquals(150, result.g);
        assertEquals(150, result.b);
    }

    @Test
    void adjustLuminance_negative() {
        RgbColorDto result = calc.adjustLuminance(new RgbColorDto(100, 100, 100), -0.5);
        assertEquals(50, result.r);
        assertEquals(50, result.g);
        assertEquals(50, result.b);
    }

    @Test
    void adjustLuminance_zero() {
        RgbColorDto result = calc.adjustLuminance(new RgbColorDto(100, 150, 200), 0.0);
        assertEquals(100, result.r);
        assertEquals(150, result.g);
        assertEquals(200, result.b);
    }

    @Test
    void adjustLuminance_clamp() {
        RgbColorDto result = calc.adjustLuminance(new RgbColorDto(200, 200, 200), 1.0);
        assertEquals(255, result.r);
    }

    // ── adjustLuminanceSaturation ───────────────────────────────────────

    @Test
    void adjustLuminanceSaturation_desaturate() {
        // amount=1.0 fully desaturates to gray: r + 1*(gray - r) = gray
        RgbColorDto result = calc.adjustLuminanceSaturation(new RgbColorDto(200, 100, 50), 1.0);
        // gray = 0.299*200 + 0.587*100 + 0.114*50 ≈ 59.8+58.7+5.7 ≈ 124
        assertTrue(result.r == result.g && result.g == result.b,
                "all channels should be equal after full desaturation (amount=1.0)");
    }

    @Test
    void adjustLuminanceSaturation_zero() {
        RgbColorDto result = calc.adjustLuminanceSaturation(new RgbColorDto(200, 100, 50), 0.0);
        assertEquals(200, result.r);
        assertEquals(100, result.g);
        assertEquals(50, result.b);
    }

    @Test
    void adjustLuminanceSaturation_boost() {
        // amount=0.5 partially desaturates toward gray
        RgbColorDto result = calc.adjustLuminanceSaturation(new RgbColorDto(200, 100, 50), 0.5);
        assertTrue(result.r < 200 && result.r > 150);
    }

    // ── grayscale ───────────────────────────────────────────────────────

    @Test
    void grayscale_color() {
        RgbColorDto result = calc.grayscale(new RgbColorDto(200, 100, 50));
        // gray ≈ 124
        assertEquals(result.r, result.g);
        assertEquals(result.g, result.b);
        assertTrue(result.r > 120 && result.r < 130);
    }

    @Test
    void grayscale_alreadyGray() {
        RgbColorDto result = calc.grayscale(new RgbColorDto(128, 128, 128));
        assertEquals(128, result.r);
        assertEquals(128, result.g);
        assertEquals(128, result.b);
    }

    @Test
    void grayscale_black() {
        RgbColorDto result = calc.grayscale(new RgbColorDto(0, 0, 0));
        assertEquals(0, result.r);
    }

    @Test
    void grayscale_white() {
        RgbColorDto result = calc.grayscale(new RgbColorDto(255, 255, 255));
        assertEquals(255, result.r);
    }

    // ── HSL adjustHue ───────────────────────────────────────────────────

    @Test
    void hsl_adjustHue_positive() {
        HslColorDto result = calc.adjustHue(new HslColorDto(100, 50, 50), 50);
        assertEquals(150.0, result.h);
        assertEquals(50.0, result.s);
        assertEquals(50.0, result.l);
    }

    @Test
    void hsl_adjustHue_wrapAround() {
        HslColorDto result = calc.adjustHue(new HslColorDto(350, 50, 50), 20);
        assertEquals(10.0, result.h);
    }

    @Test
    void hsl_adjustHue_negative() {
        HslColorDto result = calc.adjustHue(new HslColorDto(10, 50, 50), -20);
        assertEquals(350.0, result.h);
    }

    @Test
    void hsl_adjustHue_zero() {
        HslColorDto result = calc.adjustHue(new HslColorDto(180, 50, 50), 0);
        assertEquals(180.0, result.h);
    }

    // ── HSL adjustSaturation ────────────────────────────────────────────

    @Test
    void hsl_adjustSaturation_positive() {
        HslColorDto result = calc.adjustSaturation(new HslColorDto(0, 50, 50), 0.5);
        assertEquals(75.0, result.s);
    }

    @Test
    void hsl_adjustSaturation_negative() {
        HslColorDto result = calc.adjustSaturation(new HslColorDto(0, 50, 50), -0.5);
        assertEquals(25.0, result.s);
    }

    @Test
    void hsl_adjustSaturation_clampHigh() {
        HslColorDto result = calc.adjustSaturation(new HslColorDto(0, 80, 50), 1.0);
        assertEquals(100.0, result.s);
    }

    @Test
    void hsl_adjustSaturation_clampLow() {
        HslColorDto result = calc.adjustSaturation(new HslColorDto(0, 10, 50), -1.0);
        assertEquals(0.0, result.s);
    }

    // ── HSL adjustLightness ─────────────────────────────────────────────

    @Test
    void hsl_adjustLightness_positive() {
        HslColorDto result = calc.adjustLightness(new HslColorDto(0, 50, 50), 0.5);
        assertEquals(75.0, result.l);
    }

    @Test
    void hsl_adjustLightness_negative() {
        HslColorDto result = calc.adjustLightness(new HslColorDto(0, 50, 50), -0.5);
        assertEquals(25.0, result.l);
    }

    @Test
    void hsl_adjustLightness_clamp() {
        HslColorDto result = calc.adjustLightness(new HslColorDto(0, 50, 90), 1.0);
        assertEquals(100.0, result.l);
    }

    // ── HSV adjustHue ───────────────────────────────────────────────────

    @Test
    void hsv_adjustHue_positive() {
        HsvColorDto result = calc.adjustHue(new HsvColorDto(100, 50, 50), 50);
        assertEquals(150.0, result.h);
    }

    @Test
    void hsv_adjustHue_wrapAround() {
        HsvColorDto result = calc.adjustHue(new HsvColorDto(350, 50, 50), 20);
        assertEquals(10.0, result.h);
    }

    // ── HSV adjustSaturation ────────────────────────────────────────────

    @Test
    void hsv_adjustSaturation_positive() {
        HsvColorDto result = calc.adjustSaturation(new HsvColorDto(0, 50, 50), 0.5);
        assertEquals(75.0, result.s);
    }

    @Test
    void hsv_adjustSaturation_clamp() {
        HsvColorDto result = calc.adjustSaturation(new HsvColorDto(0, 90, 50), 1.0);
        assertEquals(100.0, result.s);
    }

    // ── HSV adjustValue ─────────────────────────────────────────────────

    @Test
    void hsv_adjustValue_positive() {
        HsvColorDto result = calc.adjustValue(new HsvColorDto(0, 50, 50), 0.5);
        assertEquals(75.0, result.v);
    }

    @Test
    void hsv_adjustValue_clamp() {
        HsvColorDto result = calc.adjustValue(new HsvColorDto(0, 50, 90), 1.0);
        assertEquals(100.0, result.v);
    }

    // ── LAB adjustLightness ─────────────────────────────────────────────

    @Test
    void lab_adjustLightness_positive() {
        LabColorDto result = calc.adjustLightness(new LabColorDto(50, 10, -10), 0.5);
        assertEquals(75.0, result.L);
        assertEquals(10.0, result.a);
        assertEquals(-10.0, result.b);
    }

    @Test
    void lab_adjustLightness_clamp() {
        LabColorDto result = calc.adjustLightness(new LabColorDto(90, 0, 0), 1.0);
        assertEquals(100.0, result.L);
    }

    // ── LAB adjustA / adjustB ───────────────────────────────────────────

    @Test
    void lab_adjustA_positive() {
        LabColorDto result = calc.adjustA(new LabColorDto(50, 10, 0), 5);
        assertEquals(15.0, result.a);
    }

    @Test
    void lab_adjustA_negative() {
        LabColorDto result = calc.adjustA(new LabColorDto(50, 10, 0), -20);
        assertEquals(-10.0, result.a);
    }

    @Test
    void lab_adjustB_positive() {
        LabColorDto result = calc.adjustB(new LabColorDto(50, 0, 10), 5);
        assertEquals(15.0, result.b);
    }

    // ── LCH adjustLightness ─────────────────────────────────────────────

    @Test
    void lch_adjustLightness_positive() {
        LchColorDto result = calc.adjustLightness(new LchColorDto(50, 30, 180), 0.5);
        assertEquals(75.0, result.L);
        assertEquals(30.0, result.C);
        assertEquals(180.0, result.h);
    }

    // ── LCH adjustChroma ────────────────────────────────────────────────

    @Test
    void lch_adjustChroma_positive() {
        LchColorDto result = calc.adjustChroma(new LchColorDto(50, 30, 180), 0.5);
        assertEquals(45.0, result.C);
    }

    @Test
    void lch_adjustChroma_negative() {
        LchColorDto result = calc.adjustChroma(new LchColorDto(50, 30, 180), -1.0);
        assertEquals(0.0, result.C); // clamped to 0
    }

    // ── LCH adjustHue ───────────────────────────────────────────────────

    @Test
    void lch_adjustHue_positive() {
        LchColorDto result = calc.adjustHue(new LchColorDto(50, 30, 100), 50);
        assertEquals(150.0, result.h);
    }

    @Test
    void lch_adjustHue_wrapAround() {
        LchColorDto result = calc.adjustHue(new LchColorDto(50, 30, 350), 20);
        assertEquals(10.0, result.h);
    }

    // ── OKLab adjustLightness ───────────────────────────────────────────

    @Test
    void oklab_adjustLightness_positive() {
        OkLabColorDto result = calc.adjustLightness(new OkLabColorDto(0.5, 0.1, -0.1), 0.5);
        assertEquals(0.75, result.L);
        assertEquals(0.1, result.a);
        assertEquals(-0.1, result.b);
    }

    @Test
    void oklab_adjustLightness_clamp() {
        OkLabColorDto result = calc.adjustLightness(new OkLabColorDto(0.9, 0, 0), 1.0);
        assertEquals(1.0, result.L);
    }

    // ── OKLab adjustA / adjustB ─────────────────────────────────────────

    @Test
    void oklab_adjustA_positive() {
        OkLabColorDto result = calc.adjustA(new OkLabColorDto(0.5, 0.1, 0.0), 0.05);
        assertEquals(0.15, result.a, 1e-12);
    }

    @Test
    void oklab_adjustB_positive() {
        OkLabColorDto result = calc.adjustB(new OkLabColorDto(0.5, 0.0, 0.1), 0.05);
        assertEquals(0.15, result.b, 1e-12);
    }

    // ── OKLCh adjustLightness ───────────────────────────────────────────

    @Test
    void oklch_adjustLightness_positive() {
        OkLchColorDto result = calc.adjustLightness(new OkLchColorDto(0.5, 0.2, 180), 0.5);
        assertEquals(0.75, result.L);
        assertEquals(0.2, result.C);
        assertEquals(180.0, result.h);
    }

    // ── OKLCh adjustChroma ──────────────────────────────────────────────

    @Test
    void oklch_adjustChroma_positive() {
        OkLchColorDto result = calc.adjustChroma(new OkLchColorDto(0.5, 0.2, 180), 0.5);
        assertEquals(0.3, result.C, 1e-12);
    }

    @Test
    void oklch_adjustChroma_negative() {
        OkLchColorDto result = calc.adjustChroma(new OkLchColorDto(0.5, 0.2, 180), -1.0);
        assertEquals(0.0, result.C);
    }

    // ── OKLCh adjustHue ─────────────────────────────────────────────────

    @Test
    void oklch_adjustHue_positive() {
        OkLchColorDto result = calc.adjustHue(new OkLchColorDto(0.5, 0.2, 100), 50);
        assertEquals(150.0, result.h);
    }

    @Test
    void oklch_adjustHue_wrapAround() {
        OkLchColorDto result = calc.adjustHue(new OkLchColorDto(0.5, 0.2, 350), 20);
        assertEquals(10.0, result.h);
    }

    // ── HWB adjustHue ───────────────────────────────────────────────────

    @Test
    void hwb_adjustHue_positive() {
        HwbColorDto result = calc.adjustHue(new HwbColorDto(100, 20, 10), 50);
        assertEquals(150.0, result.h);
        assertEquals(20.0, result.w);
        assertEquals(10.0, result.b);
    }

    @Test
    void hwb_adjustHue_wrapAround() {
        HwbColorDto result = calc.adjustHue(new HwbColorDto(350, 20, 10), 20);
        assertEquals(10.0, result.h);
    }

    // ── HWB adjustWhiteness ─────────────────────────────────────────────

    @Test
    void hwb_adjustWhiteness_positive() {
        HwbColorDto result = calc.adjustWhiteness(new HwbColorDto(0, 20, 10), 0.5);
        assertEquals(30.0, result.w);
    }

    @Test
    void hwb_adjustWhiteness_clamp() {
        HwbColorDto result = calc.adjustWhiteness(new HwbColorDto(0, 90, 10), 1.0);
        assertEquals(100.0, result.w);
    }

    // ── HWB adjustBlackness ─────────────────────────────────────────────

    @Test
    void hwb_adjustBlackness_positive() {
        HwbColorDto result = calc.adjustBlackness(new HwbColorDto(0, 20, 10), 0.5);
        assertEquals(15.0, result.b);
    }

    @Test
    void hwb_adjustBlackness_clamp() {
        HwbColorDto result = calc.adjustBlackness(new HwbColorDto(0, 20, 90), 1.0);
        assertEquals(100.0, result.b);
    }

    // ── YCbCr adjustLuma ────────────────────────────────────────────────

    @Test
    void ycbcr_adjustLuma_positive() {
        YcbcrColorDto result = calc.adjustLuma(new YcbcrColorDto(0.5, 0.5, 0.5), 0.5);
        assertEquals(0.75, result.y);
        assertEquals(0.5, result.cb);
        assertEquals(0.5, result.cr);
    }

    @Test
    void ycbcr_adjustLuma_clamp() {
        YcbcrColorDto result = calc.adjustLuma(new YcbcrColorDto(0.9, 0.5, 0.5), 1.0);
        assertEquals(1.0, result.y);
    }

    // ── YCbCr adjustBlueChroma ──────────────────────────────────────────

    @Test
    void ycbcr_adjustBlueChroma_positive() {
        YcbcrColorDto result = calc.adjustBlueChroma(new YcbcrColorDto(0.5, 0.4, 0.5), 0.2);
        assertEquals(0.6, result.cb, 1e-12);
    }

    @Test
    void ycbcr_adjustBlueChroma_clamp() {
        YcbcrColorDto result = calc.adjustBlueChroma(new YcbcrColorDto(0.5, 0.9, 0.5), 0.5);
        assertEquals(1.0, result.cb);
    }

    // ── YCbCr adjustRedChroma ───────────────────────────────────────────

    @Test
    void ycbcr_adjustRedChroma_positive() {
        YcbcrColorDto result = calc.adjustRedChroma(new YcbcrColorDto(0.5, 0.5, 0.4), 0.2);
        assertEquals(0.6, result.cr, 1e-12);
    }

    @Test
    void ycbcr_adjustRedChroma_clamp() {
        YcbcrColorDto result = calc.adjustRedChroma(new YcbcrColorDto(0.5, 0.5, 0.9), 0.5);
        assertEquals(1.0, result.cr);
    }

    // ── deltaE ──────────────────────────────────────────────────────────

    @Test
    void deltaE_CIE76_identical() {
        LabColorDto lab = new LabColorDto(50, 10, -10);
        assertEquals(0.0, calc.deltaE(lab, lab, "CIE76"));
    }

    @Test
    void deltaE_CIE76_different() {
        LabColorDto lab1 = new LabColorDto(50, 10, -10);
        LabColorDto lab2 = new LabColorDto(60, 15, -5);
        double result = calc.deltaE(lab1, lab2, "CIE76");
        assertTrue(result > 0.0);
    }

    @Test
    void deltaE_CIE94_identical() {
        LabColorDto lab = new LabColorDto(50, 10, -10);
        assertEquals(0.0, calc.deltaE(lab, lab, "CIE94"));
    }

    @Test
    void deltaE_CIE94_different() {
        LabColorDto lab1 = new LabColorDto(50, 10, -10);
        LabColorDto lab2 = new LabColorDto(60, 15, -5);
        double result = calc.deltaE(lab1, lab2, "CIE94");
        assertTrue(result > 0.0);
    }

    @Test
    void deltaE_CIEDE2000_identical() {
        LabColorDto lab = new LabColorDto(50, 10, -10);
        assertEquals(0.0, calc.deltaE(lab, lab, "CIEDE2000"), 1e-12);
    }

    @Test
    void deltaE_CIEDE2000_different() {
        LabColorDto lab1 = new LabColorDto(50, 10, -10);
        LabColorDto lab2 = new LabColorDto(60, 15, -5);
        double result = calc.deltaE(lab1, lab2, "CIEDE2000");
        assertTrue(result > 0.0);
    }

    @Test
    void deltaE_invalidFormula_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.deltaE(new LabColorDto(50, 0, 0), new LabColorDto(50, 0, 0), "CIE99"));
    }

    @Test
    void deltaE_caseInsensitive() {
        LabColorDto lab = new LabColorDto(50, 10, -10);
        assertEquals(0.0, calc.deltaE(lab, lab, "cie76"));
    }

    // ── wcagContrastRatio ───────────────────────────────────────────────

    @Test
    void wcagContrastRatio_blackWhite() {
        double ratio = calc.wcagContrastRatio(new RgbColorDto(0, 0, 0), new RgbColorDto(255, 255, 255));
        assertTrue(ratio > 20.0 && ratio < 22.0); // ~21:1
    }

    @Test
    void wcagContrastRatio_sameColor() {
        double ratio = calc.wcagContrastRatio(new RgbColorDto(128, 128, 128), new RgbColorDto(128, 128, 128));
        assertEquals(1.0, ratio, 1e-8);
    }

    @Test
    void wcagContrastRatio_symmetric() {
        double r1 = calc.wcagContrastRatio(new RgbColorDto(0, 0, 0), new RgbColorDto(255, 255, 255));
        double r2 = calc.wcagContrastRatio(new RgbColorDto(255, 255, 255), new RgbColorDto(0, 0, 0));
        assertEquals(r1, r2, 1e-8);
    }

    @Test
    void wcagContrastRatio_grayVsGray() {
        double ratio = calc.wcagContrastRatio(new RgbColorDto(100, 100, 100), new RgbColorDto(200, 200, 200));
        assertTrue(ratio > 1.0 && ratio < 10.0);
    }

    // ── mix ─────────────────────────────────────────────────────────────

    @Test
    void mix_ratio0() {
        RgbColorDto result = calc.mix(new RgbColorDto(100, 100, 100), new RgbColorDto(200, 200, 200), 0.0);
        assertEquals(100, result.r);
        assertEquals(100, result.g);
        assertEquals(100, result.b);
    }

    @Test
    void mix_ratio1() {
        RgbColorDto result = calc.mix(new RgbColorDto(100, 100, 100), new RgbColorDto(200, 200, 200), 1.0);
        assertEquals(200, result.r);
        assertEquals(200, result.g);
        assertEquals(200, result.b);
    }

    @Test
    void mix_ratioHalf() {
        RgbColorDto result = calc.mix(new RgbColorDto(100, 100, 100), new RgbColorDto(200, 200, 200), 0.5);
        assertEquals(150, result.r);
        assertEquals(150, result.g);
        assertEquals(150, result.b);
    }

    @Test
    void mix_ratioOutOfRange() {
        // ratio 1.5 should be clamped to 1.0
        RgbColorDto result = calc.mix(new RgbColorDto(100, 100, 100), new RgbColorDto(200, 200, 200), 1.5);
        assertEquals(200, result.r);
    }

    @Test
    void mix_negativeRatio() {
        // ratio -0.5 should be clamped to 0.0
        RgbColorDto result = calc.mix(new RgbColorDto(100, 100, 100), new RgbColorDto(200, 200, 200), -0.5);
        assertEquals(100, result.r);
    }

    @Test
    void mix_differentChannels() {
        RgbColorDto result = calc.mix(new RgbColorDto(0, 100, 200), new RgbColorDto(200, 0, 100), 0.5);
        assertEquals(100, result.r);
        assertEquals(50, result.g);
        assertEquals(150, result.b);
    }

    // ── temperatureToRgb ────────────────────────────────────────────────

    @Test
    void temperatureToRgb_3000K() {
        // warm white / tungsten
        RgbColorDto result = calc.temperatureToRgb(3000);
        assertTrue(result.r >= 255); // red should be maxed
        assertTrue(result.g > 100 && result.g < 200);
        assertTrue(result.b < result.g);
    }

    @Test
    void temperatureToRgb_6500K() {
        // daylight / white
        RgbColorDto result = calc.temperatureToRgb(6500);
        assertTrue(result.r >= 255);
        assertTrue(result.g >= 240);
        assertTrue(result.b >= 240);
    }

    @Test
    void temperatureToRgb_1000K() {
        // very warm, low bound: t=10, g ≈ 99.47*ln(10)-161.12 ≈ 68
        RgbColorDto result = calc.temperatureToRgb(1000);
        assertEquals(255, result.r);
        assertTrue(result.g > 60 && result.g < 80);
        assertEquals(0, result.b);
    }

    @Test
    void temperatureToRgb_40000K() {
        // very cool, high bound
        RgbColorDto result = calc.temperatureToRgb(40000);
        assertTrue(result.r < 200);
        assertTrue(result.g < 200);
        assertEquals(255, result.b);
    }

    @Test
    void temperatureToRgb_6600K_boundary() {
        // t=66 is the boundary between the two formulas
        RgbColorDto result = calc.temperatureToRgb(6600);
        // should produce valid RGB
        assertTrue(result.r >= 0 && result.r <= 255);
        assertTrue(result.g >= 0 && result.g <= 255);
        assertTrue(result.b >= 0 && result.b <= 255);
    }

    @Test
    void temperatureToRgb_allClamped() {
        RgbColorDto result = calc.temperatureToRgb(5000);
        assertTrue(result.r >= 0 && result.r <= 255);
        assertTrue(result.g >= 0 && result.g <= 255);
        assertTrue(result.b >= 0 && result.b <= 255);
    }

    // ═══════════════════════════════════════════════════════════════════
    // linear-RGB operations (lossless, no gamma clamp)
    // ═══════════════════════════════════════════════════════════════════

    // ── invertLinear ───────────────────────────────────────────────────

    @Test
    void invertLinear_black() {
        double[] result = calc.invertLinear(new double[]{0.0, 0.0, 0.0});
        assertEquals(1.0, result[0]);
        assertEquals(1.0, result[1]);
        assertEquals(1.0, result[2]);
    }

    @Test
    void invertLinear_white() {
        double[] result = calc.invertLinear(new double[]{1.0, 1.0, 1.0});
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
        assertEquals(0.0, result[2]);
    }

    @Test
    void invertLinear_midGray() {
        double[] result = calc.invertLinear(new double[]{0.5, 0.5, 0.5});
        assertEquals(0.5, result[0]);
        assertEquals(0.5, result[1]);
        assertEquals(0.5, result[2]);
    }

    @Test
    void invertLinear_doubleInvert() {
        double[] original = {0.2, 0.5, 0.8};
        double[] inverted = calc.invertLinear(original);
        double[] back = calc.invertLinear(inverted);
        assertEquals(original[0], back[0], 1e-12);
        assertEquals(original[1], back[1], 1e-12);
        assertEquals(original[2], back[2], 1e-12);
    }

    // ── adjustContrastLinear ───────────────────────────────────────────

    @Test
    void adjustContrastLinear_positive() {
        double[] result = calc.adjustContrastLinear(new double[]{0.4, 0.4, 0.4}, 0.5);
        // f=1.5, (0.4-0.5)*1.5+0.5 = -0.1*1.5+0.5 = 0.35
        assertEquals(0.35, result[0], 1e-12);
        assertEquals(0.35, result[1], 1e-12);
        assertEquals(0.35, result[2], 1e-12);
    }

    @Test
    void adjustContrastLinear_negative() {
        double[] result = calc.adjustContrastLinear(new double[]{0.8, 0.8, 0.8}, -0.5);
        // f=0.5, (0.8-0.5)*0.5+0.5 = 0.3*0.5+0.5 = 0.65
        assertEquals(0.65, result[0], 1e-12);
        assertEquals(0.65, result[1], 1e-12);
        assertEquals(0.65, result[2], 1e-12);
    }

    @Test
    void adjustContrastLinear_zero() {
        double[] result = calc.adjustContrastLinear(new double[]{0.2, 0.5, 0.8}, 0.0);
        assertEquals(0.2, result[0], 1e-12);
        assertEquals(0.5, result[1], 1e-12);
        assertEquals(0.8, result[2], 1e-12);
    }

    @Test
    void adjustContrastLinear_clamps() {
        double[] result = calc.adjustContrastLinear(new double[]{0.0, 0.5, 1.0}, 10.0);
        assertEquals(0.0, result[0]);
        assertEquals(0.5, result[1]);
        assertEquals(1.0, result[2]);
    }

    // ── levelStretchLinear ─────────────────────────────────────────────

    @Test
    void levelStretchLinear_noChange() {
        double[] result = calc.levelStretchLinear(new double[]{0.2, 0.5, 0.8}, 0, 0, 1.0);
        assertEquals(0.2, result[0], 1e-12);
        assertEquals(0.5, result[1], 1e-12);
        assertEquals(0.8, result[2], 1e-12);
    }

    @Test
    void levelStretchLinear_blackPoint() {
        // blackDelta=50: low=50/255≈0.196, high=1.0, range≈0.804
        double[] result = calc.levelStretchLinear(new double[]{0.196, 0.4, 0.8}, 50, 0, 1.0);
        assertEquals(0.0, result[0], 1e-6);
        assertTrue(result[1] > 0.2 && result[1] < 0.3);
        assertTrue(result[2] > 0.7);
    }

    @Test
    void levelStretchLinear_gamma() {
        double[] result = calc.levelStretchLinear(new double[]{0.5, 0.5, 0.5}, 0, 0, 2.0);
        // t=0.5, t^2=0.25
        assertEquals(0.25, result[0], 1e-6);
    }

    // ── levelSqueezeLinear ─────────────────────────────────────────────

    @Test
    void levelSqueezeLinear_noChange() {
        double[] result = calc.levelSqueezeLinear(new double[]{0.2, 0.5, 0.8}, 0, 0, 1.0);
        assertEquals(0.2, result[0], 1e-12);
        assertEquals(0.5, result[1], 1e-12);
        assertEquals(0.8, result[2], 1e-12);
    }

    @Test
    void levelSqueezeLinear_blackPoint() {
        // blackDelta=50: low≈0.196, high=1.0, range≈0.804
        double[] result = calc.levelSqueezeLinear(new double[]{0.0, 0.5, 1.0}, 50, 0, 1.0);
        assertEquals(0.196, result[0], 1e-3);
        assertTrue(result[1] > 0.5 && result[1] < 0.7);
        assertEquals(1.0, result[2], 1e-6);
    }

    // ── adjustRedLinear / adjustGreenLinear / adjustBlueLinear ─────────

    @Test
    void adjustRedLinear_positive() {
        double[] result = calc.adjustRedLinear(new double[]{0.5, 0.3, 0.1}, 25.5);
        // 25.5/255 = 0.1, so 0.5+0.1=0.6
        assertEquals(0.6, result[0], 1e-12);
        assertEquals(0.3, result[1], 1e-12);
        assertEquals(0.1, result[2], 1e-12);
    }

    @Test
    void adjustRedLinear_negative() {
        double[] result = calc.adjustRedLinear(new double[]{0.5, 0.3, 0.1}, -51);
        // -51/255 = -0.2, so 0.5-0.2=0.3
        assertEquals(0.3, result[0], 1e-12);
    }

    @Test
    void adjustGreenLinear_positive() {
        double[] result = calc.adjustGreenLinear(new double[]{0.5, 0.3, 0.1}, 51);
        // 51/255 = 0.2, so 0.3+0.2=0.5
        assertEquals(0.5, result[1], 1e-12);
    }

    @Test
    void adjustBlueLinear_positive() {
        double[] result = calc.adjustBlueLinear(new double[]{0.5, 0.3, 0.1}, 51);
        // 51/255 = 0.2, so 0.1+0.2=0.3
        assertEquals(0.3, result[2], 1e-12);
    }

    // ── adjustBrightnessLinear ─────────────────────────────────────────

    @Test
    void adjustBrightnessLinear_positive() {
        double[] result = calc.adjustBrightnessLinear(new double[]{0.2, 0.3, 0.4}, 25.5);
        // 25.5/255 = 0.1, so all +0.1
        assertEquals(0.3, result[0], 1e-12);
        assertEquals(0.4, result[1], 1e-12);
        assertEquals(0.5, result[2], 1e-12);
    }

    @Test
    void adjustBrightnessLinear_negative() {
        double[] result = calc.adjustBrightnessLinear(new double[]{0.5, 0.5, 0.5}, -51);
        // -51/255 = -0.2, so all -0.2
        assertEquals(0.3, result[0], 1e-12);
        assertEquals(0.3, result[1], 1e-12);
        assertEquals(0.3, result[2], 1e-12);
    }

    // ── adjustLuminanceLinear ──────────────────────────────────────────

    @Test
    void adjustLuminanceLinear_positive() {
        double[] result = calc.adjustLuminanceLinear(new double[]{0.2, 0.3, 0.4}, 0.5);
        // factor=1.5
        assertEquals(0.3, result[0], 1e-12);
        assertEquals(0.45, result[1], 1e-12);
        assertEquals(0.6, result[2], 1e-12);
    }

    @Test
    void adjustLuminanceLinear_negative() {
        double[] result = calc.adjustLuminanceLinear(new double[]{0.2, 0.3, 0.4}, -0.5);
        // factor=0.5
        assertEquals(0.1, result[0], 1e-12);
        assertEquals(0.15, result[1], 1e-12);
        assertEquals(0.2, result[2], 1e-12);
    }

    @Test
    void adjustLuminanceLinear_zero() {
        double[] result = calc.adjustLuminanceLinear(new double[]{0.2, 0.5, 0.8}, 0.0);
        assertEquals(0.2, result[0], 1e-12);
        assertEquals(0.5, result[1], 1e-12);
        assertEquals(0.8, result[2], 1e-12);
    }

    // ── adjustLuminanceSaturationLinear ────────────────────────────────

    @Test
    void adjustLuminanceSaturationLinear_desaturate() {
        // amount=1.0 fully desaturates to gray
        double[] result = calc.adjustLuminanceSaturationLinear(new double[]{0.8, 0.4, 0.2}, 1.0);
        // gray = 0.2126*0.8 + 0.7152*0.4 + 0.0722*0.2 ≈ 0.17008+0.28608+0.01444 ≈ 0.4706
        assertEquals(result[0], result[1], 1e-12);
        assertEquals(result[1], result[2], 1e-12);
    }

    @Test
    void adjustLuminanceSaturationLinear_zero() {
        double[] result = calc.adjustLuminanceSaturationLinear(new double[]{0.8, 0.4, 0.2}, 0.0);
        assertEquals(0.8, result[0], 1e-12);
        assertEquals(0.4, result[1], 1e-12);
        assertEquals(0.2, result[2], 1e-12);
    }

    @Test
    void adjustLuminanceSaturationLinear_boost() {
        double[] result = calc.adjustLuminanceSaturationLinear(new double[]{0.8, 0.4, 0.2}, 0.5);
        assertTrue(result[0] < 0.8 && result[0] > 0.6);
    }

    // ── grayscaleLinear ────────────────────────────────────────────────

    @Test
    void grayscaleLinear_color() {
        double[] result = calc.grayscaleLinear(new double[]{0.8, 0.4, 0.2});
        assertEquals(result[0], result[1], 1e-12);
        assertEquals(result[1], result[2], 1e-12);
        assertTrue(result[0] > 0.4 && result[0] < 0.5);
    }

    @Test
    void grayscaleLinear_alreadyGray() {
        double[] result = calc.grayscaleLinear(new double[]{0.5, 0.5, 0.5});
        assertEquals(0.5, result[0], 1e-12);
        assertEquals(0.5, result[1], 1e-12);
        assertEquals(0.5, result[2], 1e-12);
    }

    @Test
    void grayscaleLinear_black() {
        double[] result = calc.grayscaleLinear(new double[]{0.0, 0.0, 0.0});
        assertEquals(0.0, result[0]);
    }

    @Test
    void grayscaleLinear_white() {
        double[] result = calc.grayscaleLinear(new double[]{1.0, 1.0, 1.0});
        assertEquals(1.0, result[0], 1e-12);
    }

    // ── mixLinear ──────────────────────────────────────────────────────

    @Test
    void mixLinear_ratio0() {
        double[] from = {0.2, 0.3, 0.4};
        double[] to = {0.8, 0.7, 0.6};
        double[] result = calc.mixLinear(from, to, 0.0);
        assertEquals(0.2, result[0], 1e-12);
        assertEquals(0.3, result[1], 1e-12);
        assertEquals(0.4, result[2], 1e-12);
    }

    @Test
    void mixLinear_ratio1() {
        double[] from = {0.2, 0.3, 0.4};
        double[] to = {0.8, 0.7, 0.6};
        double[] result = calc.mixLinear(from, to, 1.0);
        assertEquals(0.8, result[0], 1e-12);
        assertEquals(0.7, result[1], 1e-12);
        assertEquals(0.6, result[2], 1e-12);
    }

    @Test
    void mixLinear_ratioHalf() {
        double[] from = {0.2, 0.3, 0.4};
        double[] to = {0.8, 0.7, 0.6};
        double[] result = calc.mixLinear(from, to, 0.5);
        assertEquals(0.5, result[0], 1e-12);
        assertEquals(0.5, result[1], 1e-12);
        assertEquals(0.5, result[2], 1e-12);
    }

    @Test
    void mixLinear_ratioOutOfRange() {
        double[] from = {0.2, 0.3, 0.4};
        double[] to = {0.8, 0.7, 0.6};
        double[] result = calc.mixLinear(from, to, 1.5);
        assertEquals(0.8, result[0], 1e-12);
    }

    @Test
    void mixLinear_negativeRatio() {
        double[] from = {0.2, 0.3, 0.4};
        double[] to = {0.8, 0.7, 0.6};
        double[] result = calc.mixLinear(from, to, -0.5);
        assertEquals(0.2, result[0], 1e-12);
    }
}
