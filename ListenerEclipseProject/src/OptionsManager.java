import java.util.*;
import net.sourceforge.jpcap.capture.PacketCapture;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 */

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class OptionsManager {

	private String device = "eth0";
	private boolean urgentScripts;
	private String captorFilter = "";
	private Launcher launcher;
	private String key = "default";
	private boolean automatic;
	
	OptionsManager(Launcher launcher) {
		this.launcher = launcher;
		DOMParser parser = new DOMParser();
        // controllo sintattico del sistema
        try {
            parser.parse("options.xml");
            Element root = parser.getDocument().getDocumentElement();
            NodeList list = root.getElementsByTagName("allowUrgentScripts");
            if (list.getLength() >= 1) {
            	if(list.item(0).getFirstChild().getTextContent().equals("true")) {
            		this.urgentScripts = true;
            	}
            }
            NodeList list3 = root.getElementsByTagName("captorFilter");
            if (list3.getLength() >= 1 && (list3.item(0).getFirstChild() != null)) {
            	this.captorFilter = list3.item(0).getFirstChild().getTextContent();
            }	
            if(this.captorFilter.equals("")) {
            	this.automatic = true;
            } else {
            	this.automatic = false;
            }
            NodeList list2 = root.getElementsByTagName("device");
            this.device = list2.item(0).getFirstChild().getTextContent();
            list2 = root.getElementsByTagName("key");
            String temp = list2.item(0).getFirstChild().getTextContent();
            if(!temp.equals("")) {
            	this.key = temp;
            }
        } catch (Exception e) {
        	//e.printStackTrace();
        }
        if(this.checkOptions()) {
        	Utility.setKey(Utility.generateKey(this.key));
        }
	}

	public void updateOptionsFile() {
		if(this.checkOptions()) {
			String file = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + 
							"\n\n<options  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + 
								"\n\t\txsi:noNamespaceSchemaLocation=\"optionsClient.xsd\">\n\n\t";
			String filter = "";
            if(!this.automatic) {
            	filter = this.captorFilter;
            }
			file += "<captorFilter>" + filter + "</captorFilter>\n\t";
			file += "<allowUrgentScripts>" + this.urgentScripts + "</allowUrgentScripts>\n\t";
			file += "<device>" + this.device + "</device>\n\t";
			file += "<key>" + this.key + "</key>\n";
			file += "</options>";
        	Utility.setKey(Utility.generateKey(this.key));
			Utility.writeFile(file, "options.xml");
		}
	}
	
	private boolean checkOptions() {
		try {
			String[] s = PacketCapture.lookupDevices();
			if (!Arrays.toString(s).contains(this.device)) {
				this.launcher.getConsoleOutput().append("\nERROR --- the device defined into the options file is not valid");
				this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
				return false;
			}
		} catch (Exception e) {
			this.launcher.getConsoleOutput().append("\nERROR --- the device defined into the options file is not valid");
			this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
			//e.printStackTrace();
			return false;
		}
		if(this.key.equals("default")) {
			this.launcher.getConsoleOutput().append("\nWARNING --- the encryption key is not defined, you are using the default key");
			this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
		}
		this.checkFilter();
		return true;
	}
	
	public void checkFilter() {
		if(this.captorFilter.equals("")) {
			this.generateAutomaticFilter();
			this.automatic = true;
		} else {
			this.launcher.getListener().setFilter(this.captorFilter);
			this.automatic = false;
		}
	}
	
	/**
	 * this method automatically generates the filter of the packet captor that receives the knock sequences
	 */
	private void generateAutomaticFilter() {
		String result = "";
		Vector<KnockSequence> seqs;
		try {
			seqs = this.launcher.getSequencesManager().getKnockSequences();
		} catch(Exception e) {
			return;
		}
		for(int i=0; i<seqs.size(); i++) {
			if(!seqs.elementAt(i).isFake()) {
				for(int j=0; j<seqs.elementAt(i).getPackets().size(); j++) {
					result += seqs.elementAt(i).getPackets().elementAt(j).getMyFilter() + " or ";
				}
			}
		}
		result = result.substring(0, result.length() - 4);
		this.captorFilter = result;	
		this.automatic = true;
	}

	public String toString() {
		return "OPTIONS\n\tdevice: " + this.device + "\n\tfilter: " + this.captorFilter + 
					"\n\tallowUrgent: " + this.urgentScripts + 
					"\n\tkey: " + this.key;
	}
	
	/**
	 * @return true if the device field has been correctly set
	 */
	public boolean isDeviceCorrect() {
		return this.device.equals("");
	}

	/**
	 * @return Returns the captorFilter.
	 */
	public String getCaptorFilter() {
		return this.captorFilter;
	}

	/**
	 * @param captorFilter The captorFilter to set.
	 */
	public void setCaptorFilter(String captorFilter) {
		this.captorFilter = captorFilter;
	}

	/**
	 * @return Returns the device.
	 */
	public String getDevice() {
		return this.device;
	}

	/**
	 * @param device The device to set.
	 */
	public void setDevice(String device) {
		this.device = device;
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
	 * @return Returns the urgentScripts.
	 */
	public boolean isUrgentScripts() {
		return this.urgentScripts;
	}

	/**
	 * @param urgentScripts The urgentScripts to set.
	 */
	public void setUrgentScripts(boolean urgentScripts) {
		this.urgentScripts = urgentScripts;
	}

	/**
	 * @return Returns the key.
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * @param key The key to set.
	 */
	public void setKey(String key) {
		this.key = key;
		Utility.setKey(Utility.generateKey(this.key));
	}

	/**
	 * @return Returns the automatic.
	 */
	public boolean isAutomatic() {
		return this.automatic;
	}

	/**
	 * @param automatic The automatic to set.
	 */
	public void setAutomatic(boolean automatic) {
		this.automatic = automatic;
	}
}
