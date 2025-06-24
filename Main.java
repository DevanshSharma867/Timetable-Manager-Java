public class Main {
    public static void main(String[] args) {
        // Make the GUI use the system's look and feel
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Run the application on the EDT
        javax.swing.SwingUtilities.invokeLater(() -> {
            new TimetableManagementSystem();
        });
    }
}
    