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
import com.asofterspace.toolbox.gui.FileTreeFile;
import com.asofterspace.toolbox.gui.FileTreeFolder;
import com.asofterspace.toolbox.gui.FileTreeModel;
import com.asofterspace.toolbox.gui.FileTreeNode;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.gui.MainWindow;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.Record;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.utils.Callback;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
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

	private AugFileTab currentlyShownTab;

	private final static String CONFIG_KEY_LAST_DIRECTORY = "lastDirectory";
	private final static String CONFIG_KEY_SCHEME = "scheme";
	private final static String CONFIG_KEY_REMOVE_TRAILING_WHITESPACE_ON_SAVE = "onSaveRemoveTrailingWhitespace";
	private final static String CONFIG_KEY_REPLACE_WHITESPACES_WITH_TABS_ON_SAVE = "onSaveReplaceWhitespacesWithTabs";
	private final static String CONFIG_KEY_REPLACE_TABS_WITH_WHITESPACES_ON_SAVE = "onSaveReplaceTabsWithWhitespaces";
	private final static String CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE = "onSaveReorganizeImports";
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

	String currentScheme;
	Boolean removeTrailingWhitespaceOnSave;
	Boolean replaceWhitespacesWithTabsOnSave;
	Boolean replaceTabsWithWhitespacesOnSave;
	Boolean reorganizeImportsOnSave;
	Boolean removeUnusedImportsOnSave;
	Boolean copyOnEnter;
	Boolean tabEntireBlocks;
	Boolean showFilesInTree;


	public MainGUI(AugFileCtrl augFileCtrl, ConfigFile config) {

		this.augFileCtrl = augFileCtrl;

		this.configuration = config;

		augFileTabArray = new AugFileTab[0];
		fileTreeModel = new FileTreeModel();
		fileTreeModel.addTreeModelListener(new TreeModelListener() {
			public void treeNodesChanged(TreeModelEvent e) {
				FileTreeNode node = fileTreeModel.getChild(e.getTreePath());
				if (node instanceof FileTreeFile) {
					FileTreeFile file = (FileTreeFile) node;
					FileTab fileTab = file.getTab();
					if (fileTab instanceof AugFileTab) {
						AugFileTab tab = (AugFileTab) fileTab;

						String oldName = tab.getName();
						String newName = fileTreeEditField.getText().trim();

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
							AugFileTab latestTab = openFilesRecursively(renameFile.getJavaFile());
							if (latestTab == null) {
								// if something went wrong, try to re-open the old file (it might still be there)
								File oldFile = new File(origFilePath);
								latestTab = openFilesRecursively(oldFile.getJavaFile());
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

		currentScheme = configuration.getValue(CONFIG_KEY_SCHEME);

		if (currentScheme == null) {
			currentScheme = GuiUtils.DARK_SCHEME;
		}

		removeTrailingWhitespaceOnSave = configuration.getBoolean(CONFIG_KEY_REMOVE_TRAILING_WHITESPACE_ON_SAVE, true);

		replaceWhitespacesWithTabsOnSave = configuration.getBoolean(CONFIG_KEY_REPLACE_WHITESPACES_WITH_TABS_ON_SAVE, true);

		replaceTabsWithWhitespacesOnSave = configuration.getBoolean(CONFIG_KEY_REPLACE_TABS_WITH_WHITESPACES_ON_SAVE, false);

		reorganizeImportsOnSave = configuration.getBoolean(CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE, true);

		removeUnusedImportsOnSave = configuration.getBoolean(CONFIG_KEY_REMOVE_UNUSED_IMPORTS_ON_SAVE, true);

		copyOnEnter = configuration.getBoolean(CONFIG_KEY_COPY_ON_ENTER, true);

		tabEntireBlocks = configuration.getBoolean(CONFIG_KEY_TAB_ENTIRE_BLOCKS, true);

		currentBackup = configuration.getInteger(CONFIG_KEY_BACKUP_NUM, 0);

		fontSize = configuration.getInteger(CONFIG_KEY_FONT_SIZE, DEFAULT_FONT_SIZE);

		showFilesInTree = configuration.getBoolean(CONFIG_KEY_SHOW_FILES_IN_TREE, true);

		Thread backupThread = new Thread(new Runnable() {
			@Override
			public void run() {
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

						configuration.set(CONFIG_KEY_BACKUP_NUM, currentBackup);
					}

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
		this.mainMenu = new MainMenu(this, mainFrame, augFileCtrl);
		this.mainPopupMenu = new MainPopupMenu(this, mainFrame);

		mainMenu.createMenu();
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

		fileListComponent.addKeyListener(new KeyListener() {

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
		});

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
						showTab((AugFileTab) tab);
					}
				}
			}
		});

		augFileListScroller = new JScrollPane(fileListComponent);
		augFileListScroller.setPreferredSize(new Dimension(8, 8));
		augFileListScroller.setBorder(BorderFactory.createEmptyBorder());

		augFileTreeScroller = new JScrollPane(fileTreeComponent);
		augFileTreeScroller.setPreferredSize(new Dimension(8, 8));
		augFileTreeScroller.setBorder(BorderFactory.createEmptyBorder());

		updateShowFilesInTree();

		searchPanel = new JPanel();
		searchPanel.setLayout(new GridBagLayout());
		searchPanel.setVisible(false);

		searchField = new JTextField();

		// listen to text updates
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				search();
			}
			public void removeUpdate(DocumentEvent e) {
				search();
			}
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
			public void actionPerformed(ActionEvent e) {
				String searchFor = searchField.getText();

				if (currentlyShownTab != null) {
					currentlyShownTab.search(searchFor);
				}
			}
		});

		replaceField = new JTextField();

		// listen to the enter key being pressed (which does not create text updates)
		replaceField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String searchFor = searchField.getText();
				String replaceWith = replaceField.getText();

				if (currentlyShownTab != null) {
					currentlyShownTab.replaceAll(searchFor, replaceWith);
				}
			}
		});

		noteArea = new JTextArea();

		noteAreaScroller = new JScrollPane(noteArea);
		noteAreaScroller.setPreferredSize(new Dimension(8, 8));
		noteAreaScroller.setBorder(BorderFactory.createEmptyBorder());
		noteAreaScroller.setVisible(false);

		searchPanel.add(searchField, new Arrangement(0, 0, 1.0, 1.0));
		searchPanel.add(replaceField, new Arrangement(0, 1, 1.0, 1.0));

		mainPanelRightOuter.add(mainPanelRight, new Arrangement(0, 0, 1.0, 1.0));
		mainPanelRightOuter.add(searchPanel, new Arrangement(0, 1, 1.0, 0.0));

		mainPanel.add(augFileListScroller, new Arrangement(0, 0, 0.2, 1.0));
		mainPanel.add(augFileTreeScroller, new Arrangement(1, 0, 0.2, 1.0));

		mainPanel.add(mainPanelRightOuter, new Arrangement(2, 0, 1.0, 1.0));

		mainPanel.add(noteAreaScroller, new Arrangement(3, 0, 0.2, 1.0));

		parent.add(mainPanel, BorderLayout.CENTER);

		return mainPanel;
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

	public void openFile() {

		// TODO :: de-localize the JFileChooser (by default it seems localized, which is inconsistent when the rest of the program is in English...)
		// (while you're at it, make Öffnen into Save for the save dialog, but keep it as Open for the open dialog... ^^)
		// TODO :: actually, write our own file chooser
		JFileChooser augFilePicker;

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
			augFilePicker = new JFileChooser(new java.io.File(lastDirectory));
		} else {
			augFilePicker = new JFileChooser();
		}

		augFilePicker.setDialogTitle("Open a Code File to Edit");
		augFilePicker.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		augFilePicker.setMultiSelectionEnabled(true);

		int result = augFilePicker.showOpenDialog(mainFrame);

		switch (result) {

			case JFileChooser.APPROVE_OPTION:

				// load the files
				augFileCtrl.getWorkspace().setString(CONFIG_KEY_LAST_DIRECTORY, augFilePicker.getCurrentDirectory().getAbsolutePath());
				configuration.create();

				AugFileTab latestTab = null;

				for (java.io.File curFile : augFilePicker.getSelectedFiles()) {
					latestTab = openFilesRecursively(curFile);
				}

				// show the latest tab that we added!
				if (latestTab != null) {
					setCurrentlyShownTab(latestTab);
				}

				regenerateAugFileList();

				reEnableDisableMenuItems();

				break;

			case JFileChooser.CANCEL_OPTION:
				// cancel was pressed... do nothing for now
				break;
		}
	}

	private AugFileTab openFilesRecursively(java.io.File parent) {

		AugFileTab result = null;

		if (parent.isDirectory()) {
			// when opening entire directories...
			java.io.File[] curFiles = parent.listFiles();

			for (java.io.File curFile : curFiles) {
				if (!curFile.isDirectory()) {
					// ... ignore gedit backup files
					if (curFile.getName().endsWith("~")) {
						continue;
					}
					// ... ignore unity meta files
					if (curFile.getName().endsWith(".meta")) {
						continue;
					}
					// ... ignore java package info files
					if (curFile.getName().equals("package-info.java")) {
						continue;
					}
					// TODO :: maybe ignore files that are covered by .gitignore?
					// (as those are often also backup files or similar...)
				}
				result = openFilesRecursively(curFile);
			}
		} else {
			File fileToOpen = new File(parent);

			AugFile newFile = augFileCtrl.loadAnotherFile(fileToOpen);

			// if the file was already opened before...
			if (newFile == null) {
				// ... then load this existing tab!
				String newFilename = fileToOpen.getCanonicalFilename();
				for (AugFileTab tab : augFileTabs) {
					if (newFilename.equals(tab.getFilePath())) {
						return tab;
					}
				}
			} else {
				// ... if not, add a tab for it
				result = new AugFileTab(mainPanelRight, newFile, this, augFileCtrl);
				augFileTabs.add(result);
			}
		}

		return result;
	}

	public void newFile() {

		SimpleFile fileToOpen = new SimpleFile("data/new.txt");

		String curDir = getCurrentDirName();

		if (curDir != null) {
			fileToOpen = new SimpleFile(curDir + "/new.txt");
		}

		fileToOpen.create();

		AugFile newFile = augFileCtrl.loadAnotherFile(fileToOpen);

		if (newFile != null) {
			augFileTabs.add(new AugFileTab(mainPanelRight, newFile, this, augFileCtrl));

			regenerateAugFileList();

			reEnableDisableMenuItems();
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

		for (AugFileTab tab : tabs) {

			augFileCtrl.removeFile(tab.getFile());

			tab.delete();

			augFileTabs.remove(tab);
		}

		setCurrentlyShownTab(null);

		regenerateAugFileList();
	}

	public void closeFiles(List<AugFileTab> tabs) {

		for (AugFileTab tab : tabs) {

			augFileCtrl.removeFile(tab.getFile());

			tab.remove();

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

		augFileListScroller.setVisible(!showFilesInTree);

		augFileTreeScroller.setVisible(showFilesInTree);

		mainFrame.pack();

		if (currentlyShownTab != null) {
			highlightTabInLeftListOrTree(currentlyShownTab);
		}
	}

	public void toggleSearchBar() {
		searchPanel.setVisible(!searchPanel.isVisible());
		mainFrame.pack();
	}

	public void toggleNoteArea() {
		noteAreaScroller.setVisible(!noteAreaScroller.isVisible());
		mainFrame.pack();
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

		mainMenu.setRemoveUnusedImportsOnSave(setTo);
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

		Integer selectedItem = fileListComponent.getSelectedIndex();

		if (selectedItem == null) {
			return;
		}

		int i = 0;

		for (AugFileTab tab : augFileTabs) {
			if (i == selectedItem) {
				showTab(tab);
				return;
			}
			i++;
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

	public void showTab(AugFileTab tabToShow) {

		for (AugFileTab tab : augFileTabs) {
			tab.hide();
		}

		tabToShow.show();
		setCurrentlyShownTab(tabToShow);
		mainMenu.reSelectCurrentCodeLanguageItem(currentlyShownTab.getSourceLanguage().toString());
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

	/*
	private void saveAugFilesAs() {

		// open a save dialog in which a directory can be picked
		JFileChooser saveCdmPicker;

		String lastDirectory = configuration.getValue(CONFIG_KEY_LAST_DIRECTORY);

		if ((lastDirectory != null) && !"".equals(lastDirectory)) {
			saveCdmPicker = new JFileChooser(new java.io.File(lastDirectory));
		} else {
			saveCdmPicker = new JFileChooser();
		}

		saveCdmPicker.setDialogTitle("Select the new CDM working directory");
		saveCdmPicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = saveCdmPicker.showOpenDialog(mainFrame);

		switch (result) {

			case JFileChooser.APPROVE_OPTION:

				configuration.set(CONFIG_KEY_LAST_DIRECTORY, saveCdmPicker.getCurrentDirectory().getAbsolutePath());
				Directory cdmDir = new Directory(saveCdmPicker.getSelectedFile());

				// if the new directory does not yet exist, then we have to create it...
				if (!cdmDir.exists()) {
					cdmDir.create();
				}

				// complain if the directory is not empty
				Boolean isEmpty = cdmDir.isEmpty();
				if ((isEmpty == null) || !isEmpty) {
					JOptionPane.showMessageDialog(mainFrame, "The specified directory is not empty - please save into an empty directory!", "Directory Not Empty", JOptionPane.ERROR_MESSAGE);
					saveCdmAs();
					return;
				}

				prepareToSave();

				// for all currently opened CDM files, save them relative to the new directory as they were in the previous one
				augFileCtrl.saveTo(cdmDir);

				// also copy over the Manifest file
				// TODO

				for (AugFileTab augFileTab : augFileTabs) {
					augFileTab.invalidateInfo();
				}

				// refreshTitleBar();

				JOptionPane.showMessageDialog(mainFrame, "The currently opened CDM files have been saved!", "CDM Saved", JOptionPane.INFORMATION_MESSAGE);

				break;

			case JFileChooser.CANCEL_OPTION:
				// cancel was pressed... do nothing for now
				break;
		}
	}

	private void openAddNewPersonDialog() {
		openAddNewAugFileDialog(AugFileKind.PERSON);
	}

	private void openAddNewCompanyDialog() {
		openAddNewAugFileDialog(AugFileKind.COMPANY);
	}

	private void openAddNewAugFileDialog(AugFileKind kind) {

		// open a dialog in which the name of the new file can be entered

		// Create the window
		final JDialog addDialog = new JDialog(mainFrame, "Add " + kind, true);
		GridLayout addDialogLayout = new GridLayout(3, 1);
		if (AugFileKind.PERSON.equals(kind)) {
			addDialogLayout = new GridLayout(5, 1);
		}
		addDialogLayout.setVgap(8);
		addDialog.setLayout(addDialogLayout);
		addDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Please enter the name of the new " + kind.toLowerCase() + ":");
		addDialog.add(explanationLabel);

		final JTextField newAugFileName = new JTextField();
		if (kind == AugFileKind.PERSON) {
			newAugFileName.setText("Someone Someonesson");
		} else {
			newAugFileName.setText("Some Company Ltd.");
		}
		addDialog.add(newAugFileName);

		// for a new user, allow selecting a company immediately - so that we know where to save that person!
		JLabel explanationLabelCompany = new JLabel();
		explanationLabelCompany.setText("Please enter the company that this person works for:");

		List<Company> companies = new ArrayList<>(augFileCtrl.getCompanies());

		Collections.sort(companies, new Comparator<Company>() {
			public int compare(Company a, Company b) {
				return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
			}
		});

		final String[] companiesArr = new String[companies.size()];
		int i = 0;
		for (Company company : companies) {
			companiesArr[i] = company.getName();
			i++;
		}

		final JComboBox<String> newCompany = new JComboBox<>(companiesArr);
		if (i > 0) {
			newCompany.setSelectedIndex(0);
		}
		newCompany.setEditable(false);

		if (AugFileKind.PERSON.equals(kind)) {
			addDialog.add(explanationLabelCompany);
			addDialog.add(newCompany);
		}

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		addDialog.add(buttonRow);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Company belongsToCompany = null;

				if (AugFileKind.PERSON.equals(kind)) {
					belongsToCompany = companies.get(newCompany.getSelectedIndex());
				}

				if (addAugFile(kind, newAugFileName.getText().trim(), belongsToCompany)) {
					addDialog.dispose();
				}
			}
		});
		buttonRow.add(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addDialog.dispose();
			}
		});
		buttonRow.add(cancelButton);

		// Set the preferred size of the dialog
		int width = 450;
		int height = 140;
		if (AugFileKind.PERSON.equals(kind)) {
			height = 210;
		}
		addDialog.setSize(width, height);
		addDialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(addDialog);
	}
	*/

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

	/*
	// TODO :: move main part of this to augFileCtrl!
	private boolean addAugFile(AugFileKind kind, String newAugFileName, Company belongsToCompany) {

		String origName = sanitizeName(newAugFileName);
		String newName = origName;

		Directory fileBaseDir = augFileCtrl.getLastLoadedDirectory();

		if (AugFileKind.PERSON.equals(kind)) {
			fileBaseDir = fileBaseDir.getChildDir(belongsToCompany.getDirectoryName());
		}

		File newFileLocation = new File(AugFileBaseDir, newName + ".xml");

		int counter = 1;

		// check that the new name is not already the file name of some other file!
		while (newFileLocation.exists()) {
			counter++;
			newName = origName + "_" + counter;
			newFileLocation = new File(AugFileBaseDir, newName + ".xml");
		}

		// add a file CI with one file with exactly this name - but do not save it on the hard disk just yet
		String fileCiContent =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<AugFile createdBy=\"Created by the " + Utils.getFullProgramIdentifier() + "\">\n" +
			"  <kind>" + kind + "</kind>\n" +
			"  <name>" + newAugFileName + "</name>\n" +
			"  <details></details>\n";

		if (AugFileKind.COMPANY.equals(kind)) {
			fileCiContent += "  <directoryName>" + newName + "</directoryName>\n";
		}

		fileCiContent += "</file>";

		File tmpCi = new File("tmpfile.tmp");
		tmpCi.setContent(AugFileCiContent);
		tmpCi.save();

		// keep track of which augFiles there were before loading somesuch... (making a shallow copy!)
		List<AugFile> augFilesBefore = new ArrayList<>(augFileCtrl.getAugFiles());

		// try {
			AugFileFile newAugFileFile;

			if (AugFileKind.PERSON.equals(kind)) {
				newAugFileFile = augFileCtrl.loadAnotherPersonFile(tmpCi, belongsToCompany);
			} else {
				newAugFileFile = augFileCtrl.loadAnotherCompanyFile(tmpCi);
			}

			List<AugFile> augFilesAfter = new ArrayList<>(augFileCtrl.getAugFiles());

			augFilesAfter.removeAll(augFilesBefore);

			if (augFilesAfter.size() != 1) {
				JOptionPane.showMessageDialog(mainFrame, "Oops - while trying to create the new file, after creating it temporarily, it could not be found!", "Sorry", JOptionPane.ERROR_MESSAGE);
				return true;
			}

			newAugFileFile.getRoot();

			newAugFileFile.setFilelocation(newFileLocation);

			tmpCi.delete();

			// add an file tab for the new file as currentlyShownTab
			setCurrentlyShownTab(new AugFileTab(mainPanelRight, augFilesAfter.iterator().next(), this, augFileCtrl));

			currentlyShownTab.setChanged(true);

			// add the new file to the GUI
			augFileTabs.add(currentlyShownTab);

		// this also automagically switches to the newly added tab, as it is the currentlyShownTab
		regenerateAugFileList();

		reEnableDisableMenuItems();

		return true;
	}

	private void openRenameCurrentAugFileDialog() {

		// figure out which file tab is currently open (show error if none is open)
		if (currentlyShownTab == null) {
			JOptionPane.showMessageDialog(mainFrame, "No file has been selected, so no file can be renamed - sorry!", "Sorry", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// open a dialog in which the new name is to be entered (pre-filled with the current name)

		// Create the window
		final JDialog renameDialog = new JDialog(mainFrame, "Rename AugFile", true);
		GridLayout renameDialogLayout = new GridLayout(3, 1);
		renameDialogLayout.setVgap(8);
		renameDialog.setLayout(renameDialogLayout);
		renameDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Please enter the new name of the file file:");
		renameDialog.add(explanationLabel);

		final JTextField newAugFileName = new JTextField();
		newAugFileName.setText(currentlyShownTab.getName());
		renameDialog.add(newAugFileName);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		renameDialog.add(buttonRow);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (renameCurrentAugFile(newAugFileName.getText().trim())) {
					renameDialog.dispose();
				}
			}
		});
		buttonRow.add(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameDialog.dispose();
			}
		});
		buttonRow.add(cancelButton);

		// Set the preferred size of the dialog
		int width = 350;
		int height = 160;
		renameDialog.setSize(width, height);
		renameDialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(renameDialog);
	}
	*/

	/**
	 * Rename the currently opened file to the name newAugFileStr
	 * @return true if something happened and the dialog should be closed, false if it should stay open
	 */
	 /*
	private boolean renameCurrentAugFile(String newAugFileStr) {

		// TODO :: also add a way to rename the associated activity, if an activity is associated, or even the alias

		if ("".equals(newAugFileStr)) {
			JOptionPane.showMessageDialog(mainFrame, "Please enter a new name for the file.", "Enter Name", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (currentlyShownTab == null) {
			JOptionPane.showMessageDialog(mainFrame, "The file cannot be renamed as currently no file has been opened.", "Sorry", JOptionPane.ERROR_MESSAGE);
			return true;
		}

		// if the name does not change - do nothing... ;)
		String oldAugFileStr = currentlyShownTab.getName();
		if (oldAugFileStr.equals(newAugFileStr)) {
			return true;
		}

		// tell the currently opened file tab to tell the cdmfile to tell the cdmfile to change the file name
		// (oh and the file tab should change its name, and and and...)
		for (AugFileTab tab : augFileTabs) {
			if (tab.isItem(oldAugFileStr)) {
				tab.setName(newAugFileStr);
				tab.show();
				setCurrentlyShownTab(tab);
			} else {
				tab.hide();
			}
		}

		// apply changed marker on the left hand side
		regenerateAugFileList();

		return true;
	}

	private void openDeleteCurrentAugFileDialog() {

		// figure out which file tab is currently open (show error if none is open)
		if (currentlyShownTab == null) {
			JOptionPane.showMessageDialog(mainFrame, "No file has been selected, so no file can be deleted - sorry!", "Sorry", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// open a dialog to confirm that the file should be deleted

		// Create the window
		String deleteAugFile = currentlyShownTab.getName();
		final JDialog deleteDialog = new JDialog(mainFrame, "Delete " + deleteAugFile, true);
		GridLayout deleteDialogLayout = new GridLayout(3, 1);
		deleteDialogLayout.setVgap(8);
		deleteDialog.setLayout(deleteDialogLayout);
		deleteDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Do you really want to delete the file:");
		deleteDialog.add(explanationLabel);

		JLabel fileNameLabel = new JLabel();
		fileNameLabel.setText(deleteAugFile);
		deleteDialog.add(AugFileNameLabel);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		deleteDialog.add(buttonRow);

		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (deleteCurrentAugFile()) {
					deleteDialog.dispose();
				}
			}
		});
		buttonRow.add(deleteButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteDialog.dispose();
			}
		});
		buttonRow.add(cancelButton);

		// Set the preferred size of the dialog
		int width = 300;
		int height = 160;
		deleteDialog.setSize(width, height);
		deleteDialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(deleteDialog);
	}
	*/

	/**
	 * Delete the currently opened file
	 * @return true if something happened and the dialog should be closed, false if it should stay open
	 */
	 /*
	private boolean deleteCurrentAugFile() {

		if (currentlyShownTab == null) {
			JOptionPane.showMessageDialog(mainFrame, "The file cannot be deleted as currently no file has been opened.", "Sorry", JOptionPane.ERROR_MESSAGE);
			return true;
		}

		// tell the currently opened file tab to tell the cdmfile to tell the cdmfile to delete the file
		// (actually, most likely the whole file has to be deleted, together with potentially the activity mapper
		// file that attaches the file to an activity, and possibly even the entire activity... hooray!)
		currentlyShownTab.delete();

		// remove the currently shown tab from the list of existing tabs
		List<AugFileTab> oldaugFileTabs = augFileTabs;

		augFileTabs = new ArrayList<>();
		for (AugFileTab sT : oldaugFileTabs) {
			if (sT != currentlyShownTab) {
				augFileTabs.add(sT);
			}
		}

		setCurrentlyShownTab(null);

		// remove file from the left hand side
		regenerateAugFileList();

		reEnableDisableMenuItems();

		return true;
	}
	*/

	/**
	 * Regenerate the file list on the left hand side based on the augFileTabs list,
	 * and (if at least one file exists), select and open the current tab or, if it
	 * is null, the lastly added one
	 */
	public void regenerateAugFileList() {

		// if there is no last shown tab...
		if (currentlyShownTab == null) {
			// ... show some random tab explicitly - this is fun, and the tabbed layout otherwise shows it anyway, so may as well...
			if (augFileTabs.size() > 0) {
				AugFileTab latestTab = augFileTabs.get(0);
				Date latestAccessTime = latestTab.getFile().getLastAccessTime();
				for (int i = 1; i < augFileTabs.size(); i++) {
					Date curAccessTime = augFileTabs.get(i).getFile().getLastAccessTime();
					if (curAccessTime.compareTo(latestAccessTime) > 0) {
						latestTab = augFileTabs.get(i);
						latestAccessTime = curAccessTime;
					}
				}
				setCurrentlyShownTab(latestTab);
			}
		}

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

		// if there still is no last shown tab (e.g. we just deleted the very last one)...
		if (currentlyShownTab == null) {
			// ... then we do not need to show or highlight any ;)
			return;
		}

		// show the last shown tab
		showTab(currentlyShownTab);

		highlightTabInLeftListOrTree(currentlyShownTab);
	}

	public void highlightTabInLeftListOrTree(AugFileTab tab) {
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
					fileTreeComponent.setSelectionPath(new TreePath(fileTreeModel.getPathToRoot(tab)));
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

		// TODO :: get current file name

		/*
		Directory lastLoadedDir = augFileCtrl.getLastLoadedDirectory();

		if (lastLoadedDir == null) {
			mainFrame.setTitle(Main.PROGRAM_TITLE);
		} else {
			mainFrame.setTitle(Main.PROGRAM_TITLE + " - " + lastLoadedDir.getDirname());
		}
		*/

		mainFrame.setTitle(Main.PROGRAM_TITLE);
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
			augFileTabs.add(new AugFileTab(mainPanelRight, file, this, augFileCtrl));
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

	public void switchToWorkspace(JCheckBoxMenuItem workspaceItem, String workspaceName) {

		mainMenu.uncheckWorkspaces();

		workspaceItem.setSelected(true);

		// set the current tab to null such that we automagically open the latest one of the newly opened workspace
		setCurrentlyShownTab(null);

		augFileCtrl.switchToWorkspace(workspaceName);

		reloadAllAugFileTabs();

		Record activeWorkspace = augFileCtrl.getActiveWorkspace();

		setRemoveTrailingWhitespaceOnSave(activeWorkspace.getBoolean(CONFIG_KEY_REMOVE_TRAILING_WHITESPACE_ON_SAVE, removeTrailingWhitespaceOnSave));
		setReplaceWhitespacesWithTabsOnSave(activeWorkspace.getBoolean(CONFIG_KEY_REPLACE_WHITESPACES_WITH_TABS_ON_SAVE, replaceWhitespacesWithTabsOnSave));
		setReplaceTabsWithWhitespacesOnSave(activeWorkspace.getBoolean(CONFIG_KEY_REPLACE_TABS_WITH_WHITESPACES_ON_SAVE, replaceTabsWithWhitespacesOnSave));
		setReorganizeImportsOnSave(activeWorkspace.getBoolean(CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE, reorganizeImportsOnSave));
		setRemoveUnusedImportsOnSave(activeWorkspace.getBoolean(CONFIG_KEY_REMOVE_UNUSED_IMPORTS_ON_SAVE, removeUnusedImportsOnSave));
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

	public MainMenu getMainMenu() {
		return mainMenu;
	}

}
