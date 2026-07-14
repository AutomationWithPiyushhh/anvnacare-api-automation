package endpoints;

import base.BaseAPI;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.request.RegisterRequest;

/**
 * RegisterAPI encapsulates HTTP operations aimed at user registration.
 * 
 * Why do we need it?
 * Separates endpoint HTTP request specifics (like POST method, body structures, and path routes) 
 * from the logical TestNG test verification methods.
 */
public class RegisterAPI {

    /**
     * Executes a POST request to register a user.
     * 
     * @param payload Register request payload POJO
     * @return Rest Assured Response object
     */
    public static Response performRegistration(RegisterRequest payload) {
        return RestAssured
        		.given()
	        		.log().all()
	                .spec(BaseAPI.getRequestSpec())
	                .body(payload)
                .when()
                	.post(Routes.REGISTER);
    }
}
