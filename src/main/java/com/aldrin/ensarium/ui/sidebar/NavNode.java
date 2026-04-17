package com.aldrin.ensarium.ui.sidebar;

import javax.swing.Icon;

public class NavNode {
    public final String key;
    public final String label;
    public final Icon icon;

    public NavNode(String key, String label, Icon icon) {
        this.key = key;
        this.label = label;
        this.icon = icon;
    }
}
