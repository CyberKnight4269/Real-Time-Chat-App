import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            // Create database tables if they don't exist
            DatabaseManager dbManager = new DatabaseManager();
            dbManager.createTablesIfNotExist();
            
            // Start with login screen
            new Login().setVisible(true);
        });
    }
}