package models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CartRequest represents the payload for managing cart items (add, update, remove).
 * 
 * Why do we need it?
 * To model payload bodies for cart interactions in a typed structure.
 * 
 * Why is this approach better?
 * Using @JsonInclude(JsonInclude.Include.NON_NULL) ensures that when performing a "remove" 
 * action, we can omit the "quantity" field entirely if it's null, aligning with the expected payload 
 * of the DELETE API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartRequest {

    @JsonProperty("action")
    private String action;

    @JsonProperty("item_id")
    private int itemId;

    @JsonProperty("item_type")
    private String itemType;

    @JsonProperty("quantity")
    private Integer quantity; // Integer allows it to be null for deletion payloads

    public CartRequest() {}

    public CartRequest(String action, int itemId, String itemType, Integer quantity) {
        this.action = action;
        this.itemId = itemId;
        this.itemType = itemType;
        this.quantity = quantity;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
