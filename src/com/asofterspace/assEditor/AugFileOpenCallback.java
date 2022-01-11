/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.codeeditor.utils.OpenFileCallback;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;


public class AugFileOpenCallback implements OpenFileCallback {

	private Directory localDirectory;

	private MainGUI mainGUI;

	private AugFileCtrl augFileCtrl;


	public AugFileOpenCallback(Directory directory, MainGUI mainGUI, AugFileCtrl augFileCtrl) {

		this.localDirectory = directory;

		this.mainGUI = mainGUI;

		this.augFileCtrl = augFileCtrl;
	}

	@Override
	public boolean openFileRelativeToThis(String basePath, List<String> relativePaths, CodeLanguage language, String extraInfo) {

		Directory baseDirectory = new Directory(localDirectory.getAbsoluteDirname() + "/" + basePath);

		// if we know nothing about the package, then just try to open the file directly
		if (extraInfo == null) {
			for (String relativePath : relativePaths) {
				File newFile = new File(baseDirectory.getAbsoluteDirname() + "/" + relativePath);
				if (newFile.exists()) {
					mainGUI.loadFile(newFile);
					return true;
				}
			}
			return false;
		}

		// oh! we know stuff about the package - so we can be much more clever! :D
		// firstly, we get the local name - so instead of foo/bar/Blubb.java, we get Blubb.java
		List<String> localNames = new ArrayList<>();
		for (String relativePath : relativePaths) {
			localNames.add(relativePath.substring(("/" + relativePath).lastIndexOf("/")));
		}

		// first of all, let's try to jump to the file if it is already open
		List<AugFileTab> tabs = mainGUI.getTabs();
		for (AugFileTab tab : tabs) {
			for (String localName : localNames) {
				if (localName.equals(tab.getName())) {
					// check if we are currently in Java...
					if ((language == CodeLanguage.JAVA) || (language == CodeLanguage.GROOVY)) {
						// ... and if we are, ensure that the package is also the correct one!
						String javaContent = tab.getContent();
						String[] javaLines = javaContent.split("\n");
						for (String javaLine : javaLines) {
							if (javaLine.startsWith("package ")) {
								String packageLine = javaLine.substring(8).trim();
								packageLine = packageLine.substring(0, packageLine.length() - 1).trim();
								// in this case, extraInfo is the original import statement content, so e.g. foo.bar.Classname
								if ((packageLine + "." + localName).equals(extraInfo + ".java") ||
									(packageLine + "." + localName).equals(extraInfo + ".groovy")) {
									mainGUI.showTab(tab, true);
									return true;
								}
							}
						}
					} else {
						mainGUI.showTab(tab, true);
						return true;
					}
				}
			}
		}

		// if we did not yet open the file, then let's try to go for the determined path name directly
		for (String relativePath : relativePaths) {
			File newFile = new File(baseDirectory.getAbsoluteDirname() + "/" + relativePath);
			if (newFile.exists()) {
				mainGUI.loadFile(newFile);
				return true;
			}
		}

		// if the new file does not exist at the determined path name, then the package must belong to a different repository... uwäh!
		// sooo try to go further up and check some other directories...
		// basically, we have to search for the file everywhere xD
		new Thread(new Runnable() {
			public void run() {
				Directory currentBaseDir = new Directory(localDirectory.getAbsoluteDirname());
				long startTime = System.currentTimeMillis();
				// do not search for more than a minute
				while (System.currentTimeMillis() - startTime < 60*1000) {
					File newFile = currentBaseDir.findFileFromList(localNames);
					if ((newFile != null) && newFile.exists()) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								mainGUI.loadFile(newFile);
							}
						});
						return;
					}
					currentBaseDir = new Directory(currentBaseDir.getAbsoluteDirname() + "/..");
				}
			}
		}).start();

		return false;
	}

	@Override
	public List<String> getOtherFileContents(List<String> fileEndings) {

		List<String> result = new ArrayList<>();

		List<AugFile> augFiles = augFileCtrl.getFiles();

		for (AugFile augFile : augFiles) {
			for (String fileEnding : fileEndings) {
				if (augFile.getFilename().endsWith(fileEnding)) {
					String content = augFile.getContent();
					if (content != null) {
						result.add(content);
					}
					break;
				}
			}
		}

		return result;
	}
}
