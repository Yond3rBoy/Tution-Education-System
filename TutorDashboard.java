import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class TutorDashboard extends JFrame {
    private User tutorUser;
    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton btnAnnouncements, btnChat, btnLogout;
    private JButton btnMyFeedback;
    private JSplitPane requestSplitPane;

    private JTable courseTable;
    private DefaultTableModel courseTableModel;
    private LocalDate currentWeekStart;
    
    private static final Color BG_COLOR = new Color(24, 34, 54);
    private static final Color FIELD_BG_COLOR = new Color(42, 53, 76);
    private static final Color PRIMARY_COLOR = new Color(67, 102, 163);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);
    private static final Color SIDEBAR_COLOR = new Color(30, 41, 61);
    
    public TutorDashboard(User user) {
        this.tutorUser = user;
        setTitle("The Learning Hub - Tutor Dashboard");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        this.currentWeekStart = java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        courseTableModel = new DefaultTableModel(new String[]{"ID", "Course Name", "Level", "Subject", "Fee", "Schedule"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createSidebarPanel(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_COLOR);

        contentPanel.add(createWelcomePanel(), "WELCOME_PANEL");
        contentPanel.add(createManageCoursesPanel(), "MANAGE_COURSES");
        contentPanel.add(createResultsPanel(), "RESULTS");
        contentPanel.add(createViewStudentsPanel(), "VIEW_STUDENTS");
        contentPanel.add(createPayrollPanel(), "PAYROLL");
        contentPanel.add(createMonthlySchedulePanel(), "MONTHLY_SCHEDULE");
        contentPanel.add(createProfilePanel(), "MY_PROFILE");
        contentPanel.add(createRecordAttendancePanel(), "RECORD_ATTENDANCE");
        
        
        JPanel myFeedbackPanel = AdminDashboard.createFeedbackReviewPanel(this.tutorUser, false);
        contentPanel.add(myFeedbackPanel, "VIEW_FEEDBACK");
        myFeedbackPanel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { 
                refreshFeedbackNotification();
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { logout(); }
        });
        

        refreshChatNotification();
        refreshAnnouncementNotification();
        refreshFeedbackNotification();

        cardLayout.show(contentPanel, "WELCOME_PANEL");
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + tutorUser.getFullName());
        welcomeLabel.setForeground(TEXT_COLOR);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JPanel topRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRightButtons.setBackground(BG_COLOR);
        btnAnnouncements = createStyledButton("Announcements");
        try {
            ImageIcon announcementIcon = new ImageIcon(new ImageIcon(getClass().getResource("/assets/announcement_logo.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            btnAnnouncements.setIcon(announcementIcon);
        } catch (Exception e) {
            System.err.println("Could not load resource /assets/announcement_logo.png");
        }

        btnChat = createStyledButton("Chat");
        try {
            ImageIcon chatIcon = new ImageIcon(new ImageIcon(getClass().getResource("/assets/chat_logo.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            btnChat.setIcon(chatIcon);
        } catch (Exception e) {
            System.err.println("Could not load resource /assets/chat_logo.png");
        }

        // --- NEW: Profile button added here ---
        JButton btnMyProfile = createStyledButton("My Profile");
        btnMyProfile.addActionListener(e -> cardLayout.show(contentPanel, "MY_PROFILE"));

        btnLogout = createStyledButton("Logout");
        try {
            ImageIcon logoutIcon = new ImageIcon(new ImageIcon(getClass().getResource("/assets/logout_logo.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            btnLogout.setIcon(logoutIcon);
        } catch (Exception e) {
            System.err.println("Could not load resource /assets/logout_logo.png");
        }
        topRightButtons.add(btnAnnouncements);
        topRightButtons.add(btnChat);
        topRightButtons.add(btnMyProfile); // Placed between Chat and Logout
        topRightButtons.add(btnLogout);
        headerPanel.add(topRightButtons, BorderLayout.EAST);

        btnAnnouncements.addActionListener(e -> openAnnouncements());
        btnChat.addActionListener(e -> openChatDialog());
        btnLogout.addActionListener(e -> logout());
        
        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel menuLabel = new JLabel("MENU");
        menuLabel.setFont(new Font("Arial", Font.BOLD, 18));
        menuLabel.setForeground(Color.WHITE);
        menuLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        sidebar.add(menuLabel);

        sidebar.add(createSidebarButton("Manage My Courses", "MANAGE_COURSES"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("View Enrolled Students", "VIEW_STUDENTS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Upload/View Results", "RESULTS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Monthly Schedule", "MONTHLY_SCHEDULE"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("My Payroll", "PAYROLL"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Record Attendance", "RECORD_ATTENDANCE"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10))); 
        btnMyFeedback = createSidebarButton("My Feedback", "VIEW_FEEDBACK");
        sidebar.add(btnMyFeedback);
        
        return sidebar;
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        JLabel welcomeMessage = new JLabel("<html><center>Welcome to the Tutor Dashboard.<br>Please select a function from the menu on the left.</center></html>");
        welcomeMessage.setFont(new Font("Arial", Font.PLAIN, 24));
        welcomeMessage.setForeground(TEXT_COLOR);
        panel.add(welcomeMessage);
        return panel;
    }
    
    private void logout() {
        java.awt.Window[] windows = java.awt.Window.getWindows();
        for (java.awt.Window window : windows) {
            window.dispose();
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private JPanel createManageCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { refreshCourseTable(); }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        courseTable = new JTable(courseTableModel);
        styleTable(courseTable);
        
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        JButton btnAdd = createStyledButton("Add New Course");
        JButton btnUpdate = createStyledButton("Update Selected Course");
        JButton btnDelete = createStyledButton("Delete Selected Course");
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> showAddCourseDialog());
        btnUpdate.addActionListener(e -> showUpdateCourseDialog());
        btnDelete.addActionListener(e -> deleteSelectedCourse());

        return panel;
    }
    
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea resultsArea = new JTextArea("Select a course to view or upload results.");
        styleJTextArea(resultsArea);
        resultsArea.setEditable(false);

        JComboBox<String> courseSelector = new JComboBox<>();
        
        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { 
                refreshCourseSelector(courseSelector);
                if (courseSelector.getItemCount() > 0) {
                    courseSelector.setSelectedIndex(0);
                }
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(BG_COLOR);
        JLabel selectLabel = new JLabel("Select Course:");
        selectLabel.setForeground(TEXT_COLOR);
        topPanel.add(selectLabel);
        topPanel.add(courseSelector);

        JButton viewButton = createStyledButton("Refresh Results");
        JButton uploadButton = createStyledButton("Upload New Result");
        topPanel.add(viewButton);
        topPanel.add(uploadButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        courseSelector.addActionListener(e -> {
            String selectedCourse = (String) courseSelector.getSelectedItem();
            if (selectedCourse == null) return;
            String courseId = selectedCourse.split(":")[0];
            String report = DataManager.getTutorCourseResultsReport(courseId);
            resultsArea.setText(report);
            resultsArea.setCaretPosition(0);
        });
        
        viewButton.addActionListener(e -> {
            String selectedCourse = (String) courseSelector.getSelectedItem();
            if (selectedCourse == null) return;
            String courseId = selectedCourse.split(":")[0];
            String report = DataManager.getTutorCourseResultsReport(courseId);
            resultsArea.setText(report);
            resultsArea.setCaretPosition(0);
        });
        uploadButton.addActionListener(e -> showUploadResultDialog(courseSelector));
        
        return panel;
    }

    private JPanel createViewStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JList<String> studentList = new JList<>();
        styleJList(studentList);
        
        JComboBox<String> courseSelector = new JComboBox<>();

        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                refreshCourseSelector(courseSelector);
                if (courseSelector.getItemCount() > 0) {
                    courseSelector.setSelectedIndex(0);
                }
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(BG_COLOR);
        JLabel selectLabel = new JLabel("Select a course to view students:");
        selectLabel.setForeground(TEXT_COLOR);
        topPanel.add(selectLabel);
        topPanel.add(courseSelector);
        
        JScrollPane scrollPane = new JScrollPane(studentList);
        scrollPane.getViewport().setBackground(BG_COLOR);
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        courseSelector.addActionListener(e -> {
            String selectedCourse = (String) courseSelector.getSelectedItem();
            if (selectedCourse == null) return;
            String courseId = selectedCourse.split(":")[0];
            List<String> students = DataManager.getStudentsByCourse(courseId);
            studentList.setListData(new Vector<>(students));
        });

        return panel;
    }


    private JPanel createPayrollPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea reportArea = new JTextArea("Select a month and year to generate your payroll report.");
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setBackground(FIELD_BG_COLOR);
        reportArea.setForeground(TEXT_COLOR);
        reportArea.setMargin(new Insets(10,10,10,10));
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlsPanel.setBackground(BG_COLOR);
        JComboBox<Integer> monthComboBox = new JComboBox<>();
        for (int i = 1; i <= 12; i++) monthComboBox.addItem(i);
        JComboBox<Integer> yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 2; i <= currentYear; i++) yearComboBox.addItem(i);
        monthComboBox.setSelectedItem(LocalDate.now().getMonthValue());
        yearComboBox.setSelectedItem(currentYear);
        JButton btnGenerate = createStyledButton("Generate My Payroll Report");
        JLabel monthLabel = new JLabel("Month:"); monthLabel.setForeground(TEXT_COLOR);
        JLabel yearLabel = new JLabel("Year:"); yearLabel.setForeground(TEXT_COLOR);
        controlsPanel.add(monthLabel);
        controlsPanel.add(monthComboBox);
        controlsPanel.add(yearLabel);
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

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(createLabel("Full Name:"), gbc);
        JTextField txtFullName = createTextField(tutorUser.getFullName(), 20);
        gbc.gridx = 1; panel.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy++; panel.add(createLabel("New Password:"), gbc);
        JPasswordField txtPassword = createPasswordField(20);
        gbc.gridx = 1; panel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy++; panel.add(createLabel("Specialization(s):"), gbc);
        JTextField txtSpecialization = createTextField(tutorUser.getSpecialization(), 20);
        gbc.gridx = 1; panel.add(txtSpecialization, gbc);

        gbc.gridy++; gbc.gridx = 0;
        gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton btnUpdate = createStyledButton("Update My Profile");
        panel.add(btnUpdate, gbc);

        btnUpdate.addActionListener(e -> {
            String newFullName = txtFullName.getText().trim();
            String newPasswordStr = new String(txtPassword.getPassword());
            String newSpecialization = txtSpecialization.getText().trim();
            String finalPassword = newPasswordStr.isEmpty() ? tutorUser.getPassword() : newPasswordStr;
            User updatedUser = new User(tutorUser.getId(), tutorUser.getUsername(), finalPassword, tutorUser.getRole(), newFullName, newSpecialization);
            if (DataManager.updateUser(updatedUser)) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                this.tutorUser = updatedUser;
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }
    
    private void refreshCourseTable() {
        courseTableModel.setRowCount(0);
        List<String[]> courses = DataManager.getCoursesByTutor(tutorUser.getId());
        for (String[] courseData : courses) {
            courseTableModel.addRow(courseData);
        }
    }
    
    private void refreshCourseSelector(JComboBox<String> selector) {
        selector.removeAllItems();
        List<String[]> courses = DataManager.getCoursesByTutor(tutorUser.getId());
        for (String[] courseData : courses) {
            selector.addItem(courseData[0] + ": " + courseData[1]);
        }
    }
    
    private void showAddCourseDialog() {
        JTextField nameField = new JTextField(20);
        JTextField levelField = new JTextField(20);
        JTextField subjectField = new JTextField(20);
        JTextField feeField = new JTextField(20);
        JTextField scheduleField = new JTextField(20);
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Course Name:")); panel.add(nameField);
        panel.add(new JLabel("Level:")); panel.add(levelField);
        panel.add(new JLabel("Subject:")); panel.add(subjectField);
        panel.add(new JLabel("Fee:")); panel.add(feeField);
        panel.add(new JLabel("Schedule:")); panel.add(scheduleField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double fee = Double.parseDouble(feeField.getText());
                if (DataManager.addCourse(nameField.getText(), tutorUser.getId(), levelField.getText(), subjectField.getText(), fee, scheduleField.getText())) {
                    JOptionPane.showMessageDialog(this, "Course added successfully!");
                    refreshCourseTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add course.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid fee amount.", "Error", JOptionPane.ERROR_MESSAGE);
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
        JTextField scheduleField = new JTextField((String) courseTableModel.getValueAt(selectedRow, 5), 20);

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
                    refreshCourseTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update course.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid fee amount.", "Error", JOptionPane.ERROR_MESSAGE);
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
                refreshCourseTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete course.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showUploadResultDialog(JComboBox<String> courseSelector) {
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
                     JOptionPane.showMessageDialog(this, "Error: Could not find a valid enrollment record.", "Error", JOptionPane.ERROR_MESSAGE);
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
    

    private void openChatDialog() {
        ChatInterfaceFrame chatInterface = new ChatInterfaceFrame(this.tutorUser, this::refreshChatNotification);
        chatInterface.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                refreshChatNotification();
            }
        });
        chatInterface.setVisible(true);
    }
    
    public void refreshChatNotification() {
        int unreadCount = DataManager.getUnreadMessageCount(this.tutorUser); 
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
        long unreadCount = DataManager.getAllAnnouncements().stream().filter(a -> !readIds.contains(a.getId())).count();
        if (unreadCount > 0) {
            btnAnnouncements.setText("Announcements (" + unreadCount + ")");
            btnAnnouncements.setForeground(Color.CYAN);
            btnAnnouncements.setFont(new Font(btnAnnouncements.getFont().getName(), Font.BOLD, btnAnnouncements.getFont().getSize()));
        } else {
            btnAnnouncements.setText("Announcements");
            btnAnnouncements.setForeground(Color.WHITE);
            btnAnnouncements.setFont(new Font(btnAnnouncements.getFont().getName(), Font.PLAIN, btnAnnouncements.getFont().getSize()));
        }
    }

    
    
    private JButton createSidebarButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(SIDEBAR_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        button.addActionListener(e -> cardLayout.show(contentPanel, actionCommand));
        
        return button;
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

    private void styleJList(JList<String> list) {
        list.setBackground(FIELD_BG_COLOR);
        list.setForeground(TEXT_COLOR);
        list.setFont(new Font("Arial", Font.PLAIN, 14));
        list.setSelectionBackground(PRIMARY_COLOR);
        list.setSelectionForeground(Color.WHITE);
    }
    
    private void styleJTextArea(JTextArea textArea) {
        textArea.setBackground(FIELD_BG_COLOR);
        textArea.setForeground(TEXT_COLOR);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setMargin(new Insets(10,10,10,10));
        textArea.setCaretColor(Color.WHITE);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private JTextField createTextField(String text, int columns) {
        JTextField field = new JTextField(text, columns);
        field.setBackground(FIELD_BG_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    private JPasswordField createPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setBackground(FIELD_BG_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    private JPanel createRecordAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        topPanel.setBackground(BG_COLOR);
        JComboBox<LocalDate> dateSelector = new JComboBox<>();
        dateSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LocalDate) {
                    LocalDate date = (LocalDate) value;
                    setText(date.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")));
                }
                return this;
            }
        });
        JComboBox<String> classSelector = new JComboBox<>();
        topPanel.add(new JLabel("Select Date:")).setForeground(TEXT_COLOR);
        topPanel.add(dateSelector);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(new JLabel("Select Class:")).setForeground(TEXT_COLOR);
        topPanel.add(classSelector);
        String[] columnNames = {"Student ID", "Student Name", "Status"};
        DefaultTableModel attendanceTableModel = new DefaultTableModel(columnNames, 0);
        JTable attendanceTable = new JTable(attendanceTableModel);
        styleTable(attendanceTable);
        attendanceTable.setCellSelectionEnabled(true);
        attendanceTable.setRowSelectionAllowed(false);
        attendanceTable.getColumnModel().getColumn(2).setCellRenderer(new AttendanceStatusRenderer());
        String[] statuses = {"Present", "Absent", "Late"};
        JComboBox<String> statusComboBox = new JComboBox<>(statuses);
        statusComboBox.setFont(new Font("Arial", Font.BOLD, 13));
        statusComboBox.setBackground(FIELD_BG_COLOR);
        statusComboBox.setForeground(TEXT_COLOR);
        attendanceTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusComboBox));
        
        // Listeners for date and class selectors...
        panel.addAncestorListener(new javax.swing.event.AncestorListener() { /* ... no changes here ... */ 
            public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                dateSelector.removeAllItems();
                LocalDate today = LocalDate.now();
                for (int i = 0; i < 7; i++) {
                    LocalDate date = today.minusDays(i);
                    if (!DataManager.getTutorClassesForDate(tutorUser.getFullName(), date).isEmpty()) {
                        dateSelector.addItem(date);
                    }
                }
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });
        dateSelector.addActionListener(e -> { /* ... no changes here ... */ 
            LocalDate selectedDate = (LocalDate) dateSelector.getSelectedItem();
            classSelector.removeAllItems();
            attendanceTableModel.setRowCount(0);
            if (selectedDate == null) return;
            List<String[]> classes = DataManager.getTutorClassesForDate(tutorUser.getFullName(), selectedDate);
            for (String[] classInfo : classes) {
                classSelector.addItem(classInfo[0] + " - " + classInfo[1]);
            }
        });

        // --- FIX #1 is inside this listener ---
        classSelector.addActionListener(e -> {
            String selectedClass = (String) classSelector.getSelectedItem();
            attendanceTableModel.setRowCount(0);
            if (selectedClass == null) return;

            String subjectName = selectedClass.substring(selectedClass.indexOf("Weekly ") + 7);
            String courseId = DataManager.getCourseIdByTutorAndSubject(tutorUser.getId(), subjectName);
            if (courseId == null) return;

            List<User> students = DataManager.getStudentsByCourse(courseId).stream()
                .map(s -> DataManager.getUserById(s.substring(s.indexOf("(") + 1, s.indexOf(")"))))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            for (User student : students) {
                // We add the student's ID to the table model for reliable saving.
                // The table columns are "Student ID", "Student Name", "Status".
                attendanceTableModel.addRow(new Object[]{student.getId(), student.getFullName(), "Present"});
            }
        });
        
        // --- Save Button setup ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(BG_COLOR);
        JButton saveButton = createStyledButton("Save Attendance");
        bottomPanel.add(saveButton);

        // --- FIX #2 is inside this listener ---
        saveButton.addActionListener(e -> {
            LocalDate selectedDate = (LocalDate) dateSelector.getSelectedItem();
            String selectedClass = (String) classSelector.getSelectedItem();
            if (selectedDate == null || selectedClass == null) {
                JOptionPane.showMessageDialog(this, "Please select both a date and a class.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (attendanceTable.getRowCount() == 0) return;

            String subjectName = selectedClass.substring(selectedClass.indexOf("Weekly ") + 7);
            String courseId = DataManager.getCourseIdByTutorAndSubject(tutorUser.getId(), subjectName);

            Map<String, String> attendanceData = new HashMap<>();
            for (int i = 0; i < attendanceTable.getRowCount(); i++) {
                // We now correctly read the student's ID from the first column (index 0).
                String studentId = (String) attendanceTableModel.getValueAt(i, 0);
                String status = (String) attendanceTableModel.getValueAt(i, 2);
                attendanceData.put(studentId, status);
            }

            if (DataManager.recordAttendance(courseId, attendanceData, selectedDate.toString())) {
                JOptionPane.showMessageDialog(this, "Attendance recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to record attendance.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMonthlySchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Top controls for weekly navigation
        JPanel topControlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        topControlsPanel.setBackground(BG_COLOR);

        JButton prevWeekButton = new JButton("<< Prev Week");
        JButton nextWeekButton = new JButton("Next Week >>");
        JLabel weekLabel = new JLabel("", SwingConstants.CENTER);
        weekLabel.setFont(new Font("Arial", Font.BOLD, 18));
        weekLabel.setForeground(TEXT_COLOR);

        topControlsPanel.add(prevWeekButton);
        topControlsPanel.add(Box.createHorizontalStrut(20));
        topControlsPanel.add(weekLabel);
        topControlsPanel.add(Box.createHorizontalStrut(20));
        topControlsPanel.add(nextWeekButton);

        // Main panel to hold class cards
        JPanel scheduleContentPanel = new JPanel();
        scheduleContentPanel.setLayout(new BoxLayout(scheduleContentPanel, BoxLayout.Y_AXIS));
        scheduleContentPanel.setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(scheduleContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Logic to update the view
        Runnable updateScheduleView = () -> {
            LocalDate start = currentWeekStart;
            LocalDate end = start.plusDays(6);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");
            weekLabel.setText(start.format(dtf) + "  -  " + end.format(dtf));

            scheduleContentPanel.removeAll();

            Map<LocalDate, List<String[]>> allSchedules = DataManager.getStoredTimetable();
            Map<LocalDate, List<String[]>> weeklySchedule = allSchedules.entrySet().stream()
                    .filter(entry -> !entry.getKey().isBefore(start) && !entry.getKey().isAfter(end))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, java.util.TreeMap::new));

            if (weeklySchedule.isEmpty()) {
                scheduleContentPanel.setLayout(new GridBagLayout());
                JLabel noDataLabel = new JLabel("No classes scheduled for this week.");
                noDataLabel.setFont(new Font("Arial", Font.ITALIC, 18));
                noDataLabel.setForeground(Color.LIGHT_GRAY);
                scheduleContentPanel.add(noDataLabel);
            } else {
                scheduleContentPanel.setLayout(new BoxLayout(scheduleContentPanel, BoxLayout.Y_AXIS));
                DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM");

                for (Map.Entry<LocalDate, List<String[]>> entry : weeklySchedule.entrySet()) {
                    LocalDate date = entry.getKey();
                    
                    // Filter to only show classes taught by the current tutor
                    List<String[]> myClasses = entry.getValue().stream()
                        .filter(classInfo -> classInfo[2].equals(tutorUser.getFullName()))
                        .collect(Collectors.toList());

                    if (myClasses.isEmpty()) continue; // Skip days where this tutor has no classes

                    RoundedPanel dayHeader = new RoundedPanel(15, new Color(20, 110, 255));
                    dayHeader.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));
                    dayHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                    JLabel dayLabel = new JLabel(date.format(dayFormatter).toUpperCase());
                    dayLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                    dayLabel.setForeground(Color.WHITE);
                    dayHeader.add(dayLabel);
                    scheduleContentPanel.add(dayHeader);
                    scheduleContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                    for (String[] classInfo : myClasses) {
                        String time = classInfo[0];
                        String rawSubject = classInfo[1];
                        String tutor = classInfo[2];

                        String subjectName = rawSubject.replace("Weekly ", "");
                        String subjectCode;
                        String[] nameParts = subjectName.split(" ");
                        if (nameParts.length > 1) {
                            StringBuilder codeBuilder = new StringBuilder();
                            for (String part : nameParts) {
                                if (!part.isEmpty()) {
                                    codeBuilder.append(part.charAt(0));
                                }
                            }
                            subjectCode = codeBuilder.toString().toUpperCase() + "-G1";
                        } else {
                            subjectCode = subjectName.toUpperCase() + "-G1";
                        }

                        ClassCard card = new ClassCard(subjectCode, subjectName, time, tutor);
                        scheduleContentPanel.add(card);
                        scheduleContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                    }
                }
            }
            scheduleContentPanel.revalidate();
            scheduleContentPanel.repaint();
        };

        // Action listeners for buttons
        prevWeekButton.addActionListener(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateScheduleView.run();
        });

        nextWeekButton.addActionListener(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateScheduleView.run();
        });
        
        // Final panel assembly
        panel.add(topControlsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { updateScheduleView.run(); }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        return panel;
    }

    public static JPanel createFeedbackReviewPanel(User user, boolean isAdminView) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);

        JPanel feedbackContentPanel = new JPanel();
        feedbackContentPanel.setLayout(new BoxLayout(feedbackContentPanel, BoxLayout.Y_AXIS));
        feedbackContentPanel.setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(feedbackContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_COLOR);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_COLOR);
        JLabel titleLabel = new JLabel("My Feedback", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        // --- Admin-specific controls to view feedback for others ---
        if (isAdminView) {
            titleLabel.setText("Review Staff Feedback");
            JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            selectionPanel.setBackground(BG_COLOR);
            JComboBox<User> userSelector = new JComboBox<>();
            List<User> staff = DataManager.getAllUsersByRole("Tutor");
            staff.addAll(DataManager.getAllUsersByRole("Receptionist"));
            staff.forEach(userSelector::addItem);
            
            userSelector.addActionListener(e -> {
                User selectedUser = (User) userSelector.getSelectedItem();
                if (selectedUser != null) {
                    displayFeedbackForUser(selectedUser, feedbackContentPanel, topPanel);
                }
            });
            
            selectionPanel.add(new JLabel("Select Staff Member:")).setForeground(TEXT_COLOR);
            selectionPanel.add(userSelector);
            topPanel.add(selectionPanel, BorderLayout.CENTER);
            
            // Initial load for the first user in the list
            if (!staff.isEmpty()) {
                displayFeedbackForUser(staff.get(0), feedbackContentPanel, topPanel);
            }

        } else { // Normal view for Tutor/Receptionist
            displayFeedbackForUser(user, feedbackContentPanel, topPanel);
            DataManager.markFeedbackAsRead(user.getId()); // Mark as read when they view it
        }

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Add this second new helper method to AdminDashboard.java as well
    private static void displayFeedbackForUser(User user, JPanel contentPanel, JPanel topPanel) {
        contentPanel.removeAll();
        
        // Clear old average rating if it exists
        for (Component comp : topPanel.getComponents()) {
            if (comp instanceof JLabel && ((JLabel) comp).getText().startsWith("Average")) {
                topPanel.remove(comp);
            }
        }

        List<Feedback> feedbackList = DataManager.getFeedbackForUser(user.getId());

        if (feedbackList.isEmpty()) {
            JLabel noFeedbackLabel = new JLabel("No feedback has been submitted yet.", SwingConstants.CENTER);
            noFeedbackLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            noFeedbackLabel.setForeground(Color.LIGHT_GRAY);
            contentPanel.add(noFeedbackLabel);
        } else {
            // Calculate and display average rating
            double averageRating = feedbackList.stream().mapToInt(Feedback::getRating).average().orElse(0.0);
            JLabel avgLabel = new JLabel(String.format("Average Rating: %.2f / 5.0", averageRating), SwingConstants.CENTER);
            avgLabel.setFont(new Font("Arial", Font.BOLD, 18));
            avgLabel.setForeground(PRIMARY_COLOR);
            avgLabel.setBorder(BorderFactory.createEmptyBorder(5,0,15,0));
            topPanel.add(avgLabel, BorderLayout.SOUTH);

            // Display each feedback entry as a card
            for (Feedback feedback : feedbackList) {
                JPanel card = new JPanel(new BorderLayout(5, 5));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, FIELD_BG_COLOR),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                card.setBackground(BG_COLOR);

                // Rating and Date panel
                JPanel cardTop = new JPanel(new BorderLayout());
                cardTop.setOpaque(false);
                
                String stars = "★".repeat(feedback.getRating()) + "☆".repeat(5 - feedback.getRating());
                JLabel ratingLabel = new JLabel(stars);
                ratingLabel.setFont(new Font("Arial", Font.BOLD, 16));
                ratingLabel.setForeground(new Color(255, 215, 0)); // Gold color for stars
                cardTop.add(ratingLabel, BorderLayout.WEST);

                JLabel dateLabel = new JLabel(feedback.getDate().toString() + " (" + feedback.getSubject() + ")");
                dateLabel.setForeground(TEXT_COLOR);
                cardTop.add(dateLabel, BorderLayout.EAST);

                // Content
                JTextArea contentArea = new JTextArea(feedback.getContent());
                contentArea.setEditable(false);
                contentArea.setLineWrap(true);
                contentArea.setWrapStyleWord(true);
                contentArea.setFont(new Font("Arial", Font.PLAIN, 14));
                contentArea.setBackground(BG_COLOR);
                contentArea.setForeground(TEXT_COLOR);
                
                card.add(cardTop, BorderLayout.NORTH);
                card.add(contentArea, BorderLayout.CENTER);
                
                contentPanel.add(card);
            }
        }
        contentPanel.revalidate();
        contentPanel.repaint();
        topPanel.revalidate();
        topPanel.repaint();
    }

    public void refreshFeedbackNotification() {
        int unreadCount = DataManager.getUnreadFeedbackCount(this.tutorUser.getId());
        if (unreadCount > 0) {
            btnMyFeedback.setText("My Feedback (" + unreadCount + ")");
            btnMyFeedback.setForeground(Color.ORANGE);
        } else {
            btnMyFeedback.setText("My Feedback");
            btnMyFeedback.setForeground(TEXT_COLOR);
        }
    }

    class AttendanceStatusRenderer extends DefaultTableCellRenderer {
        // Define custom colors for clarity
        private static final Color PRESENT_BG = new Color(39, 87, 61); // Dark Green
        private static final Color LATE_BG = new Color(103, 81, 23);  // Dark Orange/Yellow
        private static final Color ABSENT_BG = new Color(112, 41, 41); // Dark Red
        private static final Color TEXT_COLOR = new Color(230, 230, 230);

        public AttendanceStatusRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER); // Center the text for a cleaner look
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Start with the default cell component
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            cell.setForeground(TEXT_COLOR);

            // Set the background based on the attendance status
            if (value != null) {
                String status = value.toString();
                switch (status) {
                    case "Present":
                        cell.setBackground(PRESENT_BG);
                        break;
                    case "Late":
                        cell.setBackground(LATE_BG);
                        break;
                    case "Absent":
                        cell.setBackground(ABSENT_BG);
                        break;
                    default:
                        cell.setBackground(table.getBackground());
                        break;
                }
            }

            // When a cell is selected, make it slightly brighter instead of the default blue
            if (isSelected) {
                cell.setBackground(cell.getBackground().brighter());
            }

            return cell;
        }
    }
}