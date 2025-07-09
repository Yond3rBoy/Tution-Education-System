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

    private JTable courseTable;
    private DefaultTableModel courseTableModel;
    private JComboBox<String> courseSelector;

    private JList<String> studentList;
    private JTextArea resultsArea;

    private static final Color BG_COLOR = new Color(24, 34, 54);
    private static final Color FIELD_BG_COLOR = new Color(42, 53, 76);
    private static final Color PRIMARY_COLOR = new Color(67, 102, 163);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);
    
    public TutorDashboard(User user) {
        this.tutorUser = user;
        setTitle("The Learning Hub - Tutor Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        courseSelector = new JComboBox<>();
        studentList = new JList<>();
        resultsArea = new JTextArea("Select a course to view or upload results.");
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_COLOR);

        contentPanel.add(createManageCoursesPanel(), "MANAGE_COURSES");
        contentPanel.add(createResultsPanel(), "RESULTS");
        contentPanel.add(createViewStudentsPanel(), "VIEW_STUDENTS");
        contentPanel.add(createPayrollPanel(), "PAYROLL");
        contentPanel.add(createWeeklyTimetablePanel(), "WEEKLY_TIMETABLE");
        contentPanel.add(createProfilePanel(), "PROFILE");
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        courseSelector.addActionListener(e -> onCourseSelectionChange());

        refreshCourseData();
        refreshChatNotification();
        refreshAnnouncementNotification();

        cardLayout.show(contentPanel, "MANAGE_COURSES");
    }

    private void onCourseSelectionChange() {
        String selectedCourse = (String) courseSelector.getSelectedItem();
        if (selectedCourse == null) return;
        
        String courseId = selectedCourse.split(":")[0];
        
        List<String> students = DataManager.getStudentsByCourse(courseId);
        studentList.setListData(new Vector<>(students));
        
        String report = DataManager.getTutorCourseResultsReport(courseId);
        resultsArea.setText(report);
        resultsArea.setCaretPosition(0);
    }

    private JPanel createHeaderPanel() {
        JPanel fullHeaderPanel = new JPanel();
        fullHeaderPanel.setLayout(new BoxLayout(fullHeaderPanel, BoxLayout.Y_AXIS));
        fullHeaderPanel.setBackground(BG_COLOR);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(BG_COLOR);
        topHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + tutorUser.getFullName());
        welcomeLabel.setForeground(TEXT_COLOR);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topHeader.add(welcomeLabel, BorderLayout.WEST);

        JPanel topRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRightButtons.setBackground(BG_COLOR);
        
        btnAnnouncements = createStyledButton("Announcements");
        btnChat = createStyledButton("Chat");
        btnLogout = createStyledButton("Logout");
        
        btnAnnouncements.addActionListener(e -> openAnnouncements());
        btnChat.addActionListener(e -> openChatDialog());
        btnLogout.addActionListener(e -> logout());
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { logout(); }
        });
        
        topRightButtons.add(btnAnnouncements);
        topRightButtons.add(btnChat);
        topRightButtons.add(btnLogout);
        topHeader.add(topRightButtons, BorderLayout.EAST);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        navPanel.setBackground(BG_COLOR);
        navPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JButton btnManageCourses = createStyledButton("Manage Courses");
        JButton btnViewResults = createStyledButton("Upload / View Results");
        JButton btnViewStudents = createStyledButton("View Enrolled Students");
        JButton btnMyPayroll = createStyledButton("My Payroll");
        JButton btnWeeklyTimetable = createStyledButton("Weekly Timetable");
        JButton btnMyProfile = createStyledButton("My Profile");

        navPanel.add(btnManageCourses);
        navPanel.add(btnViewResults);
        navPanel.add(btnViewStudents);
        navPanel.add(btnMyPayroll);
        navPanel.add(btnWeeklyTimetable);
        navPanel.add(btnMyProfile);

        btnManageCourses.addActionListener(e -> cardLayout.show(contentPanel, "MANAGE_COURSES"));
        btnViewResults.addActionListener(e -> cardLayout.show(contentPanel, "RESULTS"));
        btnViewStudents.addActionListener(e -> cardLayout.show(contentPanel, "VIEW_STUDENTS"));
        btnMyPayroll.addActionListener(e -> cardLayout.show(contentPanel, "PAYROLL"));
        btnWeeklyTimetable.addActionListener(e -> cardLayout.show(contentPanel, "WEEKLY_TIMETABLE"));
        btnMyProfile.addActionListener(e -> cardLayout.show(contentPanel, "PROFILE"));

        fullHeaderPanel.add(topHeader);
        fullHeaderPanel.add(navPanel);
        return fullHeaderPanel;
    }
    
    private void logout() {
        this.dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private JPanel createManageCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columnNames = {"ID", "Course Name", "Level", "Subject", "Fee"};
        courseTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        courseTable = new JTable(courseTableModel);
        styleTable(courseTable);
        
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
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

        styleJTextArea(resultsArea);
        resultsArea.setEditable(false);

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

        viewButton.addActionListener(e -> onCourseSelectionChange());
        uploadButton.addActionListener(e -> showUploadResultDialog());
        
        return panel;
    }

    private JPanel createViewStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(BG_COLOR);
        JLabel selectLabel = new JLabel("Select a course to view students:");
        selectLabel.setForeground(TEXT_COLOR);
        topPanel.add(selectLabel);
        topPanel.add(courseSelector);
        
        styleJList(studentList);
        studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(studentList);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
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

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Full Name:"); nameLabel.setForeground(TEXT_COLOR); panel.add(nameLabel, gbc);
        JTextField txtFullName = new JTextField(tutorUser.getFullName(), 20); gbc.gridx = 1; panel.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel passLabel = new JLabel("New Password (leave blank):"); passLabel.setForeground(TEXT_COLOR); panel.add(passLabel, gbc);
        JPasswordField txtPassword = new JPasswordField(20); gbc.gridx = 1; panel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel specLabel = new JLabel("Specialization(s):"); specLabel.setForeground(TEXT_COLOR); panel.add(specLabel, gbc);
        JTextField txtSpecialization = new JTextField(tutorUser.getSpecialization(), 20); gbc.gridx = 1; panel.add(txtSpecialization, gbc);

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


    private void refreshCourseData() {
        refreshCourseTable();
        refreshCourseSelector();
        if (courseSelector.getItemCount() > 0) {
            courseSelector.setSelectedIndex(0);
        } else {
            // If there are no courses, clear the dependent panels
            studentList.setListData(new Vector<>());
            resultsArea.setText("No courses available.");
        }
    }

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
                    refreshCourseData();
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
                refreshCourseData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete course.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
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
        // IMPORTANT: Ensure 'currentUser' is set to the correct user for the dashboard
        // e.g., this.adminUser, this.receptionistUser, etc.
        User currentUser = this.tutorUser; // Change this for each dashboard!

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
        int unreadCount = DataManager.getUnreadMessageCount(this.tutorUser); 
        if (unreadCount > 0) {
            btnChat.setText("Chat (" + unreadCount + ")");
            btnChat.setForeground(Color.RED);
        } else {
            btnChat.setText("Chat");
            btnChat.setForeground(Color.WHITE);
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
        } else {
            btnAnnouncements.setText("Announcements");
            btnAnnouncements.setForeground(Color.WHITE);
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

        String[] days = {"Time Slot", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[] timeSlots = {
            "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
            "12:00-13:00",
            "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00",
            "17:00-18:00", "18:00-19:00", "19:00-20:00"
        };

        DefaultTableModel timetableModel = new DefaultTableModel(null, days) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable timetableTable = new JTable(timetableModel);
        styleTable(timetableTable);
        timetableTable.setRowHeight(35);

        Map<String, Map<String, String>> weeklyData = DataManager.getStoredTimetable();

        for (String timeSlot : timeSlots) {
            Object[] rowData = new Object[days.length];
            rowData[0] = timeSlot;
            if (timeSlot.equals("12:00-13:00")) {
                for (int i = 1; i < days.length; i++) rowData[i] = "--- RECESS ---";
            } else {
                for (int i = 1; i < days.length; i++) {
                    String day = days[i];
                    String courseName = " ";
                    if (weeklyData.containsKey(day)) {
                        for (Map.Entry<String, String> entry : weeklyData.get(day).entrySet()) {
                            if (entry.getKey().contains(timeSlot.split("-")[0])) {
                                courseName = entry.getValue();
                                break;
                            }
                        }
                    }
                    rowData[i] = courseName;
                }
            }
            timetableModel.addRow(rowData);
        }
        
        JScrollPane scrollPane = new JScrollPane(timetableTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        
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

    private class CenterTableCellRenderer extends DefaultTableCellRenderer {
        public CenterTableCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }
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
}
