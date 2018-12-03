package io.whalebone.publicapi.ejb.elastic;

public class ElasticSearchException extends RuntimeException {
    private static final long serialVersionUID = -2896313150237347683L;

    public ElasticSearchException(String message) {
        super(message);
    }

    public ElasticSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
