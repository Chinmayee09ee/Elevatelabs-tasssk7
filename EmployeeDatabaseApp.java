import java.sql.*;
import java.util.Scanner;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Employee Model Class - represents the database table structure
class Employee {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private double salary;
    private LocalDate hireDate;
    
    // Default constructor
    public Employee() {}
    
    // Constructor without ID (for new employees)
    public Employee(String firstName, String lastName, String email, String department, double salary, LocalDate hireDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.hireDate = hireDate;
    }
    
    // Constructor with ID (for existing employees)
    public Employee(int id, String firstName, String lastName, String email, String department, double salary, LocalDate hireDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.hireDate = hireDate;
    }
    
    // Getters and Setters
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
        return String.format("ID: %d | %s %s | %s | %s | $%.2f | Hired: %s", 
            id, firstName, lastName, email, department, salary, hireDate);
    }
}

// Database Connection Utility Class - demonstrates JDBC DriverManager
class DatabaseConnection {
    // Database configuration - supports both MySQL and PostgreSQL
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/employee_db";
    private static final String POSTGRESQL_URL = "jdbc:postgresql://localhost:5432/employee_db";
    private static final String USERNAME = "root"; // Change as needed
    private static final String PASSWORD = "password"; // Change as needed
    
    // Database type selection
    private static final String DB_TYPE = "mysql"; // Change to "postgresql" if using PostgreSQL
    
    // Get database connection - demonstrates how to connect Java to MySQL/PostgreSQL
    public static Connection getConnection() throws SQLException {
        try {
            // Load the appropriate JDBC driver
            if (DB_TYPE.equals("mysql")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(MYSQL_URL, USERNAME, PASSWORD);
            } else {
                Class.forName("org.postgresql.Driver");
                return DriverManager.getConnection(POSTGRESQL_URL, USERNAME, PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found: " + e.getMessage());
        }
    }
    
    // Test database connection
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Database connection successful!");
            System.out.println("Database: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("Version: " + conn.getMetaData().getDatabaseProductVersion());
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed: " + e.getMessage());
            return false;
        }
    }
}

// Employee DAO (Data Access Object) Class - demonstrates CRUD operations
class EmployeeDAO {
    
    // CREATE - Add new employee (demonstrates PreparedStatement and SQL Injection prevention)
    public boolean addEmployee(Employee employee) {
        String sql = "INSERT INTO employees (first_name, last_name, email, department, salary, hire_date) VALUES (?, ?, ?, ?, ?, ?)";
        
        // Try-with-resources automatically closes Connection, PreparedStatement - demonstrates how to close connections
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Set parameters using PreparedStatement - prevents SQL injection
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getEmail());
            stmt.setString(4, employee.getDepartment());
            stmt.setDouble(5, employee.getSalary());
            stmt.setDate(6, Date.valueOf(employee.getHireDate()));
            
            int rowsAffected = stmt.executeUpdate();
            
            // Get the generated ID
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        employee.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("✅ Employee added successfully with ID: " + employee.getId());
                return true;
            }
            
        } catch (SQLException e) {
            // Demonstrates how to handle SQL exceptions
            System.out.println("❌ Error adding employee: " + e.getMessage());
            handleSQLException(e);
        }
        return false;
    }
    
    // READ - Get all employees (demonstrates ResultSet usage)
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) { // Demonstrates ResultSet
            
            // Process ResultSet - demonstrates how to work with ResultSet
            while (rs.next()) {
                Employee emp = new Employee(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("department"),
                    rs.getDouble("salary"),
                    rs.getDate("hire_date").toLocalDate()
                );
                employees.add(emp);
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving employees: " + e.getMessage());
            handleSQLException(e);
        }
        return employees;
    }
    
    // READ - Get employee by ID
    public Employee getEmployeeById(int id) {
        String sql = "SELECT * FROM employees WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving employee: " + e.getMessage());
            handleSQLException(e);
        }
        return null;
    }
    
    // UPDATE - Update employee information
    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE employees SET first_name = ?, last_name = ?, email = ?, department = ?, salary = ?, hire_date = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getEmail());
            stmt.setString(4, employee.getDepartment());
            stmt.setDouble(5, employee.getSalary());
            stmt.setDate(6, Date.valueOf(employee.getHireDate()));
            stmt.setInt(7, employee.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Employee updated successfully!");
                return true;
            } else {
                System.out.println("❌ No employee found with ID: " + employee.getId());
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error updating employee: " + e.getMessage());
            handleSQLException(e);
        }
        return false;
    }
    
    // DELETE - Delete employee by ID
    public boolean deleteEmployee(int id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Employee deleted successfully!");
                return true;
            } else {
                System.out.println("❌ No employee found with ID: " + id);
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error deleting employee: " + e.getMessage());
            handleSQLException(e);
        }
        return false;
    }
    
    // Search employees by department
    public List<Employee> getEmployeesByDepartment(String department) {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE department LIKE ? ORDER BY last_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + department + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Employee emp = new Employee(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getDouble("salary"),
                        rs.getDate("hire_date").toLocalDate()
                    );
                    employees.add(emp);
                }
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error searching employees: " + e.getMessage());
            handleSQLException(e);
        }
        return employees;
    }
    
    // Demonstrate transaction handling and auto-commit
    public boolean bulkUpdateSalary(String department, double increasePercentage) {
        String sql = "UPDATE employees SET salary = salary * (1 + ?) WHERE department = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Demonstrate auto-commit control
            conn.setAutoCommit(false); // Start transaction
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, increasePercentage / 100.0);
                stmt.setString(2, department);
                
                int rowsAffected = stmt.executeUpdate();
                
                // Commit the transaction
                conn.commit();
                System.out.println("✅ Salary updated for " + rowsAffected + " employees in " + department + " department");
                return true;
                
            } catch (SQLException e) {
                // Rollback on error
                conn.rollback();
                System.out.println("❌ Transaction rolled back due to error: " + e.getMessage());
                throw e;
            } finally {
                conn.setAutoCommit(true); // Reset auto-commit
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error in bulk salary update: " + e.getMessage());
            handleSQLException(e);
        }
        return false;
    }
    
    // SQL Exception handling method
    private void handleSQLException(SQLException e) {
        System.out.println("SQL Error Details:");
        System.out.println("Error Code: " + e.getErrorCode());
        System.out.println("SQL State: " + e.getSQLState());
        System.out.println("Message: " + e.getMessage());
    }
    
    // Create table if it doesn't exist
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS employees (
                id INT AUTO_INCREMENT PRIMARY KEY,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                department VARCHAR(50) NOT NULL,
                salary DECIMAL(10, 2) NOT NULL,
                hire_date DATE NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.executeUpdate();
            System.out.println("✅ Employee table ready!");
            
        } catch (SQLException e) {
            System.out.println("❌ Error creating table: " + e.getMessage());
            handleSQLException(e);
        }
    }
}

// Main Application Class
public class EmployeeDatabaseApp {
    private static EmployeeDAO employeeDAO = new EmployeeDAO();
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("🏢 ELEVATE LABS - EMPLOYEE DATABASE MANAGEMENT SYSTEM");
        System.out.println("=====================================================");
        System.out.println("Demonstrating Java JDBC concepts:");
        System.out.println("• JDBC Connection");
        System.out.println("• PreparedStatement (SQL Injection Prevention)");
        System.out.println("• ResultSet Processing");
        System.out.println("• CRUD Operations");
        System.out.println("• Exception Handling");
        System.out.println("• Transaction Management");
        
        // Test database connection
        if (!DatabaseConnection.testConnection()) {
            System.out.println("\n❌ Please check your database configuration and try again.");
            System.out.println("Make sure MySQL/PostgreSQL is running and credentials are correct.");
            return;
        }
        
        // Create table if needed
        employeeDAO.createTableIfNotExists();
        
        // Main application loop
        boolean running = true;
        while (running) {
            showMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1 -> addEmployee();
                case 2 -> viewAllEmployees();
                case 3 -> viewEmployeeById();
                case 4 -> updateEmployee();
                case 5 -> deleteEmployee();
                case 6 -> searchEmployeesByDepartment();
                case 7 -> bulkSalaryUpdate();
                case 8 -> showDatabaseInfo();
                case 9 -> {
                    System.out.println("👋 Thank you for using Employee Database App!");
                    running = false;
                }
                default -> System.out.println("❌ Invalid choice. Please try again.");
            }
            
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
        
        scanner.close();
    }
    
    private static void showMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📋 EMPLOYEE MANAGEMENT MENU");
        System.out.println("=".repeat(50));
        System.out.println("1. ➕ Add New Employee");
        System.out.println("2. 👥 View All Employees");
        System.out.println("3. 🔍 View Employee by ID");
        System.out.println("4. ✏️  Update Employee");
        System.out.println("5. 🗑️  Delete Employee");
        System.out.println("6. 🔎 Search by Department");
        System.out.println("7. 💰 Bulk Salary Update");
        System.out.println("8. 📊 Database Information");
        System.out.println("9. 🚪 Exit");
        System.out.println("=".repeat(50));
    }
    
    private static void addEmployee() {
        System.out.println("\n➕ ADD NEW EMPLOYEE");
        System.out.println("-".repeat(30));
        
        try {
            String firstName = getStringInput("First Name: ");
            String lastName = getStringInput("Last Name: ");
            String email = getStringInput("Email: ");
            String department = getStringInput("Department: ");
            double salary = getDoubleInput("Salary: $");
            
            System.out.print("Hire Date (YYYY-MM-DD) [Press Enter for today]: ");
            String dateInput = scanner.nextLine().trim();
            LocalDate hireDate = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput);
            
            Employee employee = new Employee(firstName, lastName, email, department, salary, hireDate);
            employeeDAO.addEmployee(employee);
            
        } catch (Exception e) {
            System.out.println("❌ Error adding employee: " + e.getMessage());
        }
    }
    
    private static void viewAllEmployees() {
        System.out.println("\n👥 ALL EMPLOYEES");
        System.out.println("-".repeat(80));
        
        List<Employee> employees = employeeDAO.getAllEmployees();
        if (employees.isEmpty()) {
            System.out.println("No employees found in the database.");
        } else {
            System.out.println("Total Employees: " + employees.size());
            System.out.println();
            for (Employee emp : employees) {
                System.out.println(emp);
            }
        }
    }
    
    private static void viewEmployeeById() {
        System.out.println("\n🔍 VIEW EMPLOYEE BY ID");
        System.out.println("-".repeat(30));
        
        int id = getIntInput("Enter Employee ID: ");
        Employee employee = employeeDAO.getEmployeeById(id);
        
        if (employee != null) {
            System.out.println("\nEmployee Details:");
            System.out.println(employee);
        } else {
            System.out.println("❌ No employee found with ID: " + id);
        }
    }
    
    private static void updateEmployee() {
        System.out.println("\n✏️ UPDATE EMPLOYEE");
        System.out.println("-".repeat(30));
        
        int id = getIntInput("Enter Employee ID to update: ");
        Employee employee = employeeDAO.getEmployeeById(id);
        
        if (employee == null) {
            System.out.println("❌ No employee found with ID: " + id);
            return;
        }
        
        System.out.println("Current Details: " + employee);
        System.out.println("\nEnter new values (press Enter to keep current value):");
        
        try {
            String firstName = getStringInputWithDefault("First Name", employee.getFirstName());
            String lastName = getStringInputWithDefault("Last Name", employee.getLastName());
            String email = getStringInputWithDefault("Email", employee.getEmail());
            String department = getStringInputWithDefault("Department", employee.getDepartment());
            double salary = getDoubleInputWithDefault("Salary", employee.getSalary());
            
            System.out.print("Hire Date (" + employee.getHireDate() + "): ");
            String dateInput = scanner.nextLine().trim();
            LocalDate hireDate = dateInput.isEmpty() ? employee.getHireDate() : LocalDate.parse(dateInput);
            
            employee.setFirstName(firstName);
            employee.setLastName(lastName);
            employee.setEmail(email);
            employee.setDepartment(department);
            employee.setSalary(salary);
            employee.setHireDate(hireDate);
            
            employeeDAO.updateEmployee(employee);
            
        } catch (Exception e) {
            System.out.println("❌ Error updating employee: " + e.getMessage());
        }
    }
    
    private static void deleteEmployee() {
        System.out.println("\n🗑️ DELETE EMPLOYEE");
        System.out.println("-".repeat(30));
        
        int id = getIntInput("Enter Employee ID to delete: ");
        Employee employee = employeeDAO.getEmployeeById(id);
        
        if (employee == null) {
            System.out.println("❌ No employee found with ID: " + id);
            return;
        }
        
        System.out.println("Employee to delete: " + employee);
        String confirm = getStringInput("Are you sure you want to delete this employee? (yes/no): ");
        
        if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
            employeeDAO.deleteEmployee(id);
        } else {
            System.out.println("❌ Deletion cancelled.");
        }
    }
    
    private static void searchEmployeesByDepartment() {
        System.out.println("\n🔎 SEARCH BY DEPARTMENT");
        System.out.println("-".repeat(30));
        
        String department = getStringInput("Enter department name (partial match supported): ");
        List<Employee> employees = employeeDAO.getEmployeesByDepartment(department);
        
        if (employees.isEmpty()) {
            System.out.println("No employees found in department containing: " + department);
        } else {
            System.out.println("Found " + employees.size() + " employee(s):");
            System.out.println();
            for (Employee emp : employees) {
                System.out.println(emp);
            }
        }
    }
    
    private static void bulkSalaryUpdate() {
        System.out.println("\n💰 BULK SALARY UPDATE");
        System.out.println("-".repeat(30));
        
        String department = getStringInput("Enter department name: ");
        double percentage = getDoubleInput("Enter salary increase percentage: ");
        
        String confirm = getStringInput("Confirm salary increase of " + percentage + "% for " + department + " department? (yes/no): ");
        
        if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
            employeeDAO.bulkUpdateSalary(department, percentage);
        } else {
            System.out.println("❌ Bulk update cancelled.");
        }
    }
    
    private static void showDatabaseInfo() {
        System.out.println("\n📊 DATABASE INFORMATION");
        System.out.println("-".repeat(30));
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            System.out.println("Database Product: " + metaData.getDatabaseProductName());
            System.out.println("Database Version: " + metaData.getDatabaseProductVersion());
            System.out.println("JDBC Driver: " + metaData.getDriverName());
            System.out.println("JDBC Version: " + metaData.getJDBCMajorVersion() + "." + metaData.getJDBCMinorVersion());
            System.out.println("Connection URL: " + metaData.getURL());
            System.out.println("Username: " + metaData.getUserName());
            System.out.println("Auto Commit: " + conn.getAutoCommit());
            
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving database information: " + e.getMessage());
        }
    }
    
    // Utility methods for input handling
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    private static String getStringInputWithDefault(String fieldName, String defaultValue) {
        System.out.print(fieldName + " (" + defaultValue + "): ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }
    
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number.");
            }
        }
    }
    
    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number.");
            }
        }
    }
    
    private static double getDoubleInputWithDefault(String fieldName, double defaultValue) {
        System.out.print(fieldName + " (" + defaultValue + "): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number, keeping current value: " + defaultValue);
            return defaultValue;
        }
    }
}
