import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class AdminDashboard extends JFrame {
    private User adminUser;
    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton btnAnnouncements, btnChat, btnLogout;
    private JPanel weeklyTimetablePanel;

    private JTable tutorTable, receptionistTable;
    private DefaultTableModel tutorTableModel, receptionistTableModel;

    private static final Color BG_COLOR = new Color(24, 34, 54);
    private static final Color FIELD_BG_COLOR = new Color(42, 53, 76);
    private static final Color PRIMARY_COLOR = new Color(67, 102, 163);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);

    public AdminDashboard(User user) {
        this.adminUser = user;
        setTitle("The Learning Hub - Admin Dashboard");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_COLOR);

        contentPanel.add(createManageTutorPanel(), "MANAGE_TUTORS");
        contentPanel.add(createManageReceptionistPanel(), "MANAGE_RECEPTIONISTS");
        contentPanel.add(createReportPanel(), "INCOME_REPORT");
        contentPanel.add(createTutorPayrollPanel(), "TUTOR_PAYROLL");
        contentPanel.add(createResultsPanelForAdmin(), "VIEW_RESULTS");
        
        weeklyTimetablePanel = createWeeklyTimetablePanel();
        contentPanel.add(weeklyTimetablePanel, "WEEKLY_TIMETABLE");
        
        contentPanel.add(createProfilePanel(), "PROFILE");
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        refreshTutorTable();
        refreshReceptionistTable();
        refreshChatNotification();
        refreshAnnouncementNotification();

        cardLayout.show(contentPanel, "MANAGE_TUTORS");
    }

    private JPanel createHeaderPanel() {
        JPanel fullHeaderPanel = new JPanel();
        fullHeaderPanel.setLayout(new BoxLayout(fullHeaderPanel, BoxLayout.Y_AXIS));
        fullHeaderPanel.setBackground(BG_COLOR);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(BG_COLOR);
        topHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + adminUser.getFullName());
        welcomeLabel.setForeground(TEXT_COLOR);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topHeader.add(welcomeLabel, BorderLayout.WEST);

        JPanel topRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRightButtons.setBackground(BG_COLOR);
        
        btnAnnouncements = createStyledButton("Announcements");
        JButton btnRelease = createStyledButton("Release Announcement");
        btnChat = createStyledButton("Chat");
        btnLogout = createStyledButton("Logout");
        
        btnAnnouncements.addActionListener(e -> openAnnouncements());
        btnRelease.addActionListener(e -> showReleaseAnnouncementDialog());
        btnChat.addActionListener(e -> openChatDialog());
        btnLogout.addActionListener(e -> logout());
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { logout(); }
        });
        
        topRightButtons.add(btnAnnouncements);
        topRightButtons.add(btnRelease);
        topRightButtons.add(btnChat);
        topRightButtons.add(btnLogout);
        topHeader.add(topRightButtons, BorderLayout.EAST);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        navPanel.setBackground(BG_COLOR);
        navPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JButton btnManageTutors = createStyledButton("Manage Tutors");
        JButton btnManageReceptionists = createStyledButton("Manage Receptionists");
        JButton btnIncomeReport = createStyledButton("Income Report");
        JButton btnTutorPayroll = createStyledButton("Tutor Payroll");
        JButton btnViewResults = createStyledButton("View Student Results");
        JButton btnWeeklyTimetable = createStyledButton("Weekly Timetable");
        JButton btnMyProfile = createStyledButton("My Profile");

        navPanel.add(btnManageTutors);
        navPanel.add(btnManageReceptionists);
        navPanel.add(btnIncomeReport);
        navPanel.add(btnTutorPayroll);
        navPanel.add(btnViewResults);
        navPanel.add(btnWeeklyTimetable); 
        navPanel.add(btnMyProfile);
        
        btnManageTutors.addActionListener(e -> cardLayout.show(contentPanel, "MANAGE_TUTORS"));
        btnManageReceptionists.addActionListener(e -> cardLayout.show(contentPanel, "MANAGE_RECEPTIONISTS"));
        btnIncomeReport.addActionListener(e -> cardLayout.show(contentPanel, "INCOME_REPORT"));
        btnTutorPayroll.addActionListener(e -> cardLayout.show(contentPanel, "TUTOR_PAYROLL"));
        btnViewResults.addActionListener(e -> cardLayout.show(contentPanel, "VIEW_RESULTS"));
        btnWeeklyTimetable.addActionListener(e -> cardLayout.show(contentPanel, "WEEKLY_TIMETABLE"));
        btnMyProfile.addActionListener(e -> cardLayout.show(contentPanel, "PROFILE"));

        fullHeaderPanel.add(topHeader);
        fullHeaderPanel.add(navPanel);
        return fullHeaderPanel;
    }


    private JPanel createManageTutorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columnNames = {"ID", "Username", "Full Name"};
        tutorTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tutorTable = new JTable(tutorTableModel);
        styleTable(tutorTable);
        tutorTable.setDefaultRenderer(Object.class, new CenterTableCellRenderer());
        JScrollPane scrollPane = new JScrollPane(tutorTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        JButton btnRegister = createStyledButton("Register New Tutor");
        JButton btnDelete = createStyledButton("Delete Selected Tutor");
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnDelete);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        btnRegister.addActionListener(e -> showRegisterDialog("Tutor"));
        btnDelete.addActionListener(e -> {
            int selectedRow = tutorTable.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) tutorTableModel.getValueAt(selectedRow, 1);
                int choice = JOptionPane.showConfirmDialog(this, "Delete " + username + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION && DataManager.deleteUser(username)) {
                    refreshTutorTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a tutor to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
        return panel;
    }
    
    private JPanel createManageReceptionistPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columnNames = {"ID", "Username", "Full Name"};
        receptionistTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        receptionistTable = new JTable(receptionistTableModel);
        styleTable(receptionistTable);
        receptionistTable.setDefaultRenderer(Object.class, new CenterTableCellRenderer());
        JScrollPane scrollPane = new JScrollPane(receptionistTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        JButton btnRegister = createStyledButton("Register New Receptionist");
        JButton btnDelete = createStyledButton("Delete Selected Receptionist");
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnDelete);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        btnRegister.addActionListener(e -> showRegisterDialog("Receptionist"));
        btnDelete.addActionListener(e -> {
            int selectedRow = receptionistTable.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) receptionistTableModel.getValueAt(selectedRow, 1);
                int choice = JOptionPane.showConfirmDialog(this, "Delete " + username + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION && DataManager.deleteUser(username)) {
                    refreshReceptionistTable();
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

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

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
        // IMPORTANT: Ensure 'currentUser' is set to the correct user for the dashboard
        // e.g., this.adminUser, this.receptionistUser, etc.
        User currentUser = this.adminUser; // Change this for each dashboard!

        ChatInterfaceFrame chatInterface = new ChatInterfaceFrame(currentUser);
        
        // Add a listener to refresh notifications when the chat window is closed
        chatInterface.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                refreshChatNotification();
            }
        });
        
        chatInterface.setVisible(true);
    }

    public void refreshChatNotification() {
        User currentUser = this.adminUser; // Correct user for this dashboard
        int unreadCount = DataManager.getUnreadMessageCount(currentUser); 
        if (unreadCount > 0) {
            btnChat.setText("Chat (" + unreadCount + ")");
            btnChat.setForeground(Color.RED);
            btnChat.setFont(new Font(btnChat.getFont().getName(), Font.BOLD, btnChat.getFont().getSize()));
        } else {
            btnChat.setText("Chat");
            btnChat.setForeground(Color.WHITE);
            btnChat.setFont(new Font(btnChat.getFont().getName(), Font.PLAIN, btnChat.getFont().getSize()));
        }
    }

    private JPanel createResultsPanelForAdmin() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JTextArea resultsArea = new JTextArea("Select a student to view their results.");
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Top panel for controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Use FlowLayout.CENTER
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
            btnAnnouncements.setForeground(Color.WHITE);
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
        this.dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
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

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return button;
    }
    
    private void styleTable(JTable table) {
        table.setBackground(BG_COLOR);
        table.setForeground(TEXT_COLOR);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setGridColor(FIELD_BG_COLOR);
        table.setRowHeight(25);
        table.setSelectionBackground(PRIMARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        JTableHeader header = table.getTableHeader();
        header.setBackground(FIELD_BG_COLOR);
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Arial", Font.BOLD, 16));
    }

    private void styleDialogComponent(JComponent component) {
        component.setBackground(new Color(24, 34, 54));
        if (component instanceof JLabel) {
            component.setForeground(Color.WHITE);
        } else if (component instanceof JTextField) {
            component.setForeground(Color.WHITE);
            ((JTextField) component).setCaretColor(Color.WHITE);
        }
    }

    private JPanel createWeeklyTimetablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Timetable Table (Center) ---
        String[] days = {"Time Slot", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        DefaultTableModel timetableModel = new DefaultTableModel(null, days) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable timetableTable = new JTable(timetableModel);
        styleTable(timetableTable);
        timetableTable.setRowHeight(35);

        populateTimetable(timetableModel);
        
        JScrollPane scrollPane = new JScrollPane(timetableTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        // --- Button Panel (South) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BG_COLOR);
        JButton generateButton = createStyledButton("Generate New Random Timetable");
        buttonPanel.add(generateButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // --- Action Listener for the Generate Button ---
        generateButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this, 
                "This will ERASE the current timetable and generate a new one.\nThis action cannot be undone. Are you sure?",
                "Confirm Timetable Regeneration", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                if (DataManager.generateAndAssignTimetable()) {
                    JOptionPane.showMessageDialog(this, "New timetable has been generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // After generating, simply re-populate the existing table model
                    populateTimetable(timetableModel);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to generate timetable. Check tutor specializations.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        return panel;
    }

    private void populateTimetable(DefaultTableModel timetableModel) {
        String[] timeSlots = {
            "08-09 AM", "09-10 AM", "10-11 AM", "11-12 PM", "12-01 PM",
            "01-02 PM", "02-03 PM", "03-04 PM", "04-05 PM", "05-06 PM",
            "06-07 PM", "07-08 PM"
        };
        String[] days = {"Time Slot", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        Map<String, String> dayToAbbrMap = Map.of("Monday", "Mon", "Tuesday", "Tue", "Wednesday", "Wed", "Thursday", "Thu", "Friday", "Fri");
        
        timetableModel.setRowCount(0);
        Map<String, Map<String, String>> weeklyData = DataManager.getStoredTimetable();

        for (String timeSlot : timeSlots) {
            Object[] rowData = new Object[days.length];
            rowData[0] = timeSlot;
            if (timeSlot.equals("12-01 PM")) {
                for (int i = 1; i < days.length; i++) rowData[i] = "--- RECESS ---";
            } else {
                for (int i = 1; i < days.length; i++) {
                    String dayAbbr = dayToAbbrMap.get(days[i]);
                    rowData[i] = weeklyData.getOrDefault(dayAbbr, new HashMap<>()).getOrDefault(timeSlot, " ");
                }
            }
            timetableModel.addRow(rowData);
        }
    }

    private void handleGenerateTimetable() {
        int choice = JOptionPane.showConfirmDialog(
            this, 
            "This will ERASE the existing timetable and generate a new one.\nThis action cannot be undone. Are you sure?",
            "Confirm Timetable Regeneration", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            if (DataManager.generateAndAssignTimetable()) {
                JOptionPane.showMessageDialog(this, "New timetable has been generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                contentPanel.remove(weeklyTimetablePanel);
                weeklyTimetablePanel = createWeeklyTimetablePanel();
                contentPanel.add(weeklyTimetablePanel, "WEEKLY_TIMETABLE");
                cardLayout.show(contentPanel, "WEEKLY_TIMETABLE");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to generate timetable. Check tutor specializations.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class CenterTableCellRenderer extends DefaultTableCellRenderer {
        public CenterTableCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }
    }
}
