# ADR-0006: Calculator Architecture — Marker Interface + Rounding Strategy

**Date:** 2025-01-01 (inferred)
**Status:** Accepted

## Context

llmtoolbox includes a large collection of "calculator" endpoints that perform deterministic computations (arithmetic, unit conversion, color space conversion, network subnetting, statistics, financial math, date/time, regex, cryptography, etc.). These are designed to relieve LLMs of hallucination-prone calculations. The calculator package contains ~100+ DTOs and ~20+ calculator/resource classes.

## Decision

### Architecture

- A **marker interface** `Calculator` (`calculators/common/Calculator.java`) provides no contract — it exists purely for IDE discoverability ("Find Usages" to locate all calculators)
- An abstract **`DoubleValueCalculator`** provides a shared `round(double)` method with configurable precision
- Each calculator domain has:
  - A **calculator class** (e.g., `BinaryCalculator`, `ColorCalculator`, `NetworkCalculator`) implementing the pure logic
  - A **resource class** (e.g., `BinaryOperatorsResource`, `ColorResource`, `NetworkResource`) handling HTTP concerns
  - **DTO classes** for request/response serialization

### Rounding Strategy

- All calculator results are rounded to a configurable number of decimal digits (`llmtoolbox.calculator.rounding-digits`, default 8)
- Rounding is applied via `Math.round(value * 10^n) / 10^n`
- Special cases: `NaN` and `Infinity` pass through unmodified; values exceeding `Long.MAX_VALUE / factor` are returned as-is to avoid overflow
- Setting `rounding-digits=-1` disables rounding entirely

## Rationale

- **Marker interface** — the calculator package is large and diverse; a common marker makes it navigable without imposing a rigid contract that wouldn't fit all domains.
- **Shared rounding** — consistent precision across all calculators prevents floating-point noise (e.g., `0.1 + 0.2 = 0.30000000000000004`) from confusing LLMs.
- **8-digit default** — IEEE 754 doubles have ~15.9 significant decimal digits; 8 is a safe middle ground that preserves meaningful precision while hiding noise.
- **Separation of concerns** — calculator classes are pure logic (testable without HTTP), resource classes handle JAX-RS annotations and request validation.

## Consequences

- The marker interface is purely conventional — there's no compile-time enforcement that all calculators implement it.
- Rounding is applied uniformly; some domains (cryptography, exact integer arithmetic) may not benefit from it, but the `-1` escape hatch exists.
- The overflow guard in `DoubleValueCalculator.round()` means very large values are returned unrounded — acceptable because beyond ~2^53, double precision drops below 1.0 anyway.
