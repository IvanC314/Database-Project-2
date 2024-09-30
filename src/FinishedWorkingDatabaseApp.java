import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FinishedWorkingDatabaseApp {
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
                System.out.println("7. List all employees two degrees away from a given employee");
                System.out.println("0. Exit");

                int option = getOption(scanner);
                scanner.nextLine();  // consume newline

                switch (option) {
                    case 1: listMaxRatioDepartments(conn); break;
                    case 2: listLongestServingManagers(conn); break;
                    case 3: listEmployeesByDecade(conn); break;
                    case 4: listHighEarningFemaleManagers(conn); break;
                    case 5: findOneDegreeSeparation(conn, scanner); break;
                    case 6: findTwoDegreesSeparation(conn, scanner); break;
                    case 7: findAllTwoDegreesAway(conn, scanner); break;
                    case 0: System.out.println("Exiting."); return;
                    default: System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    private static int getOption(Scanner scanner) {
        int option = -1;
        while (option < 0 || option > 7) {
            System.out.print("Choose an option (0-7): ");
            if (scanner.hasNextInt()) {
                option = scanner.nextInt();
                if (option < 0 || option > 7) {
                    System.out.println("Invalid option. Please enter a number between 0 and 7.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();  // Clear invalid input
            }
        }
        return option;
    }

    private static void findAllTwoDegreesAway(Connection conn, Scanner scanner) throws SQLException {
        String employeeName = getEmployeeName(scanner, "the employee");
    
        List<Integer> employeeIds = getEmployeeIds(conn, employeeName);
    
        if (employeeIds.isEmpty()) {
            System.out.println("Employee not found.");
            return;
        }
    
        int employeeId = chooseEmployeeId(scanner, employeeName, employeeIds);
    
        String sql = "SELECT DISTINCT " +
                     "e1.first_name AS input_first, e1.last_name AS input_last, " +
                     "d1.dept_name AS dept1, " +
                     "e2.first_name AS intermediate_first, e2.last_name AS intermediate_last, " +
                     "d2.dept_name AS dept2, " +
                     "e3.first_name AS output_first, e3.last_name AS output_last " +
                     "FROM employees e1 " +
                     "JOIN dept_emp de1 ON e1.emp_no = de1.emp_no " +
                     "JOIN departments d1 ON de1.dept_no = d1.dept_no " +
                     "JOIN dept_emp de2 ON d1.dept_no = de2.dept_no " +
                     "JOIN employees e2 ON de2.emp_no = e2.emp_no " +
                     "JOIN dept_emp de3 ON e2.emp_no = de3.emp_no " +
                     "JOIN departments d2 ON de3.dept_no = d2.dept_no " +
                     "JOIN dept_emp de4 ON d2.dept_no = de4.dept_no " +
                     "JOIN employees e3 ON de4.emp_no = e3.emp_no " +
                     "WHERE e1.emp_no = ? " +
                     "AND e1.emp_no != e2.emp_no AND e2.emp_no != e3.emp_no AND e1.emp_no != e3.emp_no " +
                     "AND ((de1.from_date <= de2.to_date AND de1.to_date >= de2.from_date) " +
                     "     OR (de2.from_date <= de1.to_date AND de2.to_date >= de1.from_date)) " +
                     "AND ((de3.from_date <= de4.to_date AND de3.to_date >= de4.from_date) " +
                     "     OR (de4.from_date <= de3.to_date AND de4.to_date >= de3.from_date)) " +
                     "LIMIT 1000";
    
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean found = false;
                System.out.println("+--------------------+--------------------+------------------------+--------------------+--------------------+------------------------+--------------------+--------------------+");
                System.out.println("| Input First Name   | Input Last Name    | Department 1           | Intermediate First | Intermediate Last  | Department 2           | Output First Name  | Output Last Name   |");
                System.out.println("+--------------------+--------------------+------------------------+--------------------+--------------------+------------------------+--------------------+--------------------+");
                while (rs.next()) {
                    found = true;
                    String inputFirst = rs.getString("input_first");
                    String inputLast = rs.getString("input_last");
                    String dept1 = rs.getString("dept1");
                    String intermediateFirst = rs.getString("intermediate_first");
                    String intermediateLast = rs.getString("intermediate_last");
                    String dept2 = rs.getString("dept2");
                    String outputFirst = rs.getString("output_first");
                    String outputLast = rs.getString("output_last");
                    System.out.printf("| %-18s | %-18s | %-22s | %-18s | %-18s | %-22s | %-18s | %-18s |\n",
                        inputFirst, inputLast, dept1, intermediateFirst, intermediateLast, dept2, outputFirst, outputLast);
                }
                System.out.println("+--------------------+--------------------+------------------------+--------------------+--------------------+------------------------+--------------------+--------------------+");
                if (!found) {
                    System.out.println("No employees found two degrees away from the input employee.");
                }
            }
        }
    }

    private static void listMaxRatioDepartments(Connection conn) throws SQLException {
        String sql = "SELECT d.dept_name, COALESCE(f.avg_salary / m.avg_salary, 0) AS ratio " +
                     "FROM departments d " +
                     "LEFT JOIN (SELECT de.dept_no, AVG(s.salary) AS avg_salary " +
                     "           FROM dept_emp de " +
                     "           JOIN employees e ON de.emp_no = e.emp_no " +
                     "           JOIN salaries s ON e.emp_no = s.emp_no " +
                     "           WHERE e.gender = 'F' AND (de.to_date = '9999-01-01' OR de.to_date > CURDATE()) " +
                     "           AND (s.to_date = '9999-01-01' OR s.to_date > CURDATE()) " +
                     "           GROUP BY de.dept_no) f ON d.dept_no = f.dept_no " +
                     "LEFT JOIN (SELECT de.dept_no, AVG(s.salary) AS avg_salary " +
                     "           FROM dept_emp de " +
                     "           JOIN employees e ON de.emp_no = e.emp_no " +
                     "           JOIN salaries s ON e.emp_no = s.emp_no " +
                     "           WHERE e.gender = 'M' AND (de.to_date = '9999-01-01' OR de.to_date > CURDATE()) " +
                     "           AND (s.to_date = '9999-01-01' OR s.to_date > CURDATE()) " +
                     "           GROUP BY de.dept_no) m ON d.dept_no = m.dept_no " +
                     "WHERE f.avg_salary IS NOT NULL AND m.avg_salary IS NOT NULL " +
                     "ORDER BY ratio DESC " +
                     "LIMIT 100";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("+------------------------+--------------+");
            System.out.println("| Department             | Salary Ratio |");
            System.out.println("+------------------------+--------------+");
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String deptName = rs.getString("dept_name");
                double ratio = rs.getDouble("ratio");
                System.out.printf("| %-22s | %-12.3f |\n", deptName, ratio);
            }
            System.out.println("+------------------------+--------------+");
            if (!hasResults) {
                System.out.println("No departments found with the specified criteria.");
            }
        }
    }

    private static void listLongestServingManagers(Connection conn) throws SQLException {
        String sql = "SELECT e.first_name, e.last_name, d.dept_name, " +
                     "       DATEDIFF(LEAST(dm.to_date, '9999-01-01'), dm.from_date) AS days_served " +
                     "FROM dept_manager dm " +
                     "JOIN employees e ON dm.emp_no = e.emp_no " +
                     "JOIN departments d ON dm.dept_no = d.dept_no " +
                     "ORDER BY days_served DESC " +
                     "LIMIT 100";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("+---------------+---------------+------------------------+-------------+");
            System.out.println("| First Name    | Last Name     | Department             | Days Served |");
            System.out.println("+---------------+---------------+------------------------+-------------+");
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String deptName = rs.getString("dept_name");
                int daysServed = rs.getInt("days_served");
                System.out.printf("| %-13s | %-13s | %-22s | %-11d |\n", firstName, lastName, deptName, daysServed);
            }
            System.out.println("+---------------+---------------+------------------------+-------------+");
            if (!hasResults) {
                System.out.println("No managers found with the specified criteria.");
            }
        }
    }

    private static void listEmployeesByDecade(Connection conn) throws SQLException {
        String sql = "SELECT d.dept_name, " +
                     "       CONCAT(FLOOR(YEAR(e.birth_date) / 10) * 10, 's') AS decade, " +
                     "       COUNT(*) AS employee_count, " +
                     "       AVG(s.salary) AS avg_salary " +
                     "FROM employees e " +
                     "JOIN dept_emp de ON e.emp_no = de.emp_no " +
                     "JOIN departments d ON de.dept_no = d.dept_no " +
                     "JOIN salaries s ON e.emp_no = s.emp_no " +
                     "WHERE (de.to_date = '9999-01-01' OR de.to_date > CURDATE()) " +
                     "  AND (s.to_date = '9999-01-01' OR s.to_date > CURDATE()) " +
                     "GROUP BY d.dept_name, decade " +
                     "ORDER BY d.dept_name, decade " +
                     "LIMIT 100";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("+------------------------+---------+----------------+---------------+");
            System.out.println("| Department             | Decade  | Employee Count | Avg Salary    |");
            System.out.println("+------------------------+---------+----------------+---------------+");
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String deptName = rs.getString("dept_name");
                String decade = rs.getString("decade");
                int count = rs.getInt("employee_count");
                double avgSalary = rs.getDouble("avg_salary");
                System.out.printf("| %-22s | %-7s | %-14d | $%-12.2f |\n", deptName, decade, count, avgSalary);
            }
            System.out.println("+------------------------+---------+----------------+---------------+");
            if (!hasResults) {
                System.out.println("No employees found with the specified criteria.");
            }
        }
    }

    private static void listHighEarningFemaleManagers(Connection conn) throws SQLException {
        String sql = "SELECT DISTINCT e.first_name, e.last_name, d.dept_name, s.salary " +
                     "FROM employees e " +
                     "JOIN dept_manager dm ON e.emp_no = dm.emp_no " +
                     "JOIN departments d ON dm.dept_no = d.dept_no " +
                     "JOIN salaries s ON e.emp_no = s.emp_no " +
                     "WHERE e.gender = 'F' " +
                     "  AND e.birth_date < '1990-01-01' " +
                     "  AND s.salary > 80000 " +
                     "  AND (dm.to_date = '9999-01-01' OR dm.to_date > CURDATE()) " +
                     "  AND (s.to_date = '9999-01-01' OR s.to_date > CURDATE()) " +
                     "ORDER BY s.salary DESC " +
                     "LIMIT 100";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("+---------------+---------------+------------------------+---------------+");
            System.out.println("| First Name    | Last Name     | Department             | Salary        |");
            System.out.println("+---------------+---------------+------------------------+---------------+");
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String deptName = rs.getString("dept_name");
                double salary = rs.getDouble("salary");
                System.out.printf("| %-13s | %-13s | %-22s | $%-12.2f |\n", firstName, lastName, deptName, salary);
            }
            System.out.println("+---------------+---------------+------------------------+---------------+");
            if (!hasResults) {
                System.out.println("No employees found with the specified criteria.");
            }
        }
    }

    private static void findOneDegreeSeparation(Connection conn, Scanner scanner) throws SQLException {
        String e1Name = getEmployeeName(scanner, "E1");
        String e2Name = getEmployeeName(scanner, "E2");

        List<Integer> e1Ids = getEmployeeIds(conn, e1Name);
        List<Integer> e2Ids = getEmployeeIds(conn, e2Name);

        if (e1Ids.isEmpty() || e2Ids.isEmpty()) {
            System.out.println("One or both employees not found.");
            return;
        }

        int e1Id = chooseEmployeeId(scanner, e1Name, e1Ids);
        int e2Id = chooseEmployeeId(scanner, e2Name, e2Ids);

        String sql = "SELECT DISTINCT d.dept_name " +
                     "FROM dept_emp de1 " +
                     "JOIN dept_emp de2 ON de1.dept_no = de2.dept_no " +
                     "JOIN departments d ON de1.dept_no = d.dept_no " +
                     "WHERE de1.emp_no = ? AND de2.emp_no = ? " +
                     "  AND ((de1.from_date <= de2.to_date AND de1.to_date >= de2.from_date) " +
                     "       OR (de2.from_date <= de1.to_date AND de2.to_date >= de1.from_date)) " +
                     "LIMIT 100";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, e1Id);
            pstmt.setInt(2, e2Id);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    if (!found) {
                        System.out.println("Employees have 1 degree of separation:");
                        found = true;
                    }
                    System.out.println("- " + rs.getString("dept_name"));
                }
                if (!found) {
                    System.out.println("No 1 degree of separation found between the employees.");
                }
            }
        }
    }


    // The new reformatted version of the function
    private static void findTwoDegreesSeparation(Connection conn, Scanner scanner) throws SQLException {
        String e1Name = getEmployeeName(scanner, "E1");
        String e2Name = getEmployeeName(scanner, "E2");
    
        List<Integer> e1Ids = getEmployeeIds(conn, e1Name);
        List<Integer> e2Ids = getEmployeeIds(conn, e2Name);
    
        if (e1Ids.isEmpty() || e2Ids.isEmpty()) {
            System.out.println("One or both employees not found.");
            return;
        }
    
        int e1Id = chooseEmployeeId(scanner, e1Name, e1Ids);
        int e2Id = chooseEmployeeId(scanner, e2Name, e2Ids);
    
        String sql = "SELECT DISTINCT " +
                     "e1.first_name AS e1_first, e1.last_name AS e1_last, " +
                     "d1.dept_name AS dept1, d1.dept_no AS dept1no," +
                     "e_int.first_name AS int_first, e_int.last_name AS int_last, e_int.emp_no AS e_int," +
                     "d2.dept_name AS dept2, d2.dept_no AS dept2no, " +
                     "e2.first_name AS e2_first, e2.last_name AS e2_last " +
                     "FROM employees e1 " +
                     "JOIN dept_emp de1 ON e1.emp_no = de1.emp_no " +
                     "JOIN departments d1 ON de1.dept_no = d1.dept_no " +
                     "JOIN dept_emp de_int ON d1.dept_no = de_int.dept_no " +
                     "JOIN employees e_int ON de_int.emp_no = e_int.emp_no " +
                     "JOIN dept_emp de_int2 ON e_int.emp_no = de_int2.emp_no " +
                     "JOIN departments d2 ON de_int2.dept_no = d2.dept_no " +
                     "JOIN dept_emp de2 ON d2.dept_no = de2.dept_no " +
                     "JOIN employees e2 ON de2.emp_no = e2.emp_no " +
                     "WHERE e1.emp_no = ? AND e2.emp_no = ? " +
                     "AND e1.emp_no != e_int.emp_no AND e_int.emp_no != e2.emp_no " +
                     "AND d1.dept_no != d2.dept_no " + 
                     "AND ((de1.from_date <= de_int.to_date AND de1.to_date >= de_int.from_date) " +
                     "     OR (de_int.from_date <= de1.to_date AND de_int.to_date >= de1.from_date)) " +
                     "AND ((de_int2.from_date <= de2.to_date AND de_int2.to_date >= de2.from_date) " +
                     "     OR (de2.from_date <= de_int2.to_date AND de2.to_date >= de_int2.from_date)) " +
                     "LIMIT 100";
    
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, e1Id);
            pstmt.setInt(2, e2Id);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean found = false;
                System.out.println("+--------------------------+---------------------+-----------------+---------------------------+---------------------+--------------------------+");
                System.out.println("| E1 Name                  | Department 1        | Intermediary No |     Intermediary Name     | Department 2        | E2 Name                  |");
                System.out.println("+--------------------------+---------------------+-----------------+---------------------------+---------------------+--------------------------+");
                while (rs.next()) {
                    found = true;
                    String e1First = rs.getString("e1_first");
                    String e1Last = rs.getString("e1_last");
                    String e1String = e1First + " " + e1Last;
                    String dept1 = rs.getString("dept1");
                    String dept1no = rs.getString("dept1no");
                    String d1String = dept1no + " " + dept1;
                    String intNo = rs.getString("e_int");
                    String intFirst = rs.getString("int_first");
                    String intLast = rs.getString("int_last");
                    String intString = intFirst + " " +intLast;
                    String dept2 = rs.getString("dept2");
                    String dept2no = rs.getString("dept2no");
                    String d2String = dept2no + " " +dept2;
                    String e2First = rs.getString("e2_first");
                    String e2Last = rs.getString("e2_last");
                    String e2String = e2First + " " +e2Last;
                    System.out.printf("| %-24s | %-19s | %-15s | %-25s | %-19s | %-24s |\n",
                        e1String, d1String, intNo, intString, d2String, e2String);
                }
                System.out.println("+--------------------------+---------------------+-----------------+---------------------------+---------------------+--------------------------+");
                if (!found) {
                    System.out.println("No 2 degrees of separation found between the employees.");
                }
            }
        }
    } // Reformatted version

    private static String getEmployeeName(Scanner scanner, String employeeLabel) {
        System.out.print("Enter the name of " + employeeLabel + ": ");
        return scanner.nextLine().trim();
    }

    private static List<Integer> getEmployeeIds(Connection conn, String name) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT emp_no FROM employees WHERE CONCAT(first_name, ' ', last_name) LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("emp_no"));
                }
            }
        }
        return ids;
    }

    private static int chooseEmployeeId(Scanner scanner, String name, List<Integer> ids) throws SQLException {
        if (ids.size() == 1) {
            return ids.get(0);
        }
        System.out.println("Multiple employees found with the name '" + name + "'. Please choose one:");
        for (int i = 0; i < ids.size(); i++) {
            System.out.println((i + 1) + ". Employee ID: " + ids.get(i));
        }
        int choice = -1;
        while (choice < 1 || choice > ids.size()) {
            System.out.print("Enter your choice (1-" + ids.size() + "): ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                if (choice < 1 || choice > ids.size()) {
                    System.out.println("Invalid choice. Please try again.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();  // Clear invalid input
            }
        }
        scanner.nextLine();  // Consume newline
        return ids.get(choice - 1);
    }
}