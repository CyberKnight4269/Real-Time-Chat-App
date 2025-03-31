public class User {
    private int userId;
    private String username;
    private String email;
    private String password;
    private String profilePicPath;
    
    public User(int userId, String username, String email, String password, String profilePicPath) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.profilePicPath = profilePicPath;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getProfilePicPath() {
        return profilePicPath;
    }
    
    public void setProfilePicPath(String profilePicPath) {
        this.profilePicPath = profilePicPath;
    }
    
    @Override
    public String toString() {
        return username;
    }
}