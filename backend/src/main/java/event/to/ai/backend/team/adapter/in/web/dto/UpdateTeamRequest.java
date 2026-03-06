package event.to.ai.backend.team.adapter.in.web.dto;

import jakarta.validation.constraints.Size;

public class UpdateTeamRequest {

    @Size(max = 200, message = "Team name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    public UpdateTeamRequest() {
    }

    public UpdateTeamRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
