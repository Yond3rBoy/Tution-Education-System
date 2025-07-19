import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ResultsHeaderPanel extends JPanel {
    public ResultsHeaderPanel(int modulesPassed, double gpa) {
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel modulesLabel = new JLabel("Modules Passed: " + modulesPassed);
        modulesLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        modulesLabel.setForeground(new Color(200, 200, 200));

        JLabel gpaLabel = new JLabel(String.format("GPA: %.2f", gpa));
        gpaLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gpaLabel.setForeground(new Color(200, 200, 200));

        JLabel titleLabel = new JLabel("Overall Results");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setOpaque(false);
        summaryPanel.add(modulesLabel, BorderLayout.WEST);
        summaryPanel.add(gpaLabel, BorderLayout.EAST);

        add(titleLabel, BorderLayout.NORTH);
        add(summaryPanel, BorderLayout.CENTER);
    }
}