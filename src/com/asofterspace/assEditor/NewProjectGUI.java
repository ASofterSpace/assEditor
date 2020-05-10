/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.SimpleFile;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class NewProjectGUI {

	private MainGUI mainGUI;

	private AugFileCtrl augFileCtrl;

	private JDialog dialog;


	public NewProjectGUI(MainGUI mainGUI, AugFileCtrl augFileCtrl) {

		this.mainGUI = mainGUI;

		this.augFileCtrl = augFileCtrl;

		this.dialog = createGUI();
	}

	private JDialog createGUI() {

		// Create the window
		final JDialog dialog = new JDialog(mainGUI.getMainFrame(), "New A Softer Space Java Project", true);
		GridLayout dialogLayout = new GridLayout(5, 1);
		dialogLayout.setVgap(8);
		dialog.setLayout(dialogLayout);
		dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Enter a new name here to create a new project:");
		dialog.add(explanationLabel);

		final JTextField newProjectName = new JTextField();
		dialog.add(newProjectName);

		JLabel folderLabel = new JLabel();
		folderLabel.setText("Enter a top level folder in which the project should be created:");
		dialog.add(folderLabel);

		final JTextField newFolder = new JTextField();
		dialog.add(newFolder);
		List<AugFile> augFiles = augFileCtrl.getFiles();
		for (AugFile augFile : augFiles) {
			Directory dir = augFile.getParentDirectory();
			String absDirName = dir.getAbsoluteDirname();
			if (absDirName.contains("asofterspace")) {
				newFolder.setText(
					absDirName.substring(0, absDirName.indexOf("asofterspace") + "asofterspace".length() + 1)
				);
			}
		}

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		dialog.add(buttonRow);

		JButton addButton = new JButton("Create this Project");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// add the workspace and switch to it
				String projectName = newProjectName.getText();
				String newFolderName = newFolder.getText();
				if (!((newFolderName.endsWith("/")) || (newFolderName.endsWith("\\")))) {
					newFolderName += "/";
				}
				newFolderName += projectName + "/";

				augFileCtrl.addWorkspace(projectName);

				augFileCtrl.sortWorkspaces();

				mainGUI.refreshWorkspaces();

				mainGUI.switchToWorkspace(projectName);

				// generate all the necessary files
				SimpleFile gitignoreFile = new SimpleFile(newFolderName + ".gitignore");
				gitignoreFile.createParentDirectory();
				gitignoreFile.saveContent("# Compiled class file\n" +
					"*.class\n" +
					"\n" +
					"# Log file\n" +
					"*.log\n" +
					"\n" +
					"# BlueJ files\n" +
					"*.ctxt\n" +
					"\n" +
					"# Mobile Tools for Java (J2ME)\n" +
					".mtj.tmp/\n" +
					"\n" +
					"# Package Files #\n" +
					"*.jar\n" +
					"*.war\n" +
					"*.ear\n" +
					"*.zip\n" +
					"*.tar.gz\n" +
					"*.rar\n" +
					"\n" +
					"# virtual machine crash logs, see http://www.java.com/en/download/help/error_hotspot.xml\n" +
					"hs_err_pid*\n" +
					"\n" +
					"# bin directory\n" +
					"bin\n" +
					"\n" +
					"# sourcefile list\n" +
					"src/sourcefiles.list\n" +
					"\n" +
					"# external dependencies (with their own repositories and life cycles)\n" +
					"src/com/asofterspace/toolbox\n" +
					"\n" +
					"# config files\n" +
					"config\n" +
					"\n" +
					"# input / output files\n" +
					"*.txt\n" +
					"*.csv\n" +
					"*.json");

				SimpleFile unlicenseFile = new SimpleFile(newFolderName + "UNLICENSE");
				unlicenseFile.saveContent("This is free and unencumbered software released into the public domain.\n" +
					"\n" +
					"Anyone is free to copy, modify, publish, use, compile, sell, or\n" +
					"distribute this software, either in source code form or as a compiled\n" +
					"binary, for any purpose, commercial or non-commercial, and by any\n" +
					"means.\n" +
					"\n" +
					"In jurisdictions that recognize copyright laws, the author or authors\n" +
					"of this software dedicate any and all copyright interest in the\n" +
					"software to the public domain. We make this dedication for the benefit\n" +
					"of the public at large and to the detriment of our heirs and\n" +
					"successors. We intend this dedication to be an overt act of\n" +
					"relinquishment in perpetuity of all present and future rights to this\n" +
					"software under copyright law.\n" +
					"\n" +
					"THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND,\n" +
					"EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF\n" +
					"MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.\n" +
					"IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR\n" +
					"OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,\n" +
					"ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR\n" +
					"OTHER DEALINGS IN THE SOFTWARE.\n" +
					"\n" +
					"For more information, please refer to <http://unlicense.org/>");

				// load all these files
				List<java.io.File> filesToOpen = new ArrayList<>();
				filesToOpen.add(gitignoreFile.getJavaFile());
				filesToOpen.add(unlicenseFile.getJavaFile());
				mainGUI.openFiles(filesToOpen, newFolderName);

				dialog.dispose();
			}
		});
		buttonRow.add(addButton);

		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonRow.add(doneButton);

		// Set the preferred size of the dialog
		int width = 600;
		int height = 240;
		dialog.setSize(width, height);
		dialog.setPreferredSize(new Dimension(width, height));

		return dialog;
	}

	public void show() {
		GuiUtils.centerAndShowWindow(dialog);
	}

}
