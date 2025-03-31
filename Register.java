import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class Register extends JFrame {
    private JTextField usernameField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JLabel profileImageLabel;
    private String profileImagePath = "profile_pics/profile.png"; // Default image
    private DatabaseManager dbManager;
    
    public Register() {
        dbManager = new DatabaseManager();
        
        setTitle("Chat App Registration");
        setSize(450, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // App Title
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Profile Image
        JPanel imagePanel = new JPanel();
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        ImageIcon defaultIcon = new ImageIcon(profileImagePath);
        Image scaledImage = defaultIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        
        profileImageLabel = new JLabel(scaledIcon);
        profileImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JButton chooseImageButton = new JButton("Choose Profile Picture");
        chooseImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseProfileImage();
            }
        });
        
        imagePanel.add(profileImageLabel);
        
        // Username Field
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, usernameField.getPreferredSize().height));
        
        // Email Field
        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, emailField.getPreferredSize().height));
        
        // Password Field
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, passwordField.getPreferredSize().height));
        
        // Confirm Password Field
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, confirmPasswordField.getPreferredSize().height));
        
        // Register Button
        JButton registerButton = new JButton("Register");
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setBackground(new Color(30, 144, 255));
        registerButton.setForeground(Color.BLACK);
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });
        
        // Login Link
        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel loginLabel = new JLabel("Already have an account?");
        JButton loginButton = new JButton("Login");
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setForeground(new Color(30, 144, 255));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openLogin();
            }
        });
        loginPanel.add(loginLabel);
        loginPanel.add(loginButton);
        
        // Add components to main panel
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(imagePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(chooseImageButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(usernameLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(emailLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(emailField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(passwordLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(confirmPasswordLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(confirmPasswordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(registerButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(loginPanel);
        
        add(mainPanel);
    }
    
    private void chooseProfileImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            profileImagePath = selectedFile.getAbsolutePath();
            
            // Update image preview
            ImageIcon newIcon = new ImageIcon(profileImagePath);
            Image scaledImage = newIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            profileImageLabel.setIcon(new ImageIcon(scaledImage));
        }
    }
    
    private void register() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Basic validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if username already exists
        if (dbManager.userExists(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create user
        User newUser = new User(0, username, email, password, profileImagePath);
        boolean success = dbManager.registerUser(newUser);
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Registration", JOptionPane.INFORMATION_MESSAGE);
            openLogin();
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Please try again.", "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    
    private void openLogin() {
        this.dispose();
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new Register().setVisible(true));
    }
}