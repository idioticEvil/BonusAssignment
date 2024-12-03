import java.sql.*;
import java.io.*;
import java.util.*;

public class Bonus {
    public static void main(String[] args) throws Exception {
        // reader is used to read input from the user
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        // connected is used to determine program state during login & connection
        boolean connected = false;
        // userDisconnect is used to close the program during normal operation
        boolean userDisconnect = false;
        // conn is the connection object to the database
        Connection conn = null;

        // Initial login and connection to the database
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
            System.out.println("Please enter an employee SSN or Project Number. To exit, type '0'");
            String input = reader.readLine();

            if (isNumeric(input)) {
                if (input.equals("0")) {
                    userDisconnect = true;
                    System.out.println("Exiting program.");
                    conn.close();
                    System.exit(0);
                } else if (input.length() == 9) { // SSN Search
                    String query = "SELECT e1.Fname || ' ' || e1.Minit || ' ' || e1.Lname AS EmployeeName, "
                    + "e2.Fname || ' ' || e2.Minit || ' ' || e2.Lname AS SupervisorName, "
                    + "e3.Fname || ' ' || e3.Minit || ' ' || e3.Lname AS DepartmentManagerName, "
                    + "(SELECT COUNT(*) FROM Dependent WHERE Dependent.Essn = e1.SSN) AS NumberOfDependents, "
                    + "p.Pname AS ProjectName,"
                    + "p.Pnumber AS ProjectNumber "
                    + "FROM Employee e1 "
                    + "LEFT JOIN Employee e2 ON e1.SUPERSSN = e2.SSN "
                    + "LEFT JOIN Department d ON e1.Dno = d.Dnumber "
                    + "LEFT JOIN Employee e3 ON d.MgrSSN = e3.SSN "
                    + "LEFT JOIN Works_On w ON e1.SSN = w.Essn "
                    + "LEFT JOIN Project p ON w.Pno = p.Pnumber "
                    + "WHERE e1.SSN = ? "
                    + "ORDER BY p.Pname";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery(query);

                } else { // Project Number Search

                }
            }
        }
    }

    private static boolean isNumeric(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false; 
            }
        }
        return true;
    }
}
