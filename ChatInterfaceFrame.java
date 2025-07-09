import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.Border;

public class ChatInterfaceFrame extends JFrame {
    private User currentUser;
    private JComboBox<String> roleFilterComboBox;
    private JList<UserWithUnreadCount> userList;
    private JTextArea conversationArea;
    private JTextField messageField;
    private JButton sendButton;
    
    private List<User> allChattableUsers;

    private static final Color BG_COLOR = new Color(24, 34, 54);
    private static final Color FIELD_BG_COLOR = new Color(42, 53, 76);
    private static final Color PRIMARY_COLOR = new Color(67, 102, 163);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);

    public ChatInterfaceFrame(User currentUser) {
        this.currentUser = currentUser;
        this.allChattableUsers = DataManager.getUsersForChat(currentUser);

        setTitle("Chat");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBackground(BG_COLOR);
        
        roleFilterComboBox = new JComboBox<>(DataManager.getChatFilterRolesForUser(currentUser));
        roleFilterComboBox.addActionListener(e -> filterUserList());
        leftPanel.add(roleFilterComboBox, BorderLayout.NORTH);

        userList = new JList<>();
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new UserWithCountCellRenderer());
        userList.addListSelectionListener(e -> {
            // This check is crucial to prevent the listener from firing multiple times for a single click.
            if (!e.getValueIsAdjusting()) {
                onUserSelected();
            }
        });
        styleJList(userList);
        leftPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBackground(BG_COLOR);
        
        conversationArea = new JTextArea("Select a user to view the conversation.");
        conversationArea.setEditable(false);
        styleJTextArea(conversationArea);
        rightPanel.add(new JScrollPane(conversationArea), BorderLayout.CENTER);
        
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBackground(BG_COLOR);
        messageField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        sendButton.setEnabled(false);
        filterUserList();
    }

    private void filterUserList() {
        String selectedRole = (String) roleFilterComboBox.getSelectedItem();
        List<User> filteredUsers;

        if (selectedRole == null || "All Roles".equals(selectedRole)) {
            filteredUsers = allChattableUsers;
        } else {
            filteredUsers = allChattableUsers.stream()
                    .filter(user -> user.getRole().equals(selectedRole))
                    .collect(Collectors.toList());
        }

        Vector<UserWithUnreadCount> usersWithCounts = new Vector<>();
        for (User user : filteredUsers) {
            int count = DataManager.getUnreadMessageCountFromSender(currentUser, user);
            usersWithCounts.add(new UserWithUnreadCount(user, count));
        }
        
        userList.setListData(usersWithCounts);
        conversationArea.setText("Select a user to view the conversation.");
        sendButton.setEnabled(false);
    }
    
    private void onUserSelected() {
        UserWithUnreadCount selectedWrapper = userList.getSelectedValue();
        if (selectedWrapper != null) {
            User selectedPartner = selectedWrapper.getUser();
            DataManager.markMessagesAsRead(currentUser, selectedPartner);
            refreshConversation();
            sendButton.setEnabled(true);
            
            // --- THE DEFINITIVE FIX FOR THE INFINITE LOOP ---
            // Instead of reloading the whole list, we just update the model for the selected item
            // and tell the list to repaint itself. This does not trigger the selection listener again.
            if (selectedWrapper.getUnreadCount() > 0) {
                selectedWrapper.markAsRead(); // Mark our local copy as read
                userList.repaint(); // Repaint the list to remove the red "(1)"
            }

        } else {
            conversationArea.setText("Select a user to view the conversation.");
            sendButton.setEnabled(false);
        }
    }


    private void sendMessage() {
        UserWithUnreadCount selectedWrapper = userList.getSelectedValue();
        if (selectedWrapper == null) return;
        User selectedPartner = selectedWrapper.getUser();

        String content = messageField.getText().trim();
        if (!content.isEmpty()) {
            if (DataManager.sendMessage(currentUser, selectedPartner, content)) {
                messageField.setText("");
                refreshConversation();
            }
        }
    }

    private void refreshConversation() {
        UserWithUnreadCount selectedWrapper = userList.getSelectedValue();
        if (selectedWrapper == null) return;
        User selectedPartner = selectedWrapper.getUser();
        
        conversationArea.setText("");
        List<String> conversation = DataManager.getConversation(currentUser, selectedPartner);
        DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

        for (String messageLine : conversation) {
            String[] parts = messageLine.split(",", 3);
            LocalDateTime timestamp = LocalDateTime.parse(parts[0], inputFormatter);
            String senderName = parts[1];
            String message = parts[2].replace(";", ",");
            
            String formattedTimestamp = timestamp.format(outputFormatter);
            senderName = senderName.equals(currentUser.getUsername()) ? "You" : selectedPartner.getFullName();
            
            conversationArea.append(String.format("[%s] %s: %s\n", formattedTimestamp, senderName, message));
        }
        conversationArea.setCaretPosition(conversationArea.getDocument().getLength());
    }

    private class UserWithCountCellRenderer extends DefaultListCellRenderer {
        
        // Define the separator border
        private final Border separatorBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof UserWithUnreadCount) {
                UserWithUnreadCount wrapper = (UserWithUnreadCount) value;
                User user = wrapper.getUser();
                int unreadCount = wrapper.getUnreadCount();

                String text = "<html><div style='padding: 5px;'>" +
                              "<b>" + user.getFullName() + "</b><br>" +
                              "<i>" + user.getRole() + "</i>";
                
                if (unreadCount > 0) {
                    text += " <font color='red'><b>(" + unreadCount + ")</b></font>";
                }
                
                text += "</div></html>";
                label.setText(text);

                label.setBorder(separatorBorder);
            }

            if (!isSelected) {
                label.setBackground(FIELD_BG_COLOR);
                label.setForeground(TEXT_COLOR);
            } else {
                label.setBackground(PRIMARY_COLOR);
                label.setForeground(Color.WHITE);
            }

            return label;
        }
    }

    private void styleJList(JList<?> list) {
        list.setBackground(FIELD_BG_COLOR);
        list.setForeground(TEXT_COLOR);
        list.setFont(new Font("Arial", Font.PLAIN, 14));
        list.setSelectionBackground(PRIMARY_COLOR);
        list.setSelectionForeground(Color.WHITE);
    }
    
    private void styleJTextArea(JTextArea textArea) {
        textArea.setBackground(BG_COLOR);
        textArea.setForeground(TEXT_COLOR);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setMargin(new Insets(10,10,10,10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
    }
}
