import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class IncomePayroll {

    private static final double CENTER_COMMISSION_RATE = 0.20;
    private static final String ENROLLMENTS_FILE = "data/enrollments.txt";

    /**
     * Generates a detailed payroll report for a specific tutor for a given month and year.
     *
     * @param tutorId The ID of the tutor to generate the report for.
     * @param month   The month of the payroll period.
     * @param year    The year of the payroll period.
     * @return A formatted string containing the payroll report.
     */
    public static String generatePayrollReport(String tutorId, int month, int year) {
        double monthlyTotalGross = 0.0;
        
        StringBuilder report = new StringBuilder();
        report.append("========================================================\n");
        report.append("          Tutor Payroll Report\n");
        report.append("========================================================\n");

        // Find the tutor's name for the report header
        User tutor = DataManager.getAllUsersByRole("Tutor").stream()
                .filter(u -> u.getId().equals(tutorId))
                .findFirst().orElse(null);
        
        if (tutor == null) {
            return "Tutor with ID " + tutorId + " not found.";
        }
        
        report.append("Tutor Name: ").append(tutor.getFullName()).append(" (").append(tutorId).append(")\n");
        report.append("Period: ").append(String.format("%02d", month)).append("/").append(year).append("\n\n");
        report.append("--- Earnings Breakdown by Course ---\n\n");

        // 1. Get all courses taught by this tutor
        List<String[]> tutorCourses = DataManager.getCoursesByTutor(tutorId);

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
                    if (Files.exists(Paths.get(ENROLLMENTS_FILE))) {
                        studentCount = Files.lines(Paths.get(ENROLLMENTS_FILE))
                                .map(line -> line.split(","))
                                .filter(data -> data.length == 3 && data[2].equals(courseId))
                                .count();
                    }
                } catch (IOException e) {
                    // Log the error or handle it as appropriate
                    System.err.println("Error reading enrollments file: " + e.getMessage());
                }
                
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
}
