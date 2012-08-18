import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class ExecuteScriptAction extends Action {
	private String script;
	private int wait;
	
	ExecuteScriptAction(Node root) {
		NodeList list = root.getChildNodes();
		this.script = list.item(1).getTextContent().trim();
		if (list.getLength() >= 4) {
			this.wait = Integer.parseInt(list.item(3).getTextContent().trim());
		}
	}
	
	public String toString() {
		return ("executeScriptAction:\n\t\tscript: " + this.script 
				+ "\n\t\twait:" + this.wait);
	}
}
