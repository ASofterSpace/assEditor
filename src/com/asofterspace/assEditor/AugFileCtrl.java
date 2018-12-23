/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This class controls the opened files, which by the act of opening have become augmented files
 */
public class AugFileCtrl {

	private final static String CONF_FILENAME = "filename";
	private final static String CONF_CARET_POS = "caretPos";
	private final static String CONF_LANGUAGE = "language";

	private ConfigFile configuration;

	private List<AugFile> files;

	private Thread saveConfigThread;


	public AugFileCtrl(ConfigFile configuration) {

		this.configuration = configuration;

		files = new ArrayList<>();

		JSON jsonConfig = configuration.getAllContents();

		List<JSON> jsonFiles = jsonConfig.getArray("files");

		if (jsonFiles != null) {
			for (JSON jsonFile : jsonFiles) {

				File fileToOpen = new File(jsonFile.getString(CONF_FILENAME));

				AugFile curFile = loadAnotherFileWithoutSaving(fileToOpen);

				if (curFile != null) {
					curFile.setInitialCaretPos(jsonFile.getInteger(CONF_CARET_POS));

					curFile.setInitialSourceLanguage(jsonFile.getString(CONF_LANGUAGE));
				}
			}
		}

		saveConfigThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					saveConfigFileList();

					try {
						// save every five seconds
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// Ooops!
					}
				}
			}
		});
		saveConfigThread.start();
	}

	/*
	public void loadDirectory(Directory baseDir) {

		this.baseDir = baseDir;

		baseDir.create();

		entries = new ArrayList<>();

		// all files directly inside the base dir are company files (then inside the companies are the actual people files)
		List<File> companyFiles = baseDir.getAllFiles(false);

		for (File companyFile : companyFiles) {

			Company curCompany = new Company(this, new EntryFile(companyFile));

			entries.add(curCompany);

			Directory curCompanyDir = baseDir.createChildDir(curCompany.getDirectoryName());

			List<File> peopleFiles = curCompanyDir.getAllFiles(false);

			for (File peopleFile : peopleFiles) {
				entries.add(new Person(this, new EntryFile(peopleFile), curCompany));
			}
		}
	}
	*/

	/**
	 * Loads a file and returns the new AugFile instance
	 * Will return null quite happily in case you are trying to open a file that was already opened!
	 */
	private AugFile loadAnotherFileWithoutSaving(File fileToLoad) {

		// first of all check that the file has not already been loaded!
		for (AugFile oldFile : files) {
			if (fileToLoad.getFilename().equals(oldFile.getFilename())) {
				// indeed! we found one that we already got!
				return null;
			}
		}

		AugFile result = new AugFile(this, fileToLoad);

		files.add(result);

		return result;
	}

	public AugFile loadAnotherFile(File fileToLoad) {

		AugFile result = loadAnotherFileWithoutSaving(fileToLoad);

		saveConfigFileList();

		return result;
	}

	public List<AugFile> getFiles() {
		return files;
	}

	public void removeAllFiles() {
		files = new ArrayList<>();

		saveConfigFileList();
	}

	public void removeFile(AugFile fileToRemove) {
		files.remove(fileToRemove);

		saveConfigFileList();
	}

	public void saveConfigFileList() {

		// TODO :: ugly - fix me! (we are setting JSON via String, which works, but wäh... ^^)
		JSON jsonConfig = configuration.getAllContents();

		StringBuilder fileListBuilder = new StringBuilder();
		String sep = "";

		for (AugFile augFile : files) {
			fileListBuilder.append("{\"");
			fileListBuilder.append(CONF_FILENAME);
			fileListBuilder.append("\": \"");
			fileListBuilder.append(augFile.getFilename());
			fileListBuilder.append("\", \"");
			fileListBuilder.append(CONF_CARET_POS);
			fileListBuilder.append("\": ");
			fileListBuilder.append(augFile.getCaretPos());
			fileListBuilder.append(", \"");
			fileListBuilder.append(CONF_LANGUAGE);
			fileListBuilder.append("\": \"");
			fileListBuilder.append(augFile.getSourceLanguage());
			fileListBuilder.append("\"}");
			fileListBuilder.append(sep);
			sep = ", ";
		}

		configuration.set("files", new JSON("[" + fileListBuilder.toString() + "]"));
	}

	/**
	 * This saves all entries
	 */
	public void save() {

		for (AugFile file : files) {
			file.save();
		}
	}

}
