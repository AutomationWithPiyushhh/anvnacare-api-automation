package base;

import config.ConfigReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import utils.SessionManager;

/**
 * BaseAPI contains Request and Response Specification builders for Rest Assured.
 * 
 * Why do we need it?
 * To eliminate redundant specification configurations (like Base URI, headers, cookies, and loggers) 
 * for every single API request.
 * 
 * Where is it used?
 * Inherited or called by all Endpoint classes (e.g., LoginAPI, RegisterAPI, CartAPI) 
 * to fetch uniform, pre-configured Request and Response specifications.
 * 
 * Why is this approach better?
 * Separating non-authenticated and authenticated specs (role-based) makes session management 
 * transparent. Also, attaching standard logging filters ensures that every request and response 
 * is logged in the console and framework logs for debugging.
 */
public class BaseAPI {

    /**
     * Returns a standard RequestSpecification for anonymous/public requests.
     * Includes Content-Type, Accept headers, and logging filters.
     * 
     * @return RequestSpecification
     */
    public static RequestSpecification getRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigReader.getBaseUrl())
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                // Automatic logging of request details
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /**
     * Returns an authenticated RequestSpecification for a specific user role.
     * Automatically extracts and injects the PHPSESSID cookie.
     * 
     * @param role User role name ("user" or "admin")
     * @return RequestSpecification
     */
    public static RequestSpecification getRequestSpec(String role) {
        String sessionCookie = SessionManager.getSessionCookie(role);
        
        return new RequestSpecBuilder()
                .setBaseUri(ConfigReader.getBaseUrl())
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                // Inject the session cookie retrieved from SessionManager
                .addCookie("PHPSESSID", sessionCookie)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /**
     * Returns a standard ResponseSpecification that asserts common response traits,
     * such as an expected content type.
     * 
     * @return ResponseSpecification
     */
    public static ResponseSpecification getResponseSpec() {
        return new ResponseSpecBuilder()
                // Do not enforce exact content type match since some hosts might append charset
                .expectContentType(io.restassured.http.ContentType.JSON)
                .build();
    }
}
