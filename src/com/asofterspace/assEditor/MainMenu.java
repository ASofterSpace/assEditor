/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.gui.FileTab;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.utils.TextEncoding;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;


public class MainMenu {

	private MainGUI mainGUI;

	private JFrame mainFrame;

	private AugFileCtrl augFileCtrl;

	private WorkspaceGUI workspaceGUI;
	private WorkspaceSearchGUI workspaceSearchGUI;

	private JMenu switchWorkspace;
	private JMenuItem refreshFiles;
	private JMenuItem saveFile;
	private JMenuItem saveAllFiles;
	private JMenuItem deleteFile;
	private JMenuItem closeFile;
	private JMenuItem closeAllFiles;
	JCheckBoxMenuItem setLightSchemeItem;
	JCheckBoxMenuItem setDarkSchemeItem;
	JCheckBoxMenuItem removeTrailingWhitespaceOnSaveItem;
	JCheckBoxMenuItem replaceWhitespacesWithTabsOnSaveItem;
	JCheckBoxMenuItem replaceTabsWithWhitespacesOnSaveItem;
	JCheckBoxMenuItem reorganizeImportsOnSaveItem;
	JCheckBoxMenuItem removeUnusedImportsOnSaveItem;
	JCheckBoxMenuItem copyOnEnterItem;
	JCheckBoxMenuItem usingUTF8WithBOM;
	JCheckBoxMenuItem usingUTF8WithoutBOM;
	JCheckBoxMenuItem usingISOLatin1;
	JCheckBoxMenuItem tabEntireBlocksItem;
	JCheckBoxMenuItem showFilesInTreeItem;
	private JMenuItem close;
	private List<JMenuItem> codeKindItems;
	private List<JCheckBoxMenuItem> codeKindItemsCurrent;
	private List<JCheckBoxMenuItem> workspaces;


	public MainMenu(MainGUI mainGUI, JFrame mainFrame, AugFileCtrl augFileCtrl) {

		this.mainGUI = mainGUI;

		this.mainFrame = mainFrame;

		this.augFileCtrl = augFileCtrl;
	}

	public JMenuBar createMenu() {

		JMenuBar menu = new JMenuBar();

		JMenu file = new JMenu("File");
		menu.add(file);

		JMenuItem newFile = new JMenuItem("New File");
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.newFile();
			}
		});
		file.add(newFile);

		JMenuItem openFile = new JMenuItem("Open Files");
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.openFile();
			}
		});
		file.add(openFile);

		refreshFiles = new JMenuItem("Refresh All Files From Disk (Discard All Unsaved Changes)");
		refreshFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// ifAllowedToLeaveCurrentDirectory(new Callback() {
					// public void call() {
						mainGUI.reloadAllAugFileTabs();
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
				mainGUI.saveFiles(mainGUI.getCurrentTabAsList());
			}
		});
		file.add(saveFile);

		saveAllFiles = new JMenuItem("Save All Files");
		saveAllFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.saveFiles(mainGUI.getTabs());
			}
		});
		file.add(saveAllFiles);

		JMenuItem saveNotes = new JMenuItem("Save Notes");
		saveNotes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.saveNotes();
			}
		});
		file.add(saveNotes);

		file.addSeparator();

		JMenuItem renameFile = new JMenuItem("Rename Current File");
		renameFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.renameSelectedFile();
			}
		});
		file.add(renameFile);

		file.addSeparator();

		deleteFile = new JMenuItem("Delete Current File");
		deleteFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.deleteFiles(mainGUI.getCurrentTabAsList());
			}
		});
		file.add(deleteFile);

		file.addSeparator();

		closeFile = new JMenuItem("Close Current File");
		closeFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.closeFiles(mainGUI.getCurrentTabAsList());
			}
		});
		file.add(closeFile);

		closeAllFiles = new JMenuItem("Close All Files");
		closeAllFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.closeFiles(mainGUI.getTabs());
			}
		});
		file.add(closeAllFiles);

		file.addSeparator();

		JMenuItem openFolder = new JMenuItem("Open Highlighted Folder");
		openFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.openHighlightedFolder();
			}
		});
		file.add(openFolder);

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
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().undo();
				}
			}
		});
		edit.add(undoItem);

		JMenuItem redoItem = new JMenuItem("Redo");
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		redoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().redo();
				}
			}
		});
		edit.add(redoItem);

		edit.addSeparator();

		JMenuItem selectToHere = new JMenuItem("Select from Start to Here");
		selectToHere.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().selectToHere();
				}
			}
		});
		edit.add(selectToHere);

		JMenuItem selectFromHere = new JMenuItem("Select from Here to End");
		selectFromHere.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().selectFromHere();
				}
			}
		});
		edit.add(selectFromHere);

		JMenuItem selectAll = new JMenuItem("Select All");
		selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		selectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().selectAll();
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
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().duplicateCurrentLine();
				}
			}
		});
		edit.add(duplicateCurrentLine);

		JMenuItem duplicateCurrentLineCtrl = new JMenuItem("Duplicate Current Line using [Ctrl] instead");
		duplicateCurrentLineCtrl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.CTRL_MASK));
		duplicateCurrentLineCtrl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().duplicateCurrentLine();
				}
			}
		});
		edit.add(duplicateCurrentLineCtrl);

		JMenuItem deleteCurrentLine = new JMenuItem("Delete Current Line");
		deleteCurrentLine.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		deleteCurrentLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().deleteCurrentLine();
				}
			}
		});
		edit.add(deleteCurrentLine);

		JMenuItem lowCurSel = new JMenuItem("Lowcase Current Selection");
		lowCurSel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().lowCurSel();
				}
			}
		});
		edit.add(lowCurSel);

		JMenuItem upCurSel = new JMenuItem("Upcase Current Selection");
		upCurSel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().upCurSel();
				}
			}
		});
		edit.add(upCurSel);

		JMenuItem lowCurWord = new JMenuItem("Lowcase Current Word");
		lowCurWord.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		lowCurWord.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().lowCurWord();
				}
			}
		});
		edit.add(lowCurWord);

		JMenuItem upCurWord = new JMenuItem("Upcase Current Word");
		upCurWord.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
		upCurWord.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().upCurWord();
				}
			}
		});
		edit.add(upCurWord);

		JMenu indentSelection = new JMenu("Indent Current Selection");
		edit.add(indentSelection);

		JMenuItem indentByTab = new JMenuItem("By Tab");
		indentByTab.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().indentSelection("\t");
				}
			}
		});
		indentSelection.add(indentByTab);

		JMenuItem indentByTwoTabs = new JMenuItem("By 2 Tabs");
		indentByTwoTabs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().indentSelection("\t\t");
				}
			}
		});
		indentSelection.add(indentByTwoTabs);

		JMenuItem indentByFourTabs = new JMenuItem("By 4 Tabs");
		indentByFourTabs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().indentSelection("\t\t\t\t");
				}
			}
		});
		indentSelection.add(indentByFourTabs);

		JMenuItem indentBySpace = new JMenuItem("By Space");
		indentBySpace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().indentSelection(" ");
				}
			}
		});
		indentSelection.add(indentBySpace);

		JMenuItem indentByTwoSpaces = new JMenuItem("By 2 Spaces");
		indentByTwoSpaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().indentSelection("  ");
				}
			}
		});
		indentSelection.add(indentByTwoSpaces);

		JMenuItem indentByFourSpaces = new JMenuItem("By 4 Spaces");
		indentByFourSpaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().indentSelection("    ");
				}
			}
		});
		indentSelection.add(indentByFourSpaces);

		JMenu unindentSelection = new JMenu("Unindent Current Selection");
		edit.add(unindentSelection);

		JMenuItem unindentOneLevel = new JMenuItem("One Level");
		unindentOneLevel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().unindentSelection(1, false);
				}
			}
		});
		unindentSelection.add(unindentOneLevel);

		JMenuItem unindentTwoLevels = new JMenuItem("2 Levels");
		unindentTwoLevels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().unindentSelection(2, false);
				}
			}
		});
		unindentSelection.add(unindentTwoLevels);

		JMenuItem unindentFourLevels = new JMenuItem("4 Levels");
		unindentFourLevels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().unindentSelection(4, false);
				}
			}
		});
		unindentSelection.add(unindentFourLevels);

		JMenuItem unindentAllLevels = new JMenuItem("All Levels");
		unindentAllLevels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().unindentSelection(Integer.MAX_VALUE, false);
				}
			}
		});
		unindentSelection.add(unindentAllLevels);

		JMenuItem forceUnindentOneLevel = new JMenuItem("Force Unindent One Level");
		forceUnindentOneLevel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().unindentSelection(1, true);
				}
			}
		});
		unindentSelection.add(forceUnindentOneLevel);

		JMenuItem forceUnindentTwoLevels = new JMenuItem("Force Unindent 2 Levels");
		forceUnindentTwoLevels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().unindentSelection(2, true);
				}
			}
		});
		unindentSelection.add(forceUnindentTwoLevels);

		JMenuItem forceUnindentFourLevels = new JMenuItem("Force Unindent 4 Levels");
		forceUnindentFourLevels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().unindentSelection(4, true);
				}
			}
		});
		unindentSelection.add(forceUnindentFourLevels);

		edit.addSeparator();

		JMenuItem applyGit = new JMenuItem("Apply Git Markers (+/- at the beginning of lines)");
		applyGit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().applyGit();
				}
			}
		});
		edit.add(applyGit);

		JMenuItem removeDebugLines = new JMenuItem("Remove Lines Containing // DEBUG or # DEBUG");
		removeDebugLines.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().removeDebugLines();
				}
			}
		});
		edit.add(removeDebugLines);

		JMenuItem writeLineNumbers = new JMenuItem("Write Line Numbers in Front of Each Line");
		writeLineNumbers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().writeLineNumbers();
				}
			}
		});
		edit.add(writeLineNumbers);

		JMenuItem removeLineNumbers = new JMenuItem("Remove Line Numbers from Front of Each Line");
		removeLineNumbers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().removeLineNumbers();
				}
			}
		});
		edit.add(removeLineNumbers);

		JMenuItem removeTrailWhitespace = new JMenuItem("Remove Trailing Whitespace");
		removeTrailWhitespace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().removeTrailingWhitespace();
				}
			}
		});
		edit.add(removeTrailWhitespace);

		JMenuItem repWhitespacesWithTabs = new JMenuItem("Replace Leading Whitespaces with Tabs");
		repWhitespacesWithTabs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().replaceLeadingWhitespacesWithTabs();
				}
			}
		});
		edit.add(repWhitespacesWithTabs);

		JMenuItem repTabsWithWhitespaces = new JMenuItem("Replace Leading Tabs with Whitespaces");
		repTabsWithWhitespaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().replaceLeadingTabsWithWhitespaces();
				}
			}
		});
		edit.add(repTabsWithWhitespaces);

		JMenuItem reorgImports = new JMenuItem("Reorganize Imports");
		reorgImports.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().reorganizeImports();
				}
			}
		});
		edit.add(reorgImports);

		JMenuItem removeUnusedImports = new JMenuItem("Remove Unused Imports");
		removeUnusedImports.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().removeUnusedImports();
				}
			}
		});
		edit.add(removeUnusedImports);

		edit.addSeparator();

		JMenuItem sortDocumentAlph = new JMenuItem("Sort Entire Document Alphabetically");
		sortDocumentAlph.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().sortDocumentAlphabetically();
				}
			}
		});
		edit.add(sortDocumentAlph);

		JMenuItem sortSelectedLinesAlph = new JMenuItem("Sort Selected Lines Alphabetically");
		sortSelectedLinesAlph.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().sortSelectedLinesAlphabetically();
				}
			}
		});
		edit.add(sortSelectedLinesAlph);

		JMenuItem sortSelectedStringsAlph = new JMenuItem("Sort Selected Strings (\"foo\", \"bar\") Alphabetically");
		sortSelectedStringsAlph.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().sortSelectedStringsAlphabetically();
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
				mainGUI.showSearchBar();
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


		JMenu operations = new JMenu("Operations");
		menu.add(operations);

		JMenuItem count100Up = new JMenuItem("Count 100 Up");
		count100Up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().count100Up();
				}
			}
		});
		operations.add(count100Up);

		operations.addSeparator();

		JMenuItem delAllContText = new JMenuItem("Delete All Lines Containing Text In Search Field");
		delAllContText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().deleteAllLinesContainingText(mainGUI.getSearchFieldText(), false);
				}
			}
		});
		operations.add(delAllContText);

		JMenuItem delAllStartText = new JMenuItem("Delete All Lines Starting With Text In Search Field");
		delAllStartText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().deleteAllLinesStartingWithText(mainGUI.getSearchFieldText(), false);
				}
			}
		});
		operations.add(delAllStartText);

		JMenuItem delAllNotContText = new JMenuItem("Delete All Lines Not Containing Text In Search Field");
		delAllNotContText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().deleteAllLinesContainingText(mainGUI.getSearchFieldText(), true);
				}
			}
		});
		operations.add(delAllNotContText);

		JMenuItem delAllNotStartText = new JMenuItem("Delete All Lines Not Starting With Text In Search Field");
		delAllNotStartText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().deleteAllLinesStartingWithText(mainGUI.getSearchFieldText(), true);
				}
			}
		});
		operations.add(delAllNotStartText);


		JMenu encodings = new JMenu("Encodings");
		menu.add(encodings);

		usingUTF8WithoutBOM = new JCheckBoxMenuItem("Default UTF-8 (without BOM)");
		usingUTF8WithoutBOM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.getCurrentTab().setEncoding(TextEncoding.UTF8_WITHOUT_BOM);
				setEncoding(TextEncoding.UTF8_WITHOUT_BOM);
			}
		});
		usingUTF8WithoutBOM.setSelected(false);
		encodings.add(usingUTF8WithoutBOM);

		usingUTF8WithBOM = new JCheckBoxMenuItem("UTF-8 with BOM");
		usingUTF8WithBOM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.getCurrentTab().setEncoding(TextEncoding.UTF8_WITH_BOM);
				setEncoding(TextEncoding.UTF8_WITH_BOM);
			}
		});
		usingUTF8WithBOM.setSelected(false);
		encodings.add(usingUTF8WithBOM);

		usingISOLatin1 = new JCheckBoxMenuItem("ISO-Latin-1");
		usingISOLatin1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.getCurrentTab().setEncoding(TextEncoding.ISO_LATIN_1);
				setEncoding(TextEncoding.ISO_LATIN_1);
			}
		});
		usingISOLatin1.setSelected(false);
		encodings.add(usingISOLatin1);

		encodings.addSeparator();

		JMenuItem allUsingUTF8WithoutBOM = new JMenuItem("Set All Files to Default UTF-8 (without BOM)");
		allUsingUTF8WithoutBOM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (AugFileTab tab : mainGUI.getTabs()) {
					tab.setEncoding(TextEncoding.UTF8_WITHOUT_BOM);
				}
				setEncoding(TextEncoding.UTF8_WITHOUT_BOM);
			}
		});
		encodings.add(allUsingUTF8WithoutBOM);

		JMenuItem allUsingUTF8WithBOM = new JMenuItem("Set All Files to UTF-8 with BOM");
		allUsingUTF8WithBOM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (AugFileTab tab : mainGUI.getTabs()) {
					tab.setEncoding(TextEncoding.UTF8_WITH_BOM);
				}
				setEncoding(TextEncoding.UTF8_WITH_BOM);
			}
		});
		encodings.add(allUsingUTF8WithBOM);

		JMenuItem allUsingISOLatin1 = new JMenuItem("Set All Files to ISO-Latin-1");
		allUsingISOLatin1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (AugFileTab tab : mainGUI.getTabs()) {
					tab.setEncoding(TextEncoding.ISO_LATIN_1);
				}
				setEncoding(TextEncoding.ISO_LATIN_1);
			}
		});
		encodings.add(allUsingISOLatin1);


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
					mainGUI.setOrUnsetCurrentCodeLanguage(ck);
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
				mainGUI.setOrUnsetCurrentCodeLanguage(null);
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
					mainGUI.setOrUnsetAllCodeLanguages(ck);
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
				mainGUI.setOrUnsetAllCodeLanguages(null);
			}
		});
		language.add(ckDefault);

		JMenu scheme = new JMenu("Editor Color Scheme");
		settings.add(scheme);
		setLightSchemeItem = new JCheckBoxMenuItem("Light");
		setLightSchemeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setScheme(GuiUtils.LIGHT_SCHEME);
			}
		});
		scheme.add(setLightSchemeItem);
		setDarkSchemeItem = new JCheckBoxMenuItem("Dark");
		setDarkSchemeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setScheme(GuiUtils.DARK_SCHEME);
			}
		});
		scheme.add(setDarkSchemeItem);

		JMenu fontSizeItem = new JMenu("Editor Font Size");
		settings.add(fontSizeItem);
		JMenuItem fontSizePlusItem = new JMenuItem("Increase");
		fontSizePlusItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setFontSize(mainGUI.getFontSize() + 1);
			}
		});
		fontSizeItem.add(fontSizePlusItem);
		JMenuItem fontSizeDefaultItem = new JMenuItem("Reset to Default");
		fontSizeDefaultItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setFontSize(MainGUI.DEFAULT_FONT_SIZE);
			}
		});
		fontSizeItem.add(fontSizeDefaultItem);
		JMenuItem fontSizeMinusItem = new JMenuItem("Decrease");
		fontSizeMinusItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setFontSize(mainGUI.getFontSize() - 1);
			}
		});
		fontSizeItem.add(fontSizeMinusItem);

		showFilesInTreeItem = new JCheckBoxMenuItem("Show Files as Tree");
		showFilesInTreeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.toggleShowFilesInTree();
			}
		});
		showFilesInTreeItem.setSelected(mainGUI.getShowFilesInTree());
		settings.add(showFilesInTreeItem);

		settings.addSeparator();

		removeTrailingWhitespaceOnSaveItem = new JCheckBoxMenuItem("Remove Trailing Whitespace on Save");
		removeTrailingWhitespaceOnSaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setRemoveTrailingWhitespaceOnSave(!mainGUI.getRemoveTrailingWhitespaceOnSave());
			}
		});
		mainGUI.setRemoveTrailingWhitespaceOnSave(mainGUI.getRemoveTrailingWhitespaceOnSave());
		settings.add(removeTrailingWhitespaceOnSaveItem);

		replaceWhitespacesWithTabsOnSaveItem = new JCheckBoxMenuItem("Replace Leading Whitespaces with Tabs on Save");
		replaceWhitespacesWithTabsOnSaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setReplaceWhitespacesWithTabsOnSave(!mainGUI.getReplaceWhitespacesWithTabsOnSave());
			}
		});
		settings.add(replaceWhitespacesWithTabsOnSaveItem);

		replaceTabsWithWhitespacesOnSaveItem = new JCheckBoxMenuItem("Replace Leading Tabs with Whitespaces on Save");
		replaceTabsWithWhitespacesOnSaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setReplaceTabsWithWhitespacesOnSave(!mainGUI.getReplaceTabsWithWhitespacesOnSave());
			}
		});
		settings.add(replaceTabsWithWhitespacesOnSaveItem);

		mainGUI.setReplaceWhitespacesWithTabsOnSave(mainGUI.getReplaceWhitespacesWithTabsOnSave());
		mainGUI.setReplaceTabsWithWhitespacesOnSave(mainGUI.getReplaceTabsWithWhitespacesOnSave());

		reorganizeImportsOnSaveItem = new JCheckBoxMenuItem("Reorganize Imports on Save");
		reorganizeImportsOnSaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setReorganizeImportsOnSave(!mainGUI.getReorganizeImportsOnSave());
			}
		});
		mainGUI.setReorganizeImportsOnSave(mainGUI.getReorganizeImportsOnSave());
		settings.add(reorganizeImportsOnSaveItem);

		removeUnusedImportsOnSaveItem = new JCheckBoxMenuItem("Remove Unused Imports on Save");
		removeUnusedImportsOnSaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setRemoveUnusedImportsOnSave(!mainGUI.getRemoveUnusedImportsOnSave());
			}
		});
		mainGUI.setRemoveUnusedImportsOnSave(mainGUI.getRemoveUnusedImportsOnSave());
		settings.add(removeUnusedImportsOnSaveItem);

		JMenuItem toggleAllSwitchesInGroup = new JMenuItem("Toggle All 'On Save' Switches On / Off");
		toggleAllSwitchesInGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean toggleTo = !mainGUI.getRemoveTrailingWhitespaceOnSave();
				mainGUI.setRemoveTrailingWhitespaceOnSave(toggleTo);
				mainGUI.setReplaceWhitespacesWithTabsOnSave(toggleTo);
				mainGUI.setReplaceTabsWithWhitespacesOnSave(false);
				mainGUI.setReorganizeImportsOnSave(toggleTo);
				mainGUI.setRemoveUnusedImportsOnSave(toggleTo);
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
				mainGUI.setTabEntireBlocks(!mainGUI.getTabEntireBlocks());
			}
		});
		mainGUI.setTabEntireBlocks(mainGUI.getTabEntireBlocks());
		settings.add(tabEntireBlocksItem);

		JMenu window = new JMenu("Window");

		JMenuItem toggleNoteArea = new JMenuItem("Toggle Note Area");
		toggleNoteArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.toggleNoteArea();
			}
		});
		window.add(toggleNoteArea);

		JMenuItem toggleSearchBar = new JMenuItem("Toggle Search Bar");
		toggleSearchBar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.toggleSearchBar();
			}
		});
		window.add(toggleSearchBar);

		menu.add(window);

		JMenu huh = new JMenu("?");

		JMenuItem openBackupPath = new JMenuItem("Open Backup Path");
		openBackupPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtils.openFolder(AugFileTab.getBackupPath());
			}
		});
		huh.add(openBackupPath);

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

		mainFrame.setJMenuBar(menu);

		return menu;
	}

	public void refreshWorkspaces() {

		switchWorkspace.removeAll();

		workspaces = new ArrayList<>();
		List<String> workspaceNames = augFileCtrl.getWorkspaces();
		for (int i = 0; i < workspaceNames.size(); i++) {
			final String workspaceName = workspaceNames.get(i);
			if (i + 1 < workspaceNames.size()) {
				final String nextWorkspaceName = workspaceNames.get(i + 1);
				int wNIndex = workspaceName.indexOf(" ");
				int nextWNIndex = nextWorkspaceName.indexOf(" ");
				// we want to check that " " was found (>= 0) and is not in the first position (therefore > 0)
				if ((wNIndex > 0) && (nextWNIndex > 0)) {
					String workspaceNamePrefix = workspaceName.substring(0, wNIndex);
					String nextWorkspaceNamePrefix = nextWorkspaceName.substring(0, nextWNIndex);
					if (workspaceNamePrefix.equals(nextWorkspaceNamePrefix)) {
						// actually group the workspaces together!
						JMenu submenu = new JMenu(workspaceNamePrefix);

						for (; i < workspaceNames.size(); i++) {
							final String innerWorkspaceName = workspaceNames.get(i);
							if (!innerWorkspaceName.startsWith(workspaceNamePrefix + " ")) {
								break;
							}
							final JCheckBoxMenuItem workspace = new JCheckBoxMenuItem(innerWorkspaceName);
							workspace.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									mainGUI.switchToWorkspace(workspace, innerWorkspaceName);
								}
							});
							submenu.add(workspace);
							workspaces.add(workspace);
						}

						// we break the inner for, then continue the outer for,
						// so we do a ++ before the next outer for loop,
						// so we do a -- here to undo that
						// (or think about it this way: we have two nested loops,
						// EACH doing i++, but we only want one ++, so we do a --
						// to counterbalance one of the ++)
						i--;

						switchWorkspace.add(submenu);
						continue;
					}
				}
			}
			final JCheckBoxMenuItem workspace = new JCheckBoxMenuItem(workspaceName);
			workspace.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mainGUI.switchToWorkspace(workspace, workspaceName);
				}
			});
			switchWorkspace.add(workspace);
			workspaces.add(workspace);
		}

		for (JCheckBoxMenuItem workspace : workspaces) {
			if (workspace.getText().equals(augFileCtrl.getWorkspaceName())) {
				workspace.setSelected(true);
			} else {
				workspace.setSelected(false);
			}
		}

		switchWorkspace.addSeparator();

		JMenuItem editWorkspaces = new JMenuItem("Edit Workspaces");
		editWorkspaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// show the workspace editing GUI
				if (workspaceGUI == null) {
					workspaceGUI = new WorkspaceGUI(mainGUI, augFileCtrl, MainMenu.this);
				}
				workspaceGUI.show();
			}
		});
		switchWorkspace.add(editWorkspaces);
	}

	public void uncheckWorkspaces() {
		for (JCheckBoxMenuItem workspace : workspaces) {
			workspace.setSelected(false);
		}
	}

	public void setRemoveUnusedImportsOnSave(boolean setTo) {
		removeUnusedImportsOnSaveItem.setSelected(setTo);
	}

	public void reEnableDisableMenuItems(boolean augFilesExist, boolean fileIsSelected) {
		refreshFiles.setEnabled(augFilesExist);
		saveFile.setEnabled(fileIsSelected);
		saveAllFiles.setEnabled(augFilesExist);
		deleteFile.setEnabled(fileIsSelected);
		closeFile.setEnabled(fileIsSelected);
		closeAllFiles.setEnabled(augFilesExist);
	}

	public void setEncoding(TextEncoding encoding) {

		usingUTF8WithBOM.setSelected(encoding == TextEncoding.UTF8_WITH_BOM);
		usingUTF8WithoutBOM.setSelected(encoding == TextEncoding.UTF8_WITHOUT_BOM);
		usingISOLatin1.setSelected(encoding == TextEncoding.ISO_LATIN_1);
	}

	public void reSelectCurrentCodeLanguageItem(String currentCodeLanguageStr) {

		for (JCheckBoxMenuItem codeKindItem : codeKindItemsCurrent) {
			codeKindItem.setSelected(false);

			if (codeKindItem.getText().equals(currentCodeLanguageStr)) {
				codeKindItem.setSelected(true);
			}
		}
	}

	public void reSelectSchemeItems() {

		setLightSchemeItem.setSelected(false);
		setDarkSchemeItem.setSelected(false);

		switch (mainGUI.getScheme()) {

			case GuiUtils.LIGHT_SCHEME:
				setLightSchemeItem.setSelected(true);
				break;

			case GuiUtils.DARK_SCHEME:
				setDarkSchemeItem.setSelected(true);
				break;
		}
	}

	private void showSearchWindow() {

		// show the workspace search GUI
		if (workspaceSearchGUI == null) {
			workspaceSearchGUI = new WorkspaceSearchGUI(mainGUI, mainFrame, augFileCtrl, MainMenu.this);
		}

		workspaceSearchGUI.show();

		workspaceSearchGUI.firstSearch();
	}

}