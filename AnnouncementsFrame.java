import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.*;

public class AnnouncementsFrame extends JFrame {
    private User currentUser;
    private JList<Announcement> announcementList;
    private JTextArea contentArea;
    private Set<String> readIds;

    public AnnouncementsFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("Announcements");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);

        announcementList = new JList<>();
        announcementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        announcementList.setCellRenderer(new AnnouncementCellRenderer());
        
        contentArea = new JTextArea("Select an announcement to view its content.");
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setMargin(new Insets(10, 10, 10, 10));

        splitPane.setLeftComponent(new JScrollPane(announcementList));
        splitPane.setRightComponent(new JScrollPane(contentArea));
        add(splitPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back to Dashboard");
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        announcementList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Announcement selected = announcementList.getSelectedValue();
                if (selected != null) {
                    contentArea.setText("Title: " + selected.getTitle() +
                                      "\nAuthor: " + selected.getAuthor() +
                                      "\nDate: " + selected.getDate() +
                                      "\n\n--------------------------------\n\n" +
                                      selected.getContent());
                    contentArea.setCaretPosition(0);

                    DataManager.markAnnouncementAsRead(currentUser, selected.getId());
                    readIds.add(selected.getId());
                    announcementList.repaint();
                }
            }      
        });

        // In the AnnouncementsFrame constructor, after the ListSelectionListener
        backButton.addActionListener(e -> {
            this.dispose(); // This simply closes the current window
        });

        refreshAnnouncements();
    }
    
    private void refreshAnnouncements() {
        this.readIds = DataManager.getReadAnnouncementIds(currentUser);
        List<Announcement> all = DataManager.getAllAnnouncements();
        announcementList.setListData(new Vector<>(all));
    }

    private class AnnouncementCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Announcement announcement = (Announcement) value;
            
            if (readIds != null && readIds.contains(announcement.getId())) {
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
            } else {
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            }
            return label;
        }
    }
}
