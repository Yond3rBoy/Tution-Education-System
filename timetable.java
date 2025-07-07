import java.util.*;

public class timetable {
    public static void main(String[] args) {
        // List of subjects
        String[] subjects = {"Math", "Science", "History", "English", "PE"};
        
        // Define how many times each subject should appear
        Map<String, Integer> subjectCount = new HashMap<>();
        subjectCount.put("Math", 3);     // Math should appear 3 times
        subjectCount.put("Science", 5);  // Science should appear 5 times
        subjectCount.put("History", 4);  // History should appear 4 times
        subjectCount.put("English", 7);  // English should appear 7 times
        subjectCount.put("PE", 6);       // PE should appear 6 times
        
        // Days of the week
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        
        // Time slots (start time - end time) for each period
        String[] timeSlots = {
            "08:00 AM - 09:00 AM", "09:00 AM - 10:00 AM", "10:00 AM - 11:00 AM",
            "11:00 AM - 12:00 PM", "12:00 PM - 01:00 PM"
        };
        
        // Create a list to hold all the subjects to be assigned to the timetable
        List<String> timetableSubjects = new ArrayList<>();
        
        // Add the subjects to the list based on how many times they should appear
        for (String subject : subjects) {
            int count = subjectCount.get(subject);
            for (int i = 0; i < count; i++) {
                timetableSubjects.add(subject);
            }
        }

        // Shuffle the list to randomize the assignment
        Collections.shuffle(timetableSubjects);
        
        // Create a timetable (5 days x 5 periods = 25 slots)
        String[][] timetable = new String[5][5];
        
        // Assign shuffled subjects to the timetable
        int index = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                timetable[i][j] = timetableSubjects.get(index++);
            }
        }

        // Print the timetable with time slots in a grid format
        System.out.println("Your Timetable with Time Slots (3rd Column Recessed):");

        // Header with Time Slots
        System.out.print("          "); // Empty space for the first column (Time Slots)
        for (int i = 0; i < 5; i++) {
            System.out.printf("%-18s", timeSlots[i]); // Print time slots with some space
        }
        System.out.println(); // New line after header

        // Print the timetable for each day with corresponding subjects
        for (int i = 0; i < 5; i++) {
            System.out.printf("%-10s", days[i]); // Print day name (e.g., Monday)
            for (int j = 0; j < 5; j++) {
                if (j == 2) {
                    // Skip the 3rd column (leave it empty)
                    System.out.print("                 "); // Empty space for 3rd column
                } else {
                    // Print subject name in each period
                    System.out.printf("%-18s", timetable[i][j]);
                }
            }
            System.out.println(); // New line after each day
        }
    }
}
