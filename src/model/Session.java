package model;

public class Session {
    private static UserModel currentUser;

    public static void setCurrentUser(UserModel user) { currentUser = user; }
    public static UserModel getCurrentUser()          { return currentUser; }
    public static boolean   isLoggedIn()              { return currentUser != null; }
    public static boolean   isAdmin()                 { return currentUser != null && currentUser.isAdmin(); }
    public static void      logout()                  { currentUser = null; }
}
