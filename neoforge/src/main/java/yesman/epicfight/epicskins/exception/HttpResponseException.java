package yesman.epicfight.epicskins.exception;

public class HttpResponseException extends RuntimeException {
	private final int statusCode;
	private final String responseBody;
	
	public HttpResponseException(String message, int statusCode, String responseBody) {
		super(message);
		
		this.statusCode = statusCode;
		this.responseBody = responseBody;
	}
	
	public int getHttpStatusCode() {
		return this.statusCode;
	}
	
	public String getResponseBody() {
		return this.responseBody;
	}
}
