import java.net.InetAddress;

/**
 * 
 */

/**
 * @author Ivano Malavolta - 169201
 *
 */
public interface Packet {
	
	public void send(PacketInjector packetInjector, InetAddress receiver, String artificialPayload) throws PacketInjectorException, Exception;

}
