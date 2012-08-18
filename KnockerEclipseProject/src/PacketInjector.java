import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Random;

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class PacketInjector {

	private OptionsManager options;
	private boolean nemesis;
	
	PacketInjector(Launcher launcher) {
		this.options = launcher.getOptions();
		this.nemesis = this.checkNemesis();
	}
	
	/**
	 * @param packet
	 * @param receiver
	 */
	public void sendTcpPacket(TcpPacket packet, InetAddress receiver, String artificialPayload) throws PacketInjectorException {
		if (!this.isNemesis()) {
        	throw new PacketInjectorException("there is a problem with the packet injector...",
        										"the current version of Nemesis is not supportted by Windows XP SP2\n\t--------- or check that WinPcap 3.0 is installed on your machine");
        }
        String command = "\"" + this.options.getNemesisLocation() + "\" tcp -S ";
        try {
        command += NetworkInterface.getByName(this.options.getDevice()).getInetAddresses().nextElement().toString().substring(1);
        command += " -D " + receiver.getHostAddress();
        command += " -a " + packet.getAckNumber();
        command += " -s " + packet.getSequenceNumber();
        command += " -w " + packet.getWindowSize();
        if(packet.isAck() || packet.isFin() || packet.isPush() || packet.isReset() || packet.isSyn() || packet.isUrgent()) {
        	command += " -f";
	        if(packet.isAck()) {
	        	command += "A";
	        }
	        if(packet.isFin()) {
	        	command += "F";
	        }
	        if(packet.isPush()) {
	        	command += "P";
	        }
		    if(packet.isReset()) {
	        	command += "R";
		    }
		    if(packet.isSyn()) {
	        	command += "S";
		    }
		    if(packet.isUrgent()) {
	        	command += "U";
		    }
        } else {
        	command += " -f- ";
        }
        command += " -x " + packet.getSrcPortNumber();
        command += " -y" + packet.getDstPortNumber();
        byte[] artif = Utility.encrypt(packet.getPayload() + artificialPayload);
        String payload = Utility.getString(artif);
        payload = Utility.getFinalBytes(payload);
        Utility.writeFile(payload, "tmp.txt");
        command += " -P " + "tmp.txt";
        if (!Utility.executeCommand(command.split(" ")).contains("Packet Injected")) {
            File tmp = new File("tmp.txt");
            tmp.delete();
        	throw new Exception();
        }
        File tmp = new File("tmp.txt");
        tmp.delete();
        } catch (Exception e) {
        	throw new PacketInjectorException("error injecting a TCP packet", "check your packet injector");
        }
	}
	
	/**
	 * @param packet
	 * @param receiver
	 */
	public void sendIcmpPacket(IcmpPacket packet, InetAddress receiver, String artificialPayload) throws PacketInjectorException {
        if (!this.isNemesis()) {
        	throw new PacketInjectorException("there is a problem with the packet injector...",
        										"the current version of Nemesis is not supportted by Windows XP SP2\n\t--------- or check that WinPcap 3.0 is installed on your machine");
        }
        String command = "\"" + this.options.getNemesisLocation() + "\" icmp -S ";
        try {
        	command += NetworkInterface.getByName(this.options.getDevice()).getInetAddresses().nextElement().toString().substring(1);
        	command += " -D " + receiver.getHostAddress();
        	command += " -c " + packet.getCode();
        	command += " -i " + packet.getType();
        	command += " -e " + new Random(System.currentTimeMillis()).nextInt(65000);
        	command += " -s " + new Random(System.currentTimeMillis()).nextInt(65000);
        	byte[] artif = Utility.encrypt(packet.getPayload() + artificialPayload);
	        String payload = Utility.getString(artif);
	        payload = Utility.getFinalBytes(payload);
	        Utility.writeFile(payload, "tmp.txt");
	        command += " -P " + "tmp.txt";
	        if (!Utility.executeCommand(command.split(" ")).contains("Packet Injected")) {
	            File tmp = new File("tmp.txt");
	            tmp.delete();
	        	throw new Exception();
	        }
	        File tmp = new File("tmp.txt");
	        tmp.delete();
        } catch (Exception e) {
        	throw new PacketInjectorException("error injecting an ICMP packet", "check your packet injector");
        }
	}
	
	public boolean isNemesis() {
        return this.nemesis;
	}
	
	public boolean checkNemesis() {
		String[] command = {"\"" + this.options.getNemesisLocation() + "\""};
		String result = "";
		try {
            result = Utility.executeCommand(command);
        } catch (Exception e) {
            return false;
        }
        if (!result.contains("NEMESIS")) {
        	return false;
        }
        return true;
	}
	/**
	 * @return Returns the options.
	 */
	public OptionsManager getOptions() {
		return this.options;
	}

	/**
	 * @param options The options to set.
	 */
	public void setOptions(OptionsManager options) {
		this.options = options;
	}

	/**
	 * @param nemesis The nemesis to set.
	 */
	public void setNemesis(boolean nemesis) {
		this.nemesis = nemesis;
	}
}
