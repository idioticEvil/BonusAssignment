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
        // username and password are used to store user input
        String password = null;
        String username = null;

        // Initial login and connection to the database
        while (!connected) {
            // Prompt user for username
            System.out.println("Welcome, please enter your username");
            username = reader.readLine();

            // Prompt user for password
            System.out.println("Please enter your password");
            password = reader.readLine();

            // Attempt to establish a database connection
            try {
                conn = DriverManager.getConnection("jdbc:oracle:thin:@//oracle.cs.ua.edu:1521/xe", username, password);
                System.out.println("\nConnection Established successfully");
                connected = true;
            } catch (SQLException e) {
                // Handle incorrect credentials
                System.out.println("\nIncorrect credentials. Would you like to try again? (yes/no)");
                String choice = reader.readLine();
                
                if (!choice.equalsIgnoreCase("yes")) {
                    // Exit the program if user chooses not to retry
                    System.out.println("Exiting program.");
                    System.exit(0);
                }
            }
        }

        // Main program loop for handling user queries
        while(!userDisconnect) {
            // Prompt user for input
            System.out.println("\nPlease enter an employee SSN or Project Number. To exit, type '0'");
            String input = reader.readLine();

            // Check if input is numeric
            if (isNumeric(input)) {
                if (input.equals("0")) {
                    // User chooses to exit the program
                    conn.close();
                    userDisconnect = true;
                    System.out.println("\nExiting program. Goodbye!");
                } else if (input.length() == 9) {
                    // Handle SSN search
                    ResultSet rs = getEmployeeInfo(conn, input);
                    if (rs == null) {
                        System.out.println("\nInvalid input. Please try again.");
                    } else {
                        parseAndPrintEmployee(rs);
                    }
                } else {
                    // Handle Project Number search
                    ResultSet rs = getProjectInfo(conn, input);
                    if (rs == null) {
                        System.out.println("\nInvalid input. Please try again.");
                    } else {
                        parseAndPrintProject(rs);
                    }
                }
            }
        }
    }

    // Retrieves employee information from the database based on SSN
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
            + "ORDER BY p.Pnumber";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, ssn);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) { // No rows found
                rs.close();
                stmt.close();
                return null;
            }

            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Retrieves project information from the database based on project number
    private static ResultSet getProjectInfo(Connection conn, String projectNumber) {
        try {
            String query = "SELECT p.Pname AS ProjectName, " 
            + "d.Dname AS DepartmentName, " 
            + "e.Fname AS EmployeeFirstName, " 
            + "e.Minit AS EmployeeMiddleInitial, " 
            + "e.Lname AS EmployeeLastName " 
            + "FROM Project p " 
            + "LEFT JOIN Department d ON p.Dnum = d.Dnumber " 
            + "LEFT JOIN Works_On w ON p.Pnumber = w.Pno " 
            + "LEFT JOIN Employee e ON w.Essn = e.SSN " 
            + "WHERE p.Pnumber = ? " 
            + "ORDER BY e.Fname, e.Lname";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, projectNumber);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) { // No rows found
                rs.close();
                stmt.close();
                return null;
            }

            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Parses and prints employee information from the ResultSet
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

    // Parses and prints project information from the ResultSet
    private static void parseAndPrintProject(ResultSet rs) {
        try {
            String projectName = null;
            String departmentName = null;
            ArrayList<String> employees = new ArrayList<String>();

            while (rs.next()) {
                if (projectName == null) {
                    projectName = rs.getString("ProjectName");
                    departmentName = rs.getString("DepartmentName");
                }

                String employeeName = buildFullName(rs.getString("EmployeeFirstName"), rs.getString("EmployeeMiddleInitial"), rs.getString("EmployeeLastName"));
                if (employeeName != null) {
                    employees.add(employeeName);
                }
            }

            System.out.println("\nProject Name: " + projectName);
            System.out.println("Controlling Department: " + departmentName);
            System.out.println("List of Employees Involved: ");
            for (String employee : employees) {
                System.out.println("    " + employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error processing project data.");
        }
    }

    // Checks if a string contains only numeric digits
    private static boolean isNumeric(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false; 
            }
        }
        return true;
    }

    // Builds a full name from first name, middle initial, and last name
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
}
