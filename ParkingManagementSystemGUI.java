
package myapp.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;

public class ParkingManagementSystemGUI extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/parking_management";
    private static final String DB_USER = "newuser";
    private static final String DB_PASSWORD = "password";
    private static final String VEHICLE_REGEX = "^[A-Za-z]{2}-\\d{2}-[A-Za-z]{1,2}-\\d{4}$";
    private static final DateTimeFormatter MYSQL_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private JTextField vehicleNumberField, vehicleTypeField, vehicleModelField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private String userId;

    private JButton registerButton, loginButton, addVehicleButton, removeVehicleButton, viewParkedVehiclesButton, logoutButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ParkingManagementSystemGUI frame = new ParkingManagementSystemGUI();
            frame.setVisible(true);
        });
    }

    public ParkingManagementSystemGUI() {
        setTitle("Parking Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        BackgroundPanel backgroundPanel = new BackgroundPanel(new ImageIcon("C:\\Users\\ANAND\\Documents\\APP\\Project\\Parking Management.png").getImage());

        JPanel menuPanel = new JPanel(new GridLayout(6, 1, 10, 10));  // 6 rows for buttons
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        menuPanel.setOpaque(false);

        // Initialize buttons
        registerButton = createCustomButton("Register");
        loginButton = createCustomButton("Login");
        addVehicleButton = createCustomButton("Add Vehicle");
        removeVehicleButton = createCustomButton("Remove Vehicle");
        viewParkedVehiclesButton = createCustomButton("View Parked Vehicles");
        logoutButton = createCustomButton("Logout");  // New logout button

        // Add only the Register and Login buttons initially
        menuPanel.add(registerButton);
        menuPanel.add(loginButton);

        // Add other buttons but set them invisible initially
        menuPanel.add(addVehicleButton);
        menuPanel.add(removeVehicleButton);
        menuPanel.add(viewParkedVehiclesButton);
        menuPanel.add(logoutButton);

        addVehicleButton.setVisible(false);
        removeVehicleButton.setVisible(false);
        viewParkedVehiclesButton.setVisible(false);
        logoutButton.setVisible(false);

        // Add the menu panel to the background panel
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.add(menuPanel, BorderLayout.WEST);

        // Add the background panel to the frame
        add(backgroundPanel);

        // Button actions
        registerButton.addActionListener(e -> showRegistrationForm());
        loginButton.addActionListener(e -> showLoginForm());
        addVehicleButton.addActionListener(e -> {
            if (userId != null) {
                showInsertVehicleForm();
            } else {
                showMessage("Please log in first.", "Login Required", JOptionPane.WARNING_MESSAGE);
            }
        });
        removeVehicleButton.addActionListener(e -> {
            if (userId != null) {
                removeVehicleForm();
            } else {
                showMessage("Please log in first.", "Login Required", JOptionPane.WARNING_MESSAGE);
            }
        });
        viewParkedVehiclesButton.addActionListener(e -> {
            if (userId != null) {
                viewParkedVehicles();
            } else {
                showMessage("Please log in first.", "Login Required", JOptionPane.WARNING_MESSAGE);
            }
        });
        logoutButton.addActionListener(e -> logout());  // Logout action
    }

    // Custom background panel class
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(Image backgroundImage) {
            this.backgroundImage = backgroundImage;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // Method to create a custom button
    private JButton createCustomButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.BLUE);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("TIMES NEW ROMAN", Font.BOLD, 20));
        button.setFocusPainted(false);
        return button;
    }

    // Updated User Registration Form
    private void showRegistrationForm() {
        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.setPreferredSize(new Dimension(400, 250));

        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        JCheckBox showPasswordCheckbox = new JCheckBox("Show Password");

        showPasswordCheckbox.addActionListener(e -> {
            if (showPasswordCheckbox.isSelected()) {
                passwordField.setEchoChar((char) 0); // Show password
            } else {
                passwordField.setEchoChar('•'); // Hide password
            }
        });

        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Phone Number:"));
        panel.add(phoneField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(showPasswordCheckbox);

        int result = JOptionPane.showConfirmDialog(null, panel, "Register", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String fullName = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                showMessage("All fields are required.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } else {
                registerUser(fullName, phone, email, username, password);
            }
        }
    }

    // User Login Form
    private void showLoginForm() {
        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.setPreferredSize(new Dimension(400, 200));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        JCheckBox showPasswordCheckbox = new JCheckBox("Show Password");

        showPasswordCheckbox.addActionListener(e -> {
            if (showPasswordCheckbox.isSelected()) {
                passwordField.setEchoChar((char) 0); // Show password
            } else {
                passwordField.setEchoChar('•'); // Hide password
            }
        });

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(showPasswordCheckbox);

        int result = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showMessage("Username and Password cannot be empty.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } else {
                loginUser(username, password);
            }
        }
    }

    // Login method that makes the other buttons visible after a successful login
    private void loginUser(String username, String password) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT user_id, password FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedHashedPassword = resultSet.getString("password");
                userId = resultSet.getString("user_id");
                if (BCrypt.checkpw(password, storedHashedPassword)) {
                    showMessage("Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Show additional buttons for signed-in users
                    addVehicleButton.setVisible(true);
                    removeVehicleButton.setVisible(true);
                    viewParkedVehiclesButton.setVisible(true);
                    logoutButton.setVisible(true);
                    registerButton.setVisible(false);
                    loginButton.setVisible(false);
                } else {
                    showMessage("Invalid password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                showMessage("User not found.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to register a new user
    private void registerUser(String fullName, String phone, String email, String username, String password) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            String userId = generateUserId(connection);

            String query = "INSERT INTO users (user_id, full_name, phone, email, username, password) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, userId);
                statement.setString(2, fullName);
                statement.setString(3, phone);
                statement.setString(4, email);
                statement.setString(5, username);
                statement.setString(6, hashedPassword);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    showMessage("Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showMessage("Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            showMessage("Error registering user: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Method to generate unique user ID
    private String generateUserId(Connection connection) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM users";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                String prefix = "A";
                return prefix + String.format("%03d", count + 1);  // Format to A001, A002, ...
            }
        }
        return null;
    }

    // Show a message dialog
    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    // Show Insert Vehicle Form
    private void showInsertVehicleForm() {
        JPanel panel = new JPanel(new GridLayout(8, 2));
        panel.setPreferredSize(new Dimension(500, 250));

        vehicleNumberField = new JTextField();
        vehicleTypeField = new JTextField();
        vehicleModelField = new JTextField();

        panel.add(new JLabel("<html>Vehicle Number (Format: XX-YY-Z-1234 or XX-YY-ZZ-1234):<html>"));
        panel.add(vehicleNumberField);
        panel.add(new JLabel("Vehicle Type:"));
        panel.add(vehicleTypeField);
        panel.add(new JLabel("Vehicle Model:"));
        panel.add(vehicleModelField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Vehicle", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String vehicleNumber = vehicleNumberField.getText().trim();
            String vehicleType = vehicleTypeField.getText().trim();
            String vehicleModel = vehicleModelField.getText().trim();

            if (vehicleNumber.isEmpty() || vehicleType.isEmpty() || vehicleModel.isEmpty()) {
                showMessage("All fields are required.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } else if (!Pattern.matches(VEHICLE_REGEX, vehicleNumber)) {
                showMessage("Invalid vehicle number format. Use format: XX-YY-ZZ-1234", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } else {
                insertVehicle(vehicleNumber, vehicleType, vehicleModel);
            }
        }
    }


    // Method to insert a vehicle into the database
    private void insertVehicle(String vehicleNumber, String vehicleType, String vehicleModel) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Get the owner's name based on the logged-in user's ID
            String query = "SELECT full_name FROM users WHERE user_id = ?";
            PreparedStatement nameStatement = connection.prepareStatement(query);
            nameStatement.setString(1, userId);
            ResultSet nameResultSet = nameStatement.executeQuery();

            String ownerName = null;
            if (nameResultSet.next()) {
                ownerName = nameResultSet.getString("full_name");
            } else {
                showMessage("Could not retrieve owner name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String insertQuery = "INSERT INTO vehicles (user_id, vehicle_number, owner_name, vehicle_type, vehicle_model, entry_time) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(insertQuery);
            statement.setString(1, userId);
            statement.setString(2, vehicleNumber);
            statement.setString(3, ownerName);
            statement.setString(4, vehicleType);
            statement.setString(5, vehicleModel);
            statement.setString(6, LocalDateTime.now().format(MYSQL_TIMESTAMP_FORMATTER)); // Current time

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                showMessage("Vehicle added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            showMessage("Error adding vehicle.", "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    //Show Vehicle Remove Form
    private void removeVehicleForm() {
        JTextField vehicleNumberField = new JTextField();
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.setPreferredSize(new Dimension(200, 50));
        panel.add(new JLabel("Enter Vehicle Number to Remove:"));
        panel.add(vehicleNumberField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Remove Vehicle", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String vehicleNumber = vehicleNumberField.getText().trim();
            if (vehicleNumber.isEmpty()) {
                showMessage("Vehicle number cannot be empty.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } else {
                removeVehicle(vehicleNumber);
            }
        }
    }

    // Method to remove a vehicle from the database
    private void removeVehicle(String vehicleNumber) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT entry_time, vehicle_type FROM vehicles WHERE vehicle_number = ? AND user_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, vehicleNumber);
            statement.setString(2, userId);  // Ensure the user can only remove their own vehicles
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                LocalDateTime parkedTime = resultSet.getTimestamp("entry_time").toLocalDateTime();
                String vehicleType = resultSet.getString("vehicle_type");

                // Calculate the total time parked
                long totalMinutes = ChronoUnit.MINUTES.between(parkedTime, LocalDateTime.now());

                // Subtract the first hour (60 minutes) for free parking
                long billableMinutes = Math.max(0, totalMinutes - 60);

                // Determine per-minute rate based on vehicle type
                double perMinuteRate = 0;
                if ("bike".equalsIgnoreCase(vehicleType)) {
                    perMinuteRate = 0.42; // ₹25 per hour -> ₹0.42 per minute
                } else if ("car".equalsIgnoreCase(vehicleType)) {
                    perMinuteRate = 0.83; // ₹50 per hour -> ₹0.83 per minute
                }

                // Calculate the bill amount for the billable minutes
                BigDecimal billAmount = BigDecimal.valueOf(billableMinutes * perMinuteRate)
                        .setScale(2, RoundingMode.HALF_UP); // Round to 2 decimal places

                showMessage("Vehicle removed successfully!\nTotal Bill: ₹" + billAmount, "Success", JOptionPane.INFORMATION_MESSAGE);

                // Remove the vehicle from the database
                String deleteQuery = "DELETE FROM vehicles WHERE vehicle_number = ? AND user_id = ?";
                PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
                deleteStatement.setString(1, vehicleNumber);
                deleteStatement.setString(2, userId);
                deleteStatement.executeUpdate();
            } else {
                showMessage("Vehicle not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            showMessage("Error removing vehicle: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Method to view parked vehicles
    private void viewParkedVehicles() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT vehicle_number, vehicle_type, vehicle_model, entry_time FROM vehicles WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();

            // Create a table to display parked vehicles
            String[] columnNames = {"Vehicle Number", "Vehicle Type", "Vehicle Model", "Parked Time"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);

            while (resultSet.next()) {
                String vehicleNumber = resultSet.getString("vehicle_number");
                String vehicleType = resultSet.getString("vehicle_type");
                String vehicleModel = resultSet.getString("vehicle_model");
                String parkedTime = resultSet.getString("entry_time");
                model.addRow(new Object[]{vehicleNumber, vehicleType, vehicleModel, parkedTime});
            }

            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(600, 200));

            JOptionPane.showMessageDialog(this, scrollPane, "Parked Vehicles", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showMessage("Error retrieving parked vehicles.", "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Logout method
    private void logout() {
        userId = null;
        addVehicleButton.setVisible(false);
        removeVehicleButton.setVisible(false);
        viewParkedVehiclesButton.setVisible(false);
        logoutButton.setVisible(false);
        registerButton.setVisible(true);
        loginButton.setVisible(true);
        showMessage("You have been logged out.", "Logout", JOptionPane.INFORMATION_MESSAGE);
    }
}
