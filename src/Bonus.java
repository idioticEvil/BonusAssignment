import java.sql.*;
import java.io.*;

public class Bonus {
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean connected = false;
        boolean userDisconnect = false;
        Connection conn = null;

        while (!connected) {
            System.out.println("Welcome, please enter your username");
            String username = reader.readLine();

            System.out.println("Please enter your password");
            String password = reader.readLine();

            try {
                conn = DriverManager.getConnection("jdbc:oracle:thin:@//oracle.cs.ua.edu:1521/xe", username, password);
                System.out.println("Connection Established successfully");
                connected = true;
            } catch (SQLException e) {
                System.out.println("Incorrect credentials. Would you like to try again? (yes/no)");
                String choice = reader.readLine();
                
                if (!choice.equalsIgnoreCase("yes")) {
                    System.out.println("Exiting program.");
                    System.exit(0);
                }
            }
        }

        while(!userDisconnect) {
            Statement stmt = conn.createStatement();
            System.out.println("Please enter an employee SSN or Project Number. To exit, type '0'");
            String input = reader.readLine();

            if (input.equals("0")) {
                userDisconnect = true;
                System.out.println("Exiting program.");
                conn.close();
                System.exit(0);
            }
        }
    }
}
