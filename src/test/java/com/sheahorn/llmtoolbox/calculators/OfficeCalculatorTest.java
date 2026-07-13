package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfficeCalculatorTest {

    private OfficeCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new OfficeCalculator(8);
    }

    // ── convertLength ──────────────────────────────────────────────────

    @Test
    void convertLength_MtoKM() {
        assertEquals(1.0, calc.convertLength(1000, LengthUnit.M, LengthUnit.KM));
    }

    @Test
    void convertLength_INtoCM() {
        assertEquals(2.54, calc.convertLength(1, LengthUnit.IN, LengthUnit.CM));
    }

    @Test
    void convertLength_MItoKM() {
        assertEquals(1.609344, calc.convertLength(1, LengthUnit.MI, LengthUnit.KM));
    }

    @Test
    void convertLength_sameUnit() {
        assertEquals(5.0, calc.convertLength(5, LengthUnit.M, LengthUnit.M));
    }

    @Test
    void convertLength_zeroValue() {
        assertEquals(0.0, calc.convertLength(0, LengthUnit.M, LengthUnit.KM));
    }

    @Test
    void convertLength_NMtoM() {
        // 1 nm = 1e-9 m, but 1e-9 rounds to 0 with 8 digits. Use 1000 nm = 1e-6 m.
        assertEquals(0.000001, calc.convertLength(1000, LengthUnit.NM, LengthUnit.M));
    }

    @Test
    void convertLength_FTtoM() {
        assertEquals(0.3048, calc.convertLength(1, LengthUnit.FT, LengthUnit.M));
    }

    @Test
    void convertLength_YDtoFT() {
        assertEquals(3.0, calc.convertLength(1, LengthUnit.YD, LengthUnit.FT));
    }

    // ── convertMass ────────────────────────────────────────────────────

    @Test
    void convertMass_KGtoLB() {
        // 1 kg = 1000g / 453.59237 ≈ 2.20462262
        assertEquals(2.20462262, calc.convertMass(1, MassUnit.KG, MassUnit.LB), 1e-8);
    }

    @Test
    void convertMass_OZtoG() {
        assertEquals(28.34952313, calc.convertMass(1, MassUnit.OZ, MassUnit.G), 1e-8);
    }

    @Test
    void convertMass_TtoKG() {
        assertEquals(1000.0, calc.convertMass(1, MassUnit.T, MassUnit.KG));
    }

    @Test
    void convertMass_sameUnit() {
        assertEquals(5.0, calc.convertMass(5, MassUnit.G, MassUnit.G));
    }

    @Test
    void convertMass_zeroValue() {
        assertEquals(0.0, calc.convertMass(0, MassUnit.KG, MassUnit.LB));
    }

    @Test
    void convertMass_MGtoG() {
        assertEquals(0.001, calc.convertMass(1, MassUnit.MG, MassUnit.G));
    }

    @Test
    void convertMass_LBtoOZ() {
        assertEquals(16.0, calc.convertMass(1, MassUnit.LB, MassUnit.OZ), 1e-8);
    }

    // ── convertVolume ──────────────────────────────────────────────────

    @Test
    void convertVolume_LtoGAL() {
        // 1 / 3.785411784 ≈ 0.26417205
        assertEquals(0.26417205, calc.convertVolume(1, VolumeUnit.L, VolumeUnit.GAL), 1e-8);
    }

    @Test
    void convertVolume_MLtoFLOZ() {
        // 0.001 / 0.0295735295625 ≈ 0.03381402
        assertEquals(0.03381402, calc.convertVolume(1, VolumeUnit.ML, VolumeUnit.FLOZ), 1e-8);
    }

    @Test
    void convertVolume_M3toL() {
        assertEquals(1000.0, calc.convertVolume(1, VolumeUnit.M3, VolumeUnit.L));
    }

    @Test
    void convertVolume_sameUnit() {
        assertEquals(5.0, calc.convertVolume(5, VolumeUnit.L, VolumeUnit.L));
    }

    @Test
    void convertVolume_zeroValue() {
        assertEquals(0.0, calc.convertVolume(0, VolumeUnit.L, VolumeUnit.GAL));
    }

    @Test
    void convertVolume_GALtoQT() {
        assertEquals(4.0, calc.convertVolume(1, VolumeUnit.GAL, VolumeUnit.QT), 1e-8);
    }

    @Test
    void convertVolume_CUPtoML() {
        // 0.2365882365 / 0.001 = 236.5882365
        assertEquals(236.5882365, calc.convertVolume(1, VolumeUnit.CUP, VolumeUnit.ML));
    }

    // ── convertArea ────────────────────────────────────────────────────

    @Test
    void convertArea_M2toACRE() {
        // 1 / 4046.8564224 ≈ 0.00024711
        assertEquals(0.00024711, calc.convertArea(1, AreaUnit.M2, AreaUnit.ACRE), 1e-8);
    }

    @Test
    void convertArea_HAtoFT2() {
        // 10000 / 0.09290304 ≈ 107639.1041671
        assertEquals(107639.1041671, calc.convertArea(1, AreaUnit.HA, AreaUnit.FT2), 1e-8);
    }

    @Test
    void convertArea_KM2toM2() {
        assertEquals(1_000_000.0, calc.convertArea(1, AreaUnit.KM2, AreaUnit.M2));
    }

    @Test
    void convertArea_sameUnit() {
        assertEquals(5.0, calc.convertArea(5, AreaUnit.M2, AreaUnit.M2));
    }

    @Test
    void convertArea_zeroValue() {
        assertEquals(0.0, calc.convertArea(0, AreaUnit.M2, AreaUnit.ACRE));
    }

    @Test
    void convertArea_ACREtoHA() {
        // 4046.8564224 / 10000 ≈ 0.40468564
        assertEquals(0.40468564, calc.convertArea(1, AreaUnit.ACRE, AreaUnit.HA), 1e-8);
    }

    // ── convertSpeed ───────────────────────────────────────────────────

    @Test
    void convertSpeed_MStoKMH() {
        // 1 / 0.2777777777777778 = 3.6
        assertEquals(3.6, calc.convertSpeed(1, SpeedUnit.MS, SpeedUnit.KMH), 1e-8);
    }

    @Test
    void convertSpeed_MPHtoKNOT() {
        // 0.44704 / 0.5144444444444445 ≈ 0.86897624
        assertEquals(0.86897624, calc.convertSpeed(1, SpeedUnit.MPH, SpeedUnit.KNOT), 1e-8);
    }

    @Test
    void convertSpeed_KMHtoMS() {
        // 0.2777777777777778 / 1.0 ≈ 0.27777778
        assertEquals(0.27777778, calc.convertSpeed(1, SpeedUnit.KMH, SpeedUnit.MS), 1e-8);
    }

    @Test
    void convertSpeed_sameUnit() {
        assertEquals(5.0, calc.convertSpeed(5, SpeedUnit.MS, SpeedUnit.MS));
    }

    @Test
    void convertSpeed_zeroValue() {
        assertEquals(0.0, calc.convertSpeed(0, SpeedUnit.MS, SpeedUnit.KMH));
    }

    @Test
    void convertSpeed_KNOTtoMPH() {
        // 0.5144444444444445 / 0.44704 ≈ 1.15077945
        assertEquals(1.15077945, calc.convertSpeed(1, SpeedUnit.KNOT, SpeedUnit.MPH), 1e-8);
    }

    // ── convertPressure ────────────────────────────────────────────────

    @Test
    void convertPressure_PAtoBAR() {
        assertEquals(1.0, calc.convertPressure(100000, PressureUnit.PA, PressureUnit.BAR));
    }

    @Test
    void convertPressure_ATMtoPSI() {
        // 101325 / 6894.757293168361 ≈ 14.69594878
        assertEquals(14.69594878, calc.convertPressure(1, PressureUnit.ATM, PressureUnit.PSI), 1e-8);
    }

    @Test
    void convertPressure_KPAtoPA() {
        assertEquals(1000.0, calc.convertPressure(1, PressureUnit.KPA, PressureUnit.PA));
    }

    @Test
    void convertPressure_sameUnit() {
        assertEquals(5.0, calc.convertPressure(5, PressureUnit.PA, PressureUnit.PA));
    }

    @Test
    void convertPressure_zeroValue() {
        assertEquals(0.0, calc.convertPressure(0, PressureUnit.PA, PressureUnit.BAR));
    }

    @Test
    void convertPressure_PSItoATM() {
        // 6894.757293168361 / 101325 ≈ 0.06804596
        assertEquals(0.06804596, calc.convertPressure(1, PressureUnit.PSI, PressureUnit.ATM), 1e-8);
    }

    // ── convertEnergy ──────────────────────────────────────────────────

    @Test
    void convertEnergy_JtoCAL() {
        // 1 / 4.184 ≈ 0.23900574
        assertEquals(0.23900574, calc.convertEnergy(1, EnergyUnit.J, EnergyUnit.CAL), 1e-8);
    }

    @Test
    void convertEnergy_KWHtoJ() {
        assertEquals(3_600_000.0, calc.convertEnergy(1, EnergyUnit.KWH, EnergyUnit.J));
    }

    @Test
    void convertEnergy_KCALtoJ() {
        assertEquals(4184.0, calc.convertEnergy(1, EnergyUnit.KCAL, EnergyUnit.J));
    }

    @Test
    void convertEnergy_sameUnit() {
        assertEquals(5.0, calc.convertEnergy(5, EnergyUnit.J, EnergyUnit.J));
    }

    @Test
    void convertEnergy_zeroValue() {
        assertEquals(0.0, calc.convertEnergy(0, EnergyUnit.J, EnergyUnit.CAL));
    }

    @Test
    void convertEnergy_WHtoKJ() {
        // 3600 / 1000 = 3.6
        assertEquals(3.6, calc.convertEnergy(1, EnergyUnit.WH, EnergyUnit.KJ));
    }

    @Test
    void convertEnergy_CALtoKCAL() {
        // 4.184 / 4184 = 0.001
        assertEquals(0.001, calc.convertEnergy(1, EnergyUnit.CAL, EnergyUnit.KCAL));
    }

    // ── convertData ────────────────────────────────────────────────────

    @Test
    void convertData_MBtoMIB() {
        // 1e6 / 1048576 ≈ 0.95367432
        assertEquals(0.95367432, calc.convertData(1, DataUnit.MB, DataUnit.MIB), 1e-8);
    }

    @Test
    void convertData_GBtoB() {
        assertEquals(1_000_000_000.0, calc.convertData(1, DataUnit.GB, DataUnit.B));
    }

    @Test
    void convertData_KIBtoB() {
        assertEquals(1024.0, calc.convertData(1, DataUnit.KIB, DataUnit.B));
    }

    @Test
    void convertData_TBtoGB() {
        assertEquals(1000.0, calc.convertData(1, DataUnit.TB, DataUnit.GB));
    }

    @Test
    void convertData_GIBtoMIB() {
        assertEquals(1024.0, calc.convertData(1, DataUnit.GIB, DataUnit.MIB));
    }

    @Test
    void convertData_sameUnit() {
        assertEquals(5.0, calc.convertData(5, DataUnit.B, DataUnit.B));
    }

    @Test
    void convertData_zeroValue() {
        assertEquals(0.0, calc.convertData(0, DataUnit.B, DataUnit.KB));
    }

    @Test
    void convertData_TIBtoGIB() {
        assertEquals(1024.0, calc.convertData(1, DataUnit.TIB, DataUnit.GIB));
    }

    @Test
    void convertData_KBtoKIB() {
        // 1000 / 1024 ≈ 0.9765625
        assertEquals(0.9765625, calc.convertData(1, DataUnit.KB, DataUnit.KIB));
    }

    // ── convertTemperature ─────────────────────────────────────────────

    @Test
    void convertTemperature_CtoF_freezing() {
        assertEquals(32.0, calc.convertTemperature(0, TemperatureUnit.C, TemperatureUnit.F));
    }

    @Test
    void convertTemperature_CtoF_boiling() {
        assertEquals(212.0, calc.convertTemperature(100, TemperatureUnit.C, TemperatureUnit.F));
    }

    @Test
    void convertTemperature_FtoC_freezing() {
        assertEquals(0.0, calc.convertTemperature(32, TemperatureUnit.F, TemperatureUnit.C));
    }

    @Test
    void convertTemperature_FtoC_boiling() {
        assertEquals(100.0, calc.convertTemperature(212, TemperatureUnit.F, TemperatureUnit.C));
    }

    @Test
    void convertTemperature_KtoC_absoluteZero() {
        assertEquals(-273.15, calc.convertTemperature(0, TemperatureUnit.K, TemperatureUnit.C));
    }

    @Test
    void convertTemperature_KtoC_freezing() {
        assertEquals(0.0, calc.convertTemperature(273.15, TemperatureUnit.K, TemperatureUnit.C));
    }

    @Test
    void convertTemperature_CtoK_freezing() {
        assertEquals(273.15, calc.convertTemperature(0, TemperatureUnit.C, TemperatureUnit.K));
    }

    @Test
    void convertTemperature_CtoK_boiling() {
        assertEquals(373.15, calc.convertTemperature(100, TemperatureUnit.C, TemperatureUnit.K));
    }

    @Test
    void convertTemperature_FtoK_freezing() {
        assertEquals(273.15, calc.convertTemperature(32, TemperatureUnit.F, TemperatureUnit.K));
    }

    @Test
    void convertTemperature_KtoF_freezing() {
        assertEquals(32.0, calc.convertTemperature(273.15, TemperatureUnit.K, TemperatureUnit.F));
    }

    @Test
    void convertTemperature_sameUnit_C() {
        assertEquals(25.0, calc.convertTemperature(25, TemperatureUnit.C, TemperatureUnit.C));
    }

    @Test
    void convertTemperature_sameUnit_F() {
        assertEquals(100.0, calc.convertTemperature(100, TemperatureUnit.F, TemperatureUnit.F));
    }

    @Test
    void convertTemperature_sameUnit_K() {
        assertEquals(300.0, calc.convertTemperature(300, TemperatureUnit.K, TemperatureUnit.K));
    }

    @Test
    void convertTemperature_negativeCrossover() {
        // -40°C = -40°F (the famous crossover point)
        assertEquals(-40.0, calc.convertTemperature(-40, TemperatureUnit.C, TemperatureUnit.F));
        assertEquals(-40.0, calc.convertTemperature(-40, TemperatureUnit.F, TemperatureUnit.C));
    }

    @Test
    void convertTemperature_bodyTemp() {
        // 37°C ≈ 98.6°F
        assertEquals(98.6, calc.convertTemperature(37, TemperatureUnit.C, TemperatureUnit.F), 1e-8);
    }

    @Test
    void convertTemperature_KtoF_absoluteZero() {
        // 0K = -459.67°F
        assertEquals(-459.67, calc.convertTemperature(0, TemperatureUnit.K, TemperatureUnit.F), 1e-8);
    }

    // ── negative values ─────────────────────────────────────────────────

    @Test
    void convertLength_negative() {
        assertEquals(-1.0, calc.convertLength(-1000, LengthUnit.M, LengthUnit.KM));
    }

    @Test
    void convertMass_negative() {
        assertEquals(-2.20462262, calc.convertMass(-1, MassUnit.KG, MassUnit.LB), 1e-8);
    }

    @Test
    void convertVolume_negative() {
        assertEquals(-0.26417205, calc.convertVolume(-1, VolumeUnit.L, VolumeUnit.GAL), 1e-8);
    }

    @Test
    void convertArea_negative() {
        assertEquals(-0.00024711, calc.convertArea(-1, AreaUnit.M2, AreaUnit.ACRE), 1e-8);
    }

    @Test
    void convertSpeed_negative() {
        assertEquals(-3.6, calc.convertSpeed(-1, SpeedUnit.MS, SpeedUnit.KMH), 1e-8);
    }

    @Test
    void convertPressure_negative() {
        assertEquals(-1.0, calc.convertPressure(-100000, PressureUnit.PA, PressureUnit.BAR));
    }

    @Test
    void convertEnergy_negative() {
        assertEquals(-0.23900574, calc.convertEnergy(-1, EnergyUnit.J, EnergyUnit.CAL), 1e-8);
    }

    @Test
    void convertData_negative() {
        assertEquals(-0.95367432, calc.convertData(-1, DataUnit.MB, DataUnit.MIB), 1e-8);
    }

    // ── extreme temperatures ────────────────────────────────────────────

    @Test
    void convertTemperature_absoluteZero_CtoK() {
        assertEquals(0.0, calc.convertTemperature(-273.15, TemperatureUnit.C, TemperatureUnit.K));
    }

    @Test
    void convertTemperature_absoluteZero_KtoC() {
        assertEquals(-273.15, calc.convertTemperature(0, TemperatureUnit.K, TemperatureUnit.C));
    }

    @Test
    void convertTemperature_sunSurface_CtoF() {
        // ~5505°C → ~9941°F
        double result = calc.convertTemperature(5505, TemperatureUnit.C, TemperatureUnit.F);
        assertTrue(result > 9900.0 && result < 10000.0);
    }

    @Test
    void convertTemperature_sunSurface_CtoK() {
        // ~5505°C → ~5778.15K
        assertEquals(5778.15, calc.convertTemperature(5505, TemperatureUnit.C, TemperatureUnit.K));
    }

    @Test
    void convertTemperature_negativeLarge_CtoF() {
        // -200°C → -328°F
        assertEquals(-328.0, calc.convertTemperature(-200, TemperatureUnit.C, TemperatureUnit.F));
    }

    @Test
    void convertTemperature_negativeLarge_FtoC() {
        // -328°F → -200°C
        assertEquals(-200.0, calc.convertTemperature(-328, TemperatureUnit.F, TemperatureUnit.C));
    }

    @Test
    void convertTemperature_negativeLarge_KtoC() {
        // 0K = -273.15°C, but negative K is physically impossible; still test the math
        // Actually K can't be negative in the enum, but let's test extreme low K
        assertEquals(-272.15, calc.convertTemperature(1, TemperatureUnit.K, TemperatureUnit.C));
    }

    @Test
    void convertTemperature_veryHigh_FtoK() {
        // 9941°F → ~5778.15K (sun surface)
        double result = calc.convertTemperature(9941, TemperatureUnit.F, TemperatureUnit.K);
        assertEquals(5778.15, result, 1e-1); // rounding tolerance
    }

    // ── rounding precision ─────────────────────────────────────────────

    @Test
    void rounding_8digits() {
        double result = calc.convertLength(1.0 / 3.0, LengthUnit.M, LengthUnit.M);
        assertEquals(0.33333333, result);
    }
}
