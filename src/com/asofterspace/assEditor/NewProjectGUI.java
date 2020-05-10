/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.StrUtils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Date;
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
		GridLayout dialogLayout = new GridLayout(7, 1);
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
		folderLabel.setText("Enter a top level folder in which the project folder should be created:");
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

		JLabel descLabel = new JLabel();
		descLabel.setText("Finally, enter a short description of the project:");
		dialog.add(descLabel);

		final JTextField descriptionField = new JTextField();
		dialog.add(descriptionField);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		dialog.add(buttonRow);

		JButton addButton = new JButton("Create this Project");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// cleanup input
				String projectName = newProjectName.getText().trim();
				String _ProjectName = StrUtils.upcaseFirstLetter(projectName);
				String _projectName = StrUtils.lowcaseFirstLetter(projectName);

				String newFolderName = newFolder.getText().trim();
				if (!((newFolderName.endsWith("/")) || (newFolderName.endsWith("\\")))) {
					newFolderName += "/";
				}
				newFolderName += projectName + "/";

				String description = descriptionField.getText().trim();

				Date now = new Date();
				String year = ""+DateUtils.getYear(now);
				String date = DateUtils.serializeDateLong(now);

				// add the workspace and switch to it
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

				SimpleFile buildBatFile = new SimpleFile(newFolderName + "build.bat");
				buildBatFile.saveContent("IF NOT EXIST ..\\Toolbox-Java\\ (\n" +
					"	echo \"It looks like you did not yet get the Toolbox-Java project - please do so (and put it as a folder next to this folder.)\"\n" +
					"	EXIT 1\n" +
					")\n" +
					"\n" +
					"cd src\\com\\asofterspace\n" +
					"\n" +
					"rd /s /q toolbox\n" +
					"\n" +
					"md toolbox\n" +
					"cd toolbox\n" +
					"\n" +
					"md io\n" +
					"md utils\n" +
					"md web\n" +
					"\n" +
					"cd ..\\..\\..\\..\n" +
					"\n" +
					"copy \"..\\Toolbox-Java\\src\\com\\asofterspace\\toolbox\\*.java\" \"src\\com\\asofterspace\\toolbox\"\n" +
					"copy \"..\\Toolbox-Java\\src\\com\\asofterspace\\toolbox\\io\\*.*\" \"src\\com\\asofterspace\\toolbox\\io\"\n" +
					"copy \"..\\Toolbox-Java\\src\\com\\asofterspace\\toolbox\\utils\\*.*\" \"src\\com\\asofterspace\\toolbox\\utils\"\n" +
					"copy \"..\\Toolbox-Java\\src\\com\\asofterspace\\toolbox\\web\\*.*\" \"src\\com\\asofterspace\\toolbox\\web\"\n" +
					"\n" +
					"rd /s /q bin\n" +
					"\n" +
					"md bin\n" +
					"\n" +
					"cd src\n" +
					"\n" +
					"dir /s /B *.java > sourcefiles.list\n" +
					"\n" +
					"javac -deprecation -Xlint:all -encoding utf8 -d ../bin @sourcefiles.list\n" +
					"\n" +
					"pause");

				SimpleFile buildShFile = new SimpleFile(newFolderName + "build.sh");
				buildShFile.saveContent("#!/bin/bash\n" +
					"\n" +
					"if [[ ! -d ../Toolbox-Java ]]; then\n" +
					"	echo \"It looks like you did not yet get the Toolbox-Java project - please do so (and put it as a folder next to this folder.)\"\n" +
					"	exit 1\n" +
					"fi\n" +
					"\n" +
					"cd src/com/asofterspace\n" +
					"\n" +
					"rm -rf toolbox\n" +
					"\n" +
					"mkdir toolbox\n" +
					"cd toolbox\n" +
					"\n" +
					"mkdir io\n" +
					"mkdir utils\n" +
					"mkdir web\n" +
					"\n" +
					"cd ../../../..\n" +
					"\n" +
					"cp ../Toolbox-Java/src/com/asofterspace/toolbox/*.java src/com/asofterspace/toolbox\n" +
					"cp ../Toolbox-Java/src/com/asofterspace/toolbox/io/*.* src/com/asofterspace/toolbox/io\n" +
					"cp ../Toolbox-Java/src/com/asofterspace/toolbox/utils/*.* src/com/asofterspace/toolbox/utils\n" +
					"cp ../Toolbox-Java/src/com/asofterspace/toolbox/web/*.* src/com/asofterspace/toolbox/web\n" +
					"\n" +
					"rm -rf bin\n" +
					"\n" +
					"mkdir bin\n" +
					"\n" +
					"cd src\n" +
					"\n" +
					"find . -name \"*.java\" > sourcefiles.list\n" +
					"\n" +
					"javac -deprecation -Xlint:all -encoding utf8 -d ../bin @sourcefiles.list");

				SimpleFile readmeFile = new SimpleFile(newFolderName + "README.md");
				readmeFile.saveContent("# " + projectName + "\n" +
					"\n" +
					"**Class:** Utility\n" +
					"\n" +
					"**Language:** Java\n" +
					"\n" +
					"**Platform:** Windows / Linux\n" +
					"\n" +
					description + "\n" +
					"\n" +
					"## Setup\n" +
					"\n" +
					"Download our Toolbox-Java (which is a separate project here on github) into an adjacent directory on your hard drive.\n" +
					"\n" +
					"Start the build by calling under Windows:\n" +
					"\n" +
					"```\n" +
					"build.bat\n" +
					"```\n" +
					"\n" +
					"Or under Linux:\n" +
					"\n" +
					"```\n" +
					"build.sh\n" +
					"```\n" +
					"\n" +
					"## Run\n" +
					"\n" +
					"To start up the " + projectName + " project after it has been built, you can call under Windows:\n" +
					"\n" +
					"```\n" +
					"run.bat\n" +
					"```\n" +
					"\n" +
					"Or under Linux:\n" +
					"\n" +
					"```\n" +
					"run.sh\n" +
					"```\n" +
					"\n" +
					"## License\n" +
					"\n" +
					"We at A Softer Space really love the Unlicense, which pretty much allows anyone to do anything with this source code.\n" +
					"For more info, see the file UNLICENSE.\n" +
					"\n" +
					"If you desperately need to use this source code under a different license, [contact us](mailto:moya@asofterspace.com) - I am sure we can figure something out.");

				SimpleFile releaseShFile = new SimpleFile(newFolderName + "release.sh");
				releaseShFile.saveContent("#!/bin/bash\n" +
					"\n" +
					"echo \"Re-building with target Java 7 (such that the compiled .class files will be compatible with as many JVMs as possible)...\"\n" +
					"\n" +
					"cd src\n" +
					"\n" +
					"# build build build!\n" +
					"javac -encoding utf8 -d ../bin -bootclasspath ../other/java7_rt.jar -source 1.7 -target 1.7 @sourcefiles.list\n" +
					"\n" +
					"cd ..\n" +
					"\n" +
					"\n" +
					"\n" +
					"echo \"Creating the release file " + projectName + ".zip...\"\n" +
					"\n" +
					"mkdir release\n" +
					"\n" +
					"cd release\n" +
					"\n" +
					"mkdir " + projectName + "\n" +
					"\n" +
					"# copy the main files\n" +
					"cp -R ../bin " + projectName + "\n" +
					"cp ../UNLICENSE " + projectName + "\n" +
					"cp ../README.md " + projectName + "\n" +
					"cp ../run.sh " + projectName + "\n" +
					"cp ../run.bat " + projectName + "\n" +
					"\n" +
					"# convert \\n to \\r\\n for the Windows files!\n" +
					"cd " + projectName + "\n" +
					"awk 1 ORS='\\r\\n' run.bat > rn\n" +
					"mv rn run.bat\n" +
					"cd ..\n" +
					"\n" +
					"# create a version tag right in the zip file\n" +
					"cd " + projectName + "\n" +
					"version=$(./run.sh --version_for_zip)\n" +
					"echo \"$version\" > \"$version\"\n" +
					"cd ..\n" +
					"\n" +
					"# zip it all up\n" +
					"zip -rq " + projectName + ".zip " + projectName + "\n" +
					"\n" +
					"mv " + projectName + ".zip ..\n" +
					"\n" +
					"cd ..\n" +
					"rm -rf release\n" +
					"\n" +
					"echo \"The file " + projectName + ".zip has been created in $(pwd)\"");

				SimpleFile runBatFile = new SimpleFile(newFolderName + "run.bat");
				runBatFile.saveContent("@echo off\n" +
					"\n" +
					"cd /D %~dp0\n" +
					"\n" +
					"java -classpath \"%~dp0\\bin\" -Xms16m -Xmx1024m com.asofterspace." + _projectName + "." + _ProjectName + " %*\n" +
					"\n" +
					"pause");

				SimpleFile runShFile = new SimpleFile(newFolderName + "run.sh");
				runShFile.saveContent("#!/bin/bash\n" +
					"\n" +
					"cd `dirname \"$0\"`\n" +
					"\n" +
					"java -classpath \"`dirname \"$0\"`/bin\" -Xms16m -Xmx1024m com.asofterspace." + _projectName + "." + _ProjectName + " \"$@\"");

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

				SimpleFile mainFile = new SimpleFile(newFolderName + "src/com/asofterspace/" + _projectName + "/" + _ProjectName + ".java");
				mainFile.createParentDirectory();
				mainFile.saveContent("/**\n" +
					" * Unlicensed code created by A Softer Space, " + year + "\n" +
					" * www.asofterspace.com/licenses/unlicense.txt\n" +
					" */\n" +
					"package com.asofterspace." + _projectName + ";\n" +
					"\n" +
					"import com.asofterspace.toolbox.io.File;\n" +
					"import com.asofterspace.toolbox.io.JSON;\n" +
					"import com.asofterspace.toolbox.io.JsonFile;\n" +
					"import com.asofterspace.toolbox.io.JsonParseException;\n" +
					"import com.asofterspace.toolbox.io.SimpleFile;\n" +
					"import com.asofterspace.toolbox.io.TextFile;\n" +
					"import com.asofterspace.toolbox.utils.DateUtils;\n" +
					"import com.asofterspace.toolbox.utils.Record;\n" +
					"import com.asofterspace.toolbox.utils.StrUtils;\n" +
					"import com.asofterspace.toolbox.Utils;\n" +
					"\n" +
					"\n" +
					"public class " + _ProjectName + " {\n" +
					"\n" +
					"	public final static String PROGRAM_TITLE = \"" + projectName + "\";\n" +
					"	public final static String VERSION_NUMBER = \"0.0.0.1(\" + Utils.TOOLBOX_VERSION_NUMBER + \")\";\n" +
					"	public final static String VERSION_DATE = \"" + date + " - " + date + "\";\n" +
					"\n" +
					"	public static void main(String[] args) {\n" +
					"\n" +
					"		// let the Utils know in what program it is being used\n" +
					"		Utils.setProgramTitle(PROGRAM_TITLE);\n" +
					"		Utils.setVersionNumber(VERSION_NUMBER);\n" +
					"		Utils.setVersionDate(VERSION_DATE);\n" +
					"\n" +
					"		if (args.length > 0) {\n" +
					"			if (args[0].equals(\"--version\")) {\n" +
					"				System.out.println(Utils.getFullProgramIdentifierWithDate());\n" +
					"				return;\n" +
					"			}\n" +
					"\n" +
					"			if (args[0].equals(\"--version_for_zip\")) {\n" +
					"				System.out.println(\"version \" + Utils.getVersionNumber());\n" +
					"				return;\n" +
					"			}\n" +
					"		}\n" +
					"\n" +
					"		System.out.println(\"Loading database...\");\n" +
					"\n" +
					"		Database database = new Database();\n" +
					"\n" +
					"		System.out.println(\"Saving database...\");\n" +
					"\n" +
					"		database.save();\n" +
					"\n" +
					"		System.out.println(\"Done! Have a nice day! :)\");\n" +
					"	}\n" +
					"\n" +
					"}");

				SimpleFile databaseFile = new SimpleFile(newFolderName + "src/com/asofterspace/" + _projectName + "/Database.java");
				databaseFile.saveContent("/**\n" +
					" * Unlicensed code created by A Softer Space, " + year + "\n" +
					" * www.asofterspace.com/licenses/unlicense.txt\n" +
					" */\n" +
					"package com.asofterspace." + _projectName + ";\n" +
					"\n" +
					"import com.asofterspace.toolbox.io.JSON;\n" +
					"import com.asofterspace.toolbox.io.JsonFile;\n" +
					"import com.asofterspace.toolbox.io.JsonParseException;\n" +
					"import com.asofterspace.toolbox.utils.Record;\n" +
					"\n" +
					"import java.util.ArrayList;\n" +
					"import java.util.List;\n" +
					"\n" +
					"\n" +
					"public class Database {\n" +
					"\n" +
					"	private JsonFile dbFile;\n" +
					"\n" +
					"	private JSON root;\n" +
					"\n" +
					"	/* here, put something like e.g.:\n" +
					"	private List<Object> objects;\n" +
					"	*/\n" +
					"\n" +
					"\n" +
					"	public Database() {\n" +
					"\n" +
					"		this.dbFile = new JsonFile(\"config/database.json\");\n" +
					"		this.dbFile.createParentDirectory();\n" +
					"		try {\n" +
					"			this.root = dbFile.getAllContents();\n" +
					"		} catch (JsonParseException e) {\n" +
					"			System.err.println(\"Oh no!\");\n" +
					"			e.printStackTrace(System.err);\n" +
					"			System.exit(1);\n" +
					"		}\n" +
					"\n" +
					"		/* here, put something like e.g.:\n" +
					"\n" +
					"		List<Record> objectsRecs = root.getArray(\"objects\");\n" +
					"\n" +
					"		this.objects = new ArrayList<>();\n" +
					"\n" +
					"		for (Record rec : objectsRecs) {\n" +
					"			objects.add(new Object(rec));\n" +
					"		}\n" +
					"		*/\n" +
					"	}\n" +
					"\n" +
					"	public Record getRoot() {\n" +
					"		return root;\n" +
					"	}\n" +
					"\n" +
					"	public void save() {\n" +
					"\n" +
					"		/* here, put something like e.g.:\n" +
					"\n" +
					"		List<Record> objectsRecs = new ArrayList<>();\n" +
					"\n" +
					"		for (Object obj : objects) {\n" +
					"			objectsRecs.add(obj.toRecord());\n" +
					"		}\n" +
					"\n" +
					"		root.set(\"objects\", objectsRecs);\n" +
					"		*/\n" +
					"\n" +
					"		dbFile.setAllContents(root);\n" +
					"		dbFile.save();\n" +
					"	}\n" +
					"}");

				// load all these files
				List<java.io.File> filesToOpen = new ArrayList<>();
				filesToOpen.add(gitignoreFile.getJavaFile());
				filesToOpen.add(buildBatFile.getJavaFile());
				filesToOpen.add(buildShFile.getJavaFile());
				filesToOpen.add(readmeFile.getJavaFile());
				filesToOpen.add(releaseShFile.getJavaFile());
				filesToOpen.add(runBatFile.getJavaFile());
				filesToOpen.add(runShFile.getJavaFile());
				filesToOpen.add(unlicenseFile.getJavaFile());
				filesToOpen.add(databaseFile.getJavaFile());
				filesToOpen.add(mainFile.getJavaFile());
				mainGUI.openFiles(filesToOpen, newFolderName);

				dialog.dispose();
			}
		});
		buttonRow.add(addButton);

		JButton doneButton = new JButton("Cancel");
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
