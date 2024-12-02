import java.sql.*;
import java.io.*;

public class Bonus {
    public static void main(String[] args) throws Exception {
        System.out.println("Connecting to database...");
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@//oracle.cs.ua.edu:1521/xe", "wwmink", "Gumcat11.exe");
        System.out.println("Connection Established successfully");
    }
}
