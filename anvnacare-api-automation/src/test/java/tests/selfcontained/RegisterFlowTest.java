package tests.selfcontained;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseAPI;
import constants.FrameworkConstants;
import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * RegisterFlowTest verifies the account registration API endpoints using
 * self-contained requests, inline hardcoded payloads, JSON Path assertions, and
 * JSON Schema validations.
 */
public class RegisterFlowTest {

	private static final Logger logger = LogManager.getLogger(RegisterFlowTest.class);

	@Test
	public void registerAndLogin() {

	    // ---------- Register ----------

	    String email = "ansh" + System.currentTimeMillis() + "@test.com";

	    Map<String, Object> registerBody = new HashMap<>();

	    registerBody.put("name", "Ansh");
	    registerBody.put("email", email);
	    registerBody.put("phone", "9876543210");
	    registerBody.put("password", "password123");

	    Response registerResponse = RestAssured
	            .given()
		            .baseUri("https://anvnacare.alwaysdata.net/")
		            .contentType("application/json")
		            .body(registerBody)
		            .post("api/register.php");

	    // Print Response
	    System.out.println("===== REGISTER RESPONSE =====");
	    System.out.println("Status Code : " + registerResponse.statusCode());
	    System.out.println("Content Type : " + registerResponse.getContentType());
	    registerResponse.prettyPrint();

	    // Verify Status Code
	    Assert.assertEquals(registerResponse.statusCode(), 200);

	    // Get values from Register Response
	    int userId = registerResponse.jsonPath().getInt("user.id");

	    // ---------- Login ----------

	    Map<String, Object> loginBody = new HashMap<>();

	    loginBody.put("email", email);
	    loginBody.put("password", "password123");
	    loginBody.put("remember", true);

	    Response loginResponse = RestAssured
	            .given()
		            .baseUri("https://anvnacare.alwaysdata.net/")
		            .contentType("application/json")
		            .body(loginBody)
		            .post("api/login.php");

	    // Print Response
	    System.out.println("\n===== LOGIN RESPONSE =====");
	    System.out.println("Status Code : " + loginResponse.statusCode());
	    System.out.println("Content Type : " + loginResponse.getContentType());
	    loginResponse.prettyPrint();

	    // Verify Status Code
	    Assert.assertEquals(loginResponse.statusCode(), 200);

	    // Verify Response
	    Assert.assertTrue(loginResponse.jsonPath().getBoolean("success"));

	    Assert.assertEquals(
	            loginResponse.jsonPath().getInt("user.id"),
	            userId
	    );

	    Assert.assertEquals(
	            loginResponse.jsonPath().getString("user.email"),
	            email
	    );
	}
	@Test(description = "Verify successful registration of a new user with unique details")
	public void testSuccessfulRegistration() {
		logger.info("Starting Test: RegisterFlowTest.testSuccessfulRegistration");

		// Arrange - Generate unique register details inline
		long randomSuffix = System.currentTimeMillis();

		String name = "Ansh " + randomSuffix;
		String email = "ansh." + randomSuffix + "@anvnacare.com";
		long phone = 9000000000L + (long)(Math.random() * 999999999);

		Map<String, Object> payload = new HashMap<>();
		payload.put("name", name);
		payload.put("email", email);
		payload.put("phone", phone);
		payload.put("password", "password123");

		// Act - API Call
		Response response = RestAssured
								.given(BaseAPI.getRequestSpec())
									.body(payload)
									.post("api/register.php");

		// Assert
		Assert.assertEquals(response.getStatusCode(), 200, "HTTP Status mismatch");

		// SLA Validation
		Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS,
				"SLA violated. Response time: " + response.getTime() + " ms");

		// JSON Schema Validation
		response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema(
				RegisterFlowTest.class.getResourceAsStream("/schemas/register-response-schema.json")));

		// JSON Path Assertions (No POJOs)
		Assert.assertTrue(response.jsonPath().getBoolean("success"), "Success flag should be true");
		Assert.assertEquals(response.jsonPath().getString("message"), "Registration successful.", "Message mismatch");
		Assert.assertNotNull(response.jsonPath().get("user"), "User block should not be null");
		Assert.assertEquals(response.jsonPath().getString("user.name"), name, "Name mismatch");
		Assert.assertEquals(response.jsonPath().getString("user.email"), email, "Email mismatch");
		Assert.assertTrue(response.jsonPath().getInt("user.id") > 0, "User ID should be a positive number");

		logger.info("Finished Test: RegisterFlowTest.testSuccessfulRegistration - PASSED");
	}

	@Test(description = "Verify registration fails when password length is below 6 characters")
	public void testRegistrationFailsWithShortPassword() {
		logger.info("Starting Test: RegisterFlowTest.testRegistrationFailsWithShortPassword");

		// Arrange - Inline payload with a short password
		String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
		String name = "Automation User " + randomSuffix;
		String email = "auto." + randomSuffix + "@anvnacare.com";
		long randomPhone = (long) (Math.random() * 9000000000L) + 1000000000L;
		String phone = String.valueOf(randomPhone);

		Map<String, Object> payload = new HashMap<>();
		payload.put("name", name);
		payload.put("email", email);
		payload.put("phone", phone);
		payload.put("password", "123"); // short password

		// Act - API Call
		Response response = RestAssured.given(BaseAPI.getRequestSpec()).body(payload).post("api/register.php");

		// Assert
		Assert.assertEquals(response.getStatusCode(), 400, "HTTP Status should be 400");

		// SLA Validation
		Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS,
				"SLA violated. Response time: " + response.getTime() + " ms");

		// JSON Path Assertions
		Assert.assertFalse(response.jsonPath().getBoolean("success"), "Success flag should be false");
		Assert.assertEquals(response.jsonPath().getString("message"), "Password must be at least 6 characters.",
				"Error message mismatch");

		logger.info("Finished Test: RegisterFlowTest.testRegistrationFailsWithShortPassword - PASSED");
	}

	@Test(description = "Verify registration fails when phone format is not a 10-digit number")
	public void testRegistrationFailsWithInvalidPhone() {
		logger.info("Starting Test: RegisterFlowTest.testRegistrationFailsWithInvalidPhone");

		// Arrange - Inline payload with an invalid phone number
		String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
		String name = "Automation User " + randomSuffix;
		String email = "auto." + randomSuffix + "@anvnacare.com";

		Map<String, Object> payload = new HashMap<>();
		payload.put("name", name);
		payload.put("email", email);
		payload.put("phone", "12345"); // invalid phone length
		payload.put("password", "password123");

		// Act - API Call
		Response response = RestAssured.given(BaseAPI.getRequestSpec()).body(payload).post("api/register.php");

		// Assert
		Assert.assertEquals(response.getStatusCode(), 400, "HTTP Status should be 400");

		// SLA Validation
		Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS,
				"SLA violated. Response time: " + response.getTime() + " ms");

		// JSON Path Assertions
		Assert.assertFalse(response.jsonPath().getBoolean("success"), "Success flag should be false");
		Assert.assertEquals(response.jsonPath().getString("message"), "Phone must be a valid 10-digit number.",
				"Error message mismatch");

		logger.info("Finished Test: RegisterFlowTest.testRegistrationFailsWithInvalidPhone - PASSED");
	}

	@Test(description = "Verify API Chaining: Register user, then login and verify user details match")
	public void testRegistrationAndLoginChaining() {
		logger.info("Starting Test: RegisterFlowTest.testRegistrationAndLoginChaining");

		// --- Step 1: Register ---
		// Arrange
		String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
		String name = "Automation User " + randomSuffix;
		String email = "auto." + randomSuffix + "@anvnacare.com";
		long randomPhone = (long) (Math.random() * 9000000000L) + 1000000000L;
		String phone = String.valueOf(randomPhone);

		Map<String, Object> registerPayload = new HashMap<>();
		registerPayload.put("name", name);
		registerPayload.put("email", email);
		registerPayload.put("phone", phone);
		registerPayload.put("password", "password123");

		// Act
		Response registerResponse = RestAssured.given(BaseAPI.getRequestSpec()).body(registerPayload)
				.post("api/register.php");

		// Assert
		Assert.assertEquals(registerResponse.getStatusCode(), 200);
		Assert.assertTrue(registerResponse.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS,
				"SLA violated. Response time: " + registerResponse.getTime() + " ms");

		// Schema Check
		registerResponse.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema(
				RegisterFlowTest.class.getResourceAsStream("/schemas/register-response-schema.json")));

		int registeredUserId = registerResponse.jsonPath().getInt("user.id");
		Assert.assertTrue(registeredUserId > 0);
		logger.info("User registered successfully. Assigned ID: {}", registeredUserId);

		// --- Step 2: Login ---
		// Arrange
		Map<String, Object> loginPayload = new HashMap<>();
		loginPayload.put("email", email);
		loginPayload.put("password", "password123");
		loginPayload.put("remember", true);

		// Act
		Response loginResponse = RestAssured.given(BaseAPI.getRequestSpec()).body(loginPayload).post("api/login.php");

		// Assert
		Assert.assertEquals(loginResponse.getStatusCode(), 200);
		Assert.assertTrue(loginResponse.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS,
				"SLA violated. Response time: " + loginResponse.getTime() + " ms");

		// Schema Check
		loginResponse.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
				.matchesJsonSchema(RegisterFlowTest.class.getResourceAsStream("/schemas/login-response-schema.json")));

		Assert.assertTrue(loginResponse.jsonPath().getBoolean("success"));
		Assert.assertEquals(loginResponse.jsonPath().getInt("user.id"), registeredUserId,
				"Chained check failed: User ID mismatch");
		Assert.assertEquals(loginResponse.jsonPath().getString("user.name"), name,
				"Chained check failed: Name mismatch");
		Assert.assertEquals(loginResponse.jsonPath().getString("user.email"), email,
				"Chained check failed: Email mismatch");

		logger.info("Finished Test: RegisterFlowTest.testRegistrationAndLoginChaining - PASSED");
	}
}
