import javax.swing.*;
import java.awt.*;

public class TutorDashboard extends JFrame {
    private Tutor tutor;

    public TutorDashboard(Tutor tutor) {
        this.tutor = tutor;
        setTitle("Tutor Dashboard");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        panel.setBackground(new Color(245, 245, 245));

        JButton viewClassesBtn = new JButton("📚 View Classes");
        JButton updatePerformanceBtn = new JButton("📈 Update Performance");
        JButton logoutBtn = new JButton("🚪 Logout");

        styleButton(viewClassesBtn);
        styleButton(updatePerformanceBtn);
        styleButton(logoutBtn);

        viewClassesBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "View Classes logic here."));
        updatePerformanceBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Update Performance logic here."));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        panel.add(viewClassesBtn);
        panel.add(updatePerformanceBtn);
        panel.add(logoutBtn);

        add(panel);
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    }
}
