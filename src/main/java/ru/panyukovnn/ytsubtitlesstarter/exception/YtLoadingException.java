package ru.panyukovnn.ytsubtitlesstarter.exception;

public class YtLoadingException extends RuntimeException {

    private final String id;

    public YtLoadingException(String id, String message) {
        super(message);
        this.id = id;
    }

    public YtLoadingException(String id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
