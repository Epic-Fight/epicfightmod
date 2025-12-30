package yesman.epicfight.api.exception;

public class AssetLoadingException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AssetLoadingException(String message) {
		super(message);
	}
	
	public AssetLoadingException(String message, Throwable ex) {
		super(message, ex);
	}
}
