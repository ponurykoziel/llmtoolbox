package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.common.DoubleValueCalculator;

public class FinancialCalculator extends DoubleValueCalculator {

    public FinancialCalculator(int roundingDigits) {
        super(roundingDigits);
    }

    public double increaseByPercent(double value, double percent) {
        double rv = round(value);
        double rp = round(percent);
        return round(rv * (1.0 + rp / 100.0));
    }

    public double decreaseByPercent(double value, double percent) {
        double rv = round(value);
        double rp = round(percent);
        return round(rv * (1.0 - rp / 100.0));
    }

    public double effectiveApy(double annualRate) {
        double ra = round(annualRate);
        double monthlyRate = ra / 100.0 / 12.0;
        return round((Math.pow(1.0 + monthlyRate, 12.0) - 1.0) * 100.0);
    }

    public double simpleInterest(double capital, double annualRate, int months) {
        double rc = round(capital);
        double ra = round(annualRate);
        return round(rc * (ra / 100.0) * (months / 12.0));
    }

    public double simpleTotal(double capital, double annualRate, int months) {
        double rc = round(capital);
        double ra = round(annualRate);
        double interest = rc * (ra / 100.0) * (months / 12.0);
        return round(rc + interest);
    }

    public double compoundInterest(double capital, double annualRate, int months) {
        double rc = round(capital);
        double ra = round(annualRate);
        double rate = ra / 100.0 / 12.0;
        double total = rc * Math.pow(1.0 + rate, months);
        return round(total - rc);
    }

    public double compoundTotal(double capital, double annualRate, int months) {
        double rc = round(capital);
        double ra = round(annualRate);
        double rate = ra / 100.0 / 12.0;
        double total = rc * Math.pow(1.0 + rate, months);
        return round(total);
    }

    public double loanMonthlyPayment(double principal, double annualRate, int months) {
        if (months <= 0) {
            return round(principal);
        }
        double rp = round(principal);
        double ra = round(annualRate);
        double r = ra / 100.0 / 12.0;
        if (r == 0.0) {
            return round(rp / months);
        }
        double factor = Math.pow(1.0 + r, months);
        return round(rp * (r * factor) / (factor - 1.0));
    }

    public double loanTotalPaid(double principal, double annualRate, int months) {
        if (months <= 0) {
            return round(principal);
        }
        double payment = loanMonthlyPayment(principal, annualRate, months);
        return round(payment * months);
    }

    public double loanTotalInterest(double principal, double annualRate, int months) {
        if (months <= 0) {
            return 0.0;
        }
        double payment = loanMonthlyPayment(principal, annualRate, months);
        return round(payment * months - round(principal));
    }
}
