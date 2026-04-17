package com.aldrin.ensarium.dispense;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.JTextComponent;

public class AutoSearch extends PlainDocument {
    private final JComboBox<Object> comboBox;
    private final List<Object> masterItems = new ArrayList<>();
    private ComboBoxModel<Object> model;
    private final JTextComponent editor;
    private boolean selecting;
    private boolean bulkUpdating;

    @SuppressWarnings("unchecked")
    public AutoSearch(final JComboBox<?> cb) {
        this.comboBox = (JComboBox<Object>) cb;
        this.model = (ComboBoxModel<Object>) cb.getModel();
        this.editor = (JTextComponent) cb.getEditor().getEditorComponent();
        cacheItems();

        cb.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!selecting) highlightCompletedText(0);
            }
        });
        editor.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (bulkUpdating) return;
                if (comboBox.isShowing() && editor.isShowing()) {
                    SwingUtilities.invokeLater(() -> {
                        if (comboBox.isShowing() && editor.isShowing()) {
                            comboBox.setPopupVisible(true);
                        }
                    });
                }
            }
        });
    }

    private void cacheItems() {
        masterItems.clear();
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            masterItems.add(comboBox.getItemAt(i));
        }
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        if (selecting || bulkUpdating) return;
        super.remove(offs, len);
        filter(getText(0, getLength()));
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (selecting || bulkUpdating) return;
        super.insertString(offs, str, a);
        filter(getText(0, getLength()));
    }

    private void filter(String text) {
        if (bulkUpdating) return;
        selecting = true;
        try {
            comboBox.removeAllItems();
            String needle = text == null ? "" : text.trim().toLowerCase();
            Object first = null;
            for (Object item : masterItems) {
                String s = Objects.toString(item, "").toLowerCase();
                if (needle.isBlank() || s.contains(needle)) {
                    comboBox.addItem(item);
                    if (first == null) first = item;
                }
            }
            comboBox.getEditor().setItem(text);
            if (comboBox.getItemCount() > 0) {
                comboBox.setSelectedItem(first);
                comboBox.getEditor().setItem(text);
                if (comboBox.isShowing() && editor.isShowing()) {
                    SwingUtilities.invokeLater(() -> {
                        if (comboBox.isShowing() && editor.isShowing()) {
                            comboBox.setPopupVisible(true);
                        }
                    });
                }
            } else if (comboBox.isShowing()) {
                comboBox.setPopupVisible(false);
            }
        } finally {
            selecting = false;
            SwingUtilities.invokeLater(() -> editor.setCaretPosition(editor.getText().length()));
        }
    }

    public void setBulkUpdating(boolean bulkUpdating) {
        this.bulkUpdating = bulkUpdating;
    }

    public void refreshItems() {
        this.model = (ComboBoxModel<Object>) comboBox.getModel();
        cacheItems();
    }

    private void highlightCompletedText(int start) {
        editor.setSelectionStart(start);
        editor.setSelectionEnd(editor.getText().length());
    }
}
