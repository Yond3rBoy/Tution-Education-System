import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AnnouncementsFrame extends JFrame {
    private User currentUser;
    private JList<Announcement> announcementList;
    private JTextArea contentArea;
    private Set<String> readIds;
    
    private JLabel announcementTitleLabel; // To display the title
    private JButton newButton;
    private JButton editButton;
    private JButton deleteButton;

    private static final Color BG_COLOR = new Color(24, 34, 54);
    private static final Color FIELD_BG_COLOR = new Color(42, 53, 76);
    private static final Color PRIMARY_COLOR = new Color(67, 102, 163);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);
    private static final Color SIDEBAR_COLOR = new Color(30, 41, 61);

    public AnnouncementsFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("Announcements");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // --- Left Sidebar Panel (Announcement List) ---
        add(createLeftSidebar(), BorderLayout.WEST);

        // --- Right Main Panel (Content Area) ---
        add(createRightContentPanel(), BorderLayout.CENTER);

        // Action Listeners for buttons
        announcementList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onAnnouncementSelected();
            }
        });
        newButton.addActionListener(e -> showCreateAnnouncementDialog());
        editButton.addActionListener(e -> editSelectedAnnouncement());
        deleteButton.addActionListener(e -> deleteSelectedAnnouncement());
        
        // Initial state
        refreshAnnouncements();
        updateButtonState();
        onAnnouncementSelected(); // To set initial text
    }

    private JPanel createLeftSidebar() {
        JPanel leftPanel = new JPanel(new BorderLayout(5, 10));
        leftPanel.setBackground(SIDEBAR_COLOR);
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        leftPanel.setPreferredSize(new Dimension(300, 0));

        // Logo/Title at the top
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.setBackground(SIDEBAR_COLOR);
        try {
            ImageIcon logoIcon = new ImageIcon(new ImageIcon(getClass().getResource("/assets/announcement_logo.png")).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
            JLabel logoLabel = new JLabel(logoIcon);
            logoLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            imagePanel.add(logoLabel);
        } catch (Exception e) {
            imagePanel.add(new JLabel("Announcements"));
        }
        leftPanel.add(imagePanel, BorderLayout.NORTH);

        // Announcement list in the center
        announcementList = new JList<>();
        announcementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        announcementList.setCellRenderer(new AnnouncementCellRenderer());
        announcementList.setBackground(SIDEBAR_COLOR);
        
        JScrollPane listScrollPane = new JScrollPane(announcementList);
        listScrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BG_COLOR));
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        // Action buttons at the bottom
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionButtonPanel.setBackground(SIDEBAR_COLOR);
        newButton = new JButton("New");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        actionButtonPanel.add(newButton);
        actionButtonPanel.add(editButton);
        actionButtonPanel.add(deleteButton);

        if (currentUser.getRole().equals("Admin") || currentUser.getRole().equals("Receptionist")) {
            leftPanel.add(actionButtonPanel, BorderLayout.SOUTH);
        }
        
        return leftPanel;
    }

    private JPanel createRightContentPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBackground(BG_COLOR);
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header for the selected announcement title
        JPanel contentHeaderPanel = new JPanel(new BorderLayout());
        contentHeaderPanel.setBackground(FIELD_BG_COLOR);
        contentHeaderPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        announcementTitleLabel = new JLabel("Select an announcement from the list");
        announcementTitleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        announcementTitleLabel.setForeground(TEXT_COLOR);
        contentHeaderPanel.add(announcementTitleLabel, BorderLayout.CENTER);
        rightPanel.add(contentHeaderPanel, BorderLayout.NORTH);

        // Main content text area
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setFont(new Font("Arial", Font.PLAIN, 16));
        contentArea.setBackground(BG_COLOR);
        contentArea.setForeground(TEXT_COLOR);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setMargin(new Insets(15, 15, 15, 15));
        
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        contentScrollPane.setBorder(null);
        rightPanel.add(contentScrollPane, BorderLayout.CENTER);
        
        return rightPanel;
    }

    private void onAnnouncementSelected() {
        Announcement selected = announcementList.getSelectedValue();
        if (selected != null) {
            announcementTitleLabel.setText(selected.getTitle());
            contentArea.setText("Author: " + selected.getAuthor() +
                              "\nDate: " + selected.getDate() +
                              "\n\n--------------------------------\n\n" +
                              selected.getContent());
            contentArea.setCaretPosition(0);

            if (!readIds.contains(selected.getId())) {
                DataManager.markAnnouncementAsRead(currentUser, selected.getId());
                readIds.add(selected.getId());
                announcementList.repaint(); // To update the font (bold -> plain)
            }
        } else {
            announcementTitleLabel.setText("No Announcement Selected");
            contentArea.setText("Please select an announcement from the list on the left.");
        }
        updateButtonState();
    }
    
    private void showCreateAnnouncementDialog() {
        ImageIcon dialogIcon = null;
        try {
            dialogIcon = new ImageIcon(new ImageIcon(getClass().getResource("/assets/announcement_logo.png")).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.err.println("CRITICAL: Could not load resource /assets/announcement_logo.png.");
        }

        JTextField titleField = new JTextField(30);
        JTextArea contentArea = new JTextArea(5, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel titlePanel = new JPanel(new BorderLayout(10, 0));
        titlePanel.add(new JLabel("Title:"), BorderLayout.WEST);
        titlePanel.add(titleField, BorderLayout.CENTER);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(contentScrollPane, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Create New Announcement",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, dialogIcon
        );

        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            if (title.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and content cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (DataManager.createAnnouncement(this.currentUser, title, content)) {
                JOptionPane.showMessageDialog(this, "Announcement created successfully.");
                refreshAnnouncements();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create announcement.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteSelectedAnnouncement() {
        Announcement selected = announcementList.getSelectedValue();
        if (selected == null) return;

        int choice = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this announcement?\n\nTitle: " + selected.getTitle(),
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            if (DataManager.deleteAnnouncement(selected.getId(), currentUser)) {
                JOptionPane.showMessageDialog(this, "Announcement deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAnnouncements();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete announcement.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editSelectedAnnouncement() {
        Announcement selected = announcementList.getSelectedValue();
        if (selected == null) return;

        ImageIcon dialogIcon = null;
        try {
            dialogIcon = new ImageIcon(new ImageIcon(getClass().getResource("/assets/announcement_logo.png")).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.err.println("CRITICAL: Could not load resource /assets/announcement_logo.png.");
        }

        JTextField titleField = new JTextField(selected.getTitle(), 30);
        JTextArea contentField = new JTextArea(selected.getContent(), 5, 30);
        contentField.setLineWrap(true);
        contentField.setWrapStyleWord(true);
        
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Title:"), BorderLayout.NORTH);
        panel.add(titleField, BorderLayout.CENTER);
        panel.add(new JScrollPane(contentField), BorderLayout.SOUTH);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Edit Announcement", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, dialogIcon
        );

        if (result == JOptionPane.OK_OPTION) {
            String newTitle = titleField.getText().trim();
            String newContent = contentField.getText().trim();
            if (newTitle.isEmpty() || newContent.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and content cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (DataManager.updateAnnouncement(selected.getId(), newTitle, newContent, currentUser)) {
                JOptionPane.showMessageDialog(this, "Announcement updated successfully.");
                refreshAnnouncements();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update announcement.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshAnnouncements() {
        this.readIds = DataManager.getReadAnnouncementIds(currentUser);
        List<Announcement> all = DataManager.getAllAnnouncements();
        announcementList.setListData(new Vector<>(all));
        if (!all.isEmpty()) {
            announcementList.setSelectedIndex(0);
        }
    }

    private void updateButtonState() {
        Announcement selected = announcementList.getSelectedValue();
        boolean isOwner = selected != null && selected.getAuthor().equals(currentUser.getUsername());
        
        editButton.setEnabled(isOwner);
        deleteButton.setEnabled(isOwner);
    }

    private class AnnouncementCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Announcement announcement = (Announcement) value;
            
            label.setBorder(new EmptyBorder(5, 10, 5, 10));

            if (readIds != null && readIds.contains(announcement.getId())) {
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
            } else {
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            }

            if (isSelected) {
                label.setBackground(PRIMARY_COLOR);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(SIDEBAR_COLOR);
                label.setForeground(TEXT_COLOR);
            }

            return label;
        }
    }
}