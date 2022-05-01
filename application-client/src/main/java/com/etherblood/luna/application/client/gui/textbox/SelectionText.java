package com.etherblood.luna.application.client.gui.textbox;

import java.util.Objects;

public record SelectionText(
        String text,
        int tail,
        int head
) {
    public SelectionText(String text) {
        this(text, text.length(), text.length());
    }

    public SelectionText {
        Objects.requireNonNull(text);
        if (tail < 0 || text.length() < tail) {
            throw new IllegalArgumentException("tail must be in the range 0 - text.length().");
        }
        if (head < 0 || text.length() < head) {
            throw new IllegalArgumentException("head must be in the range 0 - text.length().");
        }
    }

    public static SelectionText empty() {
        return new SelectionText("", 0, 0);
    }

    public String selected() {
        return text.substring(Math.min(tail, head), Math.max(tail, head));
    }

    public SelectionText set(String value) {
        StringBuilder builder = new StringBuilder();
        builder.append(text, 0, Math.min(tail, head));
        builder.append(value);
        int nextCursor = builder.length();
        builder.append(text, Math.max(tail, head), text.length());
        return new SelectionText(builder.toString(), nextCursor, nextCursor);
    }

    public SelectionText selectAll() {
        return select(0, text.length());
    }

    public SelectionText select(int tail, int head) {
        return new SelectionText(text, tail, head);
    }

    public SelectionText left(boolean shift, boolean ctrl) {
        int newTail;
        int newHead;
        if (shift) {
            newTail = tail;
            newHead = navigateLeft(text, head, ctrl);
        } else if (ctrl || tail == head) {
            newTail = newHead = navigateLeft(text, head, ctrl);
        } else {
            newTail = newHead = Math.min(tail, head);
        }
        return new SelectionText(text, newTail, newHead);
    }

    public SelectionText right(boolean shift, boolean ctrl) {
        int newTail;
        int newHead;
        if (shift) {
            newTail = tail;
            newHead = navigateRight(text, head, ctrl);
        } else if (ctrl || tail == head) {
            newTail = newHead = navigateRight(text, head, ctrl);
        } else {
            newTail = newHead = Math.max(tail, head);
        }
        return new SelectionText(text, newTail, newHead);
    }

    public SelectionText fullLeft(boolean shift) {
        return new SelectionText(text, shift ? tail : 0, 0);
    }

    public SelectionText fullRight(boolean shift) {
        return new SelectionText(text, shift ? tail : text.length(), text.length());
    }

    public SelectionText deleteLeft(boolean ctrl) {
        if (tail == head) {
            if (tail > 0) {
                int left = navigateLeft(text, tail, ctrl);
                String nextText = text.substring(0, left) + text.substring(tail);
                return new SelectionText(nextText, left, left);
            }
            return this;
        } else {
            return set("");
        }
    }

    public SelectionText deleteRight(boolean ctrl) {
        if (tail == head) {
            if (tail < text.length()) {
                int right = navigateRight(text, tail, ctrl);
                String nextText = text.substring(0, tail) + text.substring(right);
                return new SelectionText(nextText, tail, tail);
            }
            return this;
        } else {
            return set("");
        }
    }

    private static int navigateLeft(String text, int cursor, boolean ctrl) {
        cursor = Math.max(0, cursor - 1);
        if (ctrl) {
            while (cursor > 0 && isAlphanumeric(Character.codePointAt(text, cursor - 1))) {
                cursor--;
            }
        }
        return cursor;
    }

    private static int navigateRight(String text, int cursor, boolean ctrl) {
        cursor = Math.min(text.length(), cursor + 1);
        if (ctrl) {
            while (cursor < text.length() && isAlphanumeric(Character.codePointAt(text, cursor))) {
                cursor++;
            }
        }
        return cursor;
    }

    private static boolean isAlphanumeric(int codepoint) {
        return Character.isAlphabetic(codepoint) || Character.isDigit(codepoint);
    }
}
