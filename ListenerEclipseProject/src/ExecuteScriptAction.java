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
public class ExecuteScriptAction implements Action, Runnable {
	
	private String script;
	private int wait;
	
	private ExecuteScriptException ex = null;
	
	ExecuteScriptAction(Node root) {
		NodeList list = root.getChildNodes();
		this.script = list.item(1).getTextContent().trim();
		if (list.getLength() >= 4) {
			this.wait = Integer.parseInt(list.item(3).getTextContent().trim());
		}
	}
	
	public void execute(InetAddress address) throws FirewallException, ExecuteScriptException {
		Thread t = new Thread(this);
		t.start();
		if(this.ex != null) {
			ExecuteScriptException e = new ExecuteScriptException(this.ex.getMessage());
			this.ex = null;
			throw e;
		}
		
	}
	
	public String toString() {
		return ("executeScriptAction:\n\t\tscript: " + this.script 
				+ "\n\t\twait:" + this.wait);
	}

	/**
	 * @return Returns the script.
	 */
	public String getScript() {
		return this.script;
	}

	/**
	 * @param script The script to set.
	 */
	public void setScript(String script) {
		this.script = script;
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
		try {
			if(this.wait != 0) {
				Thread.sleep(this.wait * 1000);
			}
			String[] command = this.script.split(" ");
			command[0] = "\"" + command[0] + "\"";
			System.out.println(Utility.executeCommand(command));
		} catch(Exception e) {
			this.ex = new ExecuteScriptException("there is a problem executing the following script:  '" + this.script + "'");
		}
	}
}
