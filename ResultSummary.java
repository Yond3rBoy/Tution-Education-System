public class ResultSummary {
    private final String courseName;
    private final String gradeLetter;
    private final double gradePoints;

    public ResultSummary(String courseName, String gradeLetter, double gradePoints) {
        this.courseName = courseName;
        this.gradeLetter = gradeLetter;
        this.gradePoints = gradePoints;
    }

    public String getCourseName() { return courseName; }
    public String getGradeLetter() { return gradeLetter; }
    public double getGradePoints() { return gradePoints; }

}