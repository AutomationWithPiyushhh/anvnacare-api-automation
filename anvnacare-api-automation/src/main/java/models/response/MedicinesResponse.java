package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * MedicinesResponse represents the deserialized JSON response of the medicines catalog API.
 * 
 * Why do we need it?
 * To validate the list of medicines returned when searching, filtering, or listing catalog items.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicinesResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("medicines")
    private List<Medicine> medicines;

    public MedicinesResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Medicine> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;
    }
}
