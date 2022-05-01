package com.etherblood.luna.application.client.gui.textbox;

import java.util.ArrayList;
import java.util.List;

public class EditableText {
    private final int historyLimit;
    private final List<SelectionText> history = new ArrayList<>();
    private int current;
    private boolean freezeText = false;

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
        if (freezeText) {
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
        if (freezeText) {
            return;
        }
        current = Math.max(0, current - 1);
    }

    public void redo() {
        if (freezeText) {
            return;
        }
        current = Math.min(history.size() - 1, current + 1);
    }

    public boolean isFreezeText() {
        return freezeText;
    }

    public void setFreezeText(boolean freezeText) {
        this.freezeText = freezeText;
    }
}
