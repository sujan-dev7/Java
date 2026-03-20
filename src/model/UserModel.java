package model;

public class UserModel {
    private int    id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String role;

    public UserModel() {}

    public UserModel(String username, String password, String email, String fullName, String role) {
        this.username = username; this.password = password;
        this.email = email;       this.fullName = fullName; this.role = role;
    }

    public int    getId()          { return id; }
    public String getUsername()    { return username; }
    public String getPassword()    { return password; }
    public String getEmail()       { return email; }
    public String getFullName()    { return fullName; }
    public String getRole()        { return role; }
    public boolean isAdmin()       { return "admin".equalsIgnoreCase(role); }

    public void setId(int id)           { this.id = id; }
    public void setUsername(String u)   { this.username = u; }
    public void setPassword(String p)   { this.password = p; }
    public void setEmail(String e)      { this.email = e; }
    public void setFullName(String n)   { this.fullName = n; }
    public void setRole(String r)       { this.role = r; }

    @Override public String toString() { return fullName + " (" + role + ")"; }
}
