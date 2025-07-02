import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.*;

public class StudentDashboard extends JFrame {
    private User studentUser;
    private JList<String> scheduleList;
    private JList<String> requestList;
    private JLabel feesLabel, paidLabel, balanceLabel;

    public StudentDashboard(User user) {
        this.studentUser = user;
        setTitle("Student Dashboard - Welcome, " + user.getFullName());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Schedule", createSchedulePanel());
        tabbedPane.addTab("Enrollment Requests", createRequestPanel());
        tabbedPane.addTab("Payment Status", createPaymentPanel());
        tabbedPane.addTab("My Profile", createProfilePanel());
        add(tabbedPane);
        
        // Load initial data
        refreshAllData();
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
}