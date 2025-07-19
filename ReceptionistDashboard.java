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
    private JButton btnMyFeedback;
    private JSplitPane requestSplitPane;
    private JComboBox<User> attendanceStudentSelector;

    private JTable studentTable, requestTable;
    private DefaultTableModel studentTableModel, requestTableModel;
    private LocalDate currentWeekStart;
    private JList<String[]> requestList;
    private DefaultListModel<String[]> requestListModel;
    
    private static final Color BG_COLOR = new Color(24, 34, 54);
    private static final Color FIELD_BG_COLOR = new Color(42, 53, 76);
    private static final Color PRIMARY_COLOR = new Color(67, 102, 163);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);
    private static final Color SIDEBAR_COLOR = new Color(30, 41, 61);

    public ReceptionistDashboard(User user) {
        this.receptionistUser = user;
        setTitle("The Learning Hub - Receptionist Dashboard");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        this.currentWeekStart = java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        studentTableModel = new DefaultTableModel(new String[]{"ID", "Username", "Full Name"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        requestTableModel = new DefaultTableModel(new String[]{"Request ID", "Student ID", "Date", "Details"}, 0) {
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
        contentPanel.add(createStudentPanel(), "MANAGE_STUDENTS");
        contentPanel.add(createStudentAccountsPanel(), "STUDENT_ACCOUNTS");
        contentPanel.add(createRequestsPanel(), "HANDLE_REQUESTS");
        contentPanel.add(createMonthlySchedulePanel(), "MONTHLY_TIMETABLE");
        contentPanel.add(createProfilePanel(), "MY_PROFILE");
        contentPanel.add(createViewAttendancePanel(), "VIEW_ATTENDANCE");

        JPanel myFeedbackPanel = AdminDashboard.createFeedbackReviewPanel(this.receptionistUser, false);
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
            public void windowClosing(WindowEvent e) {
                logout();
            }
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
        
        JLabel welcomeLabel = new JLabel("Welcome, " + receptionistUser.getFullName());
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

        sidebar.add(createSidebarButton("Manage Students", "MANAGE_STUDENTS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Student Accounts", "STUDENT_ACCOUNTS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Handle Requests", "HANDLE_REQUESTS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Monthly Timetable", "MONTHLY_TIMETABLE"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("View Attendance", "VIEW_ATTENDANCE"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        btnMyFeedback = createSidebarButton("My Feedback", "VIEW_FEEDBACK");
        sidebar.add(btnMyFeedback);
        
        return sidebar;
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        JLabel welcomeMessage = new JLabel("<html><center>Welcome to the Receptionist Dashboard.<br>Please select a function from the menu on the left.</center></html>");
        welcomeMessage.setFont(new Font("Arial", Font.PLAIN, 24));
        welcomeMessage.setForeground(TEXT_COLOR);
        panel.add(welcomeMessage);
        return panel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { refreshStudentTable(); }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        studentTable = new JTable(studentTableModel);
        styleTable(studentTable);
        
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        JButton btnRegister = createStyledButton("Register New Student");
        JButton btnUpdate = createStyledButton("Update Enrollment");
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
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);

        // --- Left Panel: List of Pending Requests ---
        // Initialize the instance variables instead of local variables
        requestListModel = new DefaultListModel<>();
        requestList = new JList<>(requestListModel); 
        
        styleJList(requestList); // Apply styling
        requestList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof String[]) {
                    String[] requestData = (String[]) value;
                    User student = DataManager.findUserById(requestData[1]);
                    setText(requestData[0] + " - " + (student != null ? student.getFullName() : "Unknown"));
                }
                return c;
            }
        });

        JScrollPane listScrollPane = new JScrollPane(requestList); // Use the instance variable
        listScrollPane.setBorder(BorderFactory.createTitledBorder(null, "Pending Enrollment Requests", 0, 0, new Font("Arial", Font.BOLD, 14), TEXT_COLOR));

        // --- Right Panel: Request Details ---
        JPanel detailsPanel = createRequestDetailsPanel();

        // --- Logic and Listeners ---
        mainPanel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                // The model is now an instance variable, so we don't need to pass it
                refreshRequestList(detailsPanel);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        requestList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && requestList.getSelectedValue() != null) {
                populateDetailsPanel(detailsPanel, requestList.getSelectedValue());
            }
        });

        // --- Final Assembly ---
        requestSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, detailsPanel);
        requestSplitPane.setDividerLocation(250);
        mainPanel.add(requestSplitPane, BorderLayout.CENTER);

        return mainPanel;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("Full Name:"), gbc);
        JTextField txtFullName = createTextField(receptionistUser.getFullName(), 20); 
        gbc.gridx = 1; panel.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(createLabel("New Password:"), gbc);
        JPasswordField txtPassword = createPasswordField(20); 
        gbc.gridx = 1; panel.add(txtPassword, gbc);
        
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
        java.awt.Window[] windows = java.awt.Window.getWindows();
        for (java.awt.Window window : windows) {
            window.dispose();
        }

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
        ChatInterfaceFrame chatInterface = new ChatInterfaceFrame(this.receptionistUser, this::refreshChatNotification);
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
            btnAnnouncements.setFont(new Font(btnAnnouncements.getFont().getName(), Font.BOLD, btnAnnouncements.getFont().getSize()));
        } else {
            btnAnnouncements.setText("Announcements");
            btnAnnouncements.setForeground(Color.WHITE);
            btnAnnouncements.setFont(new Font(btnAnnouncements.getFont().getName(), Font.PLAIN, btnAnnouncements.getFont().getSize()));
        }
    }
    
    private JPanel createMonthlySchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // --- TOP CONTROLS FOR WEEKLY NAVIGATION ---
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

        // --- MAIN CONTENT PANEL ---
        JPanel scheduleContentPanel = new JPanel();
        scheduleContentPanel.setLayout(new BoxLayout(scheduleContentPanel, BoxLayout.Y_AXIS));
        scheduleContentPanel.setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(scheduleContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // --- LOGIC TO UPDATE THE VIEW ---
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

                    RoundedPanel dayHeader = new RoundedPanel(15, new Color(20, 110, 255));
                    dayHeader.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));
                    dayHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                    JLabel dayLabel = new JLabel(date.format(dayFormatter).toUpperCase());
                    dayLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                    dayLabel.setForeground(Color.WHITE);
                    dayHeader.add(dayLabel);
                    scheduleContentPanel.add(dayHeader);
                    scheduleContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                    for (String[] classInfo : entry.getValue()) {
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

        // --- ACTION LISTENERS for the buttons ---
        prevWeekButton.addActionListener(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateScheduleView.run();
        });

        nextWeekButton.addActionListener(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateScheduleView.run();
        });
        
        // --- FINAL PANEL ASSEMBLY ---
        panel.add(topControlsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add a listener to load the view when the panel is first shown
        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { updateScheduleView.run(); }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        return panel;
    }

    private JPanel createStudentAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout(10,10));
        topPanel.setBackground(BG_COLOR);

        JPanel studentSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studentSelectionPanel.setBackground(BG_COLOR);
        JLabel selectLabel = new JLabel("Select Student:");
        selectLabel.setForeground(TEXT_COLOR);
        List<User> allStudents = DataManager.getAllUsersByRole("Student");
        JComboBox<User> studentSelector = new JComboBox<>(new Vector<>(allStudents));
        studentSelectionPanel.add(selectLabel);
        studentSelectionPanel.add(studentSelector);

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
        
        String[] columnNames = {"Enrollment ID", "Course", "Fee", "Amount Paid", "Balance", "Status"};
        DefaultTableModel enrollmentTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable enrollmentTable = new JTable(enrollmentTableModel);
        styleTable(enrollmentTable);

        JScrollPane scrollPane = new JScrollPane(enrollmentTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BG_COLOR);
        JButton acceptPaymentButton = createStyledButton("Accept Payment for Selected Enrollment");
        buttonPanel.add(acceptPaymentButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        studentSelector.addActionListener(e -> {
            User selectedStudent = (User) studentSelector.getSelectedItem();
            if (selectedStudent == null) return;
            
            Map<String, Double> summary = DataManager.getPaymentStatus(selectedStudent.getId());
            totalFeesLabel.setText(String.format("Total Fees: $%.2f", summary.getOrDefault("totalFees", 0.0)));
            totalPaidLabel.setText(String.format("Total Paid: $%.2f", summary.getOrDefault("totalPaid", 0.0)));
            double balance = summary.getOrDefault("balance", 0.0);
            balanceLabel.setText(String.format("Balance Due: $%.2f", balance));
            balanceLabel.setForeground(balance > 0 ? Color.ORANGE : TEXT_COLOR);
            
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
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(this, "Payment amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (amount > balanceDue) {
                        JOptionPane.showMessageDialog(this, "Payment amount cannot be greater than the balance due.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    String receipt = DataManager.acceptPayment(enrollmentId, amount);
                    JTextArea receiptArea = new JTextArea(receipt, 10, 30);
                    receiptArea.setEditable(false);
                    JOptionPane.showMessageDialog(this, new JScrollPane(receiptArea), "Payment Successful", JOptionPane.INFORMATION_MESSAGE);
                    
                    if (studentSelector.getActionListeners().length > 0) {
                        studentSelector.getActionListeners()[0].actionPerformed(null);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid amount entered. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        if (studentSelector.getItemCount() > 0) {
            studentSelector.setSelectedIndex(0);
        } else {
             if (studentSelector.getActionListeners().length > 0) {
                studentSelector.getActionListeners()[0].actionPerformed(null);
             }
        }
        
        return panel;
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
    
    private void styleLargeLabel(JLabel label) {
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Arial", Font.BOLD, 18));
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

    private class CenterTableCellRenderer extends DefaultTableCellRenderer {
        public CenterTableCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }
    }

    private JPanel createViewAttendancePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // --- Top Panel: Student Selection ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(BG_COLOR);
        
        // Use the instance variable here
        attendanceStudentSelector = new JComboBox<>(); 
        
        topPanel.add(new JLabel("Select Student to View Attendance:")).setForeground(TEXT_COLOR);
        topPanel.add(attendanceStudentSelector); // <-- FIX: Use the instance variable

        // --- Center Panel: List of Attendance Cards ---
        JPanel contentListPanel = new JPanel();
        contentListPanel.setLayout(new BoxLayout(contentListPanel, BoxLayout.Y_AXIS));
        contentListPanel.setBackground(BG_COLOR);
        JScrollPane scrollPane = new JScrollPane(contentListPanel);
        scrollPane.setBorder(null);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Logic and Listeners ---
        mainPanel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                attendanceStudentSelector.removeAllItems(); // <-- FIX: Use the instance variable
                DataManager.getAllUsersByRole("Student").forEach(attendanceStudentSelector::addItem); // <-- FIX: Use the instance variable
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        // When a student is selected, update the list of attendance cards
        attendanceStudentSelector.addActionListener(e -> { // <-- FIX: Use the instance variable
            User selectedStudent = (User) attendanceStudentSelector.getSelectedItem(); // <-- FIX: Use the instance variable
            contentListPanel.removeAll(); 

            if (selectedStudent == null) {
                contentListPanel.revalidate();
                contentListPanel.repaint();
                return;
            }

            List<AttendanceSummary> summaries = DataManager.getAttendanceSummaryForStudent(selectedStudent.getId());

            if (summaries.isEmpty()) {
                contentListPanel.setLayout(new GridBagLayout());
                JLabel noDataLabel = new JLabel("No attendance records found for this student.");
                noDataLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                noDataLabel.setForeground(Color.LIGHT_GRAY);
                contentListPanel.add(noDataLabel);
            } else {
                contentListPanel.setLayout(new BoxLayout(contentListPanel, BoxLayout.Y_AXIS));
                for (AttendanceSummary summary : summaries) {
                    contentListPanel.add(new AttendanceCard(summary));
                    contentListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            }

            contentListPanel.revalidate();
            contentListPanel.repaint();
        });
        
        // Trigger the action listener for the first item when the panel loads
        if (attendanceStudentSelector.getItemCount() > 0) { // <-- FIX: Use the instance variable
            attendanceStudentSelector.setSelectedIndex(0); // <-- FIX: Use the instance variable
        }

        return mainPanel;
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
        int unreadCount = DataManager.getUnreadFeedbackCount(this.receptionistUser.getId());
        if (unreadCount > 0) {
            btnMyFeedback.setText("My Feedback (" + unreadCount + ")");
            btnMyFeedback.setForeground(Color.ORANGE);
        } else {
            btnMyFeedback.setText("My Feedback");
            btnMyFeedback.setForeground(TEXT_COLOR);
        }
    }

    private void styleJList(JList<?> list) { // Use JList<?> to accept any type
        list.setBackground(FIELD_BG_COLOR);
        list.setForeground(TEXT_COLOR);
        list.setFont(new Font("Arial", Font.PLAIN, 14));
        list.setSelectionBackground(PRIMARY_COLOR);
        list.setSelectionForeground(Color.WHITE);
    }

    private JPanel createRequestDetailsPanel() {
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(FIELD_BG_COLOR);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        detailsPanel.add(createLabel("Student Name: -"), gbc); gbc.gridy = 1;
        detailsPanel.add(createLabel("Student ID: -"), gbc); gbc.gridy = 2;
        detailsPanel.add(createLabel("Outstanding Balance: -"), gbc); gbc.gridy = 3;
        gbc.insets = new Insets(15, 5, 5, 5); gbc.gridy = 4;
        detailsPanel.add(createLabel("Request Details:"), gbc); gbc.insets = new Insets(0, 5, 8, 5); gbc.gridy = 5;

        JTextArea detailsArea = new JTextArea(5, 20);
        styleJTextArea(detailsArea);
        detailsArea.setEditable(false);


        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        detailsPanel.add(new JScrollPane(detailsArea), gbc);

        gbc.weighty = 0;
        gbc.weightx = 0;

        gbc.fill = GridBagConstraints.NONE; gbc.gridy = 6; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 5, 5, 5);
        JButton approveButton = createStyledButton("Approve & Enroll"); approveButton.setBackground(new Color(34, 139, 34));
        detailsPanel.add(approveButton, gbc); gbc.gridx = 1;
        JButton rejectButton = createStyledButton("Reject"); rejectButton.setBackground(new Color(178, 34, 34));
        detailsPanel.add(rejectButton, gbc);

        approveButton.addActionListener(e -> handleRequestAction(true));
        rejectButton.addActionListener(e -> handleRequestAction(false));

        return detailsPanel;
    }

    private void refreshRequestList(JPanel detailsPanel) {
        requestListModel.clear();
        DataManager.getAllPendingRequests().forEach(requestListModel::addElement);
        if (requestListModel.isEmpty()) {
            populateDetailsPanel(detailsPanel, null);
        }
    }

    private void populateDetailsPanel(JPanel detailsPanel, String[] requestData) {
        JLabel studentNameLabel = (JLabel) detailsPanel.getComponent(0);
        JLabel studentIdLabel = (JLabel) detailsPanel.getComponent(1);
        JLabel balanceLabel = (JLabel) detailsPanel.getComponent(2);
        JTextArea detailsArea = (JTextArea) ((JScrollPane) detailsPanel.getComponent(4)).getViewport().getView();
        JButton approveButton = (JButton) detailsPanel.getComponent(5);

        if (requestData == null) {
            studentNameLabel.setText("Student Name: -");
            studentIdLabel.setText("Student ID: -");
            balanceLabel.setText("Outstanding Balance: -");
            detailsArea.setText("Select a request from the list to see details.");
            approveButton.setEnabled(false);
            return;
        }

        String studentId = requestData[1];
        User student = DataManager.findUserById(studentId);
        studentNameLabel.setText("Student Name: " + (student != null ? student.getFullName() : "Unknown"));
        studentIdLabel.setText("Student ID: " + studentId);
        detailsArea.setText(requestData[2]);
        
        Map<String, Double> paymentStatus = DataManager.getPaymentStatus(studentId);
        double balance = paymentStatus.getOrDefault("balance", 0.0);
        balanceLabel.setText(String.format("Outstanding Balance: $%.2f", balance));
        
        if (balance > 0) {
            balanceLabel.setForeground(Color.ORANGE);
            approveButton.setEnabled(false);
            approveButton.setToolTipText("Cannot approve: Student has an outstanding balance.");
        } else {
            balanceLabel.setForeground(Color.GREEN);
            approveButton.setEnabled(true);
            approveButton.setToolTipText("Approve request and enroll student in selected courses.");
        }
    }

    private void handleRequestAction(boolean isApproved) {
        String[] requestData = requestList.getSelectedValue();
        if (requestData == null) {
            // This should not happen if buttons are enabled correctly, but it's a good safeguard.
            JOptionPane.showMessageDialog(this, "Please select a request first.", "No Request Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String requestId = requestData[0];
        String studentId = requestData[1];
        
        if (isApproved) {
            List<String> requestedCourseIds = parseCourseIdsFromRequest(requestData[2]);
            if (requestedCourseIds.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Could not identify any valid courses in the request.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Set<String> currentCourseIds = DataManager.getStudentCourseIDs(studentId);
            currentCourseIds.addAll(requestedCourseIds);
            
            if (DataManager.updateStudentEnrollments(studentId, new ArrayList<>(currentCourseIds))) {
                DataManager.updateRequestStatus(requestId, "Approved & Enrolled");
                JOptionPane.showMessageDialog(this, "Student successfully enrolled.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // This might happen if there's a data consistency issue.
                JOptionPane.showMessageDialog(this, "An unexpected error occurred during enrollment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // --- START OF NEW REJECTION LOGIC ---
            
            // 1. Prompt for a reason using an input dialog.
            String reason = JOptionPane.showInputDialog(
                this, 
                "Please enter the reason for rejecting this request:", 
                "Rejection Reason Required", 
                JOptionPane.PLAIN_MESSAGE
            );

            // 2. Check if the receptionist provided a reason.
            if (reason != null && !reason.trim().isEmpty()) {
                // 3. If a reason was given, update the status with the reason.
                String newStatus = "Rejected: " + reason.trim();
                DataManager.updateRequestStatus(requestId, newStatus);
                JOptionPane.showMessageDialog(this, "Request has been rejected and the reason was recorded.", "Request Rejected", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // 4. If they clicked "Cancel" or entered nothing, abort the rejection.
                JOptionPane.showMessageDialog(this, "Rejection cancelled. A reason is required to reject a request.", "Action Cancelled", JOptionPane.WARNING_MESSAGE);
            }
            // --- END OF NEW REJECTION LOGIC ---
        }
        
        // Refresh the list to show the request has been removed from the queue.
        refreshRequestList((JPanel) requestSplitPane.getRightComponent());
    }

    private List<String> parseCourseIdsFromRequest(String requestDetails) {
        // Format: "Requesting enrollment for: C-101: Course Name, C-102: Another Course"
        List<String> ids = new ArrayList<>();
        String prefix = "Requesting enrollment for: ";
        if (requestDetails.startsWith(prefix)) {
            String allCoursesPart = requestDetails.substring(prefix.length());
            String[] courseParts = allCoursesPart.split(",");
            for (String part : courseParts) {
                if (part.contains(":")) {
                    ids.add(part.split(":")[0].trim());
                }
            }
        }
        return ids;
    }
    private void styleJTextArea(JTextArea textArea) {
        textArea.setBackground(FIELD_BG_COLOR);
        textArea.setForeground(TEXT_COLOR);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setMargin(new Insets(10,10,10,10));
        textArea.setCaretColor(Color.WHITE);
    }

    private class AttendanceCard extends JPanel {
        public AttendanceCard(AttendanceSummary summary) {
            setBackground(ReceptionistDashboard.FIELD_BG_COLOR);
            setLayout(new BorderLayout(15, 5));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ReceptionistDashboard.BG_COLOR),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            // Left side: Course Name and details
            JLabel courseNameLabel = new JLabel(summary.getCourseName());
            courseNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
            courseNameLabel.setForeground(ReceptionistDashboard.TEXT_COLOR);
            String detailsText = String.format("Classes: %d/%d", summary.getAttendedClasses(), summary.getTotalClasses());
            JLabel detailsLabel = new JLabel(detailsText);
            detailsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            detailsLabel.setForeground(Color.LIGHT_GRAY);
            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.add(courseNameLabel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
            infoPanel.add(detailsLabel);
            add(infoPanel, BorderLayout.CENTER);

            // Right side: Percentage
            double percentage = summary.getPercentage();
            JLabel percentageLabel = new JLabel(String.format("(%.0f%%)", percentage));
            percentageLabel.setFont(new Font("Arial", Font.BOLD, 16));
            if (percentage >= 80.0) percentageLabel.setForeground(new Color(34, 177, 76));
            else if (percentage >= 60.0) percentageLabel.setForeground(Color.ORANGE);
            else percentageLabel.setForeground(new Color(237, 28, 36));
            JPanel percentagePanel = new JPanel(new GridBagLayout());
            percentagePanel.setOpaque(false);
            percentagePanel.add(percentageLabel);
            add(percentagePanel, BorderLayout.EAST);

            // Click listener to show details
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    showAttendanceDetailsDialog(summary.getCourseId(), summary.getCourseName());
                }
                public void mouseEntered(java.awt.event.MouseEvent e) { setBackground(ReceptionistDashboard.PRIMARY_COLOR.darker()); }
                public void mouseExited(java.awt.event.MouseEvent e) { setBackground(ReceptionistDashboard.FIELD_BG_COLOR); }
            });
        }
    }

    private void showAttendanceDetailsDialog(String courseId, String courseName) {
        User selectedStudent = (User) attendanceStudentSelector.getSelectedItem();
        
        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "No student selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a non-editable table model
        DefaultTableModel detailsModel = new DefaultTableModel(new String[]{"Date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Fetch and populate data
        List<String[]> records = DataManager.getAttendanceForStudent(selectedStudent.getId(), courseId);
        for (String[] record : records) {
            detailsModel.addRow(new Object[]{record[2], record[3]});
        }

        JTable detailsTable = new JTable(detailsModel);
        styleTable(detailsTable); // Use your existing table styling

        // Show the pop-up dialog
        JOptionPane.showMessageDialog(
            this,
            new JScrollPane(detailsTable),
            "Attendance Details for " + courseName,
            JOptionPane.PLAIN_MESSAGE
        );
    }
}