package dao;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BaseDAO {

    public Connection con = null;
    public PreparedStatement ps = null;
    public ResultSet rs = null;
    public String xSql = null;

    // Define connection parameters
    private String database_url = "jdbc:postgresql://localhost:5432/ams";
    private String user_name = "postgres";
    private String user_password = "swp391";

    public BaseDAO() {
        try {
            // Load driver and establish connection
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(database_url, user_name, user_password);
            System.out.println("Successfully connected to PostgreSQL database server.");
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public void finalize() {
        try {
            if (con != null) {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
