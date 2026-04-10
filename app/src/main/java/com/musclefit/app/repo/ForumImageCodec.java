package com.musclefit.app.repo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ForumImageCodec {
    private ForumImageCodec() {
    }

    public static String encode(List<String> imageUris) {
        if (imageUris == null || imageUris.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String uri : imageUris) {
            if (uri == null) {
                continue;
            }
            String trimmed = uri.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(escape(trimmed)).append('"');
        }
        sb.append(']');
        return sb.toString();
    }

    public static List<String> decode(String json) {
        if (json == null) {
            return Collections.emptyList();
        }
        String raw = json.trim();
        if (raw.isEmpty() || raw.charAt(0) != '[' || raw.charAt(raw.length() - 1) != ']') {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        int i = 1;
        int end = raw.length() - 1;
        while (i < end) {
            char c = raw.charAt(i);
            if (Character.isWhitespace(c) || c == ',') {
                i++;
                continue;
            }
            if (c != '"') {
                return Collections.emptyList();
            }
            i++;
            StringBuilder value = new StringBuilder();
            boolean escaped = false;
            boolean closed = false;
            while (i < end) {
                char ch = raw.charAt(i++);
                if (escaped) {
                    value.append(unescapeChar(ch));
                    escaped = false;
                    continue;
                }
                if (ch == '\\') {
                    escaped = true;
                    continue;
                }
                if (ch == '"') {
                    closed = true;
                    break;
                }
                value.append(ch);
            }
            if (!closed) {
                return Collections.emptyList();
            }
            String decoded = value.toString().trim();
            if (!decoded.isEmpty()) {
                result.add(decoded);
            }
        }
        return result;
    }

    private static String escape(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' || c == '"') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static char unescapeChar(char escaped) {
        if (escaped == '"' || escaped == '\\') {
            return escaped;
        }
        return escaped;
    }
}
