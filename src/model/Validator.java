package model;

/**
 * Validator — all input validation rules.
 * Username comparison is CASE-SENSITIVE: "Admin" and "admin" are different users.
 */
public class Validator {

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    // ── Username: 3–20 chars, letters/digits/underscore, CASE-SENSITIVE ──────────
    public static boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    // ── Email format check ────────────────────────────────────────────────────────
    public static boolean isValidEmail(String email) {
        return email != null &&
            email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[A-Za-z]{2,}$");
    }

    // ── Password: minimum 6 characters ───────────────────────────────────────────
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // ── Passwords match ───────────────────────────────────────────────────────────
    public static boolean passwordsMatch(String p1, String p2) {
        return p1 != null && p1.equals(p2);
    }

    // ── Login form validation ─────────────────────────────────────────────────────
    // Returns null if valid, error string if not.
    public static String validateLogin(String username, String password) {
        if (isEmpty(username)) return "Username is required.";
        if (isEmpty(password)) return "Password is required.";
        return null;
    }

    // ── Registration form validation ──────────────────────────────────────────────
    // Usernames are CASE-SENSITIVE — "Admin" and "admin" are treated as different.
    public static String validateRegistration(String username, String password,
                                              String confirmPassword, String email,
                                              String fullName, String role) {
        if (isEmpty(fullName))
            return "Full name is required.";
        if (!isValidUsername(username))
            return "Username must be 3–20 characters (letters, digits, underscore). Case-sensitive.";
        if (!isValidEmail(email))
            return "Enter a valid email address.";
        if (!isValidPassword(password))
            return "Password must be at least 6 characters.";
        if (!passwordsMatch(password, confirmPassword))
            return "Passwords do not match.";
        if (isEmpty(role))
            return "Please select a role.";
        return null;
    }

    // ── Equipment form validation ─────────────────────────────────────────────────
    public static String validateEquipment(String name, String category, String serial) {
        if (isEmpty(name))     return "Equipment name is required.";
        if (isEmpty(category)) return "Category is required.";
        if (isEmpty(serial))   return "Serial number is required.";
        return null;
    }
}
