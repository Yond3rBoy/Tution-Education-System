import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

public class LoginFrame extends JFrame {

    // --- UI Components ---
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton verifyButton;
    private JButton loginButton;
    private JButton backButton;
    private JLabel passwordLabel;

    // --- State Variables ---
    private int loginAttempts = 0;
    private static final int MAX_ATTEMPTS = 3;
    private String verifiedUsername = null;

    // --- Styling ---
    private static final Color BG_COLOR = new Color(24, 34, 54);
    private static final Color PRIMARY_COLOR = new Color(67, 102, 163);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);
    private static final Color FIELD_BG_COLOR = new Color(42, 53, 76);

    public LoginFrame() {
        setTitle("The Learning Hub - Login");
        setSize(500, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // --- Main Panel Setup ---
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Each component on its own row
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Logo ---
        try {
            ImageIcon logoIcon = new ImageIcon(new ImageIcon("assets/logo.png").getImage().getScaledInstance(197, 103, Image.SCALE_SMOOTH));
            JLabel logoLabel = new JLabel(logoIcon);
            gbc.insets = new Insets(20, 10, 10, 10);
            mainPanel.add(logoLabel, gbc);
        } catch (Exception e) {
            System.out.println("Logo not found. Place 'logo.png' in the 'assets' folder.");
        }

        // --- Title ---
        JLabel titleLabel = new JLabel("The Learning Hub", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);
        gbc.insets = new Insets(10, 10, 30, 10);
        mainPanel.add(titleLabel, gbc);
        
        gbc.insets = new Insets(8, 10, 8, 10); // Reset insets

        // --- Username Components ---
        mainPanel.add(createLabel("Username"), gbc);
        usernameField = createTextField();
        mainPanel.add(usernameField, gbc);
        verifyButton = createButton("Verify", PRIMARY_COLOR);
        mainPanel.add(verifyButton, gbc);

        // --- Password Components ---
        passwordLabel = createLabel("Password");
        mainPanel.add(passwordLabel, gbc);
        passwordField = createPasswordField();
        mainPanel.add(passwordField, gbc);
        loginButton = createButton("Login", PRIMARY_COLOR);
        mainPanel.add(loginButton, gbc);

        // --- Back Button ---
        backButton = createButton("Back", FIELD_BG_COLOR);
        mainPanel.add(backButton, gbc);

        add(mainPanel);

        // --- Action Listeners ---
        verifyButton.addActionListener(e -> handleVerification());
        usernameField.addActionListener(e -> handleVerification()); // For Enter key

        loginButton.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin()); // For Enter key

        backButton.addActionListener(e -> resetToUsernameStep());

        // --- Set Initial State ---
        resetToUsernameStep();
    }

    private void handleVerification() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (DataManager.isUsernameValid(username)) {
            this.verifiedUsername = username;
            switchToPasswordStep();
        } else {
            JOptionPane.showMessageDialog(this, "Username not found. Please try again.", "Verification Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLogin() {
        String password = new String(passwordField.getPassword());
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = DataManager.authenticateUser(verifiedUsername, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Login Successful! Welcome, " + user.getFullName(), "Success", JOptionPane.INFORMATION_MESSAGE);
            openDashboard(user);
            this.dispose();
        } else {
            loginAttempts++;
            int attemptsLeft = MAX_ATTEMPTS - loginAttempts;
            if (attemptsLeft > 0) {
                JOptionPane.showMessageDialog(this, "Invalid password. You have " + attemptsLeft + " attempt(s) left.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "You have exceeded the maximum login attempts. Please contact an admin.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                loginButton.setEnabled(false);
                passwordField.setEnabled(false);
                backButton.setEnabled(false);
            }
        }
    }

    private void resetToUsernameStep() {
        usernameField.setEditable(true);
        usernameField.setText("");
        verifyButton.setVisible(true);

        passwordLabel.setVisible(false);
        passwordField.setVisible(false);
        passwordField.setText("");
        loginButton.setVisible(false);
        backButton.setVisible(false);
        
        loginAttempts = 0; // Reset attempts when going back
        verifiedUsername = null;
        
        // Set focus and default button for username verification
        usernameField.requestFocusInWindow();
        getRootPane().setDefaultButton(verifyButton);
    }

    private void switchToPasswordStep() {
        usernameField.setEditable(false);
        verifyButton.setVisible(false);
        
        passwordLabel.setVisible(true);
        passwordField.setVisible(true);
        loginButton.setVisible(true);
        backButton.setVisible(true);

        // Set focus and default button for login
        passwordField.requestFocusInWindow();
        getRootPane().setDefaultButton(loginButton);
    }

    // --- Helper methods for creating styled components ---
    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        styleField(field);
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField(20);
        styleField(field);
        return field;
    }
    
    // In LoginFrame.java

    private void styleField(JComponent field) {
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setBackground(FIELD_BG_COLOR);
        field.setForeground(TEXT_COLOR);
        
        // --- THE FIX ---
        // Check if the component is a text component before trying to set the caret color.
        if (field instanceof javax.swing.text.JTextComponent) {
            // Cast the generic JComponent to the specific JTextComponent
            ((javax.swing.text.JTextComponent) field).setCaretColor(TEXT_COLOR);
        }
        
        Border line = BorderFactory.createLineBorder(TEXT_COLOR, 1, true);
        Border padding = BorderFactory.createEmptyBorder(5, 10, 5, 10);
        field.setBorder(BorderFactory.createCompoundBorder(line, padding));
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        // Align label to the left side of the column
        label.setAlignmentX(Component.LEFT_ALIGNMENT); 
        return label;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return button;
    }
    
    // This method remains the same
    private void openDashboard(User user) {
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
                JOptionPane.showMessageDialog(this, "Unknown role...", "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }
        dashboard.setVisible(true);
    }
}
