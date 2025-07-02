import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.*;

public class StudentDashboard extends JFrame {
    private User studentUser;
    private JList<String> scheduleList;
    private JList<String> requestList;
    private JLabel feesLabel, paidLabel, balanceLabel;
    private JButton btnChat;
    private JButton btnAnnouncements;

    public StudentDashboard(User user) {
        this.studentUser = user;
        setTitle("Student Dashboard - Welcome, " + user.getFullName());
        setSize(800, 600);
        // CHANGED: Dispose this window only, don't exit the whole app
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main container panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top panel for action buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout"); // NEW Logout button
        btnAnnouncements = new JButton("Announcements");
        btnChat = new JButton("Chat");
        topPanel.add(btnAnnouncements);
        topPanel.add(btnChat);
        topPanel.add(btnLogout); // Add logout button to panel
        
        // Tabbed pane for core functions
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Schedule", createSchedulePanel());
        tabbedPane.addTab("Enrollment Requests", createRequestPanel());
        tabbedPane.addTab("Payment Status", createPaymentPanel());
        tabbedPane.addTab("My Results", createResultsPanel());
        tabbedPane.addTab("My Profile", createProfilePanel());
        
        // Assemble the main view
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
        
        // --- Action Listeners ---
        btnLogout.addActionListener(e -> logout()); // NEW Logout action
        btnChat.addActionListener(e -> openChatDialog());
        btnAnnouncements.addActionListener(e -> openAnnouncements());
        
        // Add a listener for the 'X' button
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                logout();
            }
        });

        // --- Initial Data Load & Notifications ---
        refreshAllData();
        refreshChatNotification();
        refreshAnnouncementNotification();
    }
    
    // --- Logout Method ---
    private void logout() {
        this.dispose(); // Close this dashboard window
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true)); // Open a new login window
    }
    
    private void refreshAllData() {
        refreshSchedule();
        refreshRequests();
        refreshPayments();
    }

    // --- Panel for Viewing Class Schedule ---
    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        scheduleList = new JList<>();
        panel.add(new JScrollPane(scheduleList), BorderLayout.CENTER);
        JButton refreshButton = new JButton("Refresh Schedule");
        refreshButton.addActionListener(e -> refreshSchedule());
        panel.add(refreshButton, BorderLayout.SOUTH);
        return panel;
    }

    // --- Panel for Managing Enrollment Requests ---
    private JPanel createRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Top part for creating a new request
        JPanel newRequestPanel = new JPanel(new BorderLayout());
        newRequestPanel.setBorder(BorderFactory.createTitledBorder("Submit a New Request"));
        JTextArea requestDetailsArea = new JTextArea(3, 30);
        newRequestPanel.add(new JScrollPane(requestDetailsArea), BorderLayout.CENTER);
        JButton submitButton = new JButton("Submit");
        newRequestPanel.add(submitButton, BorderLayout.EAST);
        
        // Bottom part for viewing/deleting pending requests
        JPanel pendingPanel = new JPanel(new BorderLayout());
        pendingPanel.setBorder(BorderFactory.createTitledBorder("My Pending Requests"));
        requestList = new JList<>();
        pendingPanel.add(new JScrollPane(requestList), BorderLayout.CENTER);
        JButton deleteButton = new JButton("Delete Selected Request");
        pendingPanel.add(deleteButton, BorderLayout.SOUTH);

        panel.add(newRequestPanel, BorderLayout.NORTH);
        panel.add(pendingPanel, BorderLayout.CENTER);

        // Action Listeners
        submitButton.addActionListener(e -> {
            String details = requestDetailsArea.getText();
            if (details.trim().isEmpty()) {
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
                JOptionPane.showMessageDialog(this, "Failed to delete request. It might have been processed already.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // --- Panel for Viewing Payment Status ---
    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        feesLabel = new JLabel("Total Course Fees: $0.00");
        paidLabel = new JLabel("Total Amount Paid: $0.00");
        balanceLabel = new JLabel("Current Balance: $0.00");
        
        feesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        paidLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JButton refreshButton = new JButton("Refresh Payment Status");
        refreshButton.addActionListener(e -> refreshPayments());
        
        panel.add(feesLabel);
        panel.add(paidLabel);
        panel.add(balanceLabel);
        panel.add(refreshButton);
        
        return panel;
    }
    
    // --- Panel for Profile Updates ---
    private JPanel createProfilePanel() {
        // This is identical to the other dashboards' profile panels
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Full Name:"), gbc);
        JTextField txtFullName = new JTextField(studentUser.getFullName(), 20);
        gbc.gridx = 1; panel.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("New Password (leave blank):"), gbc);
        JPasswordField txtPassword = new JPasswordField(20);
        gbc.gridx = 1; panel.add(txtPassword, gbc);
        
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton btnUpdate = new JButton("Update My Profile");
        panel.add(btnUpdate, gbc);

        btnUpdate.addActionListener(e -> {
            String newFullName = txtFullName.getText().trim();
            String newPasswordStr = new String(txtPassword.getPassword());
            String finalPassword = newPasswordStr.isEmpty() ? studentUser.getPassword() : newPasswordStr;
            
            User updatedUser = new User(
                studentUser.getId(), studentUser.getUsername(), finalPassword,
                studentUser.getRole(), newFullName, ""); // Specialization is blank for students

            if (DataManager.updateUser(updatedUser)) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                this.studentUser = updatedUser;
                this.setTitle("Student Dashboard - Welcome, " + updatedUser.getFullName());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // --- Data Refresh Methods ---
    private void refreshSchedule() {
        List<String> scheduleData = DataManager.getStudentSchedule(studentUser.getId());
        scheduleList.setListData(new Vector<>(scheduleData));
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
        balanceLabel.setText(String.format("Current Balance: $%.2f", balance));
        balanceLabel.setForeground(balance > 0 ? Color.RED : new Color(0, 128, 0)); // Red if balance due, green otherwise
    }

    private void openChatDialog() {
        User currentUser = this.studentUser; // Use the correct user object

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
        User currentUser = this.studentUser; // Use the correct user object
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

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JTextArea resultsArea = new JTextArea("Click 'View/Refresh' to see your results.");
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);
        
        JButton refreshButton = new JButton("View / Refresh Results");
        refreshButton.addActionListener(e -> {
            String report = DataManager.getStudentResultsReport(studentUser.getId());
            resultsArea.setText(report);
            resultsArea.setCaretPosition(0); // Scroll to top
        });
        
        panel.add(refreshButton, BorderLayout.SOUTH);
        return panel;
    }

    private void openAnnouncements() {
        // CHANGE this.adminUser to the correct user for each dashboard
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
    
    // Method to update the button with unread count
    public void refreshAnnouncementNotification() {
        // CHANGE this.adminUser to the correct user for each dashboard
        Set<String> readIds = DataManager.getReadAnnouncementIds(this.studentUser);
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
}
