/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.guiImages.FancyCodeEditor;
import com.asofterspace.toolbox.images.DefaultImageFile;
import com.asofterspace.toolbox.images.Image;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.utils.StrUtils;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;


public class AssEditor {

	public final static String PROGRAM_TITLE = "A Softer Space Editor";
	public final static String VERSION_NUMBER = "0.0.4.6(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "18. December 2018 - 10. November 2020";

	private final static String CONFIG_KEY_BACKUP_SETTINGS_NUM = "backupSettingsNum";

	private static ConfigFile config;
	private static AugFileCtrl augFileCtrl;
	private final static List<Image> stamps = new ArrayList<>();
	private final static List<FancyCodeEditor> wantStamps = new ArrayList<>();


	/**
	 * TODO:
	 * enable stats at the bottom
	 * add highlighting for datex
	 * enable hex view
	 * add compiler call, and report results
	 * ask to save before closing if files are unsaved
	 * replace also including newlines etc.! (so have a little button next to the search bar which shows either "text matching" or "extended (\n, \r, \t)")
	 * let user remove and reorder workspaces
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
	 * make scroll speed configurable (currently set to 99 in the AugFileTab)
	 * when opening file through ctrl click, first search in all currently open folders - not just the folder of the current AugFileTab (so that we get Toolbox stuff more likely e.g.)
	 * also allow importing workspaces
	 * let the text oscillate its colors by re-assigning the attributes in the Code class? :)
	 * only refresh the Code Editor once all the character attributes have been set...
	 * (just setting a switch to return from the paintComponent call in the CodeEditor class does work somewhat-ly...)
	 */
	public static void main(String[] args) {

		// let the Utils know in what program it is being used
		Utils.setProgramTitle(PROGRAM_TITLE);
		Utils.setVersionNumber(VERSION_NUMBER);
		Utils.setVersionDate(VERSION_DATE);

		config = null;
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
				continue;
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

		// we prevent saving as we would save a hundred times during startup, and will allow
		// it again after the startup is done in MainGUI
		config.preventSaving();

		augFileCtrl = new AugFileCtrl(config, standalone, openFilenames.size() < 1);

		for (String filename : openFilenames) {
			augFileCtrl.loadAnotherFileWithoutSaving(new File(filename));
		}

		SwingUtilities.invokeLater(new MainGUI(augFileCtrl, config, standalone));
	}

	public static void performPostStartupActions() {

		augFileCtrl.saveConfigFileList();

		String classPath = System.getProperty("java.class.path");
		Directory stampDir = new Directory(classPath + "/../res/stamps");
		boolean recursively = true;
		List<File> stampFiles = stampDir.getAllFiles(recursively);
		for (File stampFile : stampFiles) {
			DefaultImageFile imgFile = new DefaultImageFile(stampFile);
			Image stamp = imgFile.getImage();
			stamp.minify();
			stamps.add(stamp);
		}

		if (stamps.size() > 0) {
			for (FancyCodeEditor stampTarget : wantStamps) {
				addStampTo(stampTarget);
			}
		}

		int currentBackup = config.getInteger(CONFIG_KEY_BACKUP_SETTINGS_NUM, 0);

		// backup the configuration (in the same location as the file content backups are kept)
		SimpleFile backupFile = new SimpleFile(getBackupPath() + "settings_" +
			StrUtils.leftPad0(currentBackup, 4) + ConfigFile.FILE_EXTENSION);
		backupFile.setContent(config.getAllContents().toString(false));
		backupFile.create();

		currentBackup++;
		if (currentBackup > 9999) {
			currentBackup = 0;
		}

		config.set(CONFIG_KEY_BACKUP_SETTINGS_NUM, currentBackup);
	}

	public static String getBackupPath() {
		return System.getProperty("java.class.path") + "/../backup/";
	}

	public static List<Image> getStamps() {
		return stamps;
	}

	public static void addStampTo(FancyCodeEditor functionMemo) {
		List<Image> stamps = AssEditor.getStamps();
		if (stamps.size() > 0) {
			int ran = (int)(Math.random() * stamps.size());
			functionMemo.setBackgroundImage(stamps.get(ran));
		} else {
			wantStamps.add(functionMemo);
		}
	}

}
