import java.io.*;
import java.net.InetAddress;
import java.util.*;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 */

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class KnockSequence {

	private Element root;
	private int id;
	private String name;
	private String description;
	private int maxSmokePackets;
	private int minSmokePackets;
	private int maxFakePayload;
	private Vector<Action> actions = new Vector<Action>();
	private Vector<Packet> packets = new Vector<Packet>();
	private String stringRepresentation;
	private KnockSequencesManager manager;
	private boolean fake = false;

	public KnockSequence(Element root, String name, KnockSequencesManager manager) throws SmokePacketsException, TimeWaitException {
		this.root = root;
		this.manager = manager;
		this.id = Integer.parseInt(root.getElementsByTagName("id").item(0).getTextContent().trim());
		this.name = name;
		this.description = root.getElementsByTagName("description").item(0).getTextContent().trim();
		this.maxSmokePackets = Integer.parseInt(root.getElementsByTagName("max").item(0).getTextContent().trim());
		this.minSmokePackets = Integer.parseInt(root.getElementsByTagName("min").item(0).getTextContent().trim());
		this.maxFakePayload = Integer.parseInt(root.getElementsByTagName("maxFakePayload").item(0).getTextContent().trim());
		if(this.maxFakePayload > 350) {
			this.maxFakePayload = 350;
		}
		if (!this.checkSmokePackets()) {
			SmokePacketsException e = new SmokePacketsException();
			e.SequenceName = this.name;
			throw new SmokePacketsException();
		}
		this.fillActions();
		this.fillPackets();
		this.fillStringRepresentation();
	}
	
	public KnockSequence(String name, KnockSequencesManager manager) {
		this.fake = true;
		this.name = name;
		this.manager = manager;
		this.fillStringRepresentation();
		String ident = this.stringRepresentation.split("<id>")[1].split("</id>")[0];
		try {
			this.id = Integer.valueOf(ident);
		} catch(Exception e) {
			this.id = manager.getNewId();
		}
	}
	
	private void fillActions() throws TimeWaitException {
		NodeList acts = root.getElementsByTagName("actions").item(0).getChildNodes();
		try {
			for (int i=1; i<acts.getLength(); i+=2) {
				if(acts.item(i).getNodeName().equals("openPort")) {
					this.actions.add(new OpenPortAction(acts.item(i)));
				}
				if(acts.item(i).getNodeName().equals("closePort")) {
					this.actions.add(new ClosePortAction(acts.item(i)));
				}
				if(acts.item(i).getNodeName().equals("executeScript")) {
					this.actions.add(new ExecuteScriptAction(acts.item(i)));
				}
			}
		} catch (TimeWaitException e) {
			e.SequenceName = this.name;
			throw e;
		}
	}
	
	private void fillPackets() {
		NodeList pkts = root.getElementsByTagName("packets").item(0).getChildNodes();
		for (int i=1; i<pkts.getLength(); i+=2) {
			if(pkts.item(i).getNodeName().equals("UDPpacket")) {
				this.packets.add(new UdpPacket(pkts.item(i)));
			}
			if(pkts.item(i).getNodeName().equals("TCPpacket")) {
				this.packets.add(new TcpPacket(pkts.item(i)));
			}
			if(pkts.item(i).getNodeName().equals("ICMPpacket")) {
				this.packets.add(new IcmpPacket(pkts.item(i)));
			}
		}
	}
	
	private boolean checkSmokePackets() {
		return this.minSmokePackets <= this.maxSmokePackets;
	}
	
	private void fillStringRepresentation() {
		String line;
		String file = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader("knockSequences/" + this.name + ".xml"));
			line = null;
			while((line = br.readLine()) != null) {
				file += "\n" + line;
			}
		} catch (Exception e) {
			System.out.println("ERROR: unable to read file '" + this.name + "'");
			System.exit(1);
		}
		this.stringRepresentation = file;
	}
	
	public void executeActions(InetAddress address) throws FirewallException, ExecuteScriptException {
		if(!this.fake) {
			for(int i=0; i<this.actions.size(); i++) {
				this.actions.elementAt(i).execute(address);
			}
		} else {
			System.out.println("sono fake");
		}
	}

	public String toString() {
		return "KnockSequence: \n\tid: " + this.id + "\n\tname: " + this.name + "\n\tdescription: " 
			+ this.description + "\n\tmaxSmoke: " + this.maxSmokePackets + "\n\tminSmoke: "
			+ this.minSmokePackets + "\n\tmaxFakePayload: " + this.maxFakePayload	
			+ "\n\tactions: " + this.actions + "\n\tpackets: " + this.packets;
	}

	/**
	 * @return Returns the actions.
	 */
	public Vector<Action> getActions() {
		return this.actions;
	}

	/**
	 * @param actions The actions to set.
	 */
	public void setActions(Vector<Action> actions) {
		this.actions = actions;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return Returns the maxFakePayload.
	 */
	public int getMaxFakePayload() {
		return this.maxFakePayload;
	}

	/**
	 * @param maxFakePayload The maxFakePayload to set.
	 */
	public void setMaxFakePayload(int maxFakePayload) {
		this.maxFakePayload = maxFakePayload;
		if(this.maxFakePayload > 350) {
			this.maxFakePayload = 350;
		}
	}

	/**
	 * @return Returns the maxSmokePackets.
	 */
	public int getMaxSmokePackets() {
		return this.maxSmokePackets;
	}

	/**
	 * @param maxSmokePackets The maxSmokePackets to set.
	 */
	public void setMaxSmokePackets(int maxSmokePackets) {
		this.maxSmokePackets = maxSmokePackets;
	}

	/**
	 * @return Returns the minSmokePackets.
	 */
	public int getMinSmokePackets() {
		return this.minSmokePackets;
	}

	/**
	 * @param minSmokePackets The minSmokePackets to set.
	 */
	public void setMinSmokePackets(int minSmokePackets) {
		this.minSmokePackets = minSmokePackets;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the packets.
	 */
	public Vector<Packet> getPackets() {
		return this.packets;
	}

	/**
	 * @param packets The packets to set.
	 */
	public void setPackets(Vector<Packet> packets) {
		this.packets = packets;
	}

	/**
	 * @return Returns the root.
	 */
	public Element getRoot() {
		return this.root;
	}

	/**
	 * @param root The root to set.
	 */
	public void setRoot(Element root) {
		this.root = root;
	}

	/**
	 * @return Returns the stringRepresentation.
	 */
	public String getStringRepresentation() {
		return this.stringRepresentation;
	}

	/**
	 * @param stringRepresentation The stringRepresentation to set.
	 */
	public void setStringRepresentation(String stringRepresentation) {
		this.stringRepresentation = stringRepresentation;
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
	 * @return Returns the fake.
	 */
	public boolean isFake() {
		return this.fake;
	}

	/**
	 * @param fake The fake to set.
	 */
	public void setFake(boolean fake) {
		this.fake = fake;
	}
}
