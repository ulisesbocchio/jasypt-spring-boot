package com.ulisesbocchio.jasyptspringboot.exception;

/**
 * @author Ulises Bocchio
 */
public class DecryptionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DecryptionException(final String message) {
        super(message);
    }

    public DecryptionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
