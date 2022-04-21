package com.etherblood.luna.application.client.text;

import java.util.ArrayList;
import java.util.List;

public class EditableText {
    private final int historyLimit = 100;
    private final List<SelectionText> history = new ArrayList<>();
    private int current;

    public EditableText(SelectionText initial) {
        history.add(initial);
    }

    public SelectionText current() {
        return history.get(current);
    }

    public boolean push(SelectionText state) {
        if (current().equals(state)) {
            return false;
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
        return true;
    }

    public void undo() {
        current = Math.max(0, current - 1);
    }

    public void redo() {
        current = Math.min(history.size() - 1, current + 1);
    }
}
