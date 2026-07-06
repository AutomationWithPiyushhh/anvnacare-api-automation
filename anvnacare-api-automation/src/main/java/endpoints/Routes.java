package endpoints;

/**
 * Routes holds the relative paths for all the endpoints in the ANVNACare application.
 * 
 * Why do we need it?
 * To avoid hardcoding endpoint URIs inside the API action classes.
 * 
 * Where is it used?
 * Used by all endpoint-specific action classes (e.g. LoginAPI, RegisterAPI, CartAPI) 
 * to resolve the request paths.
 * 
 * Why is this approach better?
 * Centralizing all routes makes it extremely easy to update endpoints if the backend API 
 * versioning or path structure changes.
 */
public class Routes {

    public static final String LOGIN = "api/login.php";
    public static final String REGISTER = "api/register.php";
    public static final String MEDICINES = "api/medicines.php";
    public static final String SEARCH = "api/search.php";
    public static final String CART = "api/cart.php";
}
