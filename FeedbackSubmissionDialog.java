// FeedbackSubmissionDialog.java

import java.awt.*;
import java.util.List;
import javax.swing.*;

public class FeedbackSubmissionDialog extends JDialog {

    private User currentUser;

    public FeedbackSubmissionDialog(JFrame owner, User currentUser) {
        super(owner, "Submit Feedback", true);
        this.currentUser = currentUser;

        setSize(500, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<CourseFeedbackItem> courseComboBox = new JComboBox<>();
        JTextArea contentArea = new JTextArea(5, 20);
        JSlider ratingSlider = new JSlider(1, 5, 3);
        ratingSlider.setMajorTickSpacing(1);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);

        List<String[]> enrolledCourses = DataManager.getStudentScheduleForTable(currentUser.getId());
        if (enrolledCourses.isEmpty()) {
            courseComboBox.addItem(new CourseFeedbackItem("No courses available for feedback", null, null));
            courseComboBox.setEnabled(false);
        } else {
            for (String[] courseData : enrolledCourses) {
                String courseName = courseData[0];
                String tutorName = courseData[1];
                User tutor = DataManager.findUserByUsername(tutorName);
                if (tutor != null) {
                    courseComboBox.addItem(new CourseFeedbackItem(courseName, tutor.getId(), tutorName));
                }
            }
        }

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Feedback for Course:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(courseComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Rating:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(ratingSlider, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHEAST;
        panel.add(new JLabel("Content:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(contentArea), gbc);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            CourseFeedbackItem selectedCourseItem = (CourseFeedbackItem) courseComboBox.getSelectedItem();
            String content = contentArea.getText();
            int rating = ratingSlider.getValue();

            if (selectedCourseItem == null || selectedCourseItem.getTutorId() == null || content.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a course and provide content for your feedback.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String subject = selectedCourseItem.getCourseName();
            String targetTutorId = selectedCourseItem.getTutorId();
            String targetRole = "Tutor";
            if (DataManager.submitFeedback(currentUser.getId(), targetRole, targetTutorId, subject, rating, content)) {
                JOptionPane.showMessageDialog(this, "Feedback submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit feedback.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(panel, BorderLayout.CENTER);
        add(submitButton, BorderLayout.SOUTH);
    }

    private static class CourseFeedbackItem {
        private final String courseName;
        private final String tutorId;
        private final String tutorName;

        public CourseFeedbackItem(String courseName, String tutorId, String tutorName) {
            this.courseName = courseName;
            this.tutorId = tutorId;
            this.tutorName = tutorName;
        }

        public String getCourseName() {
            return courseName;
        }

        public String getTutorId() {
            return tutorId;
        }

        @Override
        public String toString() {
            if (tutorName == null) {
                return courseName;
            }
            return courseName + " (Tutor: " + tutorName + ")";
        }
    }
}