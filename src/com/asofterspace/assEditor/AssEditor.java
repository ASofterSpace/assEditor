/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.guiImages.FancyCodeEditor;
import com.asofterspace.toolbox.images.DefaultImageFile;
import com.asofterspace.toolbox.images.Image;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.io.TextFile;
import com.asofterspace.toolbox.utils.StrUtils;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;


public class AssEditor {

	public final static String PROGRAM_TITLE = "A Softer Space Editor";
	public final static String VERSION_NUMBER = "0.0.7.8(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "18. December 2018 - 14. March 2025";

	private final static String CONFIG_KEY_BACKUP_SETTINGS_NUM = "backupSettingsNum";
	private final static String SETTINGS_FILE_NAME = "settings";

	public final static int CONTENT_BACKUP_AMOUNT = 100;
	private final static int SETTINGS_BACKUP_AMOUNT = 25;

	private static ConfigFile config;
	private static AugFileCtrl augFileCtrl;
	private final static List<Image> stamps = new ArrayList<>();
	private final static List<FancyCodeEditor> wantStamps = new ArrayList<>();

	private static boolean standalone = false;
	private static boolean editmode = false;


	public static void main(String[] args) {

		// let the Utils know in what program it is being used
		Utils.setProgramTitle(PROGRAM_TITLE);
		Utils.setVersionNumber(VERSION_NUMBER);
		Utils.setVersionDate(VERSION_DATE);

		config = null;

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
			if ("--edit".equals(arg)) {
				standalone = true;
				editmode = true;
				continue;
			}
			// if this argument was not one of the predefined startup arguments,
			// then just open it as a file ;)
			openFilenames.add(arg);
		}

		try {
			// we get a config file based on the classpath, such that we know that this is always the
			// same "install" location, without change even if we are called from somewhere else
			config = new ConfigFile(SETTINGS_FILE_NAME, true);

		} catch (JsonParseException e1) {
			try {
				TextFile configAsText = new TextFile(ConfigFile.getConfigFilename(SETTINGS_FILE_NAME, true));
				configAsText.saveContent("");
				config = new ConfigFile(SETTINGS_FILE_NAME, true);
			} catch (JsonParseException e2) {
				System.out.println("newly created JSON could not be parsed: " + e2);
				System.exit(1);
			}
		}

		// create a default config file, if necessary
		if (config.getAllContents().isEmpty()) {
			try {
				config.setAllContents(new JSON("{\"workspaces\": [{\"name\": \"default\", \"files\": []}]}"));
			} catch (JsonParseException e3) {
				System.out.println("default JSON could not be parsed: " + e3);
				System.exit(1);
			}
		}

		// we prevent saving as we would save a hundred times during startup, and will allow
		// it again after the startup is done in MainGUI
		config.preventSaving();

		if (editmode) {
			config.set(MainGUI.CONFIG_KEY_SCHEME, GuiUtils.LIGHT_SCHEME);
			config.set(MainGUI.CONFIG_KEY_FONT_SIZE, 18);
		}

		augFileCtrl = new AugFileCtrl(config, standalone, editmode, openFilenames);

		SwingUtilities.invokeLater(new MainGUI(augFileCtrl, config, standalone, editmode));
	}

	public static void performPostStartupActions() {

		// no need to do anything that takes RAM space and CPU time in edit mode ^^
		if (editmode) {
			return;
		}

		augFileCtrl.saveConfigFileList();

		// especially stamps take 300 MB RAM... just no need at all in edit mode, they aren't even shown!
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
			StrUtils.leftPad0(currentBackup, 2) + ConfigFile.FILE_EXTENSION);
		backupFile.setContent(config.getAllContents().toString(false));
		backupFile.create();

		currentBackup++;
		if (currentBackup >= SETTINGS_BACKUP_AMOUNT) {
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
