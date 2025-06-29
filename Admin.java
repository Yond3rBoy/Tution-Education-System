import java.io.*;
import java.util.Scanner;

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
