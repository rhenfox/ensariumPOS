package com.aldrin.ensarium.util;

import com.aldrin.ensarium.stockin.LookupOption;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public final class ComboAutoFillSupport {

    private ComboAutoFillSupport() {
    }

    public static void install(JComboBox<LookupOption> combo, List<LookupOption> items, boolean addBlank) {
        combo.setEditable(false);
        updateItems(combo, items, addBlank);
    }

    public static void updateItems(JComboBox<LookupOption> combo, List<LookupOption> items) {
        updateItems(combo, items, false);
    }

    public static void updateItems(JComboBox<LookupOption> combo, List<LookupOption> items, boolean addBlank) {
        DefaultComboBoxModel<LookupOption> model = new DefaultComboBoxModel<>();
        if (addBlank) {
            model.addElement(new LookupOption(null, "", ""));
        }
        if (items != null) {
            for (LookupOption item : items) {
                model.addElement(item);
            }
        }
        combo.setModel(model);
        if (model.getSize() > 0) {
            combo.setSelectedIndex(0);
        }
    }
}
