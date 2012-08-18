import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

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
	
	public void send(PacketInjector packetInjector, InetAddress receiver, String artificialPayload) throws Exception {
		NetworkInterface device = NetworkInterface.getByName(packetInjector.getOptions().getDevice());
        DatagramSocket socket = new DatagramSocket(this.srcPortNumber, device.getInetAddresses().nextElement());
        byte[] artif = Utility.encrypt(this.payload + artificialPayload);
        DatagramPacket sendpacket = new DatagramPacket(artif, artif.length, receiver, this.dstPortNumber);
        try{
            socket.send(sendpacket);
        }
        catch(SocketException ex) {
            //ex.printStackTrace();
        }
        socket.close();
	}

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
}
