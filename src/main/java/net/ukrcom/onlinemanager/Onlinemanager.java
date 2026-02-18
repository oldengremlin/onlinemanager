/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package net.ukrcom.onlinemanager;

import javax.swing.JFrame;

/**
 *
 * @author olden
 */
public class Onlinemanager {

    public static void main(String[] args) {
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        System.setProperty("org.apache.pdfbox.rendering.UsePureJavaCMYKConversion", "true");
        JFrame frame = new jMainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }
}
