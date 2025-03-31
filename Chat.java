import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.*;
import java.util.List;

public class Chat extends JFrame {
    private JPanel chatPanel, contactsPanel;
    private JTextField searchField, messageField;
    private JScrollPane chatScrollPane;
    private JLabel chatTitle;
    private Map<String, List<JComponent>> chatHistory;
    private String currentChat;
    private User currentUser;
    private DatabaseManager dbManager;
    
    public Chat(User user) {
        this.currentUser = user;
        this.dbManager = new DatabaseManager();
        
        setTitle("Chat Application");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        chatHistory = new HashMap<>();

        // Left Panel (Contacts + Settings)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(270, getHeight()));
        leftPanel.setBackground(new Color(230, 236, 240));

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        searchPanel.setBackground(Color.WHITE);

        searchField = new JTextField("Search...");
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(null);
        searchField.setForeground(Color.GRAY);

        // Placeholder Logic
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search...");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        searchPanel.add(searchField, BorderLayout.CENTER);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        // Contacts Panel
        contactsPanel = new JPanel();
        contactsPanel.setLayout(new BoxLayout(contactsPanel, BoxLayout.Y_AXIS));
        leftPanel.add(new JScrollPane(contactsPanel), BorderLayout.CENTER);
        loadContactsFromDatabase();

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterContacts(searchField.getText().trim());
            }
        });

        // Settings Button
        JButton settingsButton = new JButton("Settings ⚙️");
        settingsButton.addActionListener(e -> openSettings());
        settingsButton.setBorder(new EmptyBorder(10, 10, 10, 10));

        leftPanel.add(settingsButton, BorderLayout.SOUTH);

        // Chat Area
        JPanel chatContainer = new JPanel(new BorderLayout());
        chatContainer.setBackground(Color.WHITE);

        chatTitle = new JLabel("Select a contact", SwingConstants.CENTER);
        chatTitle.setFont(new Font("Arial", Font.BOLD, 16));
        chatTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatContainer.add(chatTitle, BorderLayout.NORTH);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);

        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setBorder(null);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatContainer.add(chatScrollPane, BorderLayout.CENTER);

        // Message Input Area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        messageField = new JTextField();
        inputPanel.add(messageField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        JButton attachButton = new JButton("📎");
        attachButton.addActionListener(e -> attachMedia());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(attachButton);
        buttonPanel.add(sendButton);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        add(leftPanel, BorderLayout.WEST);
        add(chatContainer, BorderLayout.CENTER);
        chatContainer.add(inputPanel, BorderLayout.SOUTH);
    }

    private void loadContactsFromDatabase() {
        List<User> contacts = dbManager.getAllContacts(currentUser.getUserId());
        populateContacts(contacts);
    }

    private void populateContacts(List<User> contacts) {
        contactsPanel.removeAll();
        for (User contact : contacts) {
            JPanel contactPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            contactPanel.setBorder(new EmptyBorder(5, 10, 5, 10)); // Reduced vertical padding (8→5)
            contactPanel.setBackground(new Color(230, 236, 240));
            contactPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Set preferred size with fixed height
            contactPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // Add this line
            
            // Load profile picture
            String imagePath = contact.getProfilePicPath();
            if (imagePath == null || imagePath.isEmpty()) {
                imagePath = "profile_pics/profile.png"; // Default image
            }
            
            ImageIcon profileIcon = new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            JLabel profileLabel = new JLabel(profileIcon);
            profileLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
    
            JLabel nameLabel = new JLabel(contact.getUsername());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
    
            contactPanel.add(profileLabel);
            contactPanel.add(nameLabel);
    
            contactPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    openChat(contact);
                }
            });
    
            contactsPanel.add(contactPanel);
            contactsPanel.add(Box.createRigidArea(new Dimension(0, 1))); // Add small spacing
        }
        contactsPanel.revalidate();
        contactsPanel.repaint();
    }
    
    private void filterContacts(String query) {
        if (query.isEmpty() || query.equals("Search...")) {
            loadContactsFromDatabase();
            return;
        }

        List<User> filteredContacts = dbManager.searchContacts(currentUser.getUserId(), query);
        populateContacts(filteredContacts);
    }

    private void openChat(User contact) {
        ImageIcon icon = new ImageIcon(contact.getProfilePicPath()); // Load image
        Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH); // Resize
        icon = new ImageIcon(img); // Set resized image

        chatTitle.setText(contact.getUsername());
        chatTitle.setIcon(icon);
        currentChat = String.valueOf(contact.getUserId());
        chatPanel.removeAll();

        // Load messages from database
        List<Message> messages = dbManager.getMessages(currentUser.getUserId(), contact.getUserId());
        displayMessages(messages);
    }

    private void displayMessages(List<Message> messages) {
        chatHistory.computeIfAbsent(currentChat, k -> new ArrayList<>());
        
        
        for (Message message : messages) {
            JPanel messagePanel = new JPanel();
            boolean isSentByCurrentUser = message.getSenderId() == currentUser.getUserId();
            
            if (isSentByCurrentUser) {
                messagePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            } else {
                messagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            }
            
            messagePanel.setBorder(new EmptyBorder(3, 5, 3, 5));
            messagePanel.setBackground(Color.WHITE);
            messagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            if (message.getType().equals("FILE") || message.getType().equals("IMAGE")) {
                messagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
            } else {
                messagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            }
            
            // Get profile picture path
            String picPath = isSentByCurrentUser ? currentUser.getProfilePicPath() : 
                             dbManager.getUserById(message.getSenderId()).getProfilePicPath();
            
            if (picPath == null || picPath.isEmpty()) {
                picPath = "profile_pics/profile.png"; // Default image
            }
            
            // Create profile picture component
            ImageIcon originalIcon = new ImageIcon(picPath);
            Image scaledImage = originalIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            ImageIcon profileIcon = new ImageIcon(scaledImage);
            
            JLabel profileLabel = new JLabel(profileIcon);
            profileLabel.setPreferredSize(new Dimension(40, 40));
            profileLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            
            // Check if it's a text message or media
            if (message.getType().equals("TEXT")) {
                String bubbleColor = isSentByCurrentUser ? "#0078FF" : "#E5E5EA";
                String textColor = isSentByCurrentUser ? "white" : "black";
                
                JLabel messageLabel = new JLabel("<html><div style='padding:10px; background:" + 
                                               bubbleColor + "; color:" + textColor + 
                                               "; border-radius:15px; max-width:250px;'>" + 
                                               message.getContent() + "</div></html>");
                
                if (isSentByCurrentUser) {
                    messagePanel.add(messageLabel);
                    messagePanel.add(profileLabel);
                    messagePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
                } else {
                    messagePanel.add(profileLabel);
                    messagePanel.add(messageLabel);
                    messagePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
                }
            } else if (message.getType().equals("IMAGE")) {
                // Display image
                ImageIcon imageIcon = new ImageIcon(new ImageIcon(message.getContent()).getImage()
                                     .getScaledInstance(150, 100, Image.SCALE_SMOOTH));
                JLabel imageLabel = new JLabel(imageIcon);
                
                if (isSentByCurrentUser) {
                    messagePanel.add(imageLabel);
                    messagePanel.add(profileLabel);
                } else {
                    messagePanel.add(profileLabel);
                    messagePanel.add(imageLabel);
                }
            }
            // Inside displayMessages method, add handling for FILE type
else if (message.getType().equals("FILE")) {
    // Extract file name from path
    String filePath = message.getContent();
    File file = new File(filePath);
    String fileName = file.getName();
    
    // Create file representation
    JPanel fileIconPanel = new JPanel();
    fileIconPanel.setLayout(new BoxLayout(fileIconPanel, BoxLayout.Y_AXIS));
    fileIconPanel.setBackground(new Color(240, 240, 240));
    fileIconPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    fileIconPanel.setPreferredSize(new Dimension(150, 80));
    
    JLabel fileIconLabel = new JLabel("📄");
    fileIconLabel.setFont(new Font("Arial", Font.PLAIN, 24));
    fileIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    JLabel fileNameLabel = new JLabel(fileName);
    fileNameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    fileNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    fileIconPanel.add(Box.createVerticalGlue());
    fileIconPanel.add(fileIconLabel);
    fileIconPanel.add(fileNameLabel);
    fileIconPanel.add(Box.createVerticalGlue());
    
    // Add click behavior
    fileIconPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    fileIconPanel.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                Desktop.getDesktop().open(new File(filePath));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(Chat.this,
                        "Cannot open file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });
    
    if (isSentByCurrentUser) {
        messagePanel.add(fileIconPanel);
        messagePanel.add(profileLabel);
    } else {
        messagePanel.add(profileLabel);
        messagePanel.add(fileIconPanel);
    }
}
            
            chatPanel.add(messagePanel);
            chatHistory.get(currentChat).add(messagePanel);
        }
        
        chatPanel.revalidate();
        chatPanel.repaint();
        
        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void sendMessage() {
        if (currentChat == null) return;
    
        String text = messageField.getText().trim();
        if (!text.isEmpty()) {
            int recipientId = Integer.parseInt(currentChat);
            
            // Save message to database
            Message message = new Message(0, currentUser.getUserId(), recipientId, text, "TEXT", new Date());
            dbManager.saveMessage(message);
            
            // Display message in UI
            JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            messagePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            messagePanel.setBackground(Color.WHITE);
    
            // Profile Picture
            ImageIcon originalIcon = new ImageIcon(currentUser.getProfilePicPath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            ImageIcon profileIcon = new ImageIcon(scaledImage);
            
            JLabel profileLabel = new JLabel(profileIcon);
            profileLabel.setPreferredSize(new Dimension(40, 40));
            profileLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    
            // Message Label
            JLabel messageLabel = new JLabel("<html><div style='padding:10px; background:#0078FF; color:white; border-radius:15px; max-width:250px;'>" + text + "</div></html>");
            messageLabel.setOpaque(true);
            
            // Adding elements to panel
            messagePanel.add(messageLabel);
            messagePanel.add(profileLabel); // Profile on the right
            
            chatPanel.add(messagePanel);
            chatPanel.revalidate();
            chatPanel.repaint();
            messageField.setText("");
    
            chatHistory.computeIfAbsent(currentChat, k -> new ArrayList<>()).add(messagePanel);
            
            // Scroll to bottom
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        }
    }

    private void attachMedia() {
    if (currentChat == null) return;

    JFileChooser fileChooser = new JFileChooser();
    // Create a filter that accepts all files but shows image files as a convenient option
    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
    fileChooser.setAcceptAllFileFilterUsed(true); // Allow all file types
    int returnValue = fileChooser.showOpenDialog(this);
    
    if (returnValue == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        String filePath = selectedFile.getAbsolutePath();
        String fileName = selectedFile.getName();
        
        // Determine if it's an image or another file type
        String fileExtension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            fileExtension = fileName.substring(i+1).toLowerCase();
        }
        
        boolean isImage = fileExtension.equals("jpg") || fileExtension.equals("jpeg") || 
                           fileExtension.equals("png") || fileExtension.equals("gif");
        
        // Save to database with appropriate type
        int recipientId = Integer.parseInt(currentChat);
        String messageType = isImage ? "IMAGE" : "FILE";
        Message message = new Message(0, currentUser.getUserId(), recipientId, filePath, messageType, new Date());
        dbManager.saveMessage(message);
        
        // Create UI elements
        JPanel attachmentPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        attachmentPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        attachmentPanel.setBackground(Color.WHITE);
        attachmentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        attachmentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // fileIconPanel.setPreferredSize(new Dimension(150, 100)); // Increase from 80 to 100
        
        // Create profile picture
        ImageIcon profileOriginalIcon = new ImageIcon(currentUser.getProfilePicPath());
        Image profileScaledImage = profileOriginalIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon profileIcon = new ImageIcon(profileScaledImage);
        
        JLabel profileLabel = new JLabel(profileIcon);
        profileLabel.setPreferredSize(new Dimension(40, 40));
        profileLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        
        // Display based on file type
        if (isImage) {
            // Handle image as before
            ImageIcon originalImageIcon = new ImageIcon(filePath);
            if (originalImageIcon.getIconWidth() <= 0) {
                System.out.println("Error loading image: " + filePath);
                // Show error message
                JOptionPane.showMessageDialog(this, "Could not load image file", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Image scaledImage = originalImageIcon.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
            ImageIcon imageIcon = new ImageIcon(scaledImage);
            JLabel imageLabel = new JLabel(imageIcon);
            imageLabel.setPreferredSize(new Dimension(150, 100));
            
            attachmentPanel.add(imageLabel);
        } else {
            // Create a file attachment representation
            JPanel fileIconPanel = new JPanel();
            fileIconPanel.setLayout(new BoxLayout(fileIconPanel, BoxLayout.Y_AXIS));
            fileIconPanel.setBackground(new Color(240, 240, 240));
            fileIconPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            fileIconPanel.setPreferredSize(new Dimension(150, 80));
            
            // File icon (can be improved with file type-specific icons)
            JLabel fileIconLabel = new JLabel("📄");
            fileIconLabel.setFont(new Font("Arial", Font.PLAIN, 24));
            fileIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // File name
            JLabel fileNameLabel = new JLabel(fileName);
            fileNameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            fileNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            fileIconPanel.add(Box.createVerticalGlue());
            fileIconPanel.add(fileIconLabel);
            fileIconPanel.add(fileNameLabel);
            fileIconPanel.add(Box.createVerticalGlue());
            
            // Add a clickable behavior to open the file
            fileIconPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            fileIconPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        // Attempt to open the file with default system application
                        Desktop.getDesktop().open(new File(filePath));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Chat.this,
                                "Cannot open file: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            attachmentPanel.add(fileIconPanel);
        }
        
        // Add profile picture
        attachmentPanel.add(profileLabel);
        
        // Store and display the panel
        chatHistory.computeIfAbsent(currentChat, k -> new ArrayList<>()).add(attachmentPanel);
        chatPanel.add(attachmentPanel);
        chatPanel.revalidate();
        chatPanel.repaint();
        
        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
        }
    }
    
    private void openSettings() {
        JDialog settingsDialog = new JDialog(this, "User Settings", true);
        settingsDialog.setSize(350, 300);
        settingsDialog.setLayout(new GridLayout(4, 2, 10, 10));

        // Username
        settingsDialog.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField(currentUser.getUsername());
        settingsDialog.add(usernameField);

        // Profile Picture
        settingsDialog.add(new JLabel("Profile Picture:"));
        JButton chooseImage = new JButton("Choose File");
        chooseImage.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                currentUser.setProfilePicPath(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        settingsDialog.add(chooseImage);

        // Password
        settingsDialog.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField(currentUser.getPassword());
        settingsDialog.add(passwordField);

        // Save Button
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            currentUser.setUsername(usernameField.getText());
            currentUser.setPassword(new String(passwordField.getPassword()));
            
            // Update user in database
            dbManager.updateUser(currentUser);
            
            settingsDialog.dispose();
        });
        settingsDialog.add(saveButton);

        settingsDialog.setVisible(true);
    }
}