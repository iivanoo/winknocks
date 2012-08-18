import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class ClosePortAction extends Action {
	private int portNumber;
	private int wait;
	private int timeout;
	
	ClosePortAction(Node root) throws TimeWaitException {
		NodeList list = root.getChildNodes();
		this.portNumber = Integer.parseInt(list.item(1).getTextContent().trim());
		if (list.getLength() >= 4 ) {
			if(list.item(3).getNodeName().equals("wait")) {
				this.wait = Integer.parseInt(list.item(3).getTextContent().trim());
			} else {
				this.timeout = Integer.parseInt(list.item(3).getTextContent().trim());
			}
			if (list.getLength() >= 6) {
				if(list.item(5).getNodeName().equals("wait")) {
					this.wait = Integer.parseInt(list.item(5).getTextContent().trim());
				} else {
					this.timeout = Integer.parseInt(list.item(5).getTextContent().trim());
				}
			}
			if (!this.checkTimeout()) {
				throw new TimeWaitException();
			}
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
		return ("closePortAction:\n\t\tportNumber: " + this.portNumber 
				+ "\n\t\twait:" + this.wait + "\n\t\ttimeout: " + this.timeout);
	}
}
