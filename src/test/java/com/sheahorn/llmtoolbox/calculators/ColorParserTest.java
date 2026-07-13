package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorParserTest {

    // ── from: hex ─────────────────────────────────────────────────────

    @Test
    void from_hex6() {
        ColorDto result = ColorParser.from("#FF8040");
        assertTrue(result instanceof HexColorDto);
        assertEquals("#FF8040", ((HexColorDto) result).hex);
    }

    @Test
    void from_hex3() {
        ColorDto result = ColorParser.from("#F80");
        assertTrue(result instanceof HexColorDto);
        assertEquals("#FF8800", ((HexColorDto) result).hex);
    }

    @Test
    void from_hexLowercase() {
        ColorDto result = ColorParser.from("#ff8040");
        assertTrue(result instanceof HexColorDto);
        assertEquals("#FF8040", ((HexColorDto) result).hex);
    }

    @Test
    void from_hexInvalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> ColorParser.from("#GGG"));
    }

    @Test
    void from_hexTooShort_throws() {
        assertThrows(IllegalArgumentException.class, () -> ColorParser.from("#FF"));
    }

    // ── from: CSS named colors ────────────────────────────────────────

    @Test
    void from_cssName_red() {
        ColorDto result = ColorParser.from("red");
        assertTrue(result instanceof HexColorDto);
        assertEquals("#FF0000", ((HexColorDto) result).hex);
    }

    @Test
    void from_cssName_dodgerblue() {
        ColorDto result = ColorParser.from("dodgerblue");
        assertTrue(result instanceof HexColorDto);
        assertEquals("#1E90FF", ((HexColorDto) result).hex);
    }

    @Test
    void from_cssName_white() {
        ColorDto result = ColorParser.from("white");
        assertTrue(result instanceof HexColorDto);
        assertEquals("#FFFFFF", ((HexColorDto) result).hex);
    }

    @Test
    void from_cssName_black() {
        ColorDto result = ColorParser.from("black");
        assertTrue(result instanceof HexColorDto);
        assertEquals("#000000", ((HexColorDto) result).hex);
    }

    @Test
    void from_cssName_caseInsensitive() {
        ColorDto lower = ColorParser.from("DodgerBlue");
        ColorDto upper = ColorParser.from("DODGERBLUE");
        assertEquals(((HexColorDto) lower).hex, ((HexColorDto) upper).hex);
    }

    // ── from: rgb() ───────────────────────────────────────────────────

    @Test
    void from_rgb() {
        ColorDto result = ColorParser.from("rgb(255,128,64)");
        assertTrue(result instanceof RgbColorDto);
        RgbColorDto rgb = (RgbColorDto) result;
        assertEquals(255, rgb.r);
        assertEquals(128, rgb.g);
        assertEquals(64, rgb.b);
    }

    @Test
    void from_rgb_withSpaces() {
        ColorDto result = ColorParser.from("rgb( 255 , 128 , 64 )");
        assertTrue(result instanceof RgbColorDto);
        RgbColorDto rgb = (RgbColorDto) result;
        assertEquals(255, rgb.r);
        assertEquals(128, rgb.g);
        assertEquals(64, rgb.b);
    }

    @Test
    void from_rgb_clamps() {
        ColorDto result = ColorParser.from("rgb(300,-10,128)");
        RgbColorDto rgb = (RgbColorDto) result;
        assertEquals(255, rgb.r);
        assertEquals(0, rgb.g);
        assertEquals(128, rgb.b);
    }

    // ── from: hsl() ───────────────────────────────────────────────────

    @Test
    void from_hsl() {
        ColorDto result = ColorParser.from("hsl(0,100,50)");
        assertTrue(result instanceof HslColorDto);
        HslColorDto hsl = (HslColorDto) result;
        assertEquals(0.0, hsl.h);
        assertEquals(100.0, hsl.s);
        assertEquals(50.0, hsl.l);
    }

    // ── from: hsv() ───────────────────────────────────────────────────

    @Test
    void from_hsv() {
        ColorDto result = ColorParser.from("hsv(0,100,100)");
        assertTrue(result instanceof HsvColorDto);
        HsvColorDto hsv = (HsvColorDto) result;
        assertEquals(0.0, hsv.h);
        assertEquals(100.0, hsv.s);
        assertEquals(100.0, hsv.v);
    }

    // ── from: cmyk() ──────────────────────────────────────────────────

    @Test
    void from_cmyk() {
        ColorDto result = ColorParser.from("cmyk(0,100,100,0)");
        assertTrue(result instanceof CmykColorDto);
        CmykColorDto cmyk = (CmykColorDto) result;
        assertEquals(0.0, cmyk.c);
        assertEquals(100.0, cmyk.m);
        assertEquals(100.0, cmyk.y);
        assertEquals(0.0, cmyk.k);
    }

    // ── from: lab() ───────────────────────────────────────────────────

    @Test
    void from_lab() {
        ColorDto result = ColorParser.from("lab(53.23,80.11,67.22)");
        assertTrue(result instanceof LabColorDto);
        LabColorDto lab = (LabColorDto) result;
        assertEquals(53.23, lab.L);
        assertEquals(80.11, lab.a);
        assertEquals(67.22, lab.b);
    }

    // ── from: lch() ───────────────────────────────────────────────────

    @Test
    void from_lch() {
        ColorDto result = ColorParser.from("lch(53.23,104.55,40.0)");
        assertTrue(result instanceof LchColorDto);
        LchColorDto lch = (LchColorDto) result;
        assertEquals(53.23, lch.L);
        assertEquals(104.55, lch.C);
        assertEquals(40.0, lch.h);
    }

    // ── from: oklab() ─────────────────────────────────────────────────

    @Test
    void from_oklab() {
        ColorDto result = ColorParser.from("oklab(0.628,0.225,0.126)");
        assertTrue(result instanceof OkLabColorDto);
        OkLabColorDto oklab = (OkLabColorDto) result;
        assertEquals(0.628, oklab.L);
        assertEquals(0.225, oklab.a);
        assertEquals(0.126, oklab.b);
    }

    // ── from: oklch() ─────────────────────────────────────────────────

    @Test
    void from_oklch() {
        ColorDto result = ColorParser.from("oklch(0.628,0.258,29.23)");
        assertTrue(result instanceof OkLchColorDto);
        OkLchColorDto oklch = (OkLchColorDto) result;
        assertEquals(0.628, oklch.L);
        assertEquals(0.258, oklch.C);
        assertEquals(29.23, oklch.h);
    }

    // ── from: hwb() ───────────────────────────────────────────────────

    @Test
    void from_hwb() {
        ColorDto result = ColorParser.from("hwb(0,0,0)");
        assertTrue(result instanceof HwbColorDto);
        HwbColorDto hwb = (HwbColorDto) result;
        assertEquals(0.0, hwb.h);
        assertEquals(0.0, hwb.w);
        assertEquals(0.0, hwb.b);
    }

    // ── from: ycbcr() ─────────────────────────────────────────────────

    @Test
    void from_ycbcr() {
        ColorDto result = ColorParser.from("ycbcr(0.5,0.5,0.5)");
        assertTrue(result instanceof YcbcrColorDto);
        YcbcrColorDto ycbcr = (YcbcrColorDto) result;
        assertEquals(0.5, ycbcr.y);
        assertEquals(0.5, ycbcr.cb);
        assertEquals(0.5, ycbcr.cr);
    }

    // ── from: invalid ─────────────────────────────────────────────────

    @Test
    void from_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> ColorParser.from(null));
    }

    @Test
    void from_blank_throws() {
        assertThrows(IllegalArgumentException.class, () -> ColorParser.from("   "));
    }

    @Test
    void from_unknownSpace_throws() {
        assertThrows(IllegalArgumentException.class, () -> ColorParser.from("xyz(1,2,3)"));
    }

    @Test
    void from_noParens_throws() {
        assertThrows(IllegalArgumentException.class, () -> ColorParser.from("something"));
    }

    @Test
    void from_wrongArgCount_throws() {
        assertThrows(IllegalArgumentException.class, () -> ColorParser.from("rgb(255,0)"));
    }

    // ── to ────────────────────────────────────────────────────────────

    @Test
    void to_RGBtoHEX() {
        RgbColorDto rgb = new RgbColorDto(255, 128, 64);
        String result = ColorParser.to(rgb, ColorSpace.HEX);
        assertEquals("#FF8040", result);
    }

    @Test
    void to_RGBtoHSL() {
        RgbColorDto rgb = new RgbColorDto(255, 0, 0);
        String result = ColorParser.to(rgb, ColorSpace.HSL);
        assertEquals("hsl(0,100,50)", result);
    }

    @Test
    void to_RGBtoHSV() {
        RgbColorDto rgb = new RgbColorDto(255, 0, 0);
        String result = ColorParser.to(rgb, ColorSpace.HSV);
        assertEquals("hsv(0,100,100)", result);
    }

    @Test
    void to_RGBtoCMYK() {
        RgbColorDto rgb = new RgbColorDto(0, 0, 0);
        String result = ColorParser.to(rgb, ColorSpace.CMYK);
        assertEquals("cmyk(0,0,0,100)", result);
    }

    @Test
    void to_RGBtoLAB() {
        RgbColorDto rgb = new RgbColorDto(255, 255, 255);
        String result = ColorParser.to(rgb, ColorSpace.LAB);
        assertTrue(result.startsWith("lab("));
    }

    @Test
    void to_RGBtoLCH() {
        RgbColorDto rgb = new RgbColorDto(255, 0, 0);
        String result = ColorParser.to(rgb, ColorSpace.LCH);
        assertTrue(result.startsWith("lch("));
    }

    @Test
    void to_RGBtoOKLAB() {
        RgbColorDto rgb = new RgbColorDto(255, 255, 255);
        String result = ColorParser.to(rgb, ColorSpace.OKLAB);
        assertTrue(result.startsWith("oklab("));
    }

    @Test
    void to_RGBtoOKLCH() {
        RgbColorDto rgb = new RgbColorDto(255, 0, 0);
        String result = ColorParser.to(rgb, ColorSpace.OKLCH);
        assertTrue(result.startsWith("oklch("));
    }

    @Test
    void to_RGBtoHWB() {
        RgbColorDto rgb = new RgbColorDto(255, 0, 0);
        String result = ColorParser.to(rgb, ColorSpace.HWB);
        assertEquals("hwb(0,0,0)", result);
    }

    @Test
    void to_RGBtoYCBCR() {
        RgbColorDto rgb = new RgbColorDto(255, 255, 255);
        String result = ColorParser.to(rgb, ColorSpace.YCBCR);
        assertTrue(result.startsWith("ycbcr("));
    }

    // ── dtoToString ───────────────────────────────────────────────────

    @Test
    void dtoToString_RGB() {
        assertEquals("rgb(255,128,64)", ColorParser.dtoToString(new RgbColorDto(255, 128, 64)));
    }

    @Test
    void dtoToString_HEX() {
        assertEquals("#FF8040", ColorParser.dtoToString(new HexColorDto("#FF8040")));
    }

    @Test
    void dtoToString_HSL() {
        assertEquals("hsl(0,100,50)", ColorParser.dtoToString(new HslColorDto(0, 100, 50)));
    }

    @Test
    void dtoToString_HSV() {
        assertEquals("hsv(0,100,100)", ColorParser.dtoToString(new HsvColorDto(0, 100, 100)));
    }

    @Test
    void dtoToString_CMYK() {
        assertEquals("cmyk(0,100,100,0)", ColorParser.dtoToString(new CmykColorDto(0, 100, 100, 0)));
    }

    @Test
    void dtoToString_LAB() {
        String result = ColorParser.dtoToString(new LabColorDto(53.23, 80.11, 67.22));
        assertTrue(result.startsWith("lab("));
    }

    @Test
    void dtoToString_LCH() {
        String result = ColorParser.dtoToString(new LchColorDto(53.23, 104.55, 40.0));
        assertTrue(result.startsWith("lch("));
    }

    @Test
    void dtoToString_OKLAB() {
        String result = ColorParser.dtoToString(new OkLabColorDto(0.628, 0.225, 0.126));
        assertTrue(result.startsWith("oklab("));
    }

    @Test
    void dtoToString_OKLCH() {
        String result = ColorParser.dtoToString(new OkLchColorDto(0.628, 0.258, 29.23));
        assertTrue(result.startsWith("oklch("));
    }

    @Test
    void dtoToString_HWB() {
        assertEquals("hwb(0,0,0)", ColorParser.dtoToString(new HwbColorDto(0, 0, 0)));
    }

    @Test
    void dtoToString_YCBCR() {
        String result = ColorParser.dtoToString(new YcbcrColorDto(0.5, 0.5, 0.5));
        assertTrue(result.startsWith("ycbcr("));
    }

    // ── parse → to roundtrip ──────────────────────────────────────────

    @Test
    void roundtrip_parseTo_hex() {
        ColorDto parsed = ColorParser.from("#FF8040");
        String back = ColorParser.to(parsed, ColorSpace.HEX);
        assertEquals("#FF8040", back);
    }

    @Test
    void roundtrip_parseTo_rgb() {
        ColorDto parsed = ColorParser.from("rgb(100,150,200)");
        String back = ColorParser.to(parsed, ColorSpace.RGB);
        assertEquals("rgb(100,150,200)", back);
    }

    @Test
    void roundtrip_parseTo_hsl() {
        ColorDto parsed = ColorParser.from("hsl(120,50,50)");
        String back = ColorParser.to(parsed, ColorSpace.HSL);
        assertEquals("hsl(120,50,50)", back);
    }

    @Test
    void roundtrip_parseTo_hsv() {
        ColorDto parsed = ColorParser.from("hsv(240,75,60)");
        String back = ColorParser.to(parsed, ColorSpace.HSV);
        assertEquals("hsv(240,75,60)", back);
    }

    @Test
    void roundtrip_parseTo_cmyk() {
        ColorDto parsed = ColorParser.from("cmyk(10,20,30,5)");
        String back = ColorParser.to(parsed, ColorSpace.CMYK);
        assertEquals("cmyk(10,20,30,5)", back);
    }

    @Test
    void roundtrip_parseTo_lab() {
        ColorDto parsed = ColorParser.from("lab(50,25,-25)");
        String back = ColorParser.to(parsed, ColorSpace.LAB);
        assertTrue(back.startsWith("lab(50.00,25.00,-25.00)"));
    }

    @Test
    void roundtrip_parseTo_lch() {
        ColorDto parsed = ColorParser.from("lch(50,30,180)");
        String back = ColorParser.to(parsed, ColorSpace.LCH);
        assertTrue(back.startsWith("lch(50.00,30.00,180.00)"));
    }

    @Test
    void roundtrip_parseTo_oklab() {
        ColorDto parsed = ColorParser.from("oklab(0.5,0.1,-0.1)");
        String back = ColorParser.to(parsed, ColorSpace.OKLAB);
        assertTrue(back.startsWith("oklab(0.5000,0.1000,-0.1000)"));
    }

    @Test
    void roundtrip_parseTo_oklch() {
        ColorDto parsed = ColorParser.from("oklch(0.5,0.2,180)");
        String back = ColorParser.to(parsed, ColorSpace.OKLCH);
        assertTrue(back.startsWith("oklch(0.5000,0.2000,180.00)"));
    }

    @Test
    void roundtrip_parseTo_hwb() {
        ColorDto parsed = ColorParser.from("hwb(120,20,10)");
        String back = ColorParser.to(parsed, ColorSpace.HWB);
        assertEquals("hwb(120,20,10)", back);
    }

    @Test
    void roundtrip_parseTo_ycbcr() {
        ColorDto parsed = ColorParser.from("ycbcr(0.5,0.5,0.5)");
        String back = ColorParser.to(parsed, ColorSpace.YCBCR);
        assertTrue(back.startsWith("ycbcr(0.50,0.50,0.50)"));
    }
}
