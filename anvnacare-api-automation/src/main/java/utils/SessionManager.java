package utils;

import config.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SessionManager is responsible for managing authentication sessions via PHPSESSID.
 * 
 * Why do we need it?
 * Many healthcare APIs require the user to be logged in. Rather than repeating login code 
 * in every test class, this manager automatically performs login, retrieves the PHPSESSID 
 * session cookie, and caches it.
 * 
 * Where is it used?
 * Used by BaseAPI, Endpoints, and Tests to authenticate requests requiring active user sessions.
 * 
 * Why is this approach better?
 * Caching session cookies in a ConcurrentHashMap keyed by role ("user" or "admin") avoids 
 * making redundant login requests during parallel test runs, making tests faster and cleaner.
 */
public class SessionManager {

    private static final Logger logger = LogManager.getLogger(SessionManager.class);
    
    // Concurrent map to hold cached cookies per role for thread safety during parallel runs
    private static final Map<String, String> sessionCookies = new ConcurrentHashMap<>();

    /**
     * Gets the PHPSESSID cookie value for a given role (e.g., "user" or "admin").
     * If the session doesn't exist, it performs login to establish one.
     * 
     * @param role User role name ("user" or "admin")
     * @return PHPSESSID cookie string
     */
    public static String getSessionCookie(String role) {
        String env = ConfigReader.getEnv();
        String mapKey = env + "_" + role.toLowerCase();
        
        return sessionCookies.computeIfAbsent(mapKey, key -> loginAndGetSessionCookie(role));
    }

    /**
     * Clears all cached session cookies.
     */
    public static void clearAllSessions() {
        sessionCookies.clear();
        logger.info("All cached session cookies cleared.");
    }

    /**
     * Helper method to call the login API and extract the PHPSESSID cookie.
     */
    private static String loginAndGetSessionCookie(String role) {
        String baseUrl = ConfigReader.getBaseUrl();
        String loginUrl = baseUrl + "api/login.php";
        String env = ConfigReader.getEnv();

        logger.info("Session not found. Initializing login flow for Role: {} in Environment: {}", role, env);

        // Fetch credentials from config based on role
        String email = ConfigReader.getProperty(env + "." + role.toLowerCase() + ".email");
        String password = ConfigReader.getProperty(env + "." + role.toLowerCase() + ".password");

        if (email == null || password == null) {
            throw new IllegalArgumentException("No credentials configured for environment: " + env + " and role: " + role);
        }

        // Construct request payload
        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("email", email);
        loginPayload.put("password", password);
        loginPayload.put("remember", true);

        // Send POST login request
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(loginPayload)
                .post(loginUrl);

        if (response.getStatusCode() == 200 && response.jsonPath().getBoolean("success")) {
            String phpSessId = response.getCookie("PHPSESSID");
            if (phpSessId == null || phpSessId.isEmpty()) {
                throw new RuntimeException("Login succeeded but no PHPSESSID cookie was returned.");
            }
            logger.info("Successfully established session for {}. PHPSESSID cookie extracted.", role);
            return phpSessId;
        } else {
            logger.error("Failed to authenticate session for {}. HTTP Status: {}, Response: {}", 
                    role, response.getStatusCode(), response.asString());
            throw new RuntimeException("Authentication failed for role: " + role + ". Message: " + response.jsonPath().getString("message"));
        }
    }
}
