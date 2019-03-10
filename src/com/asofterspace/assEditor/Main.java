/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.Utils;

import javax.swing.SwingUtilities;


public class Main {

	public final static String PROGRAM_TITLE = "A Softer Space Editor";
	public final static String VERSION_NUMBER = "0.0.1.3(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "18. December 2018 - 10. March 2019";


	/**
	 * TODO:
	 * converters inbuilt
	 * enable stats at the bottom
	 * indent 1, 2, 4, 8, tab, 2 tab, unindent, force unindent by 1 or 2 characters
	 * add highlighting for datex
	 * increase / decrease font size
	 * cut, copy, paste
	 * enable hex view
	 * enable [Ctrl]+function name to jump to its declaration
	 * add compiler call, and report results
	 * ask to save before closing if files are unsaved
	 * replace also including newlines etc.!
	 * let user add and remove workspaces
	 * create a better opening dialog (e.g. one in which we can enter a path at the top, and where we can
	 * automagically descend into Java-madness without having to click through src>main>java>com>ass>...)
	 * increase scroll speed while scrolling through code files
	 * do not base left-hand tabs on filename only (such that several Main.java can be opened at the same time)
	 * not only call resizeNameLabel() in the AugFileTab.show(), but also in an onResize event!
	 * show "changemark *" at the top also
	 * disable "reorganize imports" in the menu when the highlighter does not support it
	 * add git diff as another file type (for .diff and .patch)
	 * add a popup to the AugFileTab's code editor (e.g. for undo + redo also there)
	 * only show undo and redo enabled when undoing / redoing curly possible
	 * when showing functions on the right, highlight whichever one we are curly inside
	 */
	public static void main(String[] args) {

		// let the Utils know in what program it is being used
		Utils.setProgramTitle(PROGRAM_TITLE);
		Utils.setVersionNumber(VERSION_NUMBER);
		Utils.setVersionDate(VERSION_DATE);

		// we get a config file based on the classpath, such that we know that this is always the
		// same "install" location, without change even if we are called from somewhere else
		ConfigFile config = new ConfigFile("settings", true);

		// create a default config file, if necessary
		if (config.getAllContents().isEmpty()) {
			config.setAllContents(new JSON("{\"workspaces\": [{\"name\": \"default\", \"files\": []}]}"));
		}

		AugFileCtrl augFileCtrl = new AugFileCtrl(config);

		for (String arg : args) {
			augFileCtrl.loadAnotherFileWithoutSaving(new File(arg));
		}

		augFileCtrl.saveConfigFileList();

		SwingUtilities.invokeLater(new GUI(augFileCtrl, config));
	}

}
