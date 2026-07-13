package com.sheahorn.llmtoolbox.calculators;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sheahorn.llmtoolbox.calculators.common.DoubleValueCalculator;

public class TextCalculator extends DoubleValueCalculator {

    public TextCalculator(int roundingDigits) {
        super(roundingDigits);
    }

    // ==================== Normalization ====================

    public String normalize(String input, TextNormalizationOptions opts) {
        if (input == null) return "";
        if (opts == null) opts = defaultOptions();

        String s = input;

        if (opts.trim != null && opts.trim) {
            s = s.trim();
        }

        if (opts.normalize_unicode != null && opts.normalize_unicode) {
            s = Normalizer.normalize(s, Normalizer.Form.NFKC);
        }

        if (opts.case_sensitive == null || !opts.case_sensitive) {
            s = s.toLowerCase();
        }

        if (opts.remove_punctuation != null && opts.remove_punctuation) {
            s = s.replaceAll("\\p{Punct}", "");
        }

        if (opts.collapse_whitespace != null && opts.collapse_whitespace) {
            s = s.replaceAll("\\s+", " ");
        }

        return s;
    }

    private TextNormalizationOptions defaultOptions() {
        TextNormalizationOptions opts = new TextNormalizationOptions();
        opts.case_sensitive = false;
        opts.trim = true;
        opts.normalize_unicode = true;
        opts.remove_punctuation = false;
        opts.collapse_whitespace = true;
        return opts;
    }

    // ==================== Levenshtein ====================

    public double levenshtein(String a, String b, TextNormalizationOptions opts) {
        return round(levenshteinDistance(normalize(a, opts), normalize(b, opts)));
    }

    // ==================== Normalized Levenshtein ====================

    public double normalizedLevenshtein(String a, String b, TextNormalizationOptions opts) {
        String na = normalize(a, opts);
        String nb = normalize(b, opts);
        int maxLen = Math.max(na.length(), nb.length());
        if (maxLen == 0) return round(1.0);

        int distance = levenshteinDistance(na, nb);
        double similarity = 1.0 - (double) distance / maxLen;
        return round(similarity);
    }

    // ==================== Jaccard ====================

    public double jaccard(String a, String b, TextNormalizationOptions opts) {
        String na = normalize(a, opts);
        String nb = normalize(b, opts);
        Set<Character> setA = new HashSet<>();
        Set<Character> setB = new HashSet<>();
        for (char c : na.toCharArray()) setA.add(c);
        for (char c : nb.toCharArray()) setB.add(c);

        Set<Character> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        Set<Character> union = new HashSet<>(setA);
        union.addAll(setB);

        if (union.isEmpty()) {
            return round(1.0);
        }
        return round((double) intersection.size() / union.size());
    }

    // ==================== N-gram Overlap ====================

    public double ngramOverlap(String a, String b, int n, TextNormalizationOptions opts) {
        if (n < 1) throw new IllegalArgumentException("n must be >= 1");
        String na = normalize(a, opts);
        String nb = normalize(b, opts);

        Set<String> ngramsA = ngrams(na, n);
        Set<String> ngramsB = ngrams(nb, n);

        if (ngramsA.isEmpty() && ngramsB.isEmpty()) {
            return round(1.0);
        }

        Set<String> intersection = new HashSet<>(ngramsA);
        intersection.retainAll(ngramsB);
        Set<String> union = new HashSet<>(ngramsA);
        union.addAll(ngramsB);

        return round((double) intersection.size() / union.size());
    }

    // ==================== Fuzzy Match ====================

    public double fuzzyMatch(String a, String b, int threshold, TextNormalizationOptions opts) {
        String na = normalize(a, opts);
        String nb = normalize(b, opts);
        int m = na.length();
        int n = nb.length();
        if (Math.abs(m - n) > threshold) {
            return round(0.0);
        }

        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        for (int j = 0; j <= n; j++) prev[j] = j;

        for (int i = 1; i <= m; i++) {
            curr[0] = i;
            int minInRow = i;
            for (int j = 1; j <= n; j++) {
                int cost = na.charAt(i - 1) == nb.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(prev[j] + 1, curr[j - 1] + 1), prev[j - 1] + cost);
                if (curr[j] < minInRow) minInRow = curr[j];
            }
            if (minInRow > threshold) {
                return round(0.0);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }

        return round(prev[n] <= threshold ? 1.0 : 0.0);
    }

    // ==================== Count Occurrences ====================

    public int countOccurrences(String text, String substring, TextNormalizationOptions opts) {
        String nt = normalize(text, opts);
        String ns = normalize(substring, opts);
        if (ns == null || ns.isEmpty()) {
            throw new IllegalArgumentException("substring must not be empty");
        }
        int count = 0;
        int idx = 0;
        while ((idx = nt.indexOf(ns, idx)) != -1) {
            count++;
            idx += ns.length();
        }
        return count;
    }

    // ==================== Diff ====================

    /**
     * Raw line-by-line diff with no normalization. Output shows the user's original
     * text exactly as provided.
     */
    public String diffRaw(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";

        String[] linesA = a.split("\n", -1);
        String[] linesB = b.split("\n", -1);

        return diffLines(linesA, linesB);
    }

    /**
     * Normalized line-by-line diff. Both inputs are normalized before comparison,
     * so the output shows normalized lines. Use {@link #diffRaw} if you need the
     * original text preserved.
     */
    public String diff(String a, String b, TextNormalizationOptions opts) {
        String na = normalize(a, opts);
        String nb = normalize(b, opts);

        String[] linesA = na.split("\n", -1);
        String[] linesB = nb.split("\n", -1);

        return diffLines(linesA, linesB);
    }

    private String diffLines(String[] linesA, String[] linesB) {
        // LCS-based diff
        int m = linesA.length;
        int n = linesB.length;
        int[][] dp = new int[m + 1][n + 1];
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                if (linesA[i].equals(linesB[j])) {
                    dp[i][j] = dp[i + 1][j + 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j + 1]);
                }
            }
        }

        List<String> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < m && j < n) {
            if (linesA[i].equals(linesB[j])) {
                result.add("  " + linesA[i]);
                i++;
                j++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                result.add("- " + linesA[i]);
                i++;
            } else {
                result.add("+ " + linesB[j]);
                j++;
            }
        }
        while (i < m) {
            result.add("- " + linesA[i]);
            i++;
        }
        while (j < n) {
            result.add("+ " + linesB[j]);
            j++;
        }

        return String.join("\n", result);
    }

    // ==================== helpers ====================

    private int levenshteinDistance(String a, String b) {
        int m = a.length();
        int n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[m][n];
    }

    private Set<String> ngrams(String s, int n) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i <= s.length() - n; i++) {
            set.add(s.substring(i, i + n));
        }
        return set;
    }
}