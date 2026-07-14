package tests.selfcontained;

import base.BaseAPI;
import constants.FrameworkConstants;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * LoginFlowTest verifies patient and admin authentication using self-contained
 * Rest Assured requests, validations, and JSON Path assertions.
 * 
 * It has zero dependencies on endpoints, models, or payloads.
 */
public class LoginFlowTest {

	private static final Logger logger = LogManager.getLogger(LoginFlowTest.class);

	@Test
	public void loginTest() {

	    Response response = RestAssured
	            .given()
	            .contentType("application/json")
	            .body("""
	                    {
	                        "email":"vasudev@anvnacare.com",
	                        "password":"vasudev@21",
	                        "remember":true
	                    }
	                    """)
	            .post("http://anvnacare.alwaysdata.net/api/login.php");

	    System.out.println(response.asPrettyString());

	}
	
	
	@Test(description = "Verify successful login with valid patient credentials")
	public void testValidPatientLogin() {
		logger.info("Starting Test: LoginFlowTest.testValidPatientLogin");

		// Arrange - Hardcoded payload
		Map<String, Object> payload = new HashMap<>();
		payload.put("email", "vasudev@anvnacare.com");
		payload.put("password", "vasudev@21");
		payload.put("remember", true);

		// Act - API Call
		Response response = RestAssured
								.given(BaseAPI.getRequestSpec())
									.body(payload)
									.post("api/login.php");

		// Assert
		Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200");

		// SLA Validation
		Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS,
				"SLA violated. Response time: " + response.getTime() + " ms");

		// JSON Schema Validation
		response
			.then()
				.assertThat()
				.body(io.restassured.module.jsv.JsonSchemaValidator
				.matchesJsonSchema(LoginFlowTest.class.getResourceAsStream("/schemas/login-response-schema.json")));

		// JSON Path Assertions (No POJOs)
		Assert.assertTrue(response.jsonPath().getBoolean("success"), "Login success flag should be true");
		Assert.assertNotNull(response.jsonPath().get("user"), "User object should not be null");
		Assert.assertEquals(response.jsonPath().getString("user.email"), "vasudev@anvnacare.com", "Email mismatch");
		Assert.assertEquals(response.jsonPath().getString("user.role"), "user", "Role should be 'user'");
		Assert.assertEquals(response.jsonPath().getString("message"), "Login successful.", "Message mismatch");

		logger.info("Finished Test: LoginFlowTest.testValidPatientLogin - PASSED");
	}

	@Test(description = "Verify successful login with valid admin credentials")
	public void testValidAdminLogin() {
		logger.info("Starting Test: LoginFlowTest.testValidAdminLogin");

		// Arrange - Hardcoded payload
		Map<String, Object> payload = new HashMap<>();
		payload.put("email", "admin@anvnacare.com");
		payload.put("password", "password123");
		payload.put("remember", true);

		// Act - API Call
		Response response = RestAssured
					.given(BaseAPI.getRequestSpec())
						.body(payload)
						.post("api/login.php");

		// Assert
		Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200");

		// SLA Validation
		Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS,
				"SLA violated. Response time: " + response.getTime() + " ms");

		// JSON Schema Validation
		response
			.then()
				.assertThat()
				.body(io.restassured.module.jsv.JsonSchemaValidator
				.matchesJsonSchema(LoginFlowTest.class.getResourceAsStream("/schemas/login-response-schema.json")));

		// JSON Path Assertions
		Assert.assertTrue(response.jsonPath().getBoolean("success"), "Login success flag should be true");
		Assert.assertNotNull(response.jsonPath().get("user"), "User object should not be null");
		Assert.assertEquals(response.jsonPath().getString("user.email"), "admin@anvnacare.com", "Email mismatch");
		Assert.assertEquals(response.jsonPath().getString("user.role"), "admin", "Role should be 'admin'");

		logger.info("Finished Test: LoginFlowTest.testValidAdminLogin - PASSED");
	}

	@Test(description = "Verify login fails with invalid password")
	public void testLoginWithInvalidPassword() {
		logger.info("Starting Test: LoginFlowTest.testLoginWithInvalidPassword");

		// Arrange - Hardcoded payload
		Map<String, Object> payload = new HashMap<>();
		payload.put("email", "vasudev@anvnacare.com");
		payload.put("password", "wrongpassword");
		payload.put("remember", true);

		// Act - API Call
		Response response = RestAssured.given(BaseAPI.getRequestSpec()).body(payload).post("api/login.php");

		// Assert
		Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401");

		// SLA Validation
		Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS,
				"SLA violated. Response time: " + response.getTime() + " ms");

		// JSON Path Assertions
		Assert.assertFalse(response.jsonPath().getBoolean("success"), "Login should fail");
		Assert.assertEquals(response.jsonPath().getString("message"), "Invalid email or password.",
				"Error message mismatch");
		Assert.assertNull(response.jsonPath().get("user"), "User details should not be returned");

		logger.info("Finished Test: LoginFlowTest.testLoginWithInvalidPassword - PASSED");
	}

	@Test(description = "Verify login validation when email is blank")
	public void testLoginWithEmptyEmail() {
		logger.info("Starting Test: LoginFlowTest.testLoginWithEmptyEmail");

		// Arrange - Hardcoded payload
		Map<String, Object> payload = new HashMap<>();
		payload.put("email", "");
		payload.put("password", "vasudev@21");
		payload.put("remember", true);

		// Act - API Call
		Response response = RestAssured.given(BaseAPI.getRequestSpec()).body(payload).post("api/login.php");

		// Assert
		Assert.assertFalse(response.jsonPath().getBoolean("success"), "Login should fail");
		Assert.assertNotNull(response.jsonPath().getString("message"), "Error message should be present");

		logger.info("Finished Test: LoginFlowTest.testLoginWithEmptyEmail - PASSED");
	}

	@Test(description = "Verify login validation when password is blank")
	public void testLoginWithEmptyPassword() {
		logger.info("Starting Test: LoginFlowTest.testLoginWithEmptyPassword");

		// Arrange - Hardcoded payload
		Map<String, Object> payload = new HashMap<>();
		payload.put("email", "vasudev@anvnacare.com");
		payload.put("password", "");
		payload.put("remember", true);

		// Act - API Call
		Response response = RestAssured.given(BaseAPI.getRequestSpec()).body(payload).post("api/login.php");

		// Assert
		Assert.assertFalse(response.jsonPath().getBoolean("success"), "Login should fail");
		Assert.assertNotNull(response.jsonPath().getString("message"), "Error message should be present");

		logger.info("Finished Test: LoginFlowTest.testLoginWithEmptyPassword - PASSED");
	}
}
