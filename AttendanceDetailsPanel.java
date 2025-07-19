import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class AttendanceDetailsPanel extends JPanel {

    public AttendanceDetailsPanel(List<String[]> records) {
        super(new BorderLayout());
        setOpaque(false); // Make it transparent to match the parent's background
        setBorder(new EmptyBorder(0, 15, 10, 15)); // Add some padding

        String[] columnNames = {"Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable detailsTable = new JTable(model);

        // Populate the table with attendance records
        for (String[] record : records) {
            model.addRow(new Object[]{record[2], record[3]});
        }
        
        // Style the table
        detailsTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        detailsTable.setRowHeight(28);
        detailsTable.setFillsViewportHeight(true);
        detailsTable.setBackground(new Color(55, 55, 55)); // Slightly lighter than the card
        detailsTable.setForeground(Color.WHITE);
        detailsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        detailsTable.getColumnModel().getColumn(1).setCellRenderer(new StatusRenderer());

        add(new JScrollPane(detailsTable), BorderLayout.CENTER);
    }

    // A self-contained renderer for coloring the status cells
    public static class StatusRenderer extends DefaultTableCellRenderer {
        private static final Color PRESENT_BG = new Color(39, 87, 61);
        private static final Color LATE_BG = new Color(103, 81, 23);
        private static final Color ABSENT_BG = new Color(112, 41, 41);
        private static final Color TEXT_COLOR = new Color(230, 230, 230);

        public StatusRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cell.setForeground(TEXT_COLOR);
            String status = value.toString();
            switch (status) {
                case "Present": cell.setBackground(PRESENT_BG); break;
                case "Late": cell.setBackground(LATE_BG); break;
                case "Absent": cell.setBackground(ABSENT_BG); break;
                default: cell.setBackground(table.getBackground()); break;
            }
            // Remove the default selection color to keep our custom background
            if (isSelected) {
                setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, UIManager.getColor("Table.selectionBackground")));
            } else {
                setBorder(null);
            }
            return cell;
        }
    }
}