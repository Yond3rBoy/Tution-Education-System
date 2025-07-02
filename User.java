public class User {
    private String id;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private String specialization; // Added for Tutors

    // Constructor for all roles
    public User(String id, String username, String password, String role, String fullName, String specialization) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.specialization = specialization;
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getSpecialization() { return specialization; }
    
    // Setters
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPassword(String password) { this.password = password; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    /**
     * CORRECTED: Converts the User object back to the correct CSV string format
     * for its specific role file.
     */
    public String toCsvString() {
        if ("Tutor".equalsIgnoreCase(this.role)) {
            // Tutors have an extra specialization field
            return String.join(",", id, username, password, fullName, specialization);
        } else {
            // Admins, Receptionists, Students have a standard format
            return String.join(",", id, username, password, fullName);
        }
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}