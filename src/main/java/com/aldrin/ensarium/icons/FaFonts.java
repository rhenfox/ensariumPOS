/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium.icons;

/**
 *
 * @author ALDRIN CABUSOG
 */
import java.awt.Font;
import java.io.InputStream;

public final class FaFonts {
    private static volatile Font SOLID;

    private FaFonts() {}

    public static Font solid() {
        if (SOLID != null) return SOLID;
        synchronized (FaFonts.class) {
            if (SOLID != null) return SOLID;
            try (InputStream in = FaFonts.class.getResourceAsStream("/fonts/fa-solid-900.ttf")) {
                if (in == null) {
                    throw new IllegalStateException("Missing resource: /fonts/fa-solid-900.ttf");
                }
                SOLID = Font.createFont(Font.TRUETYPE_FONT, in);
                return SOLID;
            } catch (Exception e) {
                throw new RuntimeException("Failed to load Font Awesome solid font", e);
            }
        }
    }
}

