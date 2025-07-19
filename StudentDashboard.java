import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class StudentDashboard extends JFrame {
    private User studentUser;
    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton btnAnnouncements, btnChat, btnLogout;
     private LocalDate currentWeekStart;
     private JPanel pendingRequestsPanel; 

    private static final Color BG_COLOR = new Color(24, 34, 54);
    private static final Color FIELD_BG_COLOR = new Color(42, 53, 76);
    private static final Color PRIMARY_COLOR = new Color(67, 102, 163);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);
    private static final Color SIDEBAR_COLOR = new Color(30, 41, 61);

    public StudentDashboard(User user) {
        this.studentUser = user;
        setTitle("The Learning Hub - Student Dashboard");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        this.currentWeekStart = java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createSidebarPanel(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_COLOR);

        contentPanel.add(createWelcomePanel(), "WELCOME_PANEL");
        contentPanel.add(createRequestPanel(), "REQUESTS");
        contentPanel.add(createPaymentPanel(), "PAYMENTS");
        contentPanel.add(createResultsPanel(), "RESULTS");
        contentPanel.add(createMonthlySchedulePanel(), "MONTHLY_SCHEDULE");
        contentPanel.add(createProfilePanel(), "MY_PROFILE");
        contentPanel.add(createMyAttendancePanel(), "MY_ATTENDANCE");
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { logout(); }
        });

        refreshChatNotification();
        refreshAnnouncementNotification();
        
        cardLayout.show(contentPanel, "WELCOME_PANEL");
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + studentUser.getFullName());
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

        sidebar.add(createSidebarButton("Enrollment Requests", "REQUESTS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Payment Status", "PAYMENTS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("My Results", "RESULTS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("Monthly Schedule", "MONTHLY_SCHEDULE"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("My Attendance", "MY_ATTENDANCE"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnSubmitFeedback = new JButton("Submit Feedback");
        btnSubmitFeedback.setFont(new Font("Arial", Font.BOLD, 16));
        btnSubmitFeedback.setBackground(SIDEBAR_COLOR);
        btnSubmitFeedback.setForeground(TEXT_COLOR);
        btnSubmitFeedback.setFocusPainted(false);
        btnSubmitFeedback.setHorizontalAlignment(SwingConstants.LEFT);
        btnSubmitFeedback.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btnSubmitFeedback.setContentAreaFilled(false);
        btnSubmitFeedback.setOpaque(true);
        btnSubmitFeedback.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSubmitFeedback.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnSubmitFeedback.getPreferredSize().height));



        btnSubmitFeedback.addActionListener(e -> {
            FeedbackSubmissionDialog dialog = new FeedbackSubmissionDialog(StudentDashboard.this, this.studentUser);
            dialog.setVisible(true);
        });

        sidebar.add(btnSubmitFeedback);

        return sidebar;
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);

        JTextArea welcomeMessage = new JTextArea("Welcome to the Student Dashboard.\nPlease select a function from the menu on the left.");

        welcomeMessage.setFont(new Font("Arial", Font.PLAIN, 24));
        welcomeMessage.setForeground(TEXT_COLOR);
        welcomeMessage.setBackground(BG_COLOR);
        welcomeMessage.setEditable(false);
        welcomeMessage.setFocusable(false);

        JPanel textWrapper = new JPanel(new GridBagLayout());
        textWrapper.setBackground(BG_COLOR);
        textWrapper.add(welcomeMessage);

        panel.add(textWrapper);
        return panel;
    }

    private void logout() {
        java.awt.Window[] windows = java.awt.Window.getWindows();
        for (java.awt.Window window : windows) {
            window.dispose();
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private JPanel createRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // This panel will be updated by the refresh method
        pendingRequestsPanel = new JPanel(new BorderLayout(10, 10));
        pendingRequestsPanel.setBackground(BG_COLOR);
        pendingRequestsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TEXT_COLOR), "My Pending Requests",
            TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 14), TEXT_COLOR));

        JButton newRequestButton = createStyledButton("Submit New Enrollment Request");
        
        // When panel is shown, refresh the list of pending requests
        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                refreshRequestsPanel();
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        // Add button listener to open the new dialog
        newRequestButton.addActionListener(e -> showSubmitRequestDialog());

        panel.add(newRequestButton, BorderLayout.NORTH);
        panel.add(pendingRequestsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        DefaultTableModel paymentHistoryTableModel = new DefaultTableModel(new String[]{"Payment ID", "Date", "Course", "Amount Paid ($)"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable paymentHistoryTable = new JTable(paymentHistoryTableModel);
        styleTable(paymentHistoryTable);
        
        JScrollPane scrollPane = new JScrollPane(paymentHistoryTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        summaryPanel.setBackground(BG_COLOR);
        JLabel feesLabel = new JLabel("Total Fees: $0.00");
        JLabel paidLabel = new JLabel("Total Paid: $0.00");
        JLabel balanceLabel = new JLabel("Balance Due: $0.00");
        styleLargeLabel(feesLabel);
        styleLargeLabel(paidLabel);
        styleLargeLabel(balanceLabel);
        summaryPanel.add(feesLabel);
        summaryPanel.add(paidLabel);
        summaryPanel.add(balanceLabel);
        
        panel.add(summaryPanel, BorderLayout.SOUTH);
        
        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                refreshPayments(paymentHistoryTableModel, feesLabel, paidLabel, balanceLabel);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });
        
        return panel;
    }
    
    private JPanel createResultsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
        headerPanel.setBackground(BG_COLOR);
        JLabel gpaLabel = new JLabel("Overall GPA: N/A");
        styleLargeLabel(gpaLabel);
        headerPanel.add(gpaLabel);

        // --- Content Panel for the list of result cards (No changes below this line) ---
        JPanel contentListPanel = new JPanel();
        contentListPanel.setLayout(new BoxLayout(contentListPanel, BoxLayout.Y_AXIS));
        contentListPanel.setBackground(BG_COLOR);
        JScrollPane scrollPane = new JScrollPane(contentListPanel);
        scrollPane.setBorder(null);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                contentListPanel.removeAll();
                List<ResultSummary> summaries = DataManager.getStudentResultSummaries(studentUser.getId());

                if (summaries.isEmpty()) {
                    gpaLabel.setText("Overall GPA: N/A");
                    contentListPanel.setLayout(new GridBagLayout());
                    JLabel noDataLabel = new JLabel("No results have been uploaded yet.");
                    noDataLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                    noDataLabel.setForeground(Color.LIGHT_GRAY);
                    contentListPanel.add(noDataLabel);
                } else {
                    double totalPoints = summaries.stream().mapToDouble(ResultSummary::getGradePoints).sum();
                    double gpa = totalPoints / summaries.size();
                    gpaLabel.setText(String.format("Overall GPA: %.2f", gpa));
                    
                    contentListPanel.setLayout(new BoxLayout(contentListPanel, BoxLayout.Y_AXIS));
                    for (ResultSummary summary : summaries) {
                        contentListPanel.add(new ResultCard(summary));
                        contentListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                    }
                }
                contentListPanel.revalidate();
                contentListPanel.repaint();
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

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
        JTextField txtFullName = createTextField(studentUser.getFullName(), 20);
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
            String finalPassword = newPasswordStr.isEmpty() ? studentUser.getPassword() : newPasswordStr;
            User updatedUser = new User(studentUser.getId(), studentUser.getUsername(), finalPassword, studentUser.getRole(), newFullName, "");
            if (DataManager.updateUser(updatedUser)) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                this.studentUser = updatedUser;
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }
    
    private void refreshRequests(JList<String> requestList) {
        Map<String, String> requests = DataManager.getPendingRequests(studentUser.getId());
        Vector<String> requestDisplay = new Vector<>();
        requests.forEach((id, details) -> requestDisplay.add(id + ": " + details));
        requestList.setListData(requestDisplay);
    }
    
    private void refreshPayments(DefaultTableModel tableModel, JLabel feesLabel, JLabel paidLabel, JLabel balanceLabel) {
        Map<String, Double> status = DataManager.getPaymentStatus(studentUser.getId());
        feesLabel.setText(String.format("Total Course Fees: $%.2f", status.getOrDefault("totalFees", 0.0)));
        paidLabel.setText(String.format("Total Amount Paid: $%.2f", status.getOrDefault("totalPaid", 0.0)));
        double balance = status.getOrDefault("balance", 0.0);
        balanceLabel.setText(String.format("Balance Due: $%.2f", balance));
        balanceLabel.setForeground(balance > 0 ? Color.ORANGE : TEXT_COLOR);

        tableModel.setRowCount(0);
        List<String[]> history = DataManager.getStudentPaymentHistory(studentUser.getId());
        for (String[] payment : history) {
            tableModel.addRow(payment);
        }
    }
    
    private void openChatDialog() {
        ChatInterfaceFrame chatInterface = new ChatInterfaceFrame(this.studentUser, this::refreshChatNotification);
        chatInterface.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                refreshChatNotification();
            }
        });
        chatInterface.setVisible(true);
    }

    public void refreshChatNotification() {
        int unreadCount = DataManager.getUnreadMessageCount(this.studentUser); 
        
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
        AnnouncementsFrame announcementsFrame = new AnnouncementsFrame(this.studentUser);
        announcementsFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                refreshAnnouncementNotification();
            }
        });
        announcementsFrame.setVisible(true);
    }
    
    public void refreshAnnouncementNotification() {
        Set<String> readIds = DataManager.getReadAnnouncementIds(this.studentUser);
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
    
    private void styleLargeLabel(JLabel label) {
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Arial", Font.BOLD, 20));
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

    private JPanel createMyAttendancePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // This panel will hold all the individual attendance cards
        JPanel contentListPanel = new JPanel();
        contentListPanel.setLayout(new BoxLayout(contentListPanel, BoxLayout.Y_AXIS));
        contentListPanel.setBackground(BG_COLOR);

        // Put the list panel inside a scroll pane
        JScrollPane scrollPane = new JScrollPane(contentListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Use an AncestorListener to load the data fresh every time the tab is viewed
        mainPanel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                // Clear any previous content
                contentListPanel.removeAll();
                
                // Fetch the summary data using the existing DataManager method
                List<AttendanceSummary> summaries = DataManager.getAttendanceSummaryForStudent(studentUser.getId());

                if (summaries.isEmpty()) {
                    // Display a helpful message if no attendance data exists
                    contentListPanel.setLayout(new GridBagLayout());
                    JLabel noDataLabel = new JLabel("No attendance records found.");
                    noDataLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                    noDataLabel.setForeground(Color.LIGHT_GRAY);
                    contentListPanel.add(noDataLabel);
                } else {
                    // Restore layout and create a card for each course summary
                    contentListPanel.setLayout(new BoxLayout(contentListPanel, BoxLayout.Y_AXIS));
                    for (AttendanceSummary summary : summaries) {
                        contentListPanel.add(new AttendanceCard(summary));
                        contentListPanel.add(Box.createRigidArea(new Dimension(0, 5))); // A small gap between cards
                    }
                }
                
                // Refresh the UI to show the new cards
                contentListPanel.revalidate();
                contentListPanel.repaint();
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        return mainPanel;
    }

    private JPanel createMonthlySchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // --- Top controls and scroll pane setup (no changes here) ---
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

        JPanel scheduleContentPanel = new JPanel();
        scheduleContentPanel.setLayout(new BoxLayout(scheduleContentPanel, BoxLayout.Y_AXIS));
        scheduleContentPanel.setBackground(BG_COLOR);
        JScrollPane scrollPane = new JScrollPane(scheduleContentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);

        // --- LOGIC TO UPDATE THE VIEW ---
        Runnable updateScheduleView = () -> {
            LocalDate start = currentWeekStart;
            LocalDate end = start.plusDays(6);
            weekLabel.setText(start.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + "  -  " + end.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

            scheduleContentPanel.removeAll();
            Map<LocalDate, List<String[]>> weeklySchedule = DataManager.getStoredTimetable().entrySet().stream()
                    .filter(entry -> !entry.getKey().isBefore(start) && !entry.getKey().isAfter(end))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, java.util.TreeMap::new));

            if (weeklySchedule.isEmpty()) {
                // ... (no data label logic is unchanged) ...
            } else {
                scheduleContentPanel.setLayout(new BoxLayout(scheduleContentPanel, BoxLayout.Y_AXIS));
                for (Map.Entry<LocalDate, List<String[]>> entry : weeklySchedule.entrySet()) {
                    // ... (day header logic is unchanged) ...

                    for (String[] classInfo : entry.getValue()) {
                        String time = classInfo[0];
                        String rawSubject = classInfo[1];
                        String tutor = classInfo[2];

                        // ============================ THE FIX ============================
                        // Find the REAL course ID instead of inventing one.
                        String subjectName = rawSubject.replace("Weekly ", "").trim();
                        User tutorUser = DataManager.findUserByUsername(tutor);
                        
                        String courseId = "Unknown Course"; // Default if not found
                        if(tutorUser != null) {
                            // This method correctly finds the course ID from the tutor and subject.
                            String foundId = DataManager.getCourseIdByTutorAndSubject(tutorUser.getId(), subjectName);
                            if (foundId != null) {
                                courseId = foundId;
                            }
                        }
                        
                        // Create the card with the REAL courseId and the clean subjectName.
                        ClassCard card = new ClassCard(courseId, subjectName, time, tutor);
                        // ========================= END OF FIX ==========================
                        
                        scheduleContentPanel.add(card);
                        scheduleContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                    }
                }
            }
            scheduleContentPanel.revalidate();
            scheduleContentPanel.repaint();
        };

        // --- ACTION LISTENERS AND FINAL ASSEMBLY (no changes here) ---
        prevWeekButton.addActionListener(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateScheduleView.run();
        });
        nextWeekButton.addActionListener(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateScheduleView.run();
        });
        panel.add(topControlsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { updateScheduleView.run(); }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        return panel;
    }

    private static class CourseDisplayItem {
        private final String courseId;
        private final String displayName;

        public CourseDisplayItem(String courseId, String displayName) {
            this.courseId = courseId;
            this.displayName = displayName;
        }

        public String getId() {
            return courseId;
        }

        @Override
        public String toString() {
            // This is what the user sees in the dropdown menu.
            return displayName;
        }
    }

    private void refreshRequestsPanel() {
        pendingRequestsPanel.removeAll(); // Clear the panel first

        Map<String, String> requests = DataManager.getPendingRequests(studentUser.getId());
        
        if (requests.isEmpty()) {
            pendingRequestsPanel.setLayout(new GridBagLayout());
            JLabel noRequestsLabel = new JLabel("You have no pending requests.");
            noRequestsLabel.setForeground(TEXT_COLOR);
            pendingRequestsPanel.add(noRequestsLabel);
        } else {
            pendingRequestsPanel.setLayout(new BorderLayout(10,10));
            Vector<String> requestDisplay = new Vector<>();
            requests.forEach((id, details) -> requestDisplay.add(id + ": " + details));
            
            JList<String> requestList = new JList<>(requestDisplay);
            styleJList(requestList);
            
            JButton deleteButton = createStyledButton("Delete Selected Request");
            deleteButton.addActionListener(e -> {
                String selectedRequest = requestList.getSelectedValue();
                if (selectedRequest == null) {
                    JOptionPane.showMessageDialog(this, "Please select a request to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String requestId = selectedRequest.split(":")[0];
                if (DataManager.deleteRequest(requestId, studentUser.getId())) {
                    JOptionPane.showMessageDialog(this, "Request deleted successfully.");
                    refreshRequestsPanel(); // Refresh again after deletion
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete request.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            pendingRequestsPanel.add(new JScrollPane(requestList), BorderLayout.CENTER);
            pendingRequestsPanel.add(deleteButton, BorderLayout.SOUTH);
        }
        pendingRequestsPanel.revalidate();
        pendingRequestsPanel.repaint();
    }


    private void showSubmitRequestDialog() {
        List<String> allAvailableCourses = DataManager.getAvailableCourses();

        Set<String> enrolledCourseIDs = DataManager.getStudentCourseIDs(this.studentUser.getId());

        List<String> coursesAvailableForRequest = allAvailableCourses.stream()
                .filter(courseInfoString -> {
                    String courseId = courseInfoString.split(":")[0];
                    return !enrolledCourseIDs.contains(courseId);
                })
                .collect(Collectors.toList());

        if (coursesAvailableForRequest.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You are already enrolled in all available courses.", "No New Courses Available", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 5. Build the dialog with the correctly filtered list.
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Please select the course(s) you wish to enroll in:"), BorderLayout.NORTH);

        JList<String> courseList = new JList<>(new Vector<>(coursesAvailableForRequest));
        courseList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        courseList.setVisibleRowCount(10);
        panel.add(new JScrollPane(courseList), BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Request New Enrollment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            List<String> selectedCourses = courseList.getSelectedValuesList();
            if (selectedCourses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You must select at least one course.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String details = "Requesting enrollment for: " + String.join(", ", selectedCourses);
            if (DataManager.submitEnrollmentRequest(studentUser.getId(), details)) {
                JOptionPane.showMessageDialog(this, "Request submitted successfully.", "Request Sent", JOptionPane.INFORMATION_MESSAGE);
                refreshRequestsPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Error submitting request.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class AttendanceCard extends JPanel {
        public AttendanceCard(AttendanceSummary summary) {
            // --- Existing setup for layout and background (No changes here) ---
            setBackground(StudentDashboard.FIELD_BG_COLOR);
            setLayout(new BorderLayout(15, 5));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, StudentDashboard.BG_COLOR),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

            // --- Left side: Course Name and Attendance Fraction (No changes here) ---
            JLabel courseNameLabel = new JLabel(summary.getCourseName());
            courseNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
            courseNameLabel.setForeground(StudentDashboard.TEXT_COLOR);
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

            // --- Right side: Percentage with Color Coding (No changes here) ---
            double percentage = summary.getPercentage();
            JLabel percentageLabel = new JLabel(String.format("(%.0f%%)", percentage));
            percentageLabel.setFont(new Font("Arial", Font.BOLD, 16));
            if (percentage >= 80.0) {
                percentageLabel.setForeground(new Color(34, 177, 76));
            } else if (percentage >= 60.0) {
                percentageLabel.setForeground(Color.ORANGE);
            } else {
                percentageLabel.setForeground(new Color(237, 28, 36));
            }
            JPanel percentagePanel = new JPanel(new GridBagLayout());
            percentagePanel.setOpaque(false);
            percentagePanel.add(percentageLabel);
            add(percentagePanel, BorderLayout.EAST);

            // --- NEW: Add interactivity to make the card clickable ---
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    // When the card is clicked, show the details dialog for this course
                    showAttendanceDetailsDialog(summary.getCourseId(), summary.getCourseName());
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    // Change color on hover for visual feedback
                    setBackground(StudentDashboard.PRIMARY_COLOR.darker());
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    // Change back to the original color when the mouse leaves
                    setBackground(StudentDashboard.FIELD_BG_COLOR);
                }
            });
        }
    }

    private void showAttendanceDetailsDialog(String courseId, String courseName) {
        // 1. Create a table model that is explicitly configured to be non-editable.
        DefaultTableModel detailsModel = new DefaultTableModel(new String[]{"Date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // This is the key: it makes every cell in the table read-only.
                return false;
            }
        };

        // 2. Fetch the detailed records for the specific course.
        List<String[]> records = DataManager.getAttendanceForStudent(studentUser.getId(), courseId);

        // 3. Populate the table model with the fetched data.
        for (String[] record : records) {
            // DataManager format: [courseId, studentId, date, status]
            String date = record[2];
            String status = record[3];
            detailsModel.addRow(new Object[]{date, status});
        }

        // 4. Create and style the JTable.
        JTable detailsTable = new JTable(detailsModel);
        styleTable(detailsTable); // Re-use your existing table styling method.

        // 5. Place the table in a scroll pane to handle long lists.
        JScrollPane scrollPane = new JScrollPane(detailsTable);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        // 6. Use JOptionPane to display the table in a simple dialog.
        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Attendance Details for: " + courseName,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private class ResultCard extends JPanel {
        public ResultCard(ResultSummary summary) {
            // Use the dashboard's color scheme
            setBackground(StudentDashboard.FIELD_BG_COLOR);
            setLayout(new BorderLayout(20, 10)); // Add horizontal gap
            // Create a border with padding and a bottom line as a separator
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, StudentDashboard.BG_COLOR),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));
            // Set a max height to keep cards a consistent size
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

            // --- Left side: Course Name and Letter Grade ---
            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false); // Make transparent to show parent background
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            JLabel courseNameLabel = new JLabel(summary.getCourseName());
            courseNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
            courseNameLabel.setForeground(new Color(60, 150, 255)); // Blue color for emphasis

            JLabel resultLabel = new JLabel("Result: " + summary.getGradeLetter());
            resultLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            resultLabel.setForeground(Color.LIGHT_GRAY);

            infoPanel.add(courseNameLabel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // A small gap
            infoPanel.add(resultLabel);
            
            add(infoPanel, BorderLayout.CENTER);

            // --- Right side: Grade Points ---
            JPanel gradePanel = new JPanel(new GridBagLayout());
            gradePanel.setOpaque(false);

            String gradeText = String.format("Grade: %.2f", summary.getGradePoints());
            JLabel gradeLabel = new JLabel(gradeText);
            gradeLabel.setFont(new Font("Arial", Font.BOLD, 16));
            gradeLabel.setForeground(new Color(34, 177, 76)); // Green color for the grade points

            gradePanel.add(gradeLabel); // GridBagLayout centers it automatically
            
            add(gradePanel, BorderLayout.EAST);
        }
    }
    
}