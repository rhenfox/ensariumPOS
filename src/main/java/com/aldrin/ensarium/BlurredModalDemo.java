/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium;

/**
 *
 * @author ALDRIN CABUSOG
 */
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class BlurredModalDemo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            FlatLightLaf.setup();
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

class MainFrame extends JFrame {

    private final DarkOverlayPane overlayPane = new DarkOverlayPane();

    public MainFrame() {
        setTitle("Swing Modal Dark Background Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        setGlassPane(overlayPane);

        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Main JFrame");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));

        JTextArea area = new JTextArea();
        area.setText("""
                This is the main JFrame.
                
                When you click the button below, a modal JDialog will appear.
                While the dialog is visible, the background frame becomes darker.
                
                This simulates a modern modal effect in Java Swing.
                """);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JButton openDialogButton = new JButton("Open Modal Dialog");
        openDialogButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        openDialogButton.addActionListener(this::openModalDialog);

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.WEST);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(openDialogButton);

        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(area), BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void openModalDialog(ActionEvent e) {
        overlayPane.setVisible(true);
        overlayPane.repaint();

        CustomDialog dialog = new CustomDialog(this);
        dialog.setVisible(true); // modal; blocks here until closed

        overlayPane.setVisible(false);
    }
}

class CustomDialog extends JDialog {

    public CustomDialog(Frame owner) {
        super(owner, "Modal Dialog", true); // true = modal
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 220);
        setLocationRelativeTo(owner);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("This is a modal JDialog.");
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JTextArea text = new JTextArea("""
                While this dialog is open:
                - the JFrame behind it is darkened
                - the user cannot interact with the frame
                """);
        text.setEditable(false);
        text.setOpaque(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(evt -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        panel.add(label, BorderLayout.NORTH);
        panel.add(text, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(panel);
    }
}

class DarkOverlayPane extends JComponent {

    public DarkOverlayPane() {
        setOpaque(false);

        // Prevent clicks from reaching the frame while overlay is visible
        addMouseListener(new java.awt.event.MouseAdapter() {
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
        });
        setFocusTraversalKeysEnabled(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dark transparent layer
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
    }
}
