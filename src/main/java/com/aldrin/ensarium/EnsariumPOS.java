/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.aldrin.ensarium;


import com.aldrin.ensarium.db.DatabaseBootstrap;
import com.aldrin.ensarium.ui.MainFrame;
import com.aldrin.ensarium.ui.dialog.DatabaseConfigDialog;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle2;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author ALDRIN CABUSOG
 */
public class EnsariumPOS {

      public static void main(String[] args) {
          
          
        SwingUtilities.invokeLater(() -> {
//            try {
//                
//            } catch (Exception ignored) {
//                try {
//                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//                } catch (Exception ignored2) {
//                }
//            }

            if (!DatabaseConfigDialog.ensureConfigured(null)) {
                return;
            }

            try {
                DatabaseBootstrap.initialize();
            } catch (Exception ex) {
                String message = ex.getCause() != null && ex.getCause().getMessage() != null
                        ? ex.getCause().getMessage()
                        : ex.getMessage();
                JOptionPane.showMessageDialog(null,
                        "Database initialization failed.\n" + message,
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            FlatLightLaf.setup();
            new MainFrame().setVisible(true);
            
           
            
        });
    }
      
      
}
