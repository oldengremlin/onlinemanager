/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.ukrcom.onlinemanager;

import java.io.Serializable;

/**
 *
 * @author olden
 */
public class jConfig implements Serializable {

    protected String serverName;
    protected String databaseName;
    protected String username;
    protected String password;

    public jConfig() {
    }

    public jConfig(String sn, String db, String un, String pw) {
        this.serverName = sn;
        this.databaseName = db;
        this.username = un;
        this.password = pw;
    }

    public String getServerName() {
        return this.serverName;
    }

    public void setServerName(String sn) {
        this.serverName = sn;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(String db) {
        this.databaseName = db;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String un) {
        this.username = un;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String pw) {
        this.password = pw;
    }

}
