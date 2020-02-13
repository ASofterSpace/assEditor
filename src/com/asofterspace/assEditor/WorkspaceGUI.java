/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.io.File;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class WorkspaceGUI {

	private MainGUI mainGUI;

	private AugFileCtrl augFileCtrl;

	private JDialog editWorkitemsDialog;


	public WorkspaceGUI(MainGUI mainGUI, AugFileCtrl augFileCtrl) {

		this.mainGUI = mainGUI;

		this.augFileCtrl = augFileCtrl;

		this.editWorkitemsDialog = createGUI();
	}

	private JDialog createGUI() {

		// TODO :: create a modal in which existing workspaces can be deleted,
		// TODO :: and existing ones can be moved up and down

		// Create the window
		final JDialog editWorkitemsDialog = new JDialog(mainGUI.getMainFrame(), "Edit Workspaces", true);
		GridLayout editWorkitemsDialogLayout = new GridLayout(4, 1);
		editWorkitemsDialogLayout.setVgap(8);
		editWorkitemsDialog.setLayout(editWorkitemsDialogLayout);
		editWorkitemsDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Enter a new name here to add a new workspace:");
		editWorkitemsDialog.add(explanationLabel);

		final JTextField newWorkspaceName = new JTextField();
		editWorkitemsDialog.add(newWorkspaceName);

		JPanel newWorkspaceButtonRow = new JPanel();
		GridLayout newWorkspaceButtonRowLayout = new GridLayout(1, 1);
		newWorkspaceButtonRowLayout.setHgap(8);
		newWorkspaceButtonRow.setLayout(newWorkspaceButtonRowLayout);
		editWorkitemsDialog.add(newWorkspaceButtonRow);

		JButton addButton = new JButton("Add this Workspace");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				augFileCtrl.addWorkspace(newWorkspaceName.getText());

				refreshWorkspaces();
			}
		});
		newWorkspaceButtonRow.add(addButton);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		editWorkitemsDialog.add(buttonRow);

		JButton sortWorkspacesAlphaButton = new JButton("Sort Workspaces Alphabetically");
		sortWorkspacesAlphaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				augFileCtrl.sortWorkspaces();

				refreshWorkspaces();
			}
		});
		buttonRow.add(sortWorkspacesAlphaButton);

		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editWorkitemsDialog.dispose();
			}
		});
		buttonRow.add(doneButton);

		// Set the preferred size of the dialog
		int width = 600;
		int height = 200;
		editWorkitemsDialog.setSize(width, height);
		editWorkitemsDialog.setPreferredSize(new Dimension(width, height));

		return editWorkitemsDialog;
	}

	public void show() {
		GuiUtils.centerAndShowWindow(editWorkitemsDialog);
	}

	private void refreshWorkspaces() {
		mainGUI.getMainMenu().refreshWorkspaces();
		mainGUI.getMainPopupMenu().refreshWorkspaces();
	}

}
