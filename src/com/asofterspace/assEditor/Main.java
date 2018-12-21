/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.Utils;
import com.asofterspace.toolbox.io.JSON;

import javax.swing.SwingUtilities;


public class Main {

	public final static String PROGRAM_TITLE = "A Softer Space Editor";
	public final static String VERSION_NUMBER = "0.0.0.4(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "18. December 2018 - 21. December 2018";

	
	/**
	 * TODO:
	 * create new (empty) file
	 * automatic backups
	 * converters inbuilt
	 * enable stats at the bottom
	 * click on name at the top to make it copyable (or even copy immediately)
	 * select + tab: move selection    
	 * indent 1, 2, 4, 8, tab, 2 tab, unindent, force unindent    
	 * checkbox: remove trailing whitespace on save   
	 * improve javacode: ()[]<>;:.!= colorful, and 
	 * new blubb - blubb colorful
	 * add highlighting for datex
	 * increase / decrease font size
	 * select all
	 * cut, copy, paste
	 * undo, redo
	 * select from here / to here
	 * enable hex view
	 * enable function overview
	 * add line numbers
	 * add compiler call, and report results
	 */
	public static void main(String[] args) {
	
		// let the Utils know in what program it is being used
		Utils.setProgramTitle(PROGRAM_TITLE);
		Utils.setVersionNumber(VERSION_NUMBER);
		Utils.setVersionDate(VERSION_DATE);

		ConfigFile config = new ConfigFile("settings");

		// create a default config file, if necessary
		if (config.getAllContents().isEmpty()) {
			config.setAllContents(new JSON("{\"files\":[]}"));
		}

		SwingUtilities.invokeLater(new GUI(config));
	}

}
