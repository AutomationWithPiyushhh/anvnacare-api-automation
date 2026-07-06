package models.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * LoginRequest represents the JSON request payload for the login endpoint.
 * 
 * Why do we need it?
 * Strong typing and POJO representation of JSON request bodies. Instead of sending raw JSON 
 * strings or using untyped Maps, we instantiate this class. Rest Assured automatically serializes 
 * this object into a JSON string using Jackson.
 * 
 * Where is it used?
 * Instantiated by LoginPayloads and sent by LoginAPI to the "/api/login.php" endpoint.
 */
public class LoginRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("remember")
    private boolean remember;

    // Default constructor (required by Jackson for deserialization if needed)
    public LoginRequest() {}

    public LoginRequest(String email, String password, boolean remember) {
        this.email = email;
        this.password = password;
        this.remember = remember;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRemember() {
        return remember;
    }

    public void setRemember(boolean remember) {
        this.remember = remember;
    }
}
