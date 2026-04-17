package com.aldrin.ensarium.util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Editable combo-box support with suggestions and inline autofill.
 */
public final class ComboBoxAutoSuggestSupport {
    private static final String KEY_ALL_ITEMS = ComboBoxAutoSuggestSupport.class.getName() + ".allItems";
    private static final String KEY_INSTALLED = ComboBoxAutoSuggestSupport.class.getName() + ".installed";

    private ComboBoxAutoSuggestSupport() {
    }

    public static <T> void install(JComboBox<T> comboBox) {
        comboBox.putClientProperty(KEY_ALL_ITEMS, snapshot(comboBox));
        if (Boolean.TRUE.equals(comboBox.getClientProperty(KEY_INSTALLED))) {
            return;
        }
        comboBox.putClientProperty(KEY_INSTALLED, Boolean.TRUE);
        comboBox.setEditable(true);

        JTextField editor = (JTextField) comboBox.getEditor().getEditorComponent();
        final boolean[] adjusting = {false};

        AutoSuggestSupport.install(editor, () -> source(comboBox).stream()
                .map(ComboBoxAutoSuggestSupport::displayText)
                .filter(s -> s != null && !s.isBlank())
                .toList());

        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                if (adjusting[0]) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    if (adjusting[0] || !editor.isFocusOwner()) {
                        return;
                    }
                    List<T> allItems = source(comboBox);
                    String typed = editor.getText();
                    List<T> filtered = filterItems(allItems, typed);
                    adjusting[0] = true;
                    try {
                        comboBox.setModel(toModel(filtered.isEmpty() ? allItems : filtered));
                        comboBox.setEditable(true);
                        editor.setText(typed);
                        editor.setCaretPosition(Math.min(typed.length(), editor.getDocument().getLength()));
                        if (!filtered.isEmpty()) {
                            comboBox.showPopup();
                        } else {
                            comboBox.hidePopup();
                        }
                    } finally {
                        adjusting[0] = false;
                    }
                });
            }
        });

        comboBox.addActionListener(e -> {
            if (adjusting[0]) {
                return;
            }
            Object selected = comboBox.getSelectedItem();
            if (selected != null && !Objects.equals(displayText(selected), editor.getText())) {
                SwingUtilities.invokeLater(() -> editor.setText(displayText(selected)));
            }
        });

        editor.addActionListener(e -> commitSelection(comboBox, editor, adjusting));
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                commitSelection(comboBox, editor, adjusting);
            }
        });
    }

    public static <T> void refresh(JComboBox<T> comboBox) {
        comboBox.putClientProperty(KEY_ALL_ITEMS, snapshot(comboBox));
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> source(JComboBox<T> comboBox) {
        Object stored = comboBox.getClientProperty(KEY_ALL_ITEMS);
        if (stored instanceof List<?> list) {
            return new ArrayList<>((List<T>) list);
        }
        return snapshot(comboBox);
    }

    private static <T> void commitSelection(JComboBox<T> comboBox, JTextField editor, boolean[] adjusting) {
        if (adjusting[0]) {
            return;
        }
        List<T> allItems = source(comboBox);
        String typed = editor.getText();
        T match = bestMatch(allItems, typed);
        adjusting[0] = true;
        try {
            comboBox.setModel(toModel(allItems));
            comboBox.setEditable(true);
            if (match != null) {
                comboBox.setSelectedItem(match);
                editor.setText(displayText(match));
            } else {
                comboBox.setSelectedItem(null);
                editor.setText(typed == null ? "" : typed);
            }
            comboBox.hidePopup();
        } finally {
            adjusting[0] = false;
        }
    }

    private static <T> List<T> snapshot(JComboBox<T> comboBox) {
        List<T> items = new ArrayList<>();
        ComboBoxModel<T> model = comboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            items.add(model.getElementAt(i));
        }
        return items;
    }

    private static <T> List<T> filterItems(List<T> items, String typed) {
        if (typed == null || typed.isBlank()) {
            return new ArrayList<>(items);
        }
        String q = typed.trim().toLowerCase();
        List<T> starts = new ArrayList<>();
        List<T> contains = new ArrayList<>();
        for (T item : items) {
            String value = displayText(item).toLowerCase();
            if (value.startsWith(q)) {
                starts.add(item);
            } else if (value.contains(q)) {
                contains.add(item);
            }
        }
        List<T> out = new ArrayList<>(starts.size() + contains.size());
        out.addAll(starts);
        out.addAll(contains);
        return out;
    }

    private static <T> T bestMatch(List<T> items, String typed) {
        if (typed == null || typed.isBlank()) {
            return items.isEmpty() ? null : items.get(0);
        }
        String q = typed.trim();
        for (T item : items) {
            if (displayText(item).equalsIgnoreCase(q)) {
                return item;
            }
        }
        String qLower = q.toLowerCase();
        for (T item : items) {
            if (displayText(item).toLowerCase().startsWith(qLower)) {
                return item;
            }
        }
        for (T item : items) {
            if (displayText(item).toLowerCase().contains(qLower)) {
                return item;
            }
        }
        return null;
    }

    private static <T> DefaultComboBoxModel<T> toModel(List<T> items) {
        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
        for (T item : items) {
            model.addElement(item);
        }
        return model;
    }

    private static String displayText(Object item) {
        return item == null ? "" : item.toString().trim();
    }
}
