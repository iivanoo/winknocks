/**
 * 
 */

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class ExecuteScriptException extends Exception {
private static final long serialVersionUID = 1L;
	
	private String message;
	
	public ExecuteScriptException(String message) {
		this.message = message;
	}

	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * @param message The message to set.
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
