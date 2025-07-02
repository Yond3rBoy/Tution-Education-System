// ... (imports remain the same) ...
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends JFrame {
    // ... (class members and constructor are the same) ...
    private User adminUser;
    private JTable tutorTable;
    private DefaultTableModel tutorTableModel;
    private JTable receptionistTable;
    private DefaultTableModel receptionistTableModel;

    public AdminDashboard(User user) {
        this.adminUser = user;
        setTitle("Admin Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Manage Tutors", createTutorPanel());
        tabbedPane.addTab("Manage Receptionists", createReceptionistPanel());
        tabbedPane.addTab("Income Report", createReportPanel());
        tabbedPane.addTab("My Profile", createProfilePanel());

        add(tabbedPane);
        
        refreshTutorTable();
        refreshReceptionistTable();
    }
    // ... (createTutorPanel, createReceptionistPanel, createReportPanel are the same) ...
        private JPanel createTutorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        String[] columnNames = {"ID", "Username", "Full Name"};
        tutorTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        tutorTable = new JTable(tutorTableModel);
        panel.add(new JScrollPane(tutorTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnRegister = new JButton("Register New Tutor");
        JButton btnDelete = new JButton("Delete Selected Tutor");
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnDelete);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        btnRegister.addActionListener(e -> showRegisterDialog("Tutor"));
        btnDelete.addActionListener(e -> {
            int selectedRow = tutorTable.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) tutorTableModel.getValueAt(selectedRow, 1);
                int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + username + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    if (DataManager.deleteUser(username)) {
                        JOptionPane.showMessageDialog(this, "Tutor deleted successfully.");
                        refreshTutorTable();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete tutor.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a tutor to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }
    
    private JPanel createReceptionistPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] columnNames = {"ID", "Username", "Full Name"};
        receptionistTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        receptionistTable = new JTable(receptionistTableModel);
        panel.add(new JScrollPane(receptionistTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnRegister = new JButton("Register New Receptionist");
        JButton btnDelete = new JButton("Delete Selected Receptionist");
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnDelete);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        btnRegister.addActionListener(e -> showRegisterDialog("Receptionist"));
        btnDelete.addActionListener(e -> {
             int selectedRow = receptionistTable.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) receptionistTableModel.getValueAt(selectedRow, 1);
                int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + username + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    if (DataManager.deleteUser(username)) {
                        JOptionPane.showMessageDialog(this, "Receptionist deleted successfully.");
                        refreshReceptionistTable();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete receptionist.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a receptionist to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        JPanel controlsPanel = new JPanel();
        JComboBox<Integer> monthComboBox = new JComboBox<>();
        for (int i = 1; i <= 12; i++) monthComboBox.addItem(i);
        JComboBox<Integer> yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 5; i <= currentYear; i++) yearComboBox.addItem(i);
        yearComboBox.setSelectedItem(currentYear);
        
        JButton btnGenerate = new JButton("Generate Report");
        controlsPanel.add(new JLabel("Month:"));
        controlsPanel.add(monthComboBox);
        controlsPanel.add(new JLabel("Year:"));
        controlsPanel.add(yearComboBox);
        controlsPanel.add(btnGenerate);
        
        JTextArea reportArea = new JTextArea("Report will be shown here.");
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        panel.add(controlsPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        btnGenerate.addActionListener(e -> {
            int month = (int) monthComboBox.getSelectedItem();
            int year = (int) yearComboBox.getSelectedItem();
            String report = DataManager.generateIncomeReport(month, year);
            reportArea.setText(report);
        });
        
        return panel;
    }

    // --- Panel for Updating Admin's Own Profile ---
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        JTextField txtId = new JTextField(adminUser.getId(), 20);
        txtId.setEditable(false);
        panel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField txtUsername = new JTextField(adminUser.getUsername(), 20);
        txtUsername.setEditable(false);
        panel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtFullName = new JTextField(adminUser.getFullName(), 20);
        panel.add(txtFullName, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("New Password (leave blank to keep current):"), gbc);
        gbc.gridx = 1;
        JPasswordField txtPassword = new JPasswordField(20);
        panel.add(txtPassword, gbc);

        gbc.gridy++; gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton btnUpdate = new JButton("Update Profile");
        panel.add(btnUpdate, gbc);

        btnUpdate.addActionListener(e -> {
            String newFullName = txtFullName.getText().trim();
            String newPasswordStr = new String(txtPassword.getPassword());

            if (newFullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Full Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get the current password if the field is left blank.
            String finalPassword = newPasswordStr.isEmpty() ? adminUser.getPassword() : newPasswordStr;

            // Create the updated user object. The constructor now handles specialization correctly.
            User updatedUser = new User(
                adminUser.getId(),
                adminUser.getUsername(),
                finalPassword,
                adminUser.getRole(),
                newFullName,
                adminUser.getSpecialization() // Pass the existing specialization
            );

            if (DataManager.updateUser(updatedUser)) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully.");
                // Update the dashboard's internal user object to reflect changes
                this.adminUser = updatedUser; 
            } else {
                 JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // ... (helper methods like refreshTutorTable and showRegisterDialog are the same) ...
        private void refreshTutorTable() {
        tutorTableModel.setRowCount(0);
        List<User> tutors = DataManager.getAllUsersByRole("Tutor");
        for (User tutor : tutors) {
            tutorTableModel.addRow(new Object[]{tutor.getId(), tutor.getUsername(), tutor.getFullName()});
        }
    }

    private void refreshReceptionistTable() {
        receptionistTableModel.setRowCount(0);
        List<User> receptionists = DataManager.getAllUsersByRole("Receptionist");
        for (User user : receptionists) {
            receptionistTableModel.addRow(new Object[]{user.getId(), user.getUsername(), user.getFullName()});
        }
    }
    
    private void showRegisterDialog(String role) {
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JTextField fullNameField = new JTextField(15);
        JTextField specializationField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);
        
        if ("Tutor".equals(role)) {
            panel.add(new JLabel("Specialization(s):"));
            panel.add(specializationField);
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Register New " + role, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String fullName = fullNameField.getText().trim();

            if(username.isEmpty() || password.isEmpty() || fullName.isEmpty()){
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String specialization = "Tutor".equals(role) ? specializationField.getText() : "";
            if(DataManager.registerUser(username, password, role, fullName, specialization)) {
                JOptionPane.showMessageDialog(this, role + " registered successfully.");
                if("Tutor".equals(role)) refreshTutorTable();
                else refreshReceptionistTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register " + role, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}