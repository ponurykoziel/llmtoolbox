package com.sheahorn.llmtoolbox.calculators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.sheahorn.llmtoolbox.calculators.common.Calculator;

public class RegexCalculator implements Calculator {

    private int parseFlags(String flags) {
        int flagBits = 0;
        if (flags != null) {
            for (char c : flags.toCharArray()) {
                switch (c) {
                    case 'i': flagBits |= Pattern.CASE_INSENSITIVE; break;
                    case 'm': flagBits |= Pattern.MULTILINE; break;
                    case 's': flagBits |= Pattern.DOTALL; break;
                    case 'x': flagBits |= Pattern.COMMENTS; break;
                    case 'u': flagBits |= Pattern.UNICODE_CASE; break;
                    case 'd': flagBits |= Pattern.UNIX_LINES; break;
                    default: throw new IllegalArgumentException("Unknown flag: " + c + ". Use: i, m, s, x, u, d");
                }
            }
        }
        return flagBits;
    }

    public String regexTester(String pattern, String text, String flags) {
        int flagBits = parseFlags(flags);

        Pattern p = Pattern.compile(pattern, flagBits);
        Matcher m = p.matcher(text);

        StringBuilder sb = new StringBuilder();
        boolean found = false;
        while (m.find()) {
            found = true;
            sb.append("Match at ").append(m.start()).append("-").append(m.end() - 1)
              .append(": \"").append(m.group()).append("\"");
            int gc = m.groupCount();
            if (gc > 0) {
                sb.append("  groups: [");
                for (int i = 1; i <= gc; i++) {
                    if (i > 1) sb.append(", ");
                    sb.append("\"").append(m.group(i) == null ? "" : m.group(i)).append("\"");
                }
                sb.append("]");
            }
            sb.append("\n");
        }
        if (!found) {
            sb.append("No matches found.");
        }
        return sb.toString().trim();
    }

    public String regexReplace(String pattern, String text, String replacement, String flags, boolean replaceAll) {
        int flagBits = parseFlags(flags);

        Pattern p = Pattern.compile(pattern, flagBits);
        Matcher m = p.matcher(text);

        if (replaceAll) {
            return m.replaceAll(replacement != null ? replacement : "");
        } else {
            return m.replaceFirst(replacement != null ? replacement : "");
        }
    }

    public String regexValidate(String pattern, String flags) {
        try {
            int flagBits = parseFlags(flags);
            Pattern.compile(pattern, flagBits);
            return "valid";
        } catch (PatternSyntaxException e) {
            return "invalid: " + e.getMessage();
        }
    }

    public String regexSplit(String pattern, String text, String flags, Integer limit) {
        int flagBits = parseFlags(flags);

        Pattern p = Pattern.compile(pattern, flagBits);
        String[] parts;
        if (limit != null && limit >= 0) {
            parts = p.split(text, limit);
        } else {
            parts = p.split(text);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("\n");
            sb.append("[").append(i).append("]: \"").append(parts[i]).append("\"");
        }
        return sb.toString();
    }

    public String regexEscape(String text) {
        return Pattern.quote(text);
    }
}
