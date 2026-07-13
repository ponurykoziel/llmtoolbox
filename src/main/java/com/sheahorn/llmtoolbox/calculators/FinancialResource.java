package com.sheahorn.llmtoolbox.calculators;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FinancialResource {

    private final FinancialCalculator calc;

    public FinancialResource(
            @ConfigProperty(name = "mfop.calculator.rounding-digits", defaultValue = "8") int roundingDigits) {
        this.calc = new FinancialCalculator(roundingDigits);
    }

    @Operation(operationId = "calculator_increase_by_percent", summary = "Increase a value by a given percentage")
    @POST @Path("/increase-by-percent")
    public AggregateResponse increaseByPercent(PercentChangeRequestDto req) {
        if (req == null || req.value == null || req.percent == null)
            throw new IllegalArgumentException("value and percent are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "increase_by_percent";
        r.result = calc.increaseByPercent(req.value, req.percent);
        return r;
    }

    @Operation(operationId = "calculator_decrease_by_percent", summary = "Decrease a value by a given percentage")
    @POST @Path("/decrease-by-percent")
    public AggregateResponse decreaseByPercent(PercentChangeRequestDto req) {
        if (req == null || req.value == null || req.percent == null)
            throw new IllegalArgumentException("value and percent are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "decrease_by_percent";
        r.result = calc.decreaseByPercent(req.value, req.percent);
        return r;
    }

    @Operation(operationId = "calculator_interest", summary = "Calculate simple and compound interest for capital at nominal annual rate over months (monthly compounding). Returns effective APY.")
    @POST @Path("/interest")
    public InterestResponse interest(InterestRequestDto req) {
        if (req == null || req.capital == null || req.annualRate == null || req.months == null)
            throw new IllegalArgumentException("capital, annualRate, and months are required");
        InterestResponse r = new InterestResponse();
        r.operation = "interest";
        r.simpleInterest = calc.simpleInterest(req.capital, req.annualRate, req.months);
        r.compoundInterest = calc.compoundInterest(req.capital, req.annualRate, req.months);
        r.simpleTotal = calc.simpleTotal(req.capital, req.annualRate, req.months);
        r.compoundTotal = calc.compoundTotal(req.capital, req.annualRate, req.months);
        r.apy = calc.effectiveApy(req.annualRate);
        return r;
    }

    @Operation(operationId = "calculator_loan", summary = "Calculate amortizing loan: monthly payment, total paid, total interest, and effective APY for principal at nominal annual rate over months")
    @POST @Path("/loan")
    public LoanResponse loan(LoanRequestDto req) {
        if (req == null || req.principal == null || req.annualRate == null || req.months == null)
            throw new IllegalArgumentException("principal, annualRate, and months are required");
        LoanResponse r = new LoanResponse();
        r.operation = "loan";
        r.monthlyPayment = calc.loanMonthlyPayment(req.principal, req.annualRate, req.months);
        r.totalPaid = calc.loanTotalPaid(req.principal, req.annualRate, req.months);
        r.totalInterest = calc.loanTotalInterest(req.principal, req.annualRate, req.months);
        r.apy = calc.effectiveApy(req.annualRate);
        return r;
    }
}
