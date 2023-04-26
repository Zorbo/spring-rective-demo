package hu.upscale.spring.demo.exception;

/**
 * @author László Zoltán
 */
public class CompressionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CompressionException(String message, Throwable cause) {
        super(message, cause);
    }
}
