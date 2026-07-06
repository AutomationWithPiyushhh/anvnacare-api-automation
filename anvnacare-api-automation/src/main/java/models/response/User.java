package models.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User represents the nested user details returned within the successful login/registration responses.
 * 
 * Why do we need it?
 * To parse the user object inside the JSON response into a typed Java class.
 */
public class User {

    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("role")
    private String role;

    // Default constructor for Jackson deserialization
    public User() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
