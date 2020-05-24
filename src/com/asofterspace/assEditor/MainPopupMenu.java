/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.io.File;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;


public class MainPopupMenu {

	private MainGUI mainGUI;

	private JFrame parent;

	private boolean standalone;

	private JPopupMenu popupMenu;

	private JMenuItem saveFilePopup;
	private JMenuItem deleteFilePopup;
	private JMenuItem closeFilePopup;
	private JMenu moveFilesToWorkspacePopup;
	private JMenu duplicateFilesToWorkspacePopup;


	public MainPopupMenu(MainGUI mainGUI, JFrame parent, boolean standalone) {

		this.mainGUI = mainGUI;

		this.parent = parent;

		this.standalone = standalone;
	}

	public JPopupMenu createPopupMenu() {

		popupMenu = new JPopupMenu();

		JMenuItem newFilePopup = new JMenuItem("New File");
		newFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.newFile();
			}
		});
		popupMenu.add(newFilePopup);

		JMenuItem copySelectedFilePopup = new JMenuItem("Copy Selected File");
		copySelectedFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.copyFiles(mainGUI.getHighlightedTabs());
			}
		});
		popupMenu.add(copySelectedFilePopup);

		JMenuItem openFilePopup = new JMenuItem("Open Files");
		openFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.showOpenFileDialog();
			}
		});
		popupMenu.add(openFilePopup);

		popupMenu.addSeparator();

		saveFilePopup = new JMenuItem("Save Selected Files");
		saveFilePopup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.saveFiles(mainGUI.getHighlightedTabs());
			}
		});
		popupMenu.add(saveFilePopup);

		JMenuItem renameFilePopup = new JMenuItem("Rename Selected File");
		// renameFilePopup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2));
		renameFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.renameSelectedFile();
			}
		});
		popupMenu.add(renameFilePopup);

		deleteFilePopup = new JMenuItem("Delete Selected Files");
		deleteFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.deleteFiles(mainGUI.getHighlightedTabs());
			}
		});
		popupMenu.add(deleteFilePopup);

		closeFilePopup = new JMenuItem("Close Selected Files");
		closeFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.closeFiles(mainGUI.getHighlightedTabs());
			}
		});
		popupMenu.add(closeFilePopup);

		if (!standalone) {
			moveFilesToWorkspacePopup = new JMenu("Move Selected Files to Workspace");
			popupMenu.add(moveFilesToWorkspacePopup);

			duplicateFilesToWorkspacePopup = new JMenu("Duplicate Selected Files in Workspace");
			popupMenu.add(duplicateFilesToWorkspacePopup);

			refreshWorkspaces();
		}

		popupMenu.addSeparator();

		JMenuItem openFolder = new JMenuItem("Open Folder");
		openFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainGUI.openHighlightedFolder();
			}
		});
		popupMenu.add(openFolder);

		// don't do the following:
		//   fileListComponent.setComponentPopupMenu(popupMenu);
		// instead manually show the popup when the right mouse key is pressed in the mouselistener
		// for the file list, because that means that we can right click on an file, select it immediately,
		// and open the popup for exactly that file

		return popupMenu;
	}

	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	public void reEnableDisableMenuItems(boolean augFilesExist, boolean fileIsSelected) {
		saveFilePopup.setEnabled(fileIsSelected);
		deleteFilePopup.setEnabled(fileIsSelected);
		closeFilePopup.setEnabled(fileIsSelected);
	}

	public void refreshWorkspaces() {

		WorkspaceUtils.createWorkspaceMenuEntries(moveFilesToWorkspacePopup, WorkspaceAction.MOVE_FILES, mainGUI);

		WorkspaceUtils.createWorkspaceMenuEntries(duplicateFilesToWorkspacePopup, WorkspaceAction.DUPLICATE_FILES, mainGUI);
	}

}
