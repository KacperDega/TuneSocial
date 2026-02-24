package com.tunesocial.backend.integration.genius.exception;

public class GeniusRateLimitException extends GeniusException {
    private final Long retryAfter;

    private static Long convertRetryAfter(Long retryAfter) {
        if (retryAfter == null || retryAfter <= 0) {
            return null;
        }
        return retryAfter;
    }

    public GeniusRateLimitException(Long retryAfter) {
        super("Genius API rate limit exceeded, try again in " + retryAfter);
        this.retryAfter = convertRetryAfter(retryAfter);
    }

    public GeniusRateLimitException(Throwable cause, Long retryAfter) {
        super("Genius API rate limit exceeded, try again in " + retryAfter, cause);
        this.retryAfter = convertRetryAfter(retryAfter);
    }

    public Long getRetryAfterMillis() {
        if (retryAfter == null) {
            return null;
        }
        return retryAfter * 1000;
    }
}
