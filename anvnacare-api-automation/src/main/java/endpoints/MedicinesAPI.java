package endpoints;

import base.BaseAPI;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.Map;

/**
 * MedicinesAPI encapsulates HTTP calls for the catalog/medicines queries.
 * 
 * Why do we need it?
 * To keep catalog querying logic isolated and simple, making it easy to call from test classes 
 * with various filters.
 */
public class MedicinesAPI {

    /**
     * Fetches medicines list with no parameters.
     */
    public static Response getAllMedicines() {
        return RestAssured.given()
                .spec(BaseAPI.getRequestSpec())
                .when()
                .get(Routes.MEDICINES);
    }

    /**
     * Fetches medicines list with customized query filters.
     * 
     * @param queryParams Map of query parameter names and values
     * @return REST Assured Response
     */
    public static Response getMedicines(Map<String, ?> queryParams) {
        return RestAssured.given()
                .spec(BaseAPI.getRequestSpec())
                .queryParams(queryParams)
                .when()
                .get(Routes.MEDICINES);
    }
}
