import java.net.InetAddress;

/**
 * @author Ivano Malavolta - 169201
 *
 */
public interface Packet {
	
	public String getMyFilter();
	public void setPayload(String payload);
	public String getPayload();
	public String getId();
	public void setId(String id);
	public String getTimestamp();
	public void setTimestamp(String time); 
	public String getUrgentScript();
	public void setUrgentScript(String urgentScript);
	public boolean equals(Packet packet);
	public void setAddress(InetAddress address);
	public InetAddress getAddress();
	public void setForcedPayload(String payload);
}
