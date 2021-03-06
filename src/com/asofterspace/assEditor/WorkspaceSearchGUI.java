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
import com.asofterspace.toolbox.Utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

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

	public final static String JUMP_TO = "Jump to ";


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
		explanationLabel.setText("Enter the text you are searching for - this does NOT ignore capitalization:");
		searchInWorkspaceDialog.add(explanationLabel, new Arrangement(0, 0, 1.0, 0.0));

		workspaceSearchField = new JTextField();
		searchInWorkspaceDialog.add(workspaceSearchField, new Arrangement(0, 1, 1.0, 0.0));

		// listen to being focused, and when it happens, select all the current content
		workspaceSearchField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				workspaceSearchField.selectAll();
			}
			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		JLabel explanationReplaceLabel = new JLabel();
		explanationReplaceLabel.setText("Enter here the replacement text in case you want to replace anything:");
		searchInWorkspaceDialog.add(explanationReplaceLabel, new Arrangement(0, 2, 1.0, 0.0));

		final JTextField workspaceReplaceField = new JTextField();
		searchInWorkspaceDialog.add(workspaceReplaceField, new Arrangement(0, 3, 1.0, 0.0));

		// listen to being focused, and when it happens, select all the current content
		workspaceReplaceField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				workspaceReplaceField.selectAll();
			}
			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		searchInWorkspaceOutputMemo = new JTextArea();

		MouseAdapter mouseListener = new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent event) {
				jumpToClickedFile();
			}

			@Override
			public void mouseClicked(MouseEvent event) {
				jumpToClickedFile();
			}

			private void jumpToClickedFile() {
				int pos = searchInWorkspaceOutputMemo.getCaretPosition();
				String text = searchInWorkspaceOutputMemo.getText();
				String clickedLine = StrUtils.getLineFromPosition(pos, text);
				if (clickedLine.startsWith(JUMP_TO) && clickedLine.endsWith(":")) {
					String filename = clickedLine.substring(JUMP_TO.length());
					filename = filename.substring(0, filename.length() - ":".length());
					AugFileTab tabToShow = mainGUI.getTabWithFilename(filename);
					if (tabToShow != null) {
						boolean highlightTab = true;
						mainGUI.showTab(tabToShow, highlightTab);
					}
				}
			}
		};

		searchInWorkspaceOutputMemo.addMouseListener(mouseListener);

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
		int height = 700;
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

		mainGUI.disableRegenerateAugFileList();

		int foundFileAmount = 0;
		int totalFileAmount = 0;

		StringBuilder result = new StringBuilder();
		result.append("Replaced ");
		result.append(searchFor);
		result.append(" with ");
		result.append(replaceWith);
		result.append(" in the following files:\n");

		// explicitly copy the list item-by-item such that we do not get concurrent modification exceptions
		List<AugFileTab> tabList = new ArrayList<>(mainGUI.getTabs());

		for (AugFileTab curTab : tabList) {
			if (curTab.replaceAll(searchFor, replaceWith)) {
				result.append("\n" + curTab.getFilePath());
				foundFileAmount++;
				curTab.save();
			}
			totalFileAmount++;
		}

		mainGUI.reenableRegenerateAugFileList();

		searchInWorkspaceOutputMemo.setText(result.toString());

		searchInWorkspaceOutputLabel.setText(
			"All occurrences of " + searchFor + " (found in " +
			StrUtils.thingOrThings(foundFileAmount, "file") +
			" out of " +
			StrUtils.thingOrThings(totalFileAmount, "file") +
			" in total) replaced with " + replaceWith + "!"
		);
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
