package RateGuard.model;

public class ApiResponse {

    private String message;
    private String clientId;
    private long remainingRequests;

    public ApiResponse(String message, String clientId, long remainingRequests) {
        this.message = message;
        this.clientId = clientId;
        this.remainingRequests = remainingRequests;
    }

    public String getMessage() {
        return message;
    }

    public String getClientId() {
        return clientId;
    }

    public long getRemainingRequests() {
        return remainingRequests;
    }
}