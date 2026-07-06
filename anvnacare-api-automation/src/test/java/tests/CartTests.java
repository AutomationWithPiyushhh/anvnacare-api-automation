package tests;

import constants.FrameworkConstants;
import endpoints.CartAPI;
import io.restassured.response.Response;
import models.request.CartRequest;
import models.response.CartResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * CartTests validates the functional behaviors of the shopping cart endpoints.
 * 
 * Why do we need it?
 * To ensure items can be added, updated, and deleted securely by authenticated users.
 * 
 * AAA Pattern:
 * - Arrange: Configure items, actions, and credentials.
 * - Act: Execute calls via CartAPI (authenticated or anonymous).
 * - Assert: Check response flags, messages, and redirects.
 */
public class CartTests {

    private static final Logger logger = LogManager.getLogger(CartTests.class);

    @Test(description = "Verify adding to cart as a guest (unauthenticated) succeeds and starts a guest session")
    public void testAddToCartAsGuest() {
        logger.info("Starting Test: testAddToCartAsGuest");

        // 1. Arrange
        CartRequest payload = new CartRequest("add", 1, "medicine", 2);

        // 2. Act
        Response response = CartAPI.addToCartUnauthenticated(payload);

        // 3. Assert
        Assert.assertEquals(response.getStatusCode(), 200, "HTTP status mismatch");
        
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // Validate JSON Schema
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(CartTests.class.getResourceAsStream("/schemas/cart-response-schema.json")));

        CartResponse cartResponse = response.as(CartResponse.class);
        
        Assert.assertTrue(cartResponse.isSuccess(), "Success should be true for guest session creation");
        Assert.assertTrue(cartResponse.getMessage().contains("added to cart."), "Message mismatch");
        Assert.assertNotNull(response.getCookie("PHPSESSID"), "Guest PHPSESSID cookie should be generated");

        logger.info("Finished Test: testAddToCartAsGuest - PASSED");
    }

    @Test(description = "Verify E2E Cart Flow: Add item -> Update quantity -> Remove item")
    public void testCartEndToEndWorkflow() {
        logger.info("Starting Test: testCartEndToEndWorkflow");
        String role = "user"; // Logged in patient session

        // --- Step 1: Add Item to Cart ---
        logger.info("Step 1: Adding medicine ID 1 to cart");
        // Arrange
        CartRequest addPayload = new CartRequest("add", 1, "medicine", 2);
        // Act
        Response addResponse = CartAPI.addToCart(addPayload, role);
        // Assert
        Assert.assertEquals(addResponse.getStatusCode(), 200);
        
        // Validate Response SLA
        Assert.assertTrue(addResponse.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + addResponse.getTime() + " ms");
        
        // Validate JSON Schema
        addResponse.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(CartTests.class.getResourceAsStream("/schemas/cart-response-schema.json")));

        CartResponse addResult = addResponse.as(CartResponse.class);
        Assert.assertTrue(addResult.isSuccess(), "Failed to add item to cart");
        Assert.assertTrue(addResult.getCartCount() >= 1, "Cart count should be at least 1");
        Assert.assertTrue(addResult.getMessage().contains("added to cart."), "Message mismatch: expected to contain 'added to cart.'");

        // --- Step 2: Update Item Quantity in Cart ---
        logger.info("Step 2: Updating quantity of medicine ID 1 in cart to 5");
        // Arrange
        CartRequest updatePayload = new CartRequest("update", 1, "medicine", 5);
        // Act
        Response updateResponse = CartAPI.updateCart(updatePayload, role);
        // Assert
        Assert.assertEquals(updateResponse.getStatusCode(), 200);
        
        // Validate Response SLA
        Assert.assertTrue(updateResponse.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + updateResponse.getTime() + " ms");
        
        // Validate JSON Schema
        updateResponse.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(CartTests.class.getResourceAsStream("/schemas/cart-response-schema.json")));

        CartResponse updateResult = updateResponse.as(CartResponse.class);
        Assert.assertTrue(updateResult.isSuccess(), "Failed to update item quantity");
        Assert.assertEquals(updateResult.getMessage(), "Cart updated.", "Message mismatch");

        // --- Step 3: Remove Item from Cart ---
        logger.info("Step 3: Removing medicine ID 1 from cart");
        // Arrange
        CartRequest removePayload = new CartRequest("remove", 1, "medicine", null);
        // Act
        Response removeResponse = CartAPI.removeCartItem(removePayload, role);
        // Assert
        Assert.assertEquals(removeResponse.getStatusCode(), 200);
        
        // Validate Response SLA
        Assert.assertTrue(removeResponse.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + removeResponse.getTime() + " ms");
        
        // Validate JSON Schema
        removeResponse.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(CartTests.class.getResourceAsStream("/schemas/cart-response-schema.json")));

        CartResponse removeResult = removeResponse.as(CartResponse.class);
        Assert.assertTrue(removeResult.isSuccess(), "Failed to remove item from cart");
        Assert.assertEquals(removeResult.getMessage(), "Item removed from cart.", "Message mismatch");

        logger.info("Finished Test: testCartEndToEndWorkflow - PASSED");
    }
}
