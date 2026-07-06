package payloads;

import java.util.UUID;
import models.request.RegisterRequest;

/**
 * RegisterPayloads provides factory methods to generate RegisterRequest payloads.
 * 
 * Why do we need it?
 * Decouples test code from payload data configurations.
 * 
 * Why is this approach better?
 * In automated API environments, registering the same email multiple times leads to "Email already exists" failures. 
 * Using UUIDs and random generation helpers allows us to obtain unique payloads for positive registration 
 * test cases automatically, ensuring tests are independent and repeatable.
 */
public class RegisterPayloads {

    /**
     * Generates a fully unique user registration payload.
     */
    public static RegisterRequest getUniqueRegisterPayload() {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        String name = "Automation User " + randomSuffix;
        String email = "auto." + randomSuffix + "@anvnacare.com";
        
        // Random 10-digit phone number
        long randomPhone = (long) (Math.random() * 9000000000L) + 1000000000L;
        String phone = String.valueOf(randomPhone);
        
        return new RegisterRequest(name, email, phone, "password123");
    }

    /**
     * Generates a registration payload with specific parameters.
     */
    public static RegisterRequest getCustomRegisterPayload(String name, String email, String phone, String password) {
        return new RegisterRequest(name, email, phone, password);
    }

    /**
     * Generates a payload with an invalid/short password.
     */
    public static RegisterRequest getInvalidPasswordRegisterPayload() {
        RegisterRequest payload = getUniqueRegisterPayload();
        payload.setPassword("123"); // Less than 6 characters boundary
        return payload;
    }

    /**
     * Generates a payload with an invalid/short phone number.
     */
    public static RegisterRequest getInvalidPhoneRegisterPayload() {
        RegisterRequest payload = getUniqueRegisterPayload();
        payload.setPhone("12345"); // Invalid pattern (must be 10 digits)
        return payload;
    }
}
