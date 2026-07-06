package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * LoginResponse represents the deserialized Java representation of the login response JSON.
 * 
 * Why do we need it?
 * To validate the fields returned by the server (e.g. success flag, welcome message, user details)
 * using typed getters, avoiding manual JSON path extractions.
 * 
 * Why is this approach better?
 * Adding @JsonIgnoreProperties(ignoreUnknown = true) ensures that if the server returns extra 
 * fields or fails to return certain fields (like the nested user object in case of invalid login), 
 * Jackson won't throw UnrecognizedPropertyException, making the parser resilient.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("user")
    private User user;

    // Default constructor for Jackson deserialization
    public LoginResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
