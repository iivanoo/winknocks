import java.io.*;
import java.net.NetworkInterface;
import java.util.*;
import org.xml.sax.*;
import com.japisoft.sc.ScEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * @author Ivano Malavolta - 169201
 *
 */
public class Launcher extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private OptionsManager options;
	private KnockSequencesManager sequencesManager;
	private PacketInjector packetInjector;
	
	private JPanel mainPanel;
	// console-output menu section
	private JTextArea consoleOutput = new JTextArea();
	private JEditorPane xmlEditor = new JEditorPane();
	private JList list;

	/**
	 * array parallelo a list che contiene gli id delle knock sequences in list
	 */
	private int[] ids;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Launcher inst = new Launcher();
		inst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		inst.setSize(new Dimension(700, 700));
		inst.setVisible(true);
	}
	
	/**
	 * constructor
	 */
	Launcher() {
		super("winKnocks  v1.0 knocker");
		System.out.println("winKnocks v1.0  \n\tIvano Malavolta\n\tUniversita' degli studi di L'Aquila\n\tcorso di Laurea Specialistica in Informatica\n\tmatricola 169201\n");
		new Splash(true);
		initGUI();
		options = new OptionsManager(this);
		updateListOfSequences();
		File f = new File("options.xml");
		if (!f.exists()) {
			this.openOptionsDialog();
		}
		this.packetInjector = new PacketInjector(this);
	}
	
	private void validateXMLFiles() {
		try {
			XMLValidate.validateOptions();
		} catch (IOException e) {
			//
		} catch (SAXException e) {
			this.consoleOutput.append("\nERROR --- the options file contains the following error:   " + e.getLocalizedMessage());
			consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
		} catch (Exception e) {
			this.consoleOutput.append("\nERROR --- an internal error occurred");
			consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
		}
		try {
			XMLValidate.validateKnockSequences();
		} catch (IOException e) {
			this.consoleOutput.append("\nERROR --- error reading the file associated to a knock sequence");
			consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
		} catch (SAXException e) {
			this.consoleOutput.append("\nERROR --- the knocking sequence '" + XMLValidate.file + "' contains the following error:   " + e.getLocalizedMessage());
			consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
			//e.printStackTrace();
		} catch (Exception e) {
			this.consoleOutput.append("\nERROR --- an internal error occurred");
			consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
		}
	}

	private void initGUI() {
		try {
			this.mainPanel = (JPanel)getContentPane();
			UIManager.put("swing.boldMetal", Boolean.FALSE);
			MetalLookAndFeel.setCurrentTheme(new ThemeRuby());
			UIManager.setLookAndFeel(new MetalLookAndFeel());
			mainPanel.setLayout(new BorderLayout());
			// menu bar
			JMenuBar mybar = new JMenuBar();
			this.setJMenuBar(mybar);
			JMenu menuFile = new JMenu("file");
			JMenuItem newMenu = new JMenuItem("new");
			newMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					putStringInEditor();
					xmlEditor.setEditable(false);
				}
			});
			menuFile.add(newMenu);
			JMenuItem modifyMenu = new JMenuItem("edit");
			modifyMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					xmlEditor.setEditable(true);
				}
			});
			menuFile.add(modifyMenu);
			JMenuItem renameMenu = new JMenuItem("rename");
			renameMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					KnockSequence sequence = sequencesManager.getSequenceByID(ids[list.getSelectedIndex()]);
					openRenameDialog(sequence);
					xmlEditor.setEditable(false);
				}
			});
			menuFile.add(renameMenu);
			JMenuItem saveMenu = new JMenuItem("save");
			saveMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					KnockSequence sequence = sequencesManager.getSequenceByID(ids[list.getSelectedIndex()]);
					openSaveDialog(sequence);
				}
			});
			menuFile.add(saveMenu);
			JMenuItem deleteMenu = new JMenuItem("delete");
			deleteMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					KnockSequence sequence = sequencesManager.getSequenceByID(ids[list.getSelectedIndex()]);
					openDeleteDialog(sequence);
					xmlEditor.setEditable(false);
				}
			});
			menuFile.add(deleteMenu);
			menuFile.addSeparator();
			JMenuItem exitMenu = new JMenuItem("exit");
		    exitMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					dispose();
					System.exit(1);
				}
			});
			menuFile.add(exitMenu);
			// editing section
			JMenu menuEditing = new JMenu("editing");
			JMenu action = new JMenu("add server-side action");
			JMenu packet = new JMenu("add a packet to the sequence");
			
			JMenuItem openPort = new JMenuItem("Open port");
		    openPort.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (checkActionPosition(xmlEditor.getText(), xmlEditor.getCaretPosition())) {
						String code = "\n\t\t<openPort>\n\t\t\t<portNumber>2000</portNumber>\n\t\t\t" + 
										"<exclusive>true</exclusive>\n\t\t\t<wait>0</wait>\n\t\t\t<timeout>60</timeout>\n\t\t</openPort>\n";
						addEditor(code, xmlEditor.getCaretPosition());
					} else {
						consoleOutput.append("\nWARNING --- invalid section for an 'open port' action.");
						consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
					}
				}
		    });	
			JMenuItem closePort = new JMenuItem("Close port");
		    closePort.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (checkActionPosition(xmlEditor.getText(), xmlEditor.getCaretPosition())) {
						String code = "\n\t\t<closePort>\n\t\t\t<portNumber>2000</portNumber>\n\t\t\t" + 
										"<wait>0</wait>\n\t\t\t<timeout>60</timeout>\n\t\t</closePort>\n";
						addEditor(code, xmlEditor.getCaretPosition());
					} else {
						consoleOutput.append("\nWARNING --- invalid section for a 'close port' action.");
						consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
					}
				}
		    });	
			JMenuItem executeScript = new JMenuItem("Execute script");
		    executeScript.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (checkActionPosition(xmlEditor.getText(), xmlEditor.getCaretPosition())) {
						String code = "\n\t\t<executeScript>\n\t\t\t<script>script name</script>\n\t\t\t" + 
										"<wait>0</wait>\n\t\t</executeScript>\n";
						addEditor(code, xmlEditor.getCaretPosition());
					} else {
						consoleOutput.append("\nWARNING --- invalid section for a an 'execute script' action.");
						consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
					}
				}
		    });	
			action.add(openPort);
			action.add(closePort);
			action.add(executeScript);

			JMenuItem udpPacket = new JMenuItem("UDP packet");
		    udpPacket.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (checkPacketPosition(xmlEditor.getText(), xmlEditor.getCaretPosition())) {
						String code = "\n\t\t<UDPpacket>\n\t\t\t<srcPortNumber>7000</srcPortNumber>\n\t\t\t" +
								"<dstPortNumber>3000</dstPortNumber>\n\t\t\t<payload>payload</payload>\n\t\t</UDPpacket>\n";
						addEditor(code, xmlEditor.getCaretPosition());
					} else {
						consoleOutput.append("\nWARNING --- invalid section for a UDP packet.");
						consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
					}
				}
		    });	
			JMenuItem tcpPacket = new JMenuItem("TCP packet");
		    tcpPacket.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (checkPacketPosition(xmlEditor.getText(), xmlEditor.getCaretPosition())) {
						String code = "\n\t\t<TCPpacket>\n\t\t\t<srcPortNumber>7000</srcPortNumber>\n\t\t\t" +
									"<dstPortNumber>3000</dstPortNumber>\n\t\t\t<sequenceNumber>1</sequenceNumber>\n\t\t\t" +
									"<ackNumber>2</ackNumber>\n\t\t\t<windowSize>50</windowSize>\n\t\t\t<flags>\n\t\t\t\t" + 
									"<ack>0</ack>\n\t\t\t\t<syn>1</syn>\n\t\t\t\t<fin>0</fin>\n\t\t\t\t<push>0</push>\n\t\t\t\t" + 
									"<reset>0</reset>\n\t\t\t\t<urgent>0</urgent>\n\t\t\t</flags>\n\t\t\t<payload>payload</payload>\n\t\t</TCPpacket>\n";
						addEditor(code, xmlEditor.getCaretPosition());
					} else {
						consoleOutput.append("\nWARNING --- invalid section for a TCP packet.");
						consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
					}
				}
		    });	
			JMenuItem icmpPacket = new JMenuItem("ICMP packet");
		    icmpPacket.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (checkPacketPosition(xmlEditor.getText(), xmlEditor.getCaretPosition())) {
						String code = "\n\t\t<ICMPpacket>\n\t\t\t<type>8</type>\n\t\t\t<code>0</code>\n\t\t\t" + 
									"<data>data</data>\n\t\t</ICMPpacket>\n";
						addEditor(code, xmlEditor.getCaretPosition());
					} else {
						consoleOutput.append("\nWARNING --- invalid section for an ICMP packet.");
						consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
					}
				}
		    });	
			packet.add(udpPacket);
			packet.add(tcpPacket);
			packet.add(icmpPacket);
			menuEditing.add(action);
			menuEditing.add(packet);
		    
			JMenu menuActions = new JMenu("actions");
			JMenuItem sendMenu = new JMenuItem("send knock sequence");
		    sendMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					openSendSequenceDialog(ids[list.getSelectedIndex()]);
					xmlEditor.setEditable(false);
				}
			});
			menuActions.add(sendMenu);

			JMenuItem refreshMenu = new JMenuItem("refresh list");
		    refreshMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					consoleOutput.append("\nList of Knock sequences updated...");
					consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
					updateListOfSequences();
					xmlEditor.setEditable(false);
				}
			});
			menuActions.add(refreshMenu);
			JMenu menuOptions = new JMenu("options");
			JMenuItem optionsMenu = new JMenuItem("settings");
		    optionsMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					openOptionsDialog();
					xmlEditor.setEditable(false);
				}
			});
			menuOptions.add(optionsMenu);
			menuOptions.addSeparator();
		    ButtonGroup themesMenuGroup = new ButtonGroup();
		    JRadioButtonMenuItem aqua = new JRadioButtonMenuItem("Aqua theme");
		    aqua.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					changeTheme(new ThemeAqua());
				}
			});
		    JRadioButtonMenuItem charcoal = new JRadioButtonMenuItem("Charcoal theme");
		    charcoal.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					changeTheme(new ThemeCharcoal());
				}
			});
		    JRadioButtonMenuItem contrast = new JRadioButtonMenuItem("Contrast theme");
		    contrast.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					changeTheme(new ThemeContrast());
				}
			});
		    JRadioButtonMenuItem emerald = new JRadioButtonMenuItem("Emerald theme");
		    emerald.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					changeTheme(new ThemeEmerald());
				}
			});
		    JRadioButtonMenuItem ruby = new JRadioButtonMenuItem("Ruby theme");
		    ruby.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					changeTheme(new ThemeRuby());
				}
			});
		    themesMenuGroup.add(charcoal);
		    themesMenuGroup.add(contrast);
		    themesMenuGroup.add(emerald);
		    themesMenuGroup.add(aqua);
		    themesMenuGroup.add(ruby);
		    menuOptions.add(aqua);
		    menuOptions.add(charcoal);
		    menuOptions.add(contrast);
		    menuOptions.add(ruby);
		    menuOptions.add(emerald);
			JMenu menuAbout = new JMenu("?");
			JMenuItem aboutMenu = new JMenuItem("about...");
		    aboutMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					new Splash(false);
				}
			});
			menuAbout.add(aboutMenu);	
			
			mybar.add(menuFile);
			mybar.add(menuActions);
			mybar.add(menuEditing);
			mybar.add(menuOptions);
			mybar.add(menuAbout);
			
			// list + xml editor
			this.list = new JList();
			this.list.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent evt) {
					int selected = list.getSelectedIndex();
					if (selected == -1) {
						selected = 0;
					}
					xmlEditor.setText(sequencesManager.getSequenceByID(ids[selected]).getStringRepresentation());
					xmlEditor.selectAll();
					xmlEditor.moveCaretPosition(0);
					xmlEditor.setEditable(false);
				}
			});
			JScrollPane scrollPane = new JScrollPane(this.list);
			JScrollPane scrollPane2 = new JScrollPane(this.xmlEditor);
			scrollPane2.setPreferredSize(new java.awt.Dimension(300, 200));
	        scrollPane2.setViewportView(this.xmlEditor);
	        this.xmlEditor.setEditable(false);
	        this.xmlEditor.setContentType("text/english");
	        ScEditorKit ek = new ScEditorKit();
	        this.xmlEditor.setEditorKit(ek);
	        ek.readSyntaxColorDescriptor("lib/knockSequence.prop");
			JSplitPane splitted = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, scrollPane, scrollPane2);
			splitted.setDividerSize(5);
			mainPanel.add(splitted, BorderLayout.CENTER);
			
			// output console
			this.consoleOutput.setEditable(false);
			this.consoleOutput.setAutoscrolls(true);
			this.consoleOutput.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			this.consoleOutput.setText("the results of your actions will be displayed here...");
			this.consoleOutput.setRows(7);
			JScrollPane scr = new JScrollPane(this.consoleOutput);
			scr.setMaximumSize(new Dimension(580, 100));
			mainPanel.add(scr, BorderLayout.SOUTH);
		} catch(Exception e) {
			//e.printStackTrace();
		}
	}
	
	private void addEditor(String code, int caretPosition) {
		String first = xmlEditor.getText().substring(0, caretPosition);
		String second = xmlEditor.getText().substring(caretPosition + 1);
		xmlEditor.setText(first + code + second);
		xmlEditor.setCaretPosition(caretPosition);
	}
	
	private void openSendSequenceDialog(int id) {
		JPanel panel = new JPanel(new GridLayout(5,0));
		panel.add(new JLabel("Receiver of knock sequence: "));
		JTextField receiverField = new JTextField();
		panel.add(receiverField);
		panel.add(new JLabel(""));
		panel.add(new JLabel("Script to execute :"));
		JTextField scriptField = new JTextField();
		scriptField.setForeground(Color.GRAY);
		panel.add(scriptField);
		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.OK_CANCEL_OPTION, JOptionPane.OK_CANCEL_OPTION,
									new ImageIcon("img/settings.png"));
		JDialog dialog = optionPane.createDialog(this, "sending sequence " + sequencesManager.getSequenceByID(id).getName());
		dialog.pack();
		dialog.setVisible(true);
		if(((Integer) optionPane.getValue()).intValue() != 2) {
			String receiver = receiverField.getText();
			String script = scriptField.getText();
			if(!((receiver == null) || receiver.equals(""))) {
				consoleOutput.append("\nSTART --- you are sending the following knock sequence: '" + sequencesManager.getSequenceByID(id).getName() + "'");
				consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
				if(sequencesManager.sendKnockSequence(ids[list.getSelectedIndex()], receiver, script)) {
					consoleOutput.append("\nSTOP --- the following knock sequence has been successfully sent: '" + sequencesManager.getSequenceByID(id).getName() + "'");
					consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
				}
			}
		}
	}
	
	private void updateListOfSequences() {
		this.sequencesManager = new KnockSequencesManager(this);
		int length = this.sequencesManager.getKnockSequences().size();
		String[] sequences = new String[length];
		int[] ids = new int[length];
		for (int i=0; i<length; i++) {
			ids[i] = this.sequencesManager.getKnockSequences().elementAt(i).getId();
			sequences[i] = this.sequencesManager.getKnockSequences().elementAt(i).getName();
		}
		this.ids = ids;
		this.list.setListData(sequences);
		list.setSelectedIndex(0);
		if(!this.sequencesManager.checkId()) {
			this.consoleOutput.append("\nERROR --- there are knock sequences with the same ID; the program may not work properly.");
			consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
		}
		this.validateXMLFiles();
	}
	
	private void changeTheme(DefaultMetalTheme theme) {
		MetalLookAndFeel.setCurrentTheme(theme);
		try {
			UIManager.setLookAndFeel(new MetalLookAndFeel());
			SwingUtilities.updateComponentTreeUI(this);
			int index = xmlEditor.getCaretPosition();
			xmlEditor.selectAll();
			this.xmlEditor.moveCaretPosition(index);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	private void openOptionsDialog() {
		JPanel panel = new JPanel(new GridLayout(12,0));
		panel.add(new JLabel());
		JPanel panel4 = new JPanel(new GridLayout(2,0));
		panel4.add(new JLabel("Location into the file system of the Nemesis tool* :"));
		JTextField location = new JTextField();
		location.setText(this.options.getNemesisLocation());
		panel4.add(location);
		panel.add(panel4);
		panel.add(new JLabel(""));
		JPanel panel3 = new JPanel(new GridLayout(2, 0));
		panel3.add(new JLabel("Key to encrypt the payload of the packets:"));
		JTextField keyField = new JTextField(this.options.getKey());
		panel3.add(keyField);
		panel.add(panel3);
		panel.add(new JLabel(""));
		panel.add(new JLabel("Device used to send knock sequences :"));
		Vector<String> elem = new Vector<String>();
		Vector<NetworkInterface> vec = new Vector<NetworkInterface>();
		boolean selected = false;
		int index = 0;
		try {
			Enumeration<NetworkInterface> interfaces;
			for(interfaces = NetworkInterface.getNetworkInterfaces() ; interfaces.hasMoreElements() ;) {
				NetworkInterface temp = interfaces.nextElement();
				if (temp.getInetAddresses().hasMoreElements() && !temp.getInetAddresses().nextElement().isLoopbackAddress()) {
					if (temp.getName().equals(this.options.getDevice())) {
						selected = true;
					} 
					if (!selected) {
						index++;
					}
					vec.add(temp);
					elem.add(temp.getDisplayName());
				}
			}
		} catch(Exception e){
			//e.printStackTrace();
		}
		JComboBox devices = new JComboBox(elem);
		if (index >= elem.size()) {
			index = 0;
		}
		if(!elem.isEmpty()) {
			devices.setSelectedIndex(index);
		}
		panel.add(devices);
		for (int i=0; i<3; i++) {
			panel.add(new JLabel());
		}
		panel.add(new JLabel("*  if you use only UDP packets you can leave this text field blank"));
		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.OK_CANCEL_OPTION, JOptionPane.OK_CANCEL_OPTION,
									new ImageIcon("img/settings.png"));
		JDialog dialog = optionPane.createDialog(this, "winKnock v1.0 settings");
		dialog.pack();
		dialog.setVisible(true);
		if((optionPane.getValue() != null) && (((Integer) optionPane.getValue()).intValue() != 2)) {
			String nemesisLocation = location.getText();
			File f = new File(nemesisLocation);
			if (!f.exists()) {
				this.consoleOutput.append("\nWARNING --- The location of Nemesis is not valid; only UDP packets are supported...\n\tYou typed: '" + f.getAbsolutePath() + "'");
				consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
				nemesisLocation = "";
			}
			String device = "";
			if(!vec.isEmpty()) {
				device = vec.elementAt(devices.getSelectedIndex()).getName();
			}
			this.options.setNemesisLocation(nemesisLocation);
			this.options.setDevice(device);
			if(keyField.getText().equals("")) {
				this.options.setKey("default");
			} else {
				this.options.setKey(keyField.getText());
			}
			this.options.updateOptionsFile();
			this.packetInjector.setOptions(this.options);
			xmlEditor.setEditable(false);
		}
	}
	
	private void openDeleteDialog(KnockSequence sequence) {
		ImageIcon icon = new ImageIcon("img/delete.png");
		JOptionPane optionPane = new JOptionPane("Are you sure you want to delete file '" + sequence.getName() + "'?", JOptionPane.YES_NO_OPTION, JOptionPane.YES_NO_OPTION,
				icon);
		JDialog dialog = optionPane.createDialog(this, "delete knock sequence");
		dialog.pack();
		dialog.setVisible(true);
		if(((Integer)optionPane.getValue()) == 0) {
			File file = new File("knockSequences/" + sequence.getName() + ".xml");
			file.delete();
			this.consoleOutput.append("\nThe following knock sequence has been deleted: '" + sequence.getName() + "'");
			consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
			this.updateListOfSequences();
		}
	}
	
	private void openSaveDialog(KnockSequence sequence) {
		ImageIcon icon = new ImageIcon("img/save.png");
		JOptionPane optionPane = new JOptionPane("Are you sure you want to save file '" + sequence.getName() + "'?", JOptionPane.YES_NO_OPTION, JOptionPane.YES_NO_OPTION,
				icon);
		JDialog dialog = optionPane.createDialog(this, "save knock sequence");
		dialog.pack();
		dialog.setVisible(true);
		if(((Integer)optionPane.getValue()) == 0) {
			Utility.writeFile(this.xmlEditor.getText(), "knockSequences/" + sequence.getName() + ".xml");
			this.consoleOutput.append("\nThe following knock sequence has been saved: '" + sequence.getName() + "'");
			consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
			this.updateListOfSequences();
			this.xmlEditor.setEditable(false);
			this.list.setSelectedIndex(this.getIndexById(sequence.getId()));
			xmlEditor.setEditable(false);
		}
	}
	
	private void openRenameDialog(KnockSequence sequence) {
		ImageIcon icon = new ImageIcon("img/settings.png");
		String name = (String) JOptionPane.showInputDialog(this, "Name of the sequence: ", "renaming " + sequence.getName(), JOptionPane.QUESTION_MESSAGE, icon, null, sequence.getName());
		if(!name.equals(sequence.getName())) {
			if (!name.equals("")) {
				try {
					File file = new File("knockSequences/" + sequence.getName() + ".xml");
					file.renameTo(new File("knockSequences/" + name + ".xml"));
					this.consoleOutput.append("\n'" + sequence.getName() + "' has been renamed to: '" + name + "'");
					consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
					sequence.setName(name);
					this.updateListOfSequences();
					xmlEditor.setEditable(false);
				} catch(Exception e) {
					//e.printStackTrace();
				}
			}
		}
	}
	
	private boolean checkPacketPosition(String text, int index) {
		if (xmlEditor.getText().substring(0, xmlEditor.getCaretPosition()).contains("<packets>") && 
				xmlEditor.getText().substring(xmlEditor.getCaretPosition() + 1).contains("</packets>")) {
			try {
				if (!text.substring(text.substring(0, index).lastIndexOf("<UDPpacket>"), index).contains("</UDPpacket>")) {
					return false;
				}
			} catch(Exception e) {}
			try{
				if (!text.substring(text.substring(0, index).lastIndexOf("<TCPpacket>"), index).contains("</TCPpacket>")) {
					return false;
				}
			} catch(Exception e) {}
			try{
				if (!text.substring(text.substring(0, index).lastIndexOf("<ICMPpacket>"), index).contains("</ICMPpacket>")) {
					return false;
				}
			} catch(Exception e) {}
			return true;
		}
		return false;
	}
	
	public boolean checkActionPosition(String text, int index) {
		if (xmlEditor.getText().substring(0, xmlEditor.getCaretPosition()).contains("<actions>") && 
				xmlEditor.getText().substring(xmlEditor.getCaretPosition() + 1).contains("</actions>")) {
			try {
				if (!text.substring(text.substring(0, index).lastIndexOf("<openPort>"), index).contains("</openPort>")) {
					return false;
				}
			} catch(Exception e) {}
			try {
				if (!text.substring(text.substring(0, index).lastIndexOf("<closePort>"), index).contains("</closePort>")) {
					return false;
				}
			} catch(Exception e) {}
			try{
				if (!text.substring(text.substring(0, index).lastIndexOf("<executeScript>"), index).contains("</executeScript>")) {
					return false;
				}
			} catch(Exception e) {}
			return true;
		}
		return false;
	}
	
	void putStringInEditor() {
		int id = this.sequencesManager.getNewId();
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n\n" + 
						"<KnockSequence  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + 
						"\n\t\txsi:noNamespaceSchemaLocation=\"KnockSequence.xsd\">" + 
			"\n\t<id>" + id + "</id>\n\t<description>description of the knock sequence</description>\n\t<smokePackets>\n\t\t" + 
			"<min>2</min>\n\t\t<max>20</max>\n\t</smokePackets>\n\t<maxFakePayload>100</maxFakePayload>\n\t" + 
			"<actions>\n\n\t\t<!-- put here the actions to be executed server-side -->\n\n\t</actions>\n\n\t" +
			"<packets>\n\n\t\t<!-- put here the packets of the knock sequence -->\n\n\t</packets>\n</KnockSequence>";
		int n = 1;
		File newFile = new File("knockSequences/newSequence0.xml");
		while (newFile.exists()) {
			newFile = new File("knockSequences/newSequence" + n++ + ".xml"); 
		}
		Utility.writeFile(text, "knockSequences/newSequence" + (n - 1) + ".xml");
		this.updateListOfSequences();
		this.list.setSelectedIndex(this.getIndexById(id));
	}
	
	private int getIndexById(int id) {
		int i;
		for (i=0; i<this.ids.length; i++) {
			if (this.ids[i] == id) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * @return Returns the consoleOutput.
	 */
	public JTextArea getConsoleOutput() {
		return this.consoleOutput;
	}

	/**
	 * @param consoleOutput The consoleOutput to set.
	 */
	public void setConsoleOutput(JTextArea consoleOutput) {
		this.consoleOutput = consoleOutput;
	}

	/**
	 * @return Returns the list.
	 */
	public JList getList() {
		return this.list;
	}

	/**
	 * @param list The list to set.
	 */
	public void setList(JList list) {
		this.list = list;
	}

	/**
	 * @return Returns the mainPanel.
	 */
	public JPanel getMainPanel() {
		return this.mainPanel;
	}

	/**
	 * @param mainPanel The mainPanel to set.
	 */
	public void setMainPanel(JPanel mainPanel) {
		this.mainPanel = mainPanel;
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
	 * @return Returns the sequencesManager.
	 */
	public KnockSequencesManager getSequencesManager() {
		return this.sequencesManager;
	}

	/**
	 * @param sequencesManager The sequencesManager to set.
	 */
	public void setSequencesManager(KnockSequencesManager sequencesManager) {
		this.sequencesManager = sequencesManager;
	}

	/**
	 * @return Returns the xmlEditor.
	 */
	public JEditorPane getXmlEditor() {
		return this.xmlEditor;
	}

	/**
	 * @param xmlEditor The xmlEditor to set.
	 */
	public void setXmlEditor(JEditorPane xmlEditor) {
		this.xmlEditor = xmlEditor;
	}

	/**
	 * @return Returns the packetInjector.
	 */
	public PacketInjector getPacketInjector() {
		return this.packetInjector;
	}

	/**
	 * @param packetInjector The packetInjector to set.
	 */
	public void setPacketInjector(PacketInjector packetInjector) {
		this.packetInjector = packetInjector;
	}
}
