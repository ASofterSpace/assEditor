/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.codeeditor.utils.OpenFileCallback;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;

import java.util.List;


public class AugFileOpenCallback implements OpenFileCallback {

	private Directory baseDirectory;

	private MainGUI mainGUI;


	public AugFileOpenCallback(Directory directory, MainGUI mainGUI) {

		this.baseDirectory = directory;

		this.mainGUI = mainGUI;
	}

	public void openFileRelativeToThis(String relativePath, CodeLanguage language, String extraInfo) {

		String localName = relativePath.substring(relativePath.lastIndexOf("/") + 1);

		// first of all, let's try to jump to the file if it is already open
		List<AugFileTab> tabs = mainGUI.getTabs();
		for (AugFileTab tab : tabs) {
			if (localName.equals(tab.getName())) {
				// check if we are currently in Java...
				if (language == CodeLanguage.JAVA) {
					// ... and if we are, ensure that the package is also the correct one!
					String javaContent = tab.getContent();
					String[] javaLines = javaContent.split("\n");
					for (String javaLine : javaLines) {
						if (javaLine.startsWith("package ")) {
							String packageLine = javaLine.substring(8).trim();
							packageLine = packageLine.substring(0, packageLine.length() - 1).trim();
							// in this case, extraInfo is the original import statement content, so e.g. foo.bar.Classname
							if ((packageLine + "." + localName).equals(extraInfo + ".java")) {
								mainGUI.showTab(tab);
								return;
							}
						}
					}
				} else {
					mainGUI.showTab(tab);
					return;
				}
			}
		}

		// if we did not yet open the file, then let's try to go for the determined path name directly
		File newFile = new File(baseDirectory.getAbsoluteDirname() + "/" + relativePath);

		// if the new file does not exist at the determined path name, then the package must belong to a different repository... uwäh!
		if (!newFile.exists()) {
			// sooo try to go further up and check some other directories...
			// basically, we have to search for the file everywhere xD
			newFile = baseDirectory.findFile(localName);

			// we here specify how many directories we are willing to go up at most to search for the file
			// (if this is too small, we will not find the file; if this is too large, we will take forever and crash the application...
			// TODO :: put this into a thread?)
			int maxUpDirs = 4;
			while ((newFile == null) && (maxUpDirs > 0)) {
				baseDirectory = new Directory(baseDirectory.getAbsoluteDirname() + "/..");
				newFile = baseDirectory.findFile(localName);
			}
		}

		mainGUI.loadFile(newFile);
	}
}
