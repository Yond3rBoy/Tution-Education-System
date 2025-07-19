// AdminDashboard.java (Fully Updated)
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class AdminDashboard extends JFrame {
    private User adminUser;

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton btnAnnouncements, btnChat, btnLogout;
    private JPanel scheduleContentPanel;
    private JButton btnMyFeedback;
    private JComboBox<User> attendanceStudentSelector;
    private DefaultTableModel courseTableModel;
    private JTable tutorTable, receptionistTable;
    private DefaultTableModel tutorTableModel, receptionistTableModel;
    
    // New field to track the currently displayed week
    private LocalDate currentWeekStart;

    private static final Color BG_COLOR = new Color(24, 34, 54);
    private static final Color FIELD_BG_COLOR = new Color(42, 53, 76);
    private static final Color PRIMARY_COLOR = new Color(67, 102, 163);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);
    private static final Color SIDEBAR_COLOR = new Color(30, 41, 61);

    public AdminDashboard(User user) {
        this.adminUser = user;
        setTitle("The Learning Hub - Admin Dashboard");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize the start of the current week
        this.currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createSidebarPanel(), BorderLayout.WEST);

        tutorTableModel = new DefaultTableModel(new String[]{"ID", "Username", "Full Name"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        receptionistTableModel = new DefaultTableModel(new String[]{"ID", "Username", "Full Name"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        courseTableModel = new DefaultTableModel(new String[]{"ID", "Course Name", "Tutor", "Subject", "Fee", "Schedule"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_COLOR);
        scheduleContentPanel = new JPanel();
        scheduleContentPanel.setBackground(BG_COLOR);

        contentPanel.add(createWelcomePanel(), "WELCOME_PANEL");
        contentPanel.add(createRegistrationPanel(), "REGISTER_USERS");
        contentPanel.add(createManageTutorPanel(), "MANAGE_TUTORS");
        contentPanel.add(createManageReceptionistPanel(), "MANAGE_RECEPTIONISTS");
        contentPanel.add(createManageCoursesPanel(), "MANAGE_COURSES");
        contentPanel.add(createReportPanel(), "INCOME_REPORT");
        contentPanel.add(createTutorPayrollPanel(), "TUTOR_PAYROLL");
        contentPanel.add(createResultsPanelForAdmin(), "VIEW_RESULTS");
        contentPanel.add(createSchedulePanel(), "MONTHLY_SCHEDULE"); // Changed method name
        contentPanel.add(createProfilePanel(), "MY_PROFILE");
        contentPanel.add(createViewAttendancePanel(), "VIEW_ATTENDANCE");
        JPanel myAdminFeedbackPanel = createFeedbackReviewPanel(this.adminUser, false);
        contentPanel.add(myAdminFeedbackPanel, "MY_ADMIN_FEEDBACK");
        contentPanel.add(createFeedbackReviewPanel(this.adminUser, true), "REVIEW_FEEDBACK");

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

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel topControlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        topControlsPanel.setBackground(BG_COLOR);
        JButton prevWeekButton = new JButton("<< Prev Week");
        JButton nextWeekButton = new JButton("Next Week >>");

        final JLabel weekLabel = new JLabel("", SwingConstants.CENTER); 
        weekLabel.setFont(new Font("Arial", Font.BOLD, 18));
        weekLabel.setForeground(TEXT_COLOR);
        topControlsPanel.add(prevWeekButton);
        topControlsPanel.add(Box.createHorizontalStrut(20));
        topControlsPanel.add(weekLabel);
        topControlsPanel.add(Box.createHorizontalStrut(20));
        topControlsPanel.add(nextWeekButton);

        final DefaultListModel<Object> listModel = new DefaultListModel<>();
        
        JList<Object> scheduleList = new JList<>(listModel);
        scheduleList.setCellRenderer(new ScheduleListRenderer());
        scheduleList.setBackground(BG_COLOR);
        scheduleList.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        scheduleList.setSelectionModel(new DefaultListSelectionModel() {
            @Override public void setSelectionInterval(int i, int i1) {}
        });

        JScrollPane scrollPane = new JScrollPane(scheduleList);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(BG_COLOR);
        JButton generateButton = createStyledButton("Generate New Timetable");
        bottomPanel.add(generateButton);

        Runnable updateScheduleView = () -> {
            LocalDate start = currentWeekStart;
            LocalDate end = start.plusDays(6);
            weekLabel.setText(start.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + "  -  " + end.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

            listModel.clear();

            Map<LocalDate, List<String[]>> weeklySchedule = DataManager.getStoredTimetable().entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(start) && !entry.getKey().isAfter(end))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, TreeMap::new));

            if (weeklySchedule.isEmpty()) {
                listModel.addElement("No classes scheduled for this week.");
            } else {
                for (Map.Entry<LocalDate, List<String[]>> entry : weeklySchedule.entrySet()) {
                    listModel.addElement(entry.getKey().format(DateTimeFormatter.ofPattern("EEE, d MMM")));
                    for (String[] classInfo : entry.getValue()) {
                        String time = classInfo[0];
                        String rawSubject = classInfo[1];
                        String tutor = classInfo[2];
                        String subjectName = rawSubject.replace("Weekly ", "").trim();
                        User tutorUser = DataManager.findUserByUsername(tutor);
                        String courseId = "N/A";
                        if(tutorUser != null) {
                            String foundId = DataManager.getCourseIdByTutorAndSubject(tutorUser.getId(), subjectName);
                            if (foundId != null) courseId = foundId;
                        }
                        listModel.addElement(new ScheduleItem(courseId, subjectName, time, tutor));
                    }
                }
            }
        };

        // --- ACTION LISTENERS AND FINAL ASSEMBLY (Unchanged) ---
        prevWeekButton.addActionListener(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateScheduleView.run();
        });
        nextWeekButton.addActionListener(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateScheduleView.run();
        });
        generateButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "This will ERASE the existing timetable and generate a new one for the next 4 weeks.\nThis action cannot be undone. Are you sure?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                    @Override protected Boolean doInBackground() { return DataManager.generateAndAssignTimetable(); }
                    @Override protected void done() {
                        try {
                            if (get()) { JOptionPane.showMessageDialog(AdminDashboard.this, "Timetable generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE); }
                            else { JOptionPane.showMessageDialog(AdminDashboard.this, "Failed to generate timetable.", "Error", JOptionPane.ERROR_MESSAGE); }
                        } catch (Exception ex) { JOptionPane.showMessageDialog(AdminDashboard.this, "An error occurred during generation.", "Error", JOptionPane.ERROR_MESSAGE); }
                        updateScheduleView.run();
                    }
                };
                worker.execute();
            }
        });

        panel.add(topControlsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { updateScheduleView.run(); }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        return panel;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + adminUser.getFullName());
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

        btnMyFeedback = createStyledButton("My Feedback"); 

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
        topRightButtons.add(btnMyProfile);
        topRightButtons.add(btnMyFeedback);
        topRightButtons.add(btnLogout);
        headerPanel.add(topRightButtons, BorderLayout.EAST);
        
        btnAnnouncements.addActionListener(e -> openAnnouncements());
        btnChat.addActionListener(e -> openChatDialog());
        btnMyFeedback.addActionListener(e -> {
            cardLayout.show(contentPanel, "MY_ADMIN_FEEDBACK");
            refreshFeedbackNotification();
        });
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

        sidebar.add(createSidebarButton("Register Users", "REGISTER_USERS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Manage Tutors", "MANAGE_TUTORS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Manage Receptionists", "MANAGE_RECEPTIONISTS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Manage Courses", "MANAGE_COURSES"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("View Student Results", "VIEW_RESULTS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("View Attendance", "VIEW_ATTENDANCE"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Monthly Schedule", "MONTHLY_SCHEDULE"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Tutor Payroll", "TUTOR_PAYROLL"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Income Report", "INCOME_REPORT"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Review Staff Feedback", "REVIEW_FEEDBACK"));
        
        return sidebar;
    }
    
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        JLabel welcomeMessage = new JLabel("<html><center>Welcome to the Admin Dashboard.<br>Please select a function from the menu on the left.</center></html>");
        welcomeMessage.setFont(new Font("Arial", Font.PLAIN, 24));
        welcomeMessage.setForeground(TEXT_COLOR);
        panel.add(welcomeMessage);
        return panel;
    }
    
    private JPanel createManageTutorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { refreshTutorTable(); }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });
        
        tutorTable = new JTable(tutorTableModel);
        styleTable(tutorTable);
        JScrollPane scrollPane = new JScrollPane(tutorTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        JButton btnEdit = createStyledButton("Edit Selected Tutor");
        JButton btnDelete = createStyledButton("Delete Selected Tutor");
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        btnEdit.addActionListener(e -> {
            int selectedRow = tutorTable.getSelectedRow();
            if (selectedRow >= 0) {
                String userId = (String) tutorTableModel.getValueAt(selectedRow, 0);
                User userToEdit = DataManager.findUserById(userId);
                if (userToEdit != null) {
                    showEditUserDialog(userToEdit);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a tutor to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

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

        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { refreshReceptionistTable(); }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        receptionistTable = new JTable(receptionistTableModel);
        styleTable(receptionistTable);
        JScrollPane scrollPane = new JScrollPane(receptionistTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        JButton btnEdit = createStyledButton("Edit Selected Receptionist");
        JButton btnDelete = createStyledButton("Delete Selected Receptionist");
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        btnEdit.addActionListener(e -> {
            int selectedRow = receptionistTable.getSelectedRow();
            if (selectedRow >= 0) {
                String userId = (String) receptionistTableModel.getValueAt(selectedRow, 0);
                User userToEdit = DataManager.findUserById(userId);
                if (userToEdit != null) {
                    showEditUserDialog(userToEdit);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a receptionist to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
        
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
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR), "Generate Income Report", 
            TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), TEXT_COLOR));
        
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        controlsPanel.setBackground(BG_COLOR);
        
        JComboBox<Object> monthComboBox = new JComboBox<>();
        monthComboBox.addItem("All Months (Yearly)");
        for (int i = 1; i <= 12; i++) {
            monthComboBox.addItem(i);
        }
        
        JComboBox<Integer> yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 5; i <= currentYear; i++) {
            yearComboBox.addItem(i);
        }
        yearComboBox.setSelectedItem(currentYear);
        
        JButton btnGenerate = createStyledButton("Generate Report");
        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setForeground(TEXT_COLOR);
        JLabel yearLabel = new JLabel("Year:");
        yearLabel.setForeground(TEXT_COLOR);

        controlsPanel.add(yearLabel);
        controlsPanel.add(yearComboBox);
        controlsPanel.add(monthLabel);
        controlsPanel.add(monthComboBox);
        controlsPanel.add(btnGenerate);
        
        JTextPane reportPane = new JTextPane();
        reportPane.setEditable(false);
        reportPane.setFont(new Font("Monospaced", Font.PLAIN, 16));
        reportPane.setBackground(FIELD_BG_COLOR);
        reportPane.setForeground(TEXT_COLOR);
        reportPane.setMargin(new Insets(10, 10, 10, 10));

        StyledDocument doc = reportPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        
        reportPane.setText("Select a period and generate a report.");
        
        panel.add(controlsPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportPane), BorderLayout.CENTER);

        monthComboBox.addActionListener(e -> {
            boolean isYearly = "All Months (Yearly)".equals(monthComboBox.getSelectedItem());
            monthLabel.setText(isYearly ? "" : "Month:");
        });
        monthComboBox.getActionListeners()[0].actionPerformed(null);

        btnGenerate.addActionListener(e -> {
            int year = (int) yearComboBox.getSelectedItem();
            Object selectedMonth = monthComboBox.getSelectedItem();
            String report;

            if ("All Months (Yearly)".equals(selectedMonth)) {
                report = DataManager.generateYearlyIncomeReport(year);
            } else {
                int month = (int) selectedMonth;
                report = DataManager.generateIncomeReport(month, year);
            }
            
            reportPane.setText(report);
            doc.setParagraphAttributes(0, doc.getLength(), center, false);
            reportPane.setCaretPosition(0);
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

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("User ID:"), gbc);
        gbc.gridx = 1;
        JTextField txtId = createTextField(adminUser.getId(), 20);
        txtId.setEditable(false);
        panel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(createLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField txtUsername = createTextField(adminUser.getUsername(), 20);
        txtUsername.setEditable(false);
        panel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(createLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtFullName = createTextField(adminUser.getFullName(), 20);
        panel.add(txtFullName, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        panel.add(createLabel("New Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField txtPassword = createPasswordField(20);
        panel.add(txtPassword, gbc);

        gbc.gridy++; gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton btnUpdate = createStyledButton("Update Profile");
        panel.add(btnUpdate, gbc);

        btnUpdate.addActionListener(e -> {
            String newFullName = txtFullName.getText().trim();
            String newPasswordStr = new String(txtPassword.getPassword());

            if (newFullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Full Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String finalPassword = newPasswordStr.isEmpty() ? adminUser.getPassword() : newPasswordStr;
            User updatedUser = new User(
                adminUser.getId(), adminUser.getUsername(), finalPassword,
                adminUser.getRole(), newFullName, adminUser.getSpecialization()
            );

            if (DataManager.updateUser(updatedUser)) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
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
    
    
    private void openChatDialog() {
        ChatInterfaceFrame chatInterface = new ChatInterfaceFrame(this.adminUser, this::refreshChatNotification);
        chatInterface.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                refreshChatNotification();
            }
        });
        chatInterface.setVisible(true);
    }

    public void refreshChatNotification() {
        int unreadCount = DataManager.getUnreadMessageCount(this.adminUser); 
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
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // --- Header Panel: Student Selector and GPA display ---
        JPanel headerPanel = new JPanel(new BorderLayout(20, 10));
        headerPanel.setBackground(BG_COLOR);

        JComboBox<User> studentSelector = new JComboBox<>();
        JLabel gpaLabel = new JLabel("Overall GPA: N/A");
        gpaLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gpaLabel.setForeground(TEXT_COLOR);
        gpaLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        headerPanel.add(studentSelector, BorderLayout.CENTER);
        headerPanel.add(gpaLabel, BorderLayout.EAST);

        // --- Content Panel: Holds the list of ResultCard components ---
        JPanel contentListPanel = new JPanel();
        contentListPanel.setLayout(new BoxLayout(contentListPanel, BoxLayout.Y_AXIS));
        contentListPanel.setBackground(BG_COLOR);
        JScrollPane scrollPane = new JScrollPane(contentListPanel);
        scrollPane.setBorder(null);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Listeners for dynamic updates ---
        mainPanel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                studentSelector.removeAllItems();
                DataManager.getAllUsersByRole("Student").forEach(studentSelector::addItem);
                // Trigger the listener for the first student if the list is not empty
                if (studentSelector.getItemCount() > 0) {
                    studentSelector.setSelectedIndex(0);
                }
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        studentSelector.addActionListener(e -> {
            User selectedStudent = (User) studentSelector.getSelectedItem();
            contentListPanel.removeAll();

            if (selectedStudent == null) {
                gpaLabel.setText("Overall GPA: N/A");
                contentListPanel.revalidate();
                contentListPanel.repaint();
                return;
            }

            // THE FIX: Use the student's ID for a reliable data lookup.
            List<ResultSummary> summaries = DataManager.getStudentResultSummaries(selectedStudent.getId());

            if (summaries.isEmpty()) {
                gpaLabel.setText("Overall GPA: N/A");
                contentListPanel.setLayout(new GridBagLayout());
                JLabel noDataLabel = new JLabel("No results have been uploaded for this student yet.");
                noDataLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                noDataLabel.setForeground(Color.LIGHT_GRAY);
                contentListPanel.add(noDataLabel);
            } else {
                // Calculate and display GPA
                double totalPoints = summaries.stream().mapToDouble(ResultSummary::getGradePoints).sum();
                double gpa = totalPoints / summaries.size();
                gpaLabel.setText(String.format("Overall GPA: %.2f", gpa));

                // Create and add a card for each result
                contentListPanel.setLayout(new BoxLayout(contentListPanel, BoxLayout.Y_AXIS));
                for (ResultSummary summary : summaries) {
                    contentListPanel.add(new ResultCard(summary));
                    contentListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            }
            contentListPanel.revalidate();
            contentListPanel.repaint();
        });
        
        return mainPanel;
    }

    private JPanel createViewAttendancePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // --- Top Panel: Student Selection ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(BG_COLOR);
        
        // Initialize the instance variable instead of a local one
        attendanceStudentSelector = new JComboBox<>();
        
        topPanel.add(new JLabel("Select Student to View Attendance:")).setForeground(TEXT_COLOR);
        topPanel.add(attendanceStudentSelector);

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
                attendanceStudentSelector.removeAllItems();
                DataManager.getAllUsersByRole("Student").forEach(attendanceStudentSelector::addItem);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        attendanceStudentSelector.addActionListener(e -> {
            User selectedStudent = (User) attendanceStudentSelector.getSelectedItem();
            contentListPanel.removeAll(); // Clear previous content

            if (selectedStudent == null) {
                contentListPanel.revalidate();
                contentListPanel.repaint();
                return;
            }

            // Fetch data using the student's ID for accuracy.
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
        
        // Trigger the listener for the first item when the panel loads.
        if (attendanceStudentSelector.getItemCount() > 0) {
            attendanceStudentSelector.setSelectedIndex(0);
        }

        return mainPanel;
    }

    private void openAnnouncements() {
        AnnouncementsFrame announcementsFrame = new AnnouncementsFrame(this.adminUser);
        announcementsFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                refreshAnnouncementNotification();
            }
        });
        announcementsFrame.setVisible(true);
    }

    public void refreshAnnouncementNotification() {
        Set<String> readIds = DataManager.getReadAnnouncementIds(this.adminUser);
        List<Announcement> allAnnouncements = DataManager.getAllAnnouncements();
        long unreadCount = allAnnouncements.stream().filter(a -> !readIds.contains(a.getId())).count();

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

    private void logout() {
        java.awt.Window[] windows = java.awt.Window.getWindows();
        for (java.awt.Window window : windows) {
            window.dispose();
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private JPanel createTutorPayrollPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea reportArea = new JTextArea("Select a tutor, month, and year to generate a payroll report.");
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        reportArea.setBackground(FIELD_BG_COLOR);
        reportArea.setForeground(TEXT_COLOR);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlsPanel.setBackground(BG_COLOR);
        
        JComboBox<User> tutorSelector = new JComboBox<>();
        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { 
                tutorSelector.removeAllItems();
                List<User> allTutors = DataManager.getAllUsersByRole("Tutor");
                for(User t : allTutors) {
                    tutorSelector.addItem(t);
                }
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        JComboBox<Integer> monthComboBox = new JComboBox<>();
        for (int i = 1; i <= 12; i++) monthComboBox.addItem(i);
        
        JComboBox<Integer> yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 2; i <= currentYear; i++) yearComboBox.addItem(i);
        
        monthComboBox.setSelectedItem(LocalDate.now().getMonthValue());
        yearComboBox.setSelectedItem(currentYear);
        
        JButton btnGenerate = createStyledButton("Generate Payroll Report");
        controlsPanel.add(new JLabel("Tutor:")).setForeground(TEXT_COLOR);
        controlsPanel.add(tutorSelector);
        controlsPanel.add(new JLabel("Month:")).setForeground(TEXT_COLOR);
        controlsPanel.add(monthComboBox);
        controlsPanel.add(new JLabel("Year:")).setForeground(TEXT_COLOR);
        controlsPanel.add(yearComboBox);
        controlsPanel.add(btnGenerate);
        
        panel.add(controlsPanel, BorderLayout.NORTH);

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

    public static JPanel createFeedbackReviewPanel(User user, boolean isAdminView) {
        final JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);

        final JPanel feedbackContentPanel = new JPanel();
        feedbackContentPanel.setLayout(new BoxLayout(feedbackContentPanel, BoxLayout.Y_AXIS));
        feedbackContentPanel.setBackground(BG_COLOR);

        final JScrollPane scrollPane = new JScrollPane(feedbackContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_COLOR);

        final JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_COLOR);
        final JLabel titleLabel = new JLabel("My Feedback", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        if (isAdminView) {
            titleLabel.setText("Review Tutor Feedback");
            JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            selectionPanel.setBackground(BG_COLOR);
            JComboBox<User> userSelector = new JComboBox<>();

            List<User> staff = DataManager.getAllUsersByRole("Tutor");
            staff.forEach(userSelector::addItem);
            
            userSelector.addActionListener(e -> {
                User selectedUser = (User) userSelector.getSelectedItem();
                if (selectedUser != null) {
                    displayFeedbackForUser(feedbackContentPanel, topPanel, true, selectedUser);
                }
            });
            
            selectionPanel.add(new JLabel("Select Tutor:")).setForeground(TEXT_COLOR);
            selectionPanel.add(userSelector);
            topPanel.add(selectionPanel, BorderLayout.CENTER);
            
            if (!staff.isEmpty()) {
                displayFeedbackForUser(feedbackContentPanel, topPanel, true, staff.get(0));
            }

        } else { 
            displayFeedbackForUser(feedbackContentPanel, topPanel, false, user);
            DataManager.markFeedbackAsRead(user.getId());
        }

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static void displayFeedbackForUser(JPanel contentPanel, JPanel topPanel, boolean showSubmitter, User user) {
        contentPanel.removeAll();
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

                // This was missing before. It is now included.
                JPanel cardTop = new JPanel(new BorderLayout());
                cardTop.setOpaque(false);
                String stars = "★".repeat(feedback.getRating()) + "☆".repeat(5 - feedback.getRating());
                JLabel ratingLabel = new JLabel(stars);
                ratingLabel.setFont(new Font("Arial", Font.BOLD, 16));
                ratingLabel.setForeground(new Color(255, 215, 0));
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
                
                // This now calls the getSubmitterId() method which we added to Feedback.java
                if (showSubmitter) {
                    User submitter = DataManager.findUserById(feedback.getSubmitterId());
                    String submitterName = (submitter != null) ? submitter.getFullName() : "Unknown Student";
                    
                    JLabel submittedByLabel = new JLabel("Submitted by: " + submitterName);
                    submittedByLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                    submittedByLabel.setForeground(Color.LIGHT_GRAY);
                    submittedByLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
                    card.add(submittedByLabel, BorderLayout.SOUTH);
                }
                
                contentPanel.add(card);
            }
        }
        contentPanel.revalidate();
        contentPanel.repaint();
        topPanel.revalidate();
        topPanel.repaint();
    }

    public void refreshFeedbackNotification() {
        int unreadCount = DataManager.getUnreadFeedbackCount(this.adminUser.getId());
        if (unreadCount > 0) {
            btnMyFeedback.setText("My Feedback (" + unreadCount + ")");
            btnMyFeedback.setForeground(Color.ORANGE);
            btnMyFeedback.setFont(new Font(btnMyFeedback.getFont().getName(), Font.BOLD, btnMyFeedback.getFont().getSize()));
        } else {
            btnMyFeedback.setText("My Feedback");
            btnMyFeedback.setForeground(Color.WHITE);
            btnMyFeedback.setFont(new Font(btnMyFeedback.getFont().getName(), Font.PLAIN, btnMyFeedback.getFont().getSize()));
        }
    }

    private class ResultCard extends JPanel {
        public ResultCard(ResultSummary summary) {
            setBackground(AdminDashboard.FIELD_BG_COLOR);
            setLayout(new BorderLayout(20, 10));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AdminDashboard.BG_COLOR),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

            // Left side: Course Name and Letter Grade
            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            JLabel courseNameLabel = new JLabel(summary.getCourseName());
            courseNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
            courseNameLabel.setForeground(new Color(60, 150, 255));

            JLabel resultLabel = new JLabel("Result: " + summary.getGradeLetter());
            resultLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            resultLabel.setForeground(Color.LIGHT_GRAY);

            infoPanel.add(courseNameLabel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(resultLabel);
            add(infoPanel, BorderLayout.CENTER);

            JPanel gradePanel = new JPanel(new GridBagLayout());
            gradePanel.setOpaque(false);
            String gradeText = String.format("Grade: %.2f", summary.getGradePoints());
            JLabel gradeLabel = new JLabel(gradeText);
            gradeLabel.setFont(new Font("Arial", Font.BOLD, 16));
            gradeLabel.setForeground(new Color(34, 177, 76));
            gradePanel.add(gradeLabel);
            add(gradePanel, BorderLayout.EAST);
        }
    }

    private class AttendanceCard extends JPanel {
        public AttendanceCard(AttendanceSummary summary) {
            setBackground(AdminDashboard.FIELD_BG_COLOR);
            setLayout(new BorderLayout(15, 5));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AdminDashboard.BG_COLOR),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            // Left side: Course Name and details
            JLabel courseNameLabel = new JLabel(summary.getCourseName());
            courseNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
            courseNameLabel.setForeground(AdminDashboard.TEXT_COLOR);
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
                public void mouseEntered(java.awt.event.MouseEvent e) { setBackground(AdminDashboard.PRIMARY_COLOR.darker()); }
                public void mouseExited(java.awt.event.MouseEvent e) { setBackground(AdminDashboard.FIELD_BG_COLOR); }
            });
        }
    }

    private void showAttendanceDetailsDialog(String courseId, String courseName) {
        User selectedStudent = (User) attendanceStudentSelector.getSelectedItem();
        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "No student selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultTableModel detailsModel = new DefaultTableModel(new String[]{"Date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<String[]> records = DataManager.getAttendanceForStudent(selectedStudent.getId(), courseId);
        for (String[] record : records) {
            detailsModel.addRow(new Object[]{record[2], record[3]});
        }

        JTable detailsTable = new JTable(detailsModel);
        styleTable(detailsTable);

        // Show the pop-up dialog
        JOptionPane.showMessageDialog(
            this,
            new JScrollPane(detailsTable),
            "Attendance Details for " + courseName,
            JOptionPane.PLAIN_MESSAGE
        );
    }

    private JPanel createManageCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Refresh the table every time this panel is shown
        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { refreshCoursesTable(); }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        JTable coursesTable = new JTable(courseTableModel);
        styleTable(coursesTable);
        panel.add(new JScrollPane(coursesTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        JButton btnAddCourse = createStyledButton("Add New Course");
        JButton btnDeleteCourse = createStyledButton("Delete Selected Course");
        buttonPanel.add(btnAddCourse);
        buttonPanel.add(btnDeleteCourse);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        btnAddCourse.addActionListener(e -> showAddCourseDialog());
        btnDeleteCourse.addActionListener(e -> {
            int selectedRow = coursesTable.getSelectedRow();
            if (selectedRow >= 0) {
                String courseId = (String) courseTableModel.getValueAt(selectedRow, 0);
                int choice = JOptionPane.showConfirmDialog(this, "Delete course " + courseId + "?\nThis will not un-enroll existing students.", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION && DataManager.deleteCourse(courseId)) {
                    refreshCoursesTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }

    private void refreshCoursesTable() {
        courseTableModel.setRowCount(0);
        // DataManager.getAvailableCourses returns a formatted string. We need raw data.
        // A new method in DataManager would be ideal, but for now, we can parse the existing file.
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader("data/courses.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 7);
                if (data.length >= 7) {
                    User tutor = DataManager.findUserById(data[2]);
                    String tutorName = (tutor != null) ? tutor.getFullName() : "Unknown";
                    courseTableModel.addRow(new Object[]{data[0], data[1], tutorName, data[4], data[5], data[6]});
                }
            }
        } catch (java.io.IOException e) {
            // file not found or other error
        }
    }

    private void showAddCourseDialog() {
        JTextField courseNameField = new JTextField(15);
        JTextField feeField = new JTextField(15);
        JTextField scheduleField = new JTextField(15);
        
        JComboBox<User> tutorSelector = new JComboBox<>();
        DataManager.getAllUsersByRole("Tutor").forEach(tutorSelector::addItem);
        
        // Use a dropdown for the standardized subjects
        JComboBox<String> subjectSelector = new JComboBox<>(DataManager.getAvailableSubjects().toArray(new String[0]));

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Course Name:"));
        panel.add(courseNameField);
        panel.add(new JLabel("Assign Tutor:"));
        panel.add(tutorSelector);
        panel.add(new JLabel("Subject:"));
        panel.add(subjectSelector);
        panel.add(new JLabel("Fee ($):"));
        panel.add(feeField);
        panel.add(new JLabel("Schedule (e.g., Mon 4-6 PM):"));
        panel.add(scheduleField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String courseName = courseNameField.getText().trim();
                String schedule = scheduleField.getText().trim();
                User selectedTutor = (User) tutorSelector.getSelectedItem();
                String selectedSubject = (String) subjectSelector.getSelectedItem();
                double fee = Double.parseDouble(feeField.getText().trim());

                if (courseName.isEmpty() || schedule.isEmpty() || selectedTutor == null) {
                    throw new IllegalArgumentException("All fields must be filled.");
                }
                
                // The level can be derived from the subject or course name, here we use subject for simplicity
                String level = selectedSubject; 

                if (DataManager.addCourse(courseName, selectedTutor.getId(), level, selectedSubject, fee, schedule)) {
                    JOptionPane.showMessageDialog(this, "Course added successfully!");
                    refreshCoursesTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add course.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid fee amount. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0)); // 1 row, 2 columns, with a horizontal gap
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add the two dedicated registration forms to the main panel
        panel.add(createTutorRegistrationForm());
        panel.add(createReceptionistRegistrationForm());

        return panel;
    }

    private JPanel createTutorRegistrationForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SIDEBAR_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR), "Register New Tutor",
            TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 18), TEXT_COLOR));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // --- Form Components ---
        JTextField usernameField = createTextField("", 20);
        JPasswordField passwordField = createPasswordField(20);
        JTextField fullNameField = createTextField("", 20);

        // ============================ THE FIX ============================
        // A JList is the correct component for selecting MULTIPLE items.
        // A JComboBox (dropdown) is only for selecting ONE item.
        JList<String> specializationList = new JList<>(DataManager.getAvailableSubjects().toArray(new String[0]));
        specializationList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane specScrollPane = new JScrollPane(specializationList);
        specScrollPane.setPreferredSize(new Dimension(250, 120));
        // ========================= END OF FIX ==========================

        // --- Layout ---
        gbc.gridx = 0; gbc.gridy = 0; panel.add(createLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(createLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(createLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(fullNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHWEST; panel.add(createLabel("Specialization(s):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(specScrollPane, gbc);

        // --- Register Button ---
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton btnRegister = createStyledButton("Register Tutor");
        panel.add(btnRegister, gbc);

        // --- Button Action ---
        btnRegister.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String fullName = fullNameField.getText().trim();
            List<String> specializations = specializationList.getSelectedValuesList();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || specializations.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields, including at least one specialization, are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (DataManager.registerUser(username, password, "Tutor", fullName, specializations)) {
                JOptionPane.showMessageDialog(this, "Tutor registered successfully!");
                refreshTutorTable();
                // Clear fields for next entry
                usernameField.setText("");
                passwordField.setText("");
                fullNameField.setText("");
                specializationList.clearSelection();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register tutor. Username might be taken.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createReceptionistRegistrationForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SIDEBAR_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR), "Register New Receptionist",
            TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 18), TEXT_COLOR));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField usernameField = createTextField("", 20);
        JPasswordField passwordField = createPasswordField(20);
        JTextField fullNameField = createTextField("", 20);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(createLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(createLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(createLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(fullNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton btnRegister = createStyledButton("Register Receptionist");
        panel.add(btnRegister, gbc);

        btnRegister.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String fullName = fullNameField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (DataManager.registerUser(username, password, "Receptionist", fullName, new java.util.ArrayList<>())) {
                JOptionPane.showMessageDialog(this, "Receptionist registered successfully!");
                refreshReceptionistTable();
                usernameField.setText("");
                passwordField.setText("");
                fullNameField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register receptionist. Username might be taken.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        return panel;
    }

    private void showEditUserDialog(User userToEdit) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField fullNameField = new JTextField(userToEdit.getFullName(), 20);
        JPasswordField passwordField = new JPasswordField(20);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(new JLabel(userToEdit.getId()), gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(new JLabel(userToEdit.getUsername()), gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(fullNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("New Password (optional):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(passwordField, gbc);

        JList<String> specializationList = null;
        if ("Tutor".equals(userToEdit.getRole())) {
            gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.NORTHWEST;
            panel.add(new JLabel("Specialization(s):"), gbc);
            
            // ============================ THE FIX ============================
            // This also uses a JList, which is consistent with the registration form.
            specializationList = new JList<>(DataManager.getAvailableSubjects().toArray(new String[0]));
            specializationList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            // ========================= END OF FIX ==========================
            
            // Pre-select the tutor's current specializations
            if (userToEdit.getSpecialization() != null && !userToEdit.getSpecialization().isEmpty()) {
                List<String> currentSpecs = java.util.Arrays.asList(userToEdit.getSpecialization().split(";"));
                java.util.ArrayList<Integer> indicesToSelect = new java.util.ArrayList<>();
                for (int i = 0; i < specializationList.getModel().getSize(); i++) {
                    if (currentSpecs.contains(specializationList.getModel().getElementAt(i))) {
                        indicesToSelect.add(i);
                    }
                }
                specializationList.setSelectedIndices(indicesToSelect.stream().mapToInt(i -> i).toArray());
            }
            
            JScrollPane scrollPane = new JScrollPane(specializationList);
            scrollPane.setPreferredSize(new Dimension(200, 100));
            gbc.gridx = 1; gbc.gridy = 4; panel.add(scrollPane, gbc);
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit User: " + userToEdit.getUsername(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newFullName = fullNameField.getText().trim();
            if (newFullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Full Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String newPassword = new String(passwordField.getPassword());
            String finalPassword = newPassword.isEmpty() ? userToEdit.getPassword() : newPassword;

            String finalSpecialization = userToEdit.getSpecialization();
            if ("Tutor".equals(userToEdit.getRole())) {
                List<String> selectedSpecs = specializationList.getSelectedValuesList();
                if (selectedSpecs.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "A tutor must have at least one specialization.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                finalSpecialization = String.join(";", selectedSpecs);
            }

            User updatedUser = new User(userToEdit.getId(), userToEdit.getUsername(), finalPassword, userToEdit.getRole(), newFullName, finalSpecialization);

            if (DataManager.updateUser(updatedUser)) {
                JOptionPane.showMessageDialog(this, "User updated successfully!");
                if ("Tutor".equals(updatedUser.getRole())) {
                    refreshTutorTable();
                } else {
                    refreshReceptionistTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private record ScheduleItem(String moduleCode, String moduleName, String time, String tutor) {}
        private class ScheduleListRenderer implements ListCellRenderer<Object> {
        private final RoundedPanel headerPanel;
        private final JLabel headerLabel;
        
        private final RoundedPanel cardPanel;
        private final JLabel moduleCodeLabel, moduleNameLabel, timeLabel, tutorLabel;
        
        private ImageIcon clockIcon, tutorIcon; // locationIcon has been removed

        public ScheduleListRenderer() {
            // --- 1. Configure the BLUE DATE HEADER (Unchanged) ---
            headerLabel = new JLabel();
            headerLabel.setForeground(Color.WHITE);
            headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            headerPanel = new RoundedPanel(15, new Color(20, 110, 255));
            headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));
            headerPanel.add(headerLabel);

            // --- 2. Configure the DARK GREY CLASS CARD ---
            cardPanel = new RoundedPanel(15, new Color(45, 45, 45));
            cardPanel.setLayout(new BorderLayout(10, 5));
            cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            try {
                clockIcon = new ImageIcon(new ImageIcon(getClass().getResource("/assets/clock_icon.png")).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
                tutorIcon = new ImageIcon(new ImageIcon(getClass().getResource("/assets/tutor_icon.png")).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
            } catch (Exception e) {
                System.err.println("Could not load icons for schedule renderer.");
            }

            moduleCodeLabel = new JLabel();
            moduleCodeLabel.setForeground(new Color(230, 80, 80));
            moduleCodeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

            moduleNameLabel = new JLabel();
            moduleNameLabel.setForeground(Color.WHITE);
            moduleNameLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            
            JPanel topInfoPanel = new JPanel();
            topInfoPanel.setOpaque(false);
            topInfoPanel.setLayout(new BoxLayout(topInfoPanel, BoxLayout.Y_AXIS));
            topInfoPanel.add(moduleCodeLabel);
            topInfoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            topInfoPanel.add(moduleNameLabel);

            timeLabel = new JLabel();
            timeLabel.setForeground(Color.LIGHT_GRAY);
            timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            timeLabel.setIcon(clockIcon);
            timeLabel.setIconTextGap(8);
            
            tutorLabel = new JLabel();
            tutorLabel.setForeground(new Color(100, 180, 255));
            tutorLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            tutorLabel.setIcon(tutorIcon);
            tutorLabel.setIconTextGap(8);

            // --- Simplified Bottom Panel (No Location) ---
            JPanel bottomDetailsPanel = new JPanel(new BorderLayout(15, 2));
            bottomDetailsPanel.setOpaque(false);
            bottomDetailsPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            
            bottomDetailsPanel.add(timeLabel, BorderLayout.WEST);
            bottomDetailsPanel.add(tutorLabel, BorderLayout.EAST);

            // Assemble the card
            cardPanel.add(topInfoPanel, BorderLayout.CENTER);
            cardPanel.add(bottomDetailsPanel, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof ScheduleItem data) {
                moduleCodeLabel.setText(data.moduleCode());
                moduleNameLabel.setText(data.moduleName());
                timeLabel.setText(data.time());
                tutorLabel.setText(data.tutor());
                return cardPanel;
            } else if (value instanceof String text) {
                headerLabel.setText(text);
                return headerPanel;
            }
            return new JLabel(); // Fallback
        }
    }
}