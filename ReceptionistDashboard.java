import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ReceptionistDashboard extends JFrame {
    private User receptionistUser;
    private JTable studentTable;
    private DefaultTableModel studentTableModel;
    private JButton btnChat;
    private JButton btnAnnouncements;
    private JTable requestTable;
    private DefaultTableModel requestTableModel;

    public ReceptionistDashboard(User user) {
        this.receptionistUser = user;
        setTitle("Receptionist Dashboard - Welcome, " + user.getFullName());
        setSize(900, 700);
        // CHANGED: Dispose this window only, don't exit the whole app
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main container panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top panel for all action buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout"); // NEW Logout button
        btnAnnouncements = new JButton("Announcements");
        JButton btnRelease = new JButton("Release Announcement");
        btnChat = new JButton("Chat");
        topPanel.add(btnAnnouncements);
        topPanel.add(btnRelease);
        topPanel.add(btnChat);
        topPanel.add(btnLogout); // Add logout button to panel

        // Tabbed pane for core functions
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Students", createStudentPanel());
        tabbedPane.addTab("My Profile", createProfilePanel());
        tabbedPane.addTab("Handle Enrollment Requests", createRequestsPanel());

        // Assemble the main view
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);

        // --- Action Listeners ---
        btnLogout.addActionListener(e -> logout()); // NEW Logout action
        btnChat.addActionListener(e -> openChatDialog());
        btnAnnouncements.addActionListener(e -> openAnnouncements());
        btnRelease.addActionListener(e -> showReleaseAnnouncementDialog());

        // Add a listener for the 'X' button
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                logout();
            }
        });

        // --- Initial Data Load & Notifications ---
        refreshStudentTable();
        refreshRequestsTable();
        refreshChatNotification();
        refreshAnnouncementNotification();
    }

    // --- Logout Method ---
    private void logout() {
        this.dispose(); // Close this dashboard window
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true)); // Open a new login window
    }

    private JPanel createStudentPanel() {
        // ... This method remains unchanged ...
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] columnNames = {"ID", "Username", "Full Name"};
        studentTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        studentTable = new JTable(studentTableModel);
        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnRegister = new JButton("Register New Student");
        JButton btnUpdate = new JButton("Update Enrollment");
        JButton btnPayment = new JButton("Accept Payment");
        JButton btnDelete = new JButton("Delete Student");
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnPayment);
        buttonPanel.add(btnDelete);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        btnRegister.addActionListener(e -> showRegisterStudentDialog());
        btnDelete.addActionListener(e -> deleteSelectedStudent());
        btnUpdate.addActionListener(e -> showUpdateEnrollmentDialog());
        btnPayment.addActionListener(e -> showPaymentDialog());

        return panel;
    }

    private void showRegisterStudentDialog() {
        // This dialog is complex, so we build it carefully
        JTextField txtFullName = new JTextField();
        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JTextField txtIc = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtContact = new JTextField();
        JTextField txtAddress = new JTextField();

        // --- KEY CHANGE: Setup the JList for multiple selections ---
        // Create a scrollable list of available courses
        JList<String> courseList = new JList<>(new Vector<>(DataManager.getAvailableCourses()));
        courseList.setVisibleRowCount(5);
        // This allows selecting multiple items, not necessarily in a continuous block
        courseList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); 

        // Build the form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(txtFullName);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(txtUsername);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(txtPassword);
        formPanel.add(new JLabel("IC/Passport:"));
        formPanel.add(txtIc);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(txtEmail);
        formPanel.add(new JLabel("Contact No:"));
        formPanel.add(txtContact);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(txtAddress);
        formPanel.add(new JLabel("Enroll in Subjects (Max 3):"));
        // Add the list inside a JScrollPane so it's scrollable if there are many courses
        formPanel.add(new JScrollPane(courseList));

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Register New Student", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            // --- KEY CHANGE: Correctly process the list of selected items ---
            
            // 1. Get the list of selected courses from the JList
            List<String> selectedCourses = courseList.getSelectedValuesList();

            // 2. Validate that no more than 3 courses were selected
            if (selectedCourses.size() > 3) {
                JOptionPane.showMessageDialog(this, "A student can enroll in a maximum of 3 subjects.", "Error", JOptionPane.ERROR_MESSAGE);
                return; // Stop the registration process
            }

            if (selectedCourses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You must select at least one course to enroll the student in.", "Warning", JOptionPane.WARNING_MESSAGE);
                return; // Stop the registration process
            }

            // 3. Extract just the Course IDs from the selected strings
            List<String> courseIDs = new ArrayList<>();
            for (String courseInfo : selectedCourses) {
                // The course string is formatted like "C-101: Primary 5 Mathematics..."
                // We split by ":" and take the first part.
                courseIDs.add(courseInfo.split(":")[0]);
            }
            
            // 4. Call the DataManager with the list of course IDs
            boolean success = DataManager.registerStudent(
                    txtFullName.getText(), txtUsername.getText(), new String(txtPassword.getPassword()),
                    txtIc.getText(), txtEmail.getText(), txtContact.getText(), txtAddress.getText(),
                    courseIDs); // Pass the list of IDs

            if (success) {
                JOptionPane.showMessageDialog(this, "Student registered and enrolled successfully!");
                refreshStudentTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register student.", "Error", JOptionPane.ERROR_MESSAGE);
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

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + studentName + "?\nThis action cannot be undone.", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (DataManager.deleteStudent(studentId)) {
                JOptionPane.showMessageDialog(this, "Student deleted successfully.");
                refreshStudentTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete student.", "Error", JOptionPane.ERROR_MESSAGE);
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

        // --- Create a more user-friendly UI with Checkboxes ---

        // 1. Get all available courses and the student's current courses
        List<String> allCoursesInfo = DataManager.getAvailableCourses();
        Set<String> currentCourseIDs = DataManager.getStudentCourseIDs(studentId);

        // 2. Create a panel to hold the checkboxes
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS)); // Stack checkboxes vertically
        
        // This map will link each checkbox to its course ID
        Map<JCheckBox, String> checkboxMap = new LinkedHashMap<>(); 

        for (String courseInfo : allCoursesInfo) {
            String courseId = courseInfo.split(":")[0];
            JCheckBox checkBox = new JCheckBox(courseInfo);

            // Pre-select the checkboxes for courses the student is already enrolled in
            if (currentCourseIDs.contains(courseId)) {
                checkBox.setSelected(true);
            }
            
            checkboxMap.put(checkBox, courseId); // Link the checkbox object to its ID
            checkboxPanel.add(checkBox);       // Add the checkbox to the panel
        }

        // 3. Show the dialog
        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        scrollPane.setPreferredSize(new Dimension(400, 300)); // Set a reasonable size for the scroll pane

        int result = JOptionPane.showConfirmDialog(this, scrollPane, "Update Enrollments for " + studentName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            // 4. Process the selected checkboxes
            List<String> newCourseIDs = new ArrayList<>();
            for (Map.Entry<JCheckBox, String> entry : checkboxMap.entrySet()) {
                if (entry.getKey().isSelected()) {
                    newCourseIDs.add(entry.getValue()); // Add the course ID if the box is checked
                }
            }

            // 5. Validate the selection
            if (newCourseIDs.size() > 3) {
                JOptionPane.showMessageDialog(this, "A student can enroll in a maximum of 3 subjects.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 6. Update the enrollments in the data file
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
    
    private void refreshStudentTable() {
        studentTableModel.setRowCount(0); // Clear table
        List<User> students = DataManager.getAllUsersByRole("Student");
        for (User student : students) {
            studentTableModel.addRow(new Object[]{student.getId(), student.getUsername(), student.getFullName()});
        }
    }

    // This panel is identical to the Admin's, demonstrating code re-use.
        // --- Panel for Updating Receptionist's Own Profile ---
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Display User ID (not editable)
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        JTextField txtId = new JTextField(receptionistUser.getId(), 20);
        txtId.setEditable(false);
        panel.add(txtId, gbc);

        // Display Username (not editable)
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField txtUsername = new JTextField(receptionistUser.getUsername(), 20);
        txtUsername.setEditable(false);
        panel.add(txtUsername, gbc);

        // Field for Full Name (editable)
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtFullName = new JTextField(receptionistUser.getFullName(), 20);
        panel.add(txtFullName, gbc);
        
        // Field for New Password (editable)
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("New Password (leave blank to keep current):"), gbc);
        gbc.gridx = 1;
        JPasswordField txtPassword = new JPasswordField(20);
        panel.add(txtPassword, gbc);

        // Update Button
        gbc.gridy++; gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton btnUpdate = new JButton("Update My Profile");
        panel.add(btnUpdate, gbc);

        // Action Listener for the Update Button
        btnUpdate.addActionListener(e -> {
            String newFullName = txtFullName.getText().trim();
            String newPasswordStr = new String(txtPassword.getPassword());

            if (newFullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Full Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // If the new password field is empty, use the existing password. Otherwise, use the new one.
            String finalPassword = newPasswordStr.isEmpty() ? receptionistUser.getPassword() : newPasswordStr;

            // Create the updated user object. The constructor handles the empty specialization for non-tutors.
            User updatedUser = new User(
                receptionistUser.getId(),
                receptionistUser.getUsername(),
                finalPassword,
                receptionistUser.getRole(),
                newFullName,
                receptionistUser.getSpecialization() // This will be empty for a receptionist
            );

            if (DataManager.updateUser(updatedUser)) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                // Update the dashboard's internal user object to reflect the changes immediately.
                this.receptionistUser = updatedUser; 
                // Also update the welcome message in the window title
                this.setTitle("Receptionist Dashboard - Welcome, " + updatedUser.getFullName());
            } else {
                 JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private void openChatDialog() {
        User currentUser = this.receptionistUser; // Use the correct user object

        List<User> eligibleUsers = DataManager.getUsersForChat(currentUser);
        if (eligibleUsers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No other users available to chat with.", "Chat", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User[] usersArray = eligibleUsers.toArray(new User[0]);

        User selectedUser = (User) JOptionPane.showInputDialog(
                this, "Select a user to chat with:", "Start a Chat",
                JOptionPane.PLAIN_MESSAGE, null, usersArray, usersArray[0]);

        if (selectedUser != null) {
            ChatFrame chatFrame = new ChatFrame(currentUser, selectedUser);
            chatFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    refreshChatNotification();
                }
            });
            chatFrame.setVisible(true);
        }
    }

    public void refreshChatNotification() {
        User currentUser = this.receptionistUser; // Use the correct user object
        int unreadCount = DataManager.getUnreadMessageCount(currentUser); 
        if (unreadCount > 0) {
            btnChat.setText("Chat (" + unreadCount + " unread)");
            btnChat.setForeground(Color.RED);
            btnChat.setFont(new Font(btnChat.getFont().getName(), Font.BOLD, btnChat.getFont().getSize()));
        } else {
            btnChat.setText("Chat");
            btnChat.setForeground(Color.BLACK);
            btnChat.setFont(new Font(btnChat.getFont().getName(), Font.PLAIN, btnChat.getFont().getSize()));
        }
    }

    private void openAnnouncements() {
        // CHANGE this.adminUser to the correct user for each dashboard
        AnnouncementsFrame announcementsFrame = new AnnouncementsFrame(this.receptionistUser);
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
        Set<String> readIds = DataManager.getReadAnnouncementIds(this.receptionistUser);
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
            if (DataManager.createAnnouncement(this.receptionistUser, title, content)) {
                JOptionPane.showMessageDialog(this, "Announcement released successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to release announcement.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

     private JPanel createRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Table to display pending requests
        String[] columnNames = {"Request ID", "Student ID", "Date", "Details"};
        requestTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        requestTable = new JTable(requestTableModel);
        panel.add(new JScrollPane(requestTable), BorderLayout.CENTER);

        // Panel for action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnViewDetails = new JButton("View Full Request Details");
        JButton btnMarkCompleted = new JButton("Mark as Completed");
        JButton btnRefresh = new JButton("Refresh List");
        buttonPanel.add(btnViewDetails);
        buttonPanel.add(btnMarkCompleted);
        buttonPanel.add(btnRefresh);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners for the buttons ---

        btnRefresh.addActionListener(e -> refreshRequestsTable());

        btnViewDetails.addActionListener(e -> {
            int selectedRow = requestTable.getSelectedRow();
            if (selectedRow >= 0) {
                // The details are in the 4th column (index 3)
                String details = (String) requestTableModel.getValueAt(selectedRow, 3);
                JTextArea textArea = new JTextArea(details);
                textArea.setWrapStyleWord(true);
                textArea.setLineWrap(true);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 200));
                JOptionPane.showMessageDialog(this, scrollPane, "Full Request Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a request to view.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnMarkCompleted.addActionListener(e -> {
            int selectedRow = requestTable.getSelectedRow();
            if (selectedRow >= 0) {
                // The request ID is in the 1st column (index 0)
                String requestId = (String) requestTableModel.getValueAt(selectedRow, 0);
                int choice = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to mark this request as completed?\nRequest ID: " + requestId, 
                    "Confirm Action", JOptionPane.YES_NO_OPTION);
                
                if (choice == JOptionPane.YES_OPTION) {
                    if (DataManager.updateRequestStatus(requestId, "COMPLETED")) {
                        JOptionPane.showMessageDialog(this, "Request status updated to COMPLETED.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshRequestsTable(); // Refresh the table to remove the completed item
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update request status.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a request to mark as completed.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }

    // --- New Method to refresh the data in the requests table ---
    private void refreshRequestsTable() {
        requestTableModel.setRowCount(0); // Clear existing data
        List<String[]> pendingRequests = DataManager.getAllPendingRequests();
        for (String[] requestData : pendingRequests) {
            // Format: requestID, studentUserID, details, status, date
            // We want to display: ID, StudentID, Date, Details
            requestTableModel.addRow(new Object[]{requestData[0], requestData[1], requestData[4], requestData[2]});
        }
    }
}
