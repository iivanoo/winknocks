import net.sourceforge.jpcap.capture.*;
import net.sourceforge.jpcap.net.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class PacketListener implements net.sourceforge.jpcap.capture.PacketListener, Runnable {

	private PacketCapture captor;
	private String deviceName;
	private Thread thread;
	private Launcher launcher;
	private boolean backgroundMode;
	private boolean testMode;
	private String filter = "";
	private Vector<CurrentSequence> currents = new Vector<CurrentSequence>();
	private Logger logger;
	private Vector<String> usedTimestamps = new Vector<String>();
	
	public PacketListener(Launcher launcher, boolean backgroundMode, PacketCapture captor, boolean testMode) {
		this.captor = captor;
		this.testMode = testMode;
		this.deviceName = launcher.getOptions().getDevice();
		this.launcher = launcher;
		this.backgroundMode = backgroundMode;
		this.filter = launcher.getOptions().getCaptorFilter().toLowerCase();
		this.logger = new Logger(launcher);
	}
	
	public PacketListener() {}
	
	public void startListening() {
		this.thread = new Thread(this);
		this.thread.start();
	}
	
	public void stopListening() {
		this.captor.endCapture();
		this.captor.removePacketListener(this);
		if(this.testMode) {
			this.captor.close();
		}
		this.currents = new Vector<CurrentSequence>();
		this.logger.registerFile();
		this.usedTimestamps = new Vector<String>();
	}

	/**
	 * @return Returns the backgroundMode.
	 */
	public boolean isBackgroundMode() {
		return this.backgroundMode;
	}

	/**
	 * @param backgroundMode The backgroundMode to set.
	 */
	public void setBackgroundMode(boolean backgroundMode) {
		this.backgroundMode = backgroundMode;
	}

	/**
	 * @return Returns the launcher.
	 */
	public Launcher getLauncher() {
		return this.launcher;
	}

	/**
	 * @param launcher The launcher to set.
	 */
	public void setLauncher(Launcher launcher) {
		this.launcher = launcher;
	}

	/**
	 * @return Returns the filter.
	 */
	public String getFilter() {
		return this.filter;
	}

	/**
	 * @param filter The filter to set.
	 */
	public void setFilter(String filter) {
		this.filter = filter;
	}

	public void run() {
		this.initCaptor();
	}
	
	/**
	 * this method initialise the packets captor and start listening to incoming packets
	 */
	private void initCaptor() {
		try {
			if(!this.testMode) {
				String dev = launcher.getOptions().getDevice().substring(0, launcher.getOptions().getDevice().indexOf("\n"));
				String zeros = "";
				for(int i=0; i<33; i++) {
					zeros += "0";
				}
				String networkS = ((Integer.toBinaryString(this.captor.getNetwork(dev)) + zeros).substring(0, 32));
				String t1 = networkS.substring(0, 8);
				String t2 = networkS.substring(8, 16);
				networkS = t2 + t1 + networkS.substring(16);
				String netmaskS = ((Integer.toBinaryString(this.captor.getNetmask(dev)) + zeros).substring(0, 32));
				t1 = netmaskS.substring(0, 8);
				t2 = netmaskS.substring(8, 16);
				netmaskS = t2 + t1 + netmaskS.substring(16);
				String ind;
				Enumeration<NetworkInterface> in = NetworkInterface.getNetworkInterfaces();
				String address = "";
				while(in.hasMoreElements()) {
					ind = "";
					NetworkInterface temp = in.nextElement();
					if(temp.getInetAddresses().hasMoreElements()) {
						InetAddress ad = temp.getInetAddresses().nextElement();
						String[] addr = (ad.getHostAddress()).split("[.]");
						for(int i=0; i<4; i++) {
							String tmp = zeros + Integer.toBinaryString(Integer.valueOf(addr[i]));
							ind += tmp.substring(tmp.length() - 8);
						}
						if(Utility.logicalAnd(netmaskS, ind).equals(networkS)) {
							address = ad.getHostAddress();
						}
					}
				}
				if(address.equals("")) {
					address = System.getenv("COMPUTERNAME");
				}
				this.captor.setFilter("ip dst host " + address + " and "+this.filter, true);
			} else {
				this.captor.setFilter(this.filter, true);
			}
			this.captor.addPacketListener(this);
			this.captor.capture(-1);
		} catch(net.sourceforge.jpcap.capture.InvalidFilterException e) {
			if(!this.launcher.getOptions().isAutomatic()) {
				this.launcher.getConsoleOutput().append("\n\tERROR --- invalid filter(" + e.getMessage() + "), please check options file");
				this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
			} else {
				this.captor.addPacketListener(this);
				try{
					this.captor.capture(-1);
				}catch(Exception ex){
					//ex.printStackTrace();
				}
			}
			this.launcher.stopListening();
			//e.printStackTrace();
		} catch(Exception e) {
			this.launcher.getConsoleOutput().append("\n\tERROR --- unable to receive packet on the specified interface(" + this.deviceName.substring(this.deviceName.indexOf("\n") + 1) + "), please check the options file");
			this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
			this.launcher.stopListening();
			//e.printStackTrace();
		}
	}
	
	public void packetArrived(net.sourceforge.jpcap.net.Packet packet) {
		String payload = null;
		if(packet instanceof UDPPacket) {
			payload = Utility.decrypt(packet.getData());
		} else {
			if((packet.getData().length > 0) && (packet.getData()[packet.getData().length - 1] == 10)) {
				packet.getData()[packet.getData().length - 1] = 32;
			}
			String a = new String(packet.getData()).trim();
			if(packet instanceof ICMPPacket && a.length() > 3) {
				a = a.substring(4);
			}
			if(packet instanceof ICMPPacket && a.length() <= 3) {
				a = a.substring(a.length());
			}
			a = Utility.getFinalString(a);
			String[] arr = a.split("-");
			try {
				for(int i=0; i<arr.length; i++) {
					Integer.parseInt(arr[i]);
				}
				payload = Utility.decrypt(Utility.getBytes(a));
			} catch(NumberFormatException e) {
				payload = null;
			}
		}
		Packet p;
		if(payload == null) {
			p = this.getPacket(packet, "<id>");
			// light intrusion attempt
			this.logger.notifyPacketNotEncrypted(p, Calendar.getInstance().getTime());
		} else {
			p = this.getPacket(packet, payload);
			// check if the packets have been previously sent(replay attack)
			if(this.isReplayAttack(p)) {
				this.logger.notifyReplayAttack(p, Calendar.getInstance().getTime());
				return;
			}
			boolean inserted = false;
			for(int i=0;i<this.currents.size() && !inserted; i++) {
				try {
					if(this.currents.elementAt(i).addPacket(p)) {
						inserted = true;
						int id = this.currents.elementAt(i).getKnockSequence().getId();
						this.logger.notifyPacketInserted(p, Calendar.getInstance().getTime(), 
														this.currents.elementAt(i).getNextIndex(),
														id);
						if(this.currents.elementAt(i).isLast(p)) {
							String urg = "none";
							if(!p.getUrgentScript().equals("")) {
								urg = p.getUrgentScript();
							}
							this.logger.addMessage("\nEND --- knock sequence with id " + id + " successfully received, all actions have been executed --- urgent script: " + urg);
							this.currents.elementAt(i).executeLast(p);
						}
						this.usedTimestamps.add(p.getId() + p.getTimestamp());
					}
				} catch(FirewallException e) {
					this.logger.addMessage("\nACTION-ERROR --- " + e.getMessage());
				} catch(ExecuteScriptException e) {
					this.logger.addMessage("\nACTION-ERROR --- " + e.getMessage());
				} catch(Exception e) {
					//e.printStackTrace();
				}
			}
			if(!inserted) {
				for(int i=0; i<this.launcher.getSequencesManager().getKnockSequences().size(); i++) {
					if(!this.launcher.getSequencesManager().getKnockSequences().elementAt(i).isFake()) {
						if(p.getId().split("-")[0].equals(Integer.toString(this.launcher.getSequencesManager().getKnockSequences().elementAt(i).getId())) &&
								this.launcher.getSequencesManager().getKnockSequences().elementAt(i).getPackets().firstElement().equals(p)) {
							int id = this.launcher.getSequencesManager().getKnockSequences().elementAt(i).getId();
							this.currents.add(new CurrentSequence(p.getId(), id,
																	this.launcher.getSequencesManager(), this, packet));
							this.logger.notifyPacketInserted(p, Calendar.getInstance().getTime(), 
									this.currents.elementAt(this.currents.size() - 1).getNextIndex(),
									this.currents.elementAt(this.currents.size() - 1).getKnockSequence().getId());
							inserted = true;
							this.usedTimestamps.add(p.getId() + p.getTimestamp());
							try {
								if(this.currents.lastElement().isLast(p)) {
									String urg = "none";
									if(!p.getUrgentScript().equals("")) {
										urg = p.getUrgentScript();
									}
									this.logger.addMessage("\nEND --- knock sequence with id " + id + " --- urgent script: " + urg);
									this.currents.lastElement().executeLast(p);
								}
							} catch(Exception e) {
								//e.printStackTrace();
							}
						}
					}
				}
			}
			if(!inserted) {
				this.logger.notifyPacketNotInserted(p, Calendar.getInstance().getTime());
			}
		}
	}

	private boolean isReplayAttack(Packet p) {
		return this.usedTimestamps.contains(p.getId() + p.getTimestamp());
	}

	private Packet getPacket(net.sourceforge.jpcap.net.Packet packet, String payload) {
		if(packet instanceof UDPPacket) {
			//udp packet
			UDPPacket temp = (UDPPacket) packet;
			UdpPacket result = new UdpPacket();
			result.setDstPortNumber(temp.getDestinationPort());
			result.setSrcPortNumber(temp.getSourcePort());
			result.setPayload(payload);
			try {
				result.setAddress(InetAddress.getByAddress(temp.getSourceAddressBytes()));
			} catch(Exception e) {
				//e.printStackTrace();
			}
			return result;
		}
		if(packet instanceof TCPPacket) {
			//tcp packet
			TCPPacket temp = (TCPPacket) packet;
			TcpPacket result = new TcpPacket();
			result.setDstPortNumber(temp.getDestinationPort());
			result.setSrcPortNumber(temp.getSourcePort());
			result.setPayload(payload);
			result.setAck(temp.isAck());
			result.setAckNumber((long) temp.getAcknowledgementNumber());
			result.setFin(temp.isFin());
			result.setPush(temp.isPsh());
			result.setReset(temp.isRst());
			result.setSequenceNumber((long) temp.getSequenceNumber());
			result.setSyn(temp.isSyn());
			result.setUrgent(temp.isUrg());
			result.setWindowSize(temp.getWindowSize());
			try {
				result.setAddress(InetAddress.getByAddress(temp.getSourceAddressBytes()));
			} catch(Exception e) {
				//e.printStackTrace();
			}
			return result;
		}
		if(packet instanceof ICMPPacket) {
			//icmp packet
			ICMPPacket temp = (ICMPPacket) packet;
			IcmpPacket result = new IcmpPacket();
			result.setType(temp.getMessageType());
			result.setCode(temp.getMessageMinorCode());
			result.setPayload(payload);
			try {
				result.setAddress(InetAddress.getByAddress(temp.getSourceAddressBytes()));
			} catch(Exception e) {
				//e.printStackTrace();
			}
			return result;
		}
		return null;
	}

	/**
	 * @return Returns the captor.
	 */
	public PacketCapture getCaptor() {
		return this.captor;
	}

	/**
	 * @param captor The captor to set.
	 */
	public void setCaptor(PacketCapture captor) {
		this.captor = captor;
	}

	/**
	 * @return Returns the currents.
	 */
	public Vector<CurrentSequence> getCurrents() {
		return this.currents;
	}

	/**
	 * @param currents The currents to set.
	 */
	public void setCurrents(Vector<CurrentSequence> currents) {
		this.currents = currents;
	}

	/**
	 * @return Returns the thread.
	 */
	public Thread getThread() {
		return this.thread;
	}

	/**
	 * @param thread The thread to set.
	 */
	public void setThread(Thread thread) {
		this.thread = thread;
	}

	/**
	 * @return Returns the logger.
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/**
	 * @param logger The logger to set.
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @return Returns the deviceName.
	 */
	public String getDeviceName() {
		return this.deviceName;
	}

	/**
	 * @param deviceName The deviceName to set.
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * @return Returns the usedTimestamps.
	 */
	public Vector<String> getUsedTimestamps() {
		return this.usedTimestamps;
	}

	/**
	 * @param usedTimestamps The usedTimestamps to set.
	 */
	public void setUsedTimestamps(Vector<String> usedTimestamps) {
		this.usedTimestamps = usedTimestamps;
	}

	/**
	 * @return Returns the testMode.
	 */
	public boolean isTestMode() {
		return this.testMode;
	}

	/**
	 * @param testMode The testMode to set.
	 */
	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}
}
