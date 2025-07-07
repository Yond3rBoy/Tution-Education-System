import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {

    // --- FILE CONSTANTS ---
    // User files
    private static final String ADMINS_FILE = "admins.txt";
    private static final String TUTORS_FILE = "tutors.txt";
    private static final String RECEPTIONISTS_FILE = "receptionists.txt";
    private static final String STUDENTS_FILE = "students.txt";
    
    // Data files
    private static final String STUDENT_DETAILS_FILE = "student_details.txt";
    private static final String COURSES_FILE = "courses.txt";
    private static final String ENROLLMENTS_FILE = "enrollments.txt";
    private static final String PAYMENTS_FILE = "payments.txt";
    private static final String REQUESTS_FILE = "requests.txt";
    private static final String CHATS_FILE = "chats.txt";
    private static final String RESULTS_FILE = "results.txt";
    private static final String ANNOUNCEMENTS_FILE = "announcements.txt";
    private static final String READ_ANNOUNCEMENTS_FILE = "read_announcements.txt";



    // --- CORE USER MANAGEMENT ---

    public static User authenticateUser(String username, String password) {
        User user = findUserInFile(ADMINS_FILE, "Admin", username, password);
        if (user != null) return user;
        user = findUserInFile(TUTORS_FILE, "Tutor", username, password);
        if (user != null) return user;
        user = findUserInFile(RECEPTIONISTS_FILE, "Receptionist", username, password);
        if (user != null) return user;
        user = findUserInFile(STUDENTS_FILE, "Student", username, password);
        if (user != null) return user;
        return null; // User not found
    }
    
    private static User findUserInFile(String filePath, String role, String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 5);
                if (data.length >= 4 && data[1].equals(username) && data[2].equals(password)) {
                    String id = data[0];
                    String fullName = data[3];
                    String specialization = (role.equals("Tutor") && data.length > 4) ? data[4] : "";
                    return new User(id, username, password, role, fullName, specialization);
                }
            }
        } catch (IOException e) { /* Ignore */ }
        return null;
    }

    public static List<User> getAllUsersByRole(String role) {
        String filePath = getFilePathForRole(role);
        if (filePath == null) return new ArrayList<>();
        return readUsersFromFile(filePath, role);
    }
    
    private static List<User> readUsersFromFile(String filePath, String role) {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 5);
                if (data.length >= 4) {
                    String specialization = (role.equals("Tutor") && data.length > 4) ? data[4] : "";
                    users.add(new User(data[0], data[1], data[2], role, data[3], specialization));
                }
            }
        } catch (IOException e) { /* Ignore */ }
        return users;
    }
    
    public static boolean updateUser(User userToUpdate) {
        String filePath = getFilePathForRole(userToUpdate.getRole());
        if (filePath == null) return false;

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            boolean updated = false;
            for (int i = 0; i < lines.size(); i++) {
                String[] data = lines.get(i).split(",", 2);
                if (data.length > 0 && data[0].equals(userToUpdate.getId())) {
                    lines.set(i, userToUpdate.toCsvString());
                    updated = true;
                    break;
                }
            }
            if (updated) {
                Files.write(Paths.get(filePath), lines);
            }
            return updated;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- ADMIN-SPECIFIC FUNCTIONS ---

    public static boolean registerUser(String username, String password, String role, String fullName, String specialization) {
        try {
            String newUserId = getNextIdForRole(role);
            if (newUserId == null) return false;

            String filePath = getFilePathForRole(role);
            String newUserLine;

            // The core data string is created first
            if ("Tutor".equalsIgnoreCase(role)) {
                newUserLine = String.join(",", newUserId, username, password, fullName, specialization);
            } else {
                newUserLine = String.join(",", newUserId, username, password, fullName);
            }

            // We append the system's line separator to the data string
            // This is more reliable than just "\n"
            String lineToWrite = newUserLine + System.lineSeparator();

            // Write the full line (with the newline) to the file
            Files.write(Paths.get(filePath), lineToWrite.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteUser(String username) {
        boolean deleted = false;
        if (deleteLineByColumnValue(ADMINS_FILE, username, 1)) deleted = true;
        if (deleteLineByColumnValue(TUTORS_FILE, username, 1)) deleted = true;
        if (deleteLineByColumnValue(RECEPTIONISTS_FILE, username, 1)) deleted = true;
        if (deleteLineByColumnValue(STUDENTS_FILE, username, 1)) deleted = true;
        return deleted;
    }
    
    public static String generateIncomeReport(int month, int year) {
        Map<String, Map<String, Double>> incomeByLevelAndSubject = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            if (!Files.exists(Paths.get(PAYMENTS_FILE))) return "No payment data found.";
            List<String> payments = Files.readAllLines(Paths.get(PAYMENTS_FILE));
            for (String paymentLine : payments) {
                String[] pData = paymentLine.split(",");
                if (pData.length < 4) continue;
                LocalDate paymentDate = LocalDate.parse(pData[3], formatter);
                if (paymentDate.getMonthValue() == month && paymentDate.getYear() == year) {
                    String enrollmentId = pData[1];
                    double amount = Double.parseDouble(pData[2]);
                    String courseId = findCourseIdForEnrollment(enrollmentId);
                    if (courseId != null) {
                        String[] courseDetails = findCourseDetails(courseId);
                        if (courseDetails != null) {
                            String level = courseDetails[0];
                            String subject = courseDetails[1];
                            incomeByLevelAndSubject.computeIfAbsent(level, k -> new HashMap<>()).merge(subject, amount, Double::sum);
                        }
                    }
                }
            }
        } catch (IOException | java.time.format.DateTimeParseException e) {
            return "Error reading data files or parsing date: " + e.getMessage();
        }
        StringBuilder report = new StringBuilder("Monthly Income Report for " + month + "/" + year + "\n");
        report.append("--------------------------------------------------\n");
        if (incomeByLevelAndSubject.isEmpty()) {
            report.append("No income recorded for this period.\n");
        } else {
            double totalIncome = 0;
            for (Map.Entry<String, Map<String, Double>> levelEntry : incomeByLevelAndSubject.entrySet()) {
                report.append("Level: ").append(levelEntry.getKey()).append("\n");
                for (Map.Entry<String, Double> subjectEntry : levelEntry.getValue().entrySet()) {
                    report.append(String.format("  - Subject: %-15s | Income: $%.2f\n", subjectEntry.getKey(), subjectEntry.getValue()));
                    totalIncome += subjectEntry.getValue();
                }
            }
            report.append("--------------------------------------------------\n");
            report.append(String.format("Total Monthly Income: $%.2f\n", totalIncome));
        }
        return report.toString();
    }
    
    // --- RECEPTIONIST-SPECIFIC FUNCTIONS ---

    // In DataManager.java
    public static List<String> getAvailableCourses() {
        List<String> courses = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(COURSES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // CHANGE THIS:
                String[] data = line.split(",", 7); // Was split(",", 6)
                // CHANGE THIS:
                if (data.length >= 7) { // Check for 7 columns now
                    // This line is now safe because the split is correct
                    String courseInfo = String.format("%s: %s (%s) - $%.2f", data[0], data[1], data[3], Double.parseDouble(data[5]));
                    courses.add(courseInfo);
                }
            }
        } catch (IOException e) { /* ignore */ }
        return courses;
    }

    public static boolean registerStudent(String fullName, String username, String password, String ic, String email, String contact, String address, List<String> courseIDs) {
        String studentId = getNextIdForRole("Student");
        String userLine = String.join(",", studentId, username, password, fullName);
        try {
            Files.write(Paths.get(STUDENTS_FILE), (userLine + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        String detailsLine = String.join(",", studentId, ic, email, contact, address);
        try {
            Files.write(Paths.get(STUDENT_DETAILS_FILE), (detailsLine + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        for (String courseId : courseIDs) {
            String enrollmentId = getNextIdForPrefix("ENR-", ENROLLMENTS_FILE);
            String enrollmentLine = String.join(",", enrollmentId, studentId, courseId);
            try {
                Files.write(Paths.get(ENROLLMENTS_FILE), (enrollmentLine + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static Map<String, String> getStudentEnrollments(String studentId) {
        Map<String, String> enrollments = new HashMap<>();
        try {
            if (!Files.exists(Paths.get(ENROLLMENTS_FILE))) return enrollments;
            List<String> enrollmentLines = Files.readAllLines(Paths.get(ENROLLMENTS_FILE));
            for (String line : enrollmentLines) {
                String[] data = line.split(",");
                if (data.length == 3 && data[1].equals(studentId)) {
                    String enrollmentId = data[0];
                    String courseId = data[2];
                    String courseInfo = getCourseInfoById(courseId);
                    if (courseInfo != null) {
                        enrollments.put(enrollmentId, courseInfo);
                    }
                }
            }
        } catch (IOException e) { /* ignore */ }
        return enrollments;
    }
    
    public static String acceptPayment(String enrollmentId, double amount) {
        String paymentId = getNextIdForPrefix("PAY-", PAYMENTS_FILE);
        String paymentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String paymentLine = String.join(",", paymentId, enrollmentId, String.valueOf(amount), paymentDate) + "\n";
        
        try {
            Files.write(Paths.get(PAYMENTS_FILE), paymentLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            return "--- RECEIPT ---\n" +
                   "Payment ID: " + paymentId + "\n" +
                   "Date: " + paymentDate + "\n" +
                   "Enrollment ID: " + enrollmentId + "\n" +
                   "Amount Paid: $" + String.format("%.2f", amount) + "\n" +
                   "----------------\n" +
                   "Thank you!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to record payment.";
        }
    }
    
    public static boolean updateStudentEnrollments(String studentId, List<String> newCourseIDs) {
        deleteLineByColumnValue(ENROLLMENTS_FILE, studentId, 1);

        for (String courseId : newCourseIDs) {
            String enrollmentId = getNextIdForPrefix("ENR-", ENROLLMENTS_FILE);
            String enrollmentLine = String.join(",", enrollmentId, studentId, courseId);
            try {
                Files.write(Paths.get(ENROLLMENTS_FILE), (enrollmentLine + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    public static boolean deleteStudent(String studentId) {
        boolean success = true;
        if (!deleteLineByColumnValue(STUDENTS_FILE, studentId, 0)) success = false;
        if (!deleteLineByColumnValue(STUDENT_DETAILS_FILE, studentId, 0)) success = false;
        if (!deleteLineByColumnValue(ENROLLMENTS_FILE, studentId, 1)) success = false;
        return success;
    }

    // --- TUTOR SPECIFIC FUNCTION ---

     public static List<String[]> getCoursesByTutor(String tutorId) {
        List<String[]> courses = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(COURSES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 7);
                // Check if the tutorID column (index 2) matches
                if (data.length >= 7 && data[2].equals(tutorId)) {
                    courses.add(data);
                }
            }
        } catch (IOException e) { /* ignore */ }
        return courses;
    }
    
    public static boolean addCourse(String courseName, String tutorId, String level, String subject, double fee, String schedule) {
        try {
            String courseId = getNextIdForPrefix("C-", COURSES_FILE, 101);
            
            // CHANGE THIS:
            // String courseLine = String.join(",", courseId, courseName, tutorId, level, subject, String.format("%.2f", fee)) + "\n";
            
            // TO THIS (add the schedule to the line being saved):
            String courseLine = String.join(",", courseId, courseName, tutorId, level, subject, String.format("%.2f", fee), schedule) + "\n";
            
            Files.write(Paths.get(COURSES_FILE), courseLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateCourse(String courseId, String courseName, String level, String subject, double fee, String schedule) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(COURSES_FILE));
            boolean updated = false;
            for (int i = 0; i < lines.size(); i++) {
                String[] data = lines.get(i).split(",", 2);
                if (data.length > 0 && data[0].equals(courseId)) {
                    String tutorId = lines.get(i).split(",")[2];
                    
                    // CHANGE THIS:
                    // String updatedLine = String.join(",", courseId, courseName, tutorId, level, subject, String.format("%.2f", fee));
                    
                    // TO THIS (add the schedule to the updated line):
                    String updatedLine = String.join(",", courseId, courseName, tutorId, level, subject, String.format("%.2f", fee), schedule);
                    
                    lines.set(i, updatedLine);
                    updated = true;
                    break;
                }
            }
            if (updated) {
                Files.write(Paths.get(COURSES_FILE), lines);
            }
            return updated;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteCourse(String courseId) {
        // Warning: This deletes the course but does not automatically un-enroll students.
        // A more robust system would handle this, e.g., by preventing deletion or notifying students.
        return deleteLineByColumnValue(COURSES_FILE, courseId, 0);
    }
    
    public static List<String> getStudentsByCourse(String courseId) {
        List<String> studentNames = new ArrayList<>();
        List<String> studentIDs = new ArrayList<>();

        // 1. Find all student IDs for the given course ID from enrollments.txt
        try (BufferedReader reader = new BufferedReader(new FileReader(ENROLLMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3 && data[2].equals(courseId)) {
                    studentIDs.add(data[1]);
                }
            }
        } catch (IOException e) { /* ignore */ }

        if (studentIDs.isEmpty()) {
            return studentNames; // No students enrolled
        }

        // 2. Find the names for each student ID from students.txt
        try (BufferedReader reader = new BufferedReader(new FileReader(STUDENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                // If this student's ID is in our list, add their name
                if (data.length >= 4 && studentIDs.contains(data[0])) {
                    studentNames.add(data[3] + " (" + data[0] + ")");
                }
            }
        } catch (IOException e) { /* ignore */ }
        
        return studentNames;
    }

    // --- STUDENT-SPECIFIC FUNCTIONS ---

    public static List<String> getStudentSchedule(String studentId) {
        List<String> schedule = new ArrayList<>();
        List<String> courseIDs = new ArrayList<>();

        // 1. Get all course IDs for the student from enrollments
        try (BufferedReader reader = new BufferedReader(new FileReader(ENROLLMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3 && data[1].equals(studentId)) {
                    courseIDs.add(data[2]);
                }
            }
        } catch (IOException e) { /* ignore */ }

        if (courseIDs.isEmpty()) return schedule;

        // 2. For each course ID, get the course name and schedule
        try (BufferedReader reader = new BufferedReader(new FileReader(COURSES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 7); // Now split by 7 for schedule
                if (data.length == 7 && courseIDs.contains(data[0])) {
                    schedule.add(data[1] + "  |  " + data[6]); // e.g., "Primary 5 Math  |  Mon 4-6 PM"
                }
            }
        } catch (IOException e) { /* ignore */ }
        
        return schedule;
    }

    public static boolean submitEnrollmentRequest(String studentId, String details) {
        String requestId = getNextIdForPrefix("REQ-", REQUESTS_FILE);
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String status = "PENDING";
        
        String requestLine = String.join(",", requestId, studentId, details, status, date) + "\n";
        
        try {
            Files.write(Paths.get(REQUESTS_FILE), requestLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String, String> getPendingRequests(String studentId) {
        Map<String, String> requests = new HashMap<>(); // Map<RequestID, Details>
        try (BufferedReader reader = new BufferedReader(new FileReader(REQUESTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5 && data[1].equals(studentId) && data[3].equalsIgnoreCase("PENDING")) {
                    requests.put(data[0], data[2]);
                }
            }
        } catch (IOException e) { /* ignore */ }
        return requests;
    }

    public static boolean deleteRequest(String requestId, String studentId) {
        // This is a secure delete: checks that the request belongs to the student AND is pending.
        File inputFile = new File(REQUESTS_FILE);
        if (!inputFile.exists()) return false;
        File tempFile = new File(REQUESTS_FILE + ".tmp");
        boolean deleted = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                // Check all conditions before deleting
                if (data.length == 5 && data[0].equals(requestId) && data[1].equals(studentId) && data[3].equalsIgnoreCase("PENDING")) {
                    deleted = true;
                    continue; // Skip writing this line to delete it
                }
                writer.write(line + System.lineSeparator());
            }
        } catch (IOException e) { e.printStackTrace(); return false; }

        if (deleted) {
            inputFile.delete();
            tempFile.renameTo(inputFile);
        } else {
            tempFile.delete(); // No changes were made
        }
        return deleted;
    }

    public static Map<String, Double> getPaymentStatus(String studentId) {
        Map<String, Double> status = new HashMap<>();
        double totalFees = 0.0;
        double totalPaid = 0.0;

        List<String> courseIDs = new ArrayList<>();
        List<String> enrollmentIDs = new ArrayList<>();

        // 1. Get all course and enrollment IDs for the student
        try (BufferedReader reader = new BufferedReader(new FileReader(ENROLLMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3 && data[1].equals(studentId)) {
                    enrollmentIDs.add(data[0]);
                    courseIDs.add(data[2]);
                }
            }
        } catch (IOException e) { /* ignore */ }

        // 2. Calculate total fees from the courses
        try (BufferedReader reader = new BufferedReader(new FileReader(COURSES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 7);
                if (data.length == 7 && courseIDs.contains(data[0])) {
                    totalFees += Double.parseDouble(data[5]);
                }
            }
        } catch (IOException | NumberFormatException e) { /* ignore */ }

        // 3. Calculate total paid from payments
        try (BufferedReader reader = new BufferedReader(new FileReader(PAYMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4 && enrollmentIDs.contains(data[1])) {
                    totalPaid += Double.parseDouble(data[2]);
                }
            }
        } catch (IOException | NumberFormatException e) { /* ignore */ }
        
        status.put("totalFees", totalFees);
        status.put("totalPaid", totalPaid);
        status.put("balance", totalFees - totalPaid);
        return status;
    }

    // --- CHAT SYSTEM FUNCTIONS ---

    public static boolean sendMessage(User sender, User receiver, String content) {
        String messageId = getNextIdForPrefix("MSG-", CHATS_FILE);
        // Use a sortable timestamp format
        String timestamp = LocalDateTime.now().toString(); 
        String status = "UNREAD";
        
        // IMPORTANT: We must escape commas in the user-generated content to not break our CSV
        String escapedContent = content.replace(",", ";"); 

        String messageLine = String.join(",", messageId, sender.getUsername(), receiver.getUsername(), timestamp, status, escapedContent) + "\n";
        
        try {
            Files.write(Paths.get(CHATS_FILE), messageLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getConversation(User user1, User user2) {
        List<String> conversation = new ArrayList<>();
        String u1 = user1.getUsername();
        String u2 = user2.getUsername();

        try (BufferedReader reader = new BufferedReader(new FileReader(CHATS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 6);
                if (data.length < 6) continue;

                String sender = data[1];
                String receiver = data[2];

                // Check if the message is between the two users
                if ((sender.equals(u1) && receiver.equals(u2)) || (sender.equals(u2) && receiver.equals(u1))) {
                    // Format for display: "Timestamp,Sender,Message"
                    // Un-escape the message content before adding it
                    String unescapedContent = data[5].replace(";", ",");
                    conversation.add(data[3] + "," + sender + "," + unescapedContent);
                }
            }
        } catch (IOException e) { /* ignore */ }
        
        // Sort the conversation by timestamp
        conversation.sort(Comparator.comparing(line -> LocalDateTime.parse(line.split(",")[0])));
        
        return conversation;
    }
    
    public static void markMessagesAsRead(User viewer, User otherUser) {
        List<String> updatedLines = new ArrayList<>();
        try {
            List<String> allLines = Files.readAllLines(Paths.get(CHATS_FILE));
            for (String line : allLines) {
                String[] data = line.split(",", 6);
                if (data.length < 6) {
                    updatedLines.add(line);
                    continue;
                }
                // Mark messages as read where the viewer is the receiver and the other user is the sender
                if (data[2].equals(viewer.getUsername()) && data[1].equals(otherUser.getUsername())) {
                    data[4] = "READ"; // Change status from UNREAD to READ
                    updatedLines.add(String.join(",", data));
                } else {
                    updatedLines.add(line);
                }
            }
            Files.write(Paths.get(CHATS_FILE), updatedLines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getUnreadMessageCount(User currentUser) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(CHATS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 6);
                if (data.length < 6) continue;
                // Count if the current user is the receiver and status is UNREAD
                if (data[2].equals(currentUser.getUsername()) && data[4].equalsIgnoreCase("UNREAD")) {
                    count++;
                }
            }
        } catch (IOException e) { /* ignore */ }
        return count;
    }

    public static List<User> getUsersForChat(User currentUser) {
        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(getAllUsersByRole("Admin"));
        allUsers.addAll(getAllUsersByRole("Receptionist"));
        allUsers.addAll(getAllUsersByRole("Tutor"));
        allUsers.addAll(getAllUsersByRole("Student"));

        List<User> eligibleUsers = new ArrayList<>();
        String currentUserRole = currentUser.getRole();

        for (User otherUser : allUsers) {
            // A user cannot chat with themselves
            if (otherUser.getUsername().equals(currentUser.getUsername())) {
                continue;
            }

            String otherUserRole = otherUser.getRole();
            
            // Apply communication rules
            boolean isBlocked = (currentUserRole.equals("Admin") && otherUserRole.equals("Student")) ||
                                (currentUserRole.equals("Student") && otherUserRole.equals("Admin"));
            
            if (!isBlocked) {
                eligibleUsers.add(otherUser);
            }
        }
        return eligibleUsers;
    }

    // --- HELPER METHODS ---

    private static String getFilePathForRole(String role) {
        switch (role) {
            case "Admin": return ADMINS_FILE;
            case "Tutor": return TUTORS_FILE;
            case "Receptionist": return RECEPTIONISTS_FILE;
            case "Student": return STUDENTS_FILE;
            default: return null;
        }
    }
    
    private static String getNextIdForRole(String role) {
        String prefix;
        int startNumber;
        switch (role) {
            case "Admin": prefix = "ADM-"; startNumber = 101; break;
            case "Receptionist": prefix = "REC-"; startNumber = 201; break;
            case "Tutor": prefix = "TUT-"; startNumber = 301; break;
            case "Student": prefix = "STU-"; startNumber = 401; break;
            default: return null;
        }
        return getNextIdForPrefix(prefix, getFilePathForRole(role), startNumber);
    }
    
    private static String getNextIdForPrefix(String prefix, String filePath) {
        return getNextIdForPrefix(prefix, filePath, 1);
    }
    
    private static String getNextIdForPrefix(String prefix, String filePath, int startNumber) {
        try {
            if (!Files.exists(Paths.get(filePath))) {
                return prefix + String.format("%03d", startNumber);
            }
            int maxNum = Files.lines(Paths.get(filePath))
                    .map(line -> line.split(",")[0])
                    .filter(id -> id.startsWith(prefix))
                    .map(id -> id.substring(prefix.length()))
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(startNumber - 1);
            return prefix + String.format("%03d", maxNum + 1);
        } catch (IOException e) {
            return prefix + String.format("%03d", startNumber); // Fallback
        }
    }

    private static boolean deleteLineByColumnValue(String filePath, String value, int columnIndex) {
        File inputFile = new File(filePath);
        if (!inputFile.exists()) return false;
        File tempFile = new File(filePath + ".tmp");
        boolean modified = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > columnIndex && data[columnIndex].equals(value)) {
                    modified = true;
                    continue;
                }
                writer.write(line + System.lineSeparator());
            }
        } catch (IOException e) { e.printStackTrace(); return false; }

        if (modified) { inputFile.delete(); tempFile.renameTo(inputFile); }
        else { tempFile.delete(); }
        return modified;
    }

    private static String findCourseIdForEnrollment(String enrollmentId) throws IOException {
        if (!Files.exists(Paths.get(ENROLLMENTS_FILE))) return null;
        return Files.lines(Paths.get(ENROLLMENTS_FILE)).map(line -> line.split(",")).filter(data -> data.length > 2 && data[0].equals(enrollmentId)).map(data -> data[2]).findFirst().orElse(null);
    }
    
    // In DataManager.java
    private static String[] findCourseDetails(String courseId) throws IOException {
        if (!Files.exists(Paths.get(COURSES_FILE))) return null;
        return Files.lines(Paths.get(COURSES_FILE))
                // CHANGE THIS:
                .map(line -> line.split(",", 7)) // Was split(",", 6)
                .filter(data -> data.length > 0 && data[0].equals(courseId))
                .map(data -> new String[]{data[3], data[4]})
                .findFirst().orElse(null);
    }

    // In DataManager.java
    public static String getCourseInfoById(String courseId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(COURSES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // CHANGE THIS:
                String[] data = line.split(",", 7); // Was split(",", 6)
                // CHANGE THIS:
                if (data.length >= 7 && data[0].equals(courseId)) { // Check for 7 columns
                    return String.format("%s (%s) - $%.2f", data[1], data[3], Double.parseDouble(data[5]));
                }
            }
        } catch (IOException e) { /* ignore */ }
        return "Unknown Course";
    }
     // --- RESULTS SYSTEM FUNCTIONS ---

    // For Tutors: To upload a result for a student in one of their courses.
    public static boolean uploadResult(String enrollmentId, String assessmentName, int score, int totalMarks) {
        String resultId = getNextIdForPrefix("RES-", RESULTS_FILE);
        String uploadDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        String resultLine = String.join(",", resultId, enrollmentId, assessmentName, String.valueOf(score), String.valueOf(totalMarks), uploadDate) + "\n";
        
        try {
            Files.write(Paths.get(RESULTS_FILE), resultLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // A private helper to get all raw result data for a specific enrollment
    private static List<String[]> getRawResultsForEnrollment(String enrollmentId) {
        List<String[]> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(RESULTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 6);
                if (data.length == 6 && data[1].equals(enrollmentId)) {
                    results.add(data);
                }
            }
        } catch (IOException e) { /* ignore */ }
        return results;
    }

    // A private helper to calculate average and grade from raw results
    private static Map<String, Object> calculateMetrics(List<String[]> rawResults) {
        Map<String, Object> metrics = new HashMap<>();
        double totalScore = 0;
        double totalMaxScore = 0;

        for (String[] result : rawResults) {
            totalScore += Double.parseDouble(result[3]);
            totalMaxScore += Double.parseDouble(result[4]);
        }

        double average = (totalMaxScore > 0) ? (totalScore / totalMaxScore) * 100 : 0;
        String grade;
        if (average >= 90) grade = "A+";
        else if (average >= 85) grade = "A";
        else if (average >= 80) grade = "A-";
        else if (average >= 75) grade = "B+";
        else if (average >= 70) grade = "B";
        else if (average >= 65) grade = "C+";
        else if (average >= 60) grade = "C";
        else if (average >= 50) grade = "D";
        else grade = "F";

        metrics.put("average", average);
        metrics.put("grade", grade);
        metrics.put("details", rawResults); // Include the raw data for detailed view
        return metrics;
    }
    
    // For Students and Admins: Get a formatted report for a single student
    public static String getStudentResultsReport(String studentId) {
        StringBuilder report = new StringBuilder();
        Map<String, String> enrollments = getStudentEnrollments(studentId); // Map<EnrollmentID, CourseInfo>

        if (enrollments.isEmpty()) {
            return "No courses enrolled.";
        }
        
        report.append("Results for Student ID: ").append(studentId).append("\n\n");

        for (Map.Entry<String, String> entry : enrollments.entrySet()) {
            String enrollmentId = entry.getKey();
            String courseInfo = entry.getValue().split(" - ")[0]; // "Course Name (Level)"
            report.append("--- Course: ").append(courseInfo).append(" ---\n");
            
            List<String[]> rawResults = getRawResultsForEnrollment(enrollmentId);
            if (rawResults.isEmpty()) {
                report.append("No results uploaded for this course yet.\n\n");
                continue;
            }
            
            Map<String, Object> metrics = calculateMetrics(rawResults);
            report.append(String.format("Overall Average: %.2f%%   Grade: %s\n", (Double) metrics.get("average"), (String) metrics.get("grade")));
            report.append("--------------------------------------------------\n");
            report.append(String.format("%-20s | %-10s | %-10s\n", "Assessment", "Score", "Total"));
            report.append("--------------------------------------------------\n");
            for (String[] result : rawResults) {
                report.append(String.format("%-20s | %-10s | %-10s\n", result[2], result[3], result[4]));
            }
            report.append("\n\n");
        }
        return report.toString();
    }

    // For Tutors: Get a summary report for all students in one of their courses
    public static String getTutorCourseResultsReport(String courseId) {
        StringBuilder report = new StringBuilder();
        List<String> studentIDs = new ArrayList<>();
        Map<String, String> enrollmentIdToStudentIdMap = new HashMap<>();

        // 1. Get all enrollments for this course
        try (BufferedReader reader = new BufferedReader(new FileReader(ENROLLMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3 && data[2].equals(courseId)) {
                    studentIDs.add(data[1]);
                    enrollmentIdToStudentIdMap.put(data[0], data[1]);
                }
            }
        } catch (IOException e) { return "Error reading enrollment data."; }
        
        if (studentIDs.isEmpty()) return "No students are enrolled in this course.";
        
        // 2. Create a map of student ID to student name
        Map<String, String> studentIdToNameMap = getAllUsersByRole("Student").stream()
                .collect(Collectors.toMap(User::getId, User::getFullName));
        
        report.append("Results Summary for Course ID: ").append(courseId).append("\n");
        report.append("------------------------------------------------------------------\n");
        report.append(String.format("%-20s | %-15s | %-10s\n", "Student Name", "Average (%)", "Grade"));
        report.append("------------------------------------------------------------------\n");

        // 3. Loop through enrollments, calculate metrics, and build the report
        for (Map.Entry<String, String> entry : enrollmentIdToStudentIdMap.entrySet()) {
            String enrollmentId = entry.getKey();
            String studentId = entry.getValue();
            String studentName = studentIdToNameMap.getOrDefault(studentId, "Unknown Student");
            
            List<String[]> rawResults = getRawResultsForEnrollment(enrollmentId);
            if (rawResults.isEmpty()) {
                report.append(String.format("%-20s | %-15s | %-10s\n", studentName, "N/A", "N/A"));
                continue;
            }
            
            Map<String, Object> metrics = calculateMetrics(rawResults);
            report.append(String.format("%-20s | %-15.2f | %-10s\n", studentName, (Double) metrics.get("average"), (String) metrics.get("grade")));
        }
        
        return report.toString();
    }

     public static boolean createAnnouncement(User author, String title, String content) {
        String announcementId = getNextIdForPrefix("ANC-", ANNOUNCEMENTS_FILE);
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        // Escape commas to prevent breaking the CSV format
        String escapedTitle = title.replace(",", ";");
        String escapedContent = content.replace(",", ";");

        String line = String.join(",", announcementId, escapedTitle, escapedContent, author.getUsername(), date) + "\n";
        try {
            Files.write(Paths.get(ANNOUNCEMENTS_FILE), line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static List<Announcement> getAllAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ANNOUNCEMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 5);
                if (data.length == 5) {
                    // Un-escape commas for display
                    String title = data[1].replace(";", ",");
                    String content = data[2].replace(";", ",");
                    announcements.add(new Announcement(data[0], title, content, data[3], data[4]));
                }
            }
        } catch (IOException e) { /* ignore */ }
        // Sort by date, newest first
        announcements.sort(Comparator.comparing(Announcement::getDate).reversed());
        return announcements;
    }

    public static Set<String> getReadAnnouncementIds(User user) {
        Set<String> readIds = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(READ_ANNOUNCEMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 2 && data[0].equals(user.getUsername())) {
                    readIds.add(data[1]);
                }
            }
        } catch (IOException e) { /* ignore */ }
        return readIds;
    }
    
    public static void markAnnouncementAsRead(User user, String announcementId) {
        // First, check if it's already marked as read to avoid duplicate entries
        Set<String> readIds = getReadAnnouncementIds(user);
        if (readIds.contains(announcementId)) {
            return; // Already read
        }
        
        String line = user.getUsername() + "," + announcementId + "\n";
        try {
            Files.write(Paths.get(READ_ANNOUNCEMENTS_FILE), line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> getAllPendingRequests() {
        List<String[]> requests = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(REQUESTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 5);
                // The status is in column 3
                if (data.length == 5 && data[3].equalsIgnoreCase("PENDING")) {
                    requests.add(data); // Add the full request data
                }
            }
        } catch (IOException e) { /* ignore if file doesn't exist */ }
        return requests;
    }

    // For Receptionist: Update the status of a request after handling it.
    public static boolean updateRequestStatus(String requestId, String newStatus) {
        File inputFile = new File(REQUESTS_FILE);
        if (!inputFile.exists()) return false;
        
        List<String> outLines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 5);
                if (data.length == 5 && data[0].equals(requestId)) {
                    data[3] = newStatus; // Update the status column
                    outLines.add(String.join(",", data));
                    updated = true;
                } else {
                    outLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (updated) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile, false))) { // false to overwrite
                for (String line : outLines) {
                    writer.write(line + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return updated;
    }

    public static Set<String> getStudentCourseIDs(String studentId) {
        Set<String> courseIDs = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ENROLLMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3 && data[1].equals(studentId)) {
                    courseIDs.add(data[2]);
                }
            }
        } catch (IOException e) { /* ignore */ }
        return courseIDs;
    }

     public static String getEnrollmentId(String studentId, String courseId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(ENROLLMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                // Check if column 1 is the studentID AND column 2 is the courseID
                if (data.length == 3 && data[1].equals(studentId) && data[2].equals(courseId)) {
                    return data[0]; // Return the enrollmentID from column 0
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return null if no matching enrollment is found
    }

    public static String generateTutorPayrollReport(String tutorId, int month, int year) {
        final double CENTER_COMMISSION_RATE = 0.20;
        double monthlyTotalGross = 0.0;
        
        StringBuilder report = new StringBuilder();
        report.append("========================================================\n");
        report.append("          Tutor Payroll Report\n");
        report.append("========================================================\n");

        // Find the tutor's name for the report header
        User tutor = getAllUsersByRole("Tutor").stream()
                .filter(u -> u.getId().equals(tutorId))
                .findFirst().orElse(null);
        
        if (tutor == null) {
            return "Tutor with ID " + tutorId + " not found.";
        }
        
        report.append("Tutor Name: ").append(tutor.getFullName()).append(" (").append(tutorId).append(")\n");
        report.append("Period: ").append(String.format("%02d", month)).append("/").append(year).append("\n\n");
        report.append("--- Earnings Breakdown by Course ---\n\n");

        // 1. Get all courses taught by this tutor
        List<String[]> tutorCourses = getCoursesByTutor(tutorId);

        if (tutorCourses.isEmpty()) {
            report.append("No courses assigned to this tutor.\n");
        } else {
            // 2. For each course, find enrolled students and calculate earnings
            for (String[] courseData : tutorCourses) {
                String courseId = courseData[0];
                String courseName = courseData[1];
                double courseFee = Double.parseDouble(courseData[5]);
                
                // Count students enrolled in this specific course
                long studentCount = 0;
                try {
                    studentCount = Files.lines(Paths.get(ENROLLMENTS_FILE))
                            .map(line -> line.split(","))
                            .filter(data -> data.length == 3 && data[2].equals(courseId))
                            .count();
                } catch (IOException e) { /* ignore */ }
                
                if (studentCount > 0) {
                    double grossForCourse = studentCount * courseFee;
                    monthlyTotalGross += grossForCourse;
                    
                    report.append("Course: ").append(courseName).append(" (").append(courseId).append(")\n");
                    report.append(String.format("  - Students Enrolled: %d\n", studentCount));
                    report.append(String.format("  - Fee per Student:   $%.2f\n", courseFee));
                    report.append(String.format("  - Gross for Course:  $%.2f\n\n", grossForCourse));
                }
            }
        }
        
        // 3. Final Calculation
        double centerCommission = monthlyTotalGross * CENTER_COMMISSION_RATE;
        double tutorNetPayout = monthlyTotalGross - centerCommission;

        report.append("--------------------------------------------------------\n");
        report.append("                   MONTHLY SUMMARY\n");
        report.append("--------------------------------------------------------\n");
        report.append(String.format("Total Gross Income (All Courses):   $%.2f\n", monthlyTotalGross));
        report.append(String.format("Center Commission (%.0f%%):           -$%.2f\n", CENTER_COMMISSION_RATE * 100, centerCommission));
        report.append("--------------------------------------------------------\n");
        report.append(String.format("TUTOR NET PAYOUT:                   $%.2f\n", tutorNetPayout));
        report.append("========================================================\n");
        
        return report.toString();
    }

    // Add this new method to DataManager.java

    // Checks if a username exists in any of the user role files.
    public static boolean isUsernameValid(String username) {
        // List all user files to check
        List<String> userFiles = Arrays.asList(ADMINS_FILE, TUTORS_FILE, RECEPTIONISTS_FILE, STUDENTS_FILE);

        for (String filePath : userFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",", 2); // Split only once to get the username efficiently
                    if (data.length > 1 && data[1].startsWith(username + ",")) {
                        return true; // Found the username, no need to check further
                    }
                }
            } catch (IOException e) {
                // Ignore if a file doesn't exist, just move to the next one
            }
        }
        return false; // Did not find the username in any file
    }

    // Generate comprehensive income vs payroll report for admin financial overview
    public static String generateIncomeVsPayrollReport(int month, int year) {
        StringBuilder report = new StringBuilder();
        report.append("=========================================================\n");
        report.append("          FINANCIAL SUMMARY REPORT\n");
        report.append("=========================================================\n");
        report.append("Period: ").append(String.format("%02d", month)).append("/").append(year).append("\n\n");

        // 1. Calculate total income for the period
        double totalIncome = 0.0;
        Map<String, Map<String, Double>> incomeByLevelAndSubject = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        try {
            if (Files.exists(Paths.get(PAYMENTS_FILE))) {
                List<String> payments = Files.readAllLines(Paths.get(PAYMENTS_FILE));
                for (String paymentLine : payments) {
                    String[] pData = paymentLine.split(",");
                    if (pData.length < 4) continue;
                    LocalDate paymentDate = LocalDate.parse(pData[3], formatter);
                    if (paymentDate.getMonthValue() == month && paymentDate.getYear() == year) {
                        double amount = Double.parseDouble(pData[2]);
                        totalIncome += amount;
                        
                        // Also categorize by level and subject for detailed breakdown
                        String enrollmentId = pData[1];
                        String courseId = findCourseIdForEnrollment(enrollmentId);
                        if (courseId != null) {
                            String[] courseDetails = findCourseDetails(courseId);
                            if (courseDetails != null) {
                                String level = courseDetails[0];
                                String subject = courseDetails[1];
                                incomeByLevelAndSubject.computeIfAbsent(level, k -> new HashMap<>()).merge(subject, amount, Double::sum);
                            }
                        }
                    }
                }
            }
        } catch (IOException | java.time.format.DateTimeParseException e) {
            return "Error reading payment data: " + e.getMessage();
        }

        // 2. Display income breakdown
        report.append("--- INCOME BREAKDOWN ---\n");
        if (incomeByLevelAndSubject.isEmpty()) {
            report.append("No income recorded for this period.\n");
        } else {
            for (Map.Entry<String, Map<String, Double>> levelEntry : incomeByLevelAndSubject.entrySet()) {
                report.append("Level: ").append(levelEntry.getKey()).append("\n");
                for (Map.Entry<String, Double> subjectEntry : levelEntry.getValue().entrySet()) {
                    report.append(String.format("  - %-15s: $%.2f\n", subjectEntry.getKey(), subjectEntry.getValue()));
                }
            }
        }
        report.append(String.format("\nTOTAL INCOME: $%.2f\n\n", totalIncome));

        // 3. Calculate payroll expenses for all tutors
        report.append("--- PAYROLL EXPENSES ---\n");
        List<User> allTutors = getAllUsersByRole("Tutor");
        double totalPayrollExpenses = 0.0;
        
        if (allTutors.isEmpty()) {
            report.append("No tutors found.\n");
        } else {
            final double CENTER_COMMISSION_RATE = 0.20;
            
            for (User tutor : allTutors) {
                String tutorId = tutor.getId();
                double tutorGrossIncome = 0.0;
                
                // Calculate gross income for this tutor
                List<String[]> tutorCourses = getCoursesByTutor(tutorId);
                for (String[] courseData : tutorCourses) {
                    String courseId = courseData[0];
                    double courseFee = Double.parseDouble(courseData[5]);
                    
                    // Count students enrolled in this course
                    long studentCount = 0;
                    try {
                        studentCount = Files.lines(Paths.get(ENROLLMENTS_FILE))
                                .map(line -> line.split(","))
                                .filter(data -> data.length == 3 && data[2].equals(courseId))
                                .count();
                    } catch (IOException e) { /* ignore */ }
                    
                    tutorGrossIncome += studentCount * courseFee;
                }
                
                double tutorNetPayout = tutorGrossIncome * (1 - CENTER_COMMISSION_RATE);
                totalPayrollExpenses += tutorNetPayout;
                
                if (tutorGrossIncome > 0) {
                    report.append(String.format("%-20s: Gross $%.2f -> Net $%.2f\n", 
                        tutor.getFullName(), tutorGrossIncome, tutorNetPayout));
                }
            }
        }
        
        report.append(String.format("\nTOTAL PAYROLL EXPENSES: $%.2f\n\n", totalPayrollExpenses));

        // 4. Calculate and display net profit/loss
        double netProfitLoss = totalIncome - totalPayrollExpenses;
        report.append("--- FINANCIAL SUMMARY ---\n");
        report.append(String.format("Total Income:         $%.2f\n", totalIncome));
        report.append(String.format("Total Payroll:        $%.2f\n", totalPayrollExpenses));
        report.append("----------------------------------\n");
        
        if (netProfitLoss >= 0) {
            report.append(String.format("NET PROFIT:           $%.2f\n", netProfitLoss));
        } else {
            report.append(String.format("NET LOSS:             $%.2f\n", Math.abs(netProfitLoss)));
        }
        
        report.append("=========================================================\n");
        
        return report.toString();
    }
}
