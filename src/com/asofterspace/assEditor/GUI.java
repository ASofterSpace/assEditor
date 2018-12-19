/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.gui.MainWindow;
import com.asofterspace.toolbox.gui.ProgressDialog;
import com.asofterspace.toolbox.Utils;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.utils.ProgressIndicator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


public class GUI extends MainWindow {

	private AugFileCtrl augFileCtrl;
	
	private JPanel mainPanelRight;

	private AugFileTab currentlyShownTab;

	// on the left hand side, we add this string to indicate that the file has changed
	private final static String CHANGE_INDICATOR = " *";
	
	private final static String CONFIG_KEY_LAST_DIRECTORY = "lastDirectory";
	private final static String CONFIG_KEY_CODE_KIND = "currentCodeKind";
	private final static String CONFIG_KEY_SCHEME = "scheme";
	final static String LIGHT_SCHEME = "light";
	final static String DARK_SCHEME = "dark";

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
	private JMenuItem close;
	private List<JCheckBoxMenuItem> codeKindItems;

	private List<AugFileTab> augFileTabs;

	private ConfigFile configuration;
	private JList<String> fileListComponent;
	private JPopupMenu fileListPopup;
	private String[] strAugFiles;
	
	private CodeKind currentCodeKind = null;
	String currentScheme;


	public GUI(ConfigFile config) {

		configuration = config;

		strAugFiles = new String[0];

		augFileTabs = new ArrayList<>();
		
		augFileCtrl = new AugFileCtrl(configuration);
		
		String currentCodeKindStr = configuration.getValue(CONFIG_KEY_CODE_KIND);
		currentCodeKind = CodeKind.getFromString(currentCodeKindStr);
		
		currentScheme = configuration.getValue(CONFIG_KEY_SCHEME);
		
		if (currentScheme == null) {
			currentScheme = LIGHT_SCHEME;
		}
	}

	@Override
	public void run() {

		super.create();

		GuiUtils.maximizeWindow(mainFrame);

		// Add content to the window
		createMenu(mainFrame);

		createPopupMenu(mainFrame);

		createMainPanel(mainFrame);

		configureGUI();

		refreshTitleBar();

		reEnableDisableMenuItems();

		reSelectSchemeItems();
		
		super.show();
		
		reloadAllAugFileTabs();
	}

	private JMenuBar createMenu(JFrame parent) {

		JMenuBar menu = new JMenuBar();

		// TODO :: add undo / redo (for basically any action, but first of all of course for the editor)

		JMenu file = new JMenu("File");
		menu.add(file);
		
		JMenuItem openFile = new JMenuItem("Open");
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openFile();
			}
		});
		file.add(openFile);
		
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
		refreshAugFiles = new JMenuItem("Refresh All AugFiles From Shared Disk");
		refreshAugFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ifAllowedToLeaveCurrentDirectory(new Callback() {
					public void call() {
						reloadAllAugFileTabs();
					}
				});
			}
		});
		file.add(refreshAugFiles);
		file.addSeparator();
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
		close = new JMenuItem("Close");
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
		
		JMenu language = new JMenu("Code Language");
		menu.add(language);
		codeKindItems = new ArrayList<>();
		for (CodeKind ck : CodeKind.values()) {
			JCheckBoxMenuItem ckItem = new JCheckBoxMenuItem(ck.toString());
			ckItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setOrUnsetCurrentCodeKind(ck);
				}
			});
			ckItem.setSelected(ck.equals(currentCodeKind));
			language.add(ckItem);
			codeKindItems.add(ckItem);
		}
		
		JMenu settings = new JMenu("Settings");
		setLightSchemeItem = new JCheckBoxMenuItem("Light Scheme");
		setLightSchemeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setScheme(LIGHT_SCHEME);
			}
		});
		settings.add(setLightSchemeItem);
		setDarkSchemeItem = new JCheckBoxMenuItem("Dark Scheme");
		setDarkSchemeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setScheme(DARK_SCHEME);
			}
		});
		settings.add(setDarkSchemeItem);
		menu.add(settings);

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

		mainPanel.add(augFileListScroller, new Arrangement(0, 0, 0.2, 1.0));

		mainPanel.add(gapPanel, new Arrangement(1, 0, 0.0, 0.0));

	    mainPanel.add(mainPanelRight, new Arrangement(2, 0, 1.0, 1.0));

		parent.add(mainPanel, BorderLayout.CENTER);

	    return mainPanel;
	}
	
	private void openFile() {

		// TODO :: de-localize the JFileChooser (by default it seems localized, which is inconsistent when the rest of the program is in English...)
		// (while you're at it, make Öffnen into Save for the save dialog, but keep it as Open for the open dialog... ^^)
		JFileChooser augFilePicker;

		String lastDirectory = configuration.getValue(CONFIG_KEY_LAST_DIRECTORY);

		if ((lastDirectory != null) && !"".equals(lastDirectory)) {
			augFilePicker = new JFileChooser(new java.io.File(lastDirectory));
		} else {
			augFilePicker = new JFileChooser();
		}

		augFilePicker.setDialogTitle("Open a CDM working directory");
		augFilePicker.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int result = augFilePicker.showOpenDialog(mainFrame);

		switch (result) {

			case JFileChooser.APPROVE_OPTION:

				// load the CDM files
				configuration.set(CONFIG_KEY_LAST_DIRECTORY, augFilePicker.getCurrentDirectory().getAbsolutePath());
				File fileToOpen = new File(augFilePicker.getSelectedFile());
				augFileCtrl.loadAnotherFile(fileToOpen);
				reloadAllAugFileTabs();
				break;

			case JFileChooser.CANCEL_OPTION:
				// cancel was pressed... do nothing for now
				break;
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

	private void setOrUnsetCurrentCodeKind(CodeKind ck) {

		String currentCodeKindStr = null;
		
		if (ck.equals(currentCodeKind)) {
			currentCodeKind = null;
		} else {
			currentCodeKind = ck;
			currentCodeKindStr = currentCodeKind.toString();
		}

		for (JCheckBoxMenuItem codeKindItem : codeKindItems) {
			codeKindItem.setSelected(false);
			
			if (codeKindItem.getText().equals(currentCodeKindStr)) {
				codeKindItem.setSelected(true);
			}
		}
		
		configuration.set(CONFIG_KEY_CODE_KIND, currentCodeKindStr);

		reloadAllAugFileTabs();
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

		reloadAllAugFileTabs();
	}

	private void showSelectedTab() {

		String selectedItem = (String) fileListComponent.getSelectedValue();

		if (selectedItem == null) {
			return;
		}

		if (selectedItem.endsWith(CHANGE_INDICATOR)) {
			selectedItem = selectedItem.substring(0, selectedItem.length() - CHANGE_INDICATOR.length());
		}

		showTab(selectedItem);
	}

	public void showTab(String name) {

		for (AugFileTab tab : augFileTabs) {
			if (tab.isItem(name)) {
				tab.show();
				currentlyShownTab = tab;
			} else {
				tab.hide();
			}
		}
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
	private void openLoadAugFilesDialog() {

		ifAllowedToLeaveCurrentDirectory(new Callback() {
			public void call() {
				// TODO :: de-localize the JFileChooser (by default it seems localized, which is inconsistent when the rest of the program is in English...)
				// (while you're at it, make Öffnen into Save for the save dialog, but keep it as Open for the open dialog... ^^)
				JFileChooser augFilePicker;

				String lastDirectory = configuration.getValue(CONFIG_KEY_LAST_DIRECTORY);

				if ((lastDirectory != null) && !"".equals(lastDirectory)) {
					augFilePicker = new JFileChooser(new java.io.File(lastDirectory));
				} else {
					augFilePicker = new JFileChooser();
				}

				// TODO :: also allow opening a CDM zipfile

				augFilePicker.setDialogTitle("Open a CDM working directory");
				augFilePicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int result = augFilePicker.showOpenDialog(mainFrame);

				switch (result) {

					case JFileChooser.APPROVE_OPTION:

						clearAllaugFileTabs();

						// load the CDM files
						configuration.set(CONFIG_KEY_LAST_DIRECTORY, augFilePicker.getCurrentDirectory().getAbsolutePath());
						final Directory cdmDir = new Directory(augFilePicker.getSelectedFile());

						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									// add a progress bar (which is especially helpful when the CDM contains no augFiles
									// so the main view stays empty after loading a CDM!)
									ProgressDialog progress = new ProgressDialog("Loading the CDM directory...");
									augFileCtrl.loadCdmDirectory(cdmDir, progress);
								} catch (AttemptingEmfException | CdmLoadingException e) {
									JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "CDM Loading Failed", JOptionPane.ERROR_MESSAGE);
								}

								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										reloadAllAugFileTabs();
									}
								});
							}
						}).start();

						break;

					case JFileChooser.CANCEL_OPTION:
						// cancel was pressed... do nothing for now
						break;
				}
			}
		});
	}
	*/

	private void saveAugFiles() {

		/*
		if (!augFileCtrl.hasDirectoryBeenLoaded()) {
			JOptionPane.showMessageDialog(mainFrame, "The augFiles cannot be saved as no directory has been opened.", "Sorry", JOptionPane.ERROR_MESSAGE);
			return;
		}
		*/

		// TODO :: add validation step here, in which we validate that all augFiles are assigned to activities, and if they are not,
		// then we ask the user explicitly whether we should really save the augFiles in the current state or not
		// (for this, we can call augFileCtrl.checkValidity())

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

				refreshTitleBar();

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
			currentlyShownTab = new AugFileTab(mainPanelRight, augFilesAfter.iterator().next(), this, augFileCtrl, currentCodeKind);

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
				currentlyShownTab = tab;
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

		currentlyShownTab = null;

		// remove file from the left hand side
		regenerateAugFileList();

		reEnableDisableMenuItems();

		return true;
	}
	*/

	/**
	 * Regenerate the file list on the left hand side based on the augFileTabs list,
	 * and (if at least one file exists), select and open the current tab or, if it
	 * is null, the first one
	 */
	public void regenerateAugFileList() {

		List<AugFileTab> tabs = new ArrayList<>();

		for (AugFileTab curTab : augFileTabs) {
			tabs.add(curTab);
		}

		Collections.sort(tabs, new Comparator<AugFileTab>() {
			public int compare(AugFileTab a, AugFileTab b) {
				return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
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

		// if there is no last shown tab...
		if (currentlyShownTab == null) {
			// ... show the first tab explicitly - this is fun, and the tabbed layout otherwise shows it anyway, so may as well...
			if (tabs.size() > 0) {
				currentlyShownTab = tabs.get(0);
			}
		}

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

		/*
		boolean dirLoaded = augFileCtrl.hasDirectoryBeenLoaded();
		
		boolean companiesExist = augFileCtrl.getCompanies().size() > 0;

		boolean augFilesExist = augFileTabs.size() > 0;

		boolean fileIsSelected = currentlyShownTab != null;

		// enabled and disable menu items according to the state of the application
		refreshAugFiles.setEnabled(dirLoaded);
		saveAugFiles.setEnabled(dirLoaded);
		// saveAugFilesAs.setEnabled(dirLoaded);
		addPerson.setEnabled(companiesExist);
		addPersonPopup.setEnabled(companiesExist);
		addCompany.setEnabled(dirLoaded);
		addCompanyPopup.setEnabled(dirLoaded);
		renameCurAugFile.setEnabled(AugFileIsSelected);
		renameCurAugFilePopup.setEnabled(AugFileIsSelected);
		deleteCurAugFile.setEnabled(AugFileIsSelected);
		deleteCurAugFilePopup.setEnabled(AugFileIsSelected);
		*/
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
		currentlyShownTab = null;

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
			augFileTabs.add(new AugFileTab(mainPanelRight, file, this, augFileCtrl, currentCodeKind));
		}

		regenerateAugFileList();

		reEnableDisableMenuItems();

		refreshTitleBar();
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

}
