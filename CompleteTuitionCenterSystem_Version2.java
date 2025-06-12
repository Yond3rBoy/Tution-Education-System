package com.education.system;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.nio.file.*;

// Main Application Class
public class CompleteTuitionCenterSystem {
    private static Scanner scanner = new Scanner(System.in);
    private static TuitionCenterManager manager = new TuitionCenterManager();
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("    COMPLETE TUITION CENTER MANAGEMENT SYSTEM");
        System.out.println("=".repeat(60));
        System.out.println("Welcome, " + getCurrentUser() + "!");
        System.out.println("Current Date & Time: " + getCurrentDateTime());
        
        // Initialize text file database and sample data
        TextFileManager.initializeFiles();
        manager.initializeSampleData();
        
        // Start notification service
        manager.getNotificationService().startService();
        
        showMainMenu();
    }
    
    private static String getCurrentUser() {
        return "Yond3rBoy"; // Current logged-in user
    }
    
    private static String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    private static void showMainMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("                    MAIN MENU");
            System.out.println("=".repeat(60));
            System.out.println("1.  Student Management");
            System.out.println("2.  Teacher Management");
            System.out.println("3.  Course Management");
            System.out.println("4.  Enrollment Management");
            System.out.println("5.  Payment System");
            System.out.println("6.  Attendance Management");
            System.out.println("7.  Grade Management");
            System.out.println("8.  Reports & Analytics");
            System.out.println("9.  Notification Center");
            System.out.println("10. Database Backup/Export");
            System.out.println("11. System Settings");
            System.out.println("0.  Exit System");
            System.out.println("=".repeat(60));
            System.out.print("Enter your choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1: studentManagementMenu(); break;
                case 2: teacherManagementMenu(); break;
                case 3: courseManagementMenu(); break;
                case 4: enrollmentManagementMenu(); break;
                case 5: paymentSystemMenu(); break;
                case 6: attendanceManagementMenu(); break;
                case 7: gradeManagementMenu(); break;
                case 8: reportsMenu(); break;
                case 9: notificationMenu(); break;
                case 10: databaseMenu(); break;
                case 11: systemSettingsMenu(); break;
                case 0: 
                    System.out.println("Thank you for using Tuition Center Management System!");
                    manager.getNotificationService().shutdown();
                    System.exit(0);
                    break;
                default: 
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    // Student Management Menu
    private static void studentManagementMenu() {
        while (true) {
            System.out.println("\n=== STUDENT MANAGEMENT ===");
            System.out.println("1. Add New Student");
            System.out.println("2. View All Students");
            System.out.println("3. Search Student");
            System.out.println("4. Update Student");
            System.out.println("5. Delete Student");
            System.out.println("6. Student Performance Report");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1: addNewStudent(); break;
                case 2: viewAllStudents(); break;
                case 3: searchStudent(); break;
                case 4: updateStudent(); break;
                case 5: deleteStudent(); break;
                case 6: studentPerformanceReport(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }
    
    // Payment System Menu
    private static void paymentSystemMenu() {
        while (true) {
            System.out.println("\n=== PAYMENT SYSTEM ===");
            System.out.println("1. Process Payment");
            System.out.println("2. View Payment History");
            System.out.println("3. Generate Receipt");
            System.out.println("4. Outstanding Payments");
            System.out.println("5. Payment Reminders");
            System.out.println("6. Refund Payment");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1: processPayment(); break;
                case 2: viewPaymentHistory(); break;
                case 3: generateReceipt(); break;
                case 4: viewOutstandingPayments(); break;
                case 5: sendPaymentReminders(); break;
                case 6: processRefund(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }
    
    // Attendance Management Menu
    private static void attendanceManagementMenu() {
        while (true) {
            System.out.println("\n=== ATTENDANCE MANAGEMENT ===");
            System.out.println("1. Mark Individual Attendance");
            System.out.println("2. Mark Bulk Attendance");
            System.out.println("3. View Attendance Report");
            System.out.println("4. Attendance Analytics");
            System.out.println("5. Send Attendance Alerts");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1: markIndividualAttendance(); break;
                case 2: markBulkAttendance(); break;
                case 3: viewAttendanceReport(); break;
                case 4: attendanceAnalytics(); break;
                case 5: sendAttendanceAlerts(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }
    
    // Grade Management Menu
    private static void gradeManagementMenu() {
        while (true) {
            System.out.println("\n=== GRADE MANAGEMENT ===");
            System.out.println("1. Add Grade/Assessment");
            System.out.println("2. Update Grade");
            System.out.println("3. Student Report Card");
            System.out.println("4. Class Performance Report");
            System.out.println("5. Grade Analytics");
            System.out.println("6. Export Grades");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1: addGrade(); break;
                case 2: updateGrade(); break;
                case 3: generateReportCard(); break;
                case 4: classPerformanceReport(); break;
                case 5: gradeAnalytics(); break;
                case 6: exportGrades(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }
    
    // Reports Menu
    private static void reportsMenu() {
        while (true) {
            System.out.println("\n=== REPORTS & ANALYTICS ===");
            System.out.println("1. Financial Report");
            System.out.println("2. Enrollment Report");
            System.out.println("3. Attendance Summary");
            System.out.println("4. Performance Analytics");
            System.out.println("5. Teacher Performance");
            System.out.println("6. Custom Report Builder");
            System.out.println("7. Export All Reports");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1: generateFinancialReport(); break;
                case 2: generateEnrollmentReport(); break;
                case 3: generateAttendanceSummary(); break;
                case 4: performanceAnalytics(); break;
                case 5: teacherPerformanceReport(); break;
                case 6: customReportBuilder(); break;
                case 7: exportAllReports(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }
    
    // Implementation methods for Student Management
    private static void addNewStudent() {
        System.out.println("\n--- Add New Student ---");
        System.out.print("Enter Student Name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine();
        
        System.out.print("Enter Grade: ");
        String grade = scanner.nextLine();
        
        System.out.print("Enter Parent Name: ");
        String parentName = scanner.nextLine();
        
        System.out.print("Enter Parent Contact: ");
        String parentContact = scanner.nextLine();
        
        Student student = new Student(0, name, email, phone, LocalDate.now(), grade, parentName, parentContact);
        
        try {
            manager.getStudentService().addStudent(student);
            System.out.println("✓ Student added successfully!");
        } catch (Exception e) {
            System.err.println("✗ Error adding student: " + e.getMessage());
        }
    }
    
    private static void processPayment() {
        System.out.println("\n--- Process Payment ---");
        
        System.out.print("Enter Enrollment ID: ");
        int enrollmentId = getIntInput();
        
        System.out.print("Enter Payment Amount: $");
        double amount = getDoubleInput();
        
        System.out.println("Payment Methods:");
        System.out.println("1. Cash");
        System.out.println("2. Credit Card");
        System.out.println("3. Bank Transfer");
        System.out.println("4. Check");
        System.out.print("Select payment method: ");
        
        int methodChoice = getIntInput();
        String[] methods = {"", "Cash", "Credit Card", "Bank Transfer", "Check"};
        String paymentMethod = (methodChoice >= 1 && methodChoice <= 4) ? methods[methodChoice] : "Cash";
        
        try {
            String result = manager.getPaymentService().processPayment(enrollmentId, amount, paymentMethod);
            System.out.println("✓ " + result);
        } catch (Exception e) {
            System.err.println("✗ Error processing payment: " + e.getMessage());
        }
    }
    
    private static void markIndividualAttendance() {
        System.out.println("\n--- Mark Individual Attendance ---");
        
        System.out.print("Enter Student ID: ");
        int studentId = getIntInput();
        
        System.out.print("Enter Course ID: ");
        int courseId = getIntInput();
        
        System.out.println("Attendance Status:");
        System.out.println("1. Present");
        System.out.println("2. Absent");
        System.out.println("3. Late");
        System.out.println("4. Excused");
        System.out.print("Select status: ");
        
        int statusChoice = getIntInput();
        Attendance.AttendanceStatus[] statuses = {
            null, 
            Attendance.AttendanceStatus.PRESENT,
            Attendance.AttendanceStatus.ABSENT,
            Attendance.AttendanceStatus.LATE,
            Attendance.AttendanceStatus.EXCUSED
        };
        
        Attendance.AttendanceStatus status = (statusChoice >= 1 && statusChoice <= 4) ? 
            statuses[statusChoice] : Attendance.AttendanceStatus.PRESENT;
        
        System.out.print("Enter notes (optional): ");
        String notes = scanner.nextLine();
        
        try {
            manager.getAttendanceService().markAttendance(studentId, courseId, LocalDate.now(), status, notes);
            System.out.println("✓ Attendance marked successfully!");
        } catch (Exception e) {
            System.err.println("✗ Error marking attendance: " + e.getMessage());
        }
    }
    
    private static void addGrade() {
        System.out.println("\n--- Add Grade/Assessment ---");
        
        System.out.print("Enter Student ID: ");
        int studentId = getIntInput();
        
        System.out.print("Enter Course ID: ");
        int courseId = getIntInput();
        
        System.out.print("Enter Assessment Type (Test/Assignment/Exam/Quiz): ");
        String assessmentType = scanner.nextLine();
        
        System.out.print("Enter Assessment Name: ");
        String assessmentName = scanner.nextLine();
        
        System.out.print("Enter Maximum Marks: ");
        double maxMarks = getDoubleInput();
        
        System.out.print("Enter Obtained Marks: ");
        double obtainedMarks = getDoubleInput();
        
        try {
            manager.getGradeService().addGrade(studentId, courseId, assessmentType, 
                                             assessmentName, maxMarks, obtainedMarks, LocalDate.now());
            System.out.println("✓ Grade added successfully!");
        } catch (Exception e) {
            System.err.println("✗ Error adding grade: " + e.getMessage());
        }
    }
    
    private static void generateFinancialReport() {
        System.out.println("\n--- Financial Report ---");
        
        System.out.print("Enter start date (YYYY-MM-DD): ");
        String startDateStr = scanner.nextLine();
        
        System.out.print("Enter end date (YYYY-MM-DD): ");
        String endDateStr = scanner.nextLine();
        
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            
            manager.getReportService().generateFinancialReport(startDate, endDate);
        } catch (Exception e) {
            System.err.println("✗ Error generating report: " + e.getMessage());
        }
    }
    
    // Utility methods
    private static int getIntInput() {
        while (true) {
            try {
                int value = scanner.nextInt();
                scanner.nextLine(); // consume newline
                return value;
            } catch (Exception e) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.nextLine(); // clear invalid input
            }
        }
    }
    
    private static double getDoubleInput() {
        while (true) {
            try {
                double value = scanner.nextDouble();
                scanner.nextLine(); // consume newline
                return value;
            } catch (Exception e) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.nextLine(); // clear invalid input
            }
        }
    }
    
    // Placeholder methods for remaining functionality
    private static void teacherManagementMenu() { System.out.println("Teacher Management - Implementation in progress..."); }
    private static void courseManagementMenu() { System.out.println("Course Management - Implementation in progress..."); }
    private static void enrollmentManagementMenu() { System.out.println("Enrollment Management - Implementation in progress..."); }
    private static void notificationMenu() { System.out.println("Notification Center - Implementation in progress..."); }
    private static void databaseMenu() { 
        System.out.println("\n=== DATABASE OPERATIONS ===");
        System.out.println("1. Backup Data");
        System.out.println("2. Restore Data");
        System.out.println("3. Export to CSV");
        System.out.println("4. View Database Statistics");
        System.out.print("Choice: ");
        int choice = getIntInput();
        switch(choice) {
            case 1: TextFileManager.backupData(); break;
            case 2: System.out.println("Restore feature - Implementation in progress..."); break;
            case 3: TextFileManager.exportToCSV(); break;
            case 4: TextFileManager.showDatabaseStats(); break;
            default: System.out.println("Invalid choice.");
        }
    }
    private static void systemSettingsMenu() { System.out.println("System Settings - Implementation in progress..."); }
    private static void viewAllStudents() { manager.getStudentService().displayAllStudents(); }
    private static void searchStudent() { 
        System.out.print("Enter Student ID to search: ");
        int id = getIntInput();
        manager.getStudentService().searchStudentById(id);
    }
    private static void updateStudent() { System.out.println("Update Student - Implementation in progress..."); }
    private static void deleteStudent() { 
        System.out.print("Enter Student ID to delete: ");
        int id = getIntInput();
        manager.getStudentService().deleteStudent(id);
    }
    private static void studentPerformanceReport() { System.out.println("Student Performance Report - Implementation in progress..."); }
    private static void viewPaymentHistory() { manager.getPaymentService().displayPaymentHistory(); }
    private static void generateReceipt() { System.out.println("Generate Receipt - Implementation in progress..."); }
    private static void viewOutstandingPayments() { manager.getPaymentService().displayOutstandingPayments(); }
    private static void sendPaymentReminders() { manager.getNotificationService().sendPaymentReminders(); }
    private static void processRefund() { System.out.println("Process Refund - Implementation in progress..."); }
    private static void markBulkAttendance() { System.out.println("Bulk Attendance - Implementation in progress..."); }
    private static void viewAttendanceReport() { manager.getAttendanceService().displayAttendanceReport(); }
    private static void attendanceAnalytics() { System.out.println("Attendance Analytics - Implementation in progress..."); }
    private static void sendAttendanceAlerts() { System.out.println("Attendance Alerts - Implementation in progress..."); }
    private static void updateGrade() { System.out.println("Update Grade - Implementation in progress..."); }
    private static void generateReportCard() { 
        System.out.print("Enter Student ID: ");
        int studentId = getIntInput();
        manager.getGradeService().generateStudentReportCard(studentId);
    }
    private static void classPerformanceReport() { System.out.println("Class Performance Report - Implementation in progress..."); }
    private static void gradeAnalytics() { System.out.println("Grade Analytics - Implementation in progress..."); }
    private static void exportGrades() { System.out.println("Export Grades - Implementation in progress..."); }
    private static void generateEnrollmentReport() { manager.getReportService().generateEnrollmentReport(); }
    private static void generateAttendanceSummary() { System.out.println("Attendance Summary - Implementation in progress..."); }
    private static void performanceAnalytics() { System.out.println("Performance Analytics - Implementation in progress..."); }
    private static void teacherPerformanceReport() { System.out.println("Teacher Performance Report - Implementation in progress..."); }
    private static void customReportBuilder() { System.out.println("Custom Report Builder - Implementation in progress..."); }
    private static void exportAllReports() { System.out.println("Export All Reports - Implementation in progress..."); }
}

// Text File Database Manager
class TextFileManager {
    private static final String DATA_FOLDER = "tuition_data";
    private static final String STUDENTS_FILE = DATA_FOLDER + "/students.txt";
    private static final String TEACHERS_FILE = DATA_FOLDER + "/teachers.txt";
    private static final String COURSES_FILE = DATA_FOLDER + "/courses.txt";
    private static final String ENROLLMENTS_FILE = DATA_FOLDER + "/enrollments.txt";
    private static final String PAYMENTS_FILE = DATA_FOLDER + "/payments.txt";
    private static final String ATTENDANCE_FILE = DATA_FOLDER + "/attendance.txt";
    private static final String GRADES_FILE = DATA_FOLDER + "/grades.txt";
    private static final String COUNTERS_FILE = DATA_FOLDER + "/counters.txt";
    
    public static void initializeFiles() {
        try {
            // Create data folder if it doesn't exist
            Path dataPath = Paths.get(DATA_FOLDER);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
            
            // Create files if they don't exist
            createFileIfNotExists(STUDENTS_FILE, "# Students Data\n# ID|Name|Email|Phone|DateOfBirth|Grade|ParentName|ParentContact|CreatedAt\n");
            createFileIfNotExists(TEACHERS_FILE, "# Teachers Data\n# ID|Name|Email|Phone|Subject|Salary|CreatedAt\n");
            createFileIfNotExists(COURSES_FILE, "# Courses Data\n# ID|CourseName|Subject|TeacherID|Fee|Duration|StartTime|EndTime|DaysOfWeek|MaxStudents|CurrentStudents|CreatedAt\n");
            createFileIfNotExists(ENROLLMENTS_FILE, "# Enrollments Data\n# ID|StudentID|CourseID|EnrollmentDate|IsActive|AmountPaid|TotalFee|CreatedAt\n");
            createFileIfNotExists(PAYMENTS_FILE, "# Payments Data\n# ID|EnrollmentID|Amount|PaymentDate|PaymentMethod|ReceiptNumber|Notes|CreatedAt\n");
            createFileIfNotExists(ATTENDANCE_FILE, "# Attendance Data\n# ID|StudentID|CourseID|AttendanceDate|Status|Notes|CreatedAt\n");
            createFileIfNotExists(GRADES_FILE, "# Grades Data\n# ID|StudentID|CourseID|AssessmentType|AssessmentName|MaxMarks|ObtainedMarks|Percentage|GradeLetter|AssessmentDate|CreatedAt\n");
            createFileIfNotExists(COUNTERS_FILE, "students:0\nteachers:0\ncourses:0\nenrollments:0\npayments:0\nattendance:0\ngrades:0\n");
            
            System.out.println("✓ Text file database initialized successfully!");
            
        } catch (IOException e) {
            System.err.println("✗ Error initializing text file database: " + e.getMessage());
        }
    }
    
    private static void createFileIfNotExists(String fileName, String header) throws IOException {
        Path filePath = Paths.get(fileName);
        if (!Files.exists(filePath)) {
            Files.write(filePath, header.getBytes());
        }
    }
    
    public static int getNextId(String entityType) {
        try {
            Map<String, Integer> counters = readCounters();
            int currentId = counters.getOrDefault(entityType, 0);
            int nextId = currentId + 1;
            counters.put(entityType, nextId);
            writeCounters(counters);
            return nextId;
        } catch (Exception e) {
            System.err.println("Error getting next ID: " + e.getMessage());
            return 1;
        }
    }
    
    private static Map<String, Integer> readCounters() throws IOException {
        Map<String, Integer> counters = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(COUNTERS_FILE));
        
        for (String line : lines) {
            if (!line.trim().isEmpty() && !line.startsWith("#")) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    counters.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                }
            }
        }
        return counters;
    }
    
    private static void writeCounters(Map<String, Integer> counters) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counters.entrySet()) {
            lines.add(entry.getKey() + ":" + entry.getValue());
        }
        Files.write(Paths.get(COUNTERS_FILE), lines);
    }
    
    public static void writeToFile(String fileName, String data) {
        try {
            Files.write(Paths.get(fileName), (data + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error writing to file " + fileName + ": " + e.getMessage());
        }
    }
    
    public static List<String> readFromFile(String fileName) {
        try {
            return Files.readAllLines(Paths.get(fileName));
        } catch (IOException e) {
            System.err.println("Error reading from file " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public static void backupData() {
        try {
            String backupFolder = "backup_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Path backupPath = Paths.get(backupFolder);
            Files.createDirectories(backupPath);
            
            // Copy all data files to backup folder
            String[] files = {STUDENTS_FILE, TEACHERS_FILE, COURSES_FILE, ENROLLMENTS_FILE, 
                             PAYMENTS_FILE, ATTENDANCE_FILE, GRADES_FILE, COUNTERS_FILE};
            
            for (String file : files) {
                Path source = Paths.get(file);
                Path target = Paths.get(backupFolder + "/" + Paths.get(file).getFileName());
                if (Files.exists(source)) {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            
            System.out.println("✓ Data backup created successfully in folder: " + backupFolder);
            
        } catch (IOException e) {
            System.err.println("✗ Error creating backup: " + e.getMessage());
        }
    }
    
    public static void exportToCSV() {
        try {
            String exportFolder = "export_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Path exportPath = Paths.get(exportFolder);
            Files.createDirectories(exportPath);
            
            // Export students to CSV
            exportStudentsToCSV(exportFolder + "/students.csv");
            exportPaymentsToCSV(exportFolder + "/payments.csv");
            exportAttendanceToCSV(exportFolder + "/attendance.csv");
            exportGradesToCSV(exportFolder + "/grades.csv");
            
            System.out.println("✓ Data exported to CSV successfully in folder: " + exportFolder);
            
        } catch (IOException e) {
            System.err.println("✗ Error exporting to CSV: " + e.getMessage());
        }
    }
    
    private static void exportStudentsToCSV(String fileName) throws IOException {
        List<String> lines = readFromFile(STUDENTS_FILE);
        List<String> csvLines = new ArrayList<>();
        csvLines.add("ID,Name,Email,Phone,DateOfBirth,Grade,ParentName,ParentContact,CreatedAt");
        
        for (String line : lines) {
            if (!line.startsWith("#") && !line.trim().isEmpty()) {
                csvLines.add(line.replace("|", ","));
            }
        }
        
        Files.write(Paths.get(fileName), csvLines);
    }
    
    private static void exportPaymentsToCSV(String fileName) throws IOException {
        List<String> lines = readFromFile(PAYMENTS_FILE);
        List<String> csvLines = new ArrayList<>();
        csvLines.add("ID,EnrollmentID,Amount,PaymentDate,PaymentMethod,ReceiptNumber,Notes,CreatedAt");
        
        for (String line : lines) {
            if (!line.startsWith("#") && !line.trim().isEmpty()) {
                csvLines.add(line.replace("|", ","));
            }
        }
        
        Files.write(Paths.get(fileName), csvLines);
    }
    
    private static void exportAttendanceToCSV(String fileName) throws IOException {
        List<String> lines = readFromFile(ATTENDANCE_FILE);
        List<String> csvLines = new ArrayList<>();
        csvLines.add("ID,StudentID,CourseID,AttendanceDate,Status,Notes,CreatedAt");
        
        for (String line : lines) {
            if (!line.startsWith("#") && !line.trim().isEmpty()) {
                csvLines.add(line.replace("|", ","));
            }
        }
        
        Files.write(Paths.get(fileName), csvLines);
    }
    
    private static void exportGradesToCSV(String fileName) throws IOException {
        List<String> lines = readFromFile(GRADES_FILE);
        List<String> csvLines = new ArrayList<>();
        csvLines.add("ID,StudentID,CourseID,AssessmentType,AssessmentName,MaxMarks,ObtainedMarks,Percentage,GradeLetter,AssessmentDate,CreatedAt");
        
        for (String line : lines) {
            if (!line.startsWith("#") && !line.trim().isEmpty()) {
                csvLines.add(line.replace("|", ","));
            }
        }
        
        Files.write(Paths.get(fileName), csvLines);
    }
    
    public static void showDatabaseStats() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("            DATABASE STATISTICS");
        System.out.println("=".repeat(50));
        
        String[] files = {"students", "teachers", "courses", "enrollments", "payments", "attendance", "grades"};
        String[] fileNames = {STUDENTS_FILE, TEACHERS_FILE, COURSES_FILE, ENROLLMENTS_FILE, 
                             PAYMENTS_FILE, ATTENDANCE_FILE, GRADES_FILE};
        
        for (int i = 0; i < files.length; i++) {
            List<String> lines = readFromFile(fileNames[i]);
            int count = 0;
            for (String line : lines) {
                if (!line.startsWith("#") && !line.trim().isEmpty()) {
                    count++;
                }
            }
            System.out.printf("%-15s: %d records\n", files[i].toUpperCase(), count);
        }
        
        System.out.println("=".repeat(50));
    }
    
    // File path getters
    public static String getStudentsFile() { return STUDENTS_FILE; }
    public static String getTeachersFile() { return TEACHERS_FILE; }
    public static String getCoursesFile() { return COURSES_FILE; }
    public static String getEnrollmentsFile() { return ENROLLMENTS_FILE; }
    public static String getPaymentsFile() { return PAYMENTS_FILE; }
    public static String getAttendanceFile() { return ATTENDANCE_FILE; }
    public static String getGradesFile() { return GRADES_FILE; }
}

// Core Entity Classes
class Student {
    private int id;
    private String name;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String grade;
    private String parentName;
    private String parentContact;
    
    public Student(int id, String name, String email, String phone, LocalDate dateOfBirth, 
                   String grade, String parentName, String parentContact) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.grade = grade;
        this.parentName = parentName;
        this.parentContact = parentContact;
    }
    
    public String toFileString() {
        return String.format("%d|%s|%s|%s|%s|%s|%s|%s|%s", 
                           id, name, email, phone, dateOfBirth, grade, parentName, parentContact, 
                           LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    public static Student fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 8) {
            return new Student(
                Integer.parseInt(parts[0]),
                parts[1], parts[2], parts[3],
                LocalDate.parse(parts[4]),
                parts[5], parts[6], parts[7]
            );
        }
        return null;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public String getParentContact() { return parentContact; }
    public void setParentContact(String parentContact) { this.parentContact = parentContact; }
    
    @Override
    public String toString() {
        return String.format("Student{ID=%d, Name='%s', Grade='%s', Email='%s', Parent='%s'}", 
                           id, name, grade, email, parentName);
    }
}

class Teacher {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private double salary;
    private List<String> qualifications;
    
    public Teacher(int id, String name, String email, String phone, String subject, double salary) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.subject = subject;
        this.salary = salary;
        this.qualifications = new ArrayList<>();
    }
    
    public String toFileString() {
        return String.format("%d|%s|%s|%s|%s|%.2f|%s", 
                           id, name, email, phone, subject, salary,
                           LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    public static Teacher fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 6) {
            return new Teacher(
                Integer.parseInt(parts[0]),
                parts[1], parts[2], parts[3], parts[4],
                Double.parseDouble(parts[5])
            );
        }
        return null;
    }
    
    public void addQualification(String qualification) {
        qualifications.add(qualification);
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    public List<String> getQualifications() { return qualifications; }
    
    @Override
    public String toString() {
        return String.format("Teacher{ID=%d, Name='%s', Subject='%s', Email='%s', Salary=%.2f}", 
                           id, name, subject, email, salary);
    }
}

class Course {
    private int id;
    private String courseName;
    private String subject;
    private Teacher teacher;
    private double fee;
    private int duration;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<String> daysOfWeek;
    private int maxStudents;
    private int currentStudents;
    
    public Course(int id, String courseName, String subject, Teacher teacher, double fee, 
                  int duration, LocalTime startTime, LocalTime endTime, 
                  List<String> daysOfWeek, int maxStudents) {
        this.id = id;
        this.courseName = courseName;
        this.subject = subject;
        this.teacher = teacher;
        this.fee = fee;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.daysOfWeek = daysOfWeek;
        this.maxStudents = maxStudents;
        this.currentStudents = 0;
    }
    
    public boolean canEnroll() { return currentStudents < maxStudents; }
    public void enrollStudent() { if (canEnroll()) currentStudents++; }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public List<String> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<String> daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    public int getMaxStudents() { return maxStudents; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }
    public int getCurrentStudents() { return currentStudents; }
    public void setCurrentStudents(int currentStudents) { this.currentStudents = currentStudents; }
    
    @Override
    public String toString() {
        return String.format("Course{ID=%d, Name='%s', Subject='%s', Teacher='%s', Fee=%.2f, Students=%d/%d}", 
                           id, courseName, subject, teacher.getName(), fee, currentStudents, maxStudents);
    }
}

class Enrollment {
    private int id;
    private Student student;
    private Course course;
    private LocalDate enrollmentDate;
    private boolean isActive;
    private double amountPaid;
    private double totalFee;
    
    public Enrollment(Student student, Course course, LocalDate enrollmentDate) {
        this.student = student;
        this.course = course;
        this.enrollmentDate = enrollmentDate;
        this.isActive = true;
        this.totalFee = course.getFee();
        this.amountPaid = 0.0;
        course.enrollStudent();
    }
    
    public String toFileString() {
        return String.format("%d|%d|%d|%s|%s|%.2f|%.2f|%s", 
                           id, student.getId(), course.getId(), enrollmentDate, isActive, 
                           amountPaid, totalFee,
                           LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    public void makePayment(double amount) {
        amountPaid += amount;
    }
    
    public double getRemainingBalance() {
        return totalFee - amountPaid;
    }
    
    public boolean isFullyPaid() {
        return amountPaid >= totalFee;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Student getStudent() { return student; }
    public Course getCourse() { return course; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    public double getTotalFee() { return totalFee; }
    
    @Override
    public String toString() {
        return String.format("Enrollment{Student='%s', Course='%s', Date=%s, Paid=%.2f/%.2f, Active=%s}", 
                           student.getName(), course.getCourseName(), enrollmentDate, 
                           amountPaid, totalFee, isActive);
    }
}

class Payment {
    private int id;
    private int enrollmentId;
    private double amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String receiptNumber;
    private String notes;
    
    public Payment(int enrollmentId, double amount, LocalDate paymentDate, 
                   String paymentMethod, String receiptNumber) {
        this.enrollmentId = enrollmentId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.receiptNumber = receiptNumber;
    }
    
    public String toFileString() {
        return String.format("%d|%d|%.2f|%s|%s|%s|%s|%s", 
                           id, enrollmentId, amount, paymentDate, paymentMethod, receiptNumber, 
                           notes != null ? notes : "",
                           LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    public static Payment fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 6) {
            Payment payment = new Payment(
                Integer.parseInt(parts[1]),
                Double.parseDouble(parts[2]),
                LocalDate.parse(parts[3]),
                parts[4], parts[5]
            );
            payment.setId(Integer.parseInt(parts[0]));
            if (parts.length > 6) {
                payment.setNotes(parts[6]);
            }
            return payment;
        }
        return null;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    @Override
    public String toString() {
        return String.format("Payment{ID=%d, Amount=%.2f, Date=%s, Method='%s', Receipt='%s'}", 
                           id, amount, paymentDate, paymentMethod, receiptNumber);
    }
}

class Attendance {
    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE, EXCUSED
    }
    
    private int id;
    private int studentId;
    private int courseId;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private String notes;
    
    public Attendance(int studentId, int courseId, LocalDate attendanceDate, AttendanceStatus status) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }
    
    public String toFileString() {
        return String.format("%d|%d|%d|%s|%s|%s|%s", 
                           id, studentId, courseId, attendanceDate, status, 
                           notes != null ? notes : "",
                           LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    public static Attendance fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            Attendance attendance = new Attendance(
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                LocalDate.parse(parts[3]),
                AttendanceStatus.valueOf(parts[4])
            );
            attendance.setId(Integer.parseInt(parts[0]));
            if (parts.length > 5) {
                attendance.setNotes(parts[5]);
            }
            return attendance;
        }
        return null;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    @Override
    public String toString() {
        return String.format("Attendance{StudentID=%d, CourseID=%d, Date=%s, Status=%s}", 
                           studentId, courseId, attendanceDate, status);
    }
}

class Grade {
    private int id;
    private int studentId;
    private int courseId;
    private String assessmentType;
    private String assessmentName;
    private double maxMarks;
    private double obtainedMarks;
    private double percentage;
    private String gradeLetter;
    private LocalDate assessmentDate;
    
    public Grade(int studentId, int courseId, String assessmentType, String assessmentName,
                 double maxMarks, double obtainedMarks, LocalDate assessmentDate) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.assessmentType = assessmentType;
        this.assessmentName = assessmentName;
        this.maxMarks = maxMarks;
        this.obtainedMarks = obtainedMarks;
        this.assessmentDate = assessmentDate;
        this.percentage = (obtainedMarks / maxMarks) * 100;
        this.gradeLetter = calculateGradeLetter(percentage);
    }
    
    public String toFileString() {
        return String.format("%d|%d|%d|%s|%s|%.2f|%.2f|%.2f|%s|%s|%s", 
                           id, studentId, courseId, assessmentType, assessmentName, 
                           maxMarks, obtainedMarks, percentage, gradeLetter, assessmentDate,
                           LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    public static Grade fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 10) {
            Grade grade = new Grade(
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                parts[3], parts[4],
                Double.parseDouble(parts[5]),
                Double.parseDouble(parts[6]),
                LocalDate.parse(parts[9])
            );
            grade.setId(Integer.parseInt(parts[0]));
            return grade;
        }
        return null;
    }
    
    private String calculateGradeLetter(double percentage) {
        if (percentage >= 90) return "A+";
        else if (percentage >= 85) return "A";
        else if (percentage >= 80) return "A-";
        else if (percentage >= 75) return "B+";
        else if (percentage >= 70) return "B";
        else if (percentage >= 65) return "B-";
        else if (percentage >= 60) return "C+";
        else if (percentage >= 55) return "C";
        else if (percentage >= 50) return "C-";
        else return "F";
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getAssessmentType() { return assessmentType; }
    public void setAssessmentType(String assessmentType) { this.assessmentType = assessmentType; }
    public String getAssessmentName() { return assessmentName; }
    public void setAssessmentName(String assessmentName) { this.assessmentName = assessmentName; }
    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }
    public double getObtainedMarks() { return obtainedMarks; }
    public void setObtainedMarks(double obtainedMarks) { 
        this.obtainedMarks = obtainedMarks;
        this.percentage = (obtainedMarks / maxMarks) * 100;
        this.gradeLetter = calculateGradeLetter(percentage);
    }
    public double getPercentage() { return percentage; }
    public String getGradeLetter() { return gradeLetter; }
    public LocalDate getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }
    
    @Override
    public String toString() {
        return String.format("Grade{Assessment='%s', Marks=%.1f/%.1f, Percentage=%.1f%%, Grade='%s'}", 
                           assessmentName, obtainedMarks, maxMarks, percentage, gradeLetter);
    }
}

// Service Classes
class StudentService {
    public void addStudent(Student student) {
        try {
            int id = TextFileManager.getNextId("students");
            student.setId(id);
            TextFileManager.writeToFile(TextFileManager.getStudentsFile(), student.toFileString());
        } catch (Exception e) {
            System.err.println("Error adding student: " + e.getMessage());
        }
    }
    
    public void displayAllStudents() {
        List<String> lines = TextFileManager.readFromFile(TextFileManager.getStudentsFile());
        
        System.out.println("\n" + "=".repeat(100));
        System.out.println("                                    ALL STUDENTS");
        System.out.println("=".repeat(100));
        System.out.printf("%-5s | %-20s | %-25s | %-12s | %-10s | %-20s\n", 
                        "ID", "Name", "Email", "Phone", "Grade", "Parent");
        System.out.println("-".repeat(100));
        
        for (String line : lines) {
            if (!line.startsWith("#") && !line.trim().isEmpty()) {
                Student student = Student.fromFileString(line);
                if (student != null) {
                    System.out.printf("%-5d | %-20s | %-25s | %-12s | %-10s | %-20s\n",
                                    student.getId(), student.getName(), student.getEmail(),
                                    student.getPhone(), student.getGrade(), student.getParentName());
                }
            }
        }
        System.out.println("=".repeat(100));
    }
    
    public void searchStudentById(int id) {
        List<String> lines = TextFileManager.readFromFile(TextFileManager.getStudentsFile());
        
        for (String line : lines) {
            if (!line.startsWith("#") && !line.trim().isEmpty()) {
                Student student = Student.fromFileString(line);
                if (student != null && student.getId() == id) {
                    System.out.println("\n✓ Student found:");
                    System.out.println(student);
                    return;
                }
            }
        }
        System.out.println("\n✗ Student with ID " + id + " not found!");
    }
    
    public void deleteStudent(int id) {
        List<String> lines = TextFileManager.readFromFile(TextFileManager.getStudentsFile());
        List<String> updatedLines = new ArrayList<>();
        boolean found = false;
        
        for (String line : lines) {
            if (line.startsWith("#") || line.trim().isEmpty()) {
                updatedLines.add(line);
            } else {
                Student student = Student.fromFileString(line);
                if (student != null && student.getId() == id) {
                    found = true;
                    System.out.println("✓ Student " + student.getName() + " deleted successfully!");
                } else {
                    updatedLines.add(line);
                }
            }
        }
        
        if (found) {
            try {
                Files.write(Paths.get(TextFileManager.getStudentsFile()), updatedLines);
            } catch (IOException e) {
                System.err.println("Error updating file: " + e.getMessage());
            }
        } else {
            System.out.println("✗ Student with ID " + id + " not found!");
        }
    }
}

class PaymentService {
    public String processPayment(int enrollmentId, double amount, String paymentMethod) {
        try {
            // Generate receipt number
            String receiptNumber = generateReceiptNumber();
            
            // Create payment record
            Payment payment = new Payment(enrollmentId, amount, LocalDate.now(), paymentMethod, receiptNumber);
            int id = TextFileManager.getNextId("payments");
            payment.setId(id);
            
            // Save payment to file
            TextFileManager.writeToFile(TextFileManager.getPaymentsFile(), payment.toFileString());
            
            // Generate receipt
            generateReceipt(payment);
            
            return "Payment processed successfully! Receipt Number: " + receiptNumber;
        } catch (Exception e) {
            return "Error processing payment: " + e.getMessage();
        }
    }
    
    private String generateReceiptNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Random random = new Random();
        int randomNum = 1000 + random.nextInt(9000);
        return "RCP" + date + randomNum;
    }
    
    private void generateReceipt(Payment payment) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("           PAYMENT RECEIPT");
        System.out.println("=".repeat(50));
        System.out.println("Receipt Number: " + payment.getReceiptNumber());
        System.out.println("Date: " + payment.getPaymentDate());
        System.out.println("Enrollment ID: " + payment.getEnrollmentId());
        System.out.println("Payment Method: " + payment.getPaymentMethod());
        System.out.println("Amount Paid: $" + String.format("%.2f", payment.getAmount()));
        System.out.println("=".repeat(50));
        System.out.println("Thank you for your payment!");
        System.out.println("=".repeat(50));
    }
    
    public void displayPaymentHistory() {
        List<String> lines = TextFileManager.readFromFile(TextFileManager.getPaymentsFile());
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                           PAYMENT HISTORY");
        System.out.println("=".repeat(80));
        System.out.printf("%-5s | %-12s | %-10s | %-12s | %-15s | %-15s\n", 
                        "ID", "Enrollment", "Amount", "Date", "Method", "Receipt");
        System.out.println("-".repeat(80));
        
        for (String line : lines) {
            if (!line.startsWith("#") && !line.trim().isEmpty()) {
                Payment payment = Payment.fromFileString(line);
                if (payment != null) {
                    System.out.printf("%-5d | %-12d | $%-9.2f | %-12s | %-15s | %-15s\n",
                                    payment.getId(), payment.getEnrollmentId(), payment.getAmount(),
                                    payment.getPaymentDate(), payment.getPaymentMethod(), 
                                    payment.getReceiptNumber());
                }
            }
        }
        System.out.println("=".repeat(80));
    }
    
    public void displayOutstandingPayments() {
        System.out.println("\n=== OUTSTANDING PAYMENTS ===");
        System.out.println("Outstanding Payments feature - Implementation in progress...");
        // This would require enrollment data to calculate outstanding amounts
    }
}

class AttendanceService {
    public void markAttendance(int studentId, int courseId, LocalDate date, 
                              Attendance.AttendanceStatus status, String notes) {
        try {
            Attendance attendance = new Attendance(studentId, courseId, date, status);
            attendance.setNotes(notes);
            int id = TextFileManager.getNextId("attendance");
            attendance.setId(id);
            
            TextFileManager.writeToFile(TextFileManager.getAttendanceFile(), attendance.toFileString());
        } catch (Exception e) {
            System.err.println("Error marking attendance: " + e.getMessage());
        }
    }
    
    public void displayAttendanceReport() {
        List<String> lines = TextFileManager.readFromFile(TextFileManager.getAttendanceFile());
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                        ATTENDANCE REPORT");
        System.out.println("=".repeat(80));
        System.out.printf("%-5s | %-10s | %-10s | %-12s | %-10s | %-20s\n", 
                        "ID", "Student", "Course", "Date", "Status", "Notes");
        System.out.println("-".repeat(80));
        
        for (String line : lines) {
            if (!line.startsWith("#") && !line.trim().isEmpty()) {
                Attendance attendance = Attendance.fromFileString(line);
                if (attendance != null) {
                    System.out.printf("%-5d | %-10d | %-10d | %-12s | %-10s | %-20s\n",
                                    attendance.getId(), attendance.getStudentId(), 
                                    attendance.getCourseId(), attendance.getAttendanceDate(),
                                    attendance.getStatus(), 
                                    attendance.getNotes() != null ? attendance.getNotes() : "");
                }
            }
        }
        System.out.println("=".repeat(80));
    }
}

class GradeService {
    public void addGrade(int studentId, int courseId, String assessmentType, String assessmentName,
                        double maxMarks, double obtainedMarks, LocalDate assessmentDate) {
        try {
            Grade grade = new Grade(studentId, courseId, assessmentType, assessmentName, 
                                   maxMarks, obtainedMarks, assessmentDate);