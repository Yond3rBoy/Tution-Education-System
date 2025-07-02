import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;

public class ChatFrame extends JFrame {
    private User currentUser;
    private User chatPartner;
    private JTextArea conversationArea;
    private JTextField messageField;

    public ChatFrame(User currentUser, User chatPartner) {
        this.currentUser = currentUser;
        this.chatPartner = chatPartner;

        setTitle("Chat with " + chatPartner.getFullName());
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Conversation Display ---
        conversationArea = new JTextArea();
        conversationArea.setEditable(false);
        conversationArea.setLineWrap(true);
        conversationArea.setWrapStyleWord(true);
        conversationArea.setMargin(new Insets(10, 10, 10, 10));
        add(new JScrollPane(conversationArea), BorderLayout.CENTER);

        // --- Message Input ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage()); // Allow sending with Enter key

        // --- Initial Load ---
        // Mark messages as read when the window is opened
        DataManager.markMessagesAsRead(currentUser, chatPartner);
        refreshConversation();
    }
    
    private void sendMessage() {
        String content = messageField.getText().trim();
        if (!content.isEmpty()) {
            if (DataManager.sendMessage(currentUser, chatPartner, content)) {
                messageField.setText("");
                refreshConversation();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to send message.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshConversation() {
        conversationArea.setText("");
        List<String> conversation = DataManager.getConversation(currentUser, chatPartner);
        DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

        for (String messageLine : conversation) {
            String[] parts = messageLine.split(",", 3);
            LocalDateTime timestamp = LocalDateTime.parse(parts[0], inputFormatter);
            String senderName = parts[1];
            String message = parts[2];
            
            String formattedTimestamp = timestamp.format(outputFormatter);
            
            // Make the current user's messages stand out
            if (senderName.equals(currentUser.getUsername())) {
                senderName = "You";
            }

            conversationArea.append(String.format("[%s] %s: %s\n", formattedTimestamp, senderName, message));
        }
    }
}
