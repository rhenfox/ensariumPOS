package com.aldrin.ensarium.util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Inline autofill support for JTextField.
 *
 * Behavior:
 * - user types a prefix
 * - first matching suggestion is filled into the field
 * - the suggested suffix is selected so the next keystroke replaces it
 */
public final class AutoSuggestSupport {
    private AutoSuggestSupport() {
    }

    public static void install(JTextField textField, Supplier<List<String>> suggestionSupplier) {
        final boolean[] adjusting = {false};
        final boolean[] backspacePressed = {false};

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                backspacePressed[0] = e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                        || e.getKeyCode() == KeyEvent.VK_DELETE;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
                    SwingUtilities.invokeLater(() -> backspacePressed[0] = false);
                }
            }
        });

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { handleChange(); }
            @Override public void removeUpdate(DocumentEvent e) { handleChange(); }
            @Override public void changedUpdate(DocumentEvent e) { handleChange(); }

            private void handleChange() {
                if (adjusting[0]) {
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    if (adjusting[0] || backspacePressed[0]) {
                        return;
                    }
                    if (!textField.isFocusOwner()) {
                        return;
                    }

                    String text = textField.getText();
                    if (text == null) {
                        return;
                    }
                    String typed = text.trim();
                    if (typed.isEmpty()) {
                        return;
                    }

                    String match = firstMatch(typed, suggestionSupplier.get());
                    if (match == null) {
                        return;
                    }
                    if (match.equals(text)) {
                        return;
                    }

                    int caret = textField.getCaretPosition();
                    if (caret < typed.length()) {
                        return;
                    }

                    try {
                        adjusting[0] = true;
                        textField.setText(match);
                        textField.setCaretPosition(typed.length());
                        textField.moveCaretPosition(match.length());
                    } finally {
                        adjusting[0] = false;
                    }
                });
            }
        });
    }

    private static String firstMatch(String typed, List<String> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        String typedLc = typed.toLowerCase();
        Set<String> unique = new LinkedHashSet<>();
        for (String item : source) {
            if (item == null) {
                continue;
            }
            String value = item.trim();
            if (!value.isEmpty()) {
                unique.add(value);
            }
        }

        for (String item : unique) {
            if (item.toLowerCase().startsWith(typedLc) && item.length() > typed.length()) {
                return item;
            }
        }
        for (String item : unique) {
            if (item.equalsIgnoreCase(typed)) {
                return item;
            }
        }
        return null;
    }
}
