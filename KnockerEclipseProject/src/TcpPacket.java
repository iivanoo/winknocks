import java.net.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class TcpPacket implements Packet {
	
	private int srcPortNumber;
	private int dstPortNumber;
	private int sequenceNumber;
	private int ackNumber;
	private int windowSize;
	private boolean ack;
	private boolean syn;
	private boolean fin;
	private boolean push;
	private boolean reset;
	private boolean urgent;
	private String payload;
	
	TcpPacket(Node root) {
		NodeList list = root.getChildNodes();
		this.srcPortNumber = Integer.parseInt(list.item(1).getTextContent().trim());
		this.dstPortNumber = Integer.parseInt(list.item(3).getTextContent().trim());
		this.sequenceNumber = Integer.parseInt(list.item(5).getTextContent().trim());
		this.ackNumber = Integer.parseInt(list.item(7).getTextContent().trim());
		this.windowSize = Integer.parseInt(list.item(9).getTextContent().trim());
		Node flags = list.item(11);
		String temp = flags.getChildNodes().item(1).getFirstChild().getTextContent().trim();
		if (temp.equals("1")) {
			this.ack = true;
		}
		temp = flags.getChildNodes().item(3).getFirstChild().getTextContent().trim();
		if (temp.equals("1")) {
			this.syn = true;
		}
		temp = flags.getChildNodes().item(5).getFirstChild().getTextContent().trim();
		if (temp.equals("1")) {
			this.fin = true;
		}
		temp = flags.getChildNodes().item(7).getFirstChild().getTextContent().trim();
		if (temp.equals("1")) {
			this.push = true;
		}
		temp = flags.getChildNodes().item(9).getFirstChild().getTextContent().trim();
		if (temp.equals("1")) {
			this.reset = true;
		}
		temp = flags.getChildNodes().item(11).getFirstChild().getTextContent().trim();
		if (temp.equals("1")) {
			this.urgent = true;
		}
		if (list.getLength() >= 14) {
			this.payload = list.item(13).getTextContent().trim();
		}
	}
	
	public TcpPacket() {}
	
	public void send(PacketInjector packetInjector, InetAddress receiver, String payload) throws PacketInjectorException {
		packetInjector.sendTcpPacket(this, receiver, payload);
	}

	public String toString() {
		return ("TCPpacket:\n\t\tsrcPortNumber: " + this.srcPortNumber 
				+ "\n\t\tdstPortNumber: " + this.dstPortNumber 
				+ "\n\t\tseqNumber: " + this.sequenceNumber
				+ "\n\t\tackNumber: " + this.ackNumber 
				+ "\n\t\twindowSize: " + this.windowSize
				+ "\n\t\tACK: " + this.ack 
				+ "\n\t\tSYN: " + this.syn
				+ "\n\t\tFIN: " + this.fin 
				+ "\n\t\tPUSH: " + this.push
				+ "\n\t\tRESET: " + this.reset 
				+ "\n\t\tURGENT: " + this.urgent
				+ "\n\t\tpayload: " + this.payload);
	}

	/**
	 * @return Returns the ack.
	 */
	public boolean isAck() {
		return this.ack;
	}

	/**
	 * @param ack The ack to set.
	 */
	public void setAck(boolean ack) {
		this.ack = ack;
	}

	/**
	 * @return Returns the ackNumber.
	 */
	public int getAckNumber() {
		return this.ackNumber;
	}

	/**
	 * @param ackNumber The ackNumber to set.
	 */
	public void setAckNumber(int ackNumber) {
		this.ackNumber = ackNumber;
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
	 * @return Returns the fin.
	 */
	public boolean isFin() {
		return this.fin;
	}

	/**
	 * @param fin The fin to set.
	 */
	public void setFin(boolean fin) {
		this.fin = fin;
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
		this.payload = payload;
	}

	/**
	 * @return Returns the push.
	 */
	public boolean isPush() {
		return this.push;
	}

	/**
	 * @param push The push to set.
	 */
	public void setPush(boolean push) {
		this.push = push;
	}

	/**
	 * @return Returns the reset.
	 */
	public boolean isReset() {
		return this.reset;
	}

	/**
	 * @param reset The reset to set.
	 */
	public void setReset(boolean reset) {
		this.reset = reset;
	}

	/**
	 * @return Returns the sequenceNumber.
	 */
	public int getSequenceNumber() {
		return this.sequenceNumber;
	}

	/**
	 * @param sequenceNumber The sequenceNumber to set.
	 */
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
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

	/**
	 * @return Returns the syn.
	 */
	public boolean isSyn() {
		return this.syn;
	}

	/**
	 * @param syn The syn to set.
	 */
	public void setSyn(boolean syn) {
		this.syn = syn;
	}

	/**
	 * @return Returns the urgent.
	 */
	public boolean isUrgent() {
		return this.urgent;
	}

	/**
	 * @param urgent The urgent to set.
	 */
	public void setUrgent(boolean urgent) {
		this.urgent = urgent;
	}

	/**
	 * @return Returns the windowSize.
	 */
	public int getWindowSize() {
		return this.windowSize;
	}

	/**
	 * @param windowSize The windowSize to set.
	 */
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}
}
