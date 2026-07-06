package tests;

import constants.FrameworkConstants;
import endpoints.MedicinesAPI;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.response.Medicine;
import models.response.MedicinesResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * MedicinesTests contains validation scenarios for the Medicines Catalog APIs.
 * 
 * Why do we need it?
 * To ensure customers can search, sort, and filter healthcare products without issues.
 * 
 * AAA Pattern:
 * - Arrange: Define parameters for filtering or sorting.
 * - Act: Fetch data using MedicinesAPI.
 * - Assert: Check correctness of lists, ordering, and data structures.
 */
public class MedicinesTests {

    private static final Logger logger = LogManager.getLogger(MedicinesTests.class);

    @Test(description = "Verify retrieving all medicines returns a valid list of items")
    public void testGetAllMedicines() {
        logger.info("Starting Test: testGetAllMedicines");

        // 1. Arrange & 2. Act
        Response response = MedicinesAPI.getAllMedicines();

        // 3. Assert
        Assert.assertEquals(response.getStatusCode(), 200, "HTTP Status should be 200");
        
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // Validate JSON Schema
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(MedicinesTests.class.getResourceAsStream("/schemas/medicines-response-schema.json")));

        MedicinesResponse medResponse = response.as(MedicinesResponse.class);
        
        Assert.assertTrue(medResponse.isSuccess(), "API success flag should be true");
        Assert.assertNotNull(medResponse.getMedicines(), "Medicines list should not be null");
        Assert.assertTrue(medResponse.getMedicines().size() > 0, "Medicines list should contain products");
        
        // Assert schema fields are positive
        for (Medicine med : medResponse.getMedicines()) {
            Assert.assertTrue(med.getId() > 0, "Medicine ID should be positive");
            Assert.assertNotNull(med.getName(), "Name should not be null");
            Assert.assertTrue(med.getMrp() >= med.getDiscountPrice(), "MRP must be greater or equal to discount price");
        }

        logger.info("Finished Test: testGetAllMedicines - PASSED");
    }

    @Test(description = "Verify filtering medicines by category returns matching category items only")
    public void testFilterByCategory() {
        logger.info("Starting Test: testFilterByCategory");

        // 1. Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("category", "OTC");

        // 2. Act
        Response response = MedicinesAPI.getMedicines(queryParams);

        // 3. Assert
        Assert.assertEquals(response.getStatusCode(), 200);
        
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // Validate JSON Schema
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(MedicinesTests.class.getResourceAsStream("/schemas/medicines-response-schema.json")));

        MedicinesResponse medResponse = response.as(MedicinesResponse.class);
        
        Assert.assertTrue(medResponse.isSuccess());
        Assert.assertNotNull(medResponse.getMedicines());
        
        for (Medicine med : medResponse.getMedicines()) {
            Assert.assertEquals(med.getCategory(), "OTC", "Filtered category mismatch");
        }

        logger.info("Finished Test: testFilterByCategory - PASSED");
    }

    @Test(description = "Verify searching medicines with keyword returns relevant items")
    public void testSearchMedicines() {
        logger.info("Starting Test: testSearchMedicines");

        // 1. Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search", "Paracetamol");

        // 2. Act
        Response response = MedicinesAPI.getMedicines(queryParams);

        // 3. Assert
        Assert.assertEquals(response.getStatusCode(), 200);
        
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // Validate JSON Schema
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(MedicinesTests.class.getResourceAsStream("/schemas/medicines-response-schema.json")));

        MedicinesResponse medResponse = response.as(MedicinesResponse.class);
        
        Assert.assertTrue(medResponse.isSuccess());
        Assert.assertNotNull(medResponse.getMedicines());
        
        for (Medicine med : medResponse.getMedicines()) {
            boolean matchesName = med.getName().toLowerCase().contains("paracetamol");
            boolean matchesManufacturer = med.getManufacturer().toLowerCase().contains("paracetamol");
            Assert.assertTrue(matchesName || matchesManufacturer, 
                    "Search query 'Paracetamol' not found in name or manufacturer: " + med.getName());
        }

        logger.info("Finished Test: testSearchMedicines - PASSED");
    }

    @Test(description = "Verify sorting medicines by price in ascending order works correctly")
    public void testSortByPriceAscending() {
        logger.info("Starting Test: testSortByPriceAscending");

        // 1. Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("sort", "price_asc");

        // 2. Act
        Response response = MedicinesAPI.getMedicines(queryParams);

        // 3. Assert
        Assert.assertEquals(response.getStatusCode(), 200);
        
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // Validate JSON Schema
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(MedicinesTests.class.getResourceAsStream("/schemas/medicines-response-schema.json")));

        MedicinesResponse medResponse = response.as(MedicinesResponse.class);
        List<Medicine> medicines = medResponse.getMedicines();
        
        Assert.assertTrue(medicines.size() >= 2, "Need at least 2 items to verify sort");

        for (int i = 0; i < medicines.size() - 1; i++) {
            double currentPrice = medicines.get(i).getDiscountPrice();
            double nextPrice = medicines.get(i + 1).getDiscountPrice();
            Assert.assertTrue(currentPrice <= nextPrice, 
                    String.format("Prices are not sorted ascending: Index %d (%f) > Index %d (%f)", 
                            i, currentPrice, i + 1, nextPrice));
        }

        logger.info("Finished Test: testSortByPriceAscending - PASSED");
    }

    @Test(description = "Verify sorting medicines by price in descending order works correctly")
    public void testSortByPriceDescending() {
        logger.info("Starting Test: testSortByPriceDescending");

        // 1. Arrange
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("sort", "price_desc");

        // 2. Act
        Response response = MedicinesAPI.getMedicines(queryParams);

        // 3. Assert
        Assert.assertEquals(response.getStatusCode(), 200);
        
        // Validate Response SLA
        Assert.assertTrue(response.getTime() <= FrameworkConstants.MAX_RESPONSE_TIME_MS, 
                "SLA violated. Response time: " + response.getTime() + " ms");
        
        // Validate JSON Schema
        response.then().assertThat().body(io.restassured.module.jsv.JsonSchemaValidator
                .matchesJsonSchema(MedicinesTests.class.getResourceAsStream("/schemas/medicines-response-schema.json")));

        MedicinesResponse medResponse = response.as(MedicinesResponse.class);
        List<Medicine> medicines = medResponse.getMedicines();
        
        Assert.assertTrue(medicines.size() >= 2, "Need at least 2 items to verify sort");

        for (int i = 0; i < medicines.size() - 1; i++) {
            double currentPrice = medicines.get(i).getDiscountPrice();
            double nextPrice = medicines.get(i + 1).getDiscountPrice();
            Assert.assertTrue(currentPrice >= nextPrice, 
                    String.format("Prices are not sorted descending: Index %d (%f) < Index %d (%f)", 
                            i, currentPrice, i + 1, nextPrice));
        }

        logger.info("Finished Test: testSortByPriceDescending - PASSED");
    }
}
