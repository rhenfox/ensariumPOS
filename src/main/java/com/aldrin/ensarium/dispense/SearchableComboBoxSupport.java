package com.aldrin.ensarium.dispense;

import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

public final class SearchableComboBoxSupport {
    private static final String AUTO_SEARCH_KEY = "ensarium.autoSearch";

    private SearchableComboBoxSupport() {}

    public static void makeSearchable(JComboBox<?> comboBox) {
        comboBox.setEditable(true);
        JTextComponent editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
        AutoSearch autoSearch = new AutoSearch(comboBox);
        editor.setDocument(autoSearch);
        comboBox.putClientProperty(AUTO_SEARCH_KEY, autoSearch);
    }

    public static void beginBulkUpdate(JComboBox<?> comboBox) {
        AutoSearch autoSearch = getAutoSearch(comboBox);
        if (autoSearch != null) autoSearch.setBulkUpdating(true);
    }

    public static void endBulkUpdate(JComboBox<?> comboBox) {
        AutoSearch autoSearch = getAutoSearch(comboBox);
        if (autoSearch != null) {
            autoSearch.refreshItems();
            autoSearch.setBulkUpdating(false);
        }
    }

    private static AutoSearch getAutoSearch(JComboBox<?> comboBox) {
        Object value = comboBox.getClientProperty(AUTO_SEARCH_KEY);
        return value instanceof AutoSearch ? (AutoSearch) value : null;
    }
}
