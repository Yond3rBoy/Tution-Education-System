import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OverallGpa {
    private final List<ResultSummary> summaries;

    public OverallGpa(List<ResultSummary> summaries) {
        this.summaries = summaries;
    }

    public int getModulesPassed() {
        return (int) summaries.stream()
                .filter(s -> !s.getGradeLetter().equals("F"))
                .count();
    }

    public double calculateGPA() {
        if (summaries.isEmpty()) {
            return 0.0;
        }
        double totalPoints = summaries.stream()
                .mapToDouble(ResultSummary::getGradePoints)
                .sum();
        return totalPoints / summaries.size();
    }

    public Map<String, Long> getGradeDistribution() {
        return summaries.stream()
                .map(ResultSummary::getGradeLetter)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }
}