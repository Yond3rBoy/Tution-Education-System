import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ReceptionistDashboard extends JFrame {
    private User receptionistUser;
    private JTable studentTable;
    private DefaultTableModel studentTableModel;
    private JButton btnChat; // Chat button

    public ReceptionistDashboard(User user) {
        this.receptionistUser = user;
        setTitle("Receptionist Dashboard - Welcome, " + user.getFullName());
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main container panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top panel for chat button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnChat = new JButton("Chat");
        topPanel.add(btnChat);

        // Tabbed pane for core functions
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Students", createStudentPanel());
        tabbedPane.addTab("My Profile", createProfilePanel());

        // Add components to the main container
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);

        // Add action listener for the chat button
        btnChat.addActionListener(e -> openChatDialog());
        
        // Initial data load
        refreshStudentTable();
        refreshChatNotification();
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

        // Create a scrollable list of available courses
        JList<String> courseList = new JList<>(new Vector<>(DataManager.getAvailableCourses()));
        courseList.setVisibleRowCount(5);
        courseList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.add(new JLabel("Full Name:"));        formPanel.add(txtFullName);
        formPanel.add(new JLabel("Username:"));        formPanel.add(txtUsername);
        formPanel.add(new JLabel("Password:"));        formPanel.add(txtPassword);
        formPanel.add(new JLabel("IC/Passport:"));      formPanel.add(txtIc);
        formPanel.add(new JLabel("Email:"));            formPanel.add(txtEmail);
        formPanel.add(new JLabel("Contact No:"));       formPanel.add(txtContact);
        formPanel.add(new JLabel("Address:"));          formPanel.add(txtAddress);
        formPanel.add(new JLabel("Enroll in Subjects:"));
        formPanel.add(new JScrollPane(courseList));

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Register New Student", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            List<String> selectedCourses = courseList.getSelectedValuesList();
            if (selectedCourses.size() > 3) {
                JOptionPane.showMessageDialog(this, "A student can enroll in a maximum of 3 subjects.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<String> courseIDs = new ArrayList<>();
            for(String courseInfo : selectedCourses) {
                courseIDs.add(courseInfo.split(":")[0]); // Extract CourseID
            }
            
            boolean success = DataManager.registerStudent(
                    txtFullName.getText(), txtUsername.getText(), new String(txtPassword.getPassword()),
                    txtIc.getText(), txtEmail.getText(), txtContact.getText(), txtAddress.getText(), courseIDs);

            if (success) {
                JOptionPane.showMessageDialog(this, "Student registered successfully!");
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
        
        // UI for the dialog
        Map<String, String> currentEnrollments = DataManager.getStudentEnrollments(studentId);
        JList<String> courseList = new JList<>(new Vector<>(DataManager.getAvailableCourses()));
        courseList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Pre-select currently enrolled courses
        List<Integer> selectedIndices = new ArrayList<>();
        List<String> allCourses = DataManager.getAvailableCourses();
        for (String enrollmentInfo : currentEnrollments.values()) {
            for (int i = 0; i < allCourses.size(); i++) {
                if(allCourses.get(i).contains(enrollmentInfo)) {
                    selectedIndices.add(i);
                }
            }
        }
        courseList.setSelectedIndices(selectedIndices.stream().mapToInt(i->i).toArray());
        
        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(courseList), "Update Enrollments for " + studentTable.getValueAt(selectedRow, 2), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            List<String> newCourseIDs = courseList.getSelectedValuesList().stream()
                                                  .map(s -> s.split(":")[0])
                                                  .collect(Collectors.toList());
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

}
