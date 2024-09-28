import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class EmployeeDatabaseApp {
    private static final String URL = "jdbc:mysql://localhost:3306/employees";
    private static final String USER = "root"; 
    private static String PASSWORD = "BigBoyServerProject2!";

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            return;
        }



        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connected to the database successfully.");
            Scanner scanner = new Scanner(System.in);
            while (true) {
                displayMenu();
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        executeQuery1(conn);
                        break;
                    case 2:
                        executeQuery2(conn);
                        break;
                    case 3:
                        executeQuery3(conn);
                        break;
                    case 4:
                        executeQuery4(conn);
                        break;
                    case 5:
                        executeQuery5(conn, scanner);
                        break;
                    case 6:
                        executeQuery6(conn, scanner);
                        break;
                    case 0:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Connection failed. Check output console.");
            e.printStackTrace();
        }
    }

    private static void displayMenu() {
        System.out.println("\nEmployee Database Queries:");
        System.out.println("1. Departments with max ratio of avg female to male salaries");
        System.out.println("2. Managers with longest office duration");
        System.out.println("3. Employees per decade and avg salaries per department");
        System.out.println("4. Female managers born before 1990 with salary > 80K");
        System.out.println("5. Find 1 degree of separation between employees");
        System.out.println("6. Find 2 degrees of separation between employees");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void executeQuery1(Connection conn) throws SQLException {
        String query = "SELECT dept_no, (AVG(CASE WHEN gender = 'F' THEN salary END) / AVG(CASE WHEN gender = 'M' THEN salary END)) AS salary_ratio " +
        "FROM employees " +
        "JOIN salaries USING(emp_no) " +
        "JOIN dept_emp USING(emp_no) " +
        "GROUP BY dept_no " +
        "ORDER BY salary_ratio DESC LIMIT 100;";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nDepartments with max ratio of avg female to male salaries:");
            System.out.printf("%-20s %-15s%n", "Department", "Salary Ratio");
            System.out.println("------------------------------------");
            while (rs.next()) {
                System.out.printf("%-20s %-15.3f%n", rs.getString("dept_no"), rs.getDouble("salary_ratio"));
            }
        }
    }

    private static void executeQuery2(Connection conn) throws SQLException {
        String query = "SELECT emp_no, SUM(DATEDIFF(to_date, from_date)) AS total_days " +
                           "FROM dept_manager " +
                           "GROUP BY emp_no " +
                           "ORDER BY total_days DESC LIMIT 100 ";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nManagers with longest office duration:");
            System.out.printf("%-15s %-15s", "Employee No", "Days in Office");
            System.out.println("--------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-15s %-15s%n", 
                    rs.getString("emp_no"), 
                    rs.getInt("total_days"));
            }
        }
    }

    private static void executeQuery3(Connection conn) throws SQLException {
        String query = "SELECT dept_no, FLOOR(YEAR(birth_date)/10)*10 AS decade, COUNT(*) AS num_employees, AVG(salary) AS avg_salary " +
                           "FROM employees " +
                           "JOIN dept_emp USING(emp_no) " +
                           "JOIN salaries USING(emp_no) " +
                           "GROUP BY dept_no, decade LIMIT 100;";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

        }
    }

    private static void executeQuery4(Connection conn) throws SQLException {
        String query = "SELECT DISTINCT e.emp_no, e.first_name, e.last_name, e.birth_date " +
                           "FROM employees e " +
                           "JOIN dept_manager dm ON e.emp_no = dm.emp_no " +
                           "JOIN salaries s ON e.emp_no = s.emp_no " +
                           "WHERE e.gender = 'F' " +
                           "AND e.birth_date < '1990-01-01' " +
                           "AND s.salary > 80000 LIMIT 100;";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nFemale managers born before 1990 with salary > 80K:");
            System.out.printf("%-15s %-15s %-15s %-15s%n", "Employee No", "First Name", "Last Name", "Birth Date");
            System.out.println("------------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-15s %-15s %-15s %15s %n", 
                    rs.getString("emp_no"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getDate("birth_date"));
            }
        }
    }

    private static void executeQuery5(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter first employee ID: ");
        int emp1 = scanner.nextInt();
        System.out.print("Enter second employee ID: ");
        int emp2 = scanner.nextInt();

        String query = "SELECT DISTINCT d1.dept_no " +
                           "FROM dept_emp d1 " +
                           "JOIN dept_emp d2 ON d1.dept_no = d2.dept_no " +
                           "WHERE d1.emp_no = " + emp1 + " AND d2.emp_no = " + emp2 + " " +
                           "AND d1.from_date <= d2.to_date AND d1.to_date >= d2.from_date;";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, emp1);
            pstmt.setInt(2, emp2);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n1 degree of separation results:");
                System.out.printf("%-10s %-15s %-15s %-10s %-15s %-15s %-10s %-20s%n", 
                    "Emp1 ID", "First Name", "Last Name", "Emp2 ID", "First Name", "Last Name", "Dept No", "Department");
                System.out.println("--------------------------------------------------------------------------------------------------");
                boolean found = false;
                while (rs.next()) {
                    System.out.printf("%-10d %-15s %-15s %-10d %-15s %-15s %-10s %-20s%n", 
                        rs.getInt("emp1_no"), rs.getString("emp1_fname"), rs.getString("emp1_lname"),
                        rs.getInt("emp2_no"), rs.getString("emp2_fname"), rs.getString("emp2_lname"),
                        rs.getString("dept_no"), rs.getString("dept_name"));
                    found = true;
                }
                if (!found) {
                    System.out.println("No 1 degree of separation found between the given employees.");
                }
            }
        }
    }

    private static void executeQuery6(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter first employee ID: ");
        int emp1 = scanner.nextInt();
        System.out.print("Enter second employee ID: ");
        int emp2 = scanner.nextInt();

        String query = "SELECT DISTINCT d1.dept_no AS dept1, d2.dept_no AS dept2 " +
                           "FROM dept_emp d1 " +
                           "JOIN dept_emp d2 ON d1.emp_no = d2.emp_no " +
                           "JOIN dept_emp d3 ON d2.emp_no = d3.emp_no " +
                           "WHERE d1.emp_no = " + emp1 + " AND d3.emp_no = " + emp2 + " " +
                           "AND d1.from_date <= d2.to_date AND d1.to_date >= d2.from_date " +
                           "AND d2.from_date <= d3.to_date AND d2.to_date >= d3.from_date;";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, emp1);
            pstmt.setInt(2, emp2);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n2 degrees of separation results:");
                System.out.printf("%-10s %-15s %-15s %-10s %-15s %-15s %-10s %-15s %-15s %-10s %-20s %-10s %-20s%n", 
                    "Emp1 ID", "First Name", "Last Name", 
                    "Emp3 ID", "First Name", "Last Name", 
                    "Emp2 ID", "First Name", "Last Name", 
                    "Dept1 No", "Department 1", "Dept2 No", "Department 2");
                System.out.println("----------------------------------------------------------------------------------------------------------------------------");
                boolean found = false;
                while (rs.next()) {
                    System.out.printf("%-10d %-15s %-15s %-10d %-15s %-15s %-10d %-15s %-15s %-10s %-20s %-10s %-20s%n", 
                        rs.getInt("emp1_no"), rs.getString("emp1_fname"), rs.getString("emp1_lname"),
                        rs.getInt("emp3_no"), rs.getString("emp3_fname"), rs.getString("emp3_lname"),
                        rs.getInt("emp2_no"), rs.getString("emp2_fname"), rs.getString("emp2_lname"),
                        rs.getString("dept1_no"), rs.getString("dept1_name"),
                        rs.getString("dept2_no"), rs.getString("dept2_name"));
                    found = true;
                }
                if (!found) {
                    System.out.println("No 2 degrees of separation found between the given employees.");
                }
            }
        }
    }
}