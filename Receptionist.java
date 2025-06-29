import java.io.*;
import java.util.Scanner;

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
