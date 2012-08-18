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
	
	
	public void send(PacketInjector packetInjector, InetAddress receiver, String payload) throws PacketInjectorException, Exception {		
		packetInjector.sendIcmpPacket(this, receiver, payload);
	}

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
	public void setPayload(String data) {
		this.payload = data;
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
}
