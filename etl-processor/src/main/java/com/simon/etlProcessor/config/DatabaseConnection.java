package com.simon.etlProcessor.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {


    private static Connection con = null;

    static {

        try {
            InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties");
            Properties prop = new Properties();
            prop.load(input);
            String url = prop.getProperty("url");
            String user = prop.getProperty("user");
            String pass = prop.getProperty("pass");
            String driver = prop.getProperty("driver");
            Class.forName(driver);
            log("mysql driver registered");
            log("getting connection");
            con = DriverManager.getConnection(url, user, pass);
            log("Connect created");

        } 
        catch (ClassNotFoundException | SQLException | IOException e) {
            log("Sorry, couldn't found JDBC driver. Make sure you have added JDBC Maven Dependency Correctly");
            e.printStackTrace();

        }


    }

    public static Connection getConnection() {
        return con;
    }

    private static void log(String string) {
        System.out.println(string);

    }

    public static void main(String[] args) {

    }
}

