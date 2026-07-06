package payloads;

import models.request.LoginRequest;

/**
 * LoginPayloads is a factory/builder class that generates LoginRequest instances.
 * 
 * Why do we need it?
 * Separates payload definitions from test cases. Instead of creating and editing LoginRequest 
 * instances directly inside our TestNG test files, we call helper methods from this class.
 * 
 * Where is it used?
 * Called by Test classes (e.g. LoginTests) during the "Arrange" phase of the AAA pattern.
 * 
 * Why is this approach better?
 * If the request schema changes (e.g. adding a new field), we only update the payload creation 
 * in this class rather than editing multiple test files.
 */
public class LoginPayloads {

    /**
     * Creates a login payload using custom email, password, and remember flag.
     */
    public static LoginRequest getLoginPayload(String email, String password, boolean remember) {
        return new LoginRequest(email, password, remember);
    }

    /**
     * Creates a default valid user login payload.
     */
    public static LoginRequest getValidUserLoginPayload() {
        return new LoginRequest("amit.kumar@anvnacare.com", "password123", true);
    }

    /**
     * Creates a default valid admin login payload.
     */
    public static LoginRequest getValidAdminLoginPayload() {
        return new LoginRequest("admin@anvnacare.com", "password123", true);
    }

    /**
     * Creates a login payload with an empty email (for negative boundary testing).
     */
    public static LoginRequest getEmptyEmailLoginPayload() {
        return new LoginRequest("", "password123", true);
    }

    /**
     * Creates a login payload with an empty password (for negative boundary testing).
     */
    public static LoginRequest getEmptyPasswordLoginPayload() {
        return new LoginRequest("amit.kumar@anvnacare.com", "", true);
    }
}
