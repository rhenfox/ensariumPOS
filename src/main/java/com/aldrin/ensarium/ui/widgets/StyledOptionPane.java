/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium.ui.widgets;

/**
 *
 * @author ALDRIN CABUSOG
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class StyledOptionPane {

    private StyledOptionPane() {
    }

    public static void showInfo(Component parent, Object message, String title) {
        JOptionPane pane = new JOptionPane(
                normalizeMessage(message),
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION
        );

        StyledButton ok = createButton("OK", StyledButton.ButtonStyle.PRIMARY, pane, JOptionPane.OK_OPTION);
        pane.setOptions(new Object[]{ok});
        pane.setInitialValue(ok);

        showDialog(parent, title, pane);
    }

    public static int showConfirm(Component parent, Object message, String title) {
        return showConfirm(parent, message, title, false);
    }

    public static int showConfirm(Component parent, Object message, String title, boolean withCancel) {
        JOptionPane pane = new JOptionPane(
                normalizeMessage(message),
                JOptionPane.QUESTION_MESSAGE,
                withCancel ? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION
        );

        StyledButton yes = createButton("Yes", StyledButton.ButtonStyle.SUCCESS, pane, JOptionPane.YES_OPTION);
        StyledButton no = createButton("No", StyledButton.ButtonStyle.SECONDARY, pane, JOptionPane.NO_OPTION);

        if (withCancel) {
            StyledButton cancel = createButton("Cancel", StyledButton.ButtonStyle.DANGER, pane, JOptionPane.CANCEL_OPTION);
            pane.setOptions(new Object[]{yes, no, cancel});
        } else {
            pane.setOptions(new Object[]{yes, no});
        }

        pane.setInitialValue(yes);

        Object value = showDialog(parent, title, pane);
        return value instanceof Integer ? (Integer) value : JOptionPane.CLOSED_OPTION;
    }

    public static <T> T showSelection(Component parent, String message, String title, T[] items, T initialSelection) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setSelectedItem(initialSelection);
        combo.setPreferredSize(new Dimension(360, 34));

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        JLabel label = new JLabel(toHtml(message));
        label.setVerticalAlignment(SwingConstants.TOP);

        panel.add(label, BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);
        panel.add(Box.createVerticalStrut(4), BorderLayout.SOUTH);

        JOptionPane pane = new JOptionPane(
                panel,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION
        );

        StyledButton ok = createButton("OK", StyledButton.ButtonStyle.PRIMARY, pane, JOptionPane.OK_OPTION);
        StyledButton cancel = createButton("Cancel", StyledButton.ButtonStyle.SECONDARY, pane, JOptionPane.CANCEL_OPTION);

        pane.setOptions(new Object[]{ok, cancel});
        pane.setInitialValue(ok);

        Object value = showDialog(parent, title, pane);
        if (value instanceof Integer && ((Integer) value) == JOptionPane.OK_OPTION) {
            return (T) combo.getSelectedItem();
        }
        return null;
    }

    private static StyledButton createButton(String text, StyledButton.ButtonStyle style, JOptionPane pane, int result) {
        StyledButton button = new StyledButton(text);
        button.setButtonStyle(style);
        button.setPreferredSize(new Dimension(110, 36));
        button.addActionListener(e -> pane.setValue(result));
        return button;
    }

    private static Object showDialog(Component parent, String title, JOptionPane pane) {
        JDialog dialog = pane.createDialog(parent, title);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setVisible(true);

        Object value = pane.getValue();
        dialog.dispose();
        return value;
    }

    private static Object normalizeMessage(Object message) {
        if (message instanceof String s) {
            return toHtml(s);
        }
        return message;
    }

    private static String toHtml(String text) {
        if (text == null || text.isBlank()) {
            return "<html></html>";
        }
        return "<html>" + text.replace("\n", "<br>") + "</html>";
    }
}
