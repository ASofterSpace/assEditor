/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.FileTab;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.utils.StrUtils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class WorkspaceSearchGUI {

	private MainGUI mainGUI;

	private JFrame mainFrame;

	private JDialog searchInWorkspaceDialog;

	private JTextArea searchInWorkspaceOutputMemo;
	private JLabel searchInWorkspaceOutputLabel;
	private JTextField workspaceSearchField;


	public WorkspaceSearchGUI(MainGUI mainGUI, JFrame mainFrame) {

		this.mainGUI = mainGUI;

		this.mainFrame = mainFrame;

		this.searchInWorkspaceDialog = createGUI();
	}

	private JDialog createGUI() {

		// Create the window
		final JDialog searchInWorkspaceDialog = new JDialog(mainFrame, "Search in Workspace", false);
		GridBagLayout searchInWorkspaceDialogLayout = new GridBagLayout();
		searchInWorkspaceDialog.setLayout(searchInWorkspaceDialogLayout);
		searchInWorkspaceDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Enter the text you are searching for:");
		searchInWorkspaceDialog.add(explanationLabel, new Arrangement(0, 0, 1.0, 0.0));

		workspaceSearchField = new JTextField();
		searchInWorkspaceDialog.add(workspaceSearchField, new Arrangement(0, 1, 1.0, 0.0));

		JLabel explanationReplaceLabel = new JLabel();
		explanationReplaceLabel.setText("Enter here the replacement text in case you want to replace anything:");
		searchInWorkspaceDialog.add(explanationReplaceLabel, new Arrangement(0, 2, 1.0, 0.0));

		final JTextField workspaceReplaceField = new JTextField();
		searchInWorkspaceDialog.add(workspaceReplaceField, new Arrangement(0, 3, 1.0, 0.0));

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
				searchInWorkspaceFor(workspaceSearchField.getText());
			}
		});
		buttonRow.add(searchButton);

		JButton replaceButton = new JButton("Replace");
		replaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchInWorkspaceAndReplaceWith(workspaceSearchField.getText(), workspaceReplaceField.getText());
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

		return searchInWorkspaceDialog;
	}

	public void show() {
		GuiUtils.centerAndShowWindow(searchInWorkspaceDialog);
	}

	private void searchInWorkspaceFor(String searchFor) {

		if ((searchFor == null) || "".equals(searchFor)) {
			return;
		}

		StringBuilder result = new StringBuilder();

		int matches = 0;
		int infiles = 0;

		for (AugFileTab curTab : mainGUI.getTabs()) {
			int curMatches = curTab.searchAndAddResultTo(searchFor, result);

			if (curMatches > 0) {
				matches += curMatches;
				infiles++;
			}
		}

		mainGUI.regenerateAugFileList();

		searchInWorkspaceOutputMemo.setText(result.toString());

		searchInWorkspaceOutputLabel.setText(
			"Found " +
			StrUtils.thingOrThings(matches, "match", "matches") +
			" in " +
			StrUtils.thingOrThings(infiles, "file") +
			"."
		);
	}

	private void searchInWorkspaceAndReplaceWith(String searchFor, String replaceWith) {

		if ((searchFor == null) || "".equals(searchFor)) {
			return;
		}

		for (AugFileTab curTab : mainGUI.getTabs()) {
			curTab.replaceAll(searchFor, replaceWith);
			curTab.saveIfChanged();
		}

		searchInWorkspaceOutputMemo.setText("All occurrences of " + searchFor + " replaced with " + replaceWith + "!");
	}

	/**
	 * When we show the search in workspace window, and have already been searching in the little search bar,
	 * then we want to immediately search for the same term also in here!
	 */
	public void firstSearch() {

		if ((workspaceSearchField.getText() == null) || "".equals(workspaceSearchField.getText())) {
			workspaceSearchField.setText(mainGUI.getSearchFieldText());
			searchInWorkspaceFor(workspaceSearchField.getText());
		}
	}

}
