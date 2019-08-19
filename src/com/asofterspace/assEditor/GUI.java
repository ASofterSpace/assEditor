/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.gui.MainWindow;
import com.asofterspace.toolbox.gui.ProgressDialog;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.Record;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.utils.ProgressIndicator;
import com.asofterspace.toolbox.Utils;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


public class GUI extends MainWindow {

	private AugFileCtrl augFileCtrl;

	private JPanel mainPanelRight;

	private JPanel searchPanel;
	private JTextField searchField;

	private AugFileTab currentlyShownTab;

	// on the left hand side, we add this string to indicate that the file has changed
	private final static String CHANGE_INDICATOR = " *";

	private final static String CONFIG_KEY_LAST_DIRECTORY = "lastDirectory";
	private final static String CONFIG_KEY_SCHEME = "scheme";
	private final static String CONFIG_KEY_REMOVE_TRAILING_WHITESPACE_ON_SAVE = "onSaveRemoveTrailingWhitespace";
	private final static String CONFIG_KEY_REPLACE_WHITESPACES_WITH_TABS_ON_SAVE = "onSaveReplaceWhitespacesWithTabs";
	private final static String CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE = "onSaveReorganizeImports";
	private final static String CONFIG_KEY_COPY_ON_ENTER = "copyOnEnter";
	private final static String CONFIG_KEY_TAB_ENTIRE_BLOCKS = "tabEntireBlocks";
	private final static String CONFIG_KEY_BACKUP_NUM = "backupNum";
	private final static String CONFIG_KEY_WIDTH = "mainFrameWidth";
	private final static String CONFIG_KEY_HEIGHT = "mainFrameHeight";
	private final static String CONFIG_KEY_LEFT = "mainFrameLeft";
	private final static String CONFIG_KEY_TOP = "mainFrameTop";
	private final static String CONFIG_KEY_FONT_SIZE = "fontSize";

	final static String LIGHT_SCHEME = "light";
	final static String DARK_SCHEME = "dark";

	private final static int DEFAULT_FONT_SIZE = 14;

	private JMenu switchWorkspace;
	private JMenuItem refreshFiles;
	private JMenuItem saveFile;
	private JMenuItem saveAllFiles;
	private JMenuItem deleteFile;
	private JMenuItem closeFile;
	private JMenuItem closeAllFiles;
	private JMenuItem saveFilePopup;
	private JMenuItem deleteFilePopup;
	private JMenuItem closeFilePopup;
	private JCheckBoxMenuItem setLightSchemeItem;
	private JCheckBoxMenuItem setDarkSchemeItem;
	private JCheckBoxMenuItem removeTrailingWhitespaceOnSaveItem;
	private JCheckBoxMenuItem replaceWhitespacesWithTabsOnSaveItem;
	private JCheckBoxMenuItem reorganizeImportsOnSaveItem;
	private JCheckBoxMenuItem copyOnEnterItem;
	private JCheckBoxMenuItem tabEntireBlocksItem;
	private JCheckBoxMenuItem usingUTF8WithBOM;
	private JCheckBoxMenuItem usingUTF8WithoutBOM;
	private JMenuItem close;
	private List<JMenuItem> codeKindItems;
	private List<JCheckBoxMenuItem> codeKindItemsCurrent;
	private List<JCheckBoxMenuItem> workspaces;

	private JDialog searchInWorkspaceDialog;
	private JTextArea searchInWorkspaceOutputMemo;
	private JLabel searchInWorkspaceOutputLabel;

	private List<AugFileTab> augFileTabs;

	private ConfigFile configuration;
	private JList<String> fileListComponent;
	private JPopupMenu fileListPopup;
	private String[] strAugFiles;

	private Integer currentBackup;

	private int fontSize;

	String currentScheme;
	Boolean removeTrailingWhitespaceOnSave;
	Boolean replaceWhitespacesWithTabsOnSave;
	Boolean reorganizeImportsOnSave;
	Boolean copyOnEnter;
	Boolean tabEntireBlocks;


	public GUI(AugFileCtrl augFileCtrl, ConfigFile config) {

		this.augFileCtrl = augFileCtrl;

		this.configuration = config;

		strAugFiles = new String[0];

		augFileTabs = new ArrayList<>();

		currentScheme = configuration.getValue(CONFIG_KEY_SCHEME);

		if (currentScheme == null) {
			currentScheme = DARK_SCHEME;
		}

		removeTrailingWhitespaceOnSave = configuration.getBoolean(CONFIG_KEY_REMOVE_TRAILING_WHITESPACE_ON_SAVE, true);

		replaceWhitespacesWithTabsOnSave = configuration.getBoolean(CONFIG_KEY_REPLACE_WHITESPACES_WITH_TABS_ON_SAVE, true);

		reorganizeImportsOnSave = configuration.getBoolean(CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE, true);

		copyOnEnter = configuration.getBoolean(CONFIG_KEY_COPY_ON_ENTER, true);

		tabEntireBlocks = configuration.getBoolean(CONFIG_KEY_TAB_ENTIRE_BLOCKS, true);

		currentBackup = configuration.getInteger(CONFIG_KEY_BACKUP_NUM, 0);

		fontSize = configuration.getInteger(CONFIG_KEY_FONT_SIZE, DEFAULT_FONT_SIZE);

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
		createMenu(mainFrame);

		createPopupMenu(mainFrame);

		createMainPanel(mainFrame);

		configureGUI();

		refreshTitleBar();

		reEnableDisableMenuItems();

		reSelectSchemeItems();

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

	private JMenuBar createMenu(JFrame parent) {

		JMenuBar menu = new JMenuBar();

		// TODO :: add undo / redo (for basically any action, but first of all of course for the editor)

		JMenu file = new JMenu("File");
		menu.add(file);

		JMenuItem newFile = new JMenuItem("New File");
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newFile();
			}
		});
		file.add(newFile);

		JMenuItem openFile = new JMenuItem("Open");
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openFile();
			}
		});
		file.add(openFile);

		refreshFiles = new JMenuItem("Refresh All Files From Disk");
		refreshFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// ifAllowedToLeaveCurrentDirectory(new Callback() {
					// public void call() {
						reloadAllAugFileTabs();
					// }
				// });
			}
		});
		file.add(refreshFiles);

		file.addSeparator();

		saveFile = new JMenuItem("Save Current File");
		saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});
		file.add(saveFile);

		/*
		JMenuItem openFile = new JMenuItem("Save Current File As...");
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		openFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openFile();
			}
		});
		file.add(openFile);
		*/

		saveAllFiles = new JMenuItem("Save All Files");
		saveAllFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAllFiles();
			}
		});
		file.add(saveAllFiles);

		file.addSeparator();

		deleteFile = new JMenuItem("Delete Current File");
		deleteFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteFile();
			}
		});
		file.add(deleteFile);

		file.addSeparator();

		closeFile = new JMenuItem("Close Current File");
		closeFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeFile();
			}
		});
		file.add(closeFile);

		closeAllFiles = new JMenuItem("Close All Files");
		closeAllFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeAllFiles();
			}
		});
		file.add(closeAllFiles);

		/*
		addPerson = new JMenuItem("Add Person");
		addPerson.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
		addPerson.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openAddNewPersonDialog();
			}
		});
		file.add(addPerson);
		addCompany = new JMenuItem("Add Company");
		addCompany.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		addCompany.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openAddNewCompanyDialog();
			}
		});
		file.add(addCompany);
		file.addSeparator();
		renameCurAugFile = new JMenuItem("Rename Current AugFile");
		renameCurAugFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openRenameCurrentAugFileDialog();
			}
		});
		file.add(renameCurAugFile);
		deleteCurAugFile = new JMenuItem("Delete Current AugFile");
		deleteCurAugFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openDeleteCurrentAugFileDialog();
			}
		});
		file.add(deleteCurAugFile);
		file.addSeparator();
		saveAugFiles = new JMenuItem("Save All Changed AugFiles");
		saveAugFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAugFiles();
			}
		});
		file.add(saveAugFiles);
		*/
		file.addSeparator();
		close = new JMenuItem("Exit");
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO :: check if we are allowed to close!
				// TODO :: also check before allowing a close from the X at the top right
				// ifAllowedToLeaveCurrentDirectory(new Callback() {
				//	public void call() {
						System.exit(0);
				//	}
				// });
			}
		});
		file.add(close);


		switchWorkspace = new JMenu("Workspace");
		menu.add(switchWorkspace);

		refreshWorkspaces();


		JMenu edit = new JMenu("Edit");
		menu.add(edit);

		JMenuItem undoItem = new JMenuItem("Undo");
		undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		undoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.undo();
				}
			}
		});
		edit.add(undoItem);

		JMenuItem redoItem = new JMenuItem("Redo");
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		redoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.redo();
				}
			}
		});
		edit.add(redoItem);

		edit.addSeparator();

		JMenuItem selectToHere = new JMenuItem("Select from Start to Here");
		selectToHere.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.selectToHere();
				}
			}
		});
		edit.add(selectToHere);

		JMenuItem selectFromHere = new JMenuItem("Select from Here to End");
		selectFromHere.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.selectFromHere();
				}
			}
		});
		edit.add(selectFromHere);

		JMenuItem selectAll = new JMenuItem("Select All");
		selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		selectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.selectAll();
				}
			}
		});
		edit.add(selectAll);

		edit.addSeparator();

		JMenuItem duplicateCurrentLine = new JMenuItem("Duplicate Current Line");
		duplicateCurrentLine.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.SHIFT_MASK));
		duplicateCurrentLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.duplicateCurrentLine();
				}
			}
		});
		edit.add(duplicateCurrentLine);

		JMenuItem duplicateCurrentLineCtrl = new JMenuItem("Duplicate Current Line using [Ctrl] instead");
		duplicateCurrentLineCtrl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.CTRL_MASK));
		duplicateCurrentLineCtrl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.duplicateCurrentLine();
				}
			}
		});
		edit.add(duplicateCurrentLineCtrl);

		JMenuItem deleteCurrentLine = new JMenuItem("Delete Current Line");
		deleteCurrentLine.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		deleteCurrentLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.deleteCurrentLine();
				}
			}
		});
		edit.add(deleteCurrentLine);

		JMenuItem lowCurSel = new JMenuItem("Lowcase Current Selection");
		lowCurSel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.lowCurSel();
				}
			}
		});
		edit.add(lowCurSel);

		JMenuItem upCurSel = new JMenuItem("Upcase Current Selection");
		upCurSel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.upCurSel();
				}
			}
		});
		edit.add(upCurSel);

		JMenuItem lowCurWord = new JMenuItem("Lowcase Current Word");
		lowCurWord.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		lowCurWord.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.lowCurWord();
				}
			}
		});
		edit.add(lowCurWord);

		JMenuItem upCurWord = new JMenuItem("Upcase Current Word");
		upCurWord.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
		upCurWord.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.upCurWord();
				}
			}
		});
		edit.add(upCurWord);

		edit.addSeparator();

		JMenuItem applyGit = new JMenuItem("Apply Git Markers (+/- at the beginning of lines)");
		applyGit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.applyGit();
				}
			}
		});
		edit.add(applyGit);

		JMenuItem writeLineNumbers = new JMenuItem("Write Line Numbers in Front of Each Line");
		writeLineNumbers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.writeLineNumbers();
				}
			}
		});
		edit.add(writeLineNumbers);

		JMenuItem removeLineNumbers = new JMenuItem("Remove Line Numbers from Front of Each Line");
		removeLineNumbers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.removeLineNumbers();
				}
			}
		});
		edit.add(removeLineNumbers);

		JMenuItem removeTrailWhitespace = new JMenuItem("Remove Trailing Whitespace");
		removeTrailWhitespace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.removeTrailingWhitespace();
				}
			}
		});
		edit.add(removeTrailWhitespace);

		JMenuItem repWhitespacesWithTabs = new JMenuItem("Replace Leading Whitespaces with Tabs");
		repWhitespacesWithTabs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.replaceLeadingWhitespacesWithTabs();
				}
			}
		});
		edit.add(repWhitespacesWithTabs);

		JMenuItem reorgImports = new JMenuItem("Reorganize Imports");
		reorgImports.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.reorganizeImports();
				}
			}
		});
		edit.add(reorgImports);

		edit.addSeparator();

		JMenuItem sortDocumentAlph = new JMenuItem("Sort Entire Document Alphabetically");
		sortDocumentAlph.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.sortDocumentAlphabetically();
				}
			}
		});
		edit.add(sortDocumentAlph);

		JMenuItem sortSelectedLinesAlph = new JMenuItem("Sort Selected Lines Alphabetically");
		sortSelectedLinesAlph.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.sortSelectedLinesAlphabetically();
				}
			}
		});
		edit.add(sortSelectedLinesAlph);

		JMenuItem sortSelectedStringsAlph = new JMenuItem("Sort Selected Strings (\"foo\", \"bar\") Alphabetically");
		sortSelectedStringsAlph.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentlyShownTab != null) {
					currentlyShownTab.sortSelectedStringsAlphabetically();
				}
			}
		});
		edit.add(sortSelectedStringsAlph);

		edit.addSeparator();

		JMenuItem search = new JMenuItem("Search");
		search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSearchBar();
			}
		});
		edit.add(search);

		JMenuItem searchInWorkspace = new JMenuItem("Search in Workspace");
		searchInWorkspace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSearchWindow();
			}
		});
		edit.add(searchInWorkspace);


		JMenu encodings = new JMenu("Encodings");
		menu.add(encodings);

		usingUTF8WithoutBOM = new JCheckBoxMenuItem("Default UTF-8 (without BOM)");
		usingUTF8WithoutBOM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentlyShownTab.setUsingUTF8BOM(false);
				setUsingUTF8WithBOM(false);
			}
		});
		usingUTF8WithoutBOM.setSelected(false);
		encodings.add(usingUTF8WithoutBOM);

		usingUTF8WithBOM = new JCheckBoxMenuItem("UTF-8 with BOM");
		usingUTF8WithBOM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentlyShownTab.setUsingUTF8BOM(true);
				setUsingUTF8WithBOM(true);
			}
		});
		usingUTF8WithBOM.setSelected(false);
		encodings.add(usingUTF8WithBOM);

		encodings.addSeparator();

		JMenuItem allUsingUTF8WithoutBOM = new JMenuItem("Set All Files to Default UTF-8 (without BOM)");
		allUsingUTF8WithoutBOM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (AugFileTab tab : augFileTabs) {
					tab.setUsingUTF8BOM(false);
				}
				setUsingUTF8WithBOM(false);
			}
		});
		encodings.add(allUsingUTF8WithoutBOM);

		JMenuItem allUsingUTF8WithBOM = new JMenuItem("Set All Files to UTF-8 with BOM");
		allUsingUTF8WithBOM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (AugFileTab tab : augFileTabs) {
					tab.setUsingUTF8BOM(true);
				}
				setUsingUTF8WithBOM(true);
			}
		});
		encodings.add(allUsingUTF8WithBOM);


		JMenu settings = new JMenu("Settings");
		menu.add(settings);

		JMenu languageCurrent = new JMenu("Code Language for Current File");
		settings.add(languageCurrent);
		codeKindItemsCurrent = new ArrayList<>();
		for (final CodeLanguage ck : CodeLanguage.values()) {
			JCheckBoxMenuItem ckItem = new JCheckBoxMenuItem(ck.toString());
			ckItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setOrUnsetCurrentCodeLanguage(ck);
				}
			});
			ckItem.setSelected(false);
			languageCurrent.add(ckItem);
			codeKindItemsCurrent.add(ckItem);
		}
		languageCurrent.addSeparator();
		JMenuItem ckCurDefault = new JMenuItem("Default");
		ckCurDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setOrUnsetCurrentCodeLanguage(null);
			}
		});
		languageCurrent.add(ckCurDefault);

		JMenu language = new JMenu("Code Language for All Files");
		settings.add(language);
		codeKindItems = new ArrayList<>();
		for (final CodeLanguage ck : CodeLanguage.values()) {
			JMenuItem ckItem = new JMenuItem(ck.toString());
			ckItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setOrUnsetAllCodeLanguages(ck);
				}
			});
			language.add(ckItem);
			codeKindItems.add(ckItem);
		}
		language.addSeparator();
		JMenuItem ckDefault = new JMenuItem("Default");
		ckDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setOrUnsetAllCodeLanguages(null);
			}
		});
		language.add(ckDefault);

		JMenu scheme = new JMenu("Editor Color Scheme");
		settings.add(scheme);
		setLightSchemeItem = new JCheckBoxMenuItem("Light");
		setLightSchemeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setScheme(LIGHT_SCHEME);
			}
		});
		scheme.add(setLightSchemeItem);
		setDarkSchemeItem = new JCheckBoxMenuItem("Dark");
		setDarkSchemeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setScheme(DARK_SCHEME);
			}
		});
		scheme.add(setDarkSchemeItem);

		JMenu fontSizeItem = new JMenu("Editor Font Size");
		settings.add(fontSizeItem);
		JMenuItem fontSizePlusItem = new JMenuItem("Increase");
		fontSizePlusItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setFontSize(getFontSize() + 1);
			}
		});
		fontSizeItem.add(fontSizePlusItem);
		JMenuItem fontSizeDefaultItem = new JMenuItem("Reset to Default");
		fontSizeDefaultItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setFontSize(DEFAULT_FONT_SIZE);
			}
		});
		fontSizeItem.add(fontSizeDefaultItem);
		JMenuItem fontSizeMinusItem = new JMenuItem("Decrease");
		fontSizeMinusItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setFontSize(getFontSize() - 1);
			}
		});
		fontSizeItem.add(fontSizeMinusItem);

		settings.addSeparator();

		removeTrailingWhitespaceOnSaveItem = new JCheckBoxMenuItem("Remove Trailing Whitespace on Save");
		removeTrailingWhitespaceOnSaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setRemoveTrailingWhitespaceOnSave(!removeTrailingWhitespaceOnSave);
			}
		});
		setRemoveTrailingWhitespaceOnSave(removeTrailingWhitespaceOnSave);
		settings.add(removeTrailingWhitespaceOnSaveItem);

		replaceWhitespacesWithTabsOnSaveItem = new JCheckBoxMenuItem("Replace Leading Whitespaces with Tabs on Save");
		replaceWhitespacesWithTabsOnSaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setReplaceWhitespacesWithTabsOnSave(!replaceWhitespacesWithTabsOnSave);
			}
		});
		setReplaceWhitespacesWithTabsOnSave(replaceWhitespacesWithTabsOnSave);
		settings.add(replaceWhitespacesWithTabsOnSaveItem);

		reorganizeImportsOnSaveItem = new JCheckBoxMenuItem("Reorganize Imports on Save");
		reorganizeImportsOnSaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setReorganizeImportsOnSave(!reorganizeImportsOnSave);
			}
		});
		setReorganizeImportsOnSave(reorganizeImportsOnSave);
		settings.add(reorganizeImportsOnSaveItem);

		JMenuItem toggleAllSwitchesInGroup = new JMenuItem("Toggle All Switches In Group");
		toggleAllSwitchesInGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean toggleTo = !removeTrailingWhitespaceOnSave;
				setRemoveTrailingWhitespaceOnSave(toggleTo);
				setReplaceWhitespacesWithTabsOnSave(toggleTo);
				setReorganizeImportsOnSave(toggleTo);
			}
		});
		settings.add(toggleAllSwitchesInGroup);

		settings.addSeparator();

		/*
		copyOnEnterItem = new JCheckBoxMenuItem("Copy Line on [Ctrl / Shift] + [Enter]");
		copyOnEnterItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setCopyOnEnter(!copyOnEnter);
			}
		});
		setCopyOnEnter(copyOnEnter);
		settings.add(copyOnEnterItem);
		*/

		tabEntireBlocksItem = new JCheckBoxMenuItem("[Tab] Entire Blocks");
		tabEntireBlocksItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setTabEntireBlocks(!tabEntireBlocks);
			}
		});
		setTabEntireBlocks(tabEntireBlocks);
		settings.add(tabEntireBlocksItem);

		JMenu huh = new JMenu("?");
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String aboutMessage = "This is the " + Main.PROGRAM_TITLE + ".\n" +
					"Version: " + Main.VERSION_NUMBER + " (" + Main.VERSION_DATE + ")\n" +
					"Brought to you by: A Softer Space";
				JOptionPane.showMessageDialog(mainFrame, aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		huh.add(about);
		menu.add(huh);

		parent.setJMenuBar(menu);

		return menu;
	}

	private JPopupMenu createPopupMenu(JFrame parent) {

		fileListPopup = new JPopupMenu();

		saveFilePopup = new JMenuItem("Save This File");
		saveFilePopup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});
		fileListPopup.add(saveFilePopup);

		deleteFilePopup = new JMenuItem("Delete This File");
		deleteFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteFile();
			}
		});
		fileListPopup.add(deleteFilePopup);

		closeFilePopup = new JMenuItem("Close This File");
		closeFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeFile();
			}
		});
		fileListPopup.add(closeFilePopup);

		/*
		addPersonPopup = new JMenuItem("Add Person");
		addPersonPopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openAddNewPersonDialog();
			}
		});
		fileListPopup.add(addPersonPopup);
		addCompanyPopup = new JMenuItem("Add Company");
		addCompanyPopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openAddNewCompanyDialog();
			}
		});
		fileListPopup.add(addCompanyPopup);
		fileListPopup.addSeparator();
		renameCurAugFilePopup = new JMenuItem("Rename Current AugFile");
		renameCurAugFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openRenameCurrentAugFileDialog();
			}
		});
		fileListPopup.add(renameCurAugFilePopup);
		deleteCurAugFilePopup = new JMenuItem("Delete Current AugFile");
		deleteCurAugFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openDeleteCurrentAugFileDialog();
			}
		});
		fileListPopup.add(deleteCurAugFilePopup);
		*/

		// don't do the following:
		//   fileListComponent.setComponentPopupMenu(popupMenu);
		// instead manually show the popup when the right mouse key is pressed in the mouselistener
		// for the file list, because that means that we can right click on an file, select it immediately,
		// and open the popup for exactly that file

		return fileListPopup;
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

		JPanel gapPanel = new JPanel();
		gapPanel.setPreferredSize(new Dimension(8, 8));

		String[] fileList = new String[0];
		fileListComponent = new JList<String>(fileList);
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
					fileListPopup.show(fileListComponent, e.getX(), e.getY());
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

		JScrollPane augFileListScroller = new JScrollPane(fileListComponent);
		augFileListScroller.setPreferredSize(new Dimension(8, 8));
		augFileListScroller.setBorder(BorderFactory.createEmptyBorder());

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

		final JTextField replaceField = new JTextField();

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

		searchPanel.add(searchField, new Arrangement(0, 0, 1.0, 1.0));
		searchPanel.add(replaceField, new Arrangement(0, 1, 1.0, 1.0));

		mainPanelRightOuter.add(mainPanelRight, new Arrangement(0, 0, 1.0, 1.0));

		mainPanelRightOuter.add(searchPanel, new Arrangement(0, 1, 1.0, 0.0));

		mainPanel.add(augFileListScroller, new Arrangement(0, 0, 0.2, 1.0));

		mainPanel.add(gapPanel, new Arrangement(1, 0, 0.0, 0.0));

		mainPanel.add(mainPanelRightOuter, new Arrangement(2, 0, 1.0, 1.0));

		parent.add(mainPanel, BorderLayout.CENTER);

		return mainPanel;
	}

	private void refreshWorkspaces() {

		switchWorkspace.removeAll();

		workspaces = new ArrayList<>();
		for (final String workspaceName : augFileCtrl.getWorkspaces()) {
			final JCheckBoxMenuItem workspace = new JCheckBoxMenuItem(workspaceName);
			workspace.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					uncheckWorkspaces();
					workspace.setSelected(true);
					augFileCtrl.switchToWorkspace(workspaceName);
					reloadAllAugFileTabs();
				}
			});
			if (workspaceName.equals(augFileCtrl.getWorkspaceName())) {
				workspace.setSelected(true);
			} else {
				workspace.setSelected(false);
			}
			switchWorkspace.add(workspace);
			workspaces.add(workspace);
		}

		switchWorkspace.addSeparator();

		JMenuItem editWorkspaces = new JMenuItem("Edit Workspaces");
		editWorkspaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO :: create a modal in which existing workspaces can be deleted,
				// TODO :: and existing ones can be moved up and down

				// Create the window
				final JDialog editWorkitemsDialog = new JDialog(mainFrame, "Edit Workspaces", true);
				GridLayout editWorkitemsDialogLayout = new GridLayout(3, 1);
				editWorkitemsDialogLayout.setVgap(8);
				editWorkitemsDialog.setLayout(editWorkitemsDialogLayout);
				editWorkitemsDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

				// Populate the window
				JLabel explanationLabel = new JLabel();
				explanationLabel.setText("Enter a new name here to add a new workspace:");
				editWorkitemsDialog.add(explanationLabel);

				final JTextField newWorkspaceName = new JTextField();
				editWorkitemsDialog.add(newWorkspaceName);

				JPanel buttonRow = new JPanel();
				GridLayout buttonRowLayout = new GridLayout(1, 2);
				buttonRowLayout.setHgap(8);
				buttonRow.setLayout(buttonRowLayout);
				editWorkitemsDialog.add(buttonRow);

				JButton addButton = new JButton("Add this Workspace");
				addButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {

						augFileCtrl.addWorkspace(newWorkspaceName.getText());

						refreshWorkspaces();
					}
				});
				buttonRow.add(addButton);

				JButton doneButton = new JButton("Done");
				doneButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						editWorkitemsDialog.dispose();
					}
				});
				buttonRow.add(doneButton);

				// Set the preferred size of the dialog
				int width = 600;
				int height = 150;
				editWorkitemsDialog.setSize(width, height);
				editWorkitemsDialog.setPreferredSize(new Dimension(width, height));

				GuiUtils.centerAndShowWindow(editWorkitemsDialog);
			}
		});
		switchWorkspace.add(editWorkspaces);
	}

	private void uncheckWorkspaces() {
		for (JCheckBoxMenuItem workspace : workspaces) {
			workspace.setSelected(false);
		}
	}

	private void showSearchBar() {

		searchPanel.setVisible(true);

		searchField.requestFocus();
	}

	private void createSearchWindow() {

		// Create the window
		searchInWorkspaceDialog = new JDialog(mainFrame, "Search in Workspace", false);
		GridBagLayout searchInWorkspaceDialogLayout = new GridBagLayout();
		searchInWorkspaceDialog.setLayout(searchInWorkspaceDialogLayout);
		searchInWorkspaceDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Enter the text you are searching for:");
		searchInWorkspaceDialog.add(explanationLabel, new Arrangement(0, 0, 1.0, 0.0));

		final JTextField searchField = new JTextField();
		searchInWorkspaceDialog.add(searchField, new Arrangement(0, 1, 1.0, 0.0));

		JLabel explanationReplaceLabel = new JLabel();
		explanationReplaceLabel.setText("Enter here the replacement text in case you want to replace anything:");
		searchInWorkspaceDialog.add(explanationReplaceLabel, new Arrangement(0, 2, 1.0, 0.0));

		final JTextField replaceField = new JTextField();
		searchInWorkspaceDialog.add(replaceField, new Arrangement(0, 3, 1.0, 0.0));

		searchInWorkspaceOutputMemo = new JTextArea();
		JScrollPane outputMemoScroller = new JScrollPane(searchInWorkspaceOutputMemo);
		searchInWorkspaceDialog.add(outputMemoScroller, new Arrangement(0, 4, 1.0, 1.0));

		searchInWorkspaceOutputLabel = new JLabel();
		searchInWorkspaceDialog.add(searchInWorkspaceOutputLabel, new Arrangement(0, 5, 1.0, 0.0));

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 3);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		searchInWorkspaceDialog.add(buttonRow, new Arrangement(0, 6, 1.0, 0.0));

		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchInWorkspaceFor(searchField.getText());
			}
		});
		buttonRow.add(searchButton);

		JButton replaceButton = new JButton("Replace");
		replaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchInWorkspaceAndReplaceWith(searchField.getText(), replaceField.getText());
			}
		});
		buttonRow.add(replaceButton);

		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchInWorkspaceDialog.dispose();
			}
		});
		buttonRow.add(doneButton);

		// Set the preferred size of the dialog
		int width = 1200;
		int height = 800;
		searchInWorkspaceDialog.setSize(width, height);
		searchInWorkspaceDialog.setPreferredSize(new Dimension(width, height));
	}

	private void searchInWorkspaceFor(String searchFor) {

		StringBuilder result = new StringBuilder();

		int matches = 0;
		int infiles = 0;

		for (AugFileTab curTab : augFileTabs) {
			int curMatches = curTab.searchAndAddResultTo(searchFor, result);

			if (curMatches > 0) {
				matches += curMatches;
				infiles++;
			}
		}

		searchInWorkspaceOutputMemo.setText(result.toString());

		searchInWorkspaceOutputLabel.setText(
			"Found " +
			Utils.thingOrThings(matches, "match", "matches") +
			" in " +
			Utils.thingOrThings(infiles, "file") +
			"."
		);
	}

	private void searchInWorkspaceAndReplaceWith(String searchFor, String replaceWith) {

		for (AugFileTab curTab : augFileTabs) {
			curTab.replaceAll(searchFor, replaceWith);
			curTab.saveIfChanged();
		}

		searchInWorkspaceOutputMemo.setText("All occurrences of " + searchFor + " replaced with " + replaceWith + "!");
	}

	private void showSearchWindow() {

		if (searchInWorkspaceDialog == null) {
			createSearchWindow();
		}

		GuiUtils.centerAndShowWindow(searchInWorkspaceDialog);
	}

	private void openFile() {

		// TODO :: de-localize the JFileChooser (by default it seems localized, which is inconsistent when the rest of the program is in English...)
		// (while you're at it, make Öffnen into Save for the save dialog, but keep it as Open for the open dialog... ^^)
		JFileChooser augFilePicker;

		String lastDirectory = getWorkspace().getString(CONFIG_KEY_LAST_DIRECTORY);

		if ((lastDirectory != null) && !"".equals(lastDirectory)) {
			augFilePicker = new JFileChooser(new java.io.File(lastDirectory));
		} else {
			augFilePicker = new JFileChooser();
		}

		augFilePicker.setDialogTitle("Open a Code File to Edit");
		augFilePicker.setFileSelectionMode(JFileChooser.FILES_ONLY);
		augFilePicker.setMultiSelectionEnabled(true);

		int result = augFilePicker.showOpenDialog(mainFrame);

		switch (result) {

			case JFileChooser.APPROVE_OPTION:

				// load the files
				getWorkspace().setString(CONFIG_KEY_LAST_DIRECTORY, augFilePicker.getCurrentDirectory().getAbsolutePath());
				configuration.create();

				for (java.io.File curFile : augFilePicker.getSelectedFiles()) {

					File fileToOpen = new File(curFile);

					AugFile newFile = augFileCtrl.loadAnotherFile(fileToOpen);

					if (newFile != null) {
						setCurrentlyShownTab(new AugFileTab(mainPanelRight, newFile, this, augFileCtrl));
						augFileTabs.add(currentlyShownTab);
					}
				}

				regenerateAugFileList();

				reEnableDisableMenuItems();

				break;

			case JFileChooser.CANCEL_OPTION:
				// cancel was pressed... do nothing for now
				break;
		}
	}

	private void newFile() {

		SimpleFile fileToOpen = new SimpleFile("data/new.txt");

		fileToOpen.create();

		AugFile newFile = augFileCtrl.loadAnotherFile(fileToOpen);

		if (newFile != null) {
			augFileTabs.add(new AugFileTab(mainPanelRight, newFile, this, augFileCtrl));

			regenerateAugFileList();

			reEnableDisableMenuItems();
		}
	}

	private void saveFile() {

		currentlyShownTab.save();
	}

	private void saveAllFiles() {

		for (AugFileTab tab : augFileTabs) {
			tab.save();
		}
	}

	/*
	private void saveAugFiles() {

		// apply all changes, such that the current source code editor contents are actually stored in the CDM file objects
		for (AugFileTab augFileTab : augFileTabs) {
			augFileTab.saveIfChanged();
		}

		// remove all change indicators on the left-hand side
		regenerateAugFileList();

		// DO NOT save all opened files - instead, we called augFileTab.saveIfChanged, so we only save un-saved changes!
		// augFileCtrl.save();

		JOptionPane.showMessageDialog(mainFrame, "All changed files have been saved!", "AugFiles Saved", JOptionPane.INFORMATION_MESSAGE);
	}
	*/

	private void deleteFile() {

		augFileCtrl.removeFile(currentlyShownTab.getFile());

		currentlyShownTab.delete();

		augFileTabs.remove(currentlyShownTab);

		regenerateAugFileList();
	}

	private void closeFile() {

		augFileCtrl.removeFile(currentlyShownTab.getFile());

		currentlyShownTab.remove();

		augFileTabs.remove(currentlyShownTab);

		regenerateAugFileList();
	}

	private void closeAllFiles() {

		augFileCtrl.removeAllFiles();

		for (AugFileTab tab : augFileTabs) {
			tab.remove();
		}

		augFileTabs = new ArrayList<>();

		regenerateAugFileList();
	}

	private void updateHighlightersOnAllTabs() {

		for (AugFileTab augFileTab : augFileTabs) {
			augFileTab.updateHighlighterConfig();
		}
	}

	private void reSelectCurrentCodeLanguageItem() {

		String currentCodeLanguageStr = currentlyShownTab.getSourceLanguage().toString();

		for (JCheckBoxMenuItem codeKindItem : codeKindItemsCurrent) {
			codeKindItem.setSelected(false);

			if (codeKindItem.getText().equals(currentCodeLanguageStr)) {
				codeKindItem.setSelected(true);
			}
		}
	}

	private void setOrUnsetCurrentCodeLanguage(CodeLanguage codeKind) {

		currentlyShownTab.setCodeLanguageAndCreateHighlighter(codeKind);

		reSelectCurrentCodeLanguageItem();
	}

	private void setOrUnsetAllCodeLanguages(CodeLanguage codeKind) {

		for (AugFileTab augFileTab : augFileTabs) {
			augFileTab.setCodeLanguageAndCreateHighlighter(codeKind);
		}

		reSelectCurrentCodeLanguageItem();
	}

	private void reSelectSchemeItems() {

		setLightSchemeItem.setSelected(false);
		setDarkSchemeItem.setSelected(false);

		switch (currentScheme) {

			case LIGHT_SCHEME:
				setLightSchemeItem.setSelected(true);
				break;

			case DARK_SCHEME:
				setDarkSchemeItem.setSelected(true);
				break;
		}
	}

	private void setScheme(String scheme) {

		currentScheme = scheme;

		reSelectSchemeItems();

		configuration.set(CONFIG_KEY_SCHEME, currentScheme);

		switch (currentScheme) {
			case LIGHT_SCHEME:
				Code.setLightSchemeForAllEditors();
				break;
			case DARK_SCHEME:
				Code.setDarkSchemeForAllEditors();
				break;
		}
	}

	private void setRemoveTrailingWhitespaceOnSave(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		removeTrailingWhitespaceOnSave = setTo;

		configuration.set(CONFIG_KEY_REMOVE_TRAILING_WHITESPACE_ON_SAVE, removeTrailingWhitespaceOnSave);

		removeTrailingWhitespaceOnSaveItem.setSelected(removeTrailingWhitespaceOnSave);
	}

	private void setReplaceWhitespacesWithTabsOnSave(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		replaceWhitespacesWithTabsOnSave = setTo;

		configuration.set(CONFIG_KEY_REPLACE_WHITESPACES_WITH_TABS_ON_SAVE, replaceWhitespacesWithTabsOnSave);

		replaceWhitespacesWithTabsOnSaveItem.setSelected(replaceWhitespacesWithTabsOnSave);
	}

	private void setReorganizeImportsOnSave(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		reorganizeImportsOnSave = setTo;

		configuration.set(CONFIG_KEY_REORGANIZE_IMPORTS_ON_SAVE, reorganizeImportsOnSave);

		reorganizeImportsOnSaveItem.setSelected(reorganizeImportsOnSave);
	}

	private void setCopyOnEnter(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		copyOnEnter = setTo;

		configuration.set(CONFIG_KEY_COPY_ON_ENTER, copyOnEnter);

		copyOnEnterItem.setSelected(copyOnEnter);

		updateHighlightersOnAllTabs();
	}

	private void setTabEntireBlocks(Boolean setTo) {

		if (setTo == null) {
			setTo = true;
		}

		tabEntireBlocks = setTo;

		configuration.set(CONFIG_KEY_TAB_ENTIRE_BLOCKS, tabEntireBlocks);

		tabEntireBlocksItem.setSelected(tabEntireBlocks);

		updateHighlightersOnAllTabs();
	}

	private void showSelectedTab() {

		String selectedItem = fileListComponent.getSelectedValue();

		if (selectedItem == null) {
			return;
		}

		if (selectedItem.endsWith(CHANGE_INDICATOR)) {
			selectedItem = selectedItem.substring(0, selectedItem.length() - CHANGE_INDICATOR.length());
		}

		showTab(selectedItem);
	}

	public void setFontSize(int newSize) {

		fontSize = newSize;

		Code.setFontSizeForAllEditors(fontSize);

		configuration.set(CONFIG_KEY_FONT_SIZE, fontSize);
	}

	public int getFontSize() {
		return fontSize;
	}

	public void showTab(String name) {

		for (AugFileTab tab : augFileTabs) {
			if (tab.isItem(name)) {
				tab.show();
				setCurrentlyShownTab(tab);
				reSelectCurrentCodeLanguageItem();
			} else {
				tab.hide();
			}
		}
	}

	private void setCurrentlyShownTab(AugFileTab tab) {

		currentlyShownTab = tab;

		setUsingUTF8WithBOM(tab.isUsingUTF8BOM());

	}

	private void setUsingUTF8WithBOM(boolean useItOrNot) {

		usingUTF8WithBOM.setSelected(useItOrNot);
		usingUTF8WithoutBOM.setSelected(!useItOrNot);
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

		List<AugFileTab> tabs = new ArrayList<>();

		for (AugFileTab curTab : augFileTabs) {
			tabs.add(curTab);
		}

		// if there is no last shown tab...
		if (currentlyShownTab == null) {
			// ... show the lastly shown tab explicitly - this is fun, and the tabbed layout otherwise shows it anyway, so may as well...
			if (tabs.size() > 0) {
				setCurrentlyShownTab(tabs.get(tabs.size() - 1));
			}
		}

		Collections.sort(tabs, new Comparator<AugFileTab>() {
			public int compare(AugFileTab a, AugFileTab b) {
				// TODO :: make it configurable whether to sort by just the name or by
				// the full name (including the path)!
				// return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
				return a.getFullName().toLowerCase().compareTo(b.getFullName().toLowerCase());
			}
		});

		strAugFiles = new String[tabs.size()];

		int i = 0;

		for (AugFileTab augFileTab : tabs) {
			strAugFiles[i] = augFileTab.getName();
			if (augFileTab.hasBeenChanged()) {
				strAugFiles[i] += CHANGE_INDICATOR;
			}
			i++;
		}

		fileListComponent.setListData(strAugFiles);

		// if there still is no last shown tab (e.g. we just deleted the very last one)...
		if (currentlyShownTab == null) {
			// ... then we do not need to show or highlight any ;)
			return;
		}

		// show the last shown tab
		showTab(currentlyShownTab.getName());

		highlightTabInLeftList(currentlyShownTab.getName());
	}

	public void highlightTabInLeftList(String name) {

		int i = 0;

		for (String strAugFile : strAugFiles) {

			if (strAugFile.endsWith(CHANGE_INDICATOR)) {
				strAugFile = strAugFile.substring(0, strAugFile.length() - CHANGE_INDICATOR.length());
			}

			if (name.equals(strAugFile)) {
				fileListComponent.setSelectedIndex(i);
				break;
			}
			i++;
		}
	}

	/**
	 * Enable and disable menu items related to the current state of the application,
	 * e.g. if no CDM is loaded at all, do not enable the user to add augFiles to the
	 * current CDM, etc.
	 */
	private void reEnableDisableMenuItems() {

		boolean augFilesExist = augFileTabs.size() > 0;

		boolean fileIsSelected = currentlyShownTab != null;

		refreshFiles.setEnabled(augFilesExist);
		saveFile.setEnabled(fileIsSelected);
		saveAllFiles.setEnabled(augFilesExist);
		deleteFile.setEnabled(fileIsSelected);
		closeFile.setEnabled(fileIsSelected);
		closeAllFiles.setEnabled(augFilesExist);
		saveFilePopup.setEnabled(fileIsSelected);
		deleteFilePopup.setEnabled(fileIsSelected);
		closeFilePopup.setEnabled(fileIsSelected);
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

	private void clearAllaugFileTabs() {

		// remove old file tabs
		for (AugFileTab augFileTab : augFileTabs) {
			augFileTab.remove();
		}
		strAugFiles = new String[0];
		augFileTabs = new ArrayList<>();
		fileListComponent.setListData(strAugFiles);
		setCurrentlyShownTab(null);

		mainPanelRight.repaint();
	}

	private void reloadAllAugFileTabs() {

		if (augFileTabs != null) {
			for (AugFileTab augFileTab : augFileTabs) {
				augFileTab.remove();
			}
		}

		// update the file list on the left and load the new file tabs
		augFileTabs = new ArrayList<>();

		List<AugFile> files = augFileCtrl.getFiles();
		for (AugFile file : files) {
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

	private Record getWorkspace() {
		return augFileCtrl.getWorkspace();
	}

}
