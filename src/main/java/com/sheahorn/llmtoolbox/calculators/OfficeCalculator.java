package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.common.DoubleValueCalculator;

public class OfficeCalculator extends DoubleValueCalculator {

    public OfficeCalculator(int roundingDigits) {
        super(roundingDigits);
    }

    // ==================== Length ====================

    public double convertLength(double value, LengthUnit source, LengthUnit target) {
        return round(value * source.getFactor() / target.getFactor());
    }

    // ==================== Mass ====================

    public double convertMass(double value, MassUnit source, MassUnit target) {
        return round(value * source.getFactor() / target.getFactor());
    }

    // ==================== Volume ====================

    public double convertVolume(double value, VolumeUnit source, VolumeUnit target) {
        return round(value * source.getFactor() / target.getFactor());
    }

    // ==================== Area ====================

    public double convertArea(double value, AreaUnit source, AreaUnit target) {
        return round(value * source.getFactor() / target.getFactor());
    }

    // ==================== Speed ====================

    public double convertSpeed(double value, SpeedUnit source, SpeedUnit target) {
        return round(value * source.getFactor() / target.getFactor());
    }

    // ==================== Pressure ====================

    public double convertPressure(double value, PressureUnit source, PressureUnit target) {
        return round(value * source.getFactor() / target.getFactor());
    }

    // ==================== Energy ====================

    public double convertEnergy(double value, EnergyUnit source, EnergyUnit target) {
        return round(value * source.getFactor() / target.getFactor());
    }

    // ==================== Data ====================

    public double convertData(double value, DataUnit source, DataUnit target) {
        return round(value * source.getFactor() / target.getFactor());
    }

    // ==================== Temperature ====================

    public double convertTemperature(double value, TemperatureUnit source, TemperatureUnit target) {
        double c;
        switch (source) {
            case F: c = (value - 32.0) * 5.0 / 9.0; break;
            case K: c = value - 273.15; break;
            default: c = value; break;
        }
        switch (target) {
            case F: return round(c * 9.0 / 5.0 + 32.0);
            case K: return round(c + 273.15);
            default: return round(c);
        }
    }


}