package tests;

import constants.FrameworkConstants;
import endpoints.LoginAPI;
import endpoints.RegisterAPI;
import io.restassured.response.Response;
import models.request.LoginRequest;
import models.request.RegisterRequest;
import models.response.LoginResponse;
import models.response.RegisterResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.LoginPayloads;
import payloads.RegisterPayloads;

/**
 * RegisterTests verifies user account registration features.
 * 
 * Why do we need it?
 * To validate registration business logic, including valid creations, input validations, 
 * and API chaining scenarios.
 * 
 * API Chaining:
 * We test the interaction of multiple APIs together:
 * Register (POST) -> Login (POST) using the credentials from the first step to confirm user access.
 */
public class RegisterTests {

    private static final Logger logger = LogManager.getLogger(RegisterTests.class);

    @Test(description = "Verify successful registration of a new user with unique details")
    public void testSuccessfulRegistration() {
        logger.info("Starting Test: testSuccessfulRegistration");

        // 1. Arrange
        RegisterRequest payload = RegisterPayloads.getUniqueRegisterPayload();

        // 2. Act
        Response response = RegisterAPI.performRegistration(payload);

        // 3. Assert
        Assert.assertEquals(response.getStatusCode(), 200, "HTTP Status mismatch");
        
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // Validate JSON Schema
        response.then()
        	.assertThat()
        	.body(io.restassured.module.jsv.JsonSchemaValidator
        			.matchesJsonSchema(RegisterTests.class.getResourceAsStream("/schemas/register-response-schema.json")));

        RegisterResponse regResponse = response.as(RegisterResponse.class);
        
        Assert.assertTrue(regResponse.isSuccess(), "Success flag should be true");
        Assert.assertEquals(regResponse.getMessage(), "Registration successful.", "Message mismatch");
        Assert.assertNotNull(regResponse.getUser(), "User block should not be null");
        Assert.assertEquals(regResponse.getUser().getName(), payload.getName(), "Name mismatch");
        Assert.assertEquals(regResponse.getUser().getEmail(), payload.getEmail(), "Email mismatch");
        Assert.assertTrue(regResponse.getUser().getId() > 0, "User ID should be a positive number");

        logger.info("Finished Test: testSuccessfulRegistration - PASSED");
    }

    @Test(description = "Verify registration fails when password length is below 6 characters")
    public void testRegistrationFailsWithShortPassword() {
        logger.info("Starting Test: testRegistrationFailsWithShortPassword");

        // 1. Arrange
        RegisterRequest payload = RegisterPayloads.getInvalidPasswordRegisterPayload();

        // 2. Act
        Response response = RegisterAPI.performRegistration(payload);

        // 3. Assert
        Assert.assertEquals(response.getStatusCode(), 400, "HTTP Status should be 400");
        
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");

        RegisterResponse regResponse = response.as(RegisterResponse.class);
        
        Assert.assertFalse(regResponse.isSuccess(), "Success flag should be false for short password");
        Assert.assertEquals(regResponse.getMessage(), "Password must be at least 6 characters.", "Error message mismatch");
        
        logger.info("Finished Test: testRegistrationFailsWithShortPassword - PASSED");
    }

    @Test(description = "Verify registration fails when phone format is not a 10-digit number")
    public void testRegistrationFailsWithInvalidPhone() {
        logger.info("Starting Test: testRegistrationFailsWithInvalidPhone");

        // 1. Arrange
        RegisterRequest payload = RegisterPayloads.getInvalidPhoneRegisterPayload();

        // 2. Act
        Response response = RegisterAPI.performRegistration(payload);

        // 3. Assert
        Assert.assertEquals(response.getStatusCode(), 400, "HTTP Status should be 400");
        
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");

        RegisterResponse regResponse = response.as(RegisterResponse.class);
        
        Assert.assertFalse(regResponse.isSuccess(), "Success flag should be false for invalid phone format");
        Assert.assertEquals(regResponse.getMessage(), "Phone must be a valid 10-digit number.", "Error message mismatch");

        logger.info("Finished Test: testRegistrationFailsWithInvalidPhone - PASSED");
    }

    @Test(description = "Verify API Chaining: Register user, then login and verify user details match")
    public void testRegistrationAndLoginChaining() {
        logger.info("Starting Test: testRegistrationAndLoginChaining");

        // --- Step 1: Register ---
        // Arrange
        RegisterRequest registerPayload = RegisterPayloads.getUniqueRegisterPayload();
        // Act
        Response registerResponse = RegisterAPI.performRegistration(registerPayload);
        // Assert
        Assert.assertEquals(registerResponse.getStatusCode(), 200);
        
        // Validate Response SLA
        Assert.assertTrue(registerResponse.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + registerResponse.getTime() + " ms");
        
        // Validate JSON Schema
        registerResponse.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(RegisterTests.class.getResourceAsStream("/schemas/register-response-schema.json")));

        RegisterResponse regData = registerResponse.as(RegisterResponse.class);
        Assert.assertTrue(regData.isSuccess());
        int registeredUserId = regData.getUser().getId();
        logger.info("User registered successfully. Assigned ID: {}", registeredUserId);

        // --- Step 2: Login ---
        // Arrange (Extract data from step 1 and construct next payload)
        LoginRequest loginPayload = LoginPayloads.getLoginPayload(
                registerPayload.getEmail(), 
                registerPayload.getPassword(), 
                true
        );
        // Act
        Response loginResponse = LoginAPI.performLogin(loginPayload);
        // Assert
        Assert.assertEquals(loginResponse.getStatusCode(), 200);
        
        // Validate Response SLA
        Assert.assertTrue(loginResponse.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + loginResponse.getTime() + " ms");
        
        // Validate JSON Schema
        loginResponse.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(RegisterTests.class.getResourceAsStream("/schemas/login-response-schema.json")));

        LoginResponse logData = loginResponse.as(LoginResponse.class);
        Assert.assertTrue(logData.isSuccess());
        
        // Chained assertions validating state consistency
        Assert.assertEquals(logData.getUser().getId(), registeredUserId, "Chained check failed: User ID mismatch after login");
        Assert.assertEquals(logData.getUser().getName(), registerPayload.getName(), "Chained check failed: Name mismatch after login");
        Assert.assertEquals(logData.getUser().getEmail(), registerPayload.getEmail(), "Chained check failed: Email mismatch after login");
        
        logger.info("Finished Test: testRegistrationAndLoginChaining - PASSED");
    }
}
