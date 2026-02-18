/*
 * File:  src/main/java/net/ukrcom/onlinemanager/radiusData.java
 */
package net.ukrcom.onlinemanager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/**
 * @author olden
 */
public class radiusData {

    protected String serverName;
    protected String databaseName;
    protected String username;
    protected String password;
    protected Connection con;
    protected Statement statement;
    protected jConfig config;

    public radiusData() throws SQLException {
        try {
            this.config = new jConfigSerializing().load();
            this.serverName = this.config.getServerName();
            this.databaseName = this.config.getDatabaseName();
            this.username = this.config.getUsername();
            this.password = this.config.getPassword();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(radiusData.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.con = DriverManager.getConnection(
                "jdbc:mysql://" + serverName + "/" + databaseName,
                username, password);

        this.statement = con.createStatement();
    }

    public radiusData closeConnections() throws SQLException {
        con.close();
        return this;
    }

    // ====================== ОСНОВНИЙ ЗАПИТ ======================
    public radiusData getData(DefaultTableModel DFT, int days, boolean onlineOnly,
            int selectedIndex, String customerFilter, String usernameFilter)
            throws SQLException {

        String sql = """
                SELECT 
                    radacct.radacctid, 
                    radacct.username, 
                    SUBSTR(radacct.username, LOCATE('@', radacct.username) + 1) AS router, 
                    acctsessionid AS sessionid, 
                    acctstarttime, 
                    acctupdatetime, 
                    acctstoptime, 
                    framedipaddress, 
                    dt, 
                    customername, 
                    location 
                FROM radacct 
                LEFT JOIN radacct_customers_link a ON a.radacctid = radacct.radacctid 
                LEFT JOIN customers b ON b.id = a.customerid 
                WHERE radacct.username LIKE 'dhcp_%'
                """;

        if (onlineOnly) {
            sql += " AND (acctstoptime IS NULL AND radacct.radacctid >= ?) ";
        } else {
            sql += " AND (acctstoptime IS NULL OR DATE_SUB(CURDATE(), INTERVAL ? DAY) <= acctstarttime) ";
        }

        sql += " AND (radacct.username LIKE ? OR framedipaddress"
                + (selectedIndex == 0 ? " LIKE " : "=") + "?) ";

        if (customerFilter.isEmpty()) {
            sql += " AND (customername IS NULL OR customername LIKE ?) ";
        } else {
            sql += " AND (customername IS NOT NULL AND customername LIKE ?) ";
        }

        sql += " ORDER BY acctstarttime, acctupdatetime, acctstoptime, router, username";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int p = 1;
            ps.setInt(p++, onlineOnly ? 0 : days);

            if (usernameFilter.isEmpty()) {
                ps.setString(p++, "%");
                ps.setString(p++, "%");
            } else {
                ps.setString(p++, "%" + usernameFilter + "%");
                ps.setString(p++, selectedIndex == 0 ? "%" + usernameFilter + "%" : usernameFilter);
            }

            ps.setString(p, customerFilter.isEmpty() ? "%" : "%" + customerFilter + "%");

            printPreparedSql(ps);   // ← один рядок

            try (ResultSet rs = ps.executeQuery()) {
                DFT.setRowCount(0);
                while (rs.next()) {
                    DFT.addRow(new Object[]{
                        rs.getString("radacctid"),
                        rs.getString("username"),
                        rs.getString("router"),
                        rs.getString("sessionid"),
                        rs.getString("acctstarttime"),
                        rs.getString("acctupdatetime"),
                        rs.getString("acctstoptime"),
                        rs.getString("framedipaddress"),
                        rs.getString("dt"),
                        rs.getString("customername"),
                        rs.getString("location")
                    });
                }
            }
        }
        return this;
    }

    // ====================== ДУБЛІКАТИ ======================
    public radiusData getDuplicateData(DefaultTableModel DFT) throws
            SQLException {
        String sql = """
                SELECT username, COUNT(*) AS count 
                FROM radacct 
                WHERE username LIKE 'dhcp_%' AND acctstoptime IS NULL 
                GROUP BY username 
                ORDER BY count DESC, username
                """;

        printSql(sql);

        try (ResultSet rs = statement.executeQuery(sql)) {
            DFT.setRowCount(0);
            while (rs.next()) {
                if (rs.getInt("count") > 1) {
                    DFT.addRow(new Object[]{
                        rs.getString("username"),
                        rs.getInt("count")
                    });
                }
            }
        }
        return this;
    }

    // ====================== ДЕТАЛІ ДУБЛІКАТІВ ======================
    public radiusData getDuplicateSessions(DefaultTableModel DFT, String username) throws
            SQLException {
        String sql = """
                SELECT radacctid, framedipaddress, acctstarttime, acctupdatetime 
                FROM radacct 
                WHERE username = ? AND acctstoptime IS NULL
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            printPreparedSql(ps);

            try (ResultSet rs = ps.executeQuery()) {
                DFT.setRowCount(0);
                while (rs.next()) {
                    DFT.addRow(new Object[]{
                        rs.getString("radacctid"),
                        rs.getString("framedipaddress"),
                        rs.getString("acctstarttime"),
                        rs.getString("acctupdatetime")
                    });
                }
            }
        }
        return this;
    }

    // ====================== КАНДИДАТ НА ЗАКРИТТЯ ======================
    public radiusData getAcctStopTimeCandidate(DefaultTableModel DFT, String startTime, String username, String ip)
            throws SQLException {

        String sql = """
                (SELECT DATE_SUB(acctstarttime, INTERVAL 1 SECOND) AS acctstoptime_candidate 
                 FROM radacct 
                 WHERE username = ? AND acctstarttime > ? 
                 ORDER BY acctstarttime, acctupdatetime, acctstoptime LIMIT 1)
                UNION
                (SELECT DATE_SUB(acctstarttime, INTERVAL 1 SECOND) AS acctstoptime_candidate 
                 FROM radacct 
                 WHERE framedipaddress = ? AND acctstarttime > ? 
                 ORDER BY acctstarttime, acctupdatetime, acctstoptime LIMIT 1)
                ORDER BY acctstoptime_candidate LIMIT 1
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, startTime);
            ps.setString(3, ip);
            ps.setString(4, startTime);

            printPreparedSql(ps);

            try (ResultSet rs = ps.executeQuery()) {
                DFT.setRowCount(0);
                while (rs.next()) {
                    DFT.addRow(new Object[]{startTime,
                                            rs.getString("acctstoptime_candidate")});
                }
            }
        }
        return this;
    }

    public radiusData correctionAcctStopTime(Long id, String stopTime) throws
            SQLException {
        String sql = "UPDATE radacct SET acctstoptime = ? WHERE radacctid = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, stopTime);
            ps.setLong(2, id);
            printPreparedSql(ps);
            ps.executeUpdate();
        }
        return this;
    }

    private void printPreparedSql(PreparedStatement ps) {
        printSql(ps
                .toString()
                .replaceAll("^[^:]+: ", "")
        );
    }

    private void printSql(String sql) {
        System.err.println(sql
                .replaceAll("\\s+", " ")
                .trim()
        );
    }
}

/*
CREATE USER 'radsel'@'94.125.120.5' IDENTIFIED BY 'YmE0OTA0MDBiZTVjZDUzMWE4Mjc2ZTM5NTYyYTZlYzNlY2Y1Y2IzYWQ5N2Q4';
GRANT SELECT,UPDATE ON radius.* TO 'radsel'@'94.125.120.5';
FLUSH PRIVILEGES;
 */
