import java.util.Objects;

public class User {
    private String id;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private String specialization;

    public User(String id, String username, String password, String role, String fullName, String specialization) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.specialization = specialization;
    }

    // Getters and Setters are unchanged...
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getSpecialization() { return specialization; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPassword(String password) { this.password = password; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String toCsvString() {
        if ("Tutor".equalsIgnoreCase(this.role)) {
            return String.join(",", id, username, password, fullName, specialization);
        } else {
            return String.join(",", id, username, password, fullName);
        }
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }

    // --- NEW AND IMPORTANT: Add these two methods ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}