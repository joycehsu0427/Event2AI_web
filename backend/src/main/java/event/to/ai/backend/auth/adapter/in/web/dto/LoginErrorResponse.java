package event.to.ai.backend.auth.adapter.in.web.dto;

public class LoginErrorResponse {

    private String errorCode;
    private String message;

    public LoginErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
