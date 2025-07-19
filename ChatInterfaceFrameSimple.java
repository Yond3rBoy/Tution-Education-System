import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChatInterfaceFrameSimple extends JFrame {
    private User currentUser;
    private JList<User> userList;
    private JList<Message> chatList;
    private DefaultListModel<User> userListModel;
    private DefaultListModel<Message> chatListModel;
    private JTextField messageField;
    private JButton sendButton;
    private User selectedUser = null;
    private JComboBox<String> roleFilterComboBox;

    public ChatInterfaceFrameSimple(User user) {
        this.currentUser = user;
        setTitle("Chat - " + currentUser.getFullName());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Models ---
        userListModel = new DefaultListModel<>();
        chatListModel = new DefaultListModel<>();

        // --- Components ---
        userList = new JList<>(userListModel);
        chatList = new JList<>(chatListModel);
        chatList.setCellRenderer(new ChatBubbleCellRenderer(currentUser.getUsername()));

        messageField = new JTextField();
        sendButton = new JButton("Send");

        String[] filterRoles = DataManager.getChatFilterRolesForUser(currentUser);
        roleFilterComboBox = new JComboBox<>(filterRoles);

        // --- Layout ---
        // This panel correctly holds the filter dropdown and the user list below it.
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.add(new JLabel("Filter by Role:"), BorderLayout.NORTH);
        filterPanel.add(roleFilterComboBox, BorderLayout.CENTER);
        
        leftPanel.add(filterPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

        // --- FIX #2: Use the 'leftPanel' here, not a new JScrollPane ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, createChatPanel());
        splitPane.setDividerLocation(200);
        add(splitPane);

        // --- Listeners ---
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userList.getSelectedValue() != null) {
                selectedUser = userList.getSelectedValue();
                loadConversation();
            }
        });

        sendButton.addActionListener(e -> sendMessage());
        // --- FIX #1: Add the listener to the message field for the Enter key ---
        messageField.addActionListener(e -> sendMessage());

        roleFilterComboBox.addActionListener(e -> updateUserList());

        updateUserList(); // Initial load
    }

    private void updateUserList() {
        String selectedRole = (String) roleFilterComboBox.getSelectedItem();
        if (selectedRole == null) return;

        userListModel.clear();
        List<User> allUsers = DataManager.getUsersForChat(currentUser);

        if ("All Roles".equals(selectedRole) || "Groups".equals(selectedRole)) {
            allUsers.forEach(userListModel::addElement);
        } else {
            allUsers.stream()
                .filter(u -> u.getRole().equals(selectedRole))
                .forEach(userListModel::addElement);
        }
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane chatScrollPane = new JScrollPane(chatList);
        chatScrollPane.setBorder(null);
        panel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadConversation() {
        if (selectedUser == null) return;

        chatListModel.clear();
        List<Message> conversation = DataManager.getConversation(currentUser.getUsername(), selectedUser.getUsername());
        conversation.forEach(chatListModel::addElement);

        if (!conversation.isEmpty()) {
            chatList.ensureIndexIsVisible(chatListModel.getSize() - 1);
        }

        DataManager.markMessagesAsRead(currentUser, selectedUser);
    }

    private void sendMessage() {
        String content = messageField.getText().trim();
        if (content.isEmpty() || selectedUser == null) {
            return;
        }

        DataManager.sendMessage(currentUser.getUsername(), selectedUser.getUsername(), content);
        messageField.setText("");
        loadConversation();
    }

    // --- Inner class for rendering chat bubbles (no changes needed here) ---
    private class ChatBubbleCellRenderer extends JPanel implements ListCellRenderer<Message> {
        private String currentUserUsername;
        private JTextArea messageArea = new JTextArea();
        private JLabel timestampLabel = new JLabel();
        private JPanel bubblePanel = new JPanel();
        private JPanel wrapperPanel = new JPanel(new FlowLayout());

        ChatBubbleCellRenderer(String currentUserUsername) {
            this.currentUserUsername = currentUserUsername;
            setLayout(new BorderLayout());
            
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setEditable(false);
            messageArea.setBorder(new EmptyBorder(5, 8, 5, 8));
            
            timestampLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            timestampLabel.setForeground(Color.GRAY);
            
            bubblePanel.setLayout(new BorderLayout());
            bubblePanel.add(messageArea, BorderLayout.CENTER);
            bubblePanel.add(timestampLabel, BorderLayout.SOUTH);
            
            wrapperPanel.add(bubblePanel);
            add(wrapperPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Message> list, Message message, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            
            messageArea.setText(message.getContent());
            timestampLabel.setText(message.getTimestamp().format(DateTimeFormatter.ofPattern("MMM dd, h:mm a")));
            
            boolean isOutgoing = message.getSenderUsername().equals(currentUserUsername);

            if (isOutgoing) {
                wrapperPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                bubblePanel.setBackground(new Color(220, 248, 198));
            } else {
                wrapperPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                bubblePanel.setBackground(Color.WHITE);
            }
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }
            
            return this;
        }
    }
}