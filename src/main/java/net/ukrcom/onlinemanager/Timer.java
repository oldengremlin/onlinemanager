/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.ukrcom.onlinemanager;

import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author olden
 */
public class Timer {

    private final jMainFrame jmf;
    private javax.swing.Timer timer;

    public Timer(jMainFrame jmf) {
        this.jmf = jmf;
    }

    public void start() {
        timer = new javax.swing.Timer(60 * 1000, (ActionEvent e) -> {
            if (jmf.jCheckBoxMenuItemAutoRefresh.isSelected() && jmf.gridOnlinePanel.jButtonRefresh.isEnabled()) {
                jmf.simpleRefresh();

                LocalDateTime currentDateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String formattedDateTimeString = currentDateTime.format(formatter);

                jmf.jCheckBoxMenuItemAutoRefresh.setText("Auto Refresh (last update at " + formattedDateTimeString + ")");
            }
        });
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

}
