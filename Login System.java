import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;

abstract class User {
    protected String username;
    protected String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public abstract void displayMenu();
}

class Admin extends User {
    public Admin(String username, String password) {
        super(username, password);
    }

    public void displayMenu() {
        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            System.out.println("=== Admin Menu ===");
            System.out.println("1. Register Tutor");
            System.out.println("2. Delete Tutor");
            System.out.println("3. Register Receptionist");
            System.out.println("4. Delete Receptionist");
            System.out.println("5. Update Profile");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> registerUser("tutors.txt", sc);
                case 2 -> deleteUser("tutors.txt", sc);
                case 3 -> registerUser("receptionists.txt", sc);
                case 4 -> deleteUser("receptionists.txt", sc);
                case 5 -> updateProfile("admins.txt", sc);
            }
        } while (choice != 0);
        System.out.println("Logged out. Returning to login...");
    }

    private void registerUser(String filename, Scanner sc) {
        try (FileWriter fw = new FileWriter(filename, true)) {
            System.out.print("Enter username: ");
            String user = sc.nextLine();
            System.out.print("Enter password: ");
            String pass = sc.nextLine();
            fw.write(user + "," + pass + "\n");
            System.out.println("User registered.");
        } catch (IOException e) {
            System.out.println("Error writing file.");
        }
    }

    private void deleteUser(String filename, Scanner sc) {
        System.out.print("Enter username to delete: ");
        String userToDelete = sc.nextLine();
        try {
            File inputFile = new File(filename);
            File tempFile = new File("temp.txt");
            Scanner reader = new Scanner(inputFile);
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (!line.startsWith(userToDelete + ",")) {
                    writer.println(line);
                }
            }

            reader.close();
            writer.close();
            inputFile.delete();
            tempFile.renameTo(inputFile);
            System.out.println("User deleted if existed.");
        } catch (IOException e) {
            System.out.println("Error deleting user.");
        }
    }

    private void updateProfile(String filename, Scanner sc) {
        System.out.print("Enter new password: ");
        String newPass = sc.nextLine();
        try {
            File inputFile = new File(filename);
            File tempFile = new File("temp.txt");
            Scanner reader = new Scanner(inputFile);
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (line.startsWith(username + ",")) {
                    writer.println(username + "," + newPass);
                } else {
                    writer.println(line);
                }
            }

            reader.close();
            writer.close();
            inputFile.delete();
            tempFile.renameTo(inputFile);
            System.out.println("Password updated.");
        } catch (IOException e) {
            System.out.println("Error updating profile.");
        }
    }
}

class Receptionist extends User {
    public Receptionist(String username, String password) {
        super(username, password);
    }

    public void displayMenu() {
        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            System.out.println("=== Receptionist Menu ===");
            System.out.println("1. Register Student");
            System.out.println("2. Delete Student");
            System.out.println("3. Update Profile");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> registerStudent(sc);
                case 2 -> deleteStudent(sc);
                case 3 -> updateProfile("receptionists.txt", sc);
            }
        } while (choice != 0);
        System.out.println("Logged out. Returning to login...");
    }

    private void registerStudent(Scanner sc) {
        try (FileWriter fw = new FileWriter("students.txt", true)) {
            System.out.print("Student username: ");
            String user = sc.nextLine();
            System.out.print("Password: ");
            String pass = sc.nextLine();
            fw.write(user + "," + pass + "\n");
            System.out.println("Student registered.");
        } catch (IOException e) {
            System.out.println("Error writing file.");
        }
    }

    private void deleteStudent(Scanner sc) {
        System.out.print("Enter student username to delete: ");
        String userToDelete = sc.nextLine();
        try {
            File inputFile = new File("students.txt");
            File tempFile = new File("temp.txt");
            Scanner reader = new Scanner(inputFile);
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (!line.startsWith(userToDelete + ",")) {
                    writer.println(line);
                }
            }

            reader.close();
            writer.close();
            inputFile.delete();
            tempFile.renameTo(inputFile);
            System.out.println("Student deleted if existed.");
        } catch (IOException e) {
            System.out.println("Error deleting student.");
        }
    }

    private void updateProfile(String filename, Scanner sc) {
        System.out.print("Enter new password: ");
        String newPass = sc.nextLine();
        try {
            File inputFile = new File(filename);
            File tempFile = new File("temp.txt");
            Scanner reader = new Scanner(inputFile);
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (line.startsWith(username + ",")) {
                    writer.println(username + "," + newPass);
                } else {
                    writer.println(line);
                }
            }

            reader.close();
            writer.close();
            inputFile.delete();
            tempFile.renameTo(inputFile);
            System.out.println("Password updated.");
        } catch (IOException e) {
            System.out.println("Error updating profile.");
        }
    }
}

class Tutor extends User {
    public Tutor(String username, String password) {
        super(username, password);
    }

    public void displayMenu() {
        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            System.out.println("=== Tutor Menu ===");
            System.out.println("1. Update Profile");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> updateProfile("tutors.txt", sc);
            }
        } while (choice != 0);
        System.out.println("Logged out. Returning to login...");
    }

    private void updateProfile(String filename, Scanner sc) {
        System.out.print("Enter new password: ");
        String newPass = sc.nextLine();
        try {
            File inputFile = new File(filename);
            File tempFile = new File("temp.txt");
            Scanner reader = new Scanner(inputFile);
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (line.startsWith(username + ",")) {
                    writer.println(username + "," + newPass);
                } else {
                    writer.println(line);
                }
            }

            reader.close();
            writer.close();
            inputFile.delete();
            tempFile.renameTo(inputFile);
            System.out.println("Password updated.");
        } catch (IOException e) {
            System.out.println("Error updating profile.");
        }
    }
}

class Student extends User {
    public Student(String username, String password) {
        super(username, password);
    }

    public void displayMenu() {
        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            System.out.println("=== Student Menu ===");
            System.out.println("1. Update Profile");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> updateProfile("students.txt", sc);
            }
        } while (choice != 0);
        System.out.println("Logged out. Returning to login...");
    }

    private void updateProfile(String filename, Scanner sc) {
        System.out.print("Enter new password: ");
        String newPass = sc.nextLine();
        try {
            File inputFile = new File(filename);
            File tempFile = new File("temp.txt");
            Scanner reader = new Scanner(inputFile);
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (line.startsWith(username + ",")) {
                    writer.println(username + "," + newPass);
                } else {
                    writer.println(line);
                }
            }

            reader.close();
            writer.close();
            inputFile.delete();
            tempFile.renameTo(inputFile);
            System.out.println("Password updated.");
        } catch (IOException e) {
            System.out.println("Error updating profile.");
        }
    }
}

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
        System.out.println("Invalid credentials. Try again.");
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

public class TuitionCentreSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("==== Tuition Centre Management System ====");
            System.out.print("Username: ");
            String username = sc.nextLine();
            System.out.print("Password: ");
            String password = sc.nextLine();

            User user = AuthService.login(username, password);
            if (user != null) {
                user.displayMenu(); // Returns to login after logout
            }
        }
    }
}
