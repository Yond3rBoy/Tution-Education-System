public class UserWithUnreadCount {
    private final User user;
    private int unreadCount; // <-- REMOVED 'final' to allow changes

    public UserWithUnreadCount(User user, int unreadCount) {
        this.user = user;
        this.unreadCount = unreadCount;
    }

    public User getUser() {
        return user;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    // --- NEW METHOD ---
    // This method allows us to update the object's state in memory after messages are read.
    public void markAsRead() {
        this.unreadCount = 0;
    }

    @Override
    public String toString() {
        return user.getFullName();
    }
}