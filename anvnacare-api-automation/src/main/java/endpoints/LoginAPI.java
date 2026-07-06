package endpoints;

import base.BaseAPI;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.request.LoginRequest;

/**
 * LoginAPI encapsulates all network request calls directed to the login endpoint.
 * 
 * Why do we need it?
 * To hide Rest Assured syntax (.given(), .when(), .then()) from the test classes. The test classes 
 * should only trigger actions and get results back, keeping tests readable.
 * 
 * Where is it used?
 * Called by LoginTests during the "Act" phase of the AAA pattern.
 * 
 * Why is this approach better?
 * If we change the HTTP method (e.g. from POST to PUT) or add custom headers specifically for 
 * login, we only edit this class without breaking the test logic in our test classes.
 */
public class LoginAPI {

    /**
     * Executes a POST request to login.
     * 
     * @param payload Login request payload object
     * @return REST Assured Response object
     */
    public static Response performLogin(LoginRequest payload) {
        return RestAssured.given()
                .spec(BaseAPI.getRequestSpec())
                .body(payload)
                .when()
                .post(Routes.LOGIN);
    }
}
