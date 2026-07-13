package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OfficeResource {

    private final OfficeCalculator calc;

    public OfficeResource(
            @ConfigProperty(name = "mfop.calculator.rounding-digits", defaultValue = "8") int roundingDigits) {
        this.calc = new OfficeCalculator(roundingDigits);
    }

    // ==================== Length ====================

    @Operation(operationId = "calculator_length_conversion", summary = "Convert between length units: nm, um, mm, cm, m, km, in, ft, yd, mi")
    @POST @Path("/convert-length")
    public AggregateResponse convertLength(UnitConversionRequestDto req) {
        if (req == null || req.value == null || req.sourceUnit == null || req.targetUnit == null)
            throw new IllegalArgumentException("value, sourceUnit, and targetUnit are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "length_conversion";
        LengthUnit source = LengthUnit.valueOf(req.sourceUnit.toUpperCase());
        LengthUnit target = LengthUnit.valueOf(req.targetUnit.toUpperCase());
        r.result = calc.convertLength(req.value, source, target);
        return r;
    }

    // ==================== Mass ====================

    @Operation(operationId = "calculator_mass_conversion", summary = "Convert between mass units: mg, g, kg, t, oz, lb")
    @POST @Path("/convert-mass")
    public AggregateResponse convertMass(UnitConversionRequestDto req) {
        if (req == null || req.value == null || req.sourceUnit == null || req.targetUnit == null)
            throw new IllegalArgumentException("value, sourceUnit, and targetUnit are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "mass_conversion";
        MassUnit source = MassUnit.valueOf(req.sourceUnit.toUpperCase());
        MassUnit target = MassUnit.valueOf(req.targetUnit.toUpperCase());
        r.result = calc.convertMass(req.value, source, target);
        return r;
    }

    // ==================== Volume ====================

    @Operation(operationId = "calculator_volume_conversion", summary = "Convert between volume units: ml, l, m3, gal, qt, pt, cup, floz")
    @POST @Path("/convert-volume")
    public AggregateResponse convertVolume(UnitConversionRequestDto req) {
        if (req == null || req.value == null || req.sourceUnit == null || req.targetUnit == null)
            throw new IllegalArgumentException("value, sourceUnit, and targetUnit are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "volume_conversion";
        VolumeUnit source = VolumeUnit.valueOf(req.sourceUnit.toUpperCase());
        VolumeUnit target = VolumeUnit.valueOf(req.targetUnit.toUpperCase());
        r.result = calc.convertVolume(req.value, source, target);
        return r;
    }

    // ==================== Area ====================

    @Operation(operationId = "calculator_area_conversion", summary = "Convert between area units: m2, km2, ha, acre, ft2")
    @POST @Path("/convert-area")
    public AggregateResponse convertArea(UnitConversionRequestDto req) {
        if (req == null || req.value == null || req.sourceUnit == null || req.targetUnit == null)
            throw new IllegalArgumentException("value, sourceUnit, and targetUnit are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "area_conversion";
        AreaUnit source = AreaUnit.valueOf(req.sourceUnit.toUpperCase());
        AreaUnit target = AreaUnit.valueOf(req.targetUnit.toUpperCase());
        r.result = calc.convertArea(req.value, source, target);
        return r;
    }

    // ==================== Speed ====================

    @Operation(operationId = "calculator_speed_conversion", summary = "Convert between speed units: ms (m/s), kmh, mph, knot")
    @POST @Path("/convert-speed")
    public AggregateResponse convertSpeed(UnitConversionRequestDto req) {
        if (req == null || req.value == null || req.sourceUnit == null || req.targetUnit == null)
            throw new IllegalArgumentException("value, sourceUnit, and targetUnit are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "speed_conversion";
        SpeedUnit source = SpeedUnit.valueOf(req.sourceUnit.toUpperCase());
        SpeedUnit target = SpeedUnit.valueOf(req.targetUnit.toUpperCase());
        r.result = calc.convertSpeed(req.value, source, target);
        return r;
    }

    // ==================== Pressure ====================

    @Operation(operationId = "calculator_pressure_conversion", summary = "Convert between pressure units: pa, kpa, bar, atm, psi")
    @POST @Path("/convert-pressure")
    public AggregateResponse convertPressure(UnitConversionRequestDto req) {
        if (req == null || req.value == null || req.sourceUnit == null || req.targetUnit == null)
            throw new IllegalArgumentException("value, sourceUnit, and targetUnit are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "pressure_conversion";
        PressureUnit source = PressureUnit.valueOf(req.sourceUnit.toUpperCase());
        PressureUnit target = PressureUnit.valueOf(req.targetUnit.toUpperCase());
        r.result = calc.convertPressure(req.value, source, target);
        return r;
    }

    // ==================== Energy ====================

    @Operation(operationId = "calculator_energy_conversion", summary = "Convert between energy units: j, kj, cal, kcal, wh, kwh")
    @POST @Path("/convert-energy")
    public AggregateResponse convertEnergy(UnitConversionRequestDto req) {
        if (req == null || req.value == null || req.sourceUnit == null || req.targetUnit == null)
            throw new IllegalArgumentException("value, sourceUnit, and targetUnit are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "energy_conversion";
        EnergyUnit source = EnergyUnit.valueOf(req.sourceUnit.toUpperCase());
        EnergyUnit target = EnergyUnit.valueOf(req.targetUnit.toUpperCase());
        r.result = calc.convertEnergy(req.value, source, target);
        return r;
    }

    // ==================== Data ====================

    @Operation(operationId = "calculator_data_conversion", summary = "Convert between data units: b, kb, kib, mb, mib, gb, gib, tb, tib")
    @POST @Path("/convert-data")
    public AggregateResponse convertData(UnitConversionRequestDto req) {
        if (req == null || req.value == null || req.sourceUnit == null || req.targetUnit == null)
            throw new IllegalArgumentException("value, sourceUnit, and targetUnit are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "data_conversion";
        DataUnit source = DataUnit.valueOf(req.sourceUnit.toUpperCase());
        DataUnit target = DataUnit.valueOf(req.targetUnit.toUpperCase());
        r.result = calc.convertData(req.value, source, target);
        return r;
    }

    // ==================== Temperature ====================

    @Operation(operationId = "calculator_temperature_conversion", summary = "Convert between temperature units: c, f, k")
    @POST @Path("/convert-temperature")
    public AggregateResponse convertTemperature(UnitConversionRequestDto req) {
        if (req == null || req.value == null || req.sourceUnit == null || req.targetUnit == null)
            throw new IllegalArgumentException("value, sourceUnit, and targetUnit are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "temperature_conversion";
        TemperatureUnit source = TemperatureUnit.valueOf(req.sourceUnit.toUpperCase());
        TemperatureUnit target = TemperatureUnit.valueOf(req.targetUnit.toUpperCase());
        r.result = calc.convertTemperature(req.value, source, target);
        return r;
    }


}
