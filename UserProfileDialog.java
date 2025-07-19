import java.awt.*;
import javax.swing.*;

public class UserProfileDialog extends JDialog {
    public UserProfileDialog(JFrame parent, User user) {
        super(parent, "User Profile", true);
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add labels for user information
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; panel.add(new JLabel(user.getFullName()), gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; panel.add(new JLabel(user.getRole()), gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1; panel.add(new JLabel(user.getId()), gbc);

        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}