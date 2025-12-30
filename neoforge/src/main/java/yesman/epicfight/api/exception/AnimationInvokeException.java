package yesman.epicfight.api.exception;

public class AnimationInvokeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AnimationInvokeException(String message) {
        super(message);
    }
    
    public AnimationInvokeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AnimationInvokeException(Throwable cause) {
        super(cause);
    }
}
