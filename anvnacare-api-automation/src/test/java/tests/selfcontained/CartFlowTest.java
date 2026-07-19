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
 * CartFlowTest verifies the shopping cart functional workflows using
 * self-contained Rest Assured requests, inline hardcoded payloads,
 * JSON Path assertions, and JSON Schema validations.
 */
public class CartFlowTest {

    private static final Logger logger = LogManager.getLogger(CartFlowTest.class);

    @Test
    public void testAddToCartAsGuest() {
        logger.info("Starting Test: CartFlowTest.testAddToCartAsGuest");

        // Arrange - Inline payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "add");
        payload.put("item_id", 1);
        payload.put("item_type", "medicine");
        payload.put("quantity", 2);

        // Act - API Call
        Response response = RestAssured.given(BaseAPI.getRequestSpec())
                .body(payload)
                .post("api/cart.php");

        // Assert
        Assert.assertEquals(response.getStatusCode(), 200, "HTTP status mismatch");
        
        // SLA Validation
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // JSON Schema Validation
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(CartFlowTest.class.getResourceAsStream("/schemas/cart-response-schema.json")));

        // JSON Path Assertions (No POJOs)
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "Success should be true for guest session creation");
        Assert.assertTrue(response.jsonPath().getString("message").contains("added to cart."), "Message mismatch");
        Assert.assertNotNull(response.getCookie("PHPSESSID"), "Guest PHPSESSID cookie should be generated");

        logger.info("Finished Test: CartFlowTest.testAddToCartAsGuest - PASSED");
    }

    @Test(description = "Verify E2E Cart Flow: Add item -> Update quantity -> Remove item")
    public void testCartEndToEndWorkflow() {
        logger.info("Starting Test: CartFlowTest.testCartEndToEndWorkflow");
        String role = "user"; // Logged in patient session

        // --- Step 1: Add Item to Cart ---
        logger.info("Step 1: Adding medicine ID 1 to cart");
        
        // Arrange
        Map<String, Object> addPayload = new HashMap<>();
        addPayload.put("action", "add");
        addPayload.put("item_id", 1);
        addPayload.put("item_type", "medicine");
        addPayload.put("quantity", 2);

        // Act
        Response addResponse = RestAssured.given(BaseAPI.getRequestSpec(role))
                .body(addPayload)
                .post("api/cart.php");

        // Assert
        Assert.assertEquals(addResponse.getStatusCode(), 200);
        Assert.assertTrue(addResponse.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + addResponse.getTime() + " ms");
        
        // Schema Check
        addResponse.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(CartFlowTest.class.getResourceAsStream("/schemas/cart-response-schema.json")));

        Assert.assertTrue(addResponse.jsonPath().getBoolean("success"), "Failed to add item to cart");
        
        Integer cartCount = addResponse.jsonPath().get("cart_count");
        Assert.assertNotNull(cartCount, "cart_count should not be null");
        Assert.assertTrue(cartCount >= 1, "Cart count should be at least 1");
        Assert.assertTrue(addResponse.jsonPath().getString("message").contains("added to cart."), "Message mismatch");

        // --- Step 2: Update Item Quantity in Cart ---
        logger.info("Step 2: Updating quantity of medicine ID 1 in cart to 5");
        
        // Arrange
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("action", "update");
        updatePayload.put("item_id", 1);
        updatePayload.put("item_type", "medicine");
        updatePayload.put("quantity", 5);

        // Act
        Response updateResponse = RestAssured.given(BaseAPI.getRequestSpec(role))
                .body(updatePayload)
                .post("api/cart.php");

        // Assert
        Assert.assertEquals(updateResponse.getStatusCode(), 200);
        Assert.assertTrue(updateResponse.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + updateResponse.getTime() + " ms");
        
        // Schema Check
        updateResponse.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(CartFlowTest.class.getResourceAsStream("/schemas/cart-response-schema.json")));

        Assert.assertTrue(updateResponse.jsonPath().getBoolean("success"), "Failed to update item quantity");
        Assert.assertEquals(updateResponse.jsonPath().getString("message"), "Cart updated.", "Message mismatch");

        // --- Step 3: Remove Item from Cart ---
        logger.info("Step 3: Removing medicine ID 1 from cart");
        
        // Arrange
        Map<String, Object> removePayload = new HashMap<>();
        removePayload.put("action", "remove");
        removePayload.put("item_id", 1);
        removePayload.put("item_type", "medicine");
        // Omit quantity

        // Act - HTTP DELETE method
        Response removeResponse = RestAssured.given(BaseAPI.getRequestSpec(role))
                .body(removePayload)
                .delete("api/cart.php");

        // Assert
        Assert.assertEquals(removeResponse.getStatusCode(), 200);
        Assert.assertTrue(removeResponse.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + removeResponse.getTime() + " ms");
        
        // Schema Check
        removeResponse.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(CartFlowTest.class.getResourceAsStream("/schemas/cart-response-schema.json")));

        Assert.assertTrue(removeResponse.jsonPath().getBoolean("success"), "Failed to remove item from cart");
        Assert.assertEquals(removeResponse.jsonPath().getString("message"), "Item removed from cart.", "Message mismatch");

        logger.info("Finished Test: CartFlowTest.testCartEndToEndWorkflow - PASSED");
    }
}
