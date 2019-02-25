/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.Utils;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.File;

import javax.swing.SwingUtilities;


public class Main {

	public final static String PROGRAM_TITLE = "A Softer Space Editor";
	public final static String VERSION_NUMBER = "0.0.1.0(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "18. December 2018 - 25. February 2019";


	/**
	 * TODO:
	 * converters inbuilt
	 * enable stats at the bottom
	 * click on filename at the top to make it copyable (or even copy immediately)
	 * indent 1, 2, 4, 8, tab, 2 tab, unindent, force unindent by 1 or 2 characters
	 * add highlighting for datex
	 * increase / decrease font size
	 * cut, copy, paste
	 * undo, redo
	 * enable hex view
	 * enable function overview
	 * enable [Ctrl]+function name to jump to its declaration
 	 * add compiler call, and report results
 	 * ask to save before closing if files are unsaved
 	 * replace (like with edit.exe, replace also including newlines etc.!)
	 * create a better opening dialog (e.g. one in which we can enter a path at the top, and where we can
	 *   automagically descend into Java-madness without having to click through src>main>java>com>ass>...)
	 * increase scroll speed while scrolling through code files
	 * do not base left-hand tabs on filename only (such that several Main.java can be opened at the same time)
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
 			config.setAllContents(new JSON("{\"files\":[]}"));
 		}

		AugFileCtrl augFileCtrl = new AugFileCtrl(config);

		for (String arg : args) {
			augFileCtrl.loadAnotherFileWithoutSaving(new File(arg));
		}

		augFileCtrl.saveConfigFileList();

		SwingUtilities.invokeLater(new GUI(augFileCtrl, config));
	}

}
