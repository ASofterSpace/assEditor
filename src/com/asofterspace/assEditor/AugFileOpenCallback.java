/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.utils.OpenFileCallback;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;


public class AugFileOpenCallback implements OpenFileCallback {

	private Directory baseDirectory;

	private MainGUI mainGUI;


	public AugFileOpenCallback(Directory directory, MainGUI mainGUI) {

		this.baseDirectory = directory;

		this.mainGUI = mainGUI;
	}

	public void openFileRelativeToThis(String relativePath) {

		File newFile = new File(baseDirectory.getAbsoluteDirname() + "/" + relativePath);

		// if the new file does not exist, try to go up and check some other directories... but which ones? hummm...
		// basically, we have to search for the file xD
		if (!newFile.exists()) {
			String localName = relativePath.substring(relativePath.lastIndexOf("/") + 1);
			newFile = baseDirectory.findFile(localName);

			int i = 4;
			while ((newFile == null) && (i > 0)) {
				baseDirectory = new Directory(baseDirectory.getAbsoluteDirname() + "/..");
				newFile = baseDirectory.findFile(localName);
			}
		}

		mainGUI.loadFile(newFile);
	}
}
