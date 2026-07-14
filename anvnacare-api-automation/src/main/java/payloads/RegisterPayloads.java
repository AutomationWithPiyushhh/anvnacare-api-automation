package payloads;

import java.util.concurrent.ThreadLocalRandom;

import models.request.RegisterRequest;

/**
 * Factory class for generating RegisterRequest payloads.
 *
 * <p>
 * Purpose:
 * <ul>
 * <li>Keeps test data separate from test logic.</li>
 * <li>Provides reusable payloads for different test scenarios.</li>
 * <li>Ensures every positive registration request uses unique user data.</li>
 * </ul>
 *
 * <p>
 * This approach avoids "Email already exists" failures and makes tests
 * independent, repeatable, and parallel-execution friendly.
 */
public final class RegisterPayloads {

	private RegisterPayloads() {
		// Prevent instantiation
	}

	private static final String DEFAULT_PASSWORD = "password123";
	private static final String EMAIL_DOMAIN = "@anvnacare.com";

	/**
	 * Generates a completely unique registration payload.
	 */
	public static RegisterRequest getUniqueRegisterPayload() {

		long uniqueId = System.currentTimeMillis();

		String name = "ansh" + uniqueId;
		String email = "ansh" + uniqueId + EMAIL_DOMAIN;
		String phone = generateRandomPhoneNumber();

		return new RegisterRequest(name, email, phone, DEFAULT_PASSWORD);
	}

	/**
	 * Generates a registration payload with custom values.
	 */
	public static RegisterRequest getCustomRegisterPayload(String name, String email, String phone, String password) {

		return new RegisterRequest(name, email, phone, password);
	}

	/**
	 * Generates payload with invalid (short) password.
	 */
	public static RegisterRequest getInvalidPasswordRegisterPayload() {

		RegisterRequest payload = getUniqueRegisterPayload();
		payload.setPassword("123");

		return payload;
	}

	/**
	 * Generates payload with invalid phone number.
	 */
	public static RegisterRequest getInvalidPhoneRegisterPayload() {

		RegisterRequest payload = getUniqueRegisterPayload();
		payload.setPhone("12345");

		return payload;
	}

	/**
	 * Generates a valid random 10-digit phone number.
	 */
	private static String generateRandomPhoneNumber() {

		long phone = ThreadLocalRandom.current().nextLong(1000000000L, 10000000000L);

		return String.valueOf(phone);
	}
}