/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class WorkspaceExportGUI {

	private MainGUI mainGUI;

	private AugFileCtrl augFileCtrl;

	private JDialog exportWorkitemsDialog;

	private JTextArea exportedWorkspaceOutputMemo;


	public WorkspaceExportGUI(MainGUI mainGUI, AugFileCtrl augFileCtrl) {

		this.mainGUI = mainGUI;

		this.augFileCtrl = augFileCtrl;

		this.exportWorkitemsDialog = createGUI();
	}

	private JDialog createGUI() {

		// Create the window
		final JDialog exportWorkitemsDialog = new JDialog(mainGUI.getMainFrame(), "Edit Workspaces", true);
		GridBagLayout exportWorkitemsDialogLayout = new GridBagLayout();
		exportWorkitemsDialog.setLayout(exportWorkitemsDialogLayout);
		exportWorkitemsDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		exportedWorkspaceOutputMemo = new JTextArea();
		JScrollPane outputMemoScroller = new JScrollPane(exportedWorkspaceOutputMemo);
		exportWorkitemsDialog.add(outputMemoScroller, new Arrangement(0, 0, 1.0, 1.0));

		JPanel buttonRow = new JPanel();
		GridBagLayout buttonRowLayout = new GridBagLayout();
		buttonRow.setLayout(buttonRowLayout);
		exportWorkitemsDialog.add(buttonRow, new Arrangement(0, 1, 1.0, 0.0));

		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportWorkitemsDialog.dispose();
			}
		});
		buttonRow.add(doneButton, new Arrangement(0, 0, 1.0, 0.0));

		// Set the preferred size of the dialog
		int width = 1200;
		int height = 800;
		exportWorkitemsDialog.setSize(width, height);
		exportWorkitemsDialog.setPreferredSize(new Dimension(width, height));

		return exportWorkitemsDialog;
	}

	public void show() {
		GuiUtils.centerAndShowWindow(exportWorkitemsDialog);
	}

	public void displayCurrentWorkspace() {
		JSON currentWorkspace = new JSON(augFileCtrl.getWorkspace());
		// export as uncompressed json
		String jsonStrWorkspace = currentWorkspace.toString(false);
		exportedWorkspaceOutputMemo.setText(jsonStrWorkspace);
	}

}
