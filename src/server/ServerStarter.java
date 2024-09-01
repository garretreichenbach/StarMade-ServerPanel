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
import server.ui.FileTreeModel;

import javax.swing.*;
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
	private ArrayList<String> openDocuments = new ArrayList<>();

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
	}

	public static void main(String[] args) {
		if(args != null && args.length > 0) gameVersion = args[0];
		else gameVersion = "0.301.100";
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
							RTextScrollPane scrollPane = new RTextScrollPane(editorTextPane);
							editorTextPane.setCodeFoldingEnabled(true);
							editorTextPane.setPaintTabLines(true);
							editorTextPane.setAutoIndentEnabled(true);
							editorTextPane.setBracketMatchingEnabled(true);
							editorTextPane.setCloseCurlyBraces(true);
							fileEditorPane.addTab(file.getName(), scrollPane);
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
							menuBar.setVisible(true);

							filesPanel.add(menuBar);
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
}