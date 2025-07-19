import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChatBubbleCellRenderer implements ListCellRenderer<ChatMessage> {
    private static final ImageIcon STUDENT_ICON = loadStudentIcon();
    private final String currentUsername;

    public ChatBubbleCellRenderer(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ChatMessage> list, ChatMessage message, int index, boolean isSelected, boolean cellHasFocus) {
        boolean isCurrentUser = message.getSenderUsername().equals(currentUsername);

        // Main container panel that controls left/right alignment
        JPanel wrapper = new JPanel(new FlowLayout(isCurrentUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 5));
        wrapper.setBackground(list.getBackground());

        // The chat bubble itself
        RoundedPanel bubble = new RoundedPanel(15, isCurrentUser ? new Color(0, 132, 255) : new Color(60, 60, 60));
        bubble.setLayout(new BorderLayout(5, 3));
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12));

        // The text content of the message
        JTextArea messageText = new JTextArea(message.getContent());
        messageText.setWrapStyleWord(true);
        messageText.setLineWrap(true);
        messageText.setEditable(false);
        messageText.setOpaque(false);
        messageText.setForeground(Color.WHITE);
        messageText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // The icon label (only visible for students)
        JLabel iconLabel = new JLabel();
        User sender = DataManager.findUserByUsername(message.getSenderUsername());
        if (sender != null && "Student".equals(sender.getRole())) {
            iconLabel.setIcon(STUDENT_ICON);
        }

        // Arrange components based on who sent the message
        if (isCurrentUser) {
            bubble.add(messageText, BorderLayout.CENTER);
        } else {
            bubble.add(iconLabel, BorderLayout.WEST);
            bubble.add(messageText, BorderLayout.CENTER);
        }

        // Add the bubble to the wrapper
        wrapper.add(bubble);
        return wrapper;
    }

    private static ImageIcon loadStudentIcon() {
        try {
            ImageIcon icon = new ImageIcon(ChatBubbleCellRenderer.class.getResource("/assets/student_icon.png"));
            Image resized = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            return new ImageIcon(resized);
        } catch (Exception e) {
            System.err.println("Could not load student_icon.png");
            return null;
        }
    }
}