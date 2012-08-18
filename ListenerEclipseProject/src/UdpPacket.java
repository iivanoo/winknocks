import java.net.InetAddress;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class UdpPacket implements Packet {
	
	private int srcPortNumber;
	private int dstPortNumber;
	private String payload = "";
	private String id = "";
	private String timestamp = "";
	private String urgentScript = "";
	private InetAddress address;

	UdpPacket(Node root) {
		NodeList list = root.getChildNodes();
		this.srcPortNumber = Integer.parseInt(list.item(1).getTextContent().trim());
		this.dstPortNumber = Integer.parseInt(list.item(3).getTextContent().trim());
		if (list.getLength() >= 6) {
			this.payload = list.item(5).getTextContent().trim();
		}
	}
	
	/**
	 * 
	 */
	public UdpPacket() {}
	
	public String toString() {
		return ("UDPpacket:\n\t\tsrcPortNumber: " + this.srcPortNumber 
				+ "\n\t\tdstPortNumber:" + this.dstPortNumber + "\n\t\tpayload: " + this.payload);
	}

	/**
	 * @return Returns the dstPortNumber.
	 */
	public int getDstPortNumber() {
		return this.dstPortNumber;
	}

	/**
	 * @param dstPortNumber The dstPortNumber to set.
	 */
	public void setDstPortNumber(int dstPortNumber) {
		this.dstPortNumber = dstPortNumber;
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
	 * @return Returns the srcPortNumber.
	 */
	public int getSrcPortNumber() {
		return this.srcPortNumber;
	}

	/**
	 * @param srcPortNumber The srcPortNumber to set.
	 */
	public void setSrcPortNumber(int srcPortNumber) {
		this.srcPortNumber = srcPortNumber;
	}

	public String getMyFilter() {
		return "(udp dst port " + this.dstPortNumber + " and src port " + this.srcPortNumber + ")";
	}
	
	public boolean equals(Packet packet) {
		UdpPacket temp = null;
		if(packet instanceof UdpPacket) {
			temp = (UdpPacket) packet;
		} else {
			return false;
		}
		return ((this.dstPortNumber == temp.dstPortNumber) &&
				(this.srcPortNumber == temp.srcPortNumber) &&
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
