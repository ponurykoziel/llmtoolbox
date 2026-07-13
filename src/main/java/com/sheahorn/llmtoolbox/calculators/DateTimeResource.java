package com.sheahorn.llmtoolbox.calculators;

import java.time.LocalDateTime;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/calculator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DateTimeResource {

    private final DateTimeCalculator calc;

    public DateTimeResource(
            @ConfigProperty(name = "mfop.calculator.rounding-digits", defaultValue = "8") int roundingDigits) {
        this.calc = new DateTimeCalculator(roundingDigits);
    }

    // ==================== Duration ====================

    @Operation(operationId = "calculator_duration_normalize",
            summary = "Normalize a duration into days/hours/minutes/seconds/millis (each field in its natural range)")
    @POST @Path("/duration-normalize")
    public DurationNormalizedResponse durationNormalize(DurationRequestDto req) {
        if (req == null) throw new IllegalArgumentException("Request is required");
        if (isAllNull(req)) throw new IllegalArgumentException("At least one duration field is required");
        return calc.durationNormalize(req);
    }

    @Operation(operationId = "calculator_duration_convert_to_unit",
            summary = "Convert a duration to totals in each unit: totalDays, totalHours, totalMinutes, totalSeconds, totalMillis")
    @POST @Path("/duration-convert-to-unit")
    public DurationTotalsResponse durationConvertToUnit(DurationRequestDto req) {
        if (req == null) throw new IllegalArgumentException("Request is required");
        if (isAllNull(req)) throw new IllegalArgumentException("At least one duration field is required");
        return calc.durationConvertToUnit(req);
    }

    // ==================== Date Range ====================

    @Operation(operationId = "calculator_date_range", summary = "Difference between two dates in days, weeks, months, or years")
    @POST @Path("/date-range")
    public AggregateResponse dateRange(DateRangeRequestDto req) {
        if (req == null || req.date1 == null || req.date2 == null || req.unit == null)
            throw new IllegalArgumentException("date1, date2, and unit are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "date_range";
        r.result = calc.dateRange(req.date1, req.date2, req.unit);
        return r;
    }

    @Operation(operationId = "calculator_time_range", summary = "Difference between two times in seconds, minutes, or hours")
    @POST @Path("/time-range")
    public AggregateResponse timeRange(TimeRangeRequestDto req) {
        if (req == null || req.time1 == null || req.time2 == null || req.unit == null)
            throw new IllegalArgumentException("time1, time2, and unit are required");
        AggregateResponse r = new AggregateResponse();
        r.operation = "time_range";
        r.result = calc.timeRange(req.time1, req.time2, req.unit);
        return r;
    }

    // ==================== Shift ====================

    @Operation(operationId = "calculator_date_shift", summary = "Add or subtract days/weeks/months/years from a date")
    @POST @Path("/date-shift")
    public TextResponse dateShift(DateShiftRequestDto req) {
        if (req == null || req.date == null || req.amount == null || req.unit == null)
            throw new IllegalArgumentException("date, amount, and unit are required");
        TextResponse r = new TextResponse();
        r.operation = "date_shift";
        r.result = calc.dateShift(req.date, req.amount, req.unit);
        return r;
    }

    @Operation(operationId = "calculator_time_shift", summary = "Add or subtract seconds/minutes/hours from a time")
    @POST @Path("/time-shift")
    public TextResponse timeShift(TimeShiftRequestDto req) {
        if (req == null || req.time == null || req.amount == null || req.unit == null)
            throw new IllegalArgumentException("time, amount, and unit are required");
        TextResponse r = new TextResponse();
        r.operation = "time_shift";
        r.result = calc.timeShift(req.time, req.amount, req.unit);
        return r;
    }

    @Operation(operationId = "calculator_datetime_shift", summary = "Add or subtract seconds/minutes/hours/days/weeks/months/years from a datetime")
    @POST @Path("/datetime-shift")
    public TextResponse dateTimeShift(DateTimeShiftRequestDto req) {
        if (req == null || req.datetime == null || req.amount == null || req.unit == null)
            throw new IllegalArgumentException("datetime, amount, and unit are required");
        TextResponse r = new TextResponse();
        r.operation = "datetime_shift";
        r.result = calc.dateTimeShift(req.datetime, req.amount, req.unit);
        return r;
    }

    // ==================== Timezone ====================

    @Operation(operationId = "calculator_timezone_convert", summary = "Convert datetime between timezones")
    @POST @Path("/timezone-convert")
    public TextResponse timezoneConvert(TimezoneConvertRequestDto req) {
        if (req == null || req.datetime == null || req.fromZone == null || req.toZone == null)
            throw new IllegalArgumentException("datetime, fromZone, and toZone are required");
        TextResponse r = new TextResponse();
        r.operation = "timezone_convert";
        r.result = calc.timezoneConvert(req.datetime, req.fromZone, req.toZone);
        return r;
    }

    // ==================== Timestamp ====================

    @Operation(operationId = "calculator_timestamp_convert", summary = "Convert between Unix timestamp and ISO datetime")
    @POST @Path("/timestamp-convert")
    public TextResponse timestampConvert(TimestampRequestDto req) {
        if (req == null) throw new IllegalArgumentException("Request is required");
        TextResponse r = new TextResponse();
        r.operation = "timestamp_convert";
        if (req.timestamp != null) {
            r.result = calc.timestampToDatetime(req.timestamp);
        } else if (req.format != null) {
            r.result = calc.datetimeToTimestamp(req.format);
        } else {
            throw new IllegalArgumentException("Provide either 'timestamp' (to convert to datetime) or 'format' (to convert to timestamp)");
        }
        return r;
    }

    @Operation(operationId = "calculator_timestamp_get_current", summary = "Get the current Unix timestamp in seconds")
    @POST @Path("/timestamp-get-current")
    public TextResponse timestampGetCurrent() {
        TextResponse r = new TextResponse();
        r.operation = "timestamp_get_current";
        r.result = String.valueOf(calc.getCurrentTimestamp());
        return r;
    }

    @Operation(operationId = "calculator_date_get_current", summary = "Get the current date in ISO format (yyyy-MM-dd)")
    @POST @Path("/date-get-current")
    public TextResponse dateGetCurrent() {
        TextResponse r = new TextResponse();
        r.operation = "date_get_current";
        r.result = calc.getCurrentDate();
        return r;
    }

    // ==================== Date Info ====================

    @Operation(operationId = "calculator_date_info", summary = "Get info about a date: weekday, week number, leap year, days in month, day of year")
    @POST @Path("/date-info")
    public TextResponse dateInfo(DateInfoRequestDto req) {
        if (req == null || req.date == null || req.date.isBlank())
            throw new IllegalArgumentException("date is required");
        TextResponse r = new TextResponse();
        r.operation = "date_info";
        r.result = calc.dateInfo(req.date);
        return r;
    }

    @Operation(operationId = "calculator_business_days_add", summary = "Add N business days (Mon-Fri) to a date")
    @POST @Path("/business-days-add")
    public TextResponse businessDaysAdd(BusinessDaysRequestDto req) {
        if (req == null || req.startDate == null || req.startDate.isBlank() || req.days == null)
            throw new IllegalArgumentException("startDate and days are required");
        TextResponse r = new TextResponse();
        r.operation = "business_days_add";
        r.result = calc.businessDaysAdd(req.startDate, req.days);
        return r;
    }

    @Operation(operationId = "calculator_date_format", summary = "Format a date with a custom pattern (e.g. dd/MM/yyyy)")
    @POST @Path("/date-format")
    public TextResponse dateFormat(FormatDateRequestDto req) {
        if (req == null || req.date == null || req.date.isBlank() || req.pattern == null || req.pattern.isBlank())
            throw new IllegalArgumentException("date and pattern are required");
        TextResponse r = new TextResponse();
        r.operation = "date_format";
        r.result = calc.dateFormat(req.date, req.pattern);
        return r;
    }

    // ==================== Date Diff ====================

    @Operation(operationId = "calculator_date_diff_normalize",
            summary = "Difference between two date-times, normalized into years/months/days/hours/minutes/seconds")
    @POST @Path("/date-diff-normalize")
    public DateDiffNormalizedResponse dateDiffNormalize(DateDiffRequestDto req) {
        if (req == null || req.from == null || req.to == null)
            throw new IllegalArgumentException("from and to are required");
        LocalDateTime from = toLocalDateTime(req.from);
        LocalDateTime to = toLocalDateTime(req.to);
        return calc.dateDiffNormalize(from, to);
    }

    @Operation(operationId = "calculator_date_diff_convert_to_unit",
            summary = "Difference between two date-times, expressed as totals in each unit: totalYears, totalMonths, totalDays, totalHours, totalMinutes, totalSeconds")
    @POST @Path("/date-diff-convert-to-unit")
    public DateDiffTotalsResponse dateDiffConvertToUnit(DateDiffRequestDto req) {
        if (req == null || req.from == null || req.to == null)
            throw new IllegalArgumentException("from and to are required");
        LocalDateTime from = toLocalDateTime(req.from);
        LocalDateTime to = toLocalDateTime(req.to);
        return calc.dateDiffConvertToUnit(from, to);
    }

    // ==================== helpers ====================

    private boolean isAllNull(DurationRequestDto req) {
        return req.days == null && req.hours == null && req.minutes == null
                && req.seconds == null && req.millis == null;
    }

    private LocalDateTime toLocalDateTime(DateTimeComponentDto dto) {
        if (dto.year == null || dto.month == null || dto.day == null)
            throw new IllegalArgumentException("year, month, and day are required");
        int hour = dto.hour != null ? dto.hour : 0;
        int minute = dto.minute != null ? dto.minute : 0;
        int second = dto.second != null ? dto.second : 0;
        return LocalDateTime.of(dto.year, dto.month, dto.day, hour, minute, second);
    }
}
