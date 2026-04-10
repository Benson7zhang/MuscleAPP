package com.musclefit.app.ui.assistant;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AiResponseCardParser {
    private static final Pattern BRACKET_SECTION_PATTERN = Pattern.compile("^【\\s*([^】]+?)\\s*】[:：]?\\s*(.*)$");
    private static final Pattern PLAIN_SECTION_PATTERN = Pattern.compile("^([\\p{IsHan}A-Za-z0-9()（）/·\\-\\s]{2,40})\\s*[:：]\\s*(.*)$");

    private AiResponseCardParser() {
    }

    public static ParsedAiResponse parse(String rawContent) {
        String safeRaw = rawContent == null ? "" : rawContent.trim();
        if (safeRaw.isEmpty()) {
            return new ParsedAiResponse("C", "", "", false, "");
        }

        Map<String, StringBuilder> sections = new LinkedHashMap<>();
        String currentSection = null;
        String[] lines = safeRaw.split("\\r?\\n");

        for (String line : lines) {
            String safeLine = line == null ? "" : line.trim();
            if (safeLine.isEmpty()) {
                continue;
            }

            SectionMatch sectionMatch = matchSection(safeLine);
            if (sectionMatch != null) {
                currentSection = sectionMatch.section;
                StringBuilder builder = sections.get(currentSection);
                if (builder == null) {
                    builder = new StringBuilder();
                    sections.put(currentSection, builder);
                }
                if (!sectionMatch.tail.isEmpty()) {
                    appendLine(builder, sectionMatch.tail);
                }
                continue;
            }

            if (currentSection == null) {
                continue;
            }
            StringBuilder builder = sections.get(currentSection);
            if (builder != null) {
                appendLine(builder, safeLine);
            }
        }

        String confidenceRaw = firstNonEmpty(
                sectionText(sections, "置信等级"),
                sectionText(sections, "建议置信等级"),
                sectionText(sections, "可信度等级")
        );
        String confidence = resolveConfidence(confidenceRaw, safeRaw, !sections.isEmpty());

        String profile = sectionText(sections, "资料解读");
        String plan = sectionText(sections, "可执行方案");
        String risk = sectionText(sections, "风险与禁忌");
        String missing = sectionText(sections, "需补充信息");
        String evidence = sectionText(sections, "依据说明");

        boolean structured = !(profile.isEmpty() && plan.isEmpty() && risk.isEmpty() && missing.isEmpty() && evidence.isEmpty());
        String primary = buildPrimary(profile, plan, risk, missing, safeRaw, structured);
        return new ParsedAiResponse(confidence, primary, sanitizeBlock(evidence), structured, safeRaw);
    }

    private static SectionMatch matchSection(String line) {
        Matcher bracketMatcher = BRACKET_SECTION_PATTERN.matcher(line);
        if (bracketMatcher.matches()) {
            String section = canonicalSectionName(bracketMatcher.group(1));
            if (section != null) {
                String tail = bracketMatcher.group(2) == null ? "" : bracketMatcher.group(2).trim();
                return new SectionMatch(section, tail);
            }
        }

        Matcher plainMatcher = PLAIN_SECTION_PATTERN.matcher(line);
        if (plainMatcher.matches()) {
            String section = canonicalSectionName(plainMatcher.group(1));
            if (section != null) {
                String tail = plainMatcher.group(2) == null ? "" : plainMatcher.group(2).trim();
                return new SectionMatch(section, tail);
            }
        }
        return null;
    }

    private static String canonicalSectionName(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim()
                .replace("（", "(")
                .replace("）", ")")
                .replace("：", "");
        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.startsWith("资料解读")) {
            return "资料解读";
        }
        if (normalized.startsWith("可执行方案")) {
            return "可执行方案";
        }
        if (normalized.startsWith("依据说明")) {
            return "依据说明";
        }
        if (normalized.startsWith("风险与禁忌")) {
            return "风险与禁忌";
        }
        if (normalized.startsWith("需补充信息")) {
            return "需补充信息";
        }
        if (normalized.startsWith("置信等级")) {
            return "置信等级";
        }
        if (normalized.startsWith("建议置信等级")) {
            return "建议置信等级";
        }
        if (normalized.startsWith("可信度等级")) {
            return "可信度等级";
        }
        return null;
    }

    private static String buildPrimary(
            String profile,
            String plan,
            String risk,
            String missing,
            String fallback,
            boolean structured
    ) {
        if (!structured) {
            return sanitizeBlock(fallback);
        }

        StringBuilder sb = new StringBuilder();
        if (!profile.isEmpty()) {
            sb.append("资料解读").append('\n').append(sanitizeBlock(profile)).append("\n\n");
        }
        if (!plan.isEmpty()) {
            sb.append("可执行方案").append('\n').append(sanitizeBlock(plan)).append("\n\n");
        }
        if (!risk.isEmpty()) {
            sb.append("风险与禁忌").append('\n').append(sanitizeBlock(risk)).append("\n\n");
        }
        if (!missing.isEmpty()) {
            sb.append("需补充信息").append('\n').append(sanitizeBlock(missing));
        }

        String out = sb.toString().trim();
        return out.isEmpty() ? sanitizeBlock(fallback) : out;
    }

    private static String resolveConfidence(String confidenceRaw, String content, boolean hasSections) {
        if (confidenceRaw != null) {
            String normalized = normalizeConfidence(confidenceRaw);
            if (normalized != null) {
                return normalized;
            }
        }

        String lowered = content.toLowerCase(Locale.ROOT);
        if (lowered.contains("基于通用假设") || lowered.contains("资料不足") || lowered.contains("未提供")) {
            return "C";
        }
        if (hasSections) {
            return "B";
        }
        return "B";
    }

    private static String normalizeConfidence(String raw) {
        if (raw == null) {
            return null;
        }
        String upper = raw.trim().toUpperCase(Locale.ROOT);
        if (upper.startsWith("S")) {
            return "S";
        }
        if (upper.startsWith("A")) {
            return "A";
        }
        if (upper.startsWith("B")) {
            return "B";
        }
        if (upper.startsWith("C")) {
            return "C";
        }
        return null;
    }

    private static String sectionText(Map<String, StringBuilder> sections, String key) {
        StringBuilder builder = sections.get(key);
        return builder == null ? "" : builder.toString().trim();
    }

    private static String firstNonEmpty(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String value : candidates) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private static void appendLine(StringBuilder builder, String value) {
        String cleaned = sanitizeLine(value);
        if (cleaned.isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(cleaned);
    }

    private static String sanitizeBlock(String block) {
        if (block == null || block.trim().isEmpty()) {
            return "";
        }
        String[] lines = block.split("\\r?\\n");
        StringBuilder out = new StringBuilder();
        for (String line : lines) {
            String cleaned = sanitizeLine(line);
            if (cleaned.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append('\n');
            }
            out.append(cleaned);
        }
        return out.toString().trim();
    }

    private static String sanitizeLine(String rawLine) {
        String line = rawLine == null ? "" : rawLine.trim();
        if (line.isEmpty()) {
            return "";
        }

        if (line.matches("^[-*_]{3,}$")) {
            return "";
        }

        line = line.replace("**", "")
                .replace("__", "")
                .replace("`", "");

        line = line.replaceFirst("^#{1,6}\\s*", "");
        line = line.replaceFirst("^>\\s*", "");
        line = line.replaceFirst("^[-*•]+\\s+", "");

        return line.trim();
    }

    private static final class SectionMatch {
        final String section;
        final String tail;

        SectionMatch(String section, String tail) {
            this.section = section;
            this.tail = tail == null ? "" : tail;
        }
    }

    public static final class ParsedAiResponse {
        public final String confidenceLevel;
        public final String primaryText;
        public final String evidenceText;
        public final boolean structured;
        public final String rawText;

        private ParsedAiResponse(String confidenceLevel, String primaryText, String evidenceText, boolean structured, String rawText) {
            this.confidenceLevel = confidenceLevel == null ? "B" : confidenceLevel;
            this.primaryText = primaryText == null ? "" : primaryText;
            this.evidenceText = evidenceText == null ? "" : evidenceText;
            this.structured = structured;
            this.rawText = rawText == null ? "" : rawText;
        }
    }
}
