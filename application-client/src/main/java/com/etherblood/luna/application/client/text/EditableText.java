package com.etherblood.luna.application.client.text;

import java.util.ArrayList;
import java.util.List;

public class EditableText {
    private final int historyLimit;
    private final List<SelectionText> history = new ArrayList<>();
    private int current;

    public EditableText(SelectionText initial) {
        this(initial, 100);
    }

    public EditableText(SelectionText initial, int historyLimit) {
        history.add(initial);
        this.historyLimit = historyLimit;
    }

    public SelectionText current() {
        return history.get(current);
    }

    public void push(SelectionText state) {
        if (current().text().equals(state.text())) {
            history.set(current, state);
            return;
        }
        for (int i = history.size() - 1; i > current; i--) {
            history.remove(i);
        }
        history.add(state);
        current++;
        while (history.size() > historyLimit) {
            history.remove(0);
            current--;
        }
    }

    public void undo() {
        current = Math.max(0, current - 1);
    }

    public void redo() {
        current = Math.min(history.size() - 1, current + 1);
    }
}
