import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ResultCardPanel extends JPanel {
    public ResultCardPanel(ResultSummary summary) {
        setLayout(new GridBagLayout());
        setBackground(new Color(45, 45, 45));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)),
            new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 0, 2, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel courseNameLabel = new JLabel(summary.getCourseName());
        courseNameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        courseNameLabel.setForeground(new Color(80, 153, 227));

        JLabel resultLabel = new JLabel("Result: ");
        resultLabel.setForeground(new Color(180, 180, 180));
        JLabel resultValue = new JLabel(summary.getGradeLetter());
        resultValue.setForeground(new Color(45, 186, 78));

        JPanel resultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        resultPanel.setOpaque(false);
        resultPanel.add(resultLabel);
        resultPanel.add(resultValue);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        add(courseNameLabel, gbc);

        gbc.gridy++;
        add(resultPanel, gbc);

    }
}