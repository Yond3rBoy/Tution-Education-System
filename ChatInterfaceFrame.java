import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class ChatInterfaceFrame extends JFrame {
    private User currentUser;
    private Runnable onMessageReadCallback;
    
    private JList<Object> contactList;
    private DefaultListModel<Object> contactListModel;
    
    private JPanel chatPanel;
    private JTextPane messageInput;
    private JButton sendButton;
    private JLabel chatPartnerNameLabel;
    private JButton btnNewGroup;
    private JButton btnManageMembers;

    private Object selectedContact;

    private JScrollPane chatScrollPane;
    private JComboBox<String> roleFilterComboBox;

    private Map<String, Integer> unreadCounts = new HashMap<>();
    private javax.swing.Timer refreshTimer;

    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Color LIST_BG_COLOR = Color.WHITE;
    private static final Color SELECTED_LIST_ITEM_BG = new Color(230, 230, 230);
    private static final Color OUTGOING_BUBBLE_BG = new Color(147, 231, 147);
    private static final Color INCOMING_BUBBLE_BG = Color.WHITE;
    private static final Color TEXT_COLOR = Color.BLACK;
    private static final Font USER_NAME_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font MESSAGE_FONT = new Font("Arial", Font.PLAIN, 13);
    private static final Font TIMESTAMP_FONT = new Font("Arial", Font.PLAIN, 10);

    public ChatInterfaceFrame(User user, Runnable onMessageReadCallback) {
        this.currentUser = user;
        this.onMessageReadCallback = onMessageReadCallback;
        setTitle("The Learning Hub - Chat");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        initUnreadCounts();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createConversationListPanel(),
                createChatViewPanel());
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        refreshTimer = new javax.swing.Timer(5000, e -> {
            refreshUnreadCounts();
            if (selectedContact != null) {
                loadConversation(false);
            }
        });
        refreshTimer.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (refreshTimer.isRunning()) {
                    refreshTimer.stop();
                }
            }
        });
        updateUserList();
    }

    private void initUnreadCounts() {
        this.unreadCounts = DataManager.getAllUnreadMessageCounts(currentUser);
    }

    private void refreshUnreadCounts() {
        this.unreadCounts = DataManager.getAllUnreadMessageCounts(currentUser);
        contactList.repaint();
    }

    private JPanel createConversationListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIST_BG_COLOR);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JPanel topControlsPanel = new JPanel(new BorderLayout(5, 5));
        topControlsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topControlsPanel.setBackground(LIST_BG_COLOR);
        
        String[] filterRoles = DataManager.getChatFilterRolesForUser(currentUser);
        roleFilterComboBox = new JComboBox<>(filterRoles);
        
        topControlsPanel.add(new JLabel("Filter by:"), BorderLayout.NORTH);
        topControlsPanel.add(roleFilterComboBox, BorderLayout.CENTER);
        
        panel.add(topControlsPanel, BorderLayout.NORTH);

        contactListModel = new DefaultListModel<>();
        contactList = new JList<>(contactListModel);
        contactList.setCellRenderer(new ContactListRenderer());
        contactList.setSelectionBackground(SELECTED_LIST_ITEM_BG);
        contactList.setFixedCellHeight(60);
        contactList.setBackground(LIST_BG_COLOR);
        contactList.setBorder(new EmptyBorder(0, 10, 0, 10));

        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && contactList.getSelectedValue() != null) {
                selectedContact = contactList.getSelectedValue();
                
                if (selectedContact instanceof User) {
                    User u = (User) selectedContact;
                    chatPartnerNameLabel.setText(u.getFullName() + " (" + u.getRole() + ")");
                } else if (selectedContact instanceof GroupChat) {
                    GroupChat g = (GroupChat) selectedContact;
                    chatPartnerNameLabel.setText(g.getGroupName() + " (Group)");
                }

                updateManageMembersButton();
                loadConversation(true);

                if (selectedContact instanceof User) {
                    DataManager.markMessagesAsRead(currentUser, (User) selectedContact);
                    refreshUnreadCounts();
                }
            }
        });
        
        roleFilterComboBox.addActionListener(e -> updateUserList());
        panel.add(new JScrollPane(contactList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 1));
        bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        String userRole = currentUser.getRole();
        if (userRole.equals("Student") || userRole.equals("Tutor")) {
            btnNewGroup = new JButton("New Group Chat");
            btnNewGroup.addActionListener(e -> showCreateGroupDialog());
            bottomPanel.add(btnNewGroup);
        }
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void updateUserList() {
        String selectedFilter = (String) roleFilterComboBox.getSelectedItem();
        if (selectedFilter == null) return;
        
        List<Object> contactsToSort = new ArrayList<>();

        if ("All Chats".equals(selectedFilter)) {

            List<User> users = DataManager.getUsersForChat(currentUser);
            users.stream()
                .filter(u -> !u.getUsername().equals(currentUser.getUsername()))
                .distinct()
                .forEach(contactsToSort::add);

            contactsToSort.addAll(DataManager.getGroupChatsForUser(currentUser));

        } else if ("Groups".equals(selectedFilter)) {

            contactsToSort.addAll(DataManager.getGroupChatsForUser(currentUser));

        } else {

            List<User> users = DataManager.getAllUsersByRole(selectedFilter);
            users.stream()
                .filter(u -> !u.getUsername().equals(currentUser.getUsername()))
                .distinct()
                .forEach(contactsToSort::add);
        }
        
        // --- The sorting and list model update logic below remains the same ---
        contactsToSort.sort((o1, o2) -> {
            boolean o1_hasUnread = false;
            Message o1_lastMessage = null;
            if (o1 instanceof User) {
                User user1 = (User) o1;
                o1_hasUnread = unreadCounts.getOrDefault(user1.getUsername(), 0) > 0;
                o1_lastMessage = DataManager.getLastMessage(currentUser.getUsername(), user1.getUsername());
            } else if (o1 instanceof GroupChat) {
                o1_lastMessage = DataManager.getLastGroupMessage(((GroupChat) o1).getGroupId());
            }
            
            boolean o2_hasUnread = false;
            Message o2_lastMessage = null;
            if (o2 instanceof User) {
                User user2 = (User) o2;
                o2_hasUnread = unreadCounts.getOrDefault(user2.getUsername(), 0) > 0;
                o2_lastMessage = DataManager.getLastMessage(currentUser.getUsername(), user2.getUsername());
            } else if (o2 instanceof GroupChat) {
                o2_lastMessage = DataManager.getLastGroupMessage(((GroupChat) o2).getGroupId());
            }
            
            if (o1_hasUnread && !o2_hasUnread) return -1;
            if (!o1_hasUnread && o2_hasUnread) return 1;
            
            LocalDateTime time1 = o1_lastMessage != null ? o1_lastMessage.getTimestamp() : null;
            LocalDateTime time2 = o2_lastMessage != null ? o2_lastMessage.getTimestamp() : null;
            
            if (time1 != null && time2 != null) {
                return time2.compareTo(time1);
            }
            if (time1 == null && time2 != null) return 1;
            if (time1 != null && time2 == null) return -1;
            
            return 0;
        });

        Object previouslySelected = contactList.getSelectedValue();
        contactListModel.clear();
        contactsToSort.forEach(contactListModel::addElement);
        
        if (previouslySelected != null) {
            contactList.setSelectedValue(previouslySelected, true);
        }
    }

    private JPanel createChatViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                new EmptyBorder(10, 15, 10, 15)
        ));
        chatPartnerNameLabel = new JLabel("Select a conversation to start");
        chatPartnerNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        header.add(chatPartnerNameLabel, BorderLayout.CENTER);

        btnManageMembers = new JButton("Manage");
        btnManageMembers.addActionListener(e -> showManageMembersDialog());
        btnManageMembers.setVisible(false);
        header.add(btnManageMembers, BorderLayout.EAST);
        
        panel.add(header, BorderLayout.NORTH);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(BG_COLOR);
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            new EmptyBorder(10, 15, 10, 15)
        ));
        messageInput = new JTextPane();
        messageInput.setFont(MESSAGE_FONT);
        JScrollPane inputScrollPane = new JScrollPane(messageInput);
        inputScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        getRootPane().setDefaultButton(sendButton);

        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateManageMembersButton() {
        if (selectedContact instanceof GroupChat) {
            String groupId = ((GroupChat) selectedContact).getGroupId();
            String creator = DataManager.getGroupChatCreator(groupId);

            boolean isCreator = currentUser.getUsername().equals(creator);
            btnManageMembers.setVisible(isCreator);
            
        } else {
            btnManageMembers.setVisible(false);
        }
    }
    
    private void loadConversation(boolean jumpToBottom) {
        chatPanel.removeAll();
        List<Message> messages = new ArrayList<>();

        if (selectedContact instanceof User) {
            messages = DataManager.getConversation(currentUser.getUsername(), ((User) selectedContact).getUsername());
        } else if (selectedContact instanceof GroupChat) {
            messages = DataManager.getGroupConversation(((GroupChat) selectedContact).getGroupId());
        }

        for (Message msg : messages) {
            chatPanel.add(createMessageBubble(msg));
            chatPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        chatPanel.revalidate();
        chatPanel.repaint();

        if (jumpToBottom) {
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        }
    }

    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty() || selectedContact == null) return;
        
        if (selectedContact instanceof User) {
            DataManager.sendMessage(currentUser.getUsername(), ((User) selectedContact).getUsername(), text);
        } else if (selectedContact instanceof GroupChat) {
            DataManager.sendGroupMessage(currentUser.getUsername(), ((GroupChat) selectedContact).getGroupId(), text);
        }
        
        messageInput.setText("");
        loadConversation(true);
        updateUserList();
    }

    private JPanel createMessageBubble(Message message) {
        boolean isOutgoing = message.getSenderUsername().equals(currentUser.getUsername());

        JPanel container = new JPanel(new FlowLayout(isOutgoing ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        container.setBackground(BG_COLOR);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);

        Color bubbleColor = isOutgoing ? OUTGOING_BUBBLE_BG : INCOMING_BUBBLE_BG;
        JPanel bubble = new RoundedPanel(15, bubbleColor);
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(new EmptyBorder(5, 10, 5, 10));

        JTextArea messageText = new JTextArea(message.getContent());
        messageText.setLineWrap(true);
        messageText.setWrapStyleWord(true);
        messageText.setEditable(false);
        messageText.setBackground(bubbleColor);
        messageText.setFont(MESSAGE_FONT);
        messageText.setForeground(TEXT_COLOR);
        messageText.setFocusable(false);
        
        if (!isOutgoing && selectedContact instanceof GroupChat) {
            User sender = DataManager.findUserByUsername(message.getSenderUsername());
            String senderName = sender != null ? sender.getFullName() : message.getSenderUsername();
            JLabel senderLabel = new JLabel(senderName);
            senderLabel.setFont(new Font("Arial", Font.BOLD, 12));
            senderLabel.setForeground(new Color(67, 102, 163));
            bubble.add(senderLabel, BorderLayout.NORTH);
        }
        
        bubble.add(messageText, BorderLayout.CENTER);
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        JLabel timestampLabel = new JLabel(message.getTimestamp().format(timeFormatter));
        timestampLabel.setFont(TIMESTAMP_FONT);
        timestampLabel.setForeground(Color.GRAY);
        timestampLabel.setBorder(new EmptyBorder(2, 10, 0, 10));

        if (isOutgoing) {
            contentPanel.add(bubble);
            contentPanel.add(timestampLabel);
            timestampLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        } else {
            contentPanel.add(bubble);
            contentPanel.add(timestampLabel);
            timestampLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        container.add(contentPanel);
        return container;
    }

    private void showCreateGroupDialog() {
        JTextField groupNameField = new JTextField();
        List<User> potentialMembers = DataManager.getAllUsersByRole("Student");
        potentialMembers.addAll(DataManager.getAllUsersByRole("Tutor"));
        potentialMembers = potentialMembers.stream()
            .filter(u -> !u.getUsername().equals(currentUser.getUsername()))
            .collect(Collectors.toList());

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Group Name:"), BorderLayout.NORTH);
        panel.add(groupNameField, BorderLayout.CENTER);

        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        Map<JCheckBox, User> checkboxMap = new LinkedHashMap<>();
        for (User user : potentialMembers) {
            JCheckBox checkBox = new JCheckBox(user.getFullName() + " (" + user.getRole() + ")");
            checkboxMap.put(checkBox, user);
            checkboxPanel.add(checkBox);
        }

        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        
        JPanel finalPanel = new JPanel(new BorderLayout(10, 10));
        finalPanel.add(panel, BorderLayout.NORTH);
        finalPanel.add(scrollPane, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, finalPanel, "Create New Group Chat", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String groupName = groupNameField.getText().trim();
            if (groupName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Group name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Set<User> selectedMembers = checkboxMap.entrySet().stream()
                .filter(entry -> entry.getKey().isSelected())
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
            
            if (DataManager.createGroupChat(groupName, currentUser, selectedMembers)) {
                JOptionPane.showMessageDialog(this, "Group created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                updateUserList();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create group.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showManageMembersDialog() {
        if (!(selectedContact instanceof GroupChat)) return;
        GroupChat selectedGroup = (GroupChat) selectedContact;

        List<User> potentialMembers = DataManager.getAllUsersByRole("Student");
        potentialMembers.addAll(DataManager.getAllUsersByRole("Tutor"));
        potentialMembers = potentialMembers.stream()
            .filter(u -> !u.getUsername().equals(currentUser.getUsername()))
            .collect(Collectors.toList());
            
        Set<String> currentMemberUsernames = DataManager.getGroupChatMembers(selectedGroup.getGroupId())
                                            .stream().map(User::getUsername)
                                            .collect(Collectors.toSet());

        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        Map<JCheckBox, User> checkboxMap = new LinkedHashMap<>();
        for (User user : potentialMembers) {
            JCheckBox checkBox = new JCheckBox(user.getFullName() + " (" + user.getRole() + ")");
            if (currentMemberUsernames.contains(user.getUsername())) {
                checkBox.setSelected(true);
            }
            checkboxMap.put(checkBox, user);
            checkboxPanel.add(checkBox);
        }
        
        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        
        int result = JOptionPane.showConfirmDialog(this, scrollPane, "Manage Members for: " + selectedGroup.getGroupName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Set<User> newMembers = checkboxMap.entrySet().stream()
                .filter(entry -> entry.getKey().isSelected())
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
            newMembers.add(currentUser); 
            
            if (DataManager.updateGroupChatMembers(selectedGroup.getGroupId(), newMembers)) {
                JOptionPane.showMessageDialog(this, "Group members updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update members.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class ContactListRenderer extends JPanel implements ListCellRenderer<Object> {
        private JLabel avatarLabel;
        private JLabel nameLabel;
        private JLabel lastMessageLabel;

        ContactListRenderer() {
            setLayout(new BorderLayout(10, 2));
            setBorder(new EmptyBorder(5, 5, 5, 5));
            
            avatarLabel = new JLabel();
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            avatarLabel.setVerticalAlignment(SwingConstants.CENTER);
            avatarLabel.setOpaque(true);
            avatarLabel.setForeground(Color.WHITE);
            avatarLabel.setFont(new Font("Arial", Font.BOLD, 20));
            avatarLabel.setPreferredSize(new Dimension(40, 40));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            
            nameLabel = new JLabel();
            nameLabel.setFont(USER_NAME_FONT);

            lastMessageLabel = new JLabel();
            lastMessageLabel.setFont(MESSAGE_FONT);
            lastMessageLabel.setForeground(Color.GRAY);

            textPanel.add(nameLabel);
            textPanel.add(lastMessageLabel);

            add(avatarLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            
            if (value instanceof User) {
                User user = (User) value;
                String nameText = user.getFullName();
                int unreadCount = unreadCounts.getOrDefault(user.getUsername(), 0);
                if (unreadCount > 0) {
                    nameLabel.setText(nameText + " (" + unreadCount + ")");
                    nameLabel.setForeground(Color.RED);
                } else {
                    nameLabel.setText(nameText);
                    nameLabel.setForeground(TEXT_COLOR);
                }
                
                Message lastMessage = DataManager.getLastMessage(currentUser.getUsername(), user.getUsername());
                lastMessageLabel.setText(lastMessage != null ? lastMessage.getContent() : "No messages yet.");
                
                avatarLabel.setText(String.valueOf(user.getFullName().charAt(0)));
                int colorIndex = Math.abs(user.getUsername().hashCode()) % 5;
                Color[] colors = {new Color(255, 120, 120), new Color(120, 180, 255), new Color(120, 220, 120), new Color(255, 180, 120), new Color(180, 120, 255)};
                avatarLabel.setBackground(colors[colorIndex]);

            } else if (value instanceof GroupChat) {
                GroupChat group = (GroupChat) value;
                nameLabel.setText(group.getGroupName());
                nameLabel.setForeground(TEXT_COLOR);
                
                Message lastMessage = DataManager.getLastGroupMessage(group.getGroupId());
                lastMessageLabel.setText(lastMessage != null ? lastMessage.getContent() : "No messages yet.");
                
                avatarLabel.setText("G");
                avatarLabel.setBackground(new Color(150, 150, 150));
            }
            
            if (isSelected) {
                setBackground(SELECTED_LIST_ITEM_BG);
            } else {
                setBackground(LIST_BG_COLOR);
            }
            setOpaque(true);
            return this;
        }
    }

}