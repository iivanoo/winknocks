import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class OpenPortAction extends Action {

	private int portNumber;
	private int wait;
	private int timeout;
	private boolean exclusive;
	
	OpenPortAction(Node root) throws TimeWaitException {
		NodeList list = root.getChildNodes();
		this.portNumber = Integer.parseInt(list.item(1).getTextContent().trim());
		this.exclusive = Boolean.parseBoolean(list.item(3).getTextContent().trim());
		if (list.getLength() >= 6 ) {
			if(list.item(5).getNodeName().equals("wait")) {
				this.wait = Integer.parseInt(list.item(5).getTextContent().trim());
			} else {
				this.timeout = Integer.parseInt(list.item(5).getTextContent().trim());
			}
			if (list.getLength() >= 8) {
				if(list.item(7).getNodeName().equals("wait")) {
					this.wait = Integer.parseInt(list.item(7).getTextContent().trim());
				} else {
					this.timeout = Integer.parseInt(list.item(7).getTextContent().trim());
				}
			}
		}
		if (!this.checkTimeout()) {
			throw new TimeWaitException();
		}
	}
	
	/**
	 * @return true if timeout is less or equal to wait
	 */
	private boolean checkTimeout() {
		if (timeout != 0) {
			return (this.timeout >= this.wait);
		}
		return true;
	}
	
	public String toString() {
		return ("openPortAction:\n\t\tportNumber: " + this.portNumber + "\n\t\texclusive: " + this.exclusive
				+ "\n\t\twait:" + this.wait + "\n\t\ttimeout: " + this.timeout);
	}

	/**
	 * @return Returns the exclusive.
	 */
	public boolean isExclusive() {
		return this.exclusive;
	}

	/**
	 * @param exclusive The exclusive to set.
	 */
	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}

	/**
	 * @return Returns the portNumber.
	 */
	public int getPortNumber() {
		return this.portNumber;
	}

	/**
	 * @param portNumber The portNumber to set.
	 */
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	/**
	 * @return Returns the timeout.
	 */
	public int getTimeout() {
		return this.timeout;
	}

	/**
	 * @param timeout The timeout to set.
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return Returns the wait.
	 */
	public int getWait() {
		return this.wait;
	}

	/**
	 * @param wait The wait to set.
	 */
	public void setWait(int wait) {
		this.wait = wait;
	}
}
