package RateGuard.model;

public class HealthResponse {

    private String status;
    private String message;
    private String redis;

    public HealthResponse(String status, String message, String redis) {
        this.status = status;
        this.message = message;
        this.redis = redis;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getRedis() {
        return redis;
    }
}