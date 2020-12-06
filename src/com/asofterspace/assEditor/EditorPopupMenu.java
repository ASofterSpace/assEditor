/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.gui.CodeEditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


public class EditorPopupMenu {

	private MainGUI mainGUI;

	private JFrame parent;

	private JPopupMenu popupMenu;

	private CodeEditor currentEditor;


	public EditorPopupMenu(MainGUI mainGUI, JFrame parent, boolean standalone) {

		this.mainGUI = mainGUI;

		this.parent = parent;
	}

	/**
	 * The editor popup menu should really just contain a few most-often-used commands
	 * which are actually useful to have
	 */
	public JPopupMenu createPopupMenu() {

		popupMenu = new JPopupMenu();

		JMenuItem indent2spaces = new JMenuItem("Indent by 2 spaces");
		indent2spaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().indentSelection("  ");
				}
			}
		});
		popupMenu.add(indent2spaces);

		JMenuItem unindent2spaces = new JMenuItem("Unindent by 2 spaces");
		unindent2spaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mainGUI.getCurrentTab() != null) {
					mainGUI.getCurrentTab().unindentSelection(2, false, " ");
				}
			}
		});
		popupMenu.add(unindent2spaces);

		popupMenu.addSeparator();

		mainGUI.getMainMenu().addMainCodeCommands(popupMenu);

		return popupMenu;
	}

	public JPopupMenu getPopupMenu() {
		// lazy initialization - create popup menu when it is first opened
		if (popupMenu == null) {
			return createPopupMenu();
		}
		return popupMenu;
	}

	public void setCurrentEditor(CodeEditor currentEditor) {
		this.currentEditor = currentEditor;
	}

}
