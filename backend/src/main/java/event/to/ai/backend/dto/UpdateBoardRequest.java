package event.to.ai.backend.dto;

import jakarta.validation.constraints.Size;

public class UpdateBoardRequest {

    @Size(max = 200, message = "Board title must not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    public UpdateBoardRequest() {
    }

    public UpdateBoardRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
