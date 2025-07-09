import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class ReceptionistDashboard extends JFrame {
    private User receptionistUser;

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton btnAnnouncements, btnChat, btnLogout;
    private JPanel weeklyTimetablePanel;

    private JTable studentTable, requestTable;
    private DefaultTableModel studentTableModel, requestTableModel;
    
    private static final Color BG_COLOR = new Color(24, 34, 54);
    private static final Color FIELD_BG_COLOR = new Color(42, 53, 76);
    private static final Color PRIMARY_COLOR = new Color(67, 102, 163);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);

    public ReceptionistDashboard(User user) {
        this.receptionistUser = user;
        setTitle("The Learning Hub - Receptionist Dashboard");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_COLOR);

        contentPanel.add(createStudentPanel(), "MANAGE_STUDENTS");
        contentPanel.add(createStudentAccountsPanel(), "STUDENT_ACCOUNTS");
        contentPanel.add(createRequestsPanel(), "HANDLE_REQUESTS");
        
        weeklyTimetablePanel = createWeeklyTimetablePanel();
        contentPanel.add(weeklyTimetablePanel, "WEEKLY_TIMETABLE");
        
        contentPanel.add(createProfilePanel(), "PROFILE");
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        refreshStudentTable();
        refreshRequestsTable();
        refreshChatNotification();
        refreshAnnouncementNotification();

        cardLayout.show(contentPanel, "MANAGE_STUDENTS");
    }

    private JPanel createHeaderPanel() {
        JPanel fullHeaderPanel = new JPanel();
        fullHeaderPanel.setLayout(new BoxLayout(fullHeaderPanel, BoxLayout.Y_AXIS));
        fullHeaderPanel.setBackground(BG_COLOR);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(BG_COLOR);
        topHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + receptionistUser.getFullName());
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

        // --- CORRECTED: Navigation bar with ONLY Receptionist functions ---
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        navPanel.setBackground(BG_COLOR);
        navPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JButton btnManageStudents = createStyledButton("Manage Students");
        JButton btnStudentAccounts = createStyledButton("Student Accounts");
        JButton btnHandleRequests = createStyledButton("Handle Requests");
        JButton btnWeeklyTimetable = createStyledButton("Weekly Timetable");
        JButton btnMyProfile = createStyledButton("My Profile");

        navPanel.add(btnManageStudents);
        navPanel.add(btnStudentAccounts);
        navPanel.add(btnHandleRequests);
        navPanel.add(btnWeeklyTimetable);
        navPanel.add(btnMyProfile);

        // Action listeners for the correct buttons
        btnManageStudents.addActionListener(e -> cardLayout.show(contentPanel, "MANAGE_STUDENTS"));
        btnStudentAccounts.addActionListener(e -> cardLayout.show(contentPanel, "STUDENT_ACCOUNTS"));
        btnHandleRequests.addActionListener(e -> cardLayout.show(contentPanel, "HANDLE_REQUESTS"));
        btnWeeklyTimetable.addActionListener(e -> cardLayout.show(contentPanel, "WEEKLY_TIMETABLE"));
        btnMyProfile.addActionListener(e -> cardLayout.show(contentPanel, "PROFILE"));

        fullHeaderPanel.add(topHeader);
        fullHeaderPanel.add(navPanel);
        return fullHeaderPanel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columnNames = {"ID", "Username", "Full Name"};
        studentTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        studentTable = new JTable(studentTableModel);
        styleTable(studentTable);
        studentTable.setDefaultRenderer(Object.class, new CenterTableCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        JButton btnRegister = createStyledButton("Register New Student");
        JButton btnUpdate = createStyledButton("Update Enrollment");
        JButton btnStudentAccounts = createStyledButton("Student Accounts");
        JButton btnDelete = createStyledButton("Delete Student");
        
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);

        btnRegister.addActionListener(e -> showRegisterStudentDialog());
        btnUpdate.addActionListener(e -> showUpdateEnrollmentDialog());
        btnDelete.addActionListener(e -> deleteSelectedStudent());
        
        return panel;
    }

    private JPanel createRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columnNames = {"Request ID", "Student ID", "Date", "Details"};
        requestTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        requestTable = new JTable(requestTableModel);
        styleTable(requestTable);
        
        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        JButton btnViewDetails = createStyledButton("View Full Request");
        JButton btnMarkCompleted = createStyledButton("Mark as Completed");
        JButton btnRefresh = createStyledButton("Refresh List");
        
        buttonPanel.add(btnViewDetails);
        buttonPanel.add(btnMarkCompleted);
        buttonPanel.add(btnRefresh);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> refreshRequestsTable());
        btnViewDetails.addActionListener(e -> {
            int selectedRow = requestTable.getSelectedRow();
            if (selectedRow >= 0) {
                String details = (String) requestTableModel.getValueAt(selectedRow, 3);
                JTextArea textArea = new JTextArea(details, 8, 40);
                textArea.setWrapStyleWord(true);
                textArea.setLineWrap(true);
                textArea.setEditable(false);
                JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Full Request Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a request to view.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnMarkCompleted.addActionListener(e -> {
            int selectedRow = requestTable.getSelectedRow();
            if (selectedRow >= 0) {
                String requestId = (String) requestTableModel.getValueAt(selectedRow, 0);
                int choice = JOptionPane.showConfirmDialog(this, "Mark request " + requestId + " as completed?", "Confirm Action", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION && DataManager.updateRequestStatus(requestId, "COMPLETED")) {
                    refreshRequestsTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a request to mark as completed.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Full Name:"); nameLabel.setForeground(TEXT_COLOR); panel.add(nameLabel, gbc);
        JTextField txtFullName = new JTextField(receptionistUser.getFullName(), 20); gbc.gridx = 1; panel.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel passLabel = new JLabel("New Password (leave blank):"); passLabel.setForeground(TEXT_COLOR); panel.add(passLabel, gbc);
        JPasswordField txtPassword = new JPasswordField(20); gbc.gridx = 1; panel.add(txtPassword, gbc);
        
        gbc.gridy++; gbc.gridx = 0;
        gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton btnUpdate = createStyledButton("Update My Profile");
        panel.add(btnUpdate, gbc);

        btnUpdate.addActionListener(e -> {
            String newFullName = txtFullName.getText().trim();
            String newPasswordStr = new String(txtPassword.getPassword());
            String finalPassword = newPasswordStr.isEmpty() ? receptionistUser.getPassword() : newPasswordStr;
            User updatedUser = new User(receptionistUser.getId(), receptionistUser.getUsername(), finalPassword, receptionistUser.getRole(), newFullName, "");
            if (DataManager.updateUser(updatedUser)) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                this.receptionistUser = updatedUser;
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }
    
    private void logout() {
        this.dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
    
    private void refreshStudentTable() {
        studentTableModel.setRowCount(0);
        List<User> students = DataManager.getAllUsersByRole("Student");
        for (User student : students) {
            studentTableModel.addRow(new Object[]{student.getId(), student.getUsername(), student.getFullName()});
        }
    }
    
    private void refreshRequestsTable() {
        requestTableModel.setRowCount(0);
        List<String[]> pendingRequests = DataManager.getAllPendingRequests();
        for (String[] requestData : pendingRequests) {
            requestTableModel.addRow(new Object[]{requestData[0], requestData[1], requestData[4], requestData[2]});
        }
    }
    
    private void showRegisterStudentDialog() {
        JTextField txtFullName = new JTextField();
        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JTextField txtIc = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtContact = new JTextField();
        JTextField txtAddress = new JTextField();

        JList<String> courseList = new JList<>(new Vector<>(DataManager.getAvailableCourses()));
        courseList.setVisibleRowCount(5);
        courseList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); 

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.add(new JLabel("Full Name:")); formPanel.add(txtFullName);
        formPanel.add(new JLabel("Username:")); formPanel.add(txtUsername);
        formPanel.add(new JLabel("Password:")); formPanel.add(txtPassword);
        formPanel.add(new JLabel("IC/Passport:")); formPanel.add(txtIc);
        formPanel.add(new JLabel("Email:")); formPanel.add(txtEmail);
        formPanel.add(new JLabel("Contact No:")); formPanel.add(txtContact);
        formPanel.add(new JLabel("Address:")); formPanel.add(txtAddress);
        formPanel.add(new JLabel("Enroll in Subjects (Max 3):"));
        formPanel.add(new JScrollPane(courseList));

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Register New Student", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            List<String> selectedCourses = courseList.getSelectedValuesList();
            if (selectedCourses.size() > 3) {
                JOptionPane.showMessageDialog(this, "A student can enroll in a maximum of 3 subjects.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (selectedCourses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You must select at least one course.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<String> courseIDs = selectedCourses.stream().map(s -> s.split(":")[0]).collect(Collectors.toList());
            
            boolean success = DataManager.registerStudent(
                    txtFullName.getText(), txtUsername.getText(), new String(txtPassword.getPassword()),
                    txtIc.getText(), txtEmail.getText(), txtContact.getText(), txtAddress.getText(), courseIDs);

            if (success) {
                JOptionPane.showMessageDialog(this, "Student registered and enrolled successfully!");
                refreshStudentTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register student.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showUpdateEnrollmentDialog() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String studentId = (String) studentTableModel.getValueAt(selectedRow, 0);
        String studentName = (String) studentTableModel.getValueAt(selectedRow, 2);

        List<String> allCoursesInfo = DataManager.getAvailableCourses();
        Set<String> currentCourseIDs = DataManager.getStudentCourseIDs(studentId);
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        Map<JCheckBox, String> checkboxMap = new LinkedHashMap<>(); 
        for (String courseInfo : allCoursesInfo) {
            String courseId = courseInfo.split(":")[0];
            JCheckBox checkBox = new JCheckBox(courseInfo);
            if (currentCourseIDs.contains(courseId)) {
                checkBox.setSelected(true);
            }
            checkboxMap.put(checkBox, courseId);
            checkboxPanel.add(checkBox);
        }
        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        int result = JOptionPane.showConfirmDialog(this, scrollPane, "Update Enrollments for " + studentName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            List<String> newCourseIDs = new ArrayList<>();
            for (Map.Entry<JCheckBox, String> entry : checkboxMap.entrySet()) {
                if (entry.getKey().isSelected()) {
                    newCourseIDs.add(entry.getValue());
                }
            }
            if (newCourseIDs.size() > 3) {
                JOptionPane.showMessageDialog(this, "A student can enroll in a maximum of 3 subjects.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (DataManager.updateStudentEnrollments(studentId, newCourseIDs)) {
                JOptionPane.showMessageDialog(this, "Enrollments updated successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update enrollments.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPaymentDialog() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to make a payment for.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String studentId = (String) studentTableModel.getValueAt(selectedRow, 0);
        Map<String, String> enrollments = DataManager.getStudentEnrollments(studentId);
        if (enrollments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "This student has no active enrollments.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Vector<String> enrollmentOptions = new Vector<>();
        enrollments.forEach((id, info) -> enrollmentOptions.add(id + ": " + info));
        JComboBox<String> enrollmentComboBox = new JComboBox<>(enrollmentOptions);
        JTextField amountField = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Select Enrollment:"));
        panel.add(enrollmentComboBox);
        panel.add(new JLabel("Amount to Pay:"));
        panel.add(amountField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Accept Payment", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String selectedEnrollment = (String) enrollmentComboBox.getSelectedItem();
                String enrollmentId = selectedEnrollment.split(":")[0];
                double amount = Double.parseDouble(amountField.getText());
                String receipt = DataManager.acceptPayment(enrollmentId, amount);
                JTextArea receiptArea = new JTextArea(receipt, 10, 30);
                receiptArea.setEditable(false);
                JOptionPane.showMessageDialog(this, new JScrollPane(receiptArea), "Payment Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String studentId = (String) studentTableModel.getValueAt(selectedRow, 0);
        String studentName = (String) studentTableModel.getValueAt(selectedRow, 2);
        int choice = JOptionPane.showConfirmDialog(this, "Delete " + studentName + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION && DataManager.deleteStudent(studentId)) {
            refreshStudentTable();
        }
    }
    
    private void openChatDialog() {
        // IMPORTANT: Ensure 'currentUser' is set to the correct user for the dashboard
        // e.g., this.adminUser, this.receptionistUser, etc.
        User currentUser = this.receptionistUser; // Change this for each dashboard!

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
        int unreadCount = DataManager.getUnreadMessageCount(this.receptionistUser); 
        if (unreadCount > 0) {
            btnChat.setText("Chat (" + unreadCount + " unread)");
            btnChat.setForeground(Color.RED);
        } else {
            btnChat.setText("Chat");
            btnChat.setForeground(Color.WHITE);
        }
    }

    private void openAnnouncements() {
        AnnouncementsFrame announcementsFrame = new AnnouncementsFrame(this.receptionistUser);
        announcementsFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) { refreshAnnouncementNotification(); }
        });
        announcementsFrame.setVisible(true);
    }
    
    public void refreshAnnouncementNotification() {
        Set<String> readIds = DataManager.getReadAnnouncementIds(this.receptionistUser);
        long unreadCount = DataManager.getAllAnnouncements().stream().filter(a -> !readIds.contains(a.getId())).count();
        if (unreadCount > 0) {
            btnAnnouncements.setText("Announcements (" + unreadCount + ")");
            btnAnnouncements.setForeground(Color.CYAN);
        } else {
            btnAnnouncements.setText("Announcements");
            btnAnnouncements.setForeground(Color.WHITE);
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
            if (DataManager.createAnnouncement(this.receptionistUser, title, content)) {
                JOptionPane.showMessageDialog(this, "Announcement released successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to release announcement.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
    // This array now matches the format used by the generator in DataManager
        String[] timeSlots = {
            "08-09 AM", "09-10 AM", "10-11 AM", "11-12 PM", "12-01 PM", // Recess
            "01-02 PM", "02-03 PM", "03-04 PM", "04-05 PM", "05-06 PM",
            "06-07 PM", "07-08 PM"
        };
        String[] days = {"Time Slot", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        
        // Map full day names to the abbreviations used in the file
        Map<String, String> dayToAbbrMap = Map.of(
            "Monday", "Mon", "Tuesday", "Tue", "Wednesday", "Wed", "Thursday", "Thu", "Friday", "Fri"
        );
        
        timetableModel.setRowCount(0);
        // --- KEY CHANGE: Use the new method to read the stored timetable ---
        Map<String, Map<String, String>> weeklyData = DataManager.getStoredTimetable();

        for (String timeSlot : timeSlots) {
            Object[] rowData = new Object[days.length];
            rowData[0] = timeSlot;
            
            if (timeSlot.equals("12-01 PM")) {
                for (int i = 1; i < days.length; i++) rowData[i] = "--- RECESS ---";
            } else {
                for (int i = 1; i < days.length; i++) {
                    String fullDayName = days[i];
                    String dayAbbr = dayToAbbrMap.get(fullDayName);
                    
                    String cellContent = " ";
                    if (weeklyData.containsKey(dayAbbr)) {
                        // Direct lookup for the content at this specific day and time
                        cellContent = weeklyData.get(dayAbbr).getOrDefault(timeSlot, " ");
                    }
                    rowData[i] = cellContent;
                }
            }
            timetableModel.addRow(rowData);
        }
    }

    private void handleGenerateTimetable() {
        int choice = JOptionPane.showConfirmDialog(
            this, 
            "This will ERASE all courses and related data (enrollments, results, etc.).\n" +
            "A new timetable will be generated with one class for each available subject.\n\n" +
            "This action cannot be undone. Are you sure?",
            "Confirm Timetable Regeneration", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            if (DataManager.generateAndAssignTimetable()) {
                JOptionPane.showMessageDialog(this, "New timetable generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                contentPanel.remove(weeklyTimetablePanel);
                weeklyTimetablePanel = createWeeklyTimetablePanel();
                contentPanel.add(weeklyTimetablePanel, "WEEKLY_TIMETABLE");
                cardLayout.show(contentPanel, "WEEKLY_TIMETABLE");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to generate timetable.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createStudentAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Top Panel: Student Selection and Summary ---
        JPanel topPanel = new JPanel(new BorderLayout(10,10));
        topPanel.setBackground(BG_COLOR);

        // Student selection dropdown
        JPanel studentSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studentSelectionPanel.setBackground(BG_COLOR);
        JLabel selectLabel = new JLabel("Select Student:");
        selectLabel.setForeground(TEXT_COLOR);
        List<User> allStudents = DataManager.getAllUsersByRole("Student");
        JComboBox<User> studentSelector = new JComboBox<>(new Vector<>(allStudents));
        studentSelectionPanel.add(selectLabel);
        studentSelectionPanel.add(studentSelector);

        // Financial summary labels
        JPanel summaryLabelsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        summaryLabelsPanel.setBackground(BG_COLOR);
        JLabel totalFeesLabel = new JLabel("Total Fees: $0.00");
        JLabel totalPaidLabel = new JLabel("Total Paid: $0.00");
        JLabel balanceLabel = new JLabel("Balance Due: $0.00");
        styleLargeLabel(totalFeesLabel);
        styleLargeLabel(totalPaidLabel);
        styleLargeLabel(balanceLabel);
        summaryLabelsPanel.add(totalFeesLabel);
        summaryLabelsPanel.add(totalPaidLabel);
        summaryLabelsPanel.add(balanceLabel);
        
        topPanel.add(studentSelectionPanel, BorderLayout.WEST);
        topPanel.add(summaryLabelsPanel, BorderLayout.EAST);
        
        // --- Center Panel: Enrollment Details Table ---
        String[] columnNames = {"Enrollment ID", "Course", "Fee", "Amount Paid", "Balance", "Status"};
        DefaultTableModel enrollmentTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable enrollmentTable = new JTable(enrollmentTableModel);
        styleTable(enrollmentTable);

        JScrollPane scrollPane = new JScrollPane(enrollmentTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        
        // --- Bottom Panel: Action Button ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BG_COLOR);
        JButton acceptPaymentButton = createStyledButton("Accept Payment for Selected Enrollment");
        buttonPanel.add(acceptPaymentButton);

        // --- Main Assembly ---
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // --- Action Listener for the Student Selector Dropdown ---
        studentSelector.addActionListener(e -> {
            User selectedStudent = (User) studentSelector.getSelectedItem();
            if (selectedStudent == null) return;
            
            // Update summary labels
            Map<String, Double> summary = DataManager.getPaymentStatus(selectedStudent.getId());
            totalFeesLabel.setText(String.format("Total Fees: $%.2f", summary.getOrDefault("totalFees", 0.0)));
            totalPaidLabel.setText(String.format("Total Paid: $%.2f", summary.getOrDefault("totalPaid", 0.0)));
            double balance = summary.getOrDefault("balance", 0.0);
            balanceLabel.setText(String.format("Balance Due: $%.2f", balance));
            balanceLabel.setForeground(balance > 0 ? Color.ORANGE : TEXT_COLOR);
            
            // Update enrollment details table
            enrollmentTableModel.setRowCount(0);
            Map<String, String> enrollments = DataManager.getStudentEnrollments(selectedStudent.getId());
            for (Map.Entry<String, String> entry : enrollments.entrySet()) {
                String enrollmentId = entry.getKey();
                String courseInfo = entry.getValue();
                
                Map<String, Double> status = DataManager.getEnrollmentPaymentStatus(enrollmentId);
                double fee = status.getOrDefault("fee", 0.0);
                double paid = status.getOrDefault("paid", 0.0);
                double bal = status.getOrDefault("balance", 0.0);
                String paymentStatus = (bal <= 0) ? "PAID" : "DUE";
                
                enrollmentTableModel.addRow(new Object[]{
                    enrollmentId, courseInfo.split(" - ")[0],
                    String.format("%.2f", fee),
                    String.format("%.2f", paid),
                    String.format("%.2f", bal),
                    paymentStatus
                });
            }
        });

        // --- Action Listener for the Accept Payment Button ---
        acceptPaymentButton.addActionListener(e -> {
            int selectedRow = enrollmentTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select an enrollment from the table to accept payment for.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String enrollmentId = (String) enrollmentTableModel.getValueAt(selectedRow, 0);
            double balanceDue = Double.parseDouble(((String) enrollmentTableModel.getValueAt(selectedRow, 4)));

            if (balanceDue <= 0) {
                JOptionPane.showMessageDialog(this, "This enrollment is already fully paid.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            String amountStr = JOptionPane.showInputDialog(this, "Enter amount to pay for Enrollment " + enrollmentId + ":\n(Balance Due: $" + String.format("%.2f", balanceDue) + ")", "Accept Payment", JOptionPane.PLAIN_MESSAGE);
            
            if (amountStr != null && !amountStr.trim().isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount > balanceDue) {
                        JOptionPane.showMessageDialog(this, "Payment amount cannot be greater than the balance due.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    String receipt = DataManager.acceptPayment(enrollmentId, amount);
                    JTextArea receiptArea = new JTextArea(receipt, 10, 30);
                    receiptArea.setEditable(false);
                    JOptionPane.showMessageDialog(this, new JScrollPane(receiptArea), "Payment Successful", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Refresh the view after successful payment
                    studentSelector.getActionListeners()[0].actionPerformed(null);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid amount entered. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Trigger the action listener once to load data for the first student in the list
        if (studentSelector.getItemCount() > 0) {
            studentSelector.setSelectedIndex(0);
            studentSelector.getActionListeners()[0].actionPerformed(null);
        }
        
        return panel;
    }

    private void styleLargeLabel(JLabel label) {
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Arial", Font.BOLD, 18));
    }

    private class CenterTableCellRenderer extends DefaultTableCellRenderer {
        public CenterTableCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }
    }
}
