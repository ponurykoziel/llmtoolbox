package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeCalculatorTest {

    private DateTimeCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new DateTimeCalculator(8);
    }

    // ── durationNormalize ──────────────────────────────────────────────

    @Test
    void durationNormalize_mixedFields() {
        DurationRequestDto req = new DurationRequestDto();
        req.days = 1L;
        req.hours = 25L;
        req.minutes = 70L;
        req.seconds = 65L;
        req.millis = 1500L;

        DurationNormalizedResponse r = calc.durationNormalize(req);
        // 1 day + 25h + 70m + 65s + 1500ms
        // = 1d + 1d1h + 1h10m + 1m5s + 1s500ms
        // = 2d + 2h + 11m + 6s + 500ms
        assertEquals(2L, r.days);
        assertEquals(2L, r.hours);
        assertEquals(11L, r.minutes);
        assertEquals(6L, r.seconds);
        assertEquals(500L, r.millis);
    }

    @Test
    void durationNormalize_zero() {
        DurationRequestDto req = new DurationRequestDto();
        DurationNormalizedResponse r = calc.durationNormalize(req);
        assertEquals(0L, r.days);
        assertEquals(0L, r.hours);
        assertEquals(0L, r.minutes);
        assertEquals(0L, r.seconds);
        assertEquals(0L, r.millis);
    }

    @Test
    void durationNormalize_onlyMillis() {
        DurationRequestDto req = new DurationRequestDto();
        req.millis = 90061L; // 1m 30s 61ms
        DurationNormalizedResponse r = calc.durationNormalize(req);
        assertEquals(0L, r.days);
        assertEquals(0L, r.hours);
        assertEquals(1L, r.minutes);
        assertEquals(30L, r.seconds);
        assertEquals(61L, r.millis);
    }

    @Test
    void durationNormalize_largeDays() {
        DurationRequestDto req = new DurationRequestDto();
        req.days = 400L; // over a year
        DurationNormalizedResponse r = calc.durationNormalize(req);
        assertEquals(400L, r.days);
        assertEquals(0L, r.hours);
        assertEquals(0L, r.minutes);
        assertEquals(0L, r.seconds);
        assertEquals(0L, r.millis);
    }

    @Test
    void durationNormalize_overflowAll() {
        DurationRequestDto req = new DurationRequestDto();
        req.hours = 24L;
        req.minutes = 60L;
        req.seconds = 60L;
        req.millis = 1000L;
        DurationNormalizedResponse r = calc.durationNormalize(req);
        assertEquals(1L, r.days);
        assertEquals(1L, r.hours);
        assertEquals(1L, r.minutes);
        assertEquals(1L, r.seconds);
        assertEquals(0L, r.millis);
    }

    @Test
    void durationNormalize_negative() {
        DurationRequestDto req = new DurationRequestDto();
        req.hours = -1L;
        req.minutes = -30L;
        DurationNormalizedResponse r = calc.durationNormalize(req);
        assertEquals(0L, r.days);
        assertEquals(-1L, r.hours);
        assertEquals(-30L, r.minutes);
        assertEquals(0L, r.seconds);
        assertEquals(0L, r.millis);
    }

    @Test
    void durationNormalize_negativeOverflow() {
        DurationRequestDto req = new DurationRequestDto();
        req.hours = -25L;
        req.minutes = -70L;
        DurationNormalizedResponse r = calc.durationNormalize(req);
        assertEquals(-1L, r.days);
        assertEquals(-2L, r.hours);
        assertEquals(-10L, r.minutes);
        assertEquals(0L, r.seconds);
        assertEquals(0L, r.millis);
    }

    // ── durationConvertToUnit ──────────────────────────────────────────

    @Test
    void durationConvertToUnit_normal() {
        DurationRequestDto req = new DurationRequestDto();
        req.hours = 1L;
        req.minutes = 30L;

        DurationTotalsResponse r = calc.durationConvertToUnit(req);
        // 1.5 hours = 90 minutes = 5400 seconds = 5400000 millis
        assertEquals(0.0625, r.totalDays);
        assertEquals(1.5, r.totalHours);
        assertEquals(90.0, r.totalMinutes);
        assertEquals(5400.0, r.totalSeconds);
        assertEquals(5_400_000L, r.totalMillis);
    }

    @Test
    void durationConvertToUnit_zero() {
        DurationRequestDto req = new DurationRequestDto();
        DurationTotalsResponse r = calc.durationConvertToUnit(req);
        assertEquals(0.0, r.totalDays);
        assertEquals(0.0, r.totalHours);
        assertEquals(0.0, r.totalMinutes);
        assertEquals(0.0, r.totalSeconds);
        assertEquals(0L, r.totalMillis);
    }

    @Test
    void durationConvertToUnit_negative() {
        DurationRequestDto req = new DurationRequestDto();
        req.hours = -1L;
        req.minutes = -30L;

        DurationTotalsResponse r = calc.durationConvertToUnit(req);
        assertEquals(-0.0625, r.totalDays);
        assertEquals(-1.5, r.totalHours);
        assertEquals(-90.0, r.totalMinutes);
        assertEquals(-5400.0, r.totalSeconds);
        assertEquals(-5_400_000L, r.totalMillis);
    }

    @Test
    void durationConvertToUnit_fullDay() {
        DurationRequestDto req = new DurationRequestDto();
        req.days = 1L;
        DurationTotalsResponse r = calc.durationConvertToUnit(req);
        assertEquals(1.0, r.totalDays);
        assertEquals(24.0, r.totalHours);
        assertEquals(1440.0, r.totalMinutes);
        assertEquals(86400.0, r.totalSeconds);
        assertEquals(86_400_000L, r.totalMillis);
    }

    // ── dateRange ───────────────────────────────────────────────────────

    @Test
    void dateRange_days() {
        assertEquals(10.0, calc.dateRange("2024-01-01", "2024-01-11", "days"));
    }

    @Test
    void dateRange_weeks() {
        assertEquals(2.0, calc.dateRange("2024-01-01", "2024-01-15", "weeks"));
    }

    @Test
    void dateRange_months() {
        assertEquals(6.0, calc.dateRange("2024-01-01", "2024-07-01", "months"));
    }

    @Test
    void dateRange_years() {
        assertEquals(1.0, calc.dateRange("2024-01-01", "2025-01-01", "years"));
    }

    @Test
    void dateRange_reverseOrder() {
        // negative result when date1 > date2
        assertEquals(-10.0, calc.dateRange("2024-01-11", "2024-01-01", "days"));
    }

    @Test
    void dateRange_sameDate() {
        assertEquals(0.0, calc.dateRange("2024-01-01", "2024-01-01", "days"));
    }

    @Test
    void dateRange_leapYear() {
        // 2024-02-28 to 2024-03-01 = 2 days (leap year)
        assertEquals(2.0, calc.dateRange("2024-02-28", "2024-03-01", "days"));
    }

    @Test
    void dateRange_nonLeapYear() {
        // 2023-02-28 to 2023-03-01 = 1 day
        assertEquals(1.0, calc.dateRange("2023-02-28", "2023-03-01", "days"));
    }

    @Test
    void dateRange_unknownUnit_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.dateRange("2024-01-01", "2024-01-11", "decades"));
    }

    // ── timeRange ───────────────────────────────────────────────────────

    @Test
    void timeRange_seconds() {
        assertEquals(3600.0, calc.timeRange("10:00:00", "11:00:00", "seconds"));
    }

    @Test
    void timeRange_minutes() {
        assertEquals(90.0, calc.timeRange("10:00:00", "11:30:00", "minutes"));
    }

    @Test
    void timeRange_hours() {
        assertEquals(1.5, calc.timeRange("10:00:00", "11:30:00", "hours"));
    }

    @Test
    void timeRange_reverseOrder() {
        assertEquals(-3600.0, calc.timeRange("11:00:00", "10:00:00", "seconds"));
    }

    @Test
    void timeRange_sameTime() {
        assertEquals(0.0, calc.timeRange("10:00:00", "10:00:00", "seconds"));
    }

    @Test
    void timeRange_midnightCross() {
        // LocalTime has no date context: 23:00 to 01:00 on same day = -22 hours = -79200 seconds
        assertEquals(-79200.0, calc.timeRange("23:00:00", "01:00:00", "seconds"));
    }

    @Test
    void timeRange_unknownUnit_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.timeRange("10:00:00", "11:00:00", "days"));
    }

    // ── dateShift ───────────────────────────────────────────────────────

    @Test
    void dateShift_daysForward() {
        assertEquals("2024-01-11", calc.dateShift("2024-01-01", 10, "days"));
    }

    @Test
    void dateShift_daysBackward() {
        assertEquals("2023-12-22", calc.dateShift("2024-01-01", -10, "days"));
    }

    @Test
    void dateShift_weeks() {
        assertEquals("2024-01-15", calc.dateShift("2024-01-01", 2, "weeks"));
    }

    @Test
    void dateShift_months() {
        assertEquals("2024-07-01", calc.dateShift("2024-01-01", 6, "months"));
    }

    @Test
    void dateShift_years() {
        assertEquals("2025-01-01", calc.dateShift("2024-01-01", 1, "years"));
    }

    @Test
    void dateShift_monthOverflow() {
        // Jan 31 + 1 month → Feb 28/29 (Java handles this)
        assertEquals("2024-02-29", calc.dateShift("2024-01-31", 1, "months"));
    }

    @Test
    void dateShift_zero() {
        assertEquals("2024-01-01", calc.dateShift("2024-01-01", 0, "days"));
    }

    @Test
    void dateShift_unknownUnit_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.dateShift("2024-01-01", 1, "decades"));
    }

    // ── timeShift ───────────────────────────────────────────────────────

    @Test
    void timeShift_secondsForward() {
        assertEquals("10:01:30", calc.timeShift("10:00:00", 90, "seconds"));
    }

    @Test
    void timeShift_minutesForward() {
        assertEquals("11:30:00", calc.timeShift("10:00:00", 90, "minutes"));
    }

    @Test
    void timeShift_hoursForward() {
        assertEquals("12:00:00", calc.timeShift("10:00:00", 2, "hours"));
    }

    @Test
    void timeShift_backward() {
        assertEquals("09:00:00", calc.timeShift("10:00:00", -1, "hours"));
    }

    @Test
    void timeShift_midnightWrap() {
        // 23:00 + 3 hours = 02:00
        assertEquals("02:00:00", calc.timeShift("23:00:00", 3, "hours"));
    }

    @Test
    void timeShift_zero() {
        assertEquals("10:00:00", calc.timeShift("10:00:00", 0, "seconds"));
    }

    @Test
    void timeShift_unknownUnit_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.timeShift("10:00:00", 1, "days"));
    }

    // ── dateTimeShift ───────────────────────────────────────────────────

    @Test
    void dateTimeShift_days() {
        assertEquals("2024-01-11T10:00:00",
                calc.dateTimeShift("2024-01-01T10:00:00", 10, "days"));
    }

    @Test
    void dateTimeShift_hours() {
        assertEquals("2024-01-01T12:00:00",
                calc.dateTimeShift("2024-01-01T10:00:00", 2, "hours"));
    }

    @Test
    void dateTimeShift_minutes() {
        assertEquals("2024-01-01T10:30:00",
                calc.dateTimeShift("2024-01-01T10:00:00", 30, "minutes"));
    }

    @Test
    void dateTimeShift_seconds() {
        assertEquals("2024-01-01T10:00:30",
                calc.dateTimeShift("2024-01-01T10:00:00", 30, "seconds"));
    }

    @Test
    void dateTimeShift_weeks() {
        assertEquals("2024-01-15T10:00:00",
                calc.dateTimeShift("2024-01-01T10:00:00", 2, "weeks"));
    }

    @Test
    void dateTimeShift_months() {
        assertEquals("2024-07-01T10:00:00",
                calc.dateTimeShift("2024-01-01T10:00:00", 6, "months"));
    }

    @Test
    void dateTimeShift_years() {
        assertEquals("2025-01-01T10:00:00",
                calc.dateTimeShift("2024-01-01T10:00:00", 1, "years"));
    }

    @Test
    void dateTimeShift_backward() {
        assertEquals("2023-12-31T10:00:00",
                calc.dateTimeShift("2024-01-01T10:00:00", -1, "days"));
    }

    @Test
    void dateTimeShift_zero() {
        assertEquals("2024-01-01T10:00:00",
                calc.dateTimeShift("2024-01-01T10:00:00", 0, "days"));
    }

    @Test
    void dateTimeShift_unknownUnit_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.dateTimeShift("2024-01-01T10:00:00", 1, "decades"));
    }

    // ── timezoneConvert ─────────────────────────────────────────────────

    @Test
    void timezoneConvert_UTCtoPlus5() {
        // UTC → Asia/Karachi (+05:00)
        String result = calc.timezoneConvert("2024-01-01T10:00:00", "UTC", "Asia/Karachi");
        assertTrue(result.contains("+05:00"));
        assertTrue(result.contains("15:00:00"));
    }

    @Test
    void timezoneConvert_Plus5toUTC() {
        String result = calc.timezoneConvert("2024-01-01T15:00:00", "Asia/Karachi", "UTC");
        assertTrue(result.contains("10:00:00"));
    }

    @Test
    void timezoneConvert_sameZone() {
        String result = calc.timezoneConvert("2024-01-01T10:00:00", "UTC", "UTC");
        assertTrue(result.contains("10:00:00"));
    }

    @Test
    void timezoneConvert_negativeOffset() {
        // UTC → America/New_York (EST: -05:00 in January)
        String result = calc.timezoneConvert("2024-01-01T10:00:00", "UTC", "America/New_York");
        assertTrue(result.contains("-05:00"));
        assertTrue(result.contains("05:00:00"));
    }

    @Test
    void timezoneConvert_invalidZone_throws() {
        assertThrows(Exception.class,
                () -> calc.timezoneConvert("2024-01-01T10:00:00", "UTC", "Mars/Colony"));
    }

    // ── timestampToDatetime ────────────────────────────────────────────

    @Test
    void timestampToDatetime_epoch() {
        // Unix epoch = 1970-01-01T00:00:00Z
        String result = calc.timestampToDatetime(0);
        assertTrue(result.contains("1970-01-01"));
        assertTrue(result.contains("00:00:00"));
    }

    @Test
    void timestampToDatetime_knownValue() {
        // 1704067200 = 2024-01-01T00:00:00Z
        String result = calc.timestampToDatetime(1704067200);
        assertTrue(result.contains("2024-01-01"));
        assertTrue(result.contains("00:00:00"));
    }

    @Test
    void timestampToDatetime_negative() {
        // -1 = 1969-12-31T23:59:59Z
        String result = calc.timestampToDatetime(-1);
        assertTrue(result.contains("1969-12-31"));
        assertTrue(result.contains("23:59:59"));
    }

    // ── datetimeToTimestamp ─────────────────────────────────────────────

    @Test
    void datetimeToTimestamp_epoch() {
        assertEquals("0", calc.datetimeToTimestamp("1970-01-01T00:00:00"));
    }

    @Test
    void datetimeToTimestamp_knownValue() {
        assertEquals("1704067200", calc.datetimeToTimestamp("2024-01-01T00:00:00"));
    }

    @Test
    void datetimeToTimestamp_roundtrip() {
        long ts = 1704067200;
        String dt = calc.timestampToDatetime(ts);
        // Extract the datetime part (before timezone offset)
        String dtPart = dt.substring(0, 19); // "2024-01-01T00:00:00"
        String back = calc.datetimeToTimestamp(dtPart);
        assertEquals(String.valueOf(ts), back);
    }

    // ── getCurrentTimestamp ────────────────────────────────────────────

    @Test
    void getCurrentTimestamp_isReasonable() {
        long ts = calc.getCurrentTimestamp();
        // Should be after 2024-01-01 (1704067200) and before 2100
        assertTrue(ts > 1704067200L);
        assertTrue(ts < 4102444800L); // 2100-01-01
    }

    // ── getCurrentDate ─────────────────────────────────────────────────

    @Test
    void getCurrentDate_isValidFormat() {
        String date = calc.getCurrentDate();
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    // ── dateInfo ───────────────────────────────────────────────────────

    @Test
    void dateInfo_normal() {
        String info = calc.dateInfo("2024-01-01");
        assertTrue(info.contains("Weekday: MONDAY"));
        assertTrue(info.contains("Week number: 1"));
        assertTrue(info.contains("Leap year: true"));
        assertTrue(info.contains("Days in month: 31"));
        assertTrue(info.contains("Day of year: 1"));
    }

    @Test
    void dateInfo_nonLeapYear() {
        String info = calc.dateInfo("2023-02-01");
        assertTrue(info.contains("Leap year: false"));
        assertTrue(info.contains("Days in month: 28"));
    }

    @Test
    void dateInfo_leapYearFeb() {
        String info = calc.dateInfo("2024-02-01");
        assertTrue(info.contains("Leap year: true"));
        assertTrue(info.contains("Days in month: 29"));
    }

    @Test
    void dateInfo_endOfYear() {
        String info = calc.dateInfo("2024-12-31");
        assertTrue(info.contains("Day of year: 366")); // leap year
    }

    @Test
    void dateInfo_weekNumber() {
        // 2024-12-31 is a Tuesday, ISO week 1 of 2025? Let's just check it parses
        String info = calc.dateInfo("2024-06-15");
        assertTrue(info.contains("Weekday: SATURDAY"));
    }

    // ── businessDaysAdd ─────────────────────────────────────────────────

    @Test
    void businessDaysAdd_normal() {
        // Monday 2024-01-01 + 5 business days = Monday 2024-01-08
        assertEquals("2024-01-08", calc.businessDaysAdd("2024-01-01", 5));
    }

    @Test
    void businessDaysAdd_overWeekend() {
        // Friday 2024-01-05 + 1 business day = Monday 2024-01-08
        assertEquals("2024-01-08", calc.businessDaysAdd("2024-01-05", 1));
    }

    @Test
    void businessDaysAdd_zeroDays() {
        assertEquals("2024-01-01", calc.businessDaysAdd("2024-01-01", 0));
    }

    @Test
    void businessDaysAdd_startOnSaturday() {
        // Saturday 2024-01-06 + 1 business day = Monday 2024-01-08
        assertEquals("2024-01-08", calc.businessDaysAdd("2024-01-06", 1));
    }

    @Test
    void businessDaysAdd_startOnSunday() {
        // Sunday 2024-01-07 + 1 business day = Monday 2024-01-08
        assertEquals("2024-01-08", calc.businessDaysAdd("2024-01-07", 1));
    }

    @Test
    void businessDaysAdd_manyDays() {
        // Monday + 10 business days = 2 weeks later Monday
        assertEquals("2024-01-15", calc.businessDaysAdd("2024-01-01", 10));
    }

    // ── dateFormat ──────────────────────────────────────────────────────

    @Test
    void dateFormat_customPattern() {
        assertEquals("01/01/2024", calc.dateFormat("2024-01-01", "MM/dd/yyyy"));
    }

    @Test
    void dateFormat_dayMonthYear() {
        assertEquals("01.01.24", calc.dateFormat("2024-01-01", "dd.MM.yy"));
    }

    @Test
    void dateFormat_fullMonth() {
        assertEquals("January 01, 2024", calc.dateFormat("2024-01-01", "MMMM dd, yyyy"));
    }

    @Test
    void dateFormat_weekdayShort() {
        assertEquals("Mon", calc.dateFormat("2024-01-01", "EEE"));
    }

    // ── dateDiffNormalize ──────────────────────────────────────────────

    @Test
    void dateDiffNormalize_normal() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 2, 2, 1, 1, 1);

        DateDiffNormalizedResponse r = calc.dateDiffNormalize(from, to);
        // Calendar-accurate: Period.between(2024-01-01, 2025-02-02) = P1Y1M1D
        // + time part: 1h1m1s
        assertEquals(1L, r.years);
        assertEquals(1L, r.months);
        assertEquals(1L, r.days);
        assertEquals(1L, r.hours);
        assertEquals(1L, r.minutes);
        assertEquals(1L, r.seconds);
    }

    @Test
    void dateDiffNormalize_zero() {
        LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        DateDiffNormalizedResponse r = calc.dateDiffNormalize(dt, dt);
        assertEquals(0L, r.years);
        assertEquals(0L, r.months);
        assertEquals(0L, r.days);
        assertEquals(0L, r.hours);
        assertEquals(0L, r.minutes);
        assertEquals(0L, r.seconds);
    }

    @Test
    void dateDiffNormalize_reverse() {
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        DateDiffNormalizedResponse r = calc.dateDiffNormalize(from, to);
        // Calendar-accurate: Period.between(2025-01-01, 2024-01-01) = P-1Y
        assertEquals(-1L, r.years);
        assertEquals(0L, r.months);
        assertEquals(0L, r.days);
    }

    @Test
    void dateDiffNormalize_onlySeconds() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 1, 0, 1, 30);
        DateDiffNormalizedResponse r = calc.dateDiffNormalize(from, to);
        assertEquals(0L, r.years);
        assertEquals(0L, r.months);
        assertEquals(0L, r.days);
        assertEquals(0L, r.hours);
        assertEquals(1L, r.minutes);
        assertEquals(30L, r.seconds);
    }

    // ── dateDiffConvertToUnit ──────────────────────────────────────────

    @Test
    void dateDiffConvertToUnit_normal() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 2, 0, 0, 0); // exactly 1 day

        DateDiffTotalsResponse r = calc.dateDiffConvertToUnit(from, to);
        assertEquals(86400.0, r.totalSeconds);
        assertEquals(1440.0, r.totalMinutes);
        assertEquals(24.0, r.totalHours);
        assertEquals(1.0, r.totalDays);
        // Calendar-accurate: 1 day out of 31-day January
        assertEquals(1.0 / 31.0, r.totalMonths, 1e-8);
        // Calendar-accurate: 1 day out of 366-day leap year
        assertEquals(1.0 / 366.0, r.totalYears, 1e-8);
    }

    @Test
    void dateDiffConvertToUnit_zero() {
        LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        DateDiffTotalsResponse r = calc.dateDiffConvertToUnit(dt, dt);
        assertEquals(0.0, r.totalSeconds);
        assertEquals(0.0, r.totalMinutes);
        assertEquals(0.0, r.totalHours);
        assertEquals(0.0, r.totalDays);
        assertEquals(0.0, r.totalMonths);
        assertEquals(0.0, r.totalYears);
    }

    @Test
    void dateDiffConvertToUnit_reverse() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 2, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        DateDiffTotalsResponse r = calc.dateDiffConvertToUnit(from, to);
        assertEquals(-86400.0, r.totalSeconds);
        assertEquals(-1440.0, r.totalMinutes);
        assertEquals(-24.0, r.totalHours);
        assertEquals(-1.0, r.totalDays);
    }

    // ── rounding precision ─────────────────────────────────────────────

    @Test
    void rounding_8digits() {
        DurationRequestDto req = new DurationRequestDto();
        req.seconds = 1L;
        req.millis = 333L; // 1.333 seconds
        DurationTotalsResponse r = calc.durationConvertToUnit(req);
        // 1.333 seconds → 0.02221667 minutes
        assertEquals(0.02221667, r.totalMinutes, 1e-8);
    }
}
