package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.FinancialCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FinancialCalculatorTest {

    private FinancialCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new FinancialCalculator(8);
    }

    // ── increaseByPercent ──────────────────────────────────────────────

    @Test
    void increaseByPercent_normal() {
        assertEquals(110.0, calc.increaseByPercent(100.0, 10.0));
    }

    @Test
    void increaseByPercent_zeroValue() {
        assertEquals(0.0, calc.increaseByPercent(0.0, 50.0));
    }

    @Test
    void increaseByPercent_zeroPercent() {
        assertEquals(100.0, calc.increaseByPercent(100.0, 0.0));
    }

    @Test
    void increaseByPercent_negativePercent() {
        assertEquals(90.0, calc.increaseByPercent(100.0, -10.0));
    }

    @Test
    void increaseByPercent_100percent() {
        assertEquals(200.0, calc.increaseByPercent(100.0, 100.0));
    }

    @Test
    void increaseByPercent_fractionalResult() {
        // 33.33 * (1 + 3.33/100) = 33.33 * 1.0333 = 34.439889
        assertEquals(34.439889, calc.increaseByPercent(33.33, 3.33));
    }

    @Test
    void increaseByPercent_largeValue() {
        assertEquals(2_000_000.0, calc.increaseByPercent(1_000_000.0, 100.0));
    }

    // ── decreaseByPercent ──────────────────────────────────────────────

    @Test
    void decreaseByPercent_normal() {
        assertEquals(90.0, calc.decreaseByPercent(100.0, 10.0));
    }

    @Test
    void decreaseByPercent_zeroValue() {
        assertEquals(0.0, calc.decreaseByPercent(0.0, 50.0));
    }

    @Test
    void decreaseByPercent_zeroPercent() {
        assertEquals(100.0, calc.decreaseByPercent(100.0, 0.0));
    }

    @Test
    void decreaseByPercent_negativePercent() {
        assertEquals(110.0, calc.decreaseByPercent(100.0, -10.0));
    }

    @Test
    void decreaseByPercent_100percent() {
        assertEquals(0.0, calc.decreaseByPercent(100.0, 100.0));
    }

    @Test
    void decreaseByPercent_over100percent() {
        assertEquals(-100.0, calc.decreaseByPercent(100.0, 200.0));
    }

    @Test
    void decreaseByPercent_fractionalResult() {
        // 33.33 * (1 - 3.33/100) = 33.33 * 0.9667 = 32.220111
        assertEquals(32.220111, calc.decreaseByPercent(33.33, 3.33));
    }

    // ── effectiveApy ───────────────────────────────────────────────────

    @Test
    void effectiveApy_5percent() {
        // (1 + 0.05/12)^12 - 1 ≈ 0.0511618979 → *100 ≈ 5.11618979
        assertEquals(5.11618979, calc.effectiveApy(5.0), 1e-8);
    }

    @Test
    void effectiveApy_zero() {
        assertEquals(0.0, calc.effectiveApy(0.0));
    }

    @Test
    void effectiveApy_100percent() {
        // (1 + 1.0/12)^12 - 1 ≈ 1.6130352902 → *100 ≈ 161.30352902
        assertEquals(161.30352902, calc.effectiveApy(100.0), 1e-6);
    }

    @Test
    void effectiveApy_smallRate() {
        // 0.1% annual → very close to 0.1%
        double apy = calc.effectiveApy(0.1);
        assertTrue(apy > 0.1 && apy < 0.1005);
    }

    // ── simpleInterest ─────────────────────────────────────────────────

    @Test
    void simpleInterest_normal() {
        // 1000 * 0.12 * (12/12) = 120.0
        assertEquals(120.0, calc.simpleInterest(1000.0, 12.0, 12));
    }

    @Test
    void simpleInterest_zeroMonths() {
        assertEquals(0.0, calc.simpleInterest(1000.0, 12.0, 0));
    }

    @Test
    void simpleInterest_zeroRate() {
        assertEquals(0.0, calc.simpleInterest(1000.0, 0.0, 12));
    }

    @Test
    void simpleInterest_zeroCapital() {
        assertEquals(0.0, calc.simpleInterest(0.0, 12.0, 12));
    }

    @Test
    void simpleInterest_partialYear() {
        // 1000 * 0.12 * (6/12) = 60.0
        assertEquals(60.0, calc.simpleInterest(1000.0, 12.0, 6));
    }

    @Test
    void simpleInterest_oneMonth() {
        // 1000 * 0.12 * (1/12) = 10.0
        assertEquals(10.0, calc.simpleInterest(1000.0, 12.0, 1));
    }

    @Test
    void simpleInterest_oddMonths() {
        // 1000 * 0.12 * (5/12) = 50.0
        assertEquals(50.0, calc.simpleInterest(1000.0, 12.0, 5));
    }

    // ── simpleTotal ────────────────────────────────────────────────────

    @Test
    void simpleTotal_normal() {
        assertEquals(1120.0, calc.simpleTotal(1000.0, 12.0, 12));
    }

    @Test
    void simpleTotal_zeroMonths() {
        assertEquals(1000.0, calc.simpleTotal(1000.0, 12.0, 0));
    }

    @Test
    void simpleTotal_zeroRate() {
        assertEquals(1000.0, calc.simpleTotal(1000.0, 0.0, 12));
    }

    // ── compoundInterest ───────────────────────────────────────────────

    @Test
    void compoundInterest_normal() {
        // 1000 * (1.01)^12 = 1126.82503013..., interest = 126.82503013
        assertEquals(126.82503013, calc.compoundInterest(1000.0, 12.0, 12), 1e-8);
    }

    @Test
    void compoundInterest_zeroMonths() {
        assertEquals(0.0, calc.compoundInterest(1000.0, 12.0, 0));
    }

    @Test
    void compoundInterest_zeroRate() {
        assertEquals(0.0, calc.compoundInterest(1000.0, 0.0, 12));
    }

    @Test
    void compoundInterest_oneMonth() {
        // 1000 * 1.01 = 1010, interest = 10.0
        assertEquals(10.0, calc.compoundInterest(1000.0, 12.0, 1));
    }

    @Test
    void compoundInterest_twoMonths() {
        // 1000 * (1.01)^2 = 1020.1, interest = 20.1
        assertEquals(20.1, calc.compoundInterest(1000.0, 12.0, 2));
    }

    @Test
    void compoundInterest_highRate() {
        // 1000 @ 120% for 12 months: r = 0.1, (1.1)^12 ≈ 3.13842837672
        // total ≈ 3138.42837672, interest ≈ 2138.42837672
        assertEquals(2138.42837672, calc.compoundInterest(1000.0, 120.0, 12), 1e-8);
    }

    // ── compoundTotal ──────────────────────────────────────────────────

    @Test
    void compoundTotal_normal() {
        assertEquals(1126.82503013, calc.compoundTotal(1000.0, 12.0, 12), 1e-8);
    }

    @Test
    void compoundTotal_zeroMonths() {
        assertEquals(1000.0, calc.compoundTotal(1000.0, 12.0, 0));
    }

    @Test
    void compoundTotal_zeroRate() {
        assertEquals(1000.0, calc.compoundTotal(1000.0, 0.0, 12));
    }

    // ── loanMonthlyPayment ─────────────────────────────────────────────

    @Test
    void loanMonthlyPayment_normal() {
        // 100000 @ 6% for 360 months: standard mortgage payment
        // r = 0.005, (1.005)^360 ≈ 6.02257521226
        // payment = 100000 * 0.005 * 6.02257521226 / (6.02257521226 - 1)
        //        ≈ 599.55052515
        assertEquals(599.55052515, calc.loanMonthlyPayment(100000.0, 6.0, 360), 1e-8);
    }

    @Test
    void loanMonthlyPayment_zeroRate() {
        // 100000 / 360 ≈ 277.77777778
        assertEquals(277.77777778, calc.loanMonthlyPayment(100000.0, 0.0, 360), 1e-8);
    }

    @Test
    void loanMonthlyPayment_zeroMonths() {
        // months <= 0 → returns principal
        assertEquals(100000.0, calc.loanMonthlyPayment(100000.0, 6.0, 0));
    }

    @Test
    void loanMonthlyPayment_oneMonth() {
        // months=1 goes through normal amortization: r=0.005, factor=1.005
        // payment = 100000 * 0.005 * 1.005 / 0.005 = 100000 * 1.005 = 100500.0
        assertEquals(100500.0, calc.loanMonthlyPayment(100000.0, 6.0, 1));
    }

    @Test
    void loanMonthlyPayment_shortLoan() {
        // 10000 @ 12% for 12 months: r = 0.01, (1.01)^12 ≈ 1.12682503013
        // payment = 10000 * 0.01 * 1.12682503013 / (1.12682503013 - 1)
        //        ≈ 888.48788678
        assertEquals(888.48788678, calc.loanMonthlyPayment(10000.0, 12.0, 12), 1e-8);
    }

    @Test
    void loanMonthlyPayment_highRate() {
        // 1000 @ 120% for 12 months: r = 0.1, (1.1)^12 ≈ 3.13842837672
        // payment = 1000 * 0.1 * 3.13842837672 / (3.13842837672 - 1)
        //        ≈ 146.7633151
        assertEquals(146.7633151, calc.loanMonthlyPayment(1000.0, 120.0, 12), 1e-8);
    }

    // ── loanTotalPaid ──────────────────────────────────────────────────

    @Test
    void loanTotalPaid_normal() {
        // monthlyPayment * 360 ≈ 215838.189054
        assertEquals(215838.189054, calc.loanTotalPaid(100000.0, 6.0, 360), 1e-8);
    }

    @Test
    void loanTotalPaid_zeroMonths() {
        assertEquals(100000.0, calc.loanTotalPaid(100000.0, 6.0, 0));
    }

    @Test
    void loanTotalPaid_oneMonth() {
        // monthlyPayment(100000, 6%, 1) = 100500, * 1 = 100500
        assertEquals(100500.0, calc.loanTotalPaid(100000.0, 6.0, 1));
    }

    @Test
    void loanTotalPaid_zeroRate() {
        // 277.77777778 * 360 ≈ 100000.0000008 (floating point)
        assertEquals(100000.0, calc.loanTotalPaid(100000.0, 0.0, 360), 1e-3);
    }

    // ── loanTotalInterest ──────────────────────────────────────────────

    @Test
    void loanTotalInterest_normal() {
        // totalPaid - principal ≈ 115838.189054
        assertEquals(115838.189054, calc.loanTotalInterest(100000.0, 6.0, 360), 1e-8);
    }

    @Test
    void loanTotalInterest_zeroMonths() {
        assertEquals(0.0, calc.loanTotalInterest(100000.0, 6.0, 0));
    }

    @Test
    void loanTotalInterest_oneMonth() {
        // totalPaid(100500) - principal(100000) = 500.0
        assertEquals(500.0, calc.loanTotalInterest(100000.0, 6.0, 1));
    }

    @Test
    void loanTotalInterest_zeroRate() {
        // totalPaid ≈ 100000.0000008, interest ≈ 8.0E-7
        assertEquals(0.0, calc.loanTotalInterest(100000.0, 0.0, 360), 1e-3);
    }

    // ── edge cases ────────────────────────────────────────────────────

    @Test
    void effectiveApy_negativeRate() {
        // negative rate: (1 + (-0.05)/12)^12 - 1
        double apy = calc.effectiveApy(-5.0);
        assertTrue(apy < 0.0);
    }

    @Test
    void loanMonthlyPayment_negativeMonths() {
        // months <= 0 → returns principal
        assertEquals(100000.0, calc.loanMonthlyPayment(100000.0, 6.0, -1));
    }

    @Test
    void loanMonthlyPayment_largeMonthCount() {
        // 3600 months = 300 years, should still compute
        double result = calc.loanMonthlyPayment(100000.0, 6.0, 3600);
        assertTrue(result > 0.0);
        assertTrue(Double.isFinite(result));
    }

    @Test
    void compoundInterest_negativeRate() {
        // negative rate: capital shrinks
        double result = calc.compoundInterest(1000.0, -12.0, 12);
        assertTrue(result < 0.0);
    }

    // ── rounding precision ─────────────────────────────────────────────

    @Test
    void rounding_8digits() {
        // Verify that results are rounded to 8 decimal digits
        double result = calc.increaseByPercent(1.0 / 3.0, 0.0);
        // 1/3 ≈ 0.33333333 (rounded to 8 digits)
        assertEquals(0.33333333, result);
    }

    @Test
    void rounding_noExcessPrecision() {
        // 100 * 1.0001 = 100.01 exactly, should stay 100.01
        assertEquals(100.01, calc.increaseByPercent(100.0, 0.01));
    }
}
