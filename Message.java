import java.time.LocalDateTime;

public class Message {
    private String senderUsername;
    private String recipientUsername;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;

    public Message(String senderUsername, String recipientUsername, String content, LocalDateTime timestamp, boolean isRead) {
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    // Getters
    public String getSenderUsername() { return senderUsername; }
    public String getRecipientUsername() { return recipientUsername; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
}