/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium.icons;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 *
 * @author ALDRIN CABUSOG
 */

//    Color iconChevLColor = UiStyle.c(211, 47, 47);    // red icon
//    Color white = Color.WHITE;
//    private final Color text = new Color(235, 239, 243);
//
//    private final Icon iconChevL = com.aldrin.quicktenda.util.FaSwingIcons.icon(FontAwesomeIcon.CHEVRON_LEFT, 18, text);
//    private final Icon iconBars = com.aldrin.quicktenda.util.FaSwingIcons.icon(FontAwesomeIcon.BARS, 18, text);
//    private final Icon iconBars1 = com.aldrin.quicktenda.util.FaSwingIcons.icon(FontAwesomeIcon.SHOPPING_CART, 18, text);
public class FaSwingIcons {

    private static final String TTF_PATH = "/de/jensd/fx/glyphs/fontawesome/fontawesome-webfont.ttf";
    private static final Font FA_BASE = loadFontOrFallback();
    private static final Map<String, Icon> CACHE = new ConcurrentHashMap<>();

    public static Icon icon(FontAwesomeIcon fa, int sizePx, Color color) {
        String key = fa.name() + "|" + sizePx + "|" + (color == null ? "null" : color.getRGB());
        return CACHE.computeIfAbsent(key, k -> render(fa, sizePx, color));
    }

    public static Icon render(FontAwesomeIcon fa, int sizePx, Color color) {
        String glyph = glyphString(fa);

        BufferedImage img = new BufferedImage(sizePx, sizePx, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color useColor = (color != null) ? color : UIManager.getColor("Button.foreground");
            if (useColor == null) {
                useColor = Color.DARK_GRAY;
            }

            float fontSize = Math.max(10f, sizePx * 0.85f);
            Font font = FA_BASE.deriveFont(Font.PLAIN, fontSize);

            g2.setColor(useColor);
            g2.setFont(font);

            FontRenderContext frc = g2.getFontRenderContext();
            Rectangle2D bounds = font.getStringBounds(glyph, frc);

            int x = (int) Math.round((sizePx - bounds.getWidth()) / 2.0);
            int y = (int) Math.round((sizePx - bounds.getHeight()) / 2.0 - bounds.getY());

            g2.drawString(glyph, x, y);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    public static String glyphString(FontAwesomeIcon fa) {
        try {
            Method m = fa.getClass().getMethod("unicodeToString");
            Object out = m.invoke(fa);
            if (out instanceof String s) {
                return s;
            }
        } catch (Exception ignored) {
        }

        for (String fieldName : new String[]{"unicode", "character", "ch", "code"}) {
            try {
                Field f = fa.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                Object v = f.get(fa);
                if (v instanceof Character c) {
                    return String.valueOf(c);
                }
                if (v instanceof String s) {
                    return s;
                }
                if (v instanceof Integer i) {
                    return new String(Character.toChars(i));
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    public static Font loadFontOrFallback() {
        try (InputStream is = FaSwingIcons.class.getResourceAsStream(TTF_PATH)) {
            if (is == null) {
                return new Font("SansSerif", Font.PLAIN, 12);
            }
            Font f = Font.createFont(Font.TRUETYPE_FONT, is);
            return f.deriveFont(Font.PLAIN, 12f);
        } catch (Exception e) {
            return new Font("SansSerif", Font.PLAIN, 12);
        }
    }
}
