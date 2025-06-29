import java.io.*;
import java.util.Scanner;

class AuthService {
    public static User login(String username, String password) {
        if (checkCredentials("admins.txt", username, password)) {
            return new Admin(username, password);
        } else if (checkCredentials("receptionists.txt", username, password)) {
            return new Receptionist(username, password);
        } else if (checkCredentials("tutors.txt", username, password)) {
            return new Tutor(username, password);
        } else if (checkCredentials("students.txt", username, password)) {
            return new Student(username, password);
        }
        return null;
    }

    private static boolean checkCredentials(String filename, String username, String password) {
        try (Scanner sc = new Scanner(new File(filename))) {
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",");
                if (parts.length >= 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("File not found: " + filename);
        }
        return false;
    }
}
