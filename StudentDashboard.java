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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class StudentDashboard extends JFrame {
    private User studentUser;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton btnAnnouncements, btnChat, btnLogout;
    private JTable paymentHistoryTable;
    private DefaultTableModel paymentHistoryTableModel;
    private JTable scheduleTable;
    private DefaultTableModel scheduleTableModel;
    
    private JList<String> scheduleList, requestList;
    private JLabel feesLabel, paidLabel, balanceLabel;

    private static final Color BG_COLOR = new Color(24, 34, 54);
    private static final Color FIELD_BG_COLOR = new Color(42, 53, 76);
    private static final Color PRIMARY_COLOR = new Color(67, 102, 163);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);

    public StudentDashboard(User user) {
        this.studentUser = user;
        setTitle("The Learning Hub - Student Dashboard");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_COLOR);

        contentPanel.add(createSchedulePanel(), "SCHEDULE");
        contentPanel.add(createRequestPanel(), "REQUESTS");
        contentPanel.add(createPaymentPanel(), "PAYMENTS");
        contentPanel.add(createResultsPanel(), "RESULTS");
        contentPanel.add(createWeeklyTimetablePanel(), "WEEKLY_TIMETABLE");
        contentPanel.add(createProfilePanel(), "PROFILE");
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        refreshAllData();
        refreshChatNotification();
        refreshAnnouncementNotification();
        
        cardLayout.show(contentPanel, "SCHEDULE");
    }

    private JPanel createHeaderPanel() {
        JPanel fullHeaderPanel = new JPanel();
        fullHeaderPanel.setLayout(new BoxLayout(fullHeaderPanel, BoxLayout.Y_AXIS));
        fullHeaderPanel.setBackground(BG_COLOR);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(BG_COLOR);
        topHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + studentUser.getFullName());
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

        JButton btnSchedule = createStyledButton("My Schedule");
        JButton btnRequests = createStyledButton("Enrollment Requests");
        JButton btnPayments = createStyledButton("Payment Status");
        JButton btnResults = createStyledButton("My Results");
        JButton btnWeeklyTimetable = createStyledButton("Weekly Timetable");
        JButton btnMyProfile = createStyledButton("My Profile");

        navPanel.add(btnSchedule);
        navPanel.add(btnRequests);
        navPanel.add(btnPayments);
        navPanel.add(btnResults);
        navPanel.add(btnWeeklyTimetable);
        navPanel.add(btnMyProfile);

        btnSchedule.addActionListener(e -> cardLayout.show(contentPanel, "SCHEDULE"));
        btnRequests.addActionListener(e -> cardLayout.show(contentPanel, "REQUESTS"));
        btnPayments.addActionListener(e -> cardLayout.show(contentPanel, "PAYMENTS"));
        btnResults.addActionListener(e -> cardLayout.show(contentPanel, "RESULTS"));
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
    
    private void refreshAllData() {
        refreshSchedule();
        refreshRequests();
        refreshPayments();
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columnNames = {"Course", "Tutor", "Schedule"};
        scheduleTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        scheduleTable = new JTable(scheduleTableModel);
        styleTable(scheduleTable); // Use your existing styling helper
        scheduleTable.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BG_COLOR);
        JButton refreshButton = createStyledButton("Refresh Schedule");
        refreshButton.addActionListener(e -> refreshSchedule());
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel newRequestPanel = new JPanel(new BorderLayout(10, 10));
        newRequestPanel.setBackground(BG_COLOR);
        newRequestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(TEXT_COLOR), "Submit a New Request", 0, 0, null, TEXT_COLOR));
        
        JTextArea requestDetailsArea = new JTextArea(3, 30);
        styleJTextArea(requestDetailsArea);
        newRequestPanel.add(new JScrollPane(requestDetailsArea), BorderLayout.CENTER);
        JButton submitButton = createStyledButton("Submit");
        newRequestPanel.add(submitButton, BorderLayout.EAST);
        
        JPanel pendingPanel = new JPanel(new BorderLayout(10, 10));
        pendingPanel.setBackground(BG_COLOR);
        pendingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(TEXT_COLOR), "My Pending Requests", 0, 0, null, TEXT_COLOR));
        
        requestList = new JList<>();
        styleJList(requestList);
        JScrollPane scrollPane = new JScrollPane(requestList);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        pendingPanel.add(scrollPane, BorderLayout.CENTER);
        
        JButton deleteButton = createStyledButton("Delete Selected Request");
        pendingPanel.add(deleteButton, BorderLayout.SOUTH);

        panel.add(newRequestPanel, BorderLayout.NORTH);
        panel.add(pendingPanel, BorderLayout.CENTER);

        submitButton.addActionListener(e -> {
            String details = requestDetailsArea.getText().trim();
            if (details.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Request details cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (DataManager.submitEnrollmentRequest(studentUser.getId(), details)) {
                JOptionPane.showMessageDialog(this, "Request submitted successfully.");
                requestDetailsArea.setText("");
                refreshRequests();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit request.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        deleteButton.addActionListener(e -> {
            String selectedRequest = requestList.getSelectedValue();
            if (selectedRequest == null) {
                JOptionPane.showMessageDialog(this, "Please select a request to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String requestId = selectedRequest.split(":")[0];
            if (DataManager.deleteRequest(requestId, studentUser.getId())) {
                JOptionPane.showMessageDialog(this, "Request deleted successfully.");
                refreshRequests();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete request.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Table for Payment History (Center) ---
        String[] columnNames = {"Payment ID", "Date", "Course", "Amount Paid ($)"};
        paymentHistoryTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        paymentHistoryTable = new JTable(paymentHistoryTableModel);
        styleTable(paymentHistoryTable);
        
        JScrollPane scrollPane = new JScrollPane(paymentHistoryTable);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);

        // --- Summary Panel (South) ---
        JPanel summaryPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        summaryPanel.setBackground(BG_COLOR);
        
        // Panel for totals
        JPanel totalsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        totalsPanel.setBackground(BG_COLOR);
        feesLabel = new JLabel("Total Fees: $0.00");
        paidLabel = new JLabel("Total Paid: $0.00");
        balanceLabel = new JLabel("Balance Due: $0.00");
        styleLargeLabel(feesLabel);
        styleLargeLabel(paidLabel);
        styleLargeLabel(balanceLabel);
        totalsPanel.add(feesLabel);
        totalsPanel.add(paidLabel);
        totalsPanel.add(balanceLabel);
        
        // Panel for refresh button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BG_COLOR);
        JButton refreshButton = createStyledButton("Refresh Status & History");
        buttonPanel.add(refreshButton);
        
        summaryPanel.add(totalsPanel);
        summaryPanel.add(buttonPanel);
        panel.add(summaryPanel, BorderLayout.SOUTH);

        // --- Action Listener ---
        refreshButton.addActionListener(e -> refreshPayments());
        
        return panel;
    }
    
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea resultsArea = new JTextArea("Click 'View/Refresh' to see your results.");
        styleJTextArea(resultsArea);
        resultsArea.setEditable(false);
        panel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BG_COLOR);
        JButton refreshButton = createStyledButton("View / Refresh Results");
        refreshButton.addActionListener(e -> {
            String report = DataManager.getStudentResultsReport(studentUser.getId());
            resultsArea.setText(report);
            resultsArea.setCaretPosition(0);
        });
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
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
        JTextField txtFullName = new JTextField(studentUser.getFullName(), 20); gbc.gridx = 1; panel.add(txtFullName, gbc);

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

    private void refreshSchedule() {
        scheduleTableModel.setRowCount(0); // Clear the table
        List<String[]> scheduleData = DataManager.getStudentScheduleForTable(studentUser.getId());
        for (String[] row : scheduleData) {
            scheduleTableModel.addRow(row);
        }
    }
    
    private void refreshRequests() {
        Map<String, String> requests = DataManager.getPendingRequests(studentUser.getId());
        Vector<String> requestDisplay = new Vector<>();
        requests.forEach((id, details) -> requestDisplay.add(id + ": " + details));
        requestList.setListData(requestDisplay);
    }
    
    private void refreshPayments() {
        Map<String, Double> status = DataManager.getPaymentStatus(studentUser.getId());
        feesLabel.setText(String.format("Total Course Fees: $%.2f", status.getOrDefault("totalFees", 0.0)));
        paidLabel.setText(String.format("Total Amount Paid: $%.2f", status.getOrDefault("totalPaid", 0.0)));
        double balance = status.getOrDefault("balance", 0.0);
        balanceLabel.setText(String.format("Balance Due: $%.2f", balance));
        balanceLabel.setForeground(balance > 0 ? Color.ORANGE : TEXT_COLOR);

        // 2. Refresh the payment history table
        paymentHistoryTableModel.setRowCount(0); // Clear the table
        List<String[]> history = DataManager.getStudentPaymentHistory(studentUser.getId());
        for (String[] payment : history) {
            // The data is already formatted: [PaymentID, Date, CourseName, Amount]
            paymentHistoryTableModel.addRow(payment);
        }
    }
    
    private void openChatDialog() {
        // IMPORTANT: Ensure 'currentUser' is set to the correct user for the dashboard
        // e.g., this.adminUser, this.receptionistUser, etc.
        User currentUser = this.studentUser; // Change this for each dashboard!

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
        // IMPORTANT: Change "this.adminUser" to the correct variable for the specific dashboard
        User currentUser = this.studentUser;
        int unreadCount = DataManager.getUnreadMessageCount(currentUser); 
        
        if (unreadCount > 0) {
            btnChat.setText("Chat (" + unreadCount + " unread)");
            btnChat.setForeground(Color.RED);
            btnChat.setFont(new Font(btnChat.getFont().getName(), Font.BOLD, btnChat.getFont().getSize()));
        } else {
            btnChat.setText("Chat");
            btnChat.setForeground(Color.WHITE);
            btnChat.setFont(new Font(btnChat.getFont().getName(), Font.PLAIN, btnChat.getFont().getSize()));
        }
    }

    // --- ANNOUNCEMENT SYSTEM METHODS ---

    private void openAnnouncements() {
        // IMPORTANT: Change "this.adminUser" to the correct variable for the specific dashboard
        AnnouncementsFrame announcementsFrame = new AnnouncementsFrame(this.studentUser);
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
        // IMPORTANT: Change "this.adminUser" to the correct variable for the specific dashboard
        User currentUser = this.studentUser;
        Set<String> readIds = DataManager.getReadAnnouncementIds(currentUser);
        List<Announcement> allAnnouncements = DataManager.getAllAnnouncements();
        long unreadCount = allAnnouncements.stream().filter(a -> !readIds.contains(a.getId())).count();

        if (unreadCount > 0) {
            btnAnnouncements.setText("Announcements (" + unreadCount + ")");
            btnAnnouncements.setForeground(Color.CYAN); // Using a different color for distinction
            btnAnnouncements.setFont(new Font(btnAnnouncements.getFont().getName(), Font.BOLD, btnAnnouncements.getFont().getSize()));
        } else {
            btnAnnouncements.setText("Announcements");
            btnAnnouncements.setForeground(Color.WHITE);
            btnAnnouncements.setFont(new Font(btnAnnouncements.getFont().getName(), Font.PLAIN, btnAnnouncements.getFont().getSize()));
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
}
