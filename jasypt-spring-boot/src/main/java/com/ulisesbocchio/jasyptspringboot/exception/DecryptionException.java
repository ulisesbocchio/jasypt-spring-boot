package com.ulisesbocchio.jasyptspringboot.exception;

import java.io.Serial;

/**
 * <p>DecryptionException class.</p>
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
public class DecryptionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for DecryptionException.</p>
     *
     * @param message a {@link java.lang.String} object
     */
    public DecryptionException(final String message) {
        super(message);
    }

    /**
     * <p>Constructor for DecryptionException.</p>
     *
     * @param message a {@link java.lang.String} object
     * @param cause   a {@link java.lang.Throwable} object
     */
    public DecryptionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
