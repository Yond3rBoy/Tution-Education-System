import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TutorDashboard extends JFrame {
    private User tutorUser;
    private JTable courseTable;
    private DefaultTableModel courseTableModel;
    private JComboBox<String> courseSelector;
    private JButton btnChat;
    private JButton btnAnnouncements;

    public TutorDashboard(User user) {
        this.tutorUser = user;
        setTitle("Tutor Dashboard - Welcome, " + user.getFullName());
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize the shared component FIRST to prevent NullPointerException
        courseSelector = new JComboBox<>();
        
        // Main container panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top panel for action buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");
        btnAnnouncements = new JButton("Announcements");
        btnChat = new JButton("Chat");
        topPanel.add(btnAnnouncements);
        topPanel.add(btnChat);
        topPanel.add(btnLogout);

        // Tabbed pane for core functions
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage My Courses", createCoursePanel());
        tabbedPane.addTab("Upload/View Results", createResultsPanel());
        tabbedPane.addTab("View Enrolled Students", createViewStudentsPanel());
        tabbedPane.addTab("My Payroll", createPayrollPanel());
        tabbedPane.addTab("My Profile", createProfilePanel());
        
        // Assemble the main view
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);

        // --- Action Listeners ---
        btnLogout.addActionListener(e -> logout());
        btnChat.addActionListener(e -> openChatDialog());
        btnAnnouncements.addActionListener(e -> openAnnouncements());

        // Add a listener for the 'X' button to trigger logout
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                logout();
            }
        });

        // --- Initial Data Load & Notifications ---
        refreshCourseData();
        refreshChatNotification();
        refreshAnnouncementNotification();
    }

    // --- Logout Method ---
    private void logout() {
        this.dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
    
    private void refreshCourseData() {
        refreshCourseTable();
        refreshCourseSelector();
    }

    // --- Panel for Managing Courses ---
    private JPanel createCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] columnNames = {"ID", "Course Name", "Level", "Subject", "Fee"};
        courseTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        courseTable = new JTable(courseTableModel);
        panel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnAdd = new JButton("Add New Course");
        JButton btnUpdate = new JButton("Update Selected Course");
        JButton btnDelete = new JButton("Delete Selected Course");
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> showAddCourseDialog());
        btnUpdate.addActionListener(e -> showUpdateCourseDialog());
        btnDelete.addActionListener(e -> deleteSelectedCourse());

        return panel;
    }
    
    // --- Panel for Uploading/Viewing Results ---
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JTextArea resultsArea = new JTextArea("Select a course to view or upload results.");
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Course:"));
        topPanel.add(courseSelector); // Uses the shared JComboBox

        JButton viewButton = new JButton("View Results Summary");
        JButton uploadButton = new JButton("Upload New Result");
        topPanel.add(viewButton);
        topPanel.add(uploadButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        viewButton.addActionListener(e -> {
            String selectedCourse = (String) courseSelector.getSelectedItem();
            if (selectedCourse == null) return;
            String courseId = selectedCourse.split(":")[0];
            String report = DataManager.getTutorCourseResultsReport(courseId);
            resultsArea.setText(report);
            resultsArea.setCaretPosition(0);
        });
        
        uploadButton.addActionListener(e -> showUploadResultDialog());

        return panel;
    }

    // --- Panel for Viewing Students ---
    private JPanel createViewStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select a course to view students:"));
        // Use the shared courseSelector, DO NOT re-initialize it here
        topPanel.add(courseSelector);
        
        JList<String> studentList = new JList<>();
        studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add a single, shared action listener to the JComboBox
        // This will update the student list whenever the selection changes
        courseSelector.addActionListener(e -> {
            String selectedCourse = (String) courseSelector.getSelectedItem();
            if (selectedCourse != null) {
                String courseId = selectedCourse.split(":")[0];
                List<String> students = DataManager.getStudentsByCourse(courseId);
                studentList.setListData(new Vector<>(students));
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(studentList), BorderLayout.CENTER);
        
        return panel;
    }

    // --- Panel for Payroll ---
    private JPanel createPayrollPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JTextArea reportArea = new JTextArea("Select a month and year to generate your payroll report.");
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JComboBox<Integer> monthComboBox = new JComboBox<>();
        for (int i = 1; i <= 12; i++) monthComboBox.addItem(i);
        
        JComboBox<Integer> yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 2; i <= currentYear; i++) yearComboBox.addItem(i);
        
        monthComboBox.setSelectedItem(LocalDate.now().getMonthValue());
        yearComboBox.setSelectedItem(currentYear);
        
        JButton btnGenerate = new JButton("Generate My Payroll Report");
        controlsPanel.add(new JLabel("Month:"));
        controlsPanel.add(monthComboBox);
        controlsPanel.add(new JLabel("Year:"));
        controlsPanel.add(yearComboBox);
        controlsPanel.add(btnGenerate);
        
        panel.add(controlsPanel, BorderLayout.NORTH);

        btnGenerate.addActionListener(e -> {
            int month = (int) monthComboBox.getSelectedItem();
            int year = (int) yearComboBox.getSelectedItem();
            String report = DataManager.generateTutorPayrollReport(tutorUser.getId(), month, year);
            reportArea.setText(report);
            reportArea.setCaretPosition(0);
        });
        
        return panel;
    }
    
    // --- Panel for Profile Updates ---
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Full Name:"), gbc);
        JTextField txtFullName = new JTextField(tutorUser.getFullName(), 20);
        gbc.gridx = 1; panel.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("New Password (leave blank):"), gbc);
        JPasswordField txtPassword = new JPasswordField(20);
        gbc.gridx = 1; panel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Specialization(s):"), gbc);
        JTextField txtSpecialization = new JTextField(tutorUser.getSpecialization(), 20);
        gbc.gridx = 1; panel.add(txtSpecialization, gbc);

        gbc.gridy++; gbc.gridx = 0;
        gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton btnUpdate = new JButton("Update My Profile");
        panel.add(btnUpdate, gbc);

        btnUpdate.addActionListener(e -> {
            String newFullName = txtFullName.getText().trim();
            String newPasswordStr = new String(txtPassword.getPassword());
            String newSpecialization = txtSpecialization.getText().trim();
            
            String finalPassword = newPasswordStr.isEmpty() ? tutorUser.getPassword() : newPasswordStr;
            
            User updatedUser = new User(
                tutorUser.getId(), tutorUser.getUsername(), finalPassword,
                tutorUser.getRole(), newFullName, newSpecialization);

            if (DataManager.updateUser(updatedUser)) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                this.tutorUser = updatedUser;
                this.setTitle("Tutor Dashboard - Welcome, " + updatedUser.getFullName());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // --- Helper Methods and Dialogs ---
    
    private void refreshCourseTable() {
        courseTableModel.setRowCount(0);
        List<String[]> courses = DataManager.getCoursesByTutor(tutorUser.getId());
        for (String[] courseData : courses) {
            courseTableModel.addRow(new Object[]{courseData[0], courseData[1], courseData[3], courseData[4], courseData[5]});
        }
    }
    
    private void refreshCourseSelector() {
        courseSelector.removeAllItems();
        List<String[]> courses = DataManager.getCoursesByTutor(tutorUser.getId());
        for (String[] courseData : courses) {
            courseSelector.addItem(courseData[0] + ": " + courseData[1]);
        }
    }

    private void showAddCourseDialog() {
        JTextField nameField = new JTextField(20);
        JTextField levelField = new JTextField(20);
        JTextField subjectField = new JTextField(20);
        JTextField feeField = new JTextField(20);
        JTextField scheduleField = new JTextField(20); // Field for schedule
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Course Name:")); panel.add(nameField);
        panel.add(new JLabel("Level (e.g., Primary 5):")); panel.add(levelField);
        panel.add(new JLabel("Subject (e.g., Math):")); panel.add(subjectField);
        panel.add(new JLabel("Fee (e.g., 200.00):")); panel.add(feeField);
        panel.add(new JLabel("Schedule (e.g., Mon 4-6 PM):")); panel.add(scheduleField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double fee = Double.parseDouble(feeField.getText());
                // Pass the schedule to the DataManager
                if (DataManager.addCourse(nameField.getText(), tutorUser.getId(), levelField.getText(), subjectField.getText(), fee, scheduleField.getText())) {
                    JOptionPane.showMessageDialog(this, "Course added successfully!");
                    refreshCourseData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add course.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid fee amount. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showUpdateCourseDialog() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseId = (String) courseTableModel.getValueAt(selectedRow, 0);
        JTextField nameField = new JTextField((String) courseTableModel.getValueAt(selectedRow, 1), 20);
        JTextField levelField = new JTextField((String) courseTableModel.getValueAt(selectedRow, 2), 20);
        JTextField subjectField = new JTextField((String) courseTableModel.getValueAt(selectedRow, 3), 20);
        JTextField feeField = new JTextField((String) courseTableModel.getValueAt(selectedRow, 4), 20);
        // We need to fetch the schedule for the selected course to pre-fill the field
        String currentSchedule = "";
        for (String[] course : DataManager.getCoursesByTutor(tutorUser.getId())) {
            if (course[0].equals(courseId) && course.length > 6) {
                currentSchedule = course[6];
                break;
            }
        }
        JTextField scheduleField = new JTextField(currentSchedule, 20);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Course Name:")); panel.add(nameField);
        panel.add(new JLabel("Level:")); panel.add(levelField);
        panel.add(new JLabel("Subject:")); panel.add(subjectField);
        panel.add(new JLabel("Fee:")); panel.add(feeField);
        panel.add(new JLabel("Schedule:")); panel.add(scheduleField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Course " + courseId, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double fee = Double.parseDouble(feeField.getText());
                if (DataManager.updateCourse(courseId, nameField.getText(), levelField.getText(), subjectField.getText(), fee, scheduleField.getText())) {
                    JOptionPane.showMessageDialog(this, "Course updated successfully!");
                    refreshCourseData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update course.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid fee amount. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String courseId = (String) courseTableModel.getValueAt(selectedRow, 0);
        String courseName = (String) courseTableModel.getValueAt(selectedRow, 1);
        
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the course:\n" + courseName + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (DataManager.deleteCourse(courseId)) {
                JOptionPane.showMessageDialog(this, "Course deleted successfully.");
                refreshCourseData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete course.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // --- CHAT SYSTEM METHODS ---
    private void openChatDialog() {
        User currentUser = this.tutorUser;

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
        User currentUser = this.tutorUser;
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

    // --- ANNOUNCEMENT SYSTEM METHODS ---
    private void openAnnouncements() {
        AnnouncementsFrame announcementsFrame = new AnnouncementsFrame(this.tutorUser);
        announcementsFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                refreshAnnouncementNotification();
            }
        });
        announcementsFrame.setVisible(true);
    }
    public void refreshAnnouncementNotification() {
        Set<String> readIds = DataManager.getReadAnnouncementIds(this.tutorUser);
        List<Announcement> allAnnouncements = DataManager.getAllAnnouncements();
        long unreadCount = allAnnouncements.stream().filter(a -> !readIds.contains(a.getId())).count();
        if (unreadCount > 0) {
            btnAnnouncements.setText("Announcements (" + unreadCount + ")");
            btnAnnouncements.setForeground(Color.BLUE);
            btnAnnouncements.setFont(new Font(btnAnnouncements.getFont().getName(), Font.BOLD, btnAnnouncements.getFont().getSize()));
        } else {
            btnAnnouncements.setText("Announcements");
            btnAnnouncements.setForeground(Color.BLACK);
            btnAnnouncements.setFont(new Font(btnAnnouncements.getFont().getName(), Font.PLAIN, btnAnnouncements.getFont().getSize()));
        }
    }
    
    // --- UPLOAD RESULT DIALOG ---
    private void showUploadResultDialog() {
        String selectedCourseInfo = (String) courseSelector.getSelectedItem();
        if (selectedCourseInfo == null) {
            JOptionPane.showMessageDialog(this, "Please select a course first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String courseId = selectedCourseInfo.split(":")[0];
        
        List<User> studentsInCourse = DataManager.getStudentsByCourse(courseId).stream()
                .map(s -> {
                    String studentId = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                    return DataManager.getAllUsersByRole("Student").stream()
                            .filter(u -> u.getId().equals(studentId))
                            .findFirst().orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (studentsInCourse.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students are enrolled in this course.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<User> studentComboBox = new JComboBox<>(new Vector<>(studentsInCourse));
        JTextField assessmentField = new JTextField(15);
        JTextField scoreField = new JTextField(5);
        JTextField totalField = new JTextField(5);
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Select Student:"));
        panel.add(studentComboBox);
        panel.add(new JLabel("Assessment Name:"));
        panel.add(assessmentField);
        panel.add(new JLabel("Score:"));
        panel.add(scoreField);
        panel.add(new JLabel("Out of (Total Marks):"));
        panel.add(totalField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Upload Result for " + selectedCourseInfo.split(":")[1].trim(), JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                User selectedStudent = (User) studentComboBox.getSelectedItem();
                if (selectedStudent == null) return;
                
                int score = Integer.parseInt(scoreField.getText());
                int total = Integer.parseInt(totalField.getText());

                String enrollmentId = DataManager.getEnrollmentId(selectedStudent.getId(), courseId);

                if (enrollmentId == null) {
                     JOptionPane.showMessageDialog(this, "Error: Could not find a valid enrollment record for this student in this course.", "Error", JOptionPane.ERROR_MESSAGE);
                     return;
                }
                
                if (DataManager.uploadResult(enrollmentId, assessmentField.getText(), score, total)) {
                    JOptionPane.showMessageDialog(this, "Result uploaded successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to upload result.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Score and Total Marks must be valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
