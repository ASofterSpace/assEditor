/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.coders.Base64Decoder;
import com.asofterspace.toolbox.coders.Base64Encoder;
import com.asofterspace.toolbox.coders.BinaryDecoder;
import com.asofterspace.toolbox.coders.BinaryEncoder;
import com.asofterspace.toolbox.coders.EgsccPrettifier;
import com.asofterspace.toolbox.coders.HexDecoder;
import com.asofterspace.toolbox.coders.HexEncoder;
import com.asofterspace.toolbox.coders.HtmlDecoder;
import com.asofterspace.toolbox.coders.HtmlEncoder;
import com.asofterspace.toolbox.coders.MorseDecoder;
import com.asofterspace.toolbox.coders.MorseEncoder;
import com.asofterspace.toolbox.coders.RomanNumeralDecoder;
import com.asofterspace.toolbox.coders.RomanNumeralEncoder;
import com.asofterspace.toolbox.coders.UrlDecoder;
import com.asofterspace.toolbox.coders.UrlEncoder;
import com.asofterspace.toolbox.coders.Utf8Decoder;
import com.asofterspace.toolbox.coders.Utf8Encoder;
import com.asofterspace.toolbox.coders.UuidEncoderDecoder;
import com.asofterspace.toolbox.gui.FileTab;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.HTML;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.io.XML;
import com.asofterspace.toolbox.utils.SortOrder;
import com.asofterspace.toolbox.utils.StringModifier;
import com.asofterspace.toolbox.utils.StrUtils;
import com.asofterspace.toolbox.utils.TextEncoding;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;


public class MainMenu {

	private MainGUI mainGUI;

	private JFrame mainFrame;

	private AugFileCtrl augFileCtrl;

	private boolean standalone;

	private WorkspaceGUI workspaceGUI;
	private WorkspaceExportGUI workspaceExportGUI;
	private WorkspaceSearchGUI workspaceSearchGUI;
	private NewProjectGUI newProjectGUI;

	private JMenu switchWorkspace;
	private JMenuItem refreshFiles;
	private JMenuItem saveFile;
	private JMenuItem saveAllFiles;
	private JMenuItem deleteFile;
	private JMenuItem closeFile;
	private JMenuItem closeAllFiles;
	JCheckBoxMenuItem setLightSchemeItem;
	JCheckBoxMenuItem setDarkSchemeItem;
	JCheckBoxMenuItem useAntiAliasingItem;
	JRadioButtonMenuItem defaultIndentTab;
	JRadioButtonMenuItem defaultIndent2Spaces;
	JRadioButtonMenuItem defaultIndent4Spaces;
	JCheckBoxMenuItem removeTrailingWhitespaceOnSaveItem;
	JCheckBoxMenuItem replaceWhitespacesWithTabsOnSaveItem;
	JCheckBoxMenuItem replaceTabsWithWhitespacesOnSaveItem;
	JCheckBoxMenuItem addMissingImportsOnSaveItem;
	JCheckBoxMenuItem reorganizeImportsOnSaveItem;
	JCheckBoxMenuItem reorganizeImportsOnSaveCompatibleItem;
	JCheckBoxMenuItem removeUnusedImportsOnSaveItem;
	JCheckBoxMenuItem copyOnEnterItem;
	JCheckBoxMenuItem usingUTF8WithBOM;
	JCheckBoxMenuItem usingUTF8WithoutBOM;
	JCheckBoxMenuItem usingISOLatin1;
	JCheckBoxMenuItem tabEntireBlocksItem;
	JCheckBoxMenuItem proposeTokenAutoCompleteItem;
	JCheckBoxMenuItem showFilesInTreeItem;
	private JMenuItem close;
	private List<JMenuItem> codeKindItems;
	private List<JCheckBoxMenuItem> codeKindItemsCurrent;
	private List<JCheckBoxMenuItem> workspaces;


	public MainMenu(MainGUI mainGUI, JFrame mainFrame, AugFileCtrl augFileCtrl, boolean standalone) {

		this.mainGUI = mainGUI;

		this.mainFrame = mainFrame;

		this.augFileCtrl = augFileCtrl;

		this.standalone = standalone;
	}

	public JMenuBar createMenu() {

		JMenuBar menu = new JMenuBar();

		JMenu file = new JMenu("File");
		menu.add(file);

		if (!standalone) {
			JMenu newProject = new JMenu("New Project");
			file.add(newProject);

			JMenuItem newAssJavaProject = new JMenuItem("New A Softer Space Java Project");
			newAssJavaProject.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (newProjectGUI == null) {
						newProjectGUI = new NewProjectGUI(mainGUI, augFileCtrl);
					}
					newProjectGUI.show();
				}
			});
			newProject.add(newAssJavaProject);

			JMenuItem newFile = new JMenuItem("New File");
			newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
			newFile.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mainGUI.newFile();
				}
			});
			file.add(newFile);

			JMenuItem copySelectedFile = new JMenuItem("Copy Selected File");
			copySelectedFile.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mainGUI.copyFiles(mainGUI.getHighlightedTabs());
				}
			});
			file.add(copySelectedFile);
		}

		JMenuItem openFile = new JMenuItem("Open File" + (standalone ? "" : "s"));
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.showOpenFileDialog();
			}
		});
		file.add(openFile);

		if (!standalone) {
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
		}

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

		if (!standalone) {
			saveAllFiles = new JMenuItem("Save All Files");
			saveAllFiles.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mainGUI.saveFiles(mainGUI.getTabs());
				}
			});
			file.add(saveAllFiles);
		}

		JMenuItem saveNotes = new JMenuItem("Save Notes");
		saveNotes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.saveNotes();
			}
		});
		file.add(saveNotes);

		file.addSeparator();

		if (!standalone) {
			JMenuItem renameFile = new JMenuItem("Rename Current File");
			renameFile.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mainGUI.renameSelectedFile();
				}
			});
			file.add(renameFile);

			file.addSeparator();
		}

		deleteFile = new JMenuItem("Delete Current File");
		deleteFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.deleteFiles(mainGUI.getCurrentTabAsList());
			}
		});
		file.add(deleteFile);

		file.addSeparator();

		if (!standalone) {
			closeFile = new JMenuItem("Close Current File");
			closeFile.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mainGUI.closeFiles(mainGUI.getCurrentTabAsList());
				}
			});
			file.add(closeFile);

			JMenuItem closeAllRemovedFiles = new JMenuItem("Close All Non-Existing Files");
			closeAllRemovedFiles.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					List<AugFileTab> removedFiles = new ArrayList<>();
					for (AugFileTab tab : mainGUI.getTabs()) {
						if (tab.isMissing()) {
							removedFiles.add(tab);
						}
					}
					mainGUI.closeFiles(removedFiles);
				}
			});
			file.add(closeAllRemovedFiles);

			closeAllFiles = new JMenuItem("Close All Files");
			closeAllFiles.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mainGUI.closeFiles(mainGUI.getTabs());
				}
			});
			file.add(closeAllFiles);

			file.addSeparator();
		}

		JMenuItem openFolder = new JMenuItem("Open " + (standalone ? "Current" : "Highlighted") + " Folder");
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


		if (!standalone) {
			switchWorkspace = new JMenu("Workspace");
			menu.add(switchWorkspace);

			refreshWorkspaces();
		}


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

		JMenuItem upLowCurSel = new JMenuItem("Up+lowcase Current Selection");
		upLowCurSel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().upLowCurSel();
				}
			}
		});
		edit.add(upLowCurSel);

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

		JMenuItem upLowCurWord = new JMenuItem("Up+lowcase Current Word");
		upLowCurWord.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		upLowCurWord.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().upLowCurWord();
				}
			}
		});
		edit.add(upLowCurWord);

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

		JMenuItem indentByEightTabs = new JMenuItem("By 8 Tabs");
		indentByEightTabs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().indentSelection("\t\t\t\t\t\t\t\t");
				}
			}
		});
		indentSelection.add(indentByEightTabs);

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

		JMenuItem indentByEightSpaces = new JMenuItem("By 8 Spaces");
		indentByEightSpaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().indentSelection("        ");
				}
			}
		});
		indentSelection.add(indentByEightSpaces);

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

		JMenuItem unindentEightLevels = new JMenuItem("8 Levels");
		unindentEightLevels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().unindentSelection(8, false);
				}
			}
		});
		unindentSelection.add(unindentEightLevels);

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

		JMenuItem forceUnindentEightLevels = new JMenuItem("Force Unindent 8 Levels");
		forceUnindentEightLevels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().unindentSelection(8, true);
				}
			}
		});
		unindentSelection.add(forceUnindentEightLevels);

		edit.addSeparator();

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

		JMenuItem addMissingImports = new JMenuItem("Add Missing Imports");
		addMissingImports.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().addMissingImports();
				}
			}
		});
		edit.add(addMissingImports);

		JMenuItem reorgImports = new JMenuItem("Reorganize Imports (Normal)");
		reorgImports.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().reorganizeImports();
				}
			}
		});
		edit.add(reorgImports);

		JMenuItem reorgImportsCompatible = new JMenuItem("Reorganize Imports (Compatibile with Wonky IDEs)");
		reorgImportsCompatible.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().reorganizeImportsCompatible();
				}
			}
		});
		edit.add(reorgImportsCompatible);

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
					mainGUI.getCurrentTab().sortDocument(SortOrder.ALPHABETICAL);
				}
			}
		});
		edit.add(sortDocumentAlph);

		JMenuItem sortDocumentNum = new JMenuItem("Sort Entire Document Numerically");
		sortDocumentNum.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().sortDocument(SortOrder.NUMERICAL);
				}
			}
		});
		edit.add(sortDocumentNum);

		JMenuItem reverseDocument = new JMenuItem("Reverse Entire Document");
		reverseDocument.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().sortDocument(SortOrder.REVERSE);
				}
			}
		});
		edit.add(reverseDocument);

		JMenuItem sortSelectedLinesAlph = new JMenuItem("Sort Selected Lines Alphabetically");
		sortSelectedLinesAlph.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().sortSelectedLines(SortOrder.ALPHABETICAL);
				}
			}
		});
		edit.add(sortSelectedLinesAlph);

		JMenuItem sortSelectedLinesNum = new JMenuItem("Sort Selected Lines Numerically");
		sortSelectedLinesNum.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().sortSelectedLines(SortOrder.NUMERICAL);
				}
			}
		});
		edit.add(sortSelectedLinesNum);

		JMenuItem reverseSelectedLines = new JMenuItem("Reverse Selected Lines");
		reverseSelectedLines.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().sortSelectedLines(SortOrder.REVERSE);
				}
			}
		});
		edit.add(reverseSelectedLines);

		JMenuItem sortSelectedStringsAlph = new JMenuItem("Sort Selected Strings (\"foo\", \"bar\") Alphabetically");
		sortSelectedStringsAlph.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().sortSelectedStrings(SortOrder.ALPHABETICAL);
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

		if (!standalone) {
			JMenuItem searchInWorkspace = new JMenuItem("Search in Workspace");
			searchInWorkspace.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showSearchWindow();
				}
			});
			edit.add(searchInWorkspace);
		}


		JMenu code = new JMenu("Code");
		menu.add(code);

		JMenuItem addConstructorJava = new JMenuItem("Add Constructor based on Selected Fields");
		addConstructorJava.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().addConstructor();
				}
			}
		});
		code.add(addConstructorJava);

		JMenuItem addGettersJava = new JMenuItem("Add Getters for Selected Fields");
		addGettersJava.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().addGetters();
				}
			}
		});
		code.add(addGettersJava);

		JMenuItem addSettersJava = new JMenuItem("Add Setters for Selected Fields");
		addSettersJava.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().addSetters();
				}
			}
		});
		code.add(addSettersJava);

		JMenuItem addGettersAndSettersJava = new JMenuItem("Add Getters and Setters for Selected Fields");
		addGettersAndSettersJava.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().addGettersAndSetters();
				}
			}
		});
		code.add(addGettersAndSettersJava);

		JMenuItem addToString = new JMenuItem("Add toString() for Selected Fields");
		addToString.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().addToString();
				}
			}
		});
		code.add(addToString);

		JMenuItem addEquals = new JMenuItem("Add equals() and hashCode() for Selected Fields");
		addEquals.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().addEquals();
				}
			}
		});
		code.add(addEquals);

		code.addSeparator();

		JMenuItem removeDebugLines = new JMenuItem("Remove Lines Containing // DEBUG or # DEBUG");
		removeDebugLines.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().removeDebugLines();
				}
			}
		});
		code.add(removeDebugLines);

		JMenuItem removeCommentsAndStrings = new JMenuItem("Remove Comments and Strings");
		removeCommentsAndStrings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().removeCommentsAndStrings();
				}
			}
		});
		code.add(removeCommentsAndStrings);

		JMenuItem removeXmlTags = new JMenuItem("Remove XML tags");
		addTextModificationAction(removeXmlTags, new StringModifier() {
			@Override
			public String modify(String str) {
				return XML.removeXmlTagsFromText(str);
			}
		});
		code.add(removeXmlTags);

		code.addSeparator();

		JMenuItem applyGit = new JMenuItem("Apply Git Markers (+/- at the beginning of lines)");
		applyGit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().applyGit(false);
				}
			}
		});
		code.add(applyGit);

		JMenuItem applyGitInv = new JMenuItem("Apply Git Markers Inverted (+/- at the beginning of lines but - is kept)");
		applyGitInv.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().applyGit(true);
				}
			}
		});
		code.add(applyGitInv);


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

		JMenuItem remTextUntil = new JMenuItem("Remove Text Until First Text In Search Field From Each Line");
		remTextUntil.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().removeUntilFirstOccurrence(mainGUI.getSearchFieldText());
				}
			}
		});
		operations.add(remTextUntil);

		JMenuItem remTextAfter = new JMenuItem("Remove Text After Last Text In Search Field From Each Line");
		remTextAfter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().removeAfterLastOccurrence(mainGUI.getSearchFieldText());
				}
			}
		});
		operations.add(remTextAfter);

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


		JMenu stats = new JMenu("Stats");
		menu.add(stats);

		JMenuItem showCharacterAmount = new JMenuItem("Show Amount of Characters");
		showCharacterAmount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					GuiUtils.notify("The currently opened text contains " +
						mainGUI.getCurrentTab().getContent().length() +
						" characters.");
				}
			}
		});
		stats.add(showCharacterAmount);

		JMenuItem showWordAmount = new JMenuItem("Show Amount of Words");
		showWordAmount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					String content = mainGUI.getCurrentTab().getContent();
					content = StrUtils.replaceAll(content, ".", " ");
					content = StrUtils.replaceAll(content, ",", " ");
					content = StrUtils.replaceAll(content, ";", " ");
					content = StrUtils.replaceAll(content, "!", " ");
					content = StrUtils.replaceAll(content, "?", " ");
					content = StrUtils.replaceAll(content, "\t", " ");
					content = StrUtils.replaceAll(content, "  ", " ");
					content = content.trim();
					int amount = StrUtils.countCharInString(' ', content) + 1;
					GuiUtils.notify("The currently opened text contains " +
						amount + " words.");
				}
			}
		});
		stats.add(showWordAmount);

		JMenuItem showLineAmount = new JMenuItem("Show Amount of Lines");
		showLineAmount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					String content = mainGUI.getCurrentTab().getContent();
					content = content.trim();
					int amount = StrUtils.countCharInString('\n', content) + 1;
					GuiUtils.notify("The currently opened text contains " +
						amount + " lines.");
				}
			}
		});
		stats.add(showLineAmount);


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

		if (!standalone) {
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
		}


		JMenu conversions = new JMenu("Conversions");
		menu.add(conversions);

		JMenuItem base64enc = new JMenuItem("Base 64 encode");
		addTextModificationAction(base64enc, new StringModifier() {
			@Override
			public String modify(String str) {
				return Base64Encoder.encode(str);
			}
		});
		conversions.add(base64enc);

		JMenuItem base64dec = new JMenuItem("Base 64 decode");
		addTextModificationAction(base64dec, new StringModifier() {
			@Override
			public String modify(String str) {
				return Base64Decoder.decode(str);
			}
		});
		conversions.add(base64dec);

		conversions.addSeparator();

		JMenuItem hexenc = new JMenuItem("Hex encode");
		addTextModificationAction(hexenc, new StringModifier() {
			@Override
			public String modify(String str) {
				return HexEncoder.encode(str);
			}
		});
		conversions.add(hexenc);

		JMenuItem hexdec = new JMenuItem("Hex decode");
		addTextModificationAction(hexdec, new StringModifier() {
			@Override
			public String modify(String str) {
				return HexDecoder.decode(str);
			}
		});
		conversions.add(hexdec);

		conversions.addSeparator();

		JMenuItem binaryenc = new JMenuItem("Binary encode");
		addTextModificationAction(binaryenc, new StringModifier() {
			@Override
			public String modify(String str) {
				return BinaryEncoder.encode(str);
			}
		});
		conversions.add(binaryenc);

		JMenuItem binarydec = new JMenuItem("Binary decode");
		addTextModificationAction(binarydec, new StringModifier() {
			@Override
			public String modify(String str) {
				return BinaryDecoder.decode(str);
			}
		});
		conversions.add(binarydec);

		conversions.addSeparator();

		JMenuItem utf8enc = new JMenuItem("UTF8 encode");
		addTextModificationAction(utf8enc, new StringModifier() {
			@Override
			public String modify(String str) {
				return Utf8Encoder.encode(str);
			}
		});
		conversions.add(utf8enc);

		JMenuItem utf8dec = new JMenuItem("UTF8 decode");
		addTextModificationAction(utf8dec, new StringModifier() {
			@Override
			public String modify(String str) {
				return Utf8Decoder.decode(str);
			}
		});
		conversions.add(utf8dec);

		conversions.addSeparator();

		JMenuItem htmlenc = new JMenuItem("HTML encode");
		addTextModificationAction(htmlenc, new StringModifier() {
			@Override
			public String modify(String str) {
				return HtmlEncoder.encode(str);
			}
		});
		conversions.add(htmlenc);

		JMenuItem htmldec = new JMenuItem("HTML decode");
		addTextModificationAction(htmldec, new StringModifier() {
			@Override
			public String modify(String str) {
				return HtmlDecoder.decode(str);
			}
		});
		conversions.add(htmldec);

		JMenuItem htmlesc = new JMenuItem("HTML escape");
		addTextModificationAction(htmlesc, new StringModifier() {
			@Override
			public String modify(String str) {
				return HTML.escapeHTMLstr(str);
			}
		});
		conversions.add(htmlesc);

		JMenuItem htmlunesc = new JMenuItem("HTML unescape");
		addTextModificationAction(htmlunesc, new StringModifier() {
			@Override
			public String modify(String str) {
				return HTML.unescapeHTMLstr(str);
			}
		});
		conversions.add(htmlunesc);

		JMenuItem removeHtmlTags = new JMenuItem("Remove HTML tags");
		addTextModificationAction(removeHtmlTags, new StringModifier() {
			@Override
			public String modify(String str) {
				return HTML.removeHtmlTagsFromText(str);
			}
		});
		conversions.add(removeHtmlTags);

		conversions.addSeparator();

		JMenuItem urlenc = new JMenuItem("URL encode");
		addTextModificationAction(urlenc, new StringModifier() {
			@Override
			public String modify(String str) {
				return UrlEncoder.encode(str);
			}
		});
		conversions.add(urlenc);

		JMenuItem urldec = new JMenuItem("URL decode");
		addTextModificationAction(urldec, new StringModifier() {
			@Override
			public String modify(String str) {
				return UrlDecoder.decode(str);
			}
		});
		conversions.add(urldec);

		conversions.addSeparator();

		JMenuItem morseenc = new JMenuItem("Morse encode");
		addTextModificationAction(morseenc, new StringModifier() {
			@Override
			public String modify(String str) {
				return MorseEncoder.encode(str);
			}
		});
		conversions.add(morseenc);

		JMenuItem morsedec = new JMenuItem("Morse decode");
		addTextModificationAction(morsedec, new StringModifier() {
			@Override
			public String modify(String str) {
				return MorseDecoder.decode(str);
			}
		});
		conversions.add(morsedec);

		conversions.addSeparator();

		JMenuItem romanNumeralenc = new JMenuItem("Roman Numeral encode");
		addTextModificationAction(romanNumeralenc, new StringModifier() {
			@Override
			public String modify(String str) {
				return RomanNumeralEncoder.encode(str);
			}
		});
		conversions.add(romanNumeralenc);

		JMenuItem romanNumeraldec = new JMenuItem("Roman Numeral decode");
		addTextModificationAction(romanNumeraldec, new StringModifier() {
			@Override
			public String modify(String str) {
				return RomanNumeralDecoder.decode(str);
			}
		});
		conversions.add(romanNumeraldec);

		conversions.addSeparator();

		JMenuItem uuidJavaToEcore = new JMenuItem("Java UUID to Ecore");
		addTextModificationAction(uuidJavaToEcore, new StringModifier() {
			@Override
			public String modify(String str) {
				return UuidEncoderDecoder.convertJavaUUIDtoEcore(str);
			}
		});
		conversions.add(uuidJavaToEcore);

		JMenuItem uuidEcoreToJava = new JMenuItem("Ecore UUID to Java");
		addTextModificationAction(uuidEcoreToJava, new StringModifier() {
			@Override
			public String modify(String str) {
				return UuidEncoderDecoder.convertEcoreUUIDtoJava(str);
			}
		});
		conversions.add(uuidEcoreToJava);

		JMenuItem generateUuid = new JMenuItem("Generate Java UUID");
		generateUuid.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AugFileTab tab = mainGUI.getCurrentTab();
				if (tab != null) {
					tab.insertText(UuidEncoderDecoder.generateJavaUUID());
				}
			}
		});
		conversions.add(generateUuid);

		conversions.addSeparator();

		JMenuItem prettyJSON = new JMenuItem("JSON prettify");
		addTextModificationAction(prettyJSON, new StringModifier() {
			@Override
			public String modify(String str) {
				try {
					JSON json = new JSON(str);
					boolean compressed = false;
					return json.toString(compressed);
				} catch (JsonParseException ex) {
					return str;
				}
			}
		});
		conversions.add(prettyJSON);

		JMenuItem miniJSON = new JMenuItem("JSON minify");
		addTextModificationAction(miniJSON, new StringModifier() {
			@Override
			public String modify(String str) {
				try {
					JSON json = new JSON(str);
					boolean compressed = true;
					return json.toString(compressed);
				} catch (JsonParseException ex) {
					return str;
				}
			}
		});
		conversions.add(miniJSON);

		JMenuItem escJSON = new JMenuItem("JSON escape");
		addTextModificationAction(escJSON, new StringModifier() {
			@Override
			public String modify(String str) {
				return JSON.escapeJSONstr(str);
			}
		});
		conversions.add(escJSON);

		conversions.addSeparator();

		JMenuItem javaStrEsc = new JMenuItem("Java string escape");
		addTextModificationAction(javaStrEsc, new StringModifier() {
			@Override
			public String modify(String str) {
				str = StrUtils.replaceAll(str, "\"", "\\\"");
				str = StrUtils.replaceAll(str, "\n", "\\n\" +\n\t\t\t\"");
				str = "\"" + str + "\"";
				return str;
			}
		});
		conversions.add(javaStrEsc);

		conversions.addSeparator();

		JMenuItem prettyEGSCC = new JMenuItem("EGS-CC prettify");
		prettyEGSCC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AugFileTab tab = mainGUI.getCurrentTab();
				if (tab != null) {
					tab.setContent(EgsccPrettifier.prettify(tab.getContent()));
				}
			}
		});
		conversions.add(prettyEGSCC);


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

		settings.addSeparator();

		JMenu defaultIndentation = new JMenu("Default Indentation");
		defaultIndentTab = new JRadioButtonMenuItem("1 Tab");
		defaultIndentTab.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setDefaultIndent("\t");
			}
		});
		defaultIndentation.add(defaultIndentTab);
		defaultIndent2Spaces = new JRadioButtonMenuItem("2 Spaces");
		defaultIndent2Spaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setDefaultIndent("  ");
			}
		});
		defaultIndentation.add(defaultIndent2Spaces);
		defaultIndent4Spaces = new JRadioButtonMenuItem("4 Spaces");
		defaultIndent4Spaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setDefaultIndent("    ");
			}
		});
		defaultIndentation.add(defaultIndent4Spaces);
		settings.add(defaultIndentation);
		mainGUI.setDefaultIndent(mainGUI.getDefaultIndent());

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

		addMissingImportsOnSaveItem = new JCheckBoxMenuItem("Add Missing Imports on Save");
		addMissingImportsOnSaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setAddMissingImportsOnSave(!mainGUI.getAddMissingImportsOnSave());
			}
		});
		mainGUI.setAddMissingImportsOnSave(mainGUI.getAddMissingImportsOnSave());
		settings.add(addMissingImportsOnSaveItem);

		reorganizeImportsOnSaveItem = new JCheckBoxMenuItem("Reorganize Imports on Save (Regular)");
		reorganizeImportsOnSaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setReorganizeImportsOnSave(!mainGUI.getReorganizeImportsOnSave());
			}
		});
		settings.add(reorganizeImportsOnSaveItem);

		reorganizeImportsOnSaveCompatibleItem = new JCheckBoxMenuItem("Reorganize Imports on Save (Compatibile with Wonky IDEs)");
		reorganizeImportsOnSaveCompatibleItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setReorganizeImportsOnSaveCompatible(!mainGUI.getReorganizeImportsOnSaveCompatible());
			}
		});
		settings.add(reorganizeImportsOnSaveCompatibleItem);

		mainGUI.setReorganizeImportsOnSave(mainGUI.getReorganizeImportsOnSave());
		mainGUI.setReorganizeImportsOnSaveCompatible(mainGUI.getReorganizeImportsOnSaveCompatible());

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
				mainGUI.setAddMissingImportsOnSave(toggleTo);
				mainGUI.setReorganizeImportsOnSave(toggleTo);
				mainGUI.setReorganizeImportsOnSaveCompatible(false);
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

		proposeTokenAutoCompleteItem = new JCheckBoxMenuItem("Propose Token Auto-Completion");
		proposeTokenAutoCompleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.setProposeTokenAutoComplete(!mainGUI.getProposeTokenAutoComplete());
			}
		});
		mainGUI.setProposeTokenAutoComplete(mainGUI.getProposeTokenAutoComplete());
		settings.add(proposeTokenAutoCompleteItem);

		JMenu window = new JMenu("Window");

		JMenuItem jumpToFile = new JMenuItem("Jump to File...");
		jumpToFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, ActionEvent.CTRL_MASK));
		jumpToFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.showJumpToFileBar();
			}
		});
		window.add(jumpToFile);

		window.addSeparator();

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

		JMenuItem toggleFileArea = new JMenuItem("Toggle File Area");
		toggleFileArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.toggleFileArea();
			}
		});
		window.add(toggleFileArea);

		window.addSeparator();

		showFilesInTreeItem = new JCheckBoxMenuItem("Show Files as Tree");
		showFilesInTreeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.toggleShowFilesInTree();
			}
		});
		showFilesInTreeItem.setSelected(mainGUI.getShowFilesInTree());
		window.add(showFilesInTreeItem);

		window.addSeparator();

		JMenu scheme = new JMenu("Editor Color Scheme");
		window.add(scheme);
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
		window.add(fontSizeItem);
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

		useAntiAliasingItem = new JCheckBoxMenuItem("Use Anti-Aliasing for Smoother Fonts (requires program restart)");
		useAntiAliasingItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.toggleUseAntiAliasing();
			}
		});
		useAntiAliasingItem.setSelected(mainGUI.getUseAntiAliasing());
		window.add(useAntiAliasingItem);

		menu.add(window);

		JMenu huh = new JMenu("?");

		JMenuItem openBackupPath = new JMenuItem("Open Backup Path");
		openBackupPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtils.openFolder(AssEditor.getBackupPath());
			}
		});
		huh.add(openBackupPath);

		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String aboutMessage = "This is the " + AssEditor.PROGRAM_TITLE + ".\n" +
					"Version: " + AssEditor.VERSION_NUMBER + " (" + AssEditor.VERSION_DATE + ")\n" +
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

		// if we are in standalone mode, do nothing
		if (switchWorkspace == null) {
			return;
		}

		List<JMenuItem> genericWorkspaceItems = WorkspaceUtils.createWorkspaceMenuEntries(
			switchWorkspace, WorkspaceAction.SWITCH_TO, mainGUI);

		workspaces = new ArrayList<>();
		for (JMenuItem genericWorkspaceItem : genericWorkspaceItems) {
			if (genericWorkspaceItem instanceof JCheckBoxMenuItem) {
				workspaces.add((JCheckBoxMenuItem) genericWorkspaceItem);
			}
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
					workspaceGUI = new WorkspaceGUI(mainGUI, augFileCtrl);
				}
				workspaceGUI.show();
			}
		});
		switchWorkspace.add(editWorkspaces);

		JMenu archiveWorkspace = new JMenu("Archive a Workspace");
		switchWorkspace.add(archiveWorkspace);

		WorkspaceUtils.createWorkspaceMenuEntries(archiveWorkspace, WorkspaceAction.ARCHIVE, mainGUI);

		JMenu unarchiveWorkspace = new JMenu("Unarchive a Workspace");
		switchWorkspace.add(unarchiveWorkspace);

		WorkspaceUtils.createWorkspaceMenuEntries(unarchiveWorkspace, WorkspaceAction.UNARCHIVE, mainGUI);

		JMenuItem exportWorkspace = new JMenuItem("Export Current Workspace");
		exportWorkspace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// show the workspace export GUI
				if (workspaceExportGUI == null) {
					workspaceExportGUI = new WorkspaceExportGUI(mainGUI, augFileCtrl);
				}
				workspaceExportGUI.show();
				workspaceExportGUI.displayCurrentWorkspace();
			}
		});
		switchWorkspace.add(exportWorkspace);
	}

	public void uncheckWorkspaces() {
		for (JCheckBoxMenuItem workspace : workspaces) {
			workspace.setSelected(false);
		}
	}

	public void reEnableDisableMenuItems(boolean augFilesExist, boolean fileIsSelected) {
		if (refreshFiles != null) {
			refreshFiles.setEnabled(augFilesExist);
		}
		if (saveFile != null) {
			saveFile.setEnabled(fileIsSelected);
		}
		if (saveAllFiles != null) {
			saveAllFiles.setEnabled(augFilesExist);
		}
		if (deleteFile != null) {
			deleteFile.setEnabled(fileIsSelected);
		}
		if (closeFile != null) {
			closeFile.setEnabled(fileIsSelected);
		}
		if (closeAllFiles != null) {
			closeAllFiles.setEnabled(augFilesExist);
		}
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
			workspaceSearchGUI = new WorkspaceSearchGUI(mainGUI, mainFrame);
		}

		workspaceSearchGUI.show();

		workspaceSearchGUI.firstSearch();
	}

	public List<JCheckBoxMenuItem> getWorkspaces() {
		return workspaces;
	}

	private void addTextModificationAction(JMenuItem item, StringModifier modifier) {
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AugFileTab tab = mainGUI.getCurrentTab();
				if (tab != null) {
					tab.modifySelectedOrAllText(modifier);
				}
			}
		});
	}
}
