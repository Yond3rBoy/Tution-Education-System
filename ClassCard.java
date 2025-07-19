import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Hashtable;
import javax.swing.*;

public class ClassCard extends RoundedPanel {

    private static final Color CARD_BACKGROUND = new Color(45, 45, 45);
    private static final Color TEXT_PRIMARY = new Color(235, 235, 235);
    private static final Color TEXT_SECONDARY = new Color(180, 180, 180);
    private static final Color TEXT_CODE_RED = new Color(227, 89, 102);
    private static final Color TEXT_LINK_BLUE = new Color(80, 153, 227);

    /**
     * CONSTRUCTOR MODIFIED: The 'location' parameter has been removed.
     */
    public ClassCard(String subjectCode, String subjectName, String time, String tutor) {
        super(15, CARD_BACKGROUND);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // --- Row 1: Subject Code & Name (No change) ---
        JLabel codeLabel = new JLabel(subjectCode);
        codeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        codeLabel.setForeground(TEXT_CODE_RED);

        JLabel nameLabel = new JLabel(subjectName.toUpperCase());
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        nameLabel.setForeground(TEXT_PRIMARY);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span across two columns
        add(codeLabel, gbc);

        gbc.gridy++;
        add(nameLabel, gbc);

        // --- Row 2: Time and Tutor (Location removed) ---
        JLabel timeLabel = new JLabel("🕒 " + formatTime(time));
        styleSecondaryLabel(timeLabel);

        JLabel tutorLabel = new JLabel("👤 " + tutor);
        styleLink(tutorLabel);
        
        // --- LAYOUT MODIFIED: Add time and tutor labels ---
        gbc.gridy++;
        gbc.insets = new Insets(10, 5, 2, 20); // Top margin
        gbc.gridwidth = 1; // Reset gridwidth
        add(timeLabel, gbc);

        gbc.gridx = 1; // Place tutor label in the next column
        add(tutorLabel, gbc);

        // The locationLabel has been completely removed.
    }

    private void styleSecondaryLabel(JLabel label) {
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
    }
    
    private void styleLink(JLabel label) {
        Font font = new Font("SansSerif", Font.PLAIN, 12);
        Hashtable<TextAttribute, Object> attributes = new Hashtable<>();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        
        label.setFont(font.deriveFont(attributes));
        label.setForeground(TEXT_LINK_BLUE);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private String formatTime(String timeRange) {
        try {
            String[] parts = timeRange.split(" ");
            String range = parts[0];
            String ampm = parts[1];
            String[] hours = range.split("-");
            return String.format("%s:00 %s - %s:00 %s", hours[0], ampm, hours[1], ampm);
        } catch(Exception e) {
            return timeRange; // Return original if format is unexpected
        }
    }
}