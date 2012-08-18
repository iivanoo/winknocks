import java.net.InetAddress;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class ClosePortAction implements Action, Runnable {
	
	private int portNumber;
	private int wait;
	private int timeout;
	
	private FirewallException ex = null;
	
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
	
	public void execute(InetAddress address) throws FirewallException, ExecuteScriptException {
		Thread t = new Thread(this);
		t.start();
		if(this.ex != null) {
			FirewallException e = new FirewallException(this.ex.getMessage());
			this.ex = null;
			throw e;
		}
	}
	
	public String toString() {
		return ("closePortAction:\n\t\tportNumber: " + this.portNumber 
				+ "\n\t\twait:" + this.wait + "\n\t\ttimeout: " + this.timeout);
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


	public void run() {
		String state = "";
		String[] command = {"netsh", "firewall", "add", "portopening", "ALL", String.valueOf(this.portNumber), "winKnocksRule", "DISABLE", "", ""};
		try {
			if(this.wait != 0) {
				Thread.sleep(this.wait * 1000);
			}
			state = Utility.executeCommand(command);
		} catch (Exception e) {}
		
		if(!state.contains("OK")){
			this.ex = new FirewallException("there is a problem with the firewall while closing port " + this.portNumber);
		}
		
		try {
			if(this.timeout != 0) {
				command[2] = "delete";
				command[6] = "";
				command[7] = "";
				command[8] = "";
				command[9] = "";
				Thread.sleep(this.timeout * 1000);
				state = Utility.executeCommand(command);
			}
		} catch (Exception e) {}
		
		if(!state.contains("OK")){
			this.ex = new FirewallException("there is a problem with the firewall while opening port " + this.portNumber);
		}
	}
}
