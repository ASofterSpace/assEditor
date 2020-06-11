/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.FileTab;
import com.asofterspace.toolbox.gui.FileTree;
import com.asofterspace.toolbox.gui.FileTreeCellRenderer;
import com.asofterspace.toolbox.gui.FileTreeFile;
import com.asofterspace.toolbox.gui.FileTreeFolder;
import com.asofterspace.toolbox.gui.FileTreeModel;
import com.asofterspace.toolbox.gui.FileTreeNode;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.gui.MainWindow;
import com.asofterspace.toolbox.gui.OpenFileDialog;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.utils.CallbackWithStatus;
import com.asofterspace.toolbox.utils.Record;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;


public class MainGUI extends MainWindow {

	private AugFileCtrl augFileCtrl;
	private MainMenu mainMenu;
	private MainPopupMenu mainPopupMenu;

	private JPanel mainPanelRight;

	private JPanel searchPanel;
	private JTextField searchField;
	private JTextField replaceField;

	private JPanel jumpToFilePanel;
	private JTextField jumpToFileField;
	private AugFileTab jumpToTab;

	private AugFileTab currentlyShownTab;

	private final static String CONFIG_KEY_LAST_DIRECTORY = "lastDirectory";
	private final static String CONFIG_KEY_SCHEME = "scheme";
	private final static String CONFIG_KEY_ANTI_ALIASING = "antiAliasing";
	private final static String CONFIG_KEY_DEFAULT_INDENTATION_STR = "defaultIndentationStr";
	private final static String CONFIG_KEY_REMOVE_TRAILING_WHITESPACE_ON_SAVE = "onSaveRemoveTrailingWhitespace";
	private final static String CONFIG_KEY_REPLACE_WHITESPACES_WITH_TABS_ON_SAVE = "onSaveReplaceWhitespacesWithTabs";
	private final static String CONFIG_KEY_REPLACE_TABS_WITH_WHITESPACES_ON_SAVE = "onSaveReplaceTabsWithWhitespaces";
	private final static String CONFIG_KEY_ADD_MISSING_IMPORTS_ON_SAVE = "onSaveAddMissingImports";
	private final static String CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE = "onSaveReorganizeImports";
	private final static String CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE_COMPATIBLE = "onSaveReorganizeImportsCompatible";
	private final static String CONFIG_KEY_REMOVE_UNUSED_IMPORTS_ON_SAVE = "onSaveRemoveUnusedImports";
	private final static String CONFIG_KEY_COPY_ON_ENTER = "copyOnEnter";
	private final static String CONFIG_KEY_TAB_ENTIRE_BLOCKS = "tabEntireBlocks";
	private final static String CONFIG_KEY_BACKUP_NUM = "backupNum";
	private final static String CONFIG_KEY_WIDTH = "mainFrameWidth";
	private final static String CONFIG_KEY_HEIGHT = "mainFrameHeight";
	private final static String CONFIG_KEY_LEFT = "mainFrameLeft";
	private final static String CONFIG_KEY_TOP = "mainFrameTop";
	private final static String CONFIG_KEY_FONT_SIZE = "fontSize";
	private final static String CONFIG_KEY_SHOW_FILES_IN_TREE = "showFilesInTree";

	public final static int DEFAULT_FONT_SIZE = 14;

	private List<AugFileTab> augFileTabs;
	private AugFileTab[] augFileTabArray;

	private ConfigFile configuration;
	private JList<AugFileTab> fileListComponent;
	private FileTree fileTreeComponent;
	private FileTreeModel fileTreeModel;
	private JTextField fileTreeEditField;
	private JScrollPane augFileListScroller;
	private JScrollPane augFileTreeScroller;
	private JTextArea noteArea;
	private JScrollPane noteAreaScroller;
	private SimpleFile noteAreaFile;

	private Integer currentBackup;

	private int fontSize;

	private boolean regenerateAugFileListEnabled = true;

	String currentScheme;
	Boolean useAntiAliasing;
	Boolean removeTrailingWhitespaceOnSave;
	Boolean replaceWhitespacesWithTabsOnSave;
	Boolean replaceTabsWithWhitespacesOnSave;
	Boolean addMissingImportsOnSave;
	Boolean reorganizeImportsOnSave;
	Boolean reorganizeImportsOnSaveCompatible;
	Boolean removeUnusedImportsOnSave;
	Boolean copyOnEnter;
	Boolean tabEntireBlocks;
	Boolean showFilesInTree;
	boolean showFiles;
	boolean standalone;
	String defaultIndentationStr;

	// this keeps track of the tabs we opened, and in which order we did so
	private List<AugFileTab> listOfPreviousTabs;
	// this keeps track of the tabs we intend to open in the future if we go to the next tab
	private List<AugFileTab> listOfFutureTabs;


	public MainGUI(AugFileCtrl augFileCtrl, ConfigFile config, boolean standalone) {

		this.augFileCtrl = augFileCtrl;

		this.configuration = config;

		this.standalone = standalone;

		this.showFiles = !standalone;

		augFileTabArray = new AugFileTab[0];
		fileTreeModel = new FileTreeModel();
		fileTreeModel.addTreeModelListener(new TreeModelListener() {
			public void treeNodesChanged(TreeModelEvent e) {
				FileTreeNode node = fileTreeModel.getChild(e.getTreePath());
				if (node instanceof FileTreeFile) {
					FileTreeFile file = (FileTreeFile) node;
					FileTab fileTab = file.getTab();
					if (fileTab  instanceof AugFileTab) {
						AugFileTab tab = (AugFileTab) fileTab;

						String oldName = tab.getName().trim();
						String newName = fileTreeEditField.getText().trim();

						// if we are changing the name of a file that has not yet been saved
						// (so still contains " *" at the end), get rid of that!
						if (oldName.endsWith(GuiUtils.CHANGE_INDICATOR)) {
							oldName = oldName.substring(0, oldName.length() - GuiUtils.CHANGE_INDICATOR.length());
						}
						if (newName.endsWith(GuiUtils.CHANGE_INDICATOR)) {
							newName = newName.substring(0, newName.length() - GuiUtils.CHANGE_INDICATOR.length());
						}

						// only if the new name is different from the old one...
						if (!newName.equals(oldName) && !newName.equals("")) {

							// save tab
							tab.save();
							String origFilePath = tab.getFilePath();

							// close tab
							List<AugFileTab> tabs = new ArrayList<>();
							tabs.add(tab);
							closeFiles(tabs);

							// rename file on disk
							File renameFile = new File(origFilePath);
							renameFile.rename(newName);

							// open renamed file as new tab
							AugFileTab latestTab = openFile(renameFile);
							if (latestTab == null) {
								// if something went wrong, try to re-open the old file (it might still be there)
								File oldFile = new File(origFilePath);
								latestTab = openFile(oldFile);
							}

							if (latestTab != null) {
								setCurrentlyShownTab(latestTab);
							}
							regenerateAugFileList();
							reEnableDisableMenuItems();
						}
					}
				}
			}
			public void treeNodesInserted(TreeModelEvent e) {}
			public void treeNodesRemoved(TreeModelEvent e) {}
			public void treeStructureChanged(TreeModelEvent e) {}
		});

		augFileTabs = new ArrayList<>();
		listOfPreviousTabs = new ArrayList<>();
		listOfFutureTabs = new ArrayList<>();

		useAntiAliasing = configuration.getBoolean(CONFIG_KEY_ANTI_ALIASING, true);
		updateUseAntiAliasing();

		currentScheme = configuration.getValue(CONFIG_KEY_SCHEME);

		if (currentScheme == null) {
			currentScheme = GuiUtils.DARK_SCHEME;
		}

		defaultIndentationStr = configuration.getValue(CONFIG_KEY_DEFAULT_INDENTATION_STR);

		removeTrailingWhitespaceOnSave = configuration.getBoolean(CONFIG_KEY_REMOVE_TRAILING_WHITESPACE_ON_SAVE, true);

		replaceWhitespacesWithTabsOnSave = configuration.getBoolean(CONFIG_KEY_REPLACE_WHITESPACES_WITH_TABS_ON_SAVE, true);

		replaceTabsWithWhitespacesOnSave = configuration.getBoolean(CONFIG_KEY_REPLACE_TABS_WITH_WHITESPACES_ON_SAVE, false);

		addMissingImportsOnSave = configuration.getBoolean(CONFIG_KEY_ADD_MISSING_IMPORTS_ON_SAVE, true);

		reorganizeImportsOnSave = configuration.getBoolean(CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE, true);

		reorganizeImportsOnSaveCompatible = configuration.getBoolean(CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE_COMPATIBLE, true);

		removeUnusedImportsOnSave = configuration.getBoolean(CONFIG_KEY_REMOVE_UNUSED_IMPORTS_ON_SAVE, true);

		copyOnEnter = configuration.getBoolean(CONFIG_KEY_COPY_ON_ENTER, true);

		tabEntireBlocks = configuration.getBoolean(CONFIG_KEY_TAB_ENTIRE_BLOCKS, true);

		currentBackup = configuration.getInteger(CONFIG_KEY_BACKUP_NUM, 0);

		fontSize = configuration.getInteger(CONFIG_KEY_FONT_SIZE, DEFAULT_FONT_SIZE);

		showFilesInTree = configuration.getBoolean(CONFIG_KEY_SHOW_FILES_IN_TREE, true);

		Thread backupThread = new Thread(new Runnable() {
			@Override
			public void run() {

				JSON configRoot = configuration.getAllContents();

				while (true) {

					if (currentlyShownTab != null) {
						currentlyShownTab.backup(currentBackup);

						currentBackup++;
						// if we ever change the rollover limit here, also update the
						// amount of leftpadding done in the currentlyShownTab.backup
						// function!
						if (currentBackup > 9999) {
							currentBackup = 0;
						}

						// we do not set on the config element, just on the JSON root,
						// as we do not want to save in here...
						configRoot.set(CONFIG_KEY_BACKUP_NUM, currentBackup);
					}

					// ... because we are going to save in here anyway!
					augFileCtrl.saveConfigFileList();

					try {
						// make backups once a minute
						Thread.sleep(60*1000);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
		backupThread.start();
	}

	@Override
	public void run() {

		super.create();

		// Add content to the window
		this.mainMenu = new MainMenu(this, mainFrame, augFileCtrl, standalone);
		mainMenu.createMenu();

		this.mainPopupMenu = new MainPopupMenu(this, mainFrame, standalone);
		mainPopupMenu.createPopupMenu();

		createMainPanel(mainFrame);

		noteAreaFile = new SimpleFile(configuration.getParentDirectory().getCanonicalDirname() + "/notes.txt");
		noteArea.setText(noteAreaFile.getContent());

		configureGUI();

		refreshTitleBar();

		reEnableDisableMenuItems();

		setScheme(currentScheme);

		// do not call super.show, as we are doing things a little bit
		// differently around here (including restoring from previous
		// position...)
		// super.show();

		final Integer lastWidth = configuration.getInteger(CONFIG_KEY_WIDTH, -1);
		final Integer lastHeight = configuration.getInteger(CONFIG_KEY_HEIGHT, -1);
		final Integer lastLeft = configuration.getInteger(CONFIG_KEY_LEFT, -1);
		final Integer lastTop = configuration.getInteger(CONFIG_KEY_TOP, -1);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Stage everything to be shown
				mainFrame.pack();

				// Actually display the whole jazz
				mainFrame.setVisible(true);

				if ((lastWidth < 1) || (lastHeight < 1)) {
					GuiUtils.maximizeWindow(mainFrame);
				} else {
					mainFrame.setSize(lastWidth, lastHeight);

					mainFrame.setPreferredSize(new Dimension(lastWidth, lastHeight));

					mainFrame.setLocation(new Point(lastLeft, lastTop));
				}

				mainFrame.addComponentListener(new ComponentAdapter() {
					public void componentResized(ComponentEvent componentEvent) {
						configuration.set(CONFIG_KEY_WIDTH, mainFrame.getWidth());
						configuration.set(CONFIG_KEY_HEIGHT, mainFrame.getHeight());
					}

					public void componentMoved(ComponentEvent componentEvent) {
						configuration.set(CONFIG_KEY_LEFT, mainFrame.getLocation().x);
						configuration.set(CONFIG_KEY_TOP, mainFrame.getLocation().y);
					}
				});

				// we allow saving, which will happen in the backup thread
				configuration.allowSaving();
			}
		});

		reloadAllAugFileTabs();
	}

	private JPanel createMainPanel(JFrame parent) {

		JPanel mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(800, 500));
		GridBagLayout mainPanelLayout = new GridBagLayout();
		mainPanel.setLayout(mainPanelLayout);

		JPanel mainPanelRightOuter = new JPanel();
		GridBagLayout mainPanelRightOuterLayout = new GridBagLayout();
		mainPanelRightOuter.setLayout(mainPanelRightOuterLayout);

		mainPanelRight = new JPanel();
		mainPanelRight.setLayout(new CardLayout());
		mainPanelRight.setPreferredSize(new Dimension(8, 8));


		// create file list and tree

		fileListComponent = new JList<AugFileTab>(augFileTabArray);
		fileTreeComponent = new FileTree(fileTreeModel);
		fileTreeEditField = new JTextField();
		TreeCellEditor fileTreeEditor = new DefaultCellEditor(fileTreeEditField);
		fileTreeComponent.setEditable(true);
		fileTreeComponent.setCellEditor(fileTreeEditor);
		augFileTabs = new ArrayList<>();

		fileListComponent.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				showSelectedTab();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				showPopupAndSelectedTab(e);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				showPopupAndSelectedTab(e);
			}

			private void showPopupAndSelectedTab(MouseEvent e) {
				if (e.isPopupTrigger()) {
					fileListComponent.setSelectedIndex(fileListComponent.locationToIndex(e.getPoint()));
					mainPopupMenu.getPopupMenu().show(fileListComponent, e.getX(), e.getY());
				}

				showSelectedTab();
			}
		});

		KeyListener selectionKeyListener = new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_UP:
					case KeyEvent.VK_DOWN:
						showSelectedTab();
						break;
				}
			}
		};

		fileListComponent.addKeyListener(selectionKeyListener);

		fileTreeComponent.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.isPopupTrigger() || (e.getButton() == 3)) {
					mainPopupMenu.getPopupMenu().show(fileTreeComponent, e.getX(), e.getY());
				}

				TreePath path = fileTreeComponent.getPathForLocation(e.getX(), e.getY());
				FileTreeNode node = fileTreeModel.getChild(path);
				if ((node != null) && (node instanceof FileTreeFile)) {
					FileTreeFile file = (FileTreeFile) node;
					FileTab tab = file.getTab();
					if (tab instanceof AugFileTab) {
						showTab((AugFileTab) tab, false);
					}
				}
			}
		});

		fileTreeComponent.addKeyListener(selectionKeyListener);

		augFileListScroller = new JScrollPane(fileListComponent);
		augFileListScroller.setPreferredSize(new Dimension(8, 8));
		augFileListScroller.setBorder(BorderFactory.createEmptyBorder());

		augFileTreeScroller = new JScrollPane(fileTreeComponent);
		augFileTreeScroller.setPreferredSize(new Dimension(8, 8));
		augFileTreeScroller.setBorder(BorderFactory.createEmptyBorder());

		updateShowFilesInTree();


		// create jump to file panel

		jumpToFilePanel = new JPanel();
		jumpToFilePanel.setLayout(new GridBagLayout());
		jumpToFilePanel.setVisible(false);

		jumpToFileField = new JTextField();

		// listen to text updates
		jumpToFileField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				jump();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				jump();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				jump();
			}
			private void jump() {
				String jumpToTabStr = jumpToFileField.getText().toLowerCase();
				String jumpToLineStr = null;

				if (jumpToTabStr.contains(":")) {
					jumpToLineStr = jumpToTabStr.substring(jumpToTabStr.indexOf(":") + 1);
					jumpToTabStr = jumpToTabStr.substring(0, jumpToTabStr.indexOf(":"));
				}

				if (!"".equals(jumpToTabStr)) {
					for (AugFileTab tab : augFileTabs) {
						if (tab.getName().toLowerCase().startsWith(jumpToTabStr)) {
							jumpToTab = tab;
							showTab(tab, false);
							if (jumpToLineStr != null) {
								try {
									int jumpToLineInt = Integer.parseInt(jumpToLineStr.trim());
									tab.jumpToLine(jumpToLineInt);
								} catch (NumberFormatException e) {
									// whoops!
								}
							}
							return;
						}
					}
				}

				if (jumpToLineStr != null) {
					try {
						int jumpToLineInt = Integer.parseInt(jumpToLineStr.trim());
						currentlyShownTab.jumpToLine(jumpToLineInt);
					} catch (NumberFormatException e) {
						// whoops!
					}
				}
			}
		});

		// listen to the enter key being pressed (which does not create text updates)
		jumpToFileField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jumpToTab != null) {
					showTab(jumpToTab, true);
				}
				jumpToFilePanel.setVisible(false);
			}
		});

		// listen to being focused, and when it happens, select all the current content
		jumpToFileField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				jumpToFileField.selectAll();
			}
			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		jumpToFilePanel.add(jumpToFileField, new Arrangement(0, 0, 1.0, 1.0));



		// create search panel

		searchPanel = new JPanel();
		searchPanel.setLayout(new GridBagLayout());
		searchPanel.setVisible(false);

		searchField = new JTextField();

		// listen to text updates
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				search();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				search();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				search();
			}
			private void search() {
				String searchFor = searchField.getText();

				if (currentlyShownTab != null) {
					currentlyShownTab.search(searchFor);
				}
			}
		});

		// listen to the enter key being pressed (which does not create text updates)
		searchField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String searchFor = searchField.getText();

				if (currentlyShownTab != null) {
					currentlyShownTab.search(searchFor);
				}
			}
		});

		// listen to being focused, and when it happens, select all the current content
		searchField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				searchField.selectAll();
			}
			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		replaceField = new JTextField();

		// listen to the enter key being pressed (which does not create text updates)
		replaceField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String searchFor = searchField.getText();
				String replaceWith = replaceField.getText();

				if (currentlyShownTab != null) {
					currentlyShownTab.replaceAll(searchFor, replaceWith);
				}
			}
		});

		// listen to being focused, and when it happens, select all the current content
		replaceField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				replaceField.selectAll();
			}
			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		searchPanel.add(searchField, new Arrangement(0, 0, 1.0, 1.0));
		searchPanel.add(replaceField, new Arrangement(0, 1, 1.0, 1.0));


		// create note area

		noteArea = new JTextArea();

		noteAreaScroller = new JScrollPane(noteArea);
		noteAreaScroller.setPreferredSize(new Dimension(8, 8));
		noteAreaScroller.setBorder(BorderFactory.createEmptyBorder());
		noteAreaScroller.setVisible(false);


		// add all the created components to the view

		mainPanelRightOuter.add(mainPanelRight, new Arrangement(0, 0, 1.0, 1.0));
		mainPanelRightOuter.add(jumpToFilePanel, new Arrangement(0, 1, 1.0, 0.0));
		mainPanelRightOuter.add(searchPanel, new Arrangement(0, 2, 1.0, 0.0));

		mainPanel.add(augFileListScroller, new Arrangement(0, 0, 0.2, 1.0));
		mainPanel.add(augFileTreeScroller, new Arrangement(1, 0, 0.2, 1.0));

		mainPanel.add(mainPanelRightOuter, new Arrangement(2, 0, 1.0, 1.0));

		mainPanel.add(noteAreaScroller, new Arrangement(3, 0, 0.2, 1.0));

		parent.add(mainPanel, BorderLayout.CENTER);

		return mainPanel;
	}

	public void showJumpToFileBar() {

		jumpToTab = currentlyShownTab;

		jumpToFilePanel.setVisible(true);

		jumpToFileField.requestFocus();
	}

	public void showSearchBar() {

		searchPanel.setVisible(true);

		searchField.requestFocus();
	}

	public String getSearchFieldText() {
		return searchField.getText();
	}

	public boolean getShowFilesInTree() {
		return showFilesInTree;
	}

	public void showOpenFileDialog() {

		OpenFileDialog augFilePicker = new OpenFileDialog();

		// if we find nothing better, use the last-used directory
		String lastDirectory = augFileCtrl.getWorkspace().getString(CONFIG_KEY_LAST_DIRECTORY);

		// if we can though, use the directory of the currently selected tab :)
		if (currentlyShownTab != null) {
			lastDirectory = currentlyShownTab.getDirectoryName();
		}

		// if we are really totally awesome though, then we are using the tree right now...
		if (showFilesInTree) {
			// ... and can get the path of the last node ...
			Object lastNode = fileTreeComponent.getLastSelectedPathComponent();
			// which might even be a folder, in which case we want THAT rather than a particular tab!
			if (lastNode instanceof FileTreeNode) {
				FileTreeNode lastTreeNode = (FileTreeNode) lastNode;
				lastDirectory = lastTreeNode.getDirectoryName();
			}
		}

		if ((lastDirectory != null) && !"".equals(lastDirectory)) {
			augFilePicker.setCurrentDirectory(new Directory(lastDirectory));
		}

		augFilePicker.setDialogTitle("Open a Code File to Edit");
		augFilePicker.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		augFilePicker.setMultiSelectionEnabled(true);

		augFilePicker.showOpenDialog(new CallbackWithStatus() {
			public void call(int status) {
				switch (status) {

					case OpenFileDialog.APPROVE_OPTION:

						openFiles(
							augFilePicker.getSelectedFiles(),
							augFilePicker.getSelectedDirectories(),
							augFilePicker.getCurrentDirectory().getAbsoluteDirname()
						);

						break;

					case OpenFileDialog.CANCEL_OPTION:
						// cancel was pressed... do nothing for now
						break;
				}
			}
		});
	}

	public void openFiles(List<File> filesToOpen, List<Directory> foldersToOpen, String newLastAbsoluteDirPath) {

		// load the files
		augFileCtrl.getWorkspace().setString(CONFIG_KEY_LAST_DIRECTORY, newLastAbsoluteDirPath);
		configuration.create();

		AugFileTab latestTab = null;

		if (filesToOpen != null) {
			for (File curFile : filesToOpen) {
				latestTab = openFile(curFile);
			}
		}

		if (foldersToOpen != null) {
			for (Directory curFolder : foldersToOpen) {
				latestTab = openFilesRecursively(curFolder);
			}
		}

		// show the latest tab that we added!
		if (latestTab != null) {
			setCurrentlyShownTab(latestTab);
		}

		regenerateAugFileList();

		reEnableDisableMenuItems();
	}

	private AugFileTab openFile(File fileToOpen) {

		// if the file was already opened before...
		String newFilename = fileToOpen.getCanonicalFilename();
		for (AugFileTab tab : augFileTabs) {
			if (newFilename.equals(tab.getFilePath())) {
				// ... then load this existing tab!
				return tab;
			}
		}

		AugFile newFile = augFileCtrl.loadAnotherFile(fileToOpen);

		if (newFile != null) {
			// ... if not, add a tab for it
			AugFileTab result = new AugFileTab(mainPanelRight, newFile, this, augFileCtrl);
			result.setDefaultIndent(defaultIndentationStr);
			augFileTabs.add(result);
			return result;
		}

		// this... should not happen!
		return null;
	}

	private AugFileTab openFilesRecursively(Directory parent) {

		AugFileTab result = null;

		// when opening entire directories...
		boolean recursively = true;
		List<File> curFiles = parent.getAllFiles(recursively);

		for (File curFile : curFiles) {

			String localFilename = curFile.getLocalFilename();

			// ... ignore gedit backup files
			if (localFilename.endsWith("~")) {
				continue;
			}
			// ... ignore unity meta files
			if (localFilename.endsWith(".meta")) {
				continue;
			}
			// ... ignore java package info files
			if (localFilename.equals("package-info.java")) {
				continue;
			}
			// TODO :: maybe ignore files that are covered by .gitignore?
			// (as those are often also backup files or similar...)

			result = openFile(curFile);
		}

		return result;
	}

	public void loadFile(File fileToOpen) {

		AugFileTab loadedTab = null;

		AugFile newFile = augFileCtrl.loadAnotherFile(fileToOpen);

		// if the file was already opened before...
		if (newFile == null) {
			// ... then load this existing tab!
			String newFilename = fileToOpen.getCanonicalFilename();
			for (AugFileTab tab : augFileTabs) {
				if (newFilename.equals(tab.getFilePath())) {
					loadedTab = tab;
					break;
				}
			}
		} else {
			// ... if not, add a tab for it
			loadedTab = new AugFileTab(mainPanelRight, newFile, this, augFileCtrl);
			loadedTab.setDefaultIndent(defaultIndentationStr);
			augFileTabs.add(loadedTab);
		}

		if (loadedTab != null) {
			showTab(loadedTab, true);

			regenerateAugFileList();

			reEnableDisableMenuItems();
		}
	}

	public void newFile() {

		SimpleFile fileToOpen = new SimpleFile("data/new.txt");

		String curDir = getCurrentDirName();

		if (curDir != null) {
			fileToOpen = new SimpleFile(curDir + "/new.txt");
		}

		fileToOpen.create();

		loadFile(fileToOpen);
	}

	public void copyFiles(List<AugFileTab> tabs) {

		for (AugFileTab tab : tabs) {

			String curDir = tab.getFile().getParentDirectory().getAbsoluteDirname();

			if (curDir != null) {
				String newFileName = tab.getName();
				if (newFileName.contains(".")) {
					newFileName = newFileName.substring(0, newFileName.lastIndexOf(".")) + " (copy)" + newFileName.substring(newFileName.lastIndexOf("."));
				} else {
					newFileName = newFileName + " (copy)";
				}
				SimpleFile fileToOpen = new SimpleFile(curDir + "/" + newFileName);

				fileToOpen.setContent(tab.getContent());

				fileToOpen.create();

				loadFile(fileToOpen);
			}
		}
	}

	public void saveFiles(List<AugFileTab> tabs) {

		for (AugFileTab tab : tabs) {
			tab.save();
		}

		saveNotes();
	}

	public void saveNotes() {

		String noteText = noteArea.getText();

		if (!("".equals(noteText))) {
			noteAreaFile.saveContent(noteText);
		}
	}

	public void deleteFiles(List<AugFileTab> tabs) {

		deleteOrCloseFiles(tabs, true);
	}

	public void closeFiles(List<AugFileTab> tabs) {

		deleteOrCloseFiles(tabs, false);
	}

	private void deleteOrCloseFiles(List<AugFileTab> tabs, boolean delete) {

		for (AugFileTab tab : tabs) {

			while (listOfPreviousTabs.contains(tab)) {
				listOfPreviousTabs.remove(tab);
			}
			while (listOfFutureTabs.contains(tab)) {
				listOfFutureTabs.remove(tab);
			}

			augFileCtrl.removeFile(tab.getFile());

			if (delete) {
				tab.delete();
			} else {
				tab.remove();
			}

			augFileTabs.remove(tab);
		}

		setCurrentlyShownTab(null);

		regenerateAugFileList();
	}

	public void renameSelectedFile() {

		if (showFilesInTree) {

			TreePath[] selectedPaths = fileTreeComponent.getSelectionPaths();
			if (selectedPaths.length > 0) {
				FileTreeNode node = fileTreeModel.getChild(selectedPaths[0]);
				if (node instanceof FileTreeFile) {
					FileTreeFile file = (FileTreeFile) node;

					fileTreeComponent.startEditingAtPath(selectedPaths[0]);
				}
			}

		} else {

			List<AugFileTab> tabs = fileListComponent.getSelectedValuesList();
			if (tabs.size() > 0) {
				AugFileTab tab = tabs.get(0);

				// TODO :: show edit field at the location of this tab
			}
		}
	}

	public List<AugFileTab> getTabs() {
		return augFileTabs;
	}

	public AugFileTab getCurrentTab() {

		return currentlyShownTab;
	}

	public List<AugFileTab> getCurrentTabAsList() {

		List<AugFileTab> result = new ArrayList<>();

		result.add(currentlyShownTab);

		return result;
	}

	private List<FileTreeFolder> getHighlightedFolders() {

		List<FileTreeFolder> result = new ArrayList<>();

		if (showFilesInTree) {
			TreePath[] selectedPaths = fileTreeComponent.getSelectionPaths();

			for (TreePath path : selectedPaths) {
				FileTreeNode node = fileTreeModel.getChild(path);
				if (node instanceof FileTreeFolder) {
					result.add((FileTreeFolder) node);
				}
			}
		}

		return result;
	}

	public List<AugFileTab> getHighlightedTabs() {

		if (showFilesInTree) {

			TreePath[] selectedPaths = fileTreeComponent.getSelectionPaths();

			List<AugFileTab> result = new ArrayList<>();

			for (TreePath path : selectedPaths) {
				FileTreeNode node = fileTreeModel.getChild(path);
				if (node instanceof FileTreeFile) {
					FileTreeFile file = (FileTreeFile) node;
					FileTab tab = file.getTab();
					if (tab instanceof AugFileTab) {
						result.add((AugFileTab) tab);
					}
				}
			}

			return result;

		} else {

			return fileListComponent.getSelectedValuesList();
		}
	}

	private void updateHighlightersOnAllTabs() {

		for (AugFileTab augFileTab : augFileTabs) {
			augFileTab.updateHighlighterConfig();
		}
	}

	public void toggleShowFilesInTree() {

		showFilesInTree = !showFilesInTree;

		mainMenu.showFilesInTreeItem.setSelected(showFilesInTree);

		updateShowFilesInTree();

		configuration.set(CONFIG_KEY_SHOW_FILES_IN_TREE, showFilesInTree);
	}

	private void updateShowFilesInTree() {

		if (showFilesInTree == null) {
			showFilesInTree = true;
		}

		augFileListScroller.setVisible(showFiles && !showFilesInTree);

		augFileTreeScroller.setVisible(showFiles && showFilesInTree);

		mainFrame.pack();

		if (showFiles && (currentlyShownTab != null)) {
			highlightTabInLeftListOrTree(currentlyShownTab);
		}
	}

	public void toggleUseAntiAliasing() {

		useAntiAliasing = !useAntiAliasing;

		mainMenu.useAntiAliasingItem.setSelected(useAntiAliasing);

		updateUseAntiAliasing();

		configuration.set(CONFIG_KEY_ANTI_ALIASING, useAntiAliasing);
	}

	private void updateUseAntiAliasing() {

		if (useAntiAliasing) {
			// enable anti-aliasing for swing
			System.setProperty("swing.aatext", "true");
			// enable anti-aliasing for awt
			System.setProperty("awt.useSystemAAFontSettings", "on");
		} else {
			// disable anti-aliasing for swing
			System.clearProperty("swing.aatext");
			// disable anti-aliasing for awt
			System.clearProperty("awt.useSystemAAFontSettings");
		}
	}

	public Boolean getUseAntiAliasing() {
		return useAntiAliasing;
	}

	public void toggleSearchBar() {
		searchPanel.setVisible(!searchPanel.isVisible());
		mainFrame.pack();
	}

	public void toggleNoteArea() {
		noteAreaScroller.setVisible(!noteAreaScroller.isVisible());
		mainFrame.pack();
	}

	public void toggleFileArea() {

		showFiles = !showFiles;

		updateShowFilesInTree();
	}

	public void setOrUnsetCurrentCodeLanguage(CodeLanguage codeKind) {

		currentlyShownTab.setCodeLanguageAndCreateHighlighter(codeKind);

		mainMenu.reSelectCurrentCodeLanguageItem(currentlyShownTab.getSourceLanguage().toString());
	}

	public void setOrUnsetAllCodeLanguages(CodeLanguage codeKind) {

		for (AugFileTab augFileTab : augFileTabs) {
			augFileTab.setCodeLanguageAndCreateHighlighter(codeKind);
		}

		mainMenu.reSelectCurrentCodeLanguageItem(currentlyShownTab.getSourceLanguage().toString());
	}

	public String getScheme() {
		return currentScheme;
	}

	public void setScheme(String scheme) {

		currentScheme = scheme;

		mainMenu.reSelectSchemeItems();

		configuration.set(CONFIG_KEY_SCHEME, currentScheme);

		switch (currentScheme) {
			case GuiUtils.LIGHT_SCHEME:
				Code.setLightSchemeForAllEditors();
				noteArea.setForeground(Color.black);
				noteArea.setBackground(Color.white);
				searchField.setForeground(Color.black);
				searchField.setBackground(Color.white);
				jumpToFileField.setForeground(Color.black);
				jumpToFileField.setBackground(Color.white);
				replaceField.setForeground(Color.black);
				replaceField.setBackground(Color.white);
				fileListComponent.setForeground(Color.black);
				fileListComponent.setBackground(Color.white);
				GuiUtils.setCornerColor(augFileListScroller, JScrollPane.LOWER_RIGHT_CORNER, Color.white);
				GuiUtils.setCornerColor(augFileTreeScroller, JScrollPane.LOWER_RIGHT_CORNER, Color.white);
				break;
			case GuiUtils.DARK_SCHEME:
				Code.setDarkSchemeForAllEditors();
				noteArea.setForeground(Color.white);
				noteArea.setBackground(Color.black);
				searchField.setForeground(Color.white);
				searchField.setBackground(Color.black);
				jumpToFileField.setForeground(Color.white);
				jumpToFileField.setBackground(Color.black);
				replaceField.setForeground(Color.white);
				replaceField.setBackground(Color.black);
				fileListComponent.setForeground(Color.white);
				fileListComponent.setBackground(Color.black);
				GuiUtils.setCornerColor(augFileListScroller, JScrollPane.LOWER_RIGHT_CORNER, Color.black);
				GuiUtils.setCornerColor(augFileTreeScroller, JScrollPane.LOWER_RIGHT_CORNER, Color.black);
				break;
		}

		setScheme(scheme, augFileListScroller);
		setScheme(scheme, augFileTreeScroller);
		setScheme(scheme, noteAreaScroller);

		fileTreeComponent.setScheme(currentScheme);

		for (AugFileTab tab : augFileTabs) {
			tab.setComponentScheme(currentScheme);
		}
	}

	public static void setScheme(String scheme, JScrollPane scroller) {

		Color barColor = null;
		Color backgroundColor = null;
		Color backgroundHighlightColor = null;
		Color backgroundLightColor = null;

		switch (scheme) {
			case GuiUtils.LIGHT_SCHEME:
				barColor = new Color(150, 50, 235);
				backgroundColor = new Color(215, 185, 240);
				backgroundHighlightColor = new Color(200, 160, 220);
				backgroundLightColor = new Color(190, 145, 212);
				break;
			case GuiUtils.DARK_SCHEME:
				barColor = new Color(120, 45, 180);
				backgroundColor = new Color(19, 18, 25);
				backgroundHighlightColor = new Color(59, 48, 85);
				backgroundLightColor = new Color(29, 28, 45);
				break;
		}

		final Color finalBarColor = barColor;
		final Color finalBackgroundColor = backgroundColor;
		final Color finalBackgroundHighlightColor = backgroundHighlightColor;
		final Color finalBackgroundLightColor = backgroundLightColor;

		scroller.getVerticalScrollBar().setBackground(backgroundColor);
		scroller.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
			@Override
			protected JButton createDecreaseButton(int orientation) {
				JButton button = super.createDecreaseButton(orientation);
				button.setBackground(finalBackgroundColor);
				return button;
			}

			@Override
			protected JButton createIncreaseButton(int orientation) {
				JButton button = super.createIncreaseButton(orientation);
				button.setBackground(finalBackgroundColor);
				return button;
			}

			@Override
			protected void configureScrollBarColors() {
				this.thumbColor = finalBarColor;
				this.thumbDarkShadowColor = finalBackgroundColor;
				this.thumbHighlightColor = finalBackgroundHighlightColor;
				this.thumbLightShadowColor = finalBackgroundLightColor;
				this.minimumThumbSize = new Dimension(4, 36);
			}
		});
		scroller.getHorizontalScrollBar().setBackground(backgroundColor);
		scroller.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
			@Override
			protected JButton createDecreaseButton(int orientation) {
				JButton button = super.createDecreaseButton(orientation);
				button.setBackground(finalBackgroundColor);
				return button;
			}

			@Override
			protected JButton createIncreaseButton(int orientation) {
				JButton button = super.createIncreaseButton(orientation);
				button.setBackground(finalBackgroundColor);
				return button;
			}

			@Override
			protected void configureScrollBarColors() {
				this.thumbColor = finalBarColor;
				this.thumbDarkShadowColor = finalBackgroundColor;
				this.thumbHighlightColor = finalBackgroundHighlightColor;
				this.thumbLightShadowColor = finalBackgroundLightColor;
				this.minimumThumbSize = new Dimension(36, 4);
			}
		});
	}

	public boolean getRemoveTrailingWhitespaceOnSave() {
		return removeTrailingWhitespaceOnSave;
	}

	public void setRemoveTrailingWhitespaceOnSave(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		removeTrailingWhitespaceOnSave = setTo;

		Record activeWorkspace = augFileCtrl.getActiveWorkspace();
		if (activeWorkspace != null) {
			activeWorkspace.set(CONFIG_KEY_REMOVE_TRAILING_WHITESPACE_ON_SAVE, removeTrailingWhitespaceOnSave);
		}

		configuration.set(CONFIG_KEY_REMOVE_TRAILING_WHITESPACE_ON_SAVE, removeTrailingWhitespaceOnSave);

		mainMenu.removeTrailingWhitespaceOnSaveItem.setSelected(removeTrailingWhitespaceOnSave);
	}

	public boolean getReplaceWhitespacesWithTabsOnSave() {
		return replaceWhitespacesWithTabsOnSave;
	}

	public void setReplaceWhitespacesWithTabsOnSave(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		replaceWhitespacesWithTabsOnSave = setTo;

		Record activeWorkspace = augFileCtrl.getActiveWorkspace();
		if (activeWorkspace != null) {
			activeWorkspace.set(CONFIG_KEY_REPLACE_WHITESPACES_WITH_TABS_ON_SAVE, replaceWhitespacesWithTabsOnSave);
		}

		configuration.set(CONFIG_KEY_REPLACE_WHITESPACES_WITH_TABS_ON_SAVE, replaceWhitespacesWithTabsOnSave);

		mainMenu.replaceWhitespacesWithTabsOnSaveItem.setSelected(replaceWhitespacesWithTabsOnSave);

		if (setTo) {
			setReplaceTabsWithWhitespacesOnSave(false);
		}
	}

	public boolean getReplaceTabsWithWhitespacesOnSave() {
		return replaceTabsWithWhitespacesOnSave;
	}

	public void setReplaceTabsWithWhitespacesOnSave(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		replaceTabsWithWhitespacesOnSave = setTo;

		Record activeWorkspace = augFileCtrl.getActiveWorkspace();
		if (activeWorkspace != null) {
			activeWorkspace.set(CONFIG_KEY_REPLACE_TABS_WITH_WHITESPACES_ON_SAVE, replaceTabsWithWhitespacesOnSave);
		}

		configuration.set(CONFIG_KEY_REPLACE_TABS_WITH_WHITESPACES_ON_SAVE, replaceTabsWithWhitespacesOnSave);

		mainMenu.replaceTabsWithWhitespacesOnSaveItem.setSelected(replaceTabsWithWhitespacesOnSave);

		if (setTo) {
			setReplaceWhitespacesWithTabsOnSave(false);
		}
	}

	public boolean getAddMissingImportsOnSave() {
		return addMissingImportsOnSave;
	}

	public void setAddMissingImportsOnSave(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		addMissingImportsOnSave = setTo;

		Record activeWorkspace = augFileCtrl.getActiveWorkspace();
		if (activeWorkspace != null) {
			activeWorkspace.set(CONFIG_KEY_ADD_MISSING_IMPORTS_ON_SAVE, addMissingImportsOnSave);
		}

		configuration.set(CONFIG_KEY_ADD_MISSING_IMPORTS_ON_SAVE, addMissingImportsOnSave);

		mainMenu.addMissingImportsOnSaveItem.setSelected(addMissingImportsOnSave);
	}

	public boolean getReorganizeImportsOnSave() {
		return reorganizeImportsOnSave;
	}

	public void setReorganizeImportsOnSave(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		reorganizeImportsOnSave = setTo;

		Record activeWorkspace = augFileCtrl.getActiveWorkspace();
		if (activeWorkspace != null) {
			activeWorkspace.set(CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE, reorganizeImportsOnSave);
		}

		configuration.set(CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE, reorganizeImportsOnSave);

		mainMenu.reorganizeImportsOnSaveItem.setSelected(reorganizeImportsOnSave);

		if (reorganizeImportsOnSave) {
			setReorganizeImportsOnSaveCompatible(false);
		}
	}

	public boolean getReorganizeImportsOnSaveCompatible() {
		return reorganizeImportsOnSaveCompatible;
	}

	public void setReorganizeImportsOnSaveCompatible(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		reorganizeImportsOnSaveCompatible = setTo;

		Record activeWorkspace = augFileCtrl.getActiveWorkspace();
		if (activeWorkspace != null) {
			activeWorkspace.set(CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE_COMPATIBLE, reorganizeImportsOnSaveCompatible);
		}

		configuration.set(CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE_COMPATIBLE, reorganizeImportsOnSaveCompatible);

		mainMenu.reorganizeImportsOnSaveCompatibleItem.setSelected(reorganizeImportsOnSaveCompatible);

		if (reorganizeImportsOnSaveCompatible) {
			setReorganizeImportsOnSave(false);
		}
	}

	public boolean getRemoveUnusedImportsOnSave() {
		return removeUnusedImportsOnSave;
	}

	public void setRemoveUnusedImportsOnSave(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		removeUnusedImportsOnSave = setTo;

		Record activeWorkspace = augFileCtrl.getActiveWorkspace();
		if (activeWorkspace != null) {
			activeWorkspace.set(CONFIG_KEY_REMOVE_UNUSED_IMPORTS_ON_SAVE, removeUnusedImportsOnSave);
		}

		configuration.set(CONFIG_KEY_REMOVE_UNUSED_IMPORTS_ON_SAVE, removeUnusedImportsOnSave);

		mainMenu.removeUnusedImportsOnSaveItem.setSelected(setTo);
	}

	public String getDefaultIndent() {
		return defaultIndentationStr;
	}

	public void setDefaultIndent(String indentationStr) {

		if (indentationStr == null) {
			indentationStr = "\t";
		}

		this.defaultIndentationStr = indentationStr;

		Record activeWorkspace = augFileCtrl.getActiveWorkspace();
		if (activeWorkspace != null) {
			activeWorkspace.set(CONFIG_KEY_DEFAULT_INDENTATION_STR, defaultIndentationStr);
		}

		configuration.set(CONFIG_KEY_DEFAULT_INDENTATION_STR, defaultIndentationStr);

		mainMenu.defaultIndent2Spaces.setSelected(false);
		mainMenu.defaultIndent4Spaces.setSelected(false);
		mainMenu.defaultIndentTab.setSelected(false);

		switch (defaultIndentationStr) {
			case "  ":
				mainMenu.defaultIndent2Spaces.setSelected(true);
				break;
			case "    ":
				mainMenu.defaultIndent4Spaces.setSelected(true);
				break;
			default:
				mainMenu.defaultIndentTab.setSelected(true);
				break;
		}

		for (AugFileTab tab : augFileTabs) {
			tab.setDefaultIndent(defaultIndentationStr);
		}
	}

	private void setCopyOnEnter(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		copyOnEnter = setTo;

		configuration.set(CONFIG_KEY_COPY_ON_ENTER, copyOnEnter);

		mainMenu.copyOnEnterItem.setSelected(copyOnEnter);

		updateHighlightersOnAllTabs();
	}

	public boolean getTabEntireBlocks() {
		return tabEntireBlocks;
	}

	public void setTabEntireBlocks(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		tabEntireBlocks = setTo;

		configuration.set(CONFIG_KEY_TAB_ENTIRE_BLOCKS, tabEntireBlocks);

		mainMenu.tabEntireBlocksItem.setSelected(tabEntireBlocks);

		updateHighlightersOnAllTabs();
	}

	private void showSelectedTab() {

		List<AugFileTab> selectedTabs = getHighlightedTabs();

		if (selectedTabs.size() > 0) {
			showTab(selectedTabs.get(0), false);
		}
	}

	public void setFontSize(int newSize) {

		fontSize = newSize;

		Code.setFontSizeForAllEditors(fontSize);

		configuration.set(CONFIG_KEY_FONT_SIZE, fontSize);
	}

	public int getFontSize() {
		return fontSize;
	}

	/**
	 * Show a tab and optionally also highlight it
	 * (do NOT highlight it if it is already highlighted, such that
	 * other selected files do not lose their selection - but DO
	 * highlight it if it is not yet, that is, when this is initiated
	 * by the backend!)
	 */
	public void showTab(AugFileTab tabToShow, boolean highlightTab) {

		// only add the current tab to the list of previous ones...
		if (currentlyShownTab != null) {
			// ... if the current tab is not also the new one ...
			if (!currentlyShownTab.equals(tabToShow)) {
				// ... and if that list is empty ...
				if (listOfPreviousTabs.size() > 0) {
					// ... or has a different tab at its end (such that the same one does not get added twice in a row)
					if (!currentlyShownTab.equals(listOfPreviousTabs.get(listOfPreviousTabs.size() - 1))) {
						listOfPreviousTabs.add(currentlyShownTab);
					}
				} else {
					listOfPreviousTabs.add(currentlyShownTab);
				}
			}
		}

		listOfFutureTabs = new ArrayList<>();

		for (AugFileTab tab : augFileTabs) {
			tab.setSelectionOrder(0);
		}
		for (int i = listOfPreviousTabs.size() - FileTreeCellRenderer.PREV_SELECTED_TAB_AMOUNT; i < listOfPreviousTabs.size(); i++) {
			if (i < 0) {
				continue;
			}
			int selOrder = i - (listOfPreviousTabs.size() - FileTreeCellRenderer.PREV_SELECTED_TAB_AMOUNT);
			listOfPreviousTabs.get(i).setSelectionOrder(selOrder);
		}

		showTabInternal(tabToShow, highlightTab);

		if (showFilesInTree) {
			fileTreeComponent.repaint();
		}

		// we want to save in the config that the new tab is now shown
		augFileCtrl.saveConfigFileList();
	}

	public void goToPreviousTab() {

		int listSize = listOfPreviousTabs.size();

		if (listSize > 0) {
			AugFileTab tabToShow = listOfPreviousTabs.get(listSize - 1);
			listOfPreviousTabs.remove(listSize - 1);
			listOfFutureTabs.add(currentlyShownTab);

			showTabInternal(tabToShow, true);
		}
	}

	public void goToNextTab() {

		int listSize = listOfFutureTabs.size();

		if (listSize > 0) {
			AugFileTab tabToShow = listOfFutureTabs.get(listSize - 1);
			listOfFutureTabs.remove(listSize - 1);
			listOfPreviousTabs.add(currentlyShownTab);

			showTabInternal(tabToShow, true);
		}
	}

	private void showTabInternal(AugFileTab tabToShow, boolean highlightTab) {

		for (AugFileTab tab : augFileTabs) {
			tab.hide();
		}

		tabToShow.show();
		setCurrentlyShownTab(tabToShow);
		mainMenu.reSelectCurrentCodeLanguageItem(currentlyShownTab.getSourceLanguage().toString());

		if (highlightTab) {
			highlightTabInLeftListOrTree(currentlyShownTab);
		}

		tabToShow.showGoBack(listOfPreviousTabs.size() > 0);
		tabToShow.showGoForward(listOfFutureTabs.size() > 0);
	}

	private void setCurrentlyShownTab(AugFileTab tab) {

		currentlyShownTab = tab;

		if (tab == null) {
			return;
		}

		mainMenu.setEncoding(tab.getEncoding());

		tab.getFile().setLastAccessTime(new Date());
	}

	private void configureGUI() {

		/*
		Integer configFontSize = configuration.getInteger(CONFIG_KEY_EDITOR_FONT_SIZE);

		if ((configFontSize != null) && (configFontSize > 0)) {
			currentFontSize = configFontSize;
		}

		GroovyCode.setFontSize(currentFontSize);
		*/
	}

	private String sanitizeName(String name) {

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < name.length(); i++) {
			char curChar = name.charAt(i);
			if (Character.isLetter(curChar) || Character.isDigit(curChar)) {
				int isCurCharAscii = name.charAt(i);
				if (isCurCharAscii < 0x80) {
					result.append(curChar);
				}
			}
		}

		if (result.length() > 0) {
			return result.toString();
		}
		return "nameless";
	}

	public void regenerateAugFileListWithoutShowingAnyTabs() {

		// regenerate the file tree
		fileTreeModel.regenerate(augFileTabs);

		// fully expand the file tree
		for (int i = 0; i < fileTreeComponent.getRowCount(); i++) {
			fileTreeComponent.expandRow(i);
		}

		Collections.sort(augFileTabs, new Comparator<AugFileTab>() {
			public int compare(AugFileTab a, AugFileTab b) {
				// TODO :: make it configurable whether to sort by just the name or by
				// the full name (including the path)!
				// return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
				return a.getFilePath().toLowerCase().compareTo(b.getFilePath().toLowerCase());
			}
		});

		augFileTabArray = new AugFileTab[augFileTabs.size()];

		int i = 0;

		for (AugFileTab augFileTab : augFileTabs) {
			augFileTabArray[i] = augFileTab;
			i++;
		}

		fileListComponent.setListData(augFileTabArray);
	}

	/**
	 * Regenerate the file list on the left hand side based on the augFileTabs list,
	 * and (if at least one file exists), select and open the current tab or, if it
	 * is null, the lastly added one
	 */
	public void regenerateAugFileList() {

		// we want to be able to temporarily disable this, if we know that we will call it often in a tight loop
		if (!regenerateAugFileListEnabled) {
			return;
		}

		// if there is no last shown tab...
		if (currentlyShownTab == null) {
			// ... show some random tab explicitly - this is fun, and the tabbed layout otherwise shows it anyway, so may as well...
			if (augFileTabs.size() > 0) {
				AugFileTab latestTab = augFileTabs.get(0);
				Date latestAccessTime = latestTab.getFile().getLastAccessTime();
				for (int i = 1; i < augFileTabs.size(); i++) {
					Date curAccessTime = augFileTabs.get(i).getFile().getLastAccessTime();
					if ((curAccessTime != null) && (curAccessTime.compareTo(latestAccessTime) > 0)) {
						latestTab = augFileTabs.get(i);
						latestAccessTime = curAccessTime;
					}
				}
				setCurrentlyShownTab(latestTab);
			}
		}

		regenerateAugFileListWithoutShowingAnyTabs();

		// if there still is no last shown tab (e.g. we just deleted the very last one)...
		if (currentlyShownTab == null) {
			// ... then we do not need to show or highlight any ;)
			return;
		}

		// show the last shown tab
		showTab(currentlyShownTab, true);
	}

	public void disableRegenerateAugFileList() {

		regenerateAugFileListEnabled = false;
	}

	public void reenableRegenerateAugFileList() {

		regenerateAugFileListEnabled = true;

		regenerateAugFileList();
	}

	public void highlightTabInLeftListOrTree(final AugFileTab tab) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// highlight tab the list
				int i = 0;
				for (AugFileTab augFileTab : augFileTabs) {
					if (tab.equals(augFileTab)) {
						fileListComponent.setSelectedIndex(i);
						break;
					}
					i++;
				}

				// highlight tab the tree
				Object[] paths = fileTreeModel.getPathToRoot(tab);
				if (paths.length > 0) {
					TreePath curPath = new TreePath(fileTreeModel.getPathToRoot(tab));
					fileTreeComponent.setSelectionPath(curPath);
					fileTreeComponent.scrollPathToVisible(curPath);
				}

				// jump back to the actual tab
				tab.setFocus();
			}
		});
	}

	/**
	 * Enable and disable menu items related to the current state of the application,
	 * e.g. if no CDM is loaded at all, do not enable the user to add augFiles to the
	 * current CDM, etc.
	 */
	private void reEnableDisableMenuItems() {

		boolean augFilesExist = augFileTabs.size() > 0;

		boolean fileIsSelected = currentlyShownTab != null;

		mainMenu.reEnableDisableMenuItems(augFilesExist, fileIsSelected);
		mainPopupMenu.reEnableDisableMenuItems(augFilesExist, fileIsSelected);
	}

	private void refreshTitleBar() {

		mainFrame.setTitle(AssEditor.PROGRAM_TITLE + " - " + augFileCtrl.getWorkspaceName() + " ");
	}

	public void reloadAllAugFileTabs() {

		if (augFileTabs != null) {
			for (AugFileTab augFileTab : augFileTabs) {
				augFileTab.remove();
			}
		}

		// update the file list on the left and load the new file tabs
		augFileTabs = new ArrayList<>();

		List<AugFile> files = augFileCtrl.getFiles();
		for (AugFile file : files) {
			file.refreshContent();
			AugFileTab newTab = new AugFileTab(mainPanelRight, file, this, augFileCtrl);
			newTab.setDefaultIndent(defaultIndentationStr);
			augFileTabs.add(newTab);
		}

		regenerateAugFileList();

		reEnableDisableMenuItems();

		// refreshTitleBar();
	}

	/**
	 * Check if currently augFiles are loaded, and if so then if files have been changed,
	 * and if yes ask the user if we want to save first, proceed, or cancel
	 * return true if we saved or proceed anyway, and false if we cancel
	 */
	 /*
	private void ifAllowedToLeaveCurrentDirectory(final Callback proceedWithThisIfAllowed) {

		// check all augFiles; if any have been changed, ask first before closing!
		boolean noneHaveBeenChanged = true;

		for (AugFileTab augFileTab : augFileTabs) {
			if (augFileTab.hasBeenChanged()) {
				noneHaveBeenChanged = false;
				break;
			}
		}

		// if none have been changed, then we are allowed to proceed in any case :)
		if (noneHaveBeenChanged) {
			proceedWithThisIfAllowed.call();
			return;
		}

		// okay, something has been changed, so we now want to ask the user about what to do...

		// Create the window
		final JDialog whatToDoDialog = new JDialog(mainFrame, "What to do?", true);
		GridLayout whatToDoDialogLayout = new GridLayout(2, 1);
		whatToDoDialogLayout.setVgap(8);
		whatToDoDialog.setLayout(whatToDoDialogLayout);
		whatToDoDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("The currently loaded augFiles have been modified - what do you want to do?");
		whatToDoDialog.add(explanationLabel);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 3);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		whatToDoDialog.add(buttonRow);

		JButton saveButton = new JButton("Save, then Proceed");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAugFiles();
				whatToDoDialog.dispose();
				proceedWithThisIfAllowed.call();
			}
		});
		buttonRow.add(saveButton);

		JButton proceedButton = new JButton("Proceed without Saving");
		proceedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				whatToDoDialog.dispose();
				proceedWithThisIfAllowed.call();
			}
		});
		buttonRow.add(proceedButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				whatToDoDialog.dispose();
			}
		});
		buttonRow.add(cancelButton);

		// Set the preferred size of the dialog
		int width = 600;
		int height = 120;
		whatToDoDialog.setSize(width, height);
		whatToDoDialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(whatToDoDialog);
	}
	*/

	public void switchToWorkspace(String workspaceName) {

		mainMenu.uncheckWorkspaces();

		List<JCheckBoxMenuItem> workspaceItems = mainMenu.getWorkspaces();
		for (JCheckBoxMenuItem workspaceItem : workspaceItems) {
			workspaceItem.setSelected(workspaceName.equals(workspaceItem.getText()));
		}

		// set the current tab to null such that we automagically open the latest one of the newly opened workspace
		setCurrentlyShownTab(null);

		// clear the back- and forward-lists when switching workspaces, as the old tabs are now invalid jump targets anyway
		listOfPreviousTabs = new ArrayList<>();
		listOfFutureTabs = new ArrayList<>();

		augFileCtrl.switchToWorkspace(workspaceName);

		reloadAllAugFileTabs();

		Record activeWorkspace = augFileCtrl.getActiveWorkspace();

		setRemoveTrailingWhitespaceOnSave(activeWorkspace.getBoolean(CONFIG_KEY_REMOVE_TRAILING_WHITESPACE_ON_SAVE, removeTrailingWhitespaceOnSave));
		setReplaceWhitespacesWithTabsOnSave(activeWorkspace.getBoolean(CONFIG_KEY_REPLACE_WHITESPACES_WITH_TABS_ON_SAVE, replaceWhitespacesWithTabsOnSave));
		setReplaceTabsWithWhitespacesOnSave(activeWorkspace.getBoolean(CONFIG_KEY_REPLACE_TABS_WITH_WHITESPACES_ON_SAVE, replaceTabsWithWhitespacesOnSave));
		setAddMissingImportsOnSave(activeWorkspace.getBoolean(CONFIG_KEY_ADD_MISSING_IMPORTS_ON_SAVE, addMissingImportsOnSave));
		setReorganizeImportsOnSave(activeWorkspace.getBoolean(CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE, reorganizeImportsOnSave));
		setReorganizeImportsOnSaveCompatible(activeWorkspace.getBoolean(CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE_COMPATIBLE, reorganizeImportsOnSaveCompatible));
		setRemoveUnusedImportsOnSave(activeWorkspace.getBoolean(CONFIG_KEY_REMOVE_UNUSED_IMPORTS_ON_SAVE, removeUnusedImportsOnSave));

		refreshTitleBar();
	}

	public void refreshWorkspaces() {
		getMainMenu().refreshWorkspaces();
		getMainPopupMenu().refreshWorkspaces();
	}

	private String getCurrentDirName() {

		// first of all, try to return a folder that we right-clicked on...
		List<FileTreeFolder> folders = getHighlightedFolders();

		if (folders.size() > 0) {

			return folders.get(0).getDirectoryName();

		} else {

			// ... or, if there is none, return an open file's parent directory
			List<AugFileTab> tabs = getHighlightedTabs();

			if (tabs.size() > 0) {
				return tabs.get(0).getFile().getParentDirectory().getAbsoluteDirname();
			}
		}

		return null;
	}

	public void openHighlightedFolder() {

		String curDir = getCurrentDirName();

		if (curDir != null) {
			GuiUtils.openFolder(curDir);
		}
	}

	public AugFileCtrl getAugFileCtrl() {
		return augFileCtrl;
	}

	public MainMenu getMainMenu() {
		return mainMenu;
	}

	public MainPopupMenu getMainPopupMenu() {
		return mainPopupMenu;
	}

}
