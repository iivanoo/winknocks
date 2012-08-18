/**
 * 
 */

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class PacketInjectorException extends Exception {

	private static final long serialVersionUID = 1L;

	private String error;
	private String description;
	
	public PacketInjectorException(String error, String description) {
		this.error = error;
		this.description = description;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the error.
	 */
	public String getError() {
		return this.error;
	}

	/**
	 * @param error The error to set.
	 */
	public void setError(String error) {
		this.error = error;
	}
}
