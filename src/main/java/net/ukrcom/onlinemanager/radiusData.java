/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.ukrcom.onlinemanager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author olden
 */
public class radiusData {

    protected String serverName;
    protected String portNumber;
    protected String databaseName;
    protected String username;
    protected String password;
    protected Connection con;
    protected Statement statement;
    protected jConfig config;

    private JFrame setup;

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

        this.con = DriverManager.getConnection("jdbc:mysql://" + this.serverName + "/" + this.databaseName, this.username, this.password);
        this.statement = con.createStatement();
    }

    public radiusData closeConnections() throws SQLException {
        this.con.close();
        return this;
    }

    public radiusData getData(DefaultTableModel DFT, int days, boolean onlineOnly, int selectedIndex, String customeFilter, String usernameFilter) throws
            SQLException {
        String SQL;

        SQL = ""
                + "SELECT "
                + " radacct.radacctid, "
                + " radacct.username, "
                + " substr(radacct.username, locate('@',radacct.username)+1) AS router, "
                + " acctsessionid AS sessionid, "
                + " acctstarttime, "
                + " acctupdatetime, "
                + " acctstoptime, "
                + " framedipaddress, "
                + " dt, "
                + " customername, "
                + " location "
                + "FROM radacct "
                + "LEFT JOIN radacct_customers_link a ON (a.radacctid = radacct.radacctid) "
                + "LEFT JOIN customers b ON (b.id = a.customerid) "
                + "WHERE "
                + " radacct.username LIKE 'dhcp_%' ";
        if (onlineOnly) {
            SQL += " AND ( acctstoptime IS NULL AND radacct.radacctid>=? ) ";
        } else {
            SQL += " AND ( acctstoptime IS NULL OR DATE_SUB(CURDATE(), INTERVAL ? DAY)<=acctstarttime ) ";
        }
        SQL += " AND ( radacct.username LIKE ? OR framedipaddress" + (selectedIndex == 0 ? " LIKE " : "=") + "? ) ";
        if (customeFilter.isEmpty()) {
            SQL += " AND ( customername IS NULL OR customername LIKE ? ) ";
        } else {
            SQL += " AND ( customername IS NOT NULL AND customername LIKE ? ) ";
        }
        SQL += "ORDER BY acctstarttime, acctupdatetime, acctstoptime, router, username";

        SQL = SQL.replaceAll("\\s+", " ").trim();

//        System.err.println("SQL: " + SQL);
        //ResultSet rs = this.statement.executeQuery(SQL);
        PreparedStatement prepareStatement = this.con.prepareStatement(SQL);

        if (onlineOnly) {
            prepareStatement.setInt(1, 0);                                  // AND ( acctstoptime IS NULL AND radacct.radacctid>=? )
        } else {
            prepareStatement.setInt(1, days);                               // AND ( acctstoptime IS NULL OR DATE_SUB(CURDATE(), INTERVAL ? DAY)<=acctstarttime )
        }

        if (usernameFilter.isEmpty()) {
            prepareStatement.setString(2, "%");                             // AND ( radacct.username LIKE ? OR framedipaddress" + (selectedIndex == 0 ? " LIKE " : "=") + "? )
            prepareStatement.setString(3, "%");                             // AND ( radacct.username LIKE ? OR framedipaddress" + (selectedIndex == 0 ? " LIKE " : "=") + "? )
        } else {
            prepareStatement.setString(2, "%" + usernameFilter + "%");      // AND ( radacct.username LIKE ? OR framedipaddress" + (selectedIndex == 0 ? " LIKE " : "=") + "? )
            if (selectedIndex == 0) {
                prepareStatement.setString(3, "%" + usernameFilter + "%");  // AND ( radacct.username LIKE ? OR framedipaddress" + (selectedIndex == 0 ? " LIKE " : "=") + "? )
            } else {
                prepareStatement.setString(3, usernameFilter);              // AND ( radacct.username LIKE ? OR framedipaddress" + (selectedIndex == 0 ? " LIKE " : "=") + "? )
            }
        }

        if (customeFilter.isEmpty()) {
            prepareStatement.setString(4, "%");                             // AND ( customername IS NULL OR customername LIKE ? )
        } else {
            prepareStatement.setString(4, "%" + customeFilter + "%");       // AND ( customername IS NOT NULL AND customername LIKE ? )
        }

//        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
//            System.err.print(ste + "\n");
//        }
        /*
        System.err.println(
                Arrays.toString(Thread.currentThread().getStackTrace()).replace(',', '\n')
        );
         */
        System.err.println(prepareStatement.toString().replaceAll("^[^:]+: ", ""));

        ResultSet rs = prepareStatement.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        DFT.setRowCount(0);

        while (rs.next()) {
            Vector rowData = new Vector();
            for (int i = 1; i <= columnCount; i++) {
                rowData.add(rs.getString("radacctid"));
                rowData.add(rs.getString("username"));
                rowData.add(rs.getString("router"));
                rowData.add(rs.getString("sessionid"));
                rowData.add(rs.getString("acctstarttime"));
                rowData.add(rs.getString("acctupdatetime"));
                rowData.add(rs.getString("acctstoptime"));
                rowData.add(rs.getString("framedipaddress"));
                rowData.add(rs.getString("dt"));
                rowData.add(rs.getString("customername"));
                rowData.add(rs.getString("location"));
            }
//            Object[] rowData = new Object[columnCount];
//                rowData.add(rs.getString("radacctid"));
//                rowData.add(rs.getString("username"));
//                rowData.add(rs.getString("router"));
//                rowData.add(rs.getString("sessionid"));
//                rowData.add(rs.getString("acctstarttime"));
//                rowData.add(rs.getString("acctupdatetime"));
//                rowData.add(rs.getString("acctstoptime"));
//                rowData.add(rs.getString("framedipaddress"));
//                rowData.add(rs.getString("dt"));
//                rowData.add(rs.getString("customername"));
//                rowData.add(rs.getString("location"));
            DFT.addRow(rowData);
        }
        return this;
    }

    public radiusData getDuplicateData(DefaultTableModel DFT) throws
            SQLException {
        String SQL = "SELECT "
                + "     username, "
                + "     COUNT(*) AS count "
                + "FROM radacct "
                + "WHERE "
                + "     radacct.username LIKE 'dhcp_%' AND "
                + "     acctstoptime IS NULL "
                + "GROUP BY username "
                + "ORDER BY count DESC, username";
        SQL = SQL.replaceAll("\\s+", " ").trim();

        System.err.println(SQL);

        ResultSet rs = this.statement.executeQuery(SQL);

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        DFT.setRowCount(0);

        while (rs.next()) {
            Vector rowData = new Vector();
            boolean needAddRow = false;
            for (int i = 1; i <= columnCount; i++) {
                if (Integer.parseInt(rs.getString("count")) > 1) {
                    rowData.add(rs.getString("username"));
                    rowData.add(rs.getString("count"));
                    needAddRow = true;
                }
            }
            if (needAddRow) {
                DFT.addRow(rowData);
            }
        }

        return this;
    }

    public radiusData getDuplicateSessions(DefaultTableModel DFT, String un) throws
            SQLException {
        String SQL = "SELECT radacctid, framedipaddress, acctstarttime, acctupdatetime "
                + "FROM radacct "
                + "WHERE username=? AND acctstoptime IS NULL";
        SQL = SQL.replaceAll("\\s+", " ").trim();
        PreparedStatement prepareStatement = this.con.prepareStatement(SQL);
        prepareStatement.setString(1, un);

        System.err.println(prepareStatement.toString().replaceAll("^[^:]+: ", ""));

        ResultSet rs = prepareStatement.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        DFT.setRowCount(0);
        while (rs.next()) {
            Vector rowData = new Vector();
            for (int i = 1; i <= columnCount; i++) {
                rowData.add(rs.getString("radacctid"));
                rowData.add(rs.getString("framedipaddress"));
                rowData.add(rs.getString("acctstarttime"));
                rowData.add(rs.getString("acctupdatetime"));
            }
            DFT.addRow(rowData);
        }

        return this;
    }

    public radiusData getAcctStopTimeCandidate(DefaultTableModel DFT, String st, String un, String ip) throws
            SQLException {
        String SQL = "( "
                + " SELECT DATE_SUB(acctstarttime, INTERVAL 1 SECOND) AS acctstoptime_candidate "
                + "     FROM radacct "
                + "     WHERE username=? AND  acctstarttime>? ORDER BY acctstarttime,acctupdatetime,acctstoptime LIMIT 1 "
                + ") UNION ( "
                + " SELECT DATE_SUB(acctstarttime, INTERVAL 1 SECOND) AS acctstoptime_candidate "
                + "     FROM radacct "
                + "     WHERE framedipaddress=? AND acctstarttime>? ORDER BY acctstarttime,acctupdatetime,acctstoptime LIMIT 1 "
                + ") ORDER BY acctstoptime_candidate LIMIT 1;";
        SQL = SQL.replaceAll("\\s+", " ").trim();
        PreparedStatement prepareStatement = this.con.prepareStatement(SQL);
        prepareStatement.setString(1, un);
        prepareStatement.setString(2, st);
        prepareStatement.setString(3, ip);
        prepareStatement.setString(4, st);

        System.err.println(prepareStatement.toString().replaceAll("^[^:]+: ", ""));

        ResultSet rs = prepareStatement.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        DFT.setRowCount(0);
        while (rs.next()) {
            Vector rowData = new Vector();
            rowData.add(st);
            for (int i = 1; i <= columnCount; i++) {
                rowData.add(rs.getString("acctstoptime_candidate"));
            }
            DFT.addRow(rowData);
        }

        return this;
    }

    public radiusData correctionAcctStopTime(Long id, String st) throws
            SQLException {
        String SQL = "UPDATE radacct SET acctstoptime=? WHERE radacctid=?";
        PreparedStatement prepareStatement = this.con.prepareStatement(SQL);
        prepareStatement.setString(1, st);
        prepareStatement.setLong(2, id);

        System.err.println(prepareStatement.toString().replaceAll("^[^:]+: ", ""));

        prepareStatement.executeUpdate();

        return this;
    }

}

/*
CREATE USER 'radsel'@'94.125.120.5' IDENTIFIED BY 'YmE0OTA0MDBiZTVjZDUzMWE4Mjc2ZTM5NTYyYTZlYzNlY2Y1Y2IzYWQ5N2Q4';
GRANT SELECT,UPDATE ON radius.* TO 'radsel'@'94.125.120.5';
FLUSH PRIVILEGES;
 */
