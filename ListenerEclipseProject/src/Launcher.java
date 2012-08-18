import java.io.*;
import java.util.*;

import net.sourceforge.jpcap.capture.PacketCapture;

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
	
	private PacketListener listener = new PacketListener();
	private OptionsManager options;
	private KnockSequencesManager sequencesManager;
	
	private JPanel mainPanel;
	// console-output menu section
	private JTextArea consoleOutput = new JTextArea();
	private JEditorPane xmlEditor = new JEditorPane();
	private JList list;
	
	private JCheckBox automaticFilter;
	private JTextField filterField;
	private JTextField keyField;
	private JMenuItem stopListenMenu;
	private JMenuItem listenMenu;
	private JMenuItem listenBkMenu;
	private JMenuItem listenTestMenu;

	/**
	 * array parallelo a list che contiene gli id delle knock sequences in list
	 */
	private int[] ids;

	private JMenuItem refreshMenu;
	private JMenu menuEditing;
	private JMenu menuFile;
	private JMenu menuOptions;
	
	private JTextArea logging;

	private JSplitPane splitted;

	private JScrollPane scrollPane2;
	
	private static PacketCapture[] captors;
	private static PacketCapture cap;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("winKnocks v1.0  \n\tIvano Malavolta\n\tUniversita' degli studi di L'Aquila\n\tcorso di Laurea Specialistica in Informatica\n\tmatricola 169201\n");
		try {
			Launcher.cap = new PacketCapture();
			String[] devs = PacketCapture.lookupDevices();
			Launcher.captors = new PacketCapture[devs.length];
			for(int i=0; i<devs.length; i++) {
				Launcher.captors[i] = new PacketCapture(); 
				Launcher.captors[i].open(devs[i].substring(0, devs[i].indexOf("\n")), 2000, false, 20);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		Launcher inst = new Launcher();
		inst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		inst.setSize(new Dimension(700, 700));
		inst.setVisible(true);
	}
	
	/**
	 * constructor
	 */
	Launcher() {
		super("winKnocks  v1.0 listener");
		new Splash(true);
		initGUI();
		options = new OptionsManager(this);
		updateListOfSequences();
		this.options.checkFilter();
		File f = new File("options.xml");
		if (!f.exists()) {
			this.openOptionsDialog();
		}
		if(!this.isFirewallActive()) {
			this.consoleOutput.append("\nWARNING --- Windows firewall is not operative");
			consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
		}
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
			this.menuFile = new JMenu("file");
			JMenuItem newMenu = new JMenuItem("new");
			newMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					putStringInEditor();
					xmlEditor.setEditable(false);
				}
			});
			this.menuFile.add(newMenu);
			JMenuItem modifyMenu = new JMenuItem("edit");
			modifyMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					xmlEditor.setEditable(true);
				}
			});
			this.menuFile.add(modifyMenu);
			JMenuItem renameMenu = new JMenuItem("rename");
			renameMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					KnockSequence sequence = sequencesManager.getSequenceByID(ids[list.getSelectedIndex()]);
					openRenameDialog(sequence);
					xmlEditor.setEditable(false);
				}
			});
			this.menuFile.add(renameMenu);
			JMenuItem saveMenu = new JMenuItem("save");
			saveMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					KnockSequence sequence = sequencesManager.getSequenceByID(ids[list.getSelectedIndex()]);
					openSaveDialog(sequence);
				}
			});
			this.menuFile.add(saveMenu);
			JMenuItem deleteMenu = new JMenuItem("delete");
			deleteMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					KnockSequence sequence = sequencesManager.getSequenceByID(ids[list.getSelectedIndex()]);
					openDeleteDialog(sequence);
				}
			});
			this.menuFile.add(deleteMenu);
			this.menuFile.addSeparator();
			JMenuItem exitMenu = new JMenuItem("exit");
		    exitMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					dispose();
					System.exit(1);
				}
			});
			this.menuFile.add(exitMenu);
			// editing section
			this.menuEditing = new JMenu("editing");
			JMenu action = new JMenu("add server-side action");
			JMenu packet = new JMenu("add a packet to the sequence");
			
			JMenuItem openPort = new JMenuItem("Open port");
		    openPort.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (checkActionPosition(xmlEditor.getText(), xmlEditor.getCaretPosition())) {
						String code = "\n\t\t<openPort>\n\t\t\t<portNumber>2000</portNumber>\n\t\t\t" + 
										"<exclusive>true</exclusive>\n\t\t\t" + 
										"<wait>0</wait>\n\t\t\t<timeout>60</timeout>\n\t\t</openPort>\n";
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
			this.menuEditing.add(action);
			this.menuEditing.add(packet);
		    // actions
			JMenu menuActions = new JMenu("actions");
			this.listenMenu = new JMenuItem("start listening...");
		    this.listenMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
				   stopListenMenu.setEnabled(true);
				   listenTestMenu.setEnabled(false);
				   listenMenu.setEnabled(false);
				   listenBkMenu.setEnabled(false);
				   refreshMenu.setEnabled(false);
				   menuFile.setEnabled(false);
				   menuOptions.setEnabled(false);
				   menuEditing.setEnabled(false);
				   list.setEnabled(false);
					xmlEditor.setEditable(false);
				   startListening(false, null);
				}
			});
			menuActions.add(this.listenMenu);
			
			this.listenBkMenu = new JMenuItem("start listening in background...");
		    this.listenBkMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					stopListenMenu.setEnabled(true);
					listenTestMenu.setEnabled(false);
					listenMenu.setEnabled(false);
					listenBkMenu.setEnabled(false);
					startListening(true, null);
				}
			}); 
			menuActions.add(this.listenBkMenu);
			
			this.listenTestMenu = new JMenuItem("start listening in test mode...");
		    this.listenTestMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					   stopListenMenu.setEnabled(true);
					   listenTestMenu.setEnabled(false);
					   listenMenu.setEnabled(false);
					   listenBkMenu.setEnabled(false);
					   refreshMenu.setEnabled(false);
					   menuFile.setEnabled(false);
					   menuOptions.setEnabled(false);
					   menuEditing.setEnabled(false);
					   list.setEnabled(false);
					   xmlEditor.setEditable(false);
					   JFileChooser chooser = new JFileChooser(".");
					   int returnVal = chooser.showOpenDialog(Launcher.this);
					   File file = null;
					   if (returnVal == JFileChooser.APPROVE_OPTION) {
						   file = chooser.getSelectedFile();
					   } else {
						   return;
					   }
					   startListening(false, file.getAbsolutePath());
				}
			}); 
			menuActions.add(this.listenTestMenu);
			
			this.stopListenMenu = new JMenuItem("stop listening");
		    this.stopListenMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					stopListening();
				}
			});
		    this.stopListenMenu.setEnabled(false);
			menuActions.add(this.stopListenMenu);
			
			this.refreshMenu = new JMenuItem("refresh list");
		    this.refreshMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					consoleOutput.append("\nList of Knock sequences updated...");
					updateListOfSequences();
					xmlEditor.setEditable(false);
				}
			});
			menuActions.add(this.refreshMenu);
		
			JMenuItem fireMenu = new JMenuItem("check firewall status");
		    fireMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if(isFirewallActive()) {
						consoleOutput.append("\nINFO --- firewall status: active");	
						consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
					} else {
						consoleOutput.append("\nINFO --- firewall status: not active");
						consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
					}
				}
			});
			menuActions.add(fireMenu);
			
			final JCheckBoxMenuItem activeMenu = new JCheckBoxMenuItem("activate Windows firewall");
		    activeMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					String[] command = {"netsh", "firewall", "set", "opmode", "ENABLE"};
					try {
						if(activeMenu.isSelected()) {
							Utility.executeCommand(command);
						} else {
							command[4] = "DISABLE";
							Utility.executeCommand(command);
						}
					    if(isFirewallActive()) {
					    	activeMenu.setSelected(true);
							consoleOutput.append("\nINFO --- firewall status: active");	
							consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
					    } else {
							consoleOutput.append("\nINFO --- firewall status: not active");
							consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
					    }
					} catch (Exception e) {}
				}
			});
		    if(this.isFirewallActive()) {
		    	activeMenu.setSelected(true);
				consoleOutput.append("\nINFO --- firewall status: active");	
				consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
		    } else {
				consoleOutput.append("\nINFO --- firewall status: not active");
				consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
		    }
			menuActions.add(activeMenu);
			
			this.menuOptions = new JMenu("options");
			JMenuItem optionsMenu = new JMenuItem("settings");
		    optionsMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					openOptionsDialog();
					xmlEditor.setEditable(false);
				}
			});
			this.menuOptions.add(optionsMenu);
			this.menuOptions.addSeparator();
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
		    this.menuOptions.add(aqua);
		    this.menuOptions.add(charcoal);
		    this.menuOptions.add(contrast);
		    this.menuOptions.add(ruby);
		    this.menuOptions.add(emerald);
			JMenu menuAbout = new JMenu("?");
			JMenuItem aboutMenu = new JMenuItem("about...");
		    aboutMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					new Splash(false);
				}
			});
			menuAbout.add(aboutMenu);	
			
			mybar.add(this.menuFile);
			mybar.add(menuActions);
			mybar.add(this.menuEditing);
			mybar.add(this.menuOptions);
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
			this.scrollPane2 = new JScrollPane(this.xmlEditor);
			this.scrollPane2.setPreferredSize(new java.awt.Dimension(300, 200));
	        this.scrollPane2.setViewportView(this.xmlEditor);
	        this.xmlEditor.setEditable(false);
	        this.xmlEditor.setContentType("text/english");
	        ScEditorKit ek = new ScEditorKit();
	        this.xmlEditor.setEditorKit(ek);
	        ek.readSyntaxColorDescriptor("lib/knockSequence.prop");
			this.splitted = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, scrollPane, this.scrollPane2);
			this.splitted.setDividerSize(5);
			mainPanel.add(this.splitted, BorderLayout.CENTER);
			
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
		this.list.setSelectedIndex(0);
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
			//e.printStackTrace();
		}
	}
		
	private void openOptionsDialog() {
		JPanel panel = new JPanel(new GridLayout(8,0));
		JCheckBox checkBox = new JCheckBox("Allow execution of scripts in urgent mode");
		if(this.options.isUrgentScripts()) {
			checkBox.setSelected(true);
		}
		panel.add(checkBox);
		panel.add(new JLabel(""));
		JPanel filter = new JPanel(new GridLayout(2, 0));
		this.filterField = new JTextField();
		this.automaticFilter = new JCheckBox("Automatically generate the filter of the knock listener [recommended]");
	    automaticFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				filterField.setEditable(!automaticFilter.isSelected());
			}
		});
	    if(this.options.isAutomatic()) {
	    	this.automaticFilter.setSelected(true);
	    	this.filterField.setEditable(false);
	    } else {
	    	this.automaticFilter.setSelected(false);
	    	this.filterField.setText(this.options.getCaptorFilter());
	    }
		filter.add(this.automaticFilter);
		filter.add(this.filterField);
		panel.add(filter);
		panel.add(new JLabel(""));
		JPanel panel3 = new JPanel(new GridLayout(2, 0));
		panel3.add(new JLabel("Key to decrypt the payload of the packets:"));
		this.keyField = new JTextField(this.options.getKey());
		panel3.add(this.keyField);
		panel.add(panel3);
		panel.add(new JLabel(""));
		JPanel panel2 = new JPanel(new GridLayout(2, 0));
		panel2.add(new JLabel("Device used to receive knock sequences :"));
		Vector<String> elem = new Vector<String>();
		Vector<String> vec = new Vector<String>();
		boolean selected = false;
		int index = 0;
		try {
			String[] devs = PacketCapture.lookupDevices();
			for(int i=0; i<devs.length; i++) {
				String temp = devs[i].substring(devs[i].indexOf("\n"));
				if (devs[i].equals(this.options.getDevice())) {
					selected = true;
				} 
				if (!selected) {
					index++;
				}
				vec.add(devs[i]);
				elem.add(temp);
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
		panel2.add(devices);
		panel.add(panel2);
		panel.add(new JLabel(""));
		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.OK_CANCEL_OPTION, JOptionPane.OK_CANCEL_OPTION,
									new ImageIcon("img/settings.png"));
		JDialog dialog = optionPane.createDialog(this, "winKnock v1.0 settings");
		dialog.pack();
		dialog.setVisible(true);
		if((optionPane.getValue() != null) && (((Integer) optionPane.getValue()).intValue() != 2)) {
			String device = "";
			if(!vec.isEmpty()) {
				device = vec.elementAt(devices.getSelectedIndex());
			}
			this.options.setUrgentScripts(checkBox.isSelected());
			if(!this.automaticFilter.isSelected()) {
				this.options.setCaptorFilter(this.filterField.getText());
			} else {
				this.options.setCaptorFilter("");
			}
			this.options.setDevice(device);
			if(this.keyField.equals("")) {
				this.options.setKey("default");
			} else {
				this.options.setKey(this.keyField.getText());
			}
			this.options.updateOptionsFile();
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
			xmlEditor.setEditable(false);
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
	
	private boolean isFirewallActive() {
		String[] command = {"netsh", "firewall", "show", "state"};
		String state = "";
		try {
			state = Utility.executeCommand(command);
		} catch(Exception e) {
			e.printStackTrace();
		}
		state = state.replaceAll(" ", "");
		return state.contains("operativa=Enable");
	}
	
	/**
	 * this method is called when the tool starts to listen for incoming knock sequences
	 */
	private void startListening(boolean backgroundMode, String fileName) {
		if(fileName != null) {
			try {
				Launcher.cap.openOffline(fileName);
			} catch(Exception e) {
				this.consoleOutput.append("\nERROR --- '" + fileName + "' is not a valid test-bed file");
				//e.printStackTrace();
			}
			this.listener = new PacketListener(this, backgroundMode, Launcher.cap, true);
			this.list.setEnabled(false);
			this.logging = new JTextArea();
			JScrollPane scr = new JScrollPane(this.logging);
			this.splitted.setRightComponent(scr);
			this.listener.startListening();
			return;
		}
		if(backgroundMode) {
			this.dispose();
		} else {
			this.consoleOutput.append("\nSTART listening for knock sequences...");
			consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
		}
		int index = Utility.getCaptorIndex(this.options.getDevice());
		if(index != -1) {
			this.listener = new PacketListener(this, backgroundMode, Launcher.captors[index], false);
			this.list.setEnabled(false);
			this.logging = new JTextArea();
			JScrollPane scr = new JScrollPane(this.logging);
			this.splitted.setRightComponent(scr);
			this.listener.startListening();
		} else {
			this.consoleOutput.append("\n\tERROR --- unable to receive packet on the specified interface(" + this.options.getDevice() + "), please check the options file");
			consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
		}
	}
	
	/**
	 * this method is called when the tool stops to listen for incoming knock sequences
	 */
	public void stopListening() {
		this.stopListenMenu.setEnabled(false);
		this.listenMenu.setEnabled(true);
		this.listenBkMenu.setEnabled(true);
		this.listenTestMenu.setEnabled(true);
		this.refreshMenu.setEnabled(true);
		this.menuFile.setEnabled(true);
		this.menuOptions.setEnabled(true);
		this.menuEditing.setEnabled(true);
		this.list.setEnabled(true);
		this.xmlEditor.setEditable(false);
		this.listener.stopListening();
		this.consoleOutput.append("\nSTOP listening for knock sequences...");
		consoleOutput.setCaretPosition(consoleOutput.getText().length() - 1);
		this.list.setEnabled(true);
		this.splitted.setRightComponent(this.scrollPane2);
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
	 * @return Returns the automaticFilter.
	 */
	public JCheckBox getAutomaticFilter() {
		return this.automaticFilter;
	}

	/**
	 * @param automaticFilter The automaticFilter to set.
	 */
	public void setAutomaticFilter(JCheckBox automaticFilter) {
		this.automaticFilter = automaticFilter;
	}

	/**
	 * @return Returns the filterField.
	 */
	public JTextField getFilterField() {
		return this.filterField;
	}

	/**
	 * @param filterField The filterField to set.
	 */
	public void setFilterField(JTextField filterField) {
		this.filterField = filterField;
	}

	/**
	 * @return Returns the ids.
	 */
	public int[] getIds() {
		return this.ids;
	}

	/**
	 * @param ids The ids to set.
	 */
	public void setIds(int[] ids) {
		this.ids = ids;
	}

	/**
	 * @return Returns the keyField.
	 */
	public JTextField getKeyField() {
		return this.keyField;
	}

	/**
	 * @param keyField The keyField to set.
	 */
	public void setKeyField(JTextField keyField) {
		this.keyField = keyField;
	}

	/**
	 * @return Returns the listenBkMenu.
	 */
	public JMenuItem getListenBkMenu() {
		return this.listenBkMenu;
	}

	/**
	 * @param listenBkMenu The listenBkMenu to set.
	 */
	public void setListenBkMenu(JMenuItem listenBkMenu) {
		this.listenBkMenu = listenBkMenu;
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

	/**
	 * @return Returns the listenMenu.
	 */
	public JMenuItem getListenMenu() {
		return this.listenMenu;
	}

	/**
	 * @param listenMenu The listenMenu to set.
	 */
	public void setListenMenu(JMenuItem listenMenu) {
		this.listenMenu = listenMenu;
	}

	/**
	 * @return Returns the logging.
	 */
	public JTextArea getLogging() {
		return this.logging;
	}

	/**
	 * @param logging The logging to set.
	 */
	public void setLogging(JTextArea logging) {
		this.logging = logging;
	}

	/**
	 * @return Returns the menuEditing.
	 */
	public JMenu getMenuEditing() {
		return this.menuEditing;
	}

	/**
	 * @param menuEditing The menuEditing to set.
	 */
	public void setMenuEditing(JMenu menuEditing) {
		this.menuEditing = menuEditing;
	}

	/**
	 * @return Returns the menuFile.
	 */
	public JMenu getMenuFile() {
		return this.menuFile;
	}

	/**
	 * @param menuFile The menuFile to set.
	 */
	public void setMenuFile(JMenu menuFile) {
		this.menuFile = menuFile;
	}

	/**
	 * @return Returns the menuOptions.
	 */
	public JMenu getMenuOptions() {
		return this.menuOptions;
	}

	/**
	 * @param menuOptions The menuOptions to set.
	 */
	public void setMenuOptions(JMenu menuOptions) {
		this.menuOptions = menuOptions;
	}

	/**
	 * @return Returns the refreshMenu.
	 */
	public JMenuItem getRefreshMenu() {
		return this.refreshMenu;
	}

	/**
	 * @param refreshMenu The refreshMenu to set.
	 */
	public void setRefreshMenu(JMenuItem refreshMenu) {
		this.refreshMenu = refreshMenu;
	}

	/**
	 * @return Returns the splitted.
	 */
	public JSplitPane getSplitted() {
		return this.splitted;
	}

	/**
	 * @param splitted The splitted to set.
	 */
	public void setSplitted(JSplitPane splitted) {
		this.splitted = splitted;
	}

	/**
	 * @return Returns the stopListenMenu.
	 */
	public JMenuItem getStopListenMenu() {
		return this.stopListenMenu;
	}

	/**
	 * @param stopListenMenu The stopListenMenu to set.
	 */
	public void setStopListenMenu(JMenuItem stopListenMenu) {
		this.stopListenMenu = stopListenMenu;
	}

	/**
	 * @return Returns the captors.
	 */
	public static PacketCapture[] getCaptors() {
		return captors;
	}

	/**
	 * @param captors The captors to set.
	 */
	public static void setCaptors(PacketCapture[] captors) {
		Launcher.captors = captors;
	}

	/**
	 * @return Returns the serialVersionUID.
	 */
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	/**
	 * @return Returns the listenTestMenu.
	 */
	public JMenuItem getListenTestMenu() {
		return this.listenTestMenu;
	}

	/**
	 * @param listenTextMenu The listenTestMenu to set.
	 */
	public void setListenTestMenu(JMenuItem listenTestMenu) {
		this.listenTestMenu = listenTestMenu;
	}

	/**
	 * @return Returns the scrollPane2.
	 */
	public JScrollPane getScrollPane2() {
		return this.scrollPane2;
	}

	/**
	 * @param scrollPane2 The scrollPane2 to set.
	 */
	public void setScrollPane2(JScrollPane scrollPane2) {
		this.scrollPane2 = scrollPane2;
	}
}
