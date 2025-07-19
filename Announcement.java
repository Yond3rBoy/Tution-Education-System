public class Announcement {
    private final String id;
    private final String title;
    private final String content;
    private final String author;
    private final String date;

    public Announcement(String id, String title, String content, String author, String date) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.date = date;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public String getDate() { return date; }

    @Override
    public String toString() {
        // This is how it will appear in a list.
        // The <html> tags allow for basic formatting like bold and line breaks.
        return "<html><b>" + title + "</b><br><i>by " + author + " on " + date + "</i></html>";
    }
}