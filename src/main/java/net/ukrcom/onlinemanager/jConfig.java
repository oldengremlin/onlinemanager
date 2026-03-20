/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.ukrcom.onlinemanager;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author olden
 */
public class jConfig implements Serializable {

    private String serverName;
    private String databaseName;
    private String username;
    private String password;
    private String restserverName;
    private String restusername;
    private String restpassword;

    private String restApiUrl;

    public jConfig() {
    }

    public jConfig(String sn, String db, String un, String pw, String rsn, String run, String rpw) {
        this.serverName = sn;
        this.databaseName = db;
        this.username = un;
        this.password = pw;
        this.restserverName = rsn;
        this.restusername = run;
        this.restpassword = rpw;
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

    public String getRestServerName() {
        return this.restserverName;
    }

    public void setRestServerName(String sn) {
        this.restserverName = sn;
        this.restApiUrl = "https://".concat(sn);
    }

    public String getRestUsername() {
        return this.restusername;
    }

    public void setRestUsername(String un) {
        this.restusername = un;
    }

    public String getRestPassword() {
        return this.restpassword;
    }

    public void setRestPassword(String pw) {
        this.restpassword = pw;
    }

    public String getRestApiUrl() {
        return restApiUrl;
    }

    public void setRestApiUrl(String url) throws URISyntaxException {
        this.restApiUrl = url;
        this.restserverName = new URI(this.restApiUrl).getHost();
    }

}
