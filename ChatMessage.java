import java.time.LocalDateTime;

public class ChatMessage {
    private final String senderUsername;
    private final String content;
    private final LocalDateTime timestamp;

    public ChatMessage(String senderUsername, String content, LocalDateTime timestamp) {
        this.senderUsername = senderUsername;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSenderUsername() { return senderUsername; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
}