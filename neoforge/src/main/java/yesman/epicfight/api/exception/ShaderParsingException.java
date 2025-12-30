package yesman.epicfight.api.exception;

public class ShaderParsingException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    public ShaderParsingException(String message) {
        super(message);
    }
    
    public ShaderParsingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ShaderParsingException(Throwable cause) {
        super(cause);
    }
}
