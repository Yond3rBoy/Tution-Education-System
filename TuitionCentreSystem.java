import java.util.Scanner;

public class TuitionCentreSystem {
    private static int attempts = 0;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            if (attempts >= 3) {
                System.out.println("Too many failed attempts. Program will now exit.");
                break;
            }

            System.out.println("==== Tuition Centre Management System ====");
            System.out.print("Username: ");
            String username = sc.nextLine();
            System.out.print("Password: ");
            String password = sc.nextLine();

            User user = AuthService.login(username, password);
            if (user != null) {
                attempts = 0; // Reset attempts on successful login
                user.displayMenu();
                System.out.print("Do you want to exit? (y/n): ");
                String exitChoice = sc.nextLine();
                if (exitChoice.equalsIgnoreCase("y")) {
                    System.out.println("Exiting system. Goodbye!");
                    break;
                }
            } else {
                attempts++;
                System.out.println("Invalid credentials. Attempt " + attempts + " of 3.");
            }
        }
    }
}