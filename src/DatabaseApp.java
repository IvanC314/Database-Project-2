import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class DatabaseApp {
    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/employees";
    private static final String USER = "root";
    private static String PASS = "password";

    public static void main(String[] args) {
        System.out.println("Enter your password: ");
        Scanner scanner = new Scanner(System.in);
        PASS = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("Connected to the database!");

            while (true) {
                System.out.println("\nChoose an option:");
                System.out.println("1. List department(s) with max ratio of average female salaries to average men salaries");
                System.out.println("2. List manager(s) who hold office for the longest duration");
                System.out.println("3. List number of employees born in each decade with average salaries per department");
                System.out.println("4. List female employees born before 1990 earning more than 80K annually and holding a manager position");
                System.out.println("5. Find 1 degree of separation between two employees");
                System.out.println("6. Find 2 degrees of separation between two employees");
                System.out.println("7. Find employees with exactly X degrees of separation from an employee");
                System.out.println("0. Exit");

                //int choice = scanner.nextInt();
                int option = getOption(scanner);

                scanner.nextLine();  // consume newline

                switch (option) {
                    case 1:
                        listMaxRatioDepartments(conn);
                        break;
                    case 2:
                        listLongestServingManagers(conn);
                        break;
                    case 3:
                        listEmployeesByDecade(conn);
                        break;
                    case 4:
                        listHighEarningFemaleManagers(conn);
                        break;
                    case 5:
                        findOneDegreeSeparation(conn, scanner);
                        break;
                    case 6:
                        findTwoDegreesSeparation(conn, scanner);
                        break;
                    case 7:
                        findEmployeesWithDegreeSeparation(conn, scanner);
                        break;
                    case 0:
                        System.out.println("Exiting.");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getOption(Scanner scanner) {
        int option = -1;
        while (option < 0) {
            System.out.print("Choose an option: ");
            if (scanner.hasNextInt()) {
                option = scanner.nextInt();
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();  // Clear invalid input
            }
        }
        return option;
    }

    private static void listMaxRatioDepartments(Connection conn) throws SQLException {
        String sql = "SELECT dept_no, MAX(female_avg_salary / male_avg_salary) AS max_ratio " +
                     "FROM ( " +
                     "    SELECT d.dept_no, " +
                     "           AVG(CASE WHEN e.gender = 'F' THEN s.salary END) AS female_avg_salary, " +
                     "           AVG(CASE WHEN e.gender = 'M' THEN s.salary END) AS male_avg_salary " +
                     "    FROM employees e " +
                     "    JOIN salaries s ON e.emp_no = s.emp_no " +
                     "    JOIN dept_emp d ON e.emp_no = d.emp_no " +
                     "    GROUP BY d.dept_no " +
                     ") AS salary_ratios " +
                     "GROUP BY dept_no " +
                     "ORDER BY max_ratio DESC LIMIT 100;";
    
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("+----------------+--------------+");
                System.out.println("| Department No  | Max Ratio    |");
                System.out.println("+----------------+--------------+");
    
                while (rs.next()) {
                    String deptNo = rs.getString("dept_no");
                    double maxRatio = rs.getDouble("max_ratio");
                    System.out.printf("| %-14s | %-12.3f |\n", deptNo, maxRatio);
                }
                System.out.println("+----------------+--------------+");
            }
        }
    }
    

    // 2. List managers with longest duration
    private static void listLongestServingManagers(Connection conn) throws SQLException {
        String sql = "SELECT emp_no, SUM(TIMESTAMPDIFF(DAY, from_date, to_date)) AS total_days FROM dept_manager " +
                     "GROUP BY emp_no ORDER BY total_days DESC LIMIT 100;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                System.out.println("Manager Emp No: " + rs.getInt("emp_no") + ", Total Days: " + rs.getInt("total_days"));
            }
        }
    }

    // 3. List employees by decade and average salary per department
    private static void listEmployeesByDecade(Connection conn) throws SQLException {
        String sql = "SELECT dept_no, FLOOR(YEAR(birth_date) / 10) * 10 AS decade, COUNT(*) AS employee_count, " +
                     "AVG(salary) AS avg_salary FROM employees " +
                     "JOIN salaries USING(emp_no) JOIN dept_emp USING(emp_no) " +
                     "GROUP BY dept_no, decade LIMIT 100;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql); 
        ResultSet rs = pstmt.executeQuery()) {
            System.out.println("+----------------+-----------+----------------+------------+"); 
            System.out.println("| Department     | Decade    | Num Employees  | Avg Salary |");
            System.out.println("+----------------+-----------+----------------+------------+"); 
            while (rs.next()) {
                System.out.printf("| %-14s | %-9s | %-14s | %-10.2f |%n", 
                    rs.getString("dept_no"),
                    rs.getInt("decade"), rs.getInt("employee_count"),
                    rs.getDouble("avg_salary"));
            }
            System.out.println("+----------------+-----------+----------------+------------+"); 

        }
    }

    // 4. List high-earning female managers born before 1990
    private static void listHighEarningFemaleManagers(Connection conn) throws SQLException {
        String sql = "SELECT DISTINCT emp_no FROM employees " +
                     "JOIN salaries USING(emp_no) " +
                     "JOIN dept_manager USING(emp_no) " +
                     "WHERE gender = 'F' AND birth_date < '1990-01-01' AND salary > 80000 LIMIT 100;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                System.out.println("Female Manager Emp No: " + rs.getInt("emp_no"));
            }
        }
    }

    // Add this method to check if an employee exists
    private static boolean employeeExists(Connection conn, int empId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE emp_no = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, empId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;  // Returns true if employee exists
                }
            }
        }
        return false;
    }

    // Modify findOneDegreeSeparation method
    private static void findOneDegreeSeparation(Connection conn, Scanner scanner) throws SQLException {
        int e1 = getEmployeeNumber(scanner, "E1");
        int e2 = getEmployeeNumber(scanner, "E2");

        // Check if both employees exist
        if (!employeeExists(conn, e1)) {
            System.out.println("Employee " + e1 + " does not exist.");
            return;
        }
        if (!employeeExists(conn, e2)) {
            System.out.println("Employee " + e2 + " does not exist.");
            return;
        }

        String sql = "SELECT D1.dept_no FROM dept_emp D1 JOIN dept_emp D2 ON D1.dept_no = D2.dept_no " +
                    "WHERE D1.emp_no = ? AND D2.emp_no = ? AND D1.to_date = D2.to_date;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, e1);
            pstmt.setInt(2, e2);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("E1 and E2 worked in department " + rs.getString("dept_no") + " at the same time.");
                } else {
                    System.out.println("No 1 degree of separation found.");
                }
            }
        }
    }


    private static int getEmployeeNumber(Scanner scanner, String employeeName) {
        int empNo = -1;
        while (empNo < 0) {
            System.out.print("Enter Emp No for " + employeeName + ": ");
            if (scanner.hasNextInt()) {
                empNo = scanner.nextInt();
            } else {
                System.out.println("Invalid input. Please enter a valid employee number.");
                scanner.next();  // Clear invalid input
            }
        }
        return empNo;
    }

    // Modify findTwoDegreesSeparation method
    private static void findTwoDegreesSeparation(Connection conn, Scanner scanner) throws SQLException {
        int e1 = getEmployeeNumber(scanner, "E1");
        int e2 = getEmployeeNumber(scanner, "E2");

        // Check if both employees exist
        if (!employeeExists(conn, e1)) {
            System.out.println("Employee " + e1 + " does not exist.");
            return;
        }
        if (!employeeExists(conn, e2)) {
            System.out.println("Employee " + e2 + " does not exist.");
            return;
        }

        String sql = "SELECT D1.dept_no, D2.dept_no FROM dept_emp D1 " +
                    "JOIN dept_emp D2 ON D1.emp_no = D2.emp_no " +
                    "WHERE D1.emp_no = ? AND D2.emp_no = ? AND D1.to_date = D2.to_date;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, e1);
            pstmt.setInt(2, e2);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("E1 and E2 have 2 degrees of separation between department " +
                            rs.getString("D1.dept_no") + " and " + rs.getString("D2.dept_no"));
                } else {
                    System.out.println("No 2 degrees of separation found.");
                }
            }
        }
    }


    // Function to find employees with exactly N degrees of separation
    private static void findEmployeesWithDegreeSeparation(Connection conn, Scanner scanner) throws SQLException {
        int e1 = getEmployeeNumber(scanner, "E1");
        int degree = getDegreeOfSeparation(scanner);

        // SQL query to find employees with exactly N degrees of separation
        String sql = "WITH RECURSIVE EmployeeHierarchy AS (" +
                    "    SELECT dept_emp.emp_no, dept_emp.dept_no, 0 AS level " +
                    "    FROM dept_emp " +
                    "    WHERE dept_emp.emp_no = ? " +
                    "    UNION ALL " +
                    "    SELECT d2.emp_no, d2.dept_no, eh.level + 1 " +
                    "    FROM dept_emp d2 " +
                    "    JOIN EmployeeHierarchy eh ON d2.dept_no = eh.dept_no AND d2.emp_no <> eh.emp_no " +
                    "    WHERE eh.level < ? " +
                    ") " +
                    "SELECT DISTINCT emp_no FROM EmployeeHierarchy WHERE level = ? LIMIT 100;";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, e1);
            pstmt.setInt(2, degree);
            pstmt.setInt(3, degree);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.println("Employee ID: " + rs.getInt("emp_no"));
                    count++;
                }

                if (count == 0) {
                    System.out.println("No employees found with exactly " + degree + " degrees of separation.");
                } else if (count == 100) {
                    System.out.println("Only the first 100 employees are displayed.");
                }
            }
        }
    }

    // Helper function to get degree of separation from the user
    private static int getDegreeOfSeparation(Scanner scanner) {
        int degree = -1;
        while (degree < 0) {
            System.out.print("Enter the degree of separation (must be 0 or greater): ");
            if (scanner.hasNextInt()) {
                degree = scanner.nextInt();
                if (degree < 0) {
                    System.out.println("Invalid input. Degree must be 0 or greater.");
                }
            } else {
                System.out.println("Invalid input. Please enter a valid integer.");
                scanner.next();  // Clear invalid input
            }
        }
        return degree;
    }


}
