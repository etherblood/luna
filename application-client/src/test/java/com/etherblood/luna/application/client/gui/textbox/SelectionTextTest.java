package com.etherblood.luna.application.client.gui.textbox;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SuppressWarnings("unused")
class SelectionTextTest {


    @MethodSource
    @ParameterizedTest
    void selected(SelectionText input, String output) {
        assertEquals(output, input.selected());
    }

    static List<Arguments> selected() {
        String text = "abcdefg";
        return List.of(
                Arguments.of(new SelectionText(text, 0, text.length()), text),
                Arguments.of(new SelectionText(text, text.length(), text.length()), ""),
                Arguments.of(new SelectionText(text, 2, 4), "cd")
        );
    }

    @MethodSource
    @ParameterizedTest
    void set(SelectionText input, String value, SelectionText output) {
        assertEquals(output, input.set(value));
    }

    static List<Arguments> set() {
        String text = "abcdefg";
        return List.of(
                Arguments.of(new SelectionText(text, 0, text.length()), "", SelectionText.empty()),
                Arguments.of(new SelectionText(text, 0, 0), "0", new SelectionText("0" + text, 1, 1)),
                Arguments.of(new SelectionText(text, text.length(), text.length()), "9", new SelectionText(text + "9", text.length() + 1, text.length() + 1)),
                Arguments.of(new SelectionText(text, 3, 4), "D", new SelectionText("abcDefg", 4, 4)),
                Arguments.of(new SelectionText(text, 4, 3), "D", new SelectionText("abcDefg", 4, 4))
        );
    }

    @Test
    void selectAll() {
        String text = "abcdefg";
        SelectionText actual = new SelectionText(text, 2, 4).selectAll();

        assertEquals(new SelectionText(text, 0, text.length()), actual);
    }

    @MethodSource
    @ParameterizedTest
    void select(SelectionText input, int tail, int head, SelectionText output) {
        assertEquals(output, input.select(tail, head));
    }

    static List<Arguments> select() {
        String text = "abcdefg";
        return List.of(
                Arguments.of(new SelectionText(text, 0, text.length()), 2, 4, new SelectionText(text, 2, 4)),
                Arguments.of(new SelectionText(text, text.length(), text.length()), 0, 0, new SelectionText(text, 0, 0)),
                Arguments.of(new SelectionText(text, 0, 0), text.length(), 0, new SelectionText(text, text.length(), 0))
        );
    }

    @MethodSource
    @ParameterizedTest
    void left(SelectionText input, boolean shift, boolean ctrl, SelectionText output) {
        assertEquals(output, input.left(shift, ctrl));
    }

    static List<Arguments> left() {
        String text = "This is7, 15A 871 senTence.";// alphanumeric indices: 0-3 5-8 10-13 14-17 18-26
        return List.of(
                Arguments.of(new SelectionText(text, 1, 2), false, false, new SelectionText(text, 1, 1)),
                Arguments.of(new SelectionText(text, 3, 1), false, false, new SelectionText(text, 1, 1)),
                Arguments.of(new SelectionText(text, 1, 1), false, false, new SelectionText(text, 0, 0)),
                Arguments.of(new SelectionText(text, 0, 0), false, false, new SelectionText(text, 0, 0)),

                Arguments.of(new SelectionText(text, 1, 2), true, false, new SelectionText(text, 1, 1)),
                Arguments.of(new SelectionText(text, 3, 1), true, false, new SelectionText(text, 3, 0)),
                Arguments.of(new SelectionText(text, 1, 1), true, false, new SelectionText(text, 1, 0)),
                Arguments.of(new SelectionText(text, 0, 0), true, false, new SelectionText(text, 0, 0)),

                Arguments.of(new SelectionText(text, 1, 8), false, true, new SelectionText(text, 5, 5)),
                Arguments.of(new SelectionText(text, 3, 1), false, true, new SelectionText(text, 0, 0)),
                Arguments.of(new SelectionText(text, 1, 1), false, true, new SelectionText(text, 0, 0)),
                Arguments.of(new SelectionText(text, 5, 5), false, true, new SelectionText(text, 0, 0)),
                Arguments.of(new SelectionText(text, 0, 0), false, true, new SelectionText(text, 0, 0)),
//                Arguments.of(new SelectionText(text, 24, 24), false, true, new SelectionText(text, 21, 21)),// some do, others don't

                Arguments.of(new SelectionText(text, 1, 8), true, true, new SelectionText(text, 1, 5)),
                Arguments.of(new SelectionText(text, 3, 1), true, true, new SelectionText(text, 3, 0)),
                Arguments.of(new SelectionText(text, 1, 1), true, true, new SelectionText(text, 1, 0)),
                Arguments.of(new SelectionText(text, 5, 5), true, true, new SelectionText(text, 5, 0)),
                Arguments.of(new SelectionText(text, 0, 0), true, true, new SelectionText(text, 0, 0))
        );
    }

    @MethodSource
    @ParameterizedTest
    void right(SelectionText input, boolean shift, boolean ctrl, SelectionText output) {
        assertEquals(output, input.right(shift, ctrl));
    }

    static List<Arguments> right() {
        String text = "This is7, 15A 871 senTence.";// alphanumeric indices: 0-3 5-7 10-12 14-16 17-24
        int length = text.length();
        return List.of(
                Arguments.of(new SelectionText(text, 1, 2), false, false, new SelectionText(text, 2, 2)),
                Arguments.of(new SelectionText(text, 3, 1), false, false, new SelectionText(text, 3, 3)),
                Arguments.of(new SelectionText(text, length - 1, length - 1), false, false, new SelectionText(text, length, length)),
                Arguments.of(new SelectionText(text, length, length), false, false, new SelectionText(text, length, length)),

                Arguments.of(new SelectionText(text, 1, 2), true, false, new SelectionText(text, 1, 3)),
                Arguments.of(new SelectionText(text, 3, 1), true, false, new SelectionText(text, 3, 2)),
                Arguments.of(new SelectionText(text, length - 1, length - 1), true, false, new SelectionText(text, length - 1, length)),
                Arguments.of(new SelectionText(text, length, length), true, false, new SelectionText(text, length, length)),

                Arguments.of(new SelectionText(text, 1, 8), false, true, new SelectionText(text, 9, 9)),
                Arguments.of(new SelectionText(text, 3, 1), false, true, new SelectionText(text, 4, 4)),
                Arguments.of(new SelectionText(text, 1, 1), false, true, new SelectionText(text, 4, 4)),
                Arguments.of(new SelectionText(text, 5, 5), false, true, new SelectionText(text, 8, 8)),
                Arguments.of(new SelectionText(text, length, length), false, true, new SelectionText(text, length, length)),

                Arguments.of(new SelectionText(text, 1, 8), true, true, new SelectionText(text, 1, 9)),
                Arguments.of(new SelectionText(text, 3, 1), true, true, new SelectionText(text, 3, 4)),
                Arguments.of(new SelectionText(text, 1, 1), true, true, new SelectionText(text, 1, 4)),
                Arguments.of(new SelectionText(text, 5, 5), true, true, new SelectionText(text, 5, 8)),
                Arguments.of(new SelectionText(text, length, length), true, true, new SelectionText(text, length, length))
        );
    }

    @MethodSource
    @ParameterizedTest
    void fullLeft(SelectionText input, boolean shift, SelectionText output) {
        assertEquals(output, input.fullLeft(shift));
    }

    static List<Arguments> fullLeft() {
        String text = "abcd";
        return List.of(
                Arguments.of(new SelectionText(text, 1, 2), false, new SelectionText(text, 0, 0)),
                Arguments.of(new SelectionText(text, 0, 0), false, new SelectionText(text, 0, 0)),
                Arguments.of(new SelectionText(text, 4, 3), false, new SelectionText(text, 0, 0)),

                Arguments.of(new SelectionText(text, 1, 2), true, new SelectionText(text, 1, 0)),
                Arguments.of(new SelectionText(text, 0, 0), true, new SelectionText(text, 0, 0)),
                Arguments.of(new SelectionText(text, 4, 3), true, new SelectionText(text, 4, 0))
        );
    }

    @MethodSource
    @ParameterizedTest
    void fullRight(SelectionText input, boolean shift, SelectionText output) {
        assertEquals(output, input.fullRight(shift));
    }

    static List<Arguments> fullRight() {
        String text = "abcd";
        return List.of(
                Arguments.of(new SelectionText(text, 1, 2), false, new SelectionText(text, 4, 4)),
                Arguments.of(new SelectionText(text, 0, 0), false, new SelectionText(text, 4, 4)),
                Arguments.of(new SelectionText(text, 4, 3), false, new SelectionText(text, 4, 4)),

                Arguments.of(new SelectionText(text, 1, 2), true, new SelectionText(text, 1, 4)),
                Arguments.of(new SelectionText(text, 0, 0), true, new SelectionText(text, 0, 4)),
                Arguments.of(new SelectionText(text, 4, 3), true, new SelectionText(text, 4, 4))
        );
    }

    @MethodSource
    @ParameterizedTest
    void deleteLeft(SelectionText input, boolean ctrl, SelectionText output) {
        assertEquals(output, input.deleteLeft(ctrl));
    }

    static List<Arguments> deleteLeft() {
        String text = "This is7, senTence.";// alphanumeric indices: 0-3 5-7 9-16
        return List.of(
                Arguments.of(new SelectionText(text, 1, 2), false, new SelectionText("Tis is7, senTence.", 1, 1)),
                Arguments.of(new SelectionText(text, 3, 1), false, new SelectionText("Ts is7, senTence.", 1, 1)),
                Arguments.of(new SelectionText(text, 1, 1), false, new SelectionText("his is7, senTence.", 0, 0)),
                Arguments.of(new SelectionText(text, 0, 0), false, new SelectionText(text, 0, 0)),

                Arguments.of(new SelectionText(text, 1, 8), true, new SelectionText("T, senTence.", 1, 1)),
                Arguments.of(new SelectionText(text, 3, 1), true, new SelectionText("Ts is7, senTence.", 1, 1)),
                Arguments.of(new SelectionText(text, 1, 1), true, new SelectionText("his is7, senTence.", 0, 0)),
                Arguments.of(new SelectionText(text, 5, 5), true, new SelectionText("is7, senTence.", 0, 0)),
                Arguments.of(new SelectionText(text, 0, 0), true, new SelectionText(text, 0, 0))
        );
    }

    @MethodSource
    @ParameterizedTest
    void deleteRight(SelectionText input, boolean ctrl, SelectionText output) {
        assertEquals(output, input.deleteRight(ctrl));
    }

    static List<Arguments> deleteRight() {
        String text = "This is7, senTence.";// alphanumeric indices: 0-3 5-7 9-16
        return List.of(
                Arguments.of(new SelectionText(text, 1, 2), false, new SelectionText("Tis is7, senTence.", 1, 1)),
                Arguments.of(new SelectionText(text, 3, 1), false, new SelectionText("Ts is7, senTence.", 1, 1)),
                Arguments.of(new SelectionText(text, 1, 1), false, new SelectionText("Tis is7, senTence.", 1, 1)),
                Arguments.of(new SelectionText(text, 0, 0), false, new SelectionText("his is7, senTence.", 0, 0)),

                Arguments.of(new SelectionText(text, 1, 8), true, new SelectionText("T, senTence.", 1, 1)),
                Arguments.of(new SelectionText(text, 3, 1), true, new SelectionText("Ts is7, senTence.", 1, 1)),
                Arguments.of(new SelectionText(text, 1, 1), true, new SelectionText("T is7, senTence.", 1, 1)),
                Arguments.of(new SelectionText(text, 5, 5), true, new SelectionText("This , senTence.", 5, 5)),
                Arguments.of(new SelectionText(text, 0, 0), true, new SelectionText(" is7, senTence.", 0, 0))
        );
    }
}