package com.sheahorn.llmtoolbox.calculators;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;

import com.sheahorn.llmtoolbox.calculators.common.DoubleValueCalculator;

public class DateTimeCalculator extends DoubleValueCalculator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ISO_LOCAL_TIME;
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public DateTimeCalculator(int roundingDigits) {
        super(roundingDigits);
    }

    // ==================== Duration ====================

    /**
     * Convert a duration request (days, hours, minutes, seconds, millis) into total milliseconds.
     */
    public long toTotalMillis(DurationRequestDto req) {
        long total = 0;
        if (req.days != null) total += req.days * 86_400_000L;
        if (req.hours != null) total += req.hours * 3_600_000L;
        if (req.minutes != null) total += req.minutes * 60_000L;
        if (req.seconds != null) total += req.seconds * 1_000L;
        if (req.millis != null) total += req.millis;
        return total;
    }

    /**
     * Normalize the total duration into days/hours/minutes/seconds/millis
     * (each field within its natural range: hours 0-23, minutes 0-59, etc.).
     */
    public DurationNormalizedResponse durationNormalize(DurationRequestDto req) {
        long total = toTotalMillis(req);
        boolean negative = total < 0;
        long absTotal = Math.abs(total);

        DurationNormalizedResponse r = new DurationNormalizedResponse();
        r.operation = "duration_normalize";

        long days = absTotal / 86_400_000L;
        absTotal %= 86_400_000L;
        long hours = absTotal / 3_600_000L;
        absTotal %= 3_600_000L;
        long minutes = absTotal / 60_000L;
        absTotal %= 60_000L;
        long seconds = absTotal / 1_000L;
        long millis = absTotal % 1_000L;

        if (negative) {
            days = -days;
            hours = -hours;
            minutes = -minutes;
            seconds = -seconds;
            millis = -millis;
        }

        r.days = days;
        r.hours = hours;
        r.minutes = minutes;
        r.seconds = seconds;
        r.millis = millis;
        return r;
    }

    /**
     * Convert the total duration to each unit as a total (e.g. totalHours = all time expressed in hours).
     */
    public DurationTotalsResponse durationConvertToUnit(DurationRequestDto req) {
        long totalMs = toTotalMillis(req);

        DurationTotalsResponse r = new DurationTotalsResponse();
        r.operation = "duration_convert_to_unit";
        r.totalDays = round(totalMs / 86_400_000.0);
        r.totalHours = round(totalMs / 3_600_000.0);
        r.totalMinutes = round(totalMs / 60_000.0);
        r.totalSeconds = round(totalMs / 1_000.0);
        r.totalMillis = totalMs;
        return r;
    }

    // ==================== Date Range ====================

    public double dateRange(String date1, String date2, String unit) {
        LocalDate d1 = LocalDate.parse(date1, DATE_FMT);
        LocalDate d2 = LocalDate.parse(date2, DATE_FMT);

        switch (unit.toLowerCase()) {
            case "days":
                return round(ChronoUnit.DAYS.between(d1, d2));
            case "weeks":
                return round(ChronoUnit.WEEKS.between(d1, d2));
            case "months":
                return round(ChronoUnit.MONTHS.between(d1, d2));
            case "years":
                return round(ChronoUnit.YEARS.between(d1, d2));
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit + ". Use: days, weeks, months, years");
        }
    }

    public double timeRange(String time1, String time2, String unit) {
        LocalTime t1 = LocalTime.parse(time1, TIME_FMT);
        LocalTime t2 = LocalTime.parse(time2, TIME_FMT);
        long seconds = ChronoUnit.SECONDS.between(t1, t2);

        switch (unit.toLowerCase()) {
            case "seconds":
                return round(seconds);
            case "minutes":
                return round(seconds / 60.0);
            case "hours":
                return round(seconds / 3600.0);
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit + ". Use: seconds, minutes, hours");
        }
    }

    // ==================== Shift ====================

    public String dateShift(String date, long amount, String unit) {
        LocalDate d = LocalDate.parse(date, DATE_FMT);
        LocalDate result;

        switch (unit.toLowerCase()) {
            case "days":
                result = d.plusDays(amount);
                break;
            case "weeks":
                result = d.plusWeeks(amount);
                break;
            case "months":
                result = d.plusMonths(amount);
                break;
            case "years":
                result = d.plusYears(amount);
                break;
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit + ". Use: days, weeks, months, years");
        }
        return result.format(DATE_FMT);
    }

    public String timeShift(String time, long amount, String unit) {
        LocalTime t = LocalTime.parse(time, TIME_FMT);
        LocalTime result;

        switch (unit.toLowerCase()) {
            case "seconds":
                result = t.plusSeconds(amount);
                break;
            case "minutes":
                result = t.plusMinutes(amount);
                break;
            case "hours":
                result = t.plusHours(amount);
                break;
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit + ". Use: seconds, minutes, hours");
        }
        return result.format(TIME_FMT);
    }

    public String dateTimeShift(String datetime, long amount, String unit) {
        LocalDateTime dt = LocalDateTime.parse(datetime, DATETIME_FMT);
        LocalDateTime result;

        switch (unit.toLowerCase()) {
            case "seconds":
                result = dt.plusSeconds(amount);
                break;
            case "minutes":
                result = dt.plusMinutes(amount);
                break;
            case "hours":
                result = dt.plusHours(amount);
                break;
            case "days":
                result = dt.plusDays(amount);
                break;
            case "weeks":
                result = dt.plusWeeks(amount);
                break;
            case "months":
                result = dt.plusMonths(amount);
                break;
            case "years":
                result = dt.plusYears(amount);
                break;
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit + ". Use: seconds, minutes, hours, days, weeks, months, years");
        }
        return result.format(DATETIME_FMT);
    }

    // ==================== Timezone ====================

    public String timezoneConvert(String datetime, String fromZone, String toZone) {
        LocalDateTime ldt = LocalDateTime.parse(datetime, DATETIME_FMT);
        ZoneId fromZ = ZoneId.of(fromZone);
        ZoneId toZ = ZoneId.of(toZone);

        ZonedDateTime fromZdt = ZonedDateTime.of(ldt, fromZ);
        ZonedDateTime toZdt = fromZdt.withZoneSameInstant(toZ);

        return toZdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    // ==================== Timestamp ====================

    public String timestampToDatetime(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        return instant.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public String datetimeToTimestamp(String datetime) {
        LocalDateTime ldt = LocalDateTime.parse(datetime, DATETIME_FMT);
        return String.valueOf(ldt.toEpochSecond(ZoneOffset.UTC));
    }

    public long getCurrentTimestamp() {
        return Instant.now().getEpochSecond();
    }

    public String getCurrentDate() {
        return LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    // ==================== Date Info ====================

    public String dateInfo(String date) {
        LocalDate d = LocalDate.parse(date, DATE_FMT);
        WeekFields wf = WeekFields.of(DayOfWeek.MONDAY, 4);

        StringBuilder sb = new StringBuilder();
        sb.append("Weekday: ").append(d.getDayOfWeek()).append("\n");
        sb.append("Week number: ").append(d.get(wf.weekOfWeekBasedYear())).append("\n");
        sb.append("Leap year: ").append(d.isLeapYear()).append("\n");
        sb.append("Days in month: ").append(d.lengthOfMonth()).append("\n");
        sb.append("Day of year: ").append(d.getDayOfYear());
        return sb.toString();
    }

    public String businessDaysAdd(String startDate, int days) {
        if (days < 0) {
            throw new IllegalArgumentException("days must be non-negative");
        }
        LocalDate d = LocalDate.parse(startDate, DATE_FMT);
        int added = 0;
        while (added < days) {
            d = d.plusDays(1);
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                added++;
            }
        }
        return d.format(DATE_FMT);
    }

    public String dateFormat(String date, String pattern) {
        LocalDate d = LocalDate.parse(date, DATE_FMT);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        return d.format(fmt);
    }

    // ==================== Date Diff ====================

    /**
     * Calendar-accurate normalized diff between two date-times.
     * Uses java.time.Period for years/months/days and Duration for hours/minutes/seconds.
     */
    public DateDiffNormalizedResponse dateDiffNormalize(LocalDateTime from, LocalDateTime to) {
        DateDiffNormalizedResponse r = new DateDiffNormalizedResponse();
        r.operation = "date_diff_normalize";

        // Calendar-accurate date part
        Period period = Period.between(from.toLocalDate(), to.toLocalDate());
        r.years = period.getYears();
        r.months = period.getMonths();
        r.days = period.getDays();

        // Time part: advance from by the period, then compute remaining Duration
        LocalDateTime fromAfterPeriod = from.plus(period);
        Duration dur = Duration.between(fromAfterPeriod, to);
        long totalSecs = dur.getSeconds();
        r.hours = totalSecs / 3600;
        totalSecs %= 3600;
        r.minutes = totalSecs / 60;
        r.seconds = totalSecs % 60;

        return r;
    }

    /**
     * Calendar-accurate totals diff between two date-times.
     * Months and years use calendar-aware fractional computation (e.g. a partial
     * February contributes days/28 or days/29, not days/30).
     */
    public DateDiffTotalsResponse dateDiffConvertToUnit(LocalDateTime from, LocalDateTime to) {
        DateDiffTotalsResponse r = new DateDiffTotalsResponse();
        r.operation = "date_diff_convert_to_unit";

        long totalSeconds = ChronoUnit.SECONDS.between(from, to);
        r.totalSeconds = round(totalSeconds);
        r.totalMinutes = round(totalSeconds / 60.0);
        r.totalHours = round(totalSeconds / 3600.0);
        r.totalDays = round(totalSeconds / 86_400.0);

        // Calendar-accurate fractional months
        long wholeMonths = ChronoUnit.MONTHS.between(from, to);
        LocalDateTime afterMonths = from.plusMonths(wholeMonths);
        long remainingDays = ChronoUnit.DAYS.between(afterMonths, to);
        int daysInMonth = afterMonths.toLocalDate().lengthOfMonth();
        r.totalMonths = round(wholeMonths + (double) remainingDays / daysInMonth);

        // Calendar-accurate fractional years
        long wholeYears = ChronoUnit.YEARS.between(from, to);
        LocalDateTime afterYears = from.plusYears(wholeYears);
        long remainingDaysForYear = ChronoUnit.DAYS.between(afterYears, to);
        int daysInYear = afterYears.toLocalDate().lengthOfYear();
        r.totalYears = round(wholeYears + (double) remainingDaysForYear / daysInYear);

        return r;
    }
}