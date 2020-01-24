/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;


public class Main {

	public final static String PROGRAM_TITLE = "A Softer Space Editor";
	public final static String VERSION_NUMBER = "0.0.2.7(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "18. December 2018 - 23. January 2020";


	/**
	 * TODO:
	 * do not constantly save the settings file (currently, when having it open in n++, we are again and again asked to refresh)
	 * converters inbuilt
	 * enable stats at the bottom
	 * add highlighting for datex
	 * increase / decrease font size
	 * enable hex view
	 * add compiler call, and report results
	 * ask to save before closing if files are unsaved
	 * replace also including newlines etc.! (so have a little button next to the search bar which shows either "text matching" or "extended (\n, \r, \t)")
	 * let user remove and reorder workspaces
	 * create a better opening dialog (e.g. one in which we can enter a path at the top, and where we can
	 *   automagically descend into Java-madness without having to click through src>main>java>com>ass>..., and where we do not
	 *   auto-open entire folders upon [Enter] but only when explicitly asking to open all files in the folder!)
	 * increase scroll speed while scrolling through code files
	 * not only call resizeNameLabel() in the AugFileTab.show(), but also in an onResize event!
	 * show "changemark *" at the top also
	 * disable "reorganize imports" in the menu when the highlighter does not support it
	 * add git diff as another file type (for .diff and .patch)
	 * add a context menu popup to the AugFileTab's code editor (e.g. for undo + redo also there, cut copy paste, etc.)
	 * only show undo and redo enabled when undoing / redoing curly possible
	 * get redo to actually work 100% properly ;)
	 * when showing functions on the right, highlight whichever one we are curly inside
	 * when we are saving the test_rest.sh file, if there is no trailing newline, a " is added ON SAVE... fix that!
	 * show (and allow us to change) whether Windows or Unix line endings are used
	 */
	public static void main(String[] args) {

		// let the Utils know in what program it is being used
		Utils.setProgramTitle(PROGRAM_TITLE);
		Utils.setVersionNumber(VERSION_NUMBER);
		Utils.setVersionDate(VERSION_DATE);

		ConfigFile config = null;
		boolean standalone = false;

		List<String> openFilenames = new ArrayList<>();

		for (String arg : args) {
			if ("--version".equals(arg)) {
				System.out.println(Utils.getFullProgramIdentifierWithDate());
				return;
			}
			if ("--version_for_zip".equals(arg)) {
				System.out.println("version " + Utils.getVersionNumber());
				return;
			}
			if ("--standalone".equals(arg)) {
				standalone = true;
				break;
			}
			// if this argument was not one of the predefined startup arguments,
			// then just open it as a file ;)
			openFilenames.add(arg);
		}

		try {
			// we get a config file based on the classpath, such that we know that this is always the
			// same "install" location, without change even if we are called from somewhere else
			config = new ConfigFile("settings", true);

			// create a default config file, if necessary
			if (config.getAllContents().isEmpty()) {
				config.setAllContents(new JSON("{\"workspaces\": [{\"name\": \"default\", \"files\": []}]}"));
			}
		} catch (JsonParseException e) {
			System.err.println("Loading the settings failed:");
			System.err.println(e);
			System.exit(1);
		}

		AugFileCtrl augFileCtrl = new AugFileCtrl(config, standalone);

		for (String filename : openFilenames) {
			augFileCtrl.loadAnotherFileWithoutSaving(new File(filename));
		}

		augFileCtrl.saveConfigFileList();

		// enable anti-aliasing for swing components (that is, the main menu)
		System.setProperty("swing.aatext", "true");
		// do NOT enable anti-aliasing for awt components (that is, the editor itself)
		// as anti-aliasing makes the code prettier, but less readable!
		// System.setProperty("awt.useSystemAAFontSettings", "on");

		SwingUtilities.invokeLater(new MainGUI(augFileCtrl, config));
	}

}
