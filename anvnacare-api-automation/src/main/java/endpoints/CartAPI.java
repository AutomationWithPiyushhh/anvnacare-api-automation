package endpoints;

import base.BaseAPI;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.request.CartRequest;

/**
 * CartAPI isolates the Rest Assured calls used to manage the customer shopping cart.
 * 
 * Why do we need it?
 * To consolidate all cart request executions (authenticated vs unauthenticated, updates vs deletions).
 * 
 * Where is it used?
 * Triggered by CartTests.
 */
public class CartAPI {

    /**
     * Adds an item to the cart using an authenticated role.
     */
    public static Response addToCart(CartRequest payload, String role) {
        return RestAssured.given()
                .spec(BaseAPI.getRequestSpec(role))
                .body(payload)
                .when()
                .post(Routes.CART);
    }

    /**
     * Adds an item to the cart without logging in (anonymous check).
     */
    public static Response addToCartUnauthenticated(CartRequest payload) {
        return RestAssured.given()
                .spec(BaseAPI.getRequestSpec())
                .body(payload)
                .when()
                .post(Routes.CART);
    }

    /**
     * Updates item quantity in the cart using an authenticated role.
     */
    public static Response updateCart(CartRequest payload, String role) {
        return RestAssured.given()
                .spec(BaseAPI.getRequestSpec(role))
                .body(payload)
                .when()
                .post(Routes.CART);
    }

    /**
     * Removes an item from the cart using an authenticated role.
     */
    public static Response removeCartItem(CartRequest payload, String role) {
        return RestAssured.given()
                .spec(BaseAPI.getRequestSpec(role))
                .body(payload)
                .when()
                .delete(Routes.CART);
    }
}
