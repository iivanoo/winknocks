import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;

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
	private String nemesisLocation = "";
	private Launcher launcher;
	private String key = "default";
	
	OptionsManager(Launcher launcher) {
		this.launcher = launcher;
		DOMParser parser = new DOMParser();
        // controllo sintattico del sistema
        try {
            parser.parse("options.xml");
            Element root = parser.getDocument().getDocumentElement();
            NodeList list = root.getElementsByTagName("nemesisLocation");
            if (list.getLength() >= 1) {
            	if(list.item(0).getFirstChild() != null) {
            		this.nemesisLocation = list.item(0).getFirstChild().getTextContent();
            	}
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
	
	public String toString() {
		return "OPTIONS\n\tdevice: " + this.device + "\n\tnemesis: " + this.nemesisLocation + 
					"\n\tkey: " + this.key;
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
	 * @return Returns the hpingLocation.
	 */
	public String getNemesisLocation() {
		return this.nemesisLocation;
	}

	/**
	 * @param hpingLocation The hpingLocation to set.
	 */
	public void setNemesisLocation(String nemesisLocation) {
		this.nemesisLocation = nemesisLocation;
		this.launcher.getPacketInjector().setNemesis(this.launcher.getPacketInjector().checkNemesis());
	}
	
	public void updateOptionsFile() {
		if(this.checkOptions()) {
			String file = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + 
							"\n\n<options  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + 
								"\n\t\txsi:noNamespaceSchemaLocation=\"optionsClient.xsd\">\n\n\t";
			file += "<nemesisLocation>" + this.nemesisLocation + "</nemesisLocation>\n\t";
			file += "<device>" + this.device + "</device>\n\t";
			file += "<key>" + this.key + "</key>\n";
			file += "</options>";
        	Utility.setKey(Utility.generateKey(this.key));
			Utility.writeFile(file, "options.xml");
		}
	}
	
	private boolean checkOptions() {
		File f = new File(this.nemesisLocation);
		if(!f.exists() && !this.nemesisLocation.equals("")) {
			this.launcher.getConsoleOutput().append("\nERROR --- the Nemesis location into the options file is not valid");
			this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
			return false;
		}
		try {
			NetworkInterface x = NetworkInterface.getByName(this.device);
			if (x == null) {
				this.launcher.getConsoleOutput().append("\nERROR --- the device defined into the options file is not valid");
				this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
				return false;
			}
		} catch (SocketException e) {
			this.launcher.getConsoleOutput().append("\nERROR --- the device defined into the options file is not valid");
			this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
			//e.printStackTrace();
			return false;
		}
		if(this.key.equals("default")) {
			this.launcher.getConsoleOutput().append("\nWARNING --- the encryption key is not defined, you are using the default key");
			this.launcher.getConsoleOutput().setCaretPosition(this.launcher.getConsoleOutput().getText().length() - 1);
		}
		return true;
	}
	
	/**
	 * @return true if the nemesis location field has been correctly set
	 */
	public boolean isNemesisLocationSet() {
		return this.nemesisLocation.equals("");
	}
	
	/**
	 * @return true if the device field has been correctly set
	 */
	public boolean isDeviceCorrect() {
		return this.device.equals("");
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
