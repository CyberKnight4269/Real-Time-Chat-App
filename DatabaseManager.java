import java.sql.*;
import java.util.*;
import javax.swing.JOptionPane;
import java.util.Date;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/chatdb";
    private static final String USER = "root";
    private static final String PASS = "divyansh@mysql12"; 
    
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found", "Database Error", JOptionPane.ERROR_MESSAGE);
            throw new SQLException("JDBC Driver not found", e);
        }
    }
    
    public void createTablesIfNotExist() {
        try (Connection conn = getConnection()) {
            // Create users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "profile_pic_path VARCHAR(255)" +
                    ")";
            
            // Create messages table
            String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages (" +
                    "message_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "sender_id INT NOT NULL, " +
                    "receiver_id INT NOT NULL, " +
                    "content TEXT NOT NULL, " + 
                    "type VARCHAR(10) NOT NULL, " + 
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (sender_id) REFERENCES users(user_id), " +
                    "FOREIGN KEY (receiver_id) REFERENCES users(user_id)" +
                    ")";
            
            Statement stmt = conn.createStatement();
            stmt.execute(createUsersTable);
            stmt.execute(createMessagesTable);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error creating database tables: " + e.getMessage(), 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, email, password, profile_pic_path) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword()); // In a real app, hash this password
            pstmt.setString(4, user.getProfilePicPath());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password); 
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String email = rs.getString("email");
                String profilePicPath = rs.getString("profile_pic_path");
                
                return new User(userId, username, email, password, profilePicPath);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password = ?, profile_pic_path = ? WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getProfilePicPath());
            pstmt.setInt(5, user.getUserId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<User> getAllContacts(int currentUserId) {
        List<User> contacts = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE user_id != ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String profilePicPath = rs.getString("profile_pic_path");
                
                User user = new User(userId, username, email, password, profilePicPath);
                contacts.add(user);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return contacts;
    }
    
    public List<User> searchContacts(int currentUserId, String query) {
        List<User> filteredContacts = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE user_id != ? AND username LIKE ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String profilePicPath = rs.getString("profile_pic_path");
                
                User user = new User(userId, username, email, password, profilePicPath);
                filteredContacts.add(user);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return filteredContacts;
    }
    
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String profilePicPath = rs.getString("profile_pic_path");
                
                return new User(userId, username, email, password, profilePicPath);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, type) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, message.getSenderId());
            pstmt.setInt(2, message.getReceiverId());
            pstmt.setString(3, message.getContent());
            pstmt.setString(4, message.getType());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Message> getMessages(int user1Id, int user2Id) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE (sender_id = ? AND receiver_id = ?) " +
                     "OR (sender_id = ? AND receiver_id = ?) ORDER BY timestamp ASC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, user1Id);
            pstmt.setInt(2, user2Id);
            pstmt.setInt(3, user2Id);
            pstmt.setInt(4, user1Id);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int messageId = rs.getInt("message_id");
                int senderId = rs.getInt("sender_id");
                int receiverId = rs.getInt("receiver_id");
                String content = rs.getString("content");
                String type = rs.getString("type");
                Date timestamp = rs.getTimestamp("timestamp");
                
                Message message = new Message(messageId, senderId, receiverId, content, type, timestamp);
                messages.add(message);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }
}