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

            System.out.println("\nPlease enter your password");
            String password = reader.readLine();

            try {
                conn = DriverManager.getConnection("jdbc:oracle:thin:@//oracle.cs.ua.edu:1521/xe", username, password);
                System.out.println("\nConnection Established successfully");
                connected = true;
            } catch (SQLException e) {
                System.out.println("\nIncorrect credentials. Would you like to try again? (yes/no)");
                String choice = reader.readLine();
                
                if (!choice.equalsIgnoreCase("yes")) {
                    System.out.println("Exiting program.");
                    System.exit(0);
                }
            }
        }

        while(!userDisconnect) {
            System.out.println("\nPlease enter an employee SSN or Project Number. To exit, type '0'");
            String input = reader.readLine();

            if (isNumeric(input)) { // Close the program
                if (input.equals("0")) {
                    userDisconnect = true;
                    System.out.println("\nExiting program. Goodbye!");
                    conn.close();
                    System.exit(0);
                } else if (input.length() == 9) { // SSN Search
                    ResultSet rs = getEmployeeInfo(conn, input);
                    if (rs == null) {
                        System.out.println("\nInvalid SSN. Please try again.");
                    } else {
                        parseAndPrintEmployee(rs);
                    }
                } else { // Project Number Search
                    System.out.println("Project Number search not yet implemented.");
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

    private static ResultSet getEmployeeInfo(Connection conn, String ssn) {
        try {
            String query = "SELECT e1.Fname AS EmployeeFName, "
            + "e1.Minit AS EmployeeMInit, "
            + "e1.Lname AS EmployeeLName, "
            + "e2.Fname AS SupervisorFName, "
            + "e2.Minit AS SupervisorMInit, "
            + "e2.Lname AS SupervisorLName, "
            + "e3.Fname AS DepartmentManagerFName, "
            + "e3.Minit AS DepartmentManagerMInit, "
            + "e3.Lname AS DepartmentManagerLName, "
            + "(SELECT COUNT(*) FROM Dependent WHERE Dependent.Essn = e1.SSN) AS NumberOfDependents, "
            + "p.Pname AS ProjectName, "
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
            stmt.setString(1, ssn);
            ResultSet rs = stmt.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String buildFullName(String fName, String mInit, String lName) {
        if (fName != null) {
            String fullName = fName;
            if (mInit != null && !mInit.isEmpty()) {
                fullName += " " + mInit + ".";
            }
            if (lName != null && !lName.isEmpty()) {
                fullName += " " + lName;
            }
            return fullName;
        } else {
            return "None";
        }
    }

    private static void parseAndPrintEmployee(ResultSet rs) {
        try {
            String employeeFName = null;
            String employeeMInit = null;
            String employeeLName = null;
            String supervisorFName = null;
            String supervisorMInit = null;
            String supervisorLName = null;
            String departmentManagerFName = null;
            String departmentManagerMInit = null;
            String departmentManagerLName = null;
            ArrayList<String> projects = new ArrayList<String>();
            int numberOfDependents = 0;

            while (rs.next()) {
                if (employeeFName == null) {
                    employeeFName = rs.getString("EmployeeFName");
                    employeeMInit = rs.getString("EmployeeMInit");
                    employeeLName = rs.getString("EmployeeLName");
                    supervisorFName = rs.getString("SupervisorFName");
                    supervisorMInit = rs.getString("SupervisorMInit");
                    supervisorLName = rs.getString("SupervisorLName");
                    departmentManagerFName = rs.getString("DepartmentManagerFName");
                    departmentManagerMInit = rs.getString("DepartmentManagerMInit");
                    departmentManagerLName = rs.getString("DepartmentManagerLName");
                    numberOfDependents = rs.getInt("NumberOfDependents");
                }

                String projectName = rs.getString("ProjectName");
                String projectNumber = rs.getString("ProjectNumber");
                if (projectName != null && projectNumber != null) {
                    projects.add(projectName + " (" + projectNumber + ")");
                }
            }

            String employeeName = buildFullName(employeeFName, employeeMInit, employeeLName);
            String supervisorName = buildFullName(supervisorFName, supervisorMInit, supervisorLName);
            String departmentManagerName = buildFullName(departmentManagerFName, departmentManagerMInit, departmentManagerLName);

            System.out.println("\nEmployee Name: " + employeeName);
            System.out.println("Supervisor: " + supervisorName);
            System.out.println("Department Manager: " + departmentManagerName);
            System.out.println("Number of Dependents: " + numberOfDependents);
            System.out.println("Projects: ");
            for (String project : projects) {
                System.out.println("    " + project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error processing employee data.");
        }
    }
}
