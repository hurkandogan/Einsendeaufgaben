package jav12Einsendeaufgaben.frageZwei.db;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConnectionManager {

    private static Connection connectionMain = null;
    private static String mysqlJdbcDriverClass = "org.gjt.mm.mysql.Driver";
    private static String mysqlJdbcConnectionProtocol = "jdbc:mysql://";
    private static String database = "/dbdemo2";

    private static boolean logger = true;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

    public static synchronized Connection getConnection(String host, String user, String passwort) throws ClassNotFoundException, SQLException{

        Class.forName(ConnectionManager.mysqlJdbcDriverClass);
        String mysqlJdbcConnectionUrl = mysqlJdbcConnectionProtocol + host + database;
        Connection connection = getConnection(mysqlJdbcConnectionUrl, user, passwort);
        if(logger) {
            System.out.println("New connection for " + user + " created " + sdf.format(new Date()));
        }
        return connection;
    }

    public static void main(String[] args){
        try {
            Connection conn = ConnectionManager.getConnection("localhost", "demo-user", "");
        }catch(ClassNotFoundException cnfe){
            System.out.println(cnfe.getMessage());
        }catch(SQLException sqle){
            System.out.println(sqle.getMessage());
        }
    }
}
