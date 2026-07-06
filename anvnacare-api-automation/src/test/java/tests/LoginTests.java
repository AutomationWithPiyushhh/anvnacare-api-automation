package tests;

import constants.FrameworkConstants;
import endpoints.LoginAPI;
import io.restassured.response.Response;
import models.request.LoginRequest;
import models.response.LoginResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.LoginPayloads;

/**
 * LoginTests verifies authentication functionalities for the ANVNACare application.
 * 
 * Why do we need it?
 * To validate that patient and admin logins work correctly under valid inputs, and fail gracefully 
 * under invalid, empty, or malicious inputs.
 * 
 * Where is it used?
 * Executed via Maven Surefire or TestNG runner to verify build health.
 * 
 * AAA Pattern:
 * - Arrange: Set up request payload, parameters, and headers.
 * - Act: Perform the API call via LoginAPI and capture the response.
 * - Assert: Check the HTTP status code, response time, headers, and body content.
 */
public class LoginTests {

    private static final Logger logger = LogManager.getLogger(LoginTests.class);

    @Test(description = "Verify successful login with valid patient credentials")
    public void testValidPatientLogin() {
        logger.info("Starting Test: testValidPatientLogin");

        // 1. Arrange
        LoginRequest payload = LoginPayloads.getValidUserLoginPayload();

        // 2. Act
        Response response = LoginAPI.performLogin(payload);

        // 3. Assert
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200");
        
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // Validate JSON Schema
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(LoginTests.class.getResourceAsStream("/schemas/login-response-schema.json")));

        // Deserialize response to POJO
        LoginResponse loginResponse = response.as(LoginResponse.class);
        Assert.assertTrue(loginResponse.isSuccess(), "Login success flag should be true");
        Assert.assertNotNull(loginResponse.getUser(), "User object should not be null");
        Assert.assertEquals(loginResponse.getUser().getEmail(), payload.getEmail(), "Email mismatch in response");
        Assert.assertEquals(loginResponse.getUser().getRole(), "user", "Role should be 'user' for patient login");
        Assert.assertEquals(loginResponse.getMessage(), "Login successful.", "Message should indicate successful login");
        
        logger.info("Finished Test: testValidPatientLogin - PASSED");
    }

    @Test(description = "Verify successful login with valid admin credentials")
    public void testValidAdminLogin() {
        logger.info("Starting Test: testValidAdminLogin");

        // 1. Arrange
        LoginRequest payload = LoginPayloads.getValidAdminLoginPayload();

        // 2. Act
        Response response = LoginAPI.performLogin(payload);

        // 3. Assert
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200");
        
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // Validate JSON Schema
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(LoginTests.class.getResourceAsStream("/schemas/login-response-schema.json")));

        LoginResponse loginResponse = response.as(LoginResponse.class);
        Assert.assertTrue(loginResponse.isSuccess(), "Login success flag should be true");
        Assert.assertNotNull(loginResponse.getUser(), "User object should not be null");
        Assert.assertEquals(loginResponse.getUser().getEmail(), payload.getEmail(), "Email mismatch in response");
        Assert.assertEquals(loginResponse.getUser().getRole(), "admin", "Role should be 'admin' for admin login");
        
        logger.info("Finished Test: testValidAdminLogin - PASSED");
    }

    @Test(description = "Verify login fails with invalid password")
    public void testLoginWithInvalidPassword() {
        logger.info("Starting Test: testLoginWithInvalidPassword");

        // 1. Arrange
        LoginRequest payload = LoginPayloads.getLoginPayload("amit.kumar@anvnacare.com", "wrongpassword", true);

        // 2. Act
        Response response = LoginAPI.performLogin(payload);

        // 3. Assert
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");

        LoginResponse loginResponse = response.as(LoginResponse.class);
        Assert.assertFalse(loginResponse.isSuccess(), "Login should fail for invalid password");
        Assert.assertEquals(loginResponse.getMessage(), "Invalid email or password.", "Error message mismatch");
        Assert.assertNull(loginResponse.getUser(), "User details should not be returned on failed login");
        
        logger.info("Finished Test: testLoginWithInvalidPassword - PASSED");
    }

    @Test(description = "Verify login validation when email is blank")
    public void testLoginWithEmptyEmail() {
        logger.info("Starting Test: testLoginWithEmptyEmail");

        // 1. Arrange
        LoginRequest payload = LoginPayloads.getEmptyEmailLoginPayload();

        // 2. Act
        Response response = LoginAPI.performLogin(payload);

        // 3. Assert
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");

        LoginResponse loginResponse = response.as(LoginResponse.class);
        Assert.assertFalse(loginResponse.isSuccess(), "Login should fail with blank email");
        Assert.assertNotNull(loginResponse.getMessage(), "Error message should be present");
        
        logger.info("Finished Test: testLoginWithEmptyEmail - PASSED");
    }

    @Test(description = "Verify login validation when password is blank")
    public void testLoginWithEmptyPassword() {
        logger.info("Starting Test: testLoginWithEmptyPassword");

        // 1. Arrange
        LoginRequest payload = LoginPayloads.getEmptyPasswordLoginPayload();

        // 2. Act
        Response response = LoginAPI.performLogin(payload);

        // 3. Assert
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");

        LoginResponse loginResponse = response.as(LoginResponse.class);
        Assert.assertFalse(loginResponse.isSuccess(), "Login should fail with blank password");
        Assert.assertNotNull(loginResponse.getMessage(), "Error message should be present");
        
        logger.info("Finished Test: testLoginWithEmptyPassword - PASSED");
    }
}
