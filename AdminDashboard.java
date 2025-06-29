import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    private Admin admin;

    public AdminDashboard(Admin admin) {
        this.admin = admin;
        setTitle("Admin Dashboard");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        panel.setBackground(new Color(245, 245, 245));

        JButton viewUsersBtn = new JButton("👥 View All Users");
        JButton changePassBtn = new JButton("🔑 Change Password");
        JButton logoutBtn = new JButton("🚪 Logout");

        styleButton(viewUsersBtn);
        styleButton(changePassBtn);
        styleButton(logoutBtn);

        viewUsersBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "View Users logic here."));
        changePassBtn.addActionListener(e -> {
            String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");
            if (newPassword != null) JOptionPane.showMessageDialog(this, "Password changed to: " + newPassword);
        });
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        panel.add(viewUsersBtn);
        panel.add(changePassBtn);
        panel.add(logoutBtn);

        add(panel);
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    }
}
