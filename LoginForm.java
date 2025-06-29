import javax.swing.*;
import java.awt.*;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginForm() {
        setTitle("Login");
        setSize(350, 220);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        inputPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        inputPanel.add(usernameField);

        inputPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        inputPanel.add(passwordField);

        JButton loginButton = new JButton("🔐 Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.addActionListener(e -> login());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        buttonPanel.add(loginButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        User user = AuthService.login(username, password);
        if (user != null) {
            dispose();
            if (user instanceof Admin) {
                new AdminDashboard((Admin) user).setVisible(true);
            } else if (user instanceof Receptionist) {
                new ReceptionistDashboard((Receptionist) user).setVisible(true);
            } else if (user instanceof Tutor) {
                new TutorDashboard((Tutor) user).setVisible(true);
            } else if (user instanceof Student) {
                new StudentDashboard((Student) user).setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.");
        }
    }

    public static void main(String[] args) {
        new LoginForm();
    }
}
