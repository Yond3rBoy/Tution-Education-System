import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Ensure the GUI is created on the Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
