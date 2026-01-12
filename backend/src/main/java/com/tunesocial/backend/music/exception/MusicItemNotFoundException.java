package com.tunesocial.backend.music.exception;

public class MusicItemNotFoundException extends RuntimeException {
    public MusicItemNotFoundException(String message) {
        super(message);
    }

    public MusicItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
