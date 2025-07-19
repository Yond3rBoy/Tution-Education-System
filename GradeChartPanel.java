import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

public class GradeChartPanel extends JPanel {
    private final Map<String, Long> gradeData;
    private final Map<String, Color> gradeColors = new LinkedHashMap<>();

    public GradeChartPanel(Map<String, Long> gradeData) {
        this.gradeData = gradeData;
        setBackground(new Color(45, 45, 45));
        setPreferredSize(new Dimension(300, 200));

        // Define colors for the bars
        gradeColors.put("A+", new Color(227, 89, 102));
        gradeColors.put("A", new Color(67, 137, 203));
        gradeColors.put("B+", new Color(207, 182, 72));
        gradeColors.put("B", new Color(74, 184, 163));
        gradeColors.put("C+", new Color(155, 92, 181));
        gradeColors.put("C", Color.GRAY);
        gradeColors.put("D", Color.DARK_GRAY);
        gradeColors.put("F", Color.DARK_GRAY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (gradeData == null || gradeData.isEmpty()) return;

        int maxCount = gradeData.values().stream().max(Long::compare).orElse(1L).intValue();
        int barWidth = 30;
        int barGap = 20;
        int startX = 30;
        int startY = getHeight() - 40;

        // Draw Y-axis
        g2d.setColor(Color.GRAY);
        g2d.drawLine(startX, startY, startX, 20);
        for (int i = 0; i <= maxCount; i++) {
            int y = startY - (i * (startY - 20) / maxCount);
            g2d.drawString(String.valueOf(i), startX - 15, y + 5);
        }

        int currentX = startX + barGap;
        for (Map.Entry<String, Long> entry : gradeData.entrySet()) {
            String grade = entry.getKey();
            int count = entry.getValue().intValue();
            int barHeight = (int) (((double) count / maxCount) * (startY - 20));

            g2d.setColor(gradeColors.getOrDefault(grade, Color.LIGHT_GRAY));
            g2d.fillRect(currentX, startY - barHeight, barWidth, barHeight);
            
            g2d.setColor(Color.WHITE);
            g2d.drawString(grade, currentX + barWidth / 2 - g2d.getFontMetrics().stringWidth(grade) / 2, startY + 15);
            
            currentX += barWidth + barGap;
        }
    }
}