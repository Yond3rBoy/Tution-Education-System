import java.awt.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private int loginAttempts = 0;
    private static final int MAX_ATTEMPTS = 3;

    public LoginFrame() {
        setTitle("Tuition Center Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setLayout(new BorderLayout(10, 10));

        // Panel for form elements
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel()); // Spacer
        loginButton = new JButton("Login");
        panel.add(loginButton);

        add(panel, BorderLayout.CENTER);

        // Login action
        loginButton.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = DataManager.authenticateUser(username, password);

        if (user != null) {
            // Successful login
            JOptionPane.showMessageDialog(this, "Login Successful! Welcome, " + user.getFullName(), "Success", JOptionPane.INFORMATION_MESSAGE);
            openDashboard(user);
            this.dispose(); // Close the login window
        } else {
            // Failed login
            loginAttempts++;
            int attemptsLeft = MAX_ATTEMPTS - loginAttempts;
            if (attemptsLeft > 0) {
                JOptionPane.showMessageDialog(this, "Invalid username or password. You have " + attemptsLeft + " attempt(s) left.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "You have exceeded the maximum login attempts. The application is now locked.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                loginButton.setEnabled(false); // Disable the login button
                usernameField.setEnabled(false);
                passwordField.setEnabled(false);
            }
        }
    }

    private void openDashboard(User user) {
        // Launch the specific dashboard based on the user's role
        JFrame dashboard = null;
        switch (user.getRole()) {
            case "Admin":
                dashboard = new AdminDashboard(user);
                break;
            case "Receptionist":
                dashboard = new ReceptionistDashboard(user);
                break;
            case "Tutor":
                dashboard = new TutorDashboard(user);
                break;
            case "Student":
                dashboard = new StudentDashboard(user);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown role. Cannot open dashboard.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }
        dashboard.setVisible(true);
    }
}