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
import java.util.List;
import java.util.Map;

/**
 * MedicinesFlowTest verifies catalog query behaviors for the Medicines Catalog APIs
 * using self-contained Rest Assured requests, validations, and JSON Path assertions.
 */
public class MedicinesFlowTest {

    private static final Logger logger = LogManager.getLogger(MedicinesFlowTest.class);

    private double parseDoubleValue(Object obj) {
        if (obj == null) {
            return 0.0;
        }
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @Test(description = "Verify retrieving all medicines returns a valid list of items")
    public void testGetAllMedicines() {
        logger.info("Starting Test: MedicinesFlowTest.testGetAllMedicines");

        // Act - API Call
        Response response = RestAssured.given(BaseAPI.getRequestSpec())
                .get("api/medicines.php");

        // Assert
        Assert.assertEquals(response.getStatusCode(), 200, "HTTP Status should be 200");
        
        // SLA Validation
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // JSON Schema Validation
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(MedicinesFlowTest.class.getResourceAsStream("/schemas/medicines-response-schema.json")));

        // JSON Path Assertions (No POJOs)
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "API success flag should be true");
        List<Map<String, Object>> medicines = response.jsonPath().getList("medicines");
        Assert.assertNotNull(medicines, "Medicines list should not be null");
        Assert.assertTrue(medicines.size() > 0, "Medicines list should contain products");
        
        for (Map<String, Object> med : medicines) {
            int id = ((Number) med.get("id")).intValue();
            String name = (String) med.get("name");
            double mrp = parseDoubleValue(med.get("mrp"));
            double discountPrice = parseDoubleValue(med.get("discount_price"));

            Assert.assertTrue(id > 0, "Medicine ID should be positive");
            Assert.assertNotNull(name, "Name should not be null");
            Assert.assertTrue(mrp >= discountPrice, "MRP must be greater or equal to discount price");
        }

        logger.info("Finished Test: MedicinesFlowTest.testGetAllMedicines - PASSED");
    }

    @Test(description = "Verify filtering medicines by category returns matching category items only")
    public void testFilterByCategory() {
        logger.info("Starting Test: MedicinesFlowTest.testFilterByCategory");

        // Act - API Call with Category filter
        Response response = RestAssured.given(BaseAPI.getRequestSpec())
                .queryParam("category", "OTC")
                .get("api/medicines.php");

        // Assert
        Assert.assertEquals(response.getStatusCode(), 200, "HTTP Status mismatch");
        
        // SLA Validation
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // JSON Schema Validation
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(MedicinesFlowTest.class.getResourceAsStream("/schemas/medicines-response-schema.json")));

        // JSON Path assertions
        Assert.assertTrue(response.jsonPath().getBoolean("success"));
        List<Map<String, Object>> medicines = response.jsonPath().getList("medicines");
        Assert.assertNotNull(medicines);
        
        for (Map<String, Object> med : medicines) {
            Assert.assertEquals(med.get("category"), "OTC", "Filtered category mismatch");
        }

        logger.info("Finished Test: MedicinesFlowTest.testFilterByCategory - PASSED");
    }

    @Test(description = "Verify searching medicines with keyword returns relevant items")
    public void testSearchMedicines() {
        logger.info("Starting Test: MedicinesFlowTest.testSearchMedicines");

        // Act - API Call with search query
        Response response = RestAssured.given(BaseAPI.getRequestSpec())
                .queryParam("search", "Paracetamol")
                .get("api/medicines.php");

        // Assert
        Assert.assertEquals(response.getStatusCode(), 200);
        
        // SLA Validation
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // JSON Schema Validation
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(MedicinesFlowTest.class.getResourceAsStream("/schemas/medicines-response-schema.json")));

        Assert.assertTrue(response.jsonPath().getBoolean("success"));
        List<Map<String, Object>> medicines = response.jsonPath().getList("medicines");
        Assert.assertNotNull(medicines);
        
        for (Map<String, Object> med : medicines) {
            String name = ((String) med.get("name")).toLowerCase();
            String manufacturer = ((String) med.get("manufacturer")).toLowerCase();
            boolean matchesName = name.contains("paracetamol");
            boolean matchesManufacturer = manufacturer.contains("paracetamol");
            Assert.assertTrue(matchesName || matchesManufacturer, 
                    "Search query 'Paracetamol' not found in name or manufacturer: " + med.get("name"));
        }

        logger.info("Finished Test: MedicinesFlowTest.testSearchMedicines - PASSED");
    }

    @Test(description = "Verify sorting medicines by price in ascending order works correctly")
    public void testSortByPriceAscending() {
        logger.info("Starting Test: MedicinesFlowTest.testSortByPriceAscending");

        // Act - API Call with sort parameter
        Response response = RestAssured.given(BaseAPI.getRequestSpec())
                .queryParam("sort", "price_asc")
                .get("api/medicines.php");

        // Assert
        Assert.assertEquals(response.getStatusCode(), 200);
        
        // SLA Validation
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // JSON Schema Validation
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(MedicinesFlowTest.class.getResourceAsStream("/schemas/medicines-response-schema.json")));

        List<Map<String, Object>> medicines = response.jsonPath().getList("medicines");
        Assert.assertTrue(medicines.size() >= 2, "Need at least 2 items to verify sort");

        for (int i = 0; i < medicines.size() - 1; i++) {
            double currentPrice = parseDoubleValue(medicines.get(i).get("discount_price"));
            double nextPrice = parseDoubleValue(medicines.get(i + 1).get("discount_price"));
            Assert.assertTrue(currentPrice <= nextPrice, 
                    String.format("Prices are not sorted ascending: Index %d (%f) > Index %d (%f)", 
                            i, currentPrice, i + 1, nextPrice));
        }

        logger.info("Finished Test: MedicinesFlowTest.testSortByPriceAscending - PASSED");
    }

    @Test(description = "Verify sorting medicines by price in descending order works correctly")
    public void testSortByPriceDescending() {
        logger.info("Starting Test: MedicinesFlowTest.testSortByPriceDescending");

        // Act - API Call with sort parameter
        Response response = RestAssured.given(BaseAPI.getRequestSpec())
                .queryParam("sort", "price_desc")
                .get("api/medicines.php");

        // Assert
        Assert.assertEquals(response.getStatusCode(), 200);
        
        // SLA Validation
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // JSON Schema Validation
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(MedicinesFlowTest.class.getResourceAsStream("/schemas/medicines-response-schema.json")));

        List<Map<String, Object>> medicines = response.jsonPath().getList("medicines");
        Assert.assertTrue(medicines.size() >= 2, "Need at least 2 items to verify sort");

        for (int i = 0; i < medicines.size() - 1; i++) {
            double currentPrice = parseDoubleValue(medicines.get(i).get("discount_price"));
            double nextPrice = parseDoubleValue(medicines.get(i + 1).get("discount_price"));
            Assert.assertTrue(currentPrice >= nextPrice, 
                    String.format("Prices are not sorted descending: Index %d (%f) < Index %d (%f)", 
                            i, currentPrice, i + 1, nextPrice));
        }

        logger.info("Finished Test: MedicinesFlowTest.testSortByPriceDescending - PASSED");
    }
}
