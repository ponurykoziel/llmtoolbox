package com.sheahorn.llmtoolbox.calculators;

import java.util.regex.Pattern;

import com.sheahorn.llmtoolbox.calculators.common.Calculator;

public class SemverCalculator implements Calculator {

    private static final Pattern PRERELEASE_NUMERIC = Pattern.compile("\\d+");

    public static class Parsed {
        public final int major;
        public final int minor;
        public final int patch;
        public final String prerelease; // null if none
        public final String build;      // null if none

        Parsed(int major, int minor, int patch, String prerelease, String build) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.prerelease = prerelease;
            this.build = build;
        }
    }

    public Parsed parse(String version) {
        String v = version.startsWith("v") ? version.substring(1) : version;

        String core;
        String prerelease = null;
        String build = null;

        int plusIdx = v.indexOf('+');
        int hyphenIdx = v.indexOf('-');

        if (plusIdx >= 0) {
            build = v.substring(plusIdx + 1);
            v = v.substring(0, plusIdx);
        }

        if (hyphenIdx >= 0) {
            prerelease = v.substring(hyphenIdx + 1);
            v = v.substring(0, hyphenIdx);
        }

        core = v;

        String[] parts = core.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid semver format. Use: MAJOR.MINOR.PATCH[-prerelease][+build]");
        }

        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2]);

            if (major < 0 || minor < 0 || patch < 0) {
                throw new IllegalArgumentException("Version components must be non-negative");
            }

            return new Parsed(major, minor, patch, prerelease, build);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Version components must be non-negative integers: " + e.getMessage(), e);
        }
    }

    public int compare(String v1, String v2) {
        Parsed p1 = parse(v1);
        Parsed p2 = parse(v2);

        if (p1.major != p2.major) return Integer.compare(p1.major, p2.major);
        if (p1.minor != p2.minor) return Integer.compare(p1.minor, p2.minor);
        if (p1.patch != p2.patch) return Integer.compare(p1.patch, p2.patch);

        // Pre-release: a version with prerelease has lower precedence than one without
        if (p1.prerelease == null && p2.prerelease == null) return 0;
        if (p1.prerelease == null) return 1;  // p1 > p2
        if (p2.prerelease == null) return -1; // p1 < p2

        // Compare prerelease identifiers
        return comparePrerelease(p1.prerelease, p2.prerelease);
    }

    private int comparePrerelease(String a, String b) {
        String[] aParts = a.split("\\.");
        String[] bParts = b.split("\\.");
        int len = Math.min(aParts.length, bParts.length);

        for (int i = 0; i < len; i++) {
            boolean aNum = PRERELEASE_NUMERIC.matcher(aParts[i]).matches();
            boolean bNum = PRERELEASE_NUMERIC.matcher(bParts[i]).matches();

            if (aNum && bNum) {
                int cmp = Integer.compare(Integer.parseInt(aParts[i]), Integer.parseInt(bParts[i]));
                if (cmp != 0) return cmp;
            } else if (aNum) {
                return -1; // numeric < alphanumeric
            } else if (bNum) {
                return 1;  // alphanumeric > numeric
            } else {
                int cmp = aParts[i].compareTo(bParts[i]);
                if (cmp != 0) return cmp;
            }
        }

        return Integer.compare(aParts.length, bParts.length);
    }

    public boolean satisfies(String version, String range) {
        Parsed v = parse(version);
        String r = range.trim();

        // Compound range: space-separated constraints, all must be satisfied.
        // Validate each part looks like a constraint before treating as compound,
        // so a malformed version string with spaces falls through to single-constraint
        // parsing instead of producing a confusing error.
        if (r.contains(" ")) {
            String[] parts = r.split("\\s+");
            boolean allValid = true;
            for (String part : parts) {
                if (!isConstraint(part)) {
                    allValid = false;
                    break;
                }
            }
            if (allValid) {
                for (String part : parts) {
                    if (!satisfies(version, part)) return false;
                }
                return true;
            }
            // Fall through: space was not a constraint separator
        }

        // Exact match: "1.2.3"
        if (r.matches("\\d+\\.\\d+\\.\\d+(-[\\w.]+)?(\\+[\\w.]+)?")) {
            return compare(version, r) == 0;
        }

        // Caret: ^1.2.3 — compatible with, >= version
        if (r.startsWith("^")) {
            String base = r.substring(1);
            Parsed b = parse(base);
            String upper;
            if (b.major > 0) {
                upper = (b.major + 1) + ".0.0";
            } else if (b.minor > 0) {
                upper = "0." + (b.minor + 1) + ".0";
            } else {
                upper = "0.0." + (b.patch + 1);
            }
            return compare(version, base) >= 0 && compare(version, upper) < 0;
        }

        // Tilde: ~1.2.3 — approximately equivalent, same minor, >= version
        if (r.startsWith("~")) {
            String base = r.substring(1);
            Parsed b = parse(base);
            String upper = b.major + "." + (b.minor + 1) + ".0";
            return compare(version, base) >= 0 && compare(version, upper) < 0;
        }

        // >=X.Y.Z
        if (r.startsWith(">=")) {
            String base = r.substring(2).trim();
            return compare(version, base) >= 0;
        }

        // <=X.Y.Z
        if (r.startsWith("<=")) {
            String base = r.substring(2).trim();
            return compare(version, base) <= 0;
        }

        // >X.Y.Z
        if (r.startsWith(">")) {
            String base = r.substring(1).trim();
            return compare(version, base) > 0;
        }

        // <X.Y.Z
        if (r.startsWith("<")) {
            String base = r.substring(1).trim();
            return compare(version, base) < 0;
        }

        throw new IllegalArgumentException("Unknown range format: " + range + ". Use: exact, ^X.Y.Z, ~X.Y.Z, >=X.Y.Z, <=X.Y.Z, >X.Y.Z, <X.Y.Z, or multiple constraints separated by space");
    }

    private boolean isConstraint(String s) {
        return s.matches("\\d+\\.\\d+\\.\\d+(-[\\w.]+)?(\\+[\\w.]+)?")
            || s.startsWith("^") || s.startsWith("~")
            || s.startsWith(">=") || s.startsWith("<=")
            || s.startsWith(">") || s.startsWith("<");
    }

    public String bump(String version, String bump) {
        Parsed p = parse(version);

        int major = p.major;
        int minor = p.minor;
        int patch = p.patch;

        switch (bump.toLowerCase()) {
            case "major":
                major++;
                minor = 0;
                patch = 0;
                break;
            case "minor":
                minor++;
                patch = 0;
                break;
            case "patch":
                patch++;
                break;
            default:
                throw new IllegalArgumentException("Unknown bump: " + bump + ". Use: major, minor, patch");
        }

        return major + "." + minor + "." + patch;
    }
}
