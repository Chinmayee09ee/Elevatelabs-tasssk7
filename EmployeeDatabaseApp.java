import java.sql.*;
import java.util.Scanner;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Employee model
class Employee {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private double salary;
    private LocalDate hireDate;

    public Employee() {}

    public Employee(String firstName, String lastName, String email, String department, double salary, LocalDate hireDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.hireDate = hireDate;
    }

    public Employee(int id, String firstName, String lastName, String email, String department, double salary, LocalDate hireDate) {
        this(firstName, lastName, email, department, salary, hireDate);
        this.id = id;
    }

    // Getters & Setters omitted for brevity...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    @Override
    public String toString() {
        return String.format(
            "ID: %d | %s %s | %s | %s | $%.2f | Hired: %s",
            id, firstName, lastName, email, department, salary, hireDate
        );
    }
}

// Connection utility - credentials removed
class DatabaseConnection {
    // INSERT your DB values here before running
    private static final String DB_URL = "";     
    private static final String USERNAME = "";   
    private static final String PASSWORD = "";   

    public static Connection getConnection() throws SQLException {
        // Load JDBC driver automatically based on your DB URL
        return DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Connected to: " + conn.getMetaData().getDatabaseProductName());
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Connection failed: " + e.getMessage());
            return false;
        }
    }
}

// Data Access Object
class EmployeeDAO {

    public boolean addEmployee(Employee emp) {
        String sql = "INSERT INTO employees (first_name, last_name, email, department, salary, hire_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, emp.getFirstName());
            stmt.setString(2, emp.getLastName());
            stmt.setString(3, emp.getEmail());
            stmt.setString(4, emp.getDepartment());
            stmt.setDouble(5, emp.getSalary());
            stmt.setDate(6, Date.valueOf(emp.getHireDate()));
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) emp.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return false;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return list;
    }

    public Employee getEmployeeById(int id) {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return null;
    }

    public boolean updateEmployee(Employee emp) {
        String sql = "UPDATE employees SET first_name=?, last_name=?, email=?, department=?, salary=?, hire_date=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emp.getFirstName());
            stmt.setString(2, emp.getLastName());
            stmt.setString(3, emp.getEmail());
            stmt.setString(4, emp.getDepartment());
            stmt.setDouble(5, emp.getSalary());
            stmt.setDate(6, Date.valueOf(emp.getHireDate()));
            stmt.setInt(7, emp.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return false;
    }

    public boolean deleteEmployee(int id) {
        String sql = "DELETE FROM employees WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return false;
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
            rs.getInt("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("department"),
            rs.getDouble("salary"),
            rs.getDate("hire_date").toLocalDate()
        );
    }

    private void printSQLException(SQLException ex) {
        System.out.println("SQLState: " + ex.getSQLState());
        System.out.println("Error Code: " + ex.getErrorCode());
        System.out.println("Message: " + ex.getMessage());
    }
}

// Main application
public class EmployeeDatabaseApp {
    private static final EmployeeDAO dao = new EmployeeDAO();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        if (!DatabaseConnection.testConnection()) return;

        boolean run = true;
        while (run) {
            showMenu();
            int choice = getInt("Choice: ");
            switch (choice) {
                case 1 -> add();
                case 2 -> list();
                case 3 -> view();
                case 4 -> update();
                case 5 -> delete();
                case 0 -> run = false;
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n1. Add Employee\n2. List Employees\n3. View Employee\n4. Update Employee\n5. Delete Employee\n0. Exit");
    }

    private static void add() {
        String fn = getStr("First name: ");
        String ln = getStr("Last name: ");
        String em = getStr("Email: ");
        String dept = getStr("Department: ");
        double sal = getDouble("Salary: ");
        LocalDate hireDate = LocalDate.now();
        dao.addEmployee(new Employee(fn, ln, em, dept, sal, hireDate));
    }

    private static void list() {
        dao.getAllEmployees().forEach(System.out::println);
    }

    private static void view() {
        int id = getInt("Enter ID: ");
        System.out.println(dao.getEmployeeById(id));
    }

    private static void update() {
        int id = getInt("Enter ID: ");
        Employee emp = dao.getEmployeeById(id);
        if (emp == null) return;
        String fn = getStr("First name ("+emp.getFirstName()+"): ");
        String ln = getStr("Last name ("+emp.getLastName()+"): ");
        emp.setFirstName(fn.isEmpty()? emp.getFirstName(): fn);
        emp.setLastName(ln.isEmpty()? emp.getLastName(): ln);
        dao.updateEmployee(emp);
    }

    private static void delete() {
        int id = getInt("Enter ID: ");
        dao.deleteEmployee(id);
    }

    private static String getStr(String prompt) { System.out.print(prompt); return sc.nextLine().trim(); }
    private static int getInt(String prompt) { System.out.print(prompt); return Integer.parseInt(sc.nextLine()); }
    private static double getDouble(String prompt) { System.out.print(prompt); return Double.parseDouble(sc.nextLine()); }
}
