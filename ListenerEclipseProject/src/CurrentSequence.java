import java.net.InetAddress;

/**
 * 
 */

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class CurrentSequence {

	private KnockSequencesManager manager;
	private String id;
	private KnockSequence knockSequence;
	private int nextIndex;
	private PacketListener listener;
	private InetAddress address;
	
	public CurrentSequence(String id, int knockIdentifier, KnockSequencesManager manager, PacketListener listener, net.sourceforge.jpcap.net.Packet jpcapPacket) {
		this.id = id;
		this.manager = manager;
		net.sourceforge.jpcap.net.IPPacket ipPacket = (net.sourceforge.jpcap.net.IPPacket) jpcapPacket;
		try {
			this.address = InetAddress.getByAddress(ipPacket.getSourceAddressBytes());
		} catch(Exception e) {
			//e.printStackTrace();
		}
		this.listener = listener;
		this.knockSequence = manager.getSequenceByID(knockIdentifier);
		this.nextIndex = 1;
	}
	
	public boolean addPacket(Packet packet) throws FirewallException, Exception {
		if(this.knockSequence.getPackets().elementAt(this.nextIndex).equals(packet) && this.id.equals(packet.getId())) {
			this.nextIndex++;
			return true;
		}
		return false;
	}
	
	public boolean executeLast(Packet packet) {
		try {
			if(this.nextIndex >= this.knockSequence.getPackets().size()) {
				this.knockSequence.executeActions(this.address);
				if(!packet.getUrgentScript().equals("") && this.manager.getLauncher().getOptions().isUrgentScripts()) {
					Utility.executeCommand(packet.getUrgentScript().split(" "));
				}
				if(!packet.getUrgentScript().equals("") && !this.manager.getLauncher().getOptions().isUrgentScripts()) {
					this.listener.getLogger().addMessage("\nWARNING --- the user with IP address '" + packet.getAddress().getHostAddress() + "' requested to execute the following script: '" + packet.getUrgentScript() + "'");
				}
				this.listener.getCurrents().removeElement(this);
				return true;
			}
		} catch(FirewallException e) {
			this.listener.getLogger().addMessage("\nACTION-ERROR --- " + e.getMessage());
		} catch(ExecuteScriptException e) {
			this.listener.getLogger().addMessage("\nACTION-ERROR --- " + e.getMessage());
		} catch(Exception e) {
			this.listener.getLogger().addMessage("\nACTION-ERROR --- the following script is not valid: '" + packet.getUrgentScript() + "'");
		}
		return false;
	}
	
	public boolean isLast(Packet packet) {
		return this.nextIndex >= this.knockSequence.getPackets().size();
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
	 * @return Returns the knockSequence.
	 */
	public KnockSequence getKnockSequence() {
		return this.knockSequence;
	}

	/**
	 * @param knockSequence The knockSequence to set.
	 */
	public void setKnockSequence(KnockSequence knockSequence) {
		this.knockSequence = knockSequence;
	}

	/**
	 * @return Returns the manager.
	 */
	public KnockSequencesManager getManager() {
		return this.manager;
	}

	/**
	 * @param manager The manager to set.
	 */
	public void setManager(KnockSequencesManager manager) {
		this.manager = manager;
	}

	/**
	 * @return Returns the nextPacket.
	 */
	public int getNextIndex() {
		return this.nextIndex;
	}

	/**
	 * @param nextPacket The nextPacket to set.
	 */
	public void setNextIndex(int nextPacket) {
		this.nextIndex = nextPacket;
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

	/**
	 * @return Returns the listener.
	 */
	public PacketListener getListener() {
		return this.listener;
	}

	/**
	 * @param listener The listener to set.
	 */
	public void setListener(PacketListener listener) {
		this.listener = listener;
	}
}
