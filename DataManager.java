import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataManager {

    // --- FILE CONSTANTS ---
    // User files
    private static final String ADMINS_FILE = "data/admins.txt";
    private static final String TUTORS_FILE = "data/tutors.txt";
    private static final String RECEPTIONISTS_FILE = "data/receptionists.txt";
    private static final String STUDENTS_FILE = "data/students.txt";
    
    // Data files
    private static final String STUDENT_DETAILS_FILE = "data/student_details.txt";
    private static final String COURSES_FILE = "data/courses.txt";
    private static final String ENROLLMENTS_FILE = "data/enrollments.txt";
    private static final String PAYMENTS_FILE = "data.txt/payments";
    private static final String REQUESTS_FILE = "data/requests.txt";

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

            if ("Tutor".equalsIgnoreCase(role)) {
                newUserLine = String.join(",", newUserId, username, password, fullName, specialization) + "\n";
            } else {
                newUserLine = String.join(",", newUserId, username, password, fullName) + "\n";
            }

            Files.write(Paths.get(filePath), newUserLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
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

    public static List<String> getAvailableCourses() {
        List<String> courses = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(COURSES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 6);
                if (data.length >= 6) {
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
                String[] data = line.split(",", 6);
                // Check if the tutorID column (index 2) matches
                if (data.length >= 6 && data[2].equals(tutorId)) {
                    courses.add(data);
                }
            }
        } catch (IOException e) { /* ignore */ }
        return courses;
    }
    
    public static boolean addCourse(String courseName, String tutorId, String level, String subject, double fee) {
        try {
            String courseId = getNextIdForPrefix("C-", COURSES_FILE, 101); // Start course IDs from 101
            String courseLine = String.join(",", courseId, courseName, tutorId, level, subject, String.format("%.2f", fee)) + "\n";
            Files.write(Paths.get(COURSES_FILE), courseLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateCourse(String courseId, String courseName, String level, String subject, double fee) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(COURSES_FILE));
            boolean updated = false;
            for (int i = 0; i < lines.size(); i++) {
                String[] data = lines.get(i).split(",", 2);
                if (data.length > 0 && data[0].equals(courseId)) {
                    String tutorId = lines.get(i).split(",")[2]; // Preserve the original tutor ID
                    String updatedLine = String.join(",", courseId, courseName, tutorId, level, subject, String.format("%.2f", fee));
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
    
    private static String[] findCourseDetails(String courseId) throws IOException {
        if (!Files.exists(Paths.get(COURSES_FILE))) return null;
        return Files.lines(Paths.get(COURSES_FILE)).map(line -> line.split(",", 6)).filter(data -> data.length > 0 && data[0].equals(courseId)).map(data -> new String[]{data[3], data[4]}).findFirst().orElse(null);
    }

    private static String getCourseInfoById(String courseId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(COURSES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 6);
                if (data.length >= 6 && data[0].equals(courseId)) {
                    return String.format("%s (%s) - $%.2f", data[1], data[3], Double.parseDouble(data[5]));
                }
            }
        } catch (IOException e) { /* ignore */ }
        return "Unknown Course";
    }
}