/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package net.ukrcom.onlinemanager;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author olden
 */
public class Onlinemanager {

    public static void main(String[] args) {
        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_WINDOWS) {
            // ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found!");
                Logger.getLogger(RadiusData.class.getName()).log(Level.SEVERE, null, e);
                System.exit(1);
            }
            // ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←
            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            System.setProperty("org.apache.pdfbox.rendering.UsePureJavaCMYKConversion", "true");
            JFrame frame = new jMainFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        } else {
            System.err.println("Unsupported operating system version.");
        }
    }
}
