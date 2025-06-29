import java.io.*;
import java.util.Scanner;

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
