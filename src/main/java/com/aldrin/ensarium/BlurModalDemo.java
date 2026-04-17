/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium;

/**
 *
 * @author ALDRIN CABUSOG
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class BlurModalDemo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new MainFrame1().setVisible(true);
        });
    }
}

class MainFrame1 extends JFrame {

    private final BlurGlassPane blurPane = new BlurGlassPane(this);

    public MainFrame1() {
        setTitle("Swing Blur Modal Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        setGlassPane(blurPane);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Main JFrame");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));

        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(new Font("SansSerif", Font.PLAIN, 16));
        text.setText("""
                This example shows a true blur effect behind a modal JDialog.

                How it works:
                1. Capture the current JFrame content as an image
                2. Downscale it for speed
                3. Apply blur several times
                4. Paint the blurred image on the glass pane
                5. Show the modal dialog
                """);

        JPanel cards = new JPanel(new GridLayout(2, 3, 12, 12));
        for (int i = 1; i <= 6; i++) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 210, 210)),
                    new EmptyBorder(12, 12, 12, 12)
            ));
            JLabel lbl = new JLabel("Panel " + i);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
            JTextArea desc = new JTextArea("Sample content " + i + "\nMore text here for the blur demo.");
            desc.setEditable(false);
            desc.setOpaque(false);
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);
            card.add(lbl, BorderLayout.NORTH);
            card.add(desc, BorderLayout.CENTER);
            cards.add(card);
        }

        JButton open = new JButton("Open Blurred Modal Dialog");
        open.setFont(new Font("SansSerif", Font.BOLD, 16));
        open.addActionListener(e -> openBlurDialog());

        JPanel north = new JPanel(new BorderLayout());
        north.add(title, BorderLayout.WEST);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.add(open);

        root.add(north, BorderLayout.NORTH);
        root.add(new JScrollPane(text), BorderLayout.WEST);
        root.add(cards, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void openBlurDialog() {
        blurPane.showBlur();

        JDialog dialog = new JDialog(this, "Blurred Modal Dialog", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(420, 240);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("This is a modal dialog");
        label.setFont(new Font("SansSerif", Font.BOLD, 20));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JTextArea area = new JTextArea("""
                The JFrame behind this dialog is blurred
                using a captured image on the glass pane.
                """);
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JButton close = new JButton("Close");
        close.addActionListener(e -> dialog.dispose());

        JPanel btn = new JPanel();
        btn.add(close);

        panel.add(label, BorderLayout.NORTH);
        panel.add(area, BorderLayout.CENTER);
        panel.add(btn, BorderLayout.SOUTH);

        dialog.setContentPane(panel);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                blurPane.hideBlur();
            }
        });

        dialog.setVisible(true);
        blurPane.hideBlur();
    }
}

class BlurGlassPane extends JComponent {

    private final JFrame frame;
    private BufferedImage blurred;

    public BlurGlassPane(JFrame frame) {
        this.frame = frame;
        setOpaque(false);

        addMouseListener(new MouseAdapter() {});
        addMouseMotionListener(new MouseMotionAdapter() {});
        addMouseWheelListener(e -> {});
        addKeyListener(new KeyAdapter() {});
        setFocusTraversalKeysEnabled(false);
    }

    public void showBlur() {
        blurred = createBlurredSnapshot(frame);
        setVisible(true);
        requestFocusInWindow();
        repaint();
    }

    public void hideBlur() {
        setVisible(false);
        blurred = null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (blurred == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(blurred, 0, 0, getWidth(), getHeight(), null);

        // optional dark tint over blur
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
    }

    private BufferedImage createBlurredSnapshot(JFrame frame) {
        int w = frame.getRootPane().getWidth();
        int h = frame.getRootPane().getHeight();

        if (w <= 0 || h <= 0) {
            w = frame.getWidth();
            h = frame.getHeight();
        }

        BufferedImage original = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = original.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        frame.getRootPane().paint(g2);
        g2.dispose();

        // downscale first for better performance
        int scaledW = Math.max(1, w / 2);
        int scaledH = Math.max(1, h / 2);

        BufferedImage small = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gSmall = small.createGraphics();
        gSmall.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gSmall.drawImage(original, 0, 0, scaledW, scaledH, null);
        gSmall.dispose();

        // apply blur several passes
        BufferedImage blurredSmall = small;
        for (int i = 0; i < 3; i++) {
            blurredSmall = blur(blurredSmall);
        }

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gResult = result.createGraphics();
        gResult.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gResult.drawImage(blurredSmall, 0, 0, w, h, null);
        gResult.dispose();

        return result;
    }

    private BufferedImage blur(BufferedImage img) {
        float[] matrix = {
            1f / 9f, 1f / 9f, 1f / 9f,
            1f / 9f, 1f / 9f, 1f / 9f,
            1f / 9f, 1f / 9f, 1f / 9f
        };

        BufferedImage dest = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, matrix), ConvolveOp.EDGE_NO_OP, null);
        op.filter(img, dest);
        return dest;
    }
}
