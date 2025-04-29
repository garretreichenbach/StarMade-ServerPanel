package server;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.RecordableTextAction;
import server.data.config.ConfigFile;
import server.data.config.PanelConfigManager;
import server.data.config.ServerConfig;
import server.data.documents.DocumentFile;
import server.ui.DatabaseTable;
import server.ui.FileTreeModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class ServerStarter extends JFrame {

	public static final int CONTROL_PANEL = 0;
	public static final int ACTIONS_PANEL = 1;
	public static final int LOGS_PANEL = 2;
	public static final int CONFIG_PANEL = 3;
	public static final int FILES_PANEL = 4;
	public static final int DATABASE_PANEL = 5;

	private static final ArrayList<String> args = new ArrayList<>();
	private static String gameVersion;
	private JTabbedPane tabPanel;
	private JPanel mainPanel;
	private JList logFiles;
	private JScrollPane logConsole;
	private JTextField commandInput;
	private JButton commandListButton;
	private JButton sendButton;
	private JTextPane logPane;
	private JTextPane configEditor;
	private JList configList;
	private JButton saveButton;
	private JButton saveAndRestartServerButton;
	private JButton revertChangesButton;
	private JTree fileTree;
	private JPanel buttonPanel;
	private JButton startServerButton;
	private JButton stopServerButton;
	private JButton restartServerButton;
	private JButton updateServerButton;
	private JPanel infoPanel;
	private JTextField serverDescriptionField;
	private JLabel severNameLabel;
	private JLabel serverDescriptionLabel;
	private JLabel serverStatusLabel;
	private JTextField serverIPField;
	private JTextField serverPortField;
	private JTextField serverNameField;
	private JSlider maxPlayers;
	private JList playerList;
	private JCheckBox publicServerCheckBox;
	private JCheckBox useAuthenticationCheckBox;
	private JCheckBox requireAuthenticationCheckBox;
	private JTabbedPane fileEditorPane;
	private JPanel filesPanel;
	private JTabbedPane databaseTabs;
	private ArrayList<String> openDocuments = new ArrayList<>();
	private String currentWorldName = "";

	public ServerStarter() {
		setUndecorated(true);
		setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
		setResizable(false);
		getRootPane().setDoubleBuffered(true);
		initMainPane();
		initActionsPane();
		initLogsPane();
		initConfigPane();
		initFilePane();
		initDatabasePane();
	}

	public static void main(String[] args) {
		if(args != null && args.length > 0) gameVersion = args[0];
		else gameVersion = "0.302.100";
		if(PanelConfigManager.ConfigValues.THEME.getValue().equals("DARK")) FlatDarkLaf.setup();
		else FlatLightLaf.setup();
		JFrame frame = new JFrame("StarMade Server [" + gameVersion + "]");
		frame.setContentPane(new ServerStarter().mainPanel);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	private void initMainPane() {
		startServerButton.addActionListener(e -> {
			try {
				StarMadeUtils.start(gameVersion, args);
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		});
		stopServerButton.addActionListener(e -> {
			JDialog dialog = new JDialog();
			dialog.setModal(true);
			dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			dialog.setSize(200, 100);
			dialog.setLocationRelativeTo(null);
			dialog.setLayout(null);
			JLabel label = new JLabel("Seconds:");
			label.setBounds(10, 10, 100, 20);
			JTextField field = new JTextField();
			field.setBounds(10, 30, 100, 20);
			JButton button = new JButton("Stop");
			button.setBounds(10, 50, 100, 20);
			button.addActionListener(e1 -> {
				try {
					StarMadeUtils.stop(Integer.parseInt(field.getText()));
				} catch(Exception exception) {
					exception.printStackTrace();
				}
				dialog.dispose();
			});
			dialog.add(label);
			dialog.add(field);
			dialog.add(button);
			dialog.setVisible(true);
		});
		restartServerButton.addActionListener(e -> {
			JDialog dialog = new JDialog();
			dialog.setModal(true);
			dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			dialog.setSize(200, 100);
			dialog.setLocationRelativeTo(null);
			dialog.setLayout(null);
			JLabel label = new JLabel("Seconds:");
			label.setBounds(10, 10, 100, 20);
			JTextField field = new JTextField();
			field.setBounds(10, 30, 100, 20);
			JButton button = new JButton("Restart");
			button.setBounds(10, 50, 100, 20);
			button.addActionListener(e1 -> {
				try {
					StarMadeUtils.restart(Integer.parseInt(field.getText()));
				} catch(Exception exception) {
					exception.printStackTrace();
				}
				dialog.dispose();
			});
			dialog.add(label);
			dialog.add(field);
			dialog.add(button);
			dialog.setVisible(true);
		});
		updateServerButton.addActionListener(e -> {
			//Todo: Update dialog
		});
		serverNameField.setText(ServerConfig.SERVER_LIST_NAME.getValue().toString());
		serverNameField.addActionListener(e -> ServerConfig.SERVER_LIST_NAME.setValue(serverNameField.getText()));
		
		serverDescriptionField.setText(ServerConfig.SERVER_LIST_DESCRIPTION.getValue().toString());
		serverDescriptionField.addActionListener(e -> ServerConfig.SERVER_LIST_DESCRIPTION.setValue(serverDescriptionField.getText()));
		
		serverIPField.setText((ServerConfig.HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST.getValue().toString().contains(":") ? ServerConfig.HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST.getValue().toString().split(":")[0] : ServerConfig.HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST.getValue().toString()));
		serverIPField.addActionListener(e -> ServerConfig.HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST.setValue((serverPortField.getText().isEmpty() ? serverIPField.getText() : serverIPField.getText() + ":" + serverPortField.getText())));
		
		serverPortField.setText((ServerConfig.HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST.getValue().toString().contains(":") ? ServerConfig.HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST.getValue().toString().split(":")[1] : ""));
		serverPortField.addActionListener(e -> ServerConfig.HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST.setValue((serverIPField.getText().isEmpty() ? serverPortField.getText() : serverIPField.getText() + ":" + serverPortField.getText())));

		publicServerCheckBox.addChangeListener(e -> ServerConfig.ANNOUNCE_SERVER_TO_SERVERLIST.setValue(publicServerCheckBox.isSelected()));
		useAuthenticationCheckBox.addChangeListener(e -> ServerConfig.USE_STARMADE_AUTHENTICATION.setValue(useAuthenticationCheckBox.isSelected()));
		requireAuthenticationCheckBox.addChangeListener(e -> ServerConfig.REQUIRE_STARMADE_AUTHENTICATION.setValue(requireAuthenticationCheckBox.isSelected() && useAuthenticationCheckBox.isSelected()));
		maxPlayers.addChangeListener(e -> ServerConfig.MAX_CLIENTS.setValue(maxPlayers.getValue()));
	}

	private void initActionsPane() {
	}

	private void initLogsPane() {
	}

	private void initConfigPane() {
		configList.setModel(new DefaultListModel<DocumentFile>());
		configList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public java.awt.Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				label.setText(value.toString());
				return label;
			}
		});
		configList.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if(e.getClickCount() == 2) {
					JList list = (JList) e.getSource();
					ConfigFile configFile = (ConfigFile) list.getSelectedValue();
					if(configFile != null) {
						try {

						} catch(Exception exception) {
							exception.printStackTrace();
						}
					}
				}
			}

			private JMenuItem createMenuItem(RecordableTextAction action) {
				JMenuItem item = new JMenuItem(action);
				item.setToolTipText(null);
				return item;
			}

			private JMenuItem createMenuItem(String text, java.awt.event.ActionListener listener) {
				JMenuItem item = new JMenuItem(text);
				item.addActionListener(listener);
				return item;
			}
		});
	}

	private void initFilePane() {
		fileTree.setModel(new FileTreeModel(StarMadeUtils.getSMFolder()));
		fileTree.setCellRenderer(new FileTreeModel.FileTreeCellRenderer());
		fileTree.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if(e.getClickCount() == 2) {
					JTree tree = (JTree) e.getSource();
					FileTreeModel model = (FileTreeModel) tree.getModel();
					File file = (File) tree.getLastSelectedPathComponent();
					if(file.isFile() && model.canEdit(file)) {
						try {
							if(openDocuments.contains(file.getName())) {
								fileEditorPane.setSelectedIndex(openDocuments.indexOf(file.getName()));
								return;
							}
							String documentText = Files.readString(Path.of(file.getPath()));
							RSyntaxDocument document = new RSyntaxDocument(DocumentFile.DocumentType.getType(file.getName()).style);
							document.insertString(0, documentText, null);
							RSyntaxTextArea editorTextPane = new RSyntaxTextArea(document);

							JMenuBar menuBar = new JMenuBar();
							JMenu fileMenu = new JMenu("File");
							fileMenu.add(createMenuItem("Save", e1 -> {
								try {
									Files.writeString(Path.of(file.getPath()), editorTextPane.getText());
								} catch(Exception exception) {
									exception.printStackTrace();
								}
							}));
							fileMenu.add(createMenuItem("Save As...", e1 -> {
								JFileChooser fileChooser = new JFileChooser();
								fileChooser.setDialogTitle("Save As...");
								fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
								fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", DocumentFile.DocumentType.getType(file.getName()).endings));
								if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
									try {
										Files.writeString(fileChooser.getSelectedFile().toPath(), editorTextPane.getText());
									} catch(Exception exception) {
										exception.printStackTrace();
									}
								}
							}));
							fileMenu.addSeparator();
							fileMenu.add(createMenuItem("Close", e1 -> {
								fileEditorPane.removeTabAt(openDocuments.indexOf(file.getName()));
								openDocuments.remove(file.getName());
							}));
							menuBar.add(fileMenu);

							JMenu editMenu = new JMenu("Edit");
							editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.UNDO_ACTION)));
							editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.REDO_ACTION)));
							editMenu.addSeparator();
							editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.CUT_ACTION)));
							editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.COPY_ACTION)));
							editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.PASTE_ACTION)));
							editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.DELETE_ACTION)));
							editMenu.addSeparator();
							editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.SELECT_ALL_ACTION)));
							menuBar.add(editMenu);
							menuBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
							
							RTextScrollPane scrollPane = new RTextScrollPane(editorTextPane);
							editorTextPane.setCodeFoldingEnabled(true);
							editorTextPane.setPaintTabLines(true);
							editorTextPane.setAutoIndentEnabled(true);
							editorTextPane.setBracketMatchingEnabled(true);
							editorTextPane.setCloseCurlyBraces(true);

							JPanel editorContainerPanel = new JPanel();
							editorContainerPanel.setLayout(new BoxLayout(editorContainerPanel, BoxLayout.Y_AXIS));
							editorContainerPanel.add(menuBar);
							editorContainerPanel.add(scrollPane);

							fileEditorPane.addTab(file.getName(), editorContainerPanel);
							openDocuments.add(file.getName());
							JPanel panel = new JPanel();
							JLabel label = new JLabel(file.getName());
							panel.add(label);
							JButton closeButton = new JButton(UIManager.getIcon("InternalFrame.closeIcon"));
							closeButton.addActionListener(e1 -> {
								fileEditorPane.removeTabAt(openDocuments.indexOf(file.getName()));
								openDocuments.remove(file.getName());
							});
							closeButton.setBorder(BorderFactory.createEmptyBorder());
							closeButton.setOpaque(false);
							panel.add(closeButton);
							fileEditorPane.setTabComponentAt(openDocuments.indexOf(file.getName()), panel);
							fileEditorPane.setSelectedIndex(openDocuments.indexOf(file.getName()));
						} catch(Exception exception) {
							exception.printStackTrace();
						}
					} else if(file.isDirectory()) {
						if(file.getParentFile().getName().endsWith("server-database")) {
							openDatabaseManager(file.getName());
						} else if(file.getName().equals("logs")) {
							tabPanel.setSelectedIndex(LOGS_PANEL);
						}
					}
				}
			}

			private JMenuItem createMenuItem(RecordableTextAction action) {
				JMenuItem item = new JMenuItem(action);
				item.setToolTipText(null);
				return item;
			}

			private JMenuItem createMenuItem(String text, java.awt.event.ActionListener listener) {
				JMenuItem item = new JMenuItem(text);
				item.addActionListener(listener);
				return item;
			}
		});
	}

	private void initDatabasePane() {
		databaseTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		databaseTabs.setTabPlacement(SwingConstants.LEFT);
		databaseTabs.setPreferredSize(new Dimension(400, 300));
		databaseTabs.setMinimumSize(new Dimension(400, 300));
		databaseTabs.setMaximumSize(new Dimension(400, 300));
		File serverDatabaseFolder = new File(StarMadeUtils.getSMFolder(), "server-database");
		File[] worlds = serverDatabaseFolder.listFiles();
		if(worlds != null) {
			for(File world : worlds) {
				if(world.isDirectory()) {
					JPanel worldPanel = new JPanel();
					worldPanel.setLayout(new BoxLayout(worldPanel, BoxLayout.Y_AXIS));
					worldPanel.add(new JLabel(world.getName()));
					databaseTabs.addTab(world.getName(), worldPanel);

					DatabaseTable table = new DatabaseTable(world);
					JScrollPane scrollPane = new JScrollPane();
					scrollPane.setViewportView(table);
					worldPanel.add(scrollPane);

					JPanel buttonPanel = new JPanel();
					buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
					buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

					JButton openButton = new JButton("Refresh");
					openButton.addActionListener(e -> {
						try {
							table.refresh();
						} catch(Exception exception) {
							exception.printStackTrace();
						}
					});

					JButton searchButton = new JButton("Search");
					searchButton.addActionListener(e -> {
						JDialog searchDialog = new JDialog();
						searchDialog.setModal(true);
						searchDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
						searchDialog.setSize(300, 200);
						searchDialog.setLocationRelativeTo(null);
						searchDialog.setLayout(new BorderLayout());
						JPanel searchPanel = new JPanel();
						searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));

						JPanel searchTypesButtonsPanel = new JPanel();
						searchTypesButtonsPanel.setLayout(new BoxLayout(searchTypesButtonsPanel, BoxLayout.X_AXIS));
						searchTypesButtonsPanel.setBorder(BorderFactory.createTitledBorder("Search Tools"));
						JButton searchByNameButton = new JButton("Search by Name");
						JButton searchByIDButton = new JButton("Search by ID");
						JButton searchByTypeButton = new JButton("Search by Type");
						JButton searchBySectorButton = new JButton("Search by Sector");
						JButton searchBySystemButton = new JButton("Search by System");

						searchTypesButtonsPanel.add(searchByNameButton);
						searchTypesButtonsPanel.add(searchByIDButton);
						searchTypesButtonsPanel.add(searchByTypeButton);
						searchTypesButtonsPanel.add(searchBySectorButton);
						searchTypesButtonsPanel.add(searchBySystemButton);
						searchPanel.add(searchTypesButtonsPanel);

						searchByNameButton.addActionListener(e1 -> {
							String name = JOptionPane.showInputDialog("Enter name to search for:");
							if(name != null && !name.isEmpty()) {
								table.searchByName(name);
							}
						});
						searchByIDButton.addActionListener(e1 -> {
							String id = JOptionPane.showInputDialog("Enter ID to search for:");
							if(id != null && !id.isEmpty()) {
								table.searchByID(id);
							}
						});
						searchByTypeButton.addActionListener(e1 -> {
							String type = JOptionPane.showInputDialog("Enter type to search for:");
							if(type != null && !type.isEmpty()) {
								table.searchByType(type);
							}
						});
						searchBySectorButton.addActionListener(e1 -> {
							String sector = JOptionPane.showInputDialog("Enter sector to search for:");
							if(sector != null && !sector.isEmpty()) {
								table.searchBySector(sector);
							}
						});
						searchBySystemButton.addActionListener(e1 -> {
							String system = JOptionPane.showInputDialog("Enter system to search for:");
							if(system != null && !system.isEmpty()) {
								table.searchBySystem(system);
							}
						});
						searchDialog.add(searchPanel, BorderLayout.CENTER);
						searchDialog.setVisible(true);
					});

					JComboBox<String> typeDropdown = new JComboBox<>();
					typeDropdown.addItem("All");
					typeDropdown.addItem("Players");
					typeDropdown.addItem("Ships");
					typeDropdown.addItem("Stations");
					typeDropdown.addItem("Planets");
					typeDropdown.addItem("Asteroids");

				}
			}
		}
	}

	private void openDatabaseManager(String worldName) {
		currentWorldName = worldName;
		tabPanel.setSelectedIndex(DATABASE_PANEL);

	}
}