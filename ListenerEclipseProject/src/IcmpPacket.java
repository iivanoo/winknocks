import java.net.InetAddress;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class IcmpPacket implements Packet {
	
	private int type;
	private int code;
	private String payload;
	private String id = "";
	private String timestamp = "";
	private String urgentScript = "";
	private InetAddress address;
	
	IcmpPacket(Node root) {
		NodeList list = root.getChildNodes();
		this.type = Integer.parseInt(list.item(1).getTextContent().trim());
		this.code = Integer.parseInt(list.item(3).getTextContent().trim());
		this.payload = list.item(5).getTextContent().trim();
	}
	
	/**
	 * 
	 */
	public IcmpPacket() {}

	public String toString() {
		return ("ICMPpacket:\n\t\ttype: " + this.type 
				+ "\n\t\tcode:" + this.code 
				+ "\n\t\tdata: " + this.payload);
	}

	/**
	 * @return Returns the code.
	 */
	public int getCode() {
		return this.code;
	}

	/**
	 * @param code The code to set.
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * @return Returns the payload.
	 */
	public String getPayload() {
		return this.payload;
	}

	/**
	 * @param payload The payload to set.
	 */
	public void setPayload(String payload) {
		int index = payload.indexOf("<id>");
		if(index != -1) {
			this.payload = payload.substring(0, index);
		}
		int start = payload.indexOf("<id>");
		int end = payload.indexOf("</id>");
		if((start != -1) && (end != -1)) {
			this.id = payload.substring(start + 4, end); 
		}
		start = payload.indexOf("<command>");
		end = payload.indexOf("</command>");
		if((start != -1) && (end != -1)) {
			this.urgentScript = payload.substring(start + 9, end); 
		}
		start = payload.indexOf("<time>");
		end = payload.indexOf("</time>");
		if((start != -1) && (end != -1)) {
			this.timestamp = payload.substring(start + 6, end); 
		}
	}
	
	public void setForcedPayload(String payload) {
		this.payload = payload;
	}

	
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}

	public String getMyFilter() {
		return "(icmp[icmptype] = " + this.type + " and icmp[icmpcode] = " + this.code + ")";
	}
	
	public boolean equals(Packet packet) {
		IcmpPacket temp = null;
		if(packet instanceof IcmpPacket) {
			temp = (IcmpPacket) packet;
		} else {
			return false;
		}
		return ((this.code == temp.code) &&
				(this.type == temp.type) &&
				(this.payload.equals(temp.payload)));
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return Returns the timestamp.
	 */
	public String getTimestamp() {
		return this.timestamp;
	}

	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return Returns the urgentScript.
	 */
	public String getUrgentScript() {
		return this.urgentScript;
	}

	/**
	 * @param urgentScript The urgentScript to set.
	 */
	public void setUrgentScript(String urgentScript) {
		this.urgentScript = urgentScript;
	}

	/**
	 * @return Returns the address.
	 */
	public InetAddress getAddress() {
		return this.address;
	}

	/**
	 * @param address The address to set.
	 */
	public void setAddress(InetAddress address) {
		this.address = address;
	}
}
