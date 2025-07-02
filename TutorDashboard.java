import java.awt.*;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TutorDashboard extends JFrame {
    private User tutorUser;
    private JTable courseTable;
    private DefaultTableModel courseTableModel;
    private JComboBox<String> courseSelector;

    public TutorDashboard(User user) {
        this.tutorUser = user;
        setTitle("Tutor Dashboard - Welcome, " + user.getFullName());
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage My Courses", createCoursePanel());
        tabbedPane.addTab("View Enrolled Students", createViewStudentsPanel());
        tabbedPane.addTab("My Profile", createProfilePanel());
        add(tabbedPane);

        refreshCourseData();
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

    // --- Panel for Viewing Students ---
    private JPanel createViewStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select a course to view students:"));
        courseSelector = new JComboBox<>();
        topPanel.add(courseSelector);
        
        JList<String> studentList = new JList<>();
        studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

    // --- Panel for Profile Updates ---
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Editable fields: Full Name, Password, Specialization
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
        courseTableModel.setRowCount(0); // Clear table
        List<String[]> courses = DataManager.getCoursesByTutor(tutorUser.getId());
        for (String[] courseData : courses) {
            // "ID", "Course Name", "Level", "Subject", "Fee"
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
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Course Name:")); panel.add(nameField);
        panel.add(new JLabel("Level (e.g., Primary 5):")); panel.add(levelField);
        panel.add(new JLabel("Subject (e.g., Math):")); panel.add(subjectField);
        panel.add(new JLabel("Fee (e.g., 200.00):")); panel.add(feeField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double fee = Double.parseDouble(feeField.getText());
                if (DataManager.addCourse(nameField.getText(), tutorUser.getId(), levelField.getText(), subjectField.getText(), fee)) {
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

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Course Name:")); panel.add(nameField);
        panel.add(new JLabel("Level:")); panel.add(levelField);
        panel.add(new JLabel("Subject:")); panel.add(subjectField);
        panel.add(new JLabel("Fee:")); panel.add(feeField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Course " + courseId, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double fee = Double.parseDouble(feeField.getText());
                if (DataManager.updateCourse(courseId, nameField.getText(), levelField.getText(), subjectField.getText(), fee)) {
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
}