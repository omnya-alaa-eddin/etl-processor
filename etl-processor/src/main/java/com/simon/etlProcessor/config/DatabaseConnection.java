package com.simon.etlProcessor.config;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {


    private static Connection con = null;

    static {
        String url = "";
        String user = "";
        String pass = "";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            log("mysql driver registered");
        } catch (ClassNotFoundException e) {
            log("Sorry, couldn't found JDBC driver. Make sure you have added JDBC Maven Dependency Correctly");
            e.printStackTrace();

        }
        try {
            log("getting connection");
            con = DriverManager.getConnection(url, user, pass);
            log("Connect created");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Connection getConnection() {
        return con;
    }

    private static void log(String string) {
        System.out.println(string);

    }
}

