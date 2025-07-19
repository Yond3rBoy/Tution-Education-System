import java.time.LocalDate;

public class Feedback {
    private String id;
    private String submitterId;
    private String targetRole;
    private String targetId;
    private String subject;
    private int rating;
    private String content;
    private LocalDate date;
    private String status;

    public Feedback(String id, String submitterId, String targetRole, String targetId, String subject, int rating, String content, LocalDate date, String status) {
        this.id = id;
        this.submitterId = submitterId;
        this.targetRole = targetRole;
        this.targetId = targetId;
        this.subject = subject;
        this.rating = rating;
        this.content = content;
        this.date = date;
        this.status = status;
    }

    public String getId() { return id; }
    public String getSubmitterId() { return submitterId; }
    public String getTargetRole() { return targetRole; }
    public String getTargetId() { return targetId; }
    public String getSubject() { return subject; }
    public int getRating() { return rating; }
    public String getContent() { return content; }
    public LocalDate getDate() { return date; }
    public String getStatus() { return status; }
}