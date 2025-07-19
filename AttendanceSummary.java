import java.text.DecimalFormat;

public class AttendanceSummary {
    private final String courseId; // <-- ADD THIS
    private final String courseName;
    private final int attendedClasses;
    private final int totalClasses;

    public AttendanceSummary(String courseId, String courseName, int attendedClasses, int totalClasses) {
        this.courseId = courseId; // <-- ADD THIS
        this.courseName = courseName;
        this.attendedClasses = attendedClasses;
        this.totalClasses = totalClasses;
    }

    public String getCourseId() { return courseId; } // <-- ADD THIS
    public String getCourseName() { return courseName; }
    public int getAttendedClasses() { return attendedClasses; }
    public int getTotalClasses() { return totalClasses; }
    public double getPercentage() { /* ... no change here ... */ 
        if (totalClasses == 0) return 0.0;
        return ((double) attendedClasses / totalClasses) * 100.0;
    }
    public String getPercentageString() { /* ... no change here ... */ 
        return new DecimalFormat("0.#").format(getPercentage()) + "%";
    }
}