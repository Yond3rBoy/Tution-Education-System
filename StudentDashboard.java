import javax.swing.*;
import java.awt.*;

public class StudentDashboard extends JFrame {
    private Student student;

    public StudentDashboard(Student student) {
        this.student = student;
        setTitle("Student Dashboard");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        panel.setBackground(new Color(245, 245, 245));

        JButton viewScheduleBtn = new JButton("📅 View Schedule");
        JButton checkPerformanceBtn = new JButton("📊 Check Performance");
        JButton logoutBtn = new JButton("🚪 Logout");

        styleButton(viewScheduleBtn);
        styleButton(checkPerformanceBtn);
        styleButton(logoutBtn);

        viewScheduleBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "View Schedule logic here."));
        checkPerformanceBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Check Performance logic here."));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        panel.add(viewScheduleBtn);
        panel.add(checkPerformanceBtn);
        panel.add(logoutBtn);

        add(panel);
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    }
}
