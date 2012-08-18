import java.io.File;
import java.util.Vector;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import java.net.*;

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class KnockSequencesManager {
	
	private Vector<KnockSequence> knockSequences = new Vector<KnockSequence>();
	private Launcher launcher;
	
	KnockSequencesManager(Launcher launcher) {
		this.launcher = launcher;
		DOMParser parser = new DOMParser();
        // controllo sintattico dei files
    	File directory = new File("knockSequences/");
		String[] fileList = directory.list();
		for (int i=0; i<fileList.length; i++) {
			if (fileList[i].endsWith(".xml")) {
		        try {
		        	parser.parse(directory.getAbsolutePath() + "/" + fileList[i]);
		        	Element root = parser.getDocument().getDocumentElement();
		        	this.knockSequences.add(new KnockSequence(root, fileList[i].replaceAll(".xml", ""), this));
		        } catch (SmokePacketsException e) {
		        	this.launcher.getConsoleOutput().append("\nERROR --- 'min smoke packets' are greater than 'max smoke packets' in the following sequence: " + fileList[i].replaceAll(".xml", ""));
					this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
		        	this.knockSequences.add(new KnockSequence(fileList[i].replaceAll(".xml", ""), this));
		        	//e.printStackTrace();
		        } catch (TimeWaitException e) {
		        	this.launcher.getConsoleOutput().append("\nERROR --- 'time wait' is greater than 'timeout' in the following sequence: " + fileList[i].replaceAll(".xml", ""));
					this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
		        	this.knockSequences.add(new KnockSequence(fileList[i].replaceAll(".xml", ""), this));
		        	//e.printStackTrace();
		        } catch (Exception e) {
		        	this.knockSequences.add(new KnockSequence(fileList[i].replaceAll(".xml", ""), this));
		        	//e.printStackTrace();
		        }
			}
		}
	}
	
	/**
	 * @param id the id of the knock sequence 
	 * @return the knock sequence that have the id id
	 */
	public KnockSequence getSequenceByID(int id) {
		KnockSequence result = this.knockSequences.firstElement();
		for (int i=0; i<this.knockSequences.size(); i++) {
			if(this.knockSequences.elementAt(i).getId() == id) {
				return this.knockSequences.elementAt(i);
			}
		}
		return result;
	}
	
	public boolean checkId() {
		for(int i=0; i<this.knockSequences.size() - 1; i++) {
			for(int j=i+1; j<this.knockSequences.size(); j++) {
				if (this.knockSequences.elementAt(i).getId() == this.knockSequences.elementAt(j).getId()) {
					return false;
				}
			}
		}
		return true;
	}
	
	public int getNewId() {
		int result = 0;
		for (int i=0; i<this.knockSequences.size(); i++) {
			if (this.knockSequences.elementAt(i).getId() == result) {
				result++;
				i = 0;
			}
		}
		return result;
	}
	
	public String getStringRepresentation(int id) {
		return this.getSequenceByID(id).getStringRepresentation();
	}
	
	/**
	 * @param id of the knock sequence to send
	 */
	public boolean sendKnockSequence(int id, String receiver, String urgentScript) {
		InetAddress receiverAddress = null;
		boolean incorrectName = false;
		try {
			receiverAddress = InetAddress.getByName(receiver);
		} catch (UnknownHostException e) {
			incorrectName = true;
		}
		if(incorrectName) {
			try {
				byte[] address = new byte[4];
				String[] str = receiver.split("[.]");
				for (int i=0; i<str.length; i++) {
					address[i] = Byte.parseByte(str[i]);
				}
				receiverAddress = InetAddress.getByAddress(address);
			} catch(Exception e) {
				this.launcher.getConsoleOutput().append("\n\tERROR --- this address is unreachable: " + receiver);
				this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
				return false;
			}
		}
		try {
			this.getSequenceByID(id).sendSequence(this.launcher.getPacketInjector(), receiverAddress, urgentScript);
		} catch(PacketInjectorException e) {
			this.launcher.getConsoleOutput().append("\n\tERROR --- " + e.getError());
			this.launcher.getConsoleOutput().append("\n\t--------- " + e.getDescription());
			this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
			return false;
		} catch (Exception e) {
			if((e.getMessage() != null) && !e.getMessage().equals("invalid")) {
				this.launcher.getConsoleOutput().append("\n\tERROR --- the network device can not send packets to: " + receiver);
				this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
			}
			//e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * @return Returns the knockSequences.
	 */
	public Vector<KnockSequence> getKnockSequences() {
		return this.knockSequences;
	}

	/**
	 * @param knockSequences The knockSequences to set.
	 */
	public void setKnockSequences(Vector<KnockSequence> knockSequences) {
		this.knockSequences = knockSequences;
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
}
