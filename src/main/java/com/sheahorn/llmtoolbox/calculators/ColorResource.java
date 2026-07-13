package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ColorResource {

    private final ColorCalculator calc = new ColorCalculator();
    private final int roundingDigits;

    public ColorResource(
            @ConfigProperty(name = "mfop.calculator.rounding-digits", defaultValue = "8") int roundingDigits) {
        this.roundingDigits = roundingDigits;
    }

    // ==================== conversion ====================

    @Operation(
        operationId = "color_convert",
        summary = "Convert a color between spaces. Source: color_space(tuple) e.g. rgb(255,0,0), hex #000000, CSS name like dodgerblue. Target: hex, rgb, hsl, hsv, cmyk, lab, lch, oklab, oklch, hwb, ycbcr."
    )
    @POST
    @Path("/convert")
    public ColorConvertResponse convert(ColorConvertRequestDto req) {
        if (req == null || req.source_color == null || req.source_color.isBlank()) {
            throw new IllegalArgumentException("source_color is required");
        }
        if (req.target_color_space == null || req.target_color_space.isBlank()) {
            throw new IllegalArgumentException("target_color_space is required");
        }

        ColorDto input = ColorParser.from(req.source_color);
        ColorSpace target = ColorSpace.valueOf(req.target_color_space.trim().toUpperCase());

        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(input, target);
        return r;
    }

    // ==================== modify operations (lossless linear-RGB path) ====================

    // All modify operations below use the lossless linear-RGB path:
    //   input → toLinearRgb() → apply operation → linearRgbTo(originalSpace)
    // This avoids the sRGB gamma compression clamp, preserving wide-gamut colors
    // (e.g. LAB/OKLab colors outside the sRGB triangle). Only WCAG contrast
    // must use the sRGB path per spec.

    // --- invert ---

    @Operation(
        operationId = "color_invert",
        summary = "Invert a color (1 - channel in linear RGB). Output in same color space as input."
    )
    @POST
    @Path("/modify/invert")
    public ColorConvertResponse invert(ColorRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        double[] linear = ColorConverter.toLinearRgb(input);
        double[] result = calc.invertLinear(linear);
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = linearRgbToString(result, originalSpace);
        return r;
    }

    // --- adjustContrast ---

    @Operation(
        operationId = "color_adjust_contrast",
        summary = "Adjust contrast by scaling channels around midpoint 0.5 in linear RGB. Amount: 0=no change, positive=more contrast, negative=less. Output in same color space as input."
    )
    @POST
    @Path("/modify/adjustContrast")
    public ColorConvertResponse adjustContrast(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        double[] linear = ColorConverter.toLinearRgb(input);
        double[] result = calc.adjustContrastLinear(linear, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = linearRgbToString(result, originalSpace);
        return r;
    }

    // --- levelStretch ---

    @Operation(
        operationId = "color_level_stretch",
        summary = "Stretch histogram: remap [blackDelta, 255-whiteDelta] to [0,255] with gamma grayDelta. Output in same color space as input."
    )
    @POST
    @Path("/modify/levelStretch")
    public ColorConvertResponse levelStretch(HistogramAdjustmentDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        double[] linear = ColorConverter.toLinearRgb(input);
        double[] result = calc.levelStretchLinear(linear,
            requireAmount(req.blackDelta), requireAmount(req.whiteDelta), requireAmount(req.grayDelta));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = linearRgbToString(result, originalSpace);
        return r;
    }

    // --- levelSqueeze ---

    @Operation(
        operationId = "color_level_squeeze",
        summary = "Squeeze histogram: remap [0,255] to [blackDelta, 255-whiteDelta] with gamma grayDelta. Output in same color space as input."
    )
    @POST
    @Path("/modify/levelSqueeze")
    public ColorConvertResponse levelSqueeze(HistogramAdjustmentDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        double[] linear = ColorConverter.toLinearRgb(input);
        double[] result = calc.levelSqueezeLinear(linear,
            requireAmount(req.blackDelta), requireAmount(req.whiteDelta), requireAmount(req.grayDelta));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = linearRgbToString(result, originalSpace);
        return r;
    }

    // --- per-channel offsets ---

    @Operation(
        operationId = "color_adjust_red",
        summary = "Offset the red channel by amount in linear RGB. Output in same color space as input."
    )
    @POST
    @Path("/modify/adjustRed")
    public ColorConvertResponse adjustRed(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        double[] linear = ColorConverter.toLinearRgb(input);
        double[] result = calc.adjustRedLinear(linear, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = linearRgbToString(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_green",
        summary = "Offset the green channel by amount in linear RGB. Output in same color space as input."
    )
    @POST
    @Path("/modify/adjustGreen")
    public ColorConvertResponse adjustGreen(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        double[] linear = ColorConverter.toLinearRgb(input);
        double[] result = calc.adjustGreenLinear(linear, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = linearRgbToString(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_blue",
        summary = "Offset the blue channel by amount in linear RGB. Output in same color space as input."
    )
    @POST
    @Path("/modify/adjustBlue")
    public ColorConvertResponse adjustBlue(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        double[] linear = ColorConverter.toLinearRgb(input);
        double[] result = calc.adjustBlueLinear(linear, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = linearRgbToString(result, originalSpace);
        return r;
    }

    // --- brightness ---

    @Operation(
        operationId = "color_adjust_brightness",
        summary = "Adjust brightness by adding amount to all channels in linear RGB. Output in same color space as input."
    )
    @POST
    @Path("/modify/adjustBrightness")
    public ColorConvertResponse adjustBrightness(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        double[] linear = ColorConverter.toLinearRgb(input);
        double[] result = calc.adjustBrightnessLinear(linear, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = linearRgbToString(result, originalSpace);
        return r;
    }

    // --- luminance ---

    @Operation(
        operationId = "color_adjust_luminance",
        summary = "Scale perceptual luminance by factor (1+amount) in linear RGB. Output in same color space as input."
    )
    @POST
    @Path("/modify/adjustLuminance")
    public ColorConvertResponse adjustLuminance(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        double[] linear = ColorConverter.toLinearRgb(input);
        double[] result = calc.adjustLuminanceLinear(linear, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = linearRgbToString(result, originalSpace);
        return r;
    }

    // --- luminance saturation ---

    @Operation(
        operationId = "color_adjust_luminance_saturation",
        summary = "Interpolate between original color and its luminance gray in linear RGB. Amount 0=original, 1=full grayscale, negative=push away from gray. Output in same color space as input."
    )
    @POST
    @Path("/modify/adjustLuminanceSaturation")
    public ColorConvertResponse adjustLuminanceSaturation(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        double[] linear = ColorConverter.toLinearRgb(input);
        double[] result = calc.adjustLuminanceSaturationLinear(linear, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = linearRgbToString(result, originalSpace);
        return r;
    }

    // --- grayscale ---

    @Operation(
        operationId = "color_grayscale",
        summary = "Convert to relative luminance grayscale in linear RGB. Output in same color space as input."
    )
    @POST
    @Path("/modify/grayscale")
    public ColorConvertResponse grayscale(ColorRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        double[] linear = ColorConverter.toLinearRgb(input);
        double[] result = calc.grayscaleLinear(linear);
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = linearRgbToString(result, originalSpace);
        return r;
    }

    // ==================== space-native operations (lossless within their family) ====================

    // HSL

    @Operation(
        operationId = "color_adjust_hue_hsl",
        summary = "Rotate HSL hue by amount in degrees (wraps 0-360). Output in same color space as input."
    )
    @POST
    @Path("/modify/hsl/adjustHue")
    public ColorConvertResponse adjustHueHsl(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        HslColorDto hsl = ColorConverter.convertTo(input, ColorSpace.HSL);
        HslColorDto result = calc.adjustHue(hsl, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_saturation_hsl",
        summary = "Scale HSL saturation by factor (1+amount), clamped 0-100. Output in same color space as input."
    )
    @POST
    @Path("/modify/hsl/adjustSaturation")
    public ColorConvertResponse adjustSaturationHsl(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        HslColorDto hsl = ColorConverter.convertTo(input, ColorSpace.HSL);
        HslColorDto result = calc.adjustSaturation(hsl, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_lightness_hsl",
        summary = "Scale HSL lightness by factor (1+amount), clamped 0-100. Output in same color space as input."
    )
    @POST
    @Path("/modify/hsl/adjustLightness")
    public ColorConvertResponse adjustLightnessHsl(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        HslColorDto hsl = ColorConverter.convertTo(input, ColorSpace.HSL);
        HslColorDto result = calc.adjustLightness(hsl, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    // HSV

    @Operation(
        operationId = "color_adjust_hue_hsv",
        summary = "Rotate HSV hue by amount in degrees (wraps 0-360). Output in same color space as input."
    )
    @POST
    @Path("/modify/hsv/adjustHue")
    public ColorConvertResponse adjustHueHsv(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        HsvColorDto hsv = ColorConverter.convertTo(input, ColorSpace.HSV);
        HsvColorDto result = calc.adjustHue(hsv, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_saturation_hsv",
        summary = "Scale HSV saturation by factor (1+amount), clamped 0-100. Output in same color space as input."
    )
    @POST
    @Path("/modify/hsv/adjustSaturation")
    public ColorConvertResponse adjustSaturationHsv(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        HsvColorDto hsv = ColorConverter.convertTo(input, ColorSpace.HSV);
        HsvColorDto result = calc.adjustSaturation(hsv, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_value_hsv",
        summary = "Scale HSV value by factor (1+amount), clamped 0-100. Output in same color space as input."
    )
    @POST
    @Path("/modify/hsv/adjustValue")
    public ColorConvertResponse adjustValueHsv(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        HsvColorDto hsv = ColorConverter.convertTo(input, ColorSpace.HSV);
        HsvColorDto result = calc.adjustValue(hsv, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    // CIELAB

    @Operation(
        operationId = "color_adjust_lightness_lab",
        summary = "Scale CIELAB L* lightness by factor (1+amount), clamped 0-100. Output in same color space as input."
    )
    @POST
    @Path("/modify/lab/adjustLightness")
    public ColorConvertResponse adjustLightnessLab(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        LabColorDto lab = ColorConverter.convertTo(input, ColorSpace.LAB);
        LabColorDto result = calc.adjustLightness(lab, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_a_lab",
        summary = "Offset CIELAB a* axis (green↔red) by amount. Output in same color space as input."
    )
    @POST
    @Path("/modify/lab/adjustA")
    public ColorConvertResponse adjustALab(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        LabColorDto lab = ColorConverter.convertTo(input, ColorSpace.LAB);
        LabColorDto result = calc.adjustA(lab, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_b_lab",
        summary = "Offset CIELAB b* axis (blue↔yellow) by amount. Output in same color space as input."
    )
    @POST
    @Path("/modify/lab/adjustB")
    public ColorConvertResponse adjustBLab(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        LabColorDto lab = ColorConverter.convertTo(input, ColorSpace.LAB);
        LabColorDto result = calc.adjustB(lab, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    // CIELCh

    @Operation(
        operationId = "color_adjust_lightness_lch",
        summary = "Scale CIELCh L* lightness by factor (1+amount), clamped 0-100. Output in same color space as input."
    )
    @POST
    @Path("/modify/lch/adjustLightness")
    public ColorConvertResponse adjustLightnessLch(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        LchColorDto lch = ColorConverter.convertTo(input, ColorSpace.LCH);
        LchColorDto result = calc.adjustLightness(lch, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_chroma_lch",
        summary = "Scale CIELCh chroma by factor (1+amount), clamped to >=0. Output in same color space as input."
    )
    @POST
    @Path("/modify/lch/adjustChroma")
    public ColorConvertResponse adjustChromaLch(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        LchColorDto lch = ColorConverter.convertTo(input, ColorSpace.LCH);
        LchColorDto result = calc.adjustChroma(lch, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_hue_lch",
        summary = "Rotate CIELCh hue by amount in degrees (wraps 0-360). Output in same color space as input."
    )
    @POST
    @Path("/modify/lch/adjustHue")
    public ColorConvertResponse adjustHueLch(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        LchColorDto lch = ColorConverter.convertTo(input, ColorSpace.LCH);
        LchColorDto result = calc.adjustHue(lch, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    // OKLab

    @Operation(
        operationId = "color_adjust_lightness_oklab",
        summary = "Scale OKLab L lightness by factor (1+amount), clamped 0-1. Output in same color space as input."
    )
    @POST
    @Path("/modify/oklab/adjustLightness")
    public ColorConvertResponse adjustLightnessOkLab(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        OkLabColorDto oklab = ColorConverter.convertTo(input, ColorSpace.OKLAB);
        OkLabColorDto result = calc.adjustLightness(oklab, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_a_oklab",
        summary = "Offset OKLab a axis by amount. Output in same color space as input."
    )
    @POST
    @Path("/modify/oklab/adjustA")
    public ColorConvertResponse adjustAOkLab(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        OkLabColorDto oklab = ColorConverter.convertTo(input, ColorSpace.OKLAB);
        OkLabColorDto result = calc.adjustA(oklab, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_b_oklab",
        summary = "Offset OKLab b axis by amount. Output in same color space as input."
    )
    @POST
    @Path("/modify/oklab/adjustB")
    public ColorConvertResponse adjustBOkLab(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        OkLabColorDto oklab = ColorConverter.convertTo(input, ColorSpace.OKLAB);
        OkLabColorDto result = calc.adjustB(oklab, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    // OKLCh

    @Operation(
        operationId = "color_adjust_lightness_oklch",
        summary = "Scale OKLCh L lightness by factor (1+amount), clamped 0-1. Output in same color space as input."
    )
    @POST
    @Path("/modify/oklch/adjustLightness")
    public ColorConvertResponse adjustLightnessOkLch(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        OkLchColorDto oklch = ColorConverter.convertTo(input, ColorSpace.OKLCH);
        OkLchColorDto result = calc.adjustLightness(oklch, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_chroma_oklch",
        summary = "Scale OKLCh chroma by factor (1+amount), clamped to >=0. Output in same color space as input."
    )
    @POST
    @Path("/modify/oklch/adjustChroma")
    public ColorConvertResponse adjustChromaOkLch(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        OkLchColorDto oklch = ColorConverter.convertTo(input, ColorSpace.OKLCH);
        OkLchColorDto result = calc.adjustChroma(oklch, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_hue_oklch",
        summary = "Rotate OKLCh hue by amount in degrees (wraps 0-360). Output in same color space as input."
    )
    @POST
    @Path("/modify/oklch/adjustHue")
    public ColorConvertResponse adjustHueOkLch(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        OkLchColorDto oklch = ColorConverter.convertTo(input, ColorSpace.OKLCH);
        OkLchColorDto result = calc.adjustHue(oklch, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    // HWB

    @Operation(
        operationId = "color_adjust_hue_hwb",
        summary = "Rotate HWB hue by amount in degrees (wraps 0-360). Output in same color space as input."
    )
    @POST
    @Path("/modify/hwb/adjustHue")
    public ColorConvertResponse adjustHueHwb(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        HwbColorDto hwb = ColorConverter.convertTo(input, ColorSpace.HWB);
        HwbColorDto result = calc.adjustHue(hwb, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_whiteness",
        summary = "Scale HWB whiteness by factor (1+amount), clamped 0-100. Output in same color space as input."
    )
    @POST
    @Path("/modify/hwb/adjustWhiteness")
    public ColorConvertResponse adjustWhiteness(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        HwbColorDto hwb = ColorConverter.convertTo(input, ColorSpace.HWB);
        HwbColorDto result = calc.adjustWhiteness(hwb, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_blackness",
        summary = "Scale HWB blackness by factor (1+amount), clamped 0-100. Output in same color space as input."
    )
    @POST
    @Path("/modify/hwb/adjustBlackness")
    public ColorConvertResponse adjustBlackness(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        HwbColorDto hwb = ColorConverter.convertTo(input, ColorSpace.HWB);
        HwbColorDto result = calc.adjustBlackness(hwb, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    // YCbCr

    @Operation(
        operationId = "color_adjust_luma",
        summary = "Scale YCbCr luma (Y) by factor (1+amount), clamped 0-1. Output in same color space as input."
    )
    @POST
    @Path("/modify/ycbcr/adjustLuma")
    public ColorConvertResponse adjustLuma(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        YcbcrColorDto ycbcr = ColorConverter.convertTo(input, ColorSpace.YCBCR);
        YcbcrColorDto result = calc.adjustLuma(ycbcr, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_blue_chroma",
        summary = "Offset YCbCr Cb (blue chroma) by amount, clamped 0-1. Output in same color space as input."
    )
    @POST
    @Path("/modify/ycbcr/adjustBlueChroma")
    public ColorConvertResponse adjustBlueChroma(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        YcbcrColorDto ycbcr = ColorConverter.convertTo(input, ColorSpace.YCBCR);
        YcbcrColorDto result = calc.adjustBlueChroma(ycbcr, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    @Operation(
        operationId = "color_adjust_red_chroma",
        summary = "Offset YCbCr Cr (red chroma) by amount, clamped 0-1. Output in same color space as input."
    )
    @POST
    @Path("/modify/ycbcr/adjustRedChroma")
    public ColorConvertResponse adjustRedChroma(AdjustBrightnessRequestDto req) {
        ColorDto input = requireColor(req);
        ColorSpace originalSpace = detectSpace(input);
        YcbcrColorDto ycbcr = ColorConverter.convertTo(input, ColorSpace.YCBCR);
        YcbcrColorDto result = calc.adjustRedChroma(ycbcr, requireAmount(req.amount));
        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, originalSpace);
        return r;
    }

    // ==================== Delta-E ====================

    @Operation(
        operationId = "color_delta_e",
        summary = "Compute perceptual color distance (Delta-E) between two colors using CIELAB. Formula: CIE76, CIE94, or CIEDE2000."
    )
    @POST
    @Path("/deltaE")
    public DeltaEResponse deltaE(DeltaERequestDto req) {
        if (req == null || req.source_color == null || req.source_color.isBlank()) {
            throw new IllegalArgumentException("source_color is required");
        }
        if (req.compare_color == null || req.compare_color.isBlank()) {
            throw new IllegalArgumentException("compare_color is required");
        }
        String formula = req.formula != null && !req.formula.isBlank() ? req.formula : "CIEDE2000";

        LabColorDto lab1 = ColorConverter.convertTo(ColorParser.from(req.source_color), ColorSpace.LAB);
        LabColorDto lab2 = ColorConverter.convertTo(ColorParser.from(req.compare_color), ColorSpace.LAB);

        DeltaEResponse r = new DeltaEResponse();
        r.delta_e = roundToDigits(calc.deltaE(lab1, lab2, formula));
        r.formula = formula.toUpperCase().trim();
        return r;
    }

    // ==================== WCAG Contrast (must use sRGB path per spec) ====================

    @Operation(
        operationId = "color_wcag_contrast",
        summary = "Compute WCAG 2.1 contrast ratio between two colors and check AA/AAA compliance."
    )
    @POST
    @Path("/contrast")
    public ContrastResponse contrast(ContrastRequestDto req) {
        if (req == null || req.source_color == null || req.source_color.isBlank()) {
            throw new IllegalArgumentException("source_color is required");
        }
        if (req.compare_color == null || req.compare_color.isBlank()) {
            throw new IllegalArgumentException("compare_color is required");
        }

        // WCAG 2.1 defines contrast ratio in sRGB relative luminance — must use sRGB path
        RgbColorDto rgb1 = ColorConverter.convertTo(ColorParser.from(req.source_color), ColorSpace.RGB);
        RgbColorDto rgb2 = ColorConverter.convertTo(ColorParser.from(req.compare_color), ColorSpace.RGB);

        double ratio = calc.wcagContrastRatio(rgb1, rgb2);

        ContrastResponse r = new ContrastResponse();
        r.contrast_ratio = roundToDigits(ratio);
        r.wcag_pass_aa_normal = ratio >= 4.5;
        r.wcag_pass_aa_large = ratio >= 3.0;
        r.wcag_pass_aaa_normal = ratio >= 7.0;
        r.wcag_pass_aaa_large = ratio >= 4.5;
        return r;
    }

    // ==================== Mix (lossless linear-RGB path) ====================

    @Operation(
        operationId = "color_mix",
        summary = "Mix two colors by ratio (0=source, 1=target) in linear RGB. Output in same color space as source."
    )
    @POST
    @Path("/mix")
    public ColorConvertResponse mix(MixRequestDto req) {
        if (req == null || req.source_color == null || req.source_color.isBlank()) {
            throw new IllegalArgumentException("source_color is required");
        }
        if (req.target_color == null || req.target_color.isBlank()) {
            throw new IllegalArgumentException("target_color is required");
        }
        double ratio = req.ratio != null ? req.ratio : 0.5;

        ColorDto input = ColorParser.from(req.source_color);
        ColorSpace originalSpace = detectSpace(input);
        double[] from = ColorConverter.toLinearRgb(input);
        double[] to = ColorConverter.toLinearRgb(ColorParser.from(req.target_color));
        double[] result = calc.mixLinear(from, to, ratio);

        ColorConvertResponse r = new ColorConvertResponse();
        r.color = linearRgbToString(result, originalSpace);
        return r;
    }

    // ==================== Temperature ====================

    @Operation(
        operationId = "color_temperature_to_rgb",
        summary = "Convert a color temperature in Kelvin to an sRGB approximation. Output as rgb(...)."
    )
    @POST
    @Path("/temperature")
    public ColorConvertResponse temperature(TemperatureRequestDto req) {
        if (req == null || req.temperature == null) {
            throw new IllegalArgumentException("temperature is required");
        }
        if (req.temperature < 1000.0 || req.temperature > 40000.0) {
            throw new IllegalArgumentException("temperature must be between 1000K and 40000K");
        }

        RgbColorDto result = calc.temperatureToRgb(req.temperature);

        ColorConvertResponse r = new ColorConvertResponse();
        r.color = ColorParser.to(result, ColorSpace.RGB);
        return r;
    }

    // ==================== helpers ====================

    private ColorDto requireColor(ColorSource req) {
        if (req == null) {
            throw new IllegalArgumentException("Request is required");
        }
        String sourceColor = req.sourceColor();
        if (sourceColor == null || sourceColor.isBlank()) {
            throw new IllegalArgumentException("source_color is required");
        }
        return ColorParser.from(sourceColor);
    }

    private double requireAmount(Double amount) {
        if (amount == null) {
            throw new IllegalArgumentException("amount is required");
        }
        return amount;
    }

    private double roundToDigits(double value) {
        if (roundingDigits <= 0) {
            return value;
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return value;
        }
        double factor = Math.pow(10, roundingDigits);
        if (Math.abs(value) > Double.MAX_VALUE / factor) {
            return value;
        }
        return Math.round(value * factor) / factor;
    }

    /** Convert linear RGB to a string in the given color space, using the lossless path. */
    private String linearRgbToString(double[] linear, ColorSpace space) {
        ColorDto dto = ColorConverter.linearRgbTo(space, linear);
        return ColorParser.dtoToString(dto);
    }

    private ColorSpace detectSpace(ColorDto color) {
        if (color instanceof RgbColorDto) return ColorSpace.RGB;
        if (color instanceof HexColorDto) return ColorSpace.HEX;
        if (color instanceof HslColorDto) return ColorSpace.HSL;
        if (color instanceof HsvColorDto) return ColorSpace.HSV;
        if (color instanceof CmykColorDto) return ColorSpace.CMYK;
        if (color instanceof LabColorDto) return ColorSpace.LAB;
        if (color instanceof LchColorDto) return ColorSpace.LCH;
        if (color instanceof OkLabColorDto) return ColorSpace.OKLAB;
        if (color instanceof OkLchColorDto) return ColorSpace.OKLCH;
        if (color instanceof HwbColorDto) return ColorSpace.HWB;
        if (color instanceof YcbcrColorDto) return ColorSpace.YCBCR;
        throw new IllegalArgumentException("Unknown ColorDto type");
    }
}
