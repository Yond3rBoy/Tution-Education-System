import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends JFrame {
    private User adminUser;
    private JTable tutorTable;
    private DefaultTableModel tutorTableModel;
    private JTable receptionistTable;
    private DefaultTableModel receptionistTableModel;
    private JButton btnChat;
    private JButton btnAnnouncements;

    public AdminDashboard(User user) {
        this.adminUser = user;
        setTitle("Admin Dashboard - Welcome, " + user.getFullName());
        setSize(900, 700); // Increased size slightly for new buttons
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                logout();
            }
        });

        // Main container panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top panel for all action buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAnnouncements = new JButton("Announcements");
        JButton btnRelease = new JButton("Release Announcement");
        btnChat = new JButton("Chat");
        topPanel.add(btnAnnouncements);
        topPanel.add(btnRelease);
        topPanel.add(btnChat);
        JButton btnLogout = new JButton("Logout"); // <-- ADD THIS
        topPanel.add(btnLogout);

        // Tabbed pane for core functions
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Tutors", createTutorPanel());
        tabbedPane.addTab("Manage Receptionists", createReceptionistPanel());
        tabbedPane.addTab("Income Report", createReportPanel());
        tabbedPane.addTab("Tutor Payroll", createTutorPayrollPanel());
        tabbedPane.addTab("Income vs Payroll", createIncomeVsPayrollPanel());
        tabbedPane.addTab("View Student Results", createResultsPanelForAdmin());
        tabbedPane.addTab("My Profile", createProfilePanel());
        

        // Assemble the main view
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);

        // --- Action Listeners ---
        btnChat.addActionListener(e -> openChatDialog());
        btnAnnouncements.addActionListener(e -> openAnnouncements()); // <-- THIS WAS MISSING
        btnRelease.addActionListener(e -> showReleaseAnnouncementDialog()); // <-- THIS WAS MISSING
        btnLogout.addActionListener(e -> logout());

        // --- Initial Data Load & Notifications ---
        refreshTutorTable();
        refreshReceptionistTable();
        refreshChatNotification();
        refreshAnnouncementNotification(); // <-- THIS WAS MISSING
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

    private void openChatDialog() {
        // This 'user' variable must be the logged-in user object for the dashboard
        User currentUser = this.adminUser; // Correct user for this dashboard

        List<User> eligibleUsers = DataManager.getUsersForChat(currentUser);
        if (eligibleUsers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No other users available to chat with.", "Chat", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User[] usersArray = eligibleUsers.toArray(new User[0]);

        User selectedUser = (User) JOptionPane.showInputDialog(
                this,
                "Select a user to chat with:",
                "Start a Chat",
                JOptionPane.PLAIN_MESSAGE,
                null,
                usersArray,
                usersArray[0]);

        if (selectedUser != null) {
            ChatFrame chatFrame = new ChatFrame(currentUser, selectedUser);
            // Add a listener to refresh notifications when the chat window closes
            chatFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    refreshChatNotification();
                }
            });
            chatFrame.setVisible(true);
        }
    }

    // Method to update the chat button with unread message count
    public void refreshChatNotification() {
        User currentUser = this.adminUser; // Correct user for this dashboard
        int unreadCount = DataManager.getUnreadMessageCount(currentUser); 
        if (unreadCount > 0) {
            btnChat.setText("Chat (" + unreadCount + ")");
            btnChat.setForeground(Color.RED);
            btnChat.setFont(new Font(btnChat.getFont().getName(), Font.BOLD, btnChat.getFont().getSize()));
        } else {
            btnChat.setText("Chat");
            btnChat.setForeground(Color.BLACK);
            btnChat.setFont(new Font(btnChat.getFont().getName(), Font.PLAIN, btnChat.getFont().getSize()));
        }
    }

        private JPanel createResultsPanelForAdmin() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JTextArea resultsArea = new JTextArea("Select a student to view their results.");
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Top panel for controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        List<User> allStudents = DataManager.getAllUsersByRole("Student");
        JComboBox<User> studentSelector = new JComboBox<>(new Vector<>(allStudents));
        
        topPanel.add(new JLabel("Select Student:"));
        topPanel.add(studentSelector);

        JButton viewButton = new JButton("View Results");
        topPanel.add(viewButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        viewButton.addActionListener(e -> {
            User selectedStudent = (User) studentSelector.getSelectedItem();
            if (selectedStudent == null) return;
            String report = DataManager.getStudentResultsReport(selectedStudent.getId());
            resultsArea.setText(report);
            resultsArea.setCaretPosition(0);
        });

        return panel;
    }

    private void openAnnouncements() {
        // CHANGE this.adminUser to the correct user for each dashboard
        AnnouncementsFrame announcementsFrame = new AnnouncementsFrame(this.adminUser);
        announcementsFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                // Refresh notification when the announcements window is closed
                refreshAnnouncementNotification();
            }
        });
        announcementsFrame.setVisible(true);
    }
    
    // Method to update the button with unread count
    public void refreshAnnouncementNotification() {
        // CHANGE this.adminUser to the correct user for each dashboard
        Set<String> readIds = DataManager.getReadAnnouncementIds(this.adminUser);
        List<Announcement> allAnnouncements = DataManager.getAllAnnouncements();
        long unreadCount = allAnnouncements.stream().filter(a -> !readIds.contains(a.getId())).count();

        if (unreadCount > 0) {
            btnAnnouncements.setText("Announcements (" + unreadCount + ")");
            btnAnnouncements.setForeground(Color.BLUE); // Use a different color than chat
            btnAnnouncements.setFont(new Font(btnAnnouncements.getFont().getName(), Font.BOLD, btnAnnouncements.getFont().getSize()));
        } else {
            btnAnnouncements.setText("Announcements");
            btnAnnouncements.setForeground(Color.BLACK);
            btnAnnouncements.setFont(new Font(btnAnnouncements.getFont().getName(), Font.PLAIN, btnAnnouncements.getFont().getSize()));
        }
    }

    private void showReleaseAnnouncementDialog() {
        JTextField titleField = new JTextField(30);
        JTextArea contentArea = new JTextArea(5, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Title:"), BorderLayout.NORTH);
        panel.add(titleField, BorderLayout.CENTER);
        panel.add(new JScrollPane(contentArea), BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel, "Release New Announcement", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            if (title.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and content cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // CHANGE this.adminUser to this.receptionistUser in the other dashboard
            if (DataManager.createAnnouncement(this.adminUser, title, content)) {
                JOptionPane.showMessageDialog(this, "Announcement released successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to release announcement.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void logout() {
        this.dispose(); // Close this dashboard window
        new LoginFrame().setVisible(true); // Open a new login screen
    }

    private JPanel createTutorPayrollPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JTextArea reportArea = new JTextArea("Select a tutor, month, and year to generate a payroll report.");
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        // Controls for selecting tutor, month, and year
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        // Dropdown for selecting a tutor
        List<User> allTutors = DataManager.getAllUsersByRole("Tutor");
        JComboBox<User> tutorSelector = new JComboBox<>(new Vector<>(allTutors));
        
        JComboBox<Integer> monthComboBox = new JComboBox<>();
        for (int i = 1; i <= 12; i++) monthComboBox.addItem(i);
        
        JComboBox<Integer> yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 2; i <= currentYear; i++) yearComboBox.addItem(i);
        
        monthComboBox.setSelectedItem(LocalDate.now().getMonthValue());
        yearComboBox.setSelectedItem(currentYear);
        
        JButton btnGenerate = new JButton("Generate Payroll Report");
        controlsPanel.add(new JLabel("Tutor:"));
        controlsPanel.add(tutorSelector);
        controlsPanel.add(new JLabel("Month:"));
        controlsPanel.add(monthComboBox);
        controlsPanel.add(new JLabel("Year:"));
        controlsPanel.add(yearComboBox);
        controlsPanel.add(btnGenerate);
        
        panel.add(controlsPanel, BorderLayout.NORTH);

        // Action listener to generate the report for the selected tutor
        btnGenerate.addActionListener(e -> {
            User selectedTutor = (User) tutorSelector.getSelectedItem();
            if (selectedTutor == null) {
                JOptionPane.showMessageDialog(this, "No tutors available to generate a report for.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int month = (int) monthComboBox.getSelectedItem();
            int year = (int) yearComboBox.getSelectedItem();
            String report = DataManager.generateTutorPayrollReport(selectedTutor.getId(), month, year);
            reportArea.setText(report);
            reportArea.setCaretPosition(0);
        });
        
        return panel;
    }

    private JPanel createIncomeVsPayrollPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JTextArea reportArea = new JTextArea("Select a month and year to generate the comprehensive financial report.");
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        // Controls for selecting month and year
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JComboBox<Integer> monthComboBox = new JComboBox<>();
        for (int i = 1; i <= 12; i++) monthComboBox.addItem(i);
        
        JComboBox<Integer> yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 2; i <= currentYear; i++) yearComboBox.addItem(i);
        
        monthComboBox.setSelectedItem(LocalDate.now().getMonthValue());
        yearComboBox.setSelectedItem(currentYear);
        
        JButton btnGenerate = new JButton("Generate Financial Summary");
        controlsPanel.add(new JLabel("Month:"));
        controlsPanel.add(monthComboBox);
        controlsPanel.add(new JLabel("Year:"));
        controlsPanel.add(yearComboBox);
        controlsPanel.add(btnGenerate);
        
        panel.add(controlsPanel, BorderLayout.NORTH);

        // Action listener to generate the comprehensive report
        btnGenerate.addActionListener(e -> {
            int month = (int) monthComboBox.getSelectedItem();
            int year = (int) yearComboBox.getSelectedItem();
            String report = DataManager.generateIncomeVsPayrollReport(month, year);
            reportArea.setText(report);
            reportArea.setCaretPosition(0);
        });
        
        return panel;
    }
}
