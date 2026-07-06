package models.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RegisterRequest represents the JSON request body for registering a new user.
 * 
 * Why do we need it?
 * To model the user registration input parameters in a strongly-typed Java POJO class,
 * which will be serialized into JSON by REST Assured and Jackson.
 */
public class RegisterRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("password")
    private String password;

    // Default constructor for Jackson
    public RegisterRequest() {}

    public RegisterRequest(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
