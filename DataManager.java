import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {
    // User files
    private static final String ADMINS_FILE = "data/admins.txt";
    private static final String TUTORS_FILE = "data/tutors.txt";
    private static final String RECEPTIONISTS_FILE = "data/receptionists.txt";
    private static final String STUDENTS_FILE = "data/students.txt";  
    // Data files
    private static final String STUDENT_DETAILS_FILE = "data/student_details.txt";
    private static final String COURSES_FILE = "data/courses.txt";
    private static final String ENROLLMENTS_FILE = "data/enrollments.txt";
    private static final String PAYMENTS_FILE = "data/payments.txt";
    private static final String REQUESTS_FILE = "data/requests.txt";
    private static final String CHATS_FILE = "data/chats.txt";
    private static final String RESULTS_FILE = "data/results.txt";
    private static final String ANNOUNCEMENTS_FILE = "data/announcements.txt";
    private static final String READ_ANNOUNCEMENTS_FILE = "data/read_announcements.txt";
    private static final String TIMETABLE_FILE = "data/timetable.txt";
    private static final String GROUP_CHATS_FILE = "data/group_chats.txt";
    private static final String ATTENDANCE_FILE = "data/attendance.txt";
    private static final String FEEDBACK_FILE = "data/feedback.txt";

    public static final List<String> AVAILABLE_SUBJECTS = Arrays.asList(
            "Mathematics", "Physics", "Additional Mathematics", "Chemistry",
            "Biology", "Chinese", "Computer Science", "English",
            "Bahasa Melayu", "History"
    );
    public static List<String> getAvailableSubjects() {
        return AVAILABLE_SUBJECTS;
    }

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

    public static boolean registerUser(String username, String password, String role, String fullName, List<String> specializations) {
        String filePath = getFilePathForRole(role);
        if (filePath == null) return false;

        if (isUsernameTaken(username)) {
            System.err.println("Username '" + username + "' is already taken.");
            return false;
        }
        
        try {
            String newUserId = getNextIdForRole(role);
            if (newUserId == null) return false;

            String newUserLine;

            // ============================ THE FIX: PART 2 ============================
            // This logic correctly handles the List<String> for Tutors.
            if ("Tutor".equalsIgnoreCase(role)) {
                // Join the list of specializations with a semicolon.
                String specializationString = String.join(";", specializations);
                newUserLine = String.join(",", newUserId, username, password, fullName, specializationString);
            } else {
                // For other roles, the specializations list is ignored.
                newUserLine = String.join(",", newUserId, username, password, fullName);
            }
            // ========================= END OF FIX ==========================

            String lineToWrite = newUserLine + System.lineSeparator();
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
        // ... (check if username is taken, same as above) ...
        if (isUsernameTaken(username)) {
            System.err.println("Username '" + username + "' is already taken.");
            return false;
        }
        
        String studentId = getNextIdForRole("Student");
        // --- THE FIX ---
        String userLine = String.join(",", studentId, username, password, fullName) + System.lineSeparator();
        try {
            Files.write(Paths.get(STUDENTS_FILE), userLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // --- THE FIX ---
        String detailsLine = String.join(",", studentId, ic, email, contact, address) + System.lineSeparator();
        try {
            Files.write(Paths.get(STUDENT_DETAILS_FILE), detailsLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        for (String courseId : courseIDs) {
            String enrollmentId = getNextIdForPrefix("ENR-", ENROLLMENTS_FILE);
            String enrollmentLine = String.join(",", enrollmentId, studentId, courseId) + System.lineSeparator();
            try {
                Files.write(Paths.get(ENROLLMENTS_FILE), enrollmentLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
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
        String paymentLine = String.join(",", paymentId, enrollmentId, String.valueOf(amount), paymentDate) + System.lineSeparator();
        
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
            String enrollmentLine = String.join(",", enrollmentId, studentId, courseId) + System.lineSeparator();
            try {
                Files.write(Paths.get(ENROLLMENTS_FILE), enrollmentLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
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

            String courseLine = String.join(",", courseId, courseName, tutorId, level, subject, String.format("%.2f", fee), schedule) + System.lineSeparator();
            
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
        
        String requestLine = String.join(",", requestId, studentId, details, status, date) + System.lineSeparator();
        
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

    public static void sendMessage(String senderUsername, String recipientUsername, String content) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String escapedContent = content.replace(",", ";").replace("\n", " | ");
        String status = "UNREAD";
        String messageLine = String.join(",", senderUsername, recipientUsername, escapedContent, timestamp, status) + System.lineSeparator();
        try {
            Files.write(Paths.get(CHATS_FILE), messageLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<Message> getConversation(String user1Username, String user2Username) {
        List<Message> conversation = new ArrayList<>();
        if (!Files.exists(Paths.get(CHATS_FILE))) {
            return conversation;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(CHATS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 5);
                if (data.length < 5) continue;
                String sender = data[0];
                String receiver = data[1];
                if ((sender.equals(user1Username) && receiver.equals(user2Username)) ||
                        (sender.equals(user2Username) && receiver.equals(user1Username))) {
                    String content = data[2].replace(";", ",").replace(" | ", "\n");
                    LocalDateTime timestamp = LocalDateTime.parse(data[3]);
                    boolean isRead = "READ".equalsIgnoreCase(data[4]);
                    conversation.add(new Message(sender, receiver, content, timestamp, isRead));
                }
            }
        } catch (IOException | java.time.format.DateTimeParseException e) {
            System.err.println("Error reading or parsing conversation: " + e.getMessage());
        }
        conversation.sort(Comparator.comparing(Message::getTimestamp));
        return conversation;
    }
    
    public static void markMessagesAsRead(User reader, User sender) {
        if (sender == null) return;
        if (!Files.exists(Paths.get(CHATS_FILE))) return;
        List<String> updatedLines = new ArrayList<>();
        boolean modified = false;

        try {
            List<String> allLines = Files.readAllLines(Paths.get(CHATS_FILE));
            for (String line : allLines) {
                String[] data = line.split(",", 5);
                if (data.length < 5) {
                    updatedLines.add(line);
                    continue;
                }
                // THE FIX: Check data[1] (recipient) and data[0] (sender)
                if (data[1].equals(reader.getUsername()) && data[0].equals(sender.getUsername()) && "UNREAD".equalsIgnoreCase(data[4])) {
                    data[4] = "READ"; // Update status
                    updatedLines.add(String.join(",", data));
                    modified = true;
                } else {
                    updatedLines.add(line);
                }
            }
            if (modified) {
                Files.write(Paths.get(CHATS_FILE), updatedLines);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getUnreadMessageCount(User currentUser) {
        if (!Files.exists(Paths.get(CHATS_FILE))) return 0;
        try {
            return (int) Files.lines(Paths.get(CHATS_FILE))
                .map(line -> line.split(",", 5))
                // THE FIX: Check data[1] (recipient) for the current user's name
                .filter(data -> data.length >= 5 && data[1].equals(currentUser.getUsername()) && "UNREAD".equalsIgnoreCase(data[4]))
                .count();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }


    public static List<User> getUsersForChat(User currentUser) {
        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(getAllUsersByRole("Admin"));
        allUsers.addAll(getAllUsersByRole("Tutor"));
        allUsers.addAll(getAllUsersByRole("Receptionist"));
        allUsers.addAll(getAllUsersByRole("Student"));

        List<User> eligibleUsers = new ArrayList<>();
        String currentUserRole = currentUser.getRole();

        // 2. Loop through every user and apply the communication rules.
        for (User otherUser : allUsers) {
            // Rule: A user cannot chat with themselves.
            if (otherUser.getUsername().equals(currentUser.getUsername())) {
                continue; // Skip to the next user in the loop
            }

            String otherUserRole = otherUser.getRole();
            
            // --- THE CRITICAL SECURITY CHECK ---

            // Rule: If the current user is an Admin, they cannot chat with Students.
            if (currentUserRole.equals("Admin") && otherUserRole.equals("Student")) {
                continue; // Skip this user
            }
            
            // Rule: If the current user is a Student, they cannot chat with Admins.
            if (currentUserRole.equals("Student") && otherUserRole.equals("Admin")) {
                continue; // Skip this user
            }

            // 3. If no rules were broken, the user is eligible to be chatted with.
            eligibleUsers.add(otherUser);
        }
        
        return eligibleUsers;
    }

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

    private static String[] findCourseDetails(String courseId) throws IOException {
        if (!Files.exists(Paths.get(COURSES_FILE))) return null;
        return Files.lines(Paths.get(COURSES_FILE))
                .map(line -> line.split(",", 7))
                .filter(data -> data.length > 0 && data[0].equals(courseId))
                .map(data -> new String[]{data[3], data[4]})
                .findFirst().orElse(null);
    }

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

    public static boolean uploadResult(String enrollmentId, String assessmentName, int score, int totalMarks) {
        String resultId = getNextIdForPrefix("RES-", RESULTS_FILE);
        String uploadDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        String resultLine = String.join(",", resultId, enrollmentId, assessmentName, String.valueOf(score), String.valueOf(totalMarks), uploadDate) + System.lineSeparator();
        try {
            Files.write(Paths.get(RESULTS_FILE), resultLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

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

        String line = String.join(",", announcementId, escapedTitle, escapedContent, author.getUsername(), date) + System.lineSeparator();
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
        
        String line = user.getUsername() + "," + announcementId + System.lineSeparator();
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
                if (data.length == 3 && data[1].equals(studentId) && data[2].equals(courseId)) {
                    return data[0];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public static boolean isUsernameValid(String username) {

        List<String> userFiles = Arrays.asList(ADMINS_FILE, TUTORS_FILE, RECEPTIONISTS_FILE, STUDENTS_FILE);

        for (String filePath : userFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",", 2);
                    if (data.length > 1 && data[1].startsWith(username + ",")) {
                        return true;
                    }
                }
            } catch (IOException e) {

            }
        }
        return false;
    }

    public static boolean deleteAnnouncement(String announcementId, User currentUser) {
        File inputFile = new File(ANNOUNCEMENTS_FILE);
        if (!inputFile.exists()) return false;
        
        File tempFile = new File(ANNOUNCEMENTS_FILE + ".tmp");
        boolean deleted = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 5);
                if (data.length == 5 && data[0].equals(announcementId) && data[3].equals(currentUser.getUsername())) {
                    deleted = true;
                    continue;
                }
                writer.write(line + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (deleted) {
            inputFile.delete();
            tempFile.renameTo(inputFile);
        } else {
            tempFile.delete();
        }
        return deleted;
    }

    public static boolean updateAnnouncement(String announcementId, String newTitle, String newContent, User currentUser) {
        File inputFile = new File(ANNOUNCEMENTS_FILE);
        if (!inputFile.exists()) return false;

        List<String> outLines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 5);
                // Security Check: Only update if the ID and author match.
                if (data.length == 5 && data[0].equals(announcementId) && data[3].equals(currentUser.getUsername())) {
                    // Re-escape commas in the new content before saving.
                    String escapedTitle = newTitle.replace(",", ";");
                    String escapedContent = newContent.replace(",", ";");
                    
                    // Reconstruct the line with the new data, keeping original ID, author, and date.
                    String updatedLine = String.join(",", data[0], escapedTitle, escapedContent, data[3], data[4]);
                    outLines.add(updatedLine);
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
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile, false))) { // false to overwrite the file
                for (String outLine : outLines) {
                    writer.write(outLine + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return updated;
    }

    public static boolean generateAndAssignTimetable() {
        System.out.println("Starting 4-week intelligent timetable generation...");

        // Step 1: Get all tutors and their specializations (same as before)
        Map<String, List<String>> subjectToTutorIds = new HashMap<>();
        Map<String, String> tutorIdToNameMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(TUTORS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 5 || data[4].trim().isEmpty()) continue;
                String tutorId = data[0].trim();
                String tutorName = data[3].trim();
                tutorIdToNameMap.put(tutorId, tutorName);
                // Specializations are stored separated by semicolons
                String[] specializations = data[4].split(";");
                for (String subject : specializations) {
                    String trimmedSubject = subject.trim();
                    if (!trimmedSubject.isEmpty()) {
                        subjectToTutorIds.computeIfAbsent(trimmedSubject, k -> new ArrayList<>()).add(tutorId);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (subjectToTutorIds.isEmpty()) {
            System.err.println("CRITICAL: No teachable subjects found. Check tutor specializations in tutors.txt.");
            return false;
        }

        // Step 2: Define available time slots and other setup
        String[] timeSlots = {"09-10 AM", "10-11 AM", "11-12 PM", "02-03 PM", "03-04 PM", "04-05 PM", "05-06 PM"};
        List<DayOfWeek> weekdays = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        Random random = new Random();
        List<String> newTimetableLines = new ArrayList<>();
        
        // Set the starting point to the Monday of the current week
        LocalDate weekStart = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Step 3: Loop for 4 weeks to generate the schedule
        for (int week = 0; week < 4; week++) {
            System.out.println("\n--- Processing Week " + (week + 1) + " (starting: " + weekStart + ") ---");
            
            // Data structures for the weekly generation. These are reset each week.
            Map<LocalDate, List<String>> availableSlotsPerDay = new LinkedHashMap<>();
            
            // Initialize available slots for the current week
            for (DayOfWeek day : weekdays) {
                availableSlotsPerDay.put(weekStart.with(day), new ArrayList<>(List.of(timeSlots)));
            }

            // Get the list of all unique subjects to be scheduled this week
            List<String> subjectsToSchedule = new ArrayList<>(subjectToTutorIds.keySet());
            Collections.shuffle(subjectsToSchedule);

            // This is the main scheduling logic for one week
            for (String subject : subjectsToSchedule) {
                boolean scheduledThisWeek = false;
                // Shuffle the days to ensure subjects are not always on the same day of the week
                List<LocalDate> shuffledDays = new ArrayList<>(availableSlotsPerDay.keySet());
                Collections.shuffle(shuffledDays);

                for (LocalDate day : shuffledDays) {
                    List<String> slotsOnDay = availableSlotsPerDay.get(day);
                    if (!slotsOnDay.isEmpty()) {
                        // Pick a random available slot on this day
                        String timeSlot = slotsOnDay.remove(random.nextInt(slotsOnDay.size()));

                        // Assign a random qualified tutor
                        List<String> qualifiedTutors = subjectToTutorIds.get(subject);
                        String tutorId = qualifiedTutors.get(random.nextInt(qualifiedTutors.size()));
                        String tutorName = tutorIdToNameMap.get(tutorId);
                        
                        // Create the timetable entry line
                        String timetableLine = String.join(",", day.toString(), timeSlot, "Weekly " + subject, tutorName);
                        newTimetableLines.add(timetableLine);
                        
                        System.out.println("  [OK] Scheduled '" + subject + "' on " + day.getDayOfWeek() + " at " + timeSlot);
                        scheduledThisWeek = true;
                        break; // Move to the next subject once this one is scheduled for the week
                    }
                }

                if (!scheduledThisWeek) {
                    System.out.println("  [WARN] Could not find a slot for '" + subject + "' in the week of " + weekStart + ". There may be more subjects than available slots.");
                }
            }
            
            // Move to the next week for the next iteration of the loop
            weekStart = weekStart.plusWeeks(1);
        }


        // Step 4: Write the generated timetable to the file, overwriting the old one
        System.out.println("\nFinished generating 4-week timetable. Writing to file...");
        try {
            Files.write(Paths.get(TIMETABLE_FILE), newTimetableLines, 
                    java.nio.file.StandardOpenOption.CREATE, 
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Timetable generated and saved successfully.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }





    public static Map<LocalDate, List<String[]>> getStoredTimetable() {
        // New structure: Map<Date, List<[Time, Subject, Tutor]>>
        Map<LocalDate, List<String[]>> weeklyTimetable = new TreeMap<>(); // TreeMap to keep dates sorted

        if (!Files.exists(Paths.get(TIMETABLE_FILE))) {
            return weeklyTimetable;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        try (BufferedReader reader = new BufferedReader(new FileReader(TIMETABLE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 4);
                if (data.length < 4) continue;

                try {
                    LocalDate date = LocalDate.parse(data[0], formatter);
                    String timeSlot = data[1];
                    String subject = data[2];
                    String tutor = data[3];

                    weeklyTimetable.computeIfAbsent(date, k -> new ArrayList<>())
                                .add(new String[]{timeSlot, subject, tutor});
                } catch (Exception e) {
                    System.err.println("Skipping malformed timetable line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("timetable.txt not found or is empty.");
        }

        // Sort classes within each day by time
        for (List<String[]> dailyClasses : weeklyTimetable.values()) {
            dailyClasses.sort(Comparator.comparing(c -> c[0]));
        }

        return weeklyTimetable;
    }

    public static List<String[]> getStudentPaymentHistory(String studentId) {
        List<String[]> paymentHistory = new ArrayList<>();

        Set<String> enrollmentIDs = getStudentEnrollments(studentId).keySet();
        if (enrollmentIDs.isEmpty()) {
            return paymentHistory;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(PAYMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] pData = line.split(",");
                if (pData.length < 4) continue;

                String enrollmentIdFromFile = pData[1];
                
                if (enrollmentIDs.contains(enrollmentIdFromFile)) {
                    String paymentId = pData[0];
                    String paymentDate = pData[3];
                    String amount = pData[2];
                    
                    String courseName = "Unknown Course";
                    String courseInfo = getStudentEnrollments(studentId).get(enrollmentIdFromFile);
                    if (courseInfo != null) {
                        courseName = courseInfo.split(" \\(")[0];
                    }
                    
                    paymentHistory.add(new String[]{paymentId, paymentDate, courseName, amount});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        paymentHistory.sort((p1, p2) -> p2[1].compareTo(p1[1]));
        
        return paymentHistory;
    }

    public static Map<String, Double> getEnrollmentPaymentStatus(String enrollmentId) {
        Map<String, Double> status = new HashMap<>();
        double courseFee = 0.0;
        double totalPaid = 0.0;
        String courseId = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(ENROLLMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3 && data[0].equals(enrollmentId)) {
                    courseId = data[2];
                    break;
                }
            }
        } catch (IOException e) { e.printStackTrace(); }

        if (courseId != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(COURSES_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",", 7);
                    if (data.length >= 6 && data[0].equals(courseId)) {
                        courseFee = Double.parseDouble(data[5]);
                        break;
                    }
                }
            } catch (IOException | NumberFormatException e) { e.printStackTrace(); }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(PAYMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4 && data[1].equals(enrollmentId)) {
                    totalPaid += Double.parseDouble(data[2]);
                }
            }
        } catch (IOException | NumberFormatException e) { e.printStackTrace(); }
        
        status.put("fee", courseFee);
        status.put("paid", totalPaid);
        status.put("balance", courseFee - totalPaid);
        return status;
    }

    public static List<String[]> getStudentScheduleForTable(String studentId) {
        List<String[]> scheduleData = new ArrayList<>();
        Set<String> courseIDs = getStudentCourseIDs(studentId);
        if (courseIDs.isEmpty()) {
            return scheduleData;
        }

        for (String courseId : courseIDs) {
            try {
                String[] courseDetails = Files.lines(Paths.get(COURSES_FILE))
                    .map(line -> line.split(",", 7))
                    .filter(data -> data.length >= 7 && data[0].equals(courseId))
                    .findFirst().orElse(null);
                
                if (courseDetails != null) {
                    String courseName = courseDetails[1];
                    String tutorId = courseDetails[2];
                    String schedule = courseDetails[6];
                    User tutorUser = getUserById(tutorId);
                    if (tutorUser != null) {
                        String tutorName = tutorUser.getFullName();
                        scheduleData.add(new String[]{courseName, tutorName, schedule});
                    } else {
                        System.err.println("Warning: Tutor with ID '" + tutorId + "' for course '" + courseName + "' not found. Skipping course in schedule view.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return scheduleData;
    }

    public static User getUserById(String userId) {
        List<String> userFiles = List.of(ADMINS_FILE, TUTORS_FILE, RECEPTIONISTS_FILE, STUDENTS_FILE);
        for (String file : userFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length > 0 && data[0].equals(userId)) {
                        return new User(data[0], data[1], "", "", data[3], "");
                    }
                }
            } catch (IOException e) {}
        }
        return null;
    }

    public static String[] getChatFilterRolesForUser(User currentUser) {
        List<String> masterRoleOrder = List.of("Admin", "Receptionist", "Tutor", "Student");
        List<String> finalRoles = new ArrayList<>();
        
        // "All Roles" and "Groups" are always available options.
        finalRoles.add("All Chats");
        finalRoles.add("Groups"); 

        String currentUserRole = currentUser.getRole();

        // Loop through the master list and add roles based on security rules.
        for (String role : masterRoleOrder) {
            // Rule: Admins cannot see "Student" in the filter list.
            if (currentUserRole.equals("Admin") && role.equals("Student")) {
                continue;
            }
            // Rule: Students cannot see "Admin" in the filter list.
            if (currentUserRole.equals("Student") && role.equals("Admin")) {
                continue;
            }
            finalRoles.add(role);
        }
        
        return finalRoles.toArray(new String[0]);
    }

    public static int getUnreadMessageCountFromSender(User receiver, User sender) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(CHATS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 6);
                if (data.length < 6) continue;
                if (data[2].equals(receiver.getUsername()) && 
                    data[1].equals(sender.getUsername()) && 
                    data[4].equalsIgnoreCase("UNREAD")) {
                    count++;
                }
            }
        } catch (IOException e) {}
        return count;
    }

    public static boolean isUsernameTaken(String username) {
        List<String> userFiles = List.of(ADMINS_FILE, TUTORS_FILE, RECEPTIONISTS_FILE, STUDENTS_FILE);
        for (String file : userFiles) {
            try {
                if (Files.exists(Paths.get(file))) {
                    boolean found = Files.lines(Paths.get(file))
                                        .anyMatch(line -> line.split(",")[1].equals(username));
                    if (found) return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String generateYearlyIncomeReport(int year) {
        Map<String, Double> incomeBySubject = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        double totalIncome = 0;

        try {
            if (!Files.exists(Paths.get(PAYMENTS_FILE))) {
                return "No payment data found.";
            }
            List<String> payments = Files.readAllLines(Paths.get(PAYMENTS_FILE));
            for (String paymentLine : payments) {
                String[] pData = paymentLine.split(",");
                if (pData.length < 4) continue;
                
                LocalDate paymentDate = LocalDate.parse(pData[3], formatter);
                if (paymentDate.getYear() == year) { // Check only the year
                    String enrollmentId = pData[1];
                    double amount = Double.parseDouble(pData[2]);
                    totalIncome += amount;

                    String courseId = findCourseIdForEnrollment(enrollmentId);
                    if (courseId != null) {
                        // findCourseDetails returns [level, subject]
                        String[] courseDetails = findCourseDetails(courseId);
                        if (courseDetails != null) {
                            String subject = courseDetails[1];
                            incomeBySubject.merge(subject, amount, Double::sum);
                        }
                    }
                }
            }
        } catch (IOException | java.time.format.DateTimeParseException e) {
            return "Error reading data files or parsing date: " + e.getMessage();
        }

        StringBuilder report = new StringBuilder();
        report.append("==================================================\n");
        report.append("          Yearly Income Report for ").append(year).append("\n");
        report.append("==================================================\n\n");
        
        if (incomeBySubject.isEmpty()) {
            report.append("No income recorded for this period.\n");
        } else {
            report.append("--- Income Breakdown by Subject ---\n\n");
            // Sort subjects alphabetically for a clean report
            List<String> sortedSubjects = new ArrayList<>(incomeBySubject.keySet());
            Collections.sort(sortedSubjects);

            for (String subject : sortedSubjects) {
                report.append(String.format("  - Subject: %-20s | Income: $%.2f\n", subject, incomeBySubject.get(subject)));
            }
            
            report.append("\n--------------------------------------------------\n");
            report.append(String.format("   TOTAL YEARLY INCOME: $%.2f\n", totalIncome));
            report.append("--------------------------------------------------\n");
        }
        
        return report.toString();
    }

    public static boolean createGroupChat(String groupName, User creator, Set<User> members) {
        String groupId = getNextIdForPrefix("GRP-", GROUP_CHATS_FILE);
        String creatorUsername = creator.getUsername();
        
        // The creator is always a member.
        members.add(creator);
        String memberUsernames = members.stream()
                                        .map(User::getUsername)
                                        .collect(Collectors.joining(";"));

        // Format: GroupID,GroupName,CreatorUsername,Member1;Member2;...
        String line = String.join(",", groupId, groupName, creatorUsername, memberUsernames) + System.lineSeparator();
        
        try {
            Files.write(Paths.get(GROUP_CHATS_FILE), line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<GroupChat> getGroupChatsForUser(User user) {
        List<GroupChat> groups = new ArrayList<>();
        if (!Files.exists(Paths.get(GROUP_CHATS_FILE))) {
            return groups;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(GROUP_CHATS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 4);
                if (data.length < 4) continue;
                
                Set<String> memberUsernames = new HashSet<>(Arrays.asList(data[3].split(";")));

                if (memberUsernames.contains(user.getUsername())) {
                    groups.add(new GroupChat(data[0], data[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public static String getGroupChatCreator(String groupId) {
        if (!Files.exists(Paths.get(GROUP_CHATS_FILE))) {
            return null;
        }
        try {
            return Files.lines(Paths.get(GROUP_CHATS_FILE))
                    .map(line -> line.split(",", 4))
                    .filter(data -> data.length >= 4 && data[0].equals(groupId))
                    .map(data -> data[2]) // Creator username is the 3rd element
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<User> getGroupChatMembers(String groupId) {
        List<User> members = new ArrayList<>();
        if (!Files.exists(Paths.get(GROUP_CHATS_FILE))) {
            return members;
        }
        try {
            String memberUsernamesLine = Files.lines(Paths.get(GROUP_CHATS_FILE))
                    .map(line -> line.split(",", 4))
                    .filter(data -> data.length >= 4 && data[0].equals(groupId))
                    .map(data -> data[3])
                    .findFirst()
                    .orElse("");
            
            if (!memberUsernamesLine.isEmpty()) {
                List<String> usernames = Arrays.asList(memberUsernamesLine.split(";"));
                for (String username : usernames) {
                    User user = findStudentOrTutorByUsername(username);
                    if (user != null) {
                        members.add(user);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return members;
    }

    public static boolean updateGroupChatMembers(String groupId, Set<User> newMembers) {
        File inputFile = new File(GROUP_CHATS_FILE);
        if (!inputFile.exists()) {
            return false;
        }
        List<String> outLines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 4);
                if (data.length >= 4 && data[0].equals(groupId)) {
                    String memberUsernames = newMembers.stream()
                                                    .map(User::getUsername)
                                                    .collect(Collectors.joining(";"));
                    
                    outLines.add(String.join(",", data[0], data[1], data[2], memberUsernames));
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
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile, false))) {
                for (String outLine : outLines) {
                    writer.write(outLine + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return updated;
    }

    public static User findUserByUsername(String username) {
        List<String> userFiles = List.of(ADMINS_FILE, TUTORS_FILE, RECEPTIONISTS_FILE, STUDENTS_FILE);
        for (String file : userFiles) {
            try {
                if (Files.exists(Paths.get(file))) {
                    Optional<User> user = Files.lines(Paths.get(file))
                        .map(line -> line.split(",", 5))
                        .filter(data -> data.length >= 4 && data[1].equals(username))
                        .map(data -> {
                            String role = getRoleFromFilePath(file);
                            String specialization = (role.equals("Tutor") && data.length > 4) ? data[4] : "";
                            return new User(data[0], data[1], data[2], role, data[3], specialization);
                        })
                        .findFirst();
                    if (user.isPresent()) return user.get();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
        return null;
    }

    private static String getRoleFromFilePath(String filePath) {
        if (filePath.contains("admins")) return "Admin";
        if (filePath.contains("tutors")) return "Tutor";
        if (filePath.contains("receptionists")) return "Receptionist";
        if (filePath.contains("students")) return "Student";
        return "Unknown";
    }

    public static boolean recordAttendance(String courseId, Map<String, String> attendanceData, String date) {
        // First, delete any existing records for this course on this date to prevent duplicates
        File inputFile = new File(ATTENDANCE_FILE);
        if (inputFile.exists()) {
            try {
                List<String> outLines = new ArrayList<>();
                List<String> allLines = Files.readAllLines(Paths.get(ATTENDANCE_FILE));
                for (String line : allLines) {
                    String[] data = line.split(",");
                    if (data.length >= 4 && data[0].equals(courseId) && data[2].equals(date)) {
                        continue; // Skip this line to delete it
                    }
                    outLines.add(line);
                }
                Files.write(Paths.get(ATTENDANCE_FILE), outLines);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // Now, write the new records
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile, true))) { // true to append
            for (Map.Entry<String, String> entry : attendanceData.entrySet()) {
                String studentId = entry.getKey();
                String status = entry.getValue();
                String line = String.join(",", courseId, studentId, date, status) + System.lineSeparator();
                writer.write(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static List<String[]> getAttendanceForStudent(String studentId, String courseId) {
        List<String[]> records = new ArrayList<>();
        if (!Files.exists(Paths.get(ATTENDANCE_FILE))) {
            return records;
        }
        try {
            records = Files.lines(Paths.get(ATTENDANCE_FILE))
                .map(line -> line.split(","))
                .filter(data -> data.length >= 4 && data[0].equals(courseId) && data[1].equals(studentId))
                .sorted((r1, r2) -> r2[2].compareTo(r1[2])) // Sort by date, descending
                .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    public static boolean updateAttendance(String courseId, String studentId, String date, String newStatus) {
        File inputFile = new File(ATTENDANCE_FILE);
        if (!inputFile.exists()) return false;
        
        List<String> outLines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 4);
                if (data.length >= 4 && data[0].equals(courseId) && data[1].equals(studentId) && data[2].equals(date)) {
                    outLines.add(String.join(",", data[0], data[1], data[2], newStatus));
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
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile, false))) {
                for (String outLine : outLines) {
                    writer.write(outLine + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return updated;
    }

    public static String generateAttendanceReport(String studentId, String courseId) {
        List<String[]> records = getAttendanceForStudent(studentId, courseId);
        if (records.isEmpty()) {
            return "No attendance records found for this student in this course.";
        }

        long totalClasses = records.size();
        long presentCount = records.stream().filter(r -> "Present".equals(r[3])).count();
        long absentCount = records.stream().filter(r -> "Absent".equals(r[3])).count();
        long lateCount = records.stream().filter(r -> "Late".equals(r[3])).count();
        
        // Attendance percentage considers "Present" and "Late" as attended.
        double attendancePercentage = (totalClasses > 0) ? ((double)(presentCount + lateCount) / totalClasses) * 100 : 0;

        User student = findUserByUsername(studentId);
        String studentName = (student != null) ? student.getFullName() : studentId;

        StringBuilder report = new StringBuilder();
        report.append("=========================================\n");
        report.append("       ATTENDANCE SUMMARY REPORT\n");
        report.append("=========================================\n\n");
        report.append(String.format(" Student: %s (%s)\n", studentName, studentId));
        report.append(String.format(" Course ID: %s\n", courseId));
        report.append("-----------------------------------------\n");
        report.append(String.format(" Total Classes Recorded: %d\n", totalClasses));
        report.append(String.format(" Days Present:           %d\n", presentCount));
        report.append(String.format(" Days Late:              %d\n", lateCount));
        report.append(String.format(" Days Absent:            %d\n", absentCount));
        report.append("-----------------------------------------\n");
        report.append(String.format(" Attendance Percentage:  %.2f%%\n", attendancePercentage));
        report.append("=========================================\n");

        return report.toString();
    }

    public static String getCourseIdFromEnrollment(String enrollmentId) {
        if (!Files.exists(Paths.get(ENROLLMENTS_FILE))) return null;
        try {
            return Files.lines(Paths.get(ENROLLMENTS_FILE))
                    .map(line -> line.split(","))
                    .filter(data -> data.length >= 3 && data[0].equals(enrollmentId))
                    .map(data -> data[2]) // The Course ID is the 3rd element
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean submitFeedback(String submitterId, String targetRole, String targetId, String subject, int rating, String content) {
        String feedbackId = getNextIdForPrefix("FB-", FEEDBACK_FILE);
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String status = "NEW"; // All new feedback is marked as "NEW"

        // Sanitize content to prevent breaking the CSV format
        String escapedContent = content.replace(",", ";").replace("\n", " | ");

        // Construct the new line with the submitter's ID included
        String feedbackLine = String.join(",",
                feedbackId,
                submitterId, // Added for traceability
                targetRole,
                targetId,
                subject,
                String.valueOf(rating),
                escapedContent,
                date,
                status)
                + System.lineSeparator();

        try {
            Files.write(Paths.get(FEEDBACK_FILE), feedbackLine.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Feedback> getFeedbackForUser(String userId) {
        List<Feedback> feedbackList = new ArrayList<>();
        if (!Files.exists(Paths.get(FEEDBACK_FILE))) {
            return feedbackList;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(FEEDBACK_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 9);
                if (data.length >= 9 && data[3].equals(userId)) {
                    String unescapedContent = data[6].replace(";", ",").replace(" | ", "\n");
                    Feedback feedback = new Feedback(
                            data[0],
                            data[1],
                            data[2],
                            data[3],
                            data[4],
                            Integer.parseInt(data[5]),
                            unescapedContent,
                            LocalDate.parse(data[7]),
                            data[8]
                    );
                    feedbackList.add(feedback);
                }
            }
        } catch (IOException | NumberFormatException | java.time.format.DateTimeParseException e) {
            e.printStackTrace();
        }
        feedbackList.sort(Comparator.comparing(Feedback::getDate).reversed());
        return feedbackList;
    }

    public static void markFeedbackAsRead(String userId) {
        if (!Files.exists(Paths.get(FEEDBACK_FILE))) return;

        List<String> outLines = new ArrayList<>();
        boolean modified = false;
        try {
            List<String> allLines = Files.readAllLines(Paths.get(FEEDBACK_FILE));
            for (String line : allLines) {
                String[] data = line.split(",", 9);
                if (data.length >= 9 && data[3].equals(userId) && "NEW".equalsIgnoreCase(data[8])) {
                    data[8] = "READ";
                    outLines.add(String.join(",", data));
                    modified = true;
                } else {
                    outLines.add(line);
                }
            }

            if (modified) {
                Files.write(Paths.get(FEEDBACK_FILE), outLines);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> getTutorClassesForDate(String tutorFullName, LocalDate date) {
        List<String[]> classes = new ArrayList<>();
        if (!Files.exists(Paths.get(TIMETABLE_FILE))) {
            return classes;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        try (BufferedReader reader = new BufferedReader(new FileReader(TIMETABLE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 4);
                if (data.length < 4) continue;

                LocalDate scheduleDate = LocalDate.parse(data[0], formatter);
                String tutorNameInSchedule = data[3];

                // Check if the date and tutor name match
                if (scheduleDate.equals(date) && tutorNameInSchedule.equals(tutorFullName)) {
                    // Return [Time, Subject]
                    classes.add(new String[]{data[1], data[2]});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    public static String getCourseIdByTutorAndSubject(String tutorId, String subjectName) {
        if (!Files.exists(Paths.get(COURSES_FILE))) {
            return null;
        }
        try {
            return Files.lines(Paths.get(COURSES_FILE))
                .map(line -> line.split(",", 7))
                .filter(data -> data.length >= 7 && data[2].equals(tutorId) && data[4].equalsIgnoreCase(subjectName))
                .map(data -> data[0]) // Return the course ID (the first element)
                .findFirst()
                .orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String[]> getAttendanceForAllCourses(String studentId) {
        List<String[]> allRecords = new ArrayList<>();
        
        // First, find all courses the student is enrolled in.
        Set<String> courseIDs = getStudentCourseIDs(studentId);
        
        // If the student has no courses, return an empty list.
        if (courseIDs.isEmpty()) {
            return allRecords;
        }

        // For each course, get its attendance and add it to the main list.
        for (String courseId : courseIDs) {
            // getAttendanceForStudent returns a List<String[]> where each array is [courseId, studentId, date, status]
            allRecords.addAll(getAttendanceForStudent(studentId, courseId));
        }

        // Sort the combined list by date, with the most recent first.
        allRecords.sort((r1, r2) -> {
            try {
                LocalDate date1 = LocalDate.parse(r1[2]);
                LocalDate date2 = LocalDate.parse(r2[2]);
                return date2.compareTo(date1); // Sort descending
            } catch (Exception e) {
                return 0;
            }
        });
        
        return allRecords;
    }

    public static List<AttendanceSummary> getAttendanceSummaryForStudent(String studentId) {
        List<AttendanceSummary> summaryList = new ArrayList<>();
        
        // 1. Get all courses the student is enrolled in.
        Map<String, String> enrollments = getStudentEnrollments(studentId);
        if (enrollments.isEmpty()) {
            return summaryList;
        }

        // 2. Get all attendance records for this student in one go.
        List<String[]> allRecords = getAttendanceForAllCourses(studentId);

        // 3. For each course, calculate the summary.
        for (Map.Entry<String, String> entry : enrollments.entrySet()) {
            String enrollmentId = entry.getKey();
            String courseId = getCourseIdFromEnrollment(enrollmentId);
            String courseName = getCourseInfoById(courseId).split(" - ")[0].split(" \\(")[0];

            if (courseId == null) continue;

            // Filter the records for the current course
            long totalClasses = allRecords.stream()
                    .filter(r -> r[0].equals(courseId))
                    .count();

            // "Attended" includes both "Present" and "Late"
            long attendedClasses = allRecords.stream()
                    .filter(r -> r[0].equals(courseId) && (r[3].equals("Present") || r[3].equals("Late")))
                    .count();

            if (totalClasses > 0) {
                summaryList.add(new AttendanceSummary(courseId, courseName, (int) attendedClasses, (int) totalClasses));
            }
        }
        
        return summaryList;
    }

    private static ResultSummary calculateGradeMetrics(String courseName, double percentage) {
        String gradeLetter;
        double gradePoints;

        if (percentage >= 80) { gradeLetter = "A+"; gradePoints = 4.00; }
        else if (percentage >= 75) { gradeLetter = "A"; gradePoints = 3.70; }
        else if (percentage >= 70) { gradeLetter = "B+"; gradePoints = 3.30; }
        else if (percentage >= 65) { gradeLetter = "B"; gradePoints = 3.00; }
        else if (percentage >= 60) { gradeLetter = "C+"; gradePoints = 2.70; }
        else if (percentage >= 55) { gradeLetter = "C"; gradePoints = 2.30; }
        else if (percentage >= 50) { gradeLetter = "D"; gradePoints = 2.00; }
        else { gradeLetter = "F"; gradePoints = 0.00; }

        return new ResultSummary(courseName, gradeLetter, gradePoints);
    }

    public static List<ResultSummary> getStudentResultSummaries(String studentId) {
        List<ResultSummary> resultSummaries = new ArrayList<>();
        Map<String, String> enrollments = getStudentEnrollments(studentId);

        for (Map.Entry<String, String> entry : enrollments.entrySet()) {
            String enrollmentId = entry.getKey();
            String courseInfo = entry.getValue();
            String courseName = courseInfo.split(" \\(")[0];

            List<String[]> rawResults = getRawResultsForEnrollment(enrollmentId);
            if (rawResults.isEmpty()) continue;

            double totalScore = 0;
            double totalMaxScore = 0;
            for (String[] result : rawResults) {
                totalScore += Double.parseDouble(result[3]);
                totalMaxScore += Double.parseDouble(result[4]);
            }

            double percentage = (totalMaxScore > 0) ? (totalScore / totalMaxScore) * 100 : 0;
            resultSummaries.add(calculateGradeMetrics(courseName, percentage));
        }
        return resultSummaries;
    }

    public static Message getLastMessage(String user1, String user2) {
        List<Message> conversation = getConversation(user1, user2); // This call is now correct
        if (conversation.isEmpty()) {
            return null;
        }
        return conversation.get(conversation.size() - 1);
    }

    public static Map<String, Integer> getAllUnreadMessageCounts(User currentUser) {
        if (!Files.exists(Paths.get(CHATS_FILE))) return new HashMap<>();
        try {
            return Files.lines(Paths.get(CHATS_FILE))
                    .map(line -> line.split(",", 5))
                    .filter(data -> data.length >= 5)
                    .filter(data -> data[1].equals(currentUser.getUsername())) // Recipient is current user
                    .filter(data -> "UNREAD".equalsIgnoreCase(data[4]))     // Message is unread
                    .collect(Collectors.groupingBy(
                            data -> data[0], // Group by sender's username
                            Collectors.summingInt(data -> 1) // Count 1 for each message
                    ));
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static void sendGroupMessage(String senderUsername, String groupId, String content) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String escapedContent = content.replace(",", ";").replace("\n", " | ");
        String status = "GROUP"; // A special status to identify it
        
        // Format: sender,recipient_group_id,content,timestamp,status
        String messageLine = String.join(",", senderUsername, groupId, escapedContent, timestamp, status) + System.lineSeparator();
        try {
            Files.write(Paths.get(CHATS_FILE), messageLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Message> getGroupConversation(String groupId) {
        List<Message> conversation = new ArrayList<>();
        if (!Files.exists(Paths.get(CHATS_FILE))) {
            return conversation;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(CHATS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 5);
                if (data.length < 5) continue;
                
                String recipient = data[1]; // Recipient is the second column
                if (recipient.equals(groupId)) { // Check if the recipient is this group
                    String sender = data[0];
                    String content = data[2].replace(";", ",").replace(" | ", "\n");
                    LocalDateTime timestamp = LocalDateTime.parse(data[3]);
                    // Group messages are considered "read" when fetched
                    conversation.add(new Message(sender, recipient, content, timestamp, true));
                }
            }
        } catch (IOException | java.time.format.DateTimeParseException e) {
            System.err.println("Error reading group conversation: " + e.getMessage());
        }
        conversation.sort(Comparator.comparing(Message::getTimestamp));
        return conversation;
    }
    
    public static Message getLastGroupMessage(String groupId) {
        List<Message> conversation = getGroupConversation(groupId);
        if (conversation == null || conversation.isEmpty()) {
            return null;
        }
        // Return the last message from the sorted list
        return conversation.get(conversation.size() - 1);
    }

    public static User findUserById(String userId) {
        List<String> userFiles = List.of(ADMINS_FILE, TUTORS_FILE, RECEPTIONISTS_FILE, STUDENTS_FILE);
        for (String file : userFiles) {
            if (!Files.exists(Paths.get(file))) continue;
            try {
                Optional<String[]> userData = Files.lines(Paths.get(file))
                    .map(line -> line.split(",", 5))
                    .filter(data -> data.length > 0 && data[0].equals(userId))
                    .findFirst();
                
                if (userData.isPresent()) {
                    String[] data = userData.get();
                    String role = getRoleFromFilePath(file); // Assumes you have getRoleFromFilePath
                    String specialization = (role.equals("Tutor") && data.length > 4) ? data[4] : "";
                    return new User(data[0], data[1], data[2], role, data[3], specialization);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static User findStudentOrTutorByUsername(String username) {
        List<String> userFiles = List.of(STUDENTS_FILE, TUTORS_FILE);

        for (String file : userFiles) {
            if (!Files.exists(Paths.get(file))) continue;
            try {
                Optional<String[]> userData = Files.lines(Paths.get(file))
                    .map(line -> line.split(",", 5))
                    .filter(data -> data.length >= 4 && data[1].equals(username))
                    .findFirst();
                
                if (userData.isPresent()) {
                    String[] data = userData.get();
                    String role = getRoleFromFilePath(file);
                    String specialization = (role.equals("Tutor") && data.length > 4) ? data[4] : "";
                    return new User(data[0], data[1], data[2], role, data[3], specialization);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null; // Return null if not found
    }

    public static int getUnreadFeedbackCount(String userId) {
        List<Feedback> feedbackList = getFeedbackForUser(userId);
        long count = feedbackList.stream()
                                .filter(f -> "NEW".equalsIgnoreCase(f.getStatus()))
                                .count();
        return (int) count;
    }
}