/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
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

	private JSON activeWorkspace;

	private Thread saveConfigThread;


	public AugFileCtrl(ConfigFile configuration) {

		this.configuration = configuration;

		String activeWorkspaceName = configuration.getValue("activeWorkspace");

		switchToWorkspace(activeWorkspaceName);

		startConfigSavingThread();
	}

	private void startConfigSavingThread() {

		saveConfigThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						// save every five seconds
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						return;
					}

					saveConfigFileList();
				}
			}
		});
		saveConfigThread.start();
	}

	public String getWorkspaceName() {
		return activeWorkspace.getString("name");
	}

	public JSON getWorkspace() {
		return activeWorkspace;
	}

	public List<String> getWorkspaces() {

		List<String> workspaces = new ArrayList<>();

		List<JSON> jsonWorkspaces = configuration.getAllContents().getArray("workspaces");

		for (JSON jsonWorkspace : jsonWorkspaces) {
			workspaces.add(jsonWorkspace.getString("name"));
		}

		return workspaces;
	}

	public void addWorkspace(String workspace) {

		JSON jsonWorkspaces = configuration.getAllContents().get("workspaces");

		JSON newWorkspace = new JSON("{\"files\":[]}");

		newWorkspace.setString("name", workspace);

		jsonWorkspaces.append(newWorkspace);

		configuration.create();
	}

	public void switchToWorkspace(String workspace) {

		List<JSON> jsonWorkspaces = configuration.getAllContents().getArray("workspaces");

		for (JSON jsonWorkspace : jsonWorkspaces) {
			if ((workspace == null) ||
				 workspace.equals(jsonWorkspace.getString("name"))) {
				switchToJsonWorkspace(jsonWorkspace);
				return;
			}
		}
	}

	private void switchToJsonWorkspace(JSON workspace) {

		activeWorkspace = workspace;

		configuration.getAllContents().setString("activeWorkspace", workspace.getString("name"));

		files = new ArrayList<>();

		List<JSON> jsonFiles = workspace.getArray("files");

		if (jsonFiles != null) {
			for (JSON jsonFile : jsonFiles) {

				File fileToOpen = new File(jsonFile.getString(CONF_FILENAME));

				AugFile curFile = loadAnotherFileWithoutSaving(fileToOpen);

				if (curFile != null) {
					curFile.setInitialCaretPos(jsonFile.getInteger(CONF_CARET_POS));

					CodeLanguage sourceLang = CodeLanguage.getFromString(jsonFile.getString(CONF_LANGUAGE));

					curFile.setSourceLanguage(sourceLang);
				}
			}
		}

		configuration.create();
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
	public AugFile loadAnotherFileWithoutSaving(File fileToLoad) {

		String newFilename = fileToLoad.getCanonicalFilename();

		// first of all check that the file has not already been loaded!
		for (AugFile oldFile : files) {
			if (newFilename.equals(oldFile.getFilename())) {
				// indeed! we found one that we already got!
				return null;
			}
		}

		AugFile result = new AugFile(this, fileToLoad);

		synchronized (files) {
			files.add(result);
		}

		return result;
	}

	public AugFile loadAnotherFile(File fileToLoad) {

		AugFile result = loadAnotherFileWithoutSaving(fileToLoad);

		saveConfigFileList();

		return result;
	}

	public List<AugFile> getFiles() {

		List<AugFile> result;

		synchronized (files) {
			result = new ArrayList<>(files);
		}

		return result;
	}

	public void removeAllFiles() {

		files = new ArrayList<>();

		saveConfigFileList();
	}

	public void removeFile(AugFile fileToRemove) {

		synchronized (files) {
			files.remove(fileToRemove);
		}

		saveConfigFileList();
	}

	public void saveConfigFileList() {

		// TODO :: ugly - fix me! (we are setting JSON via String, which works, but wäh... ^^)
		StringBuilder fileListBuilder = new StringBuilder();
		String sep = "";

		synchronized (files) {
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
		}

		activeWorkspace.set("files", new JSON("[" + fileListBuilder.toString() + "]"));

		configuration.create();
	}

	/**
	 * This saves all entries
	 */
	public void save() {

		synchronized (files) {
			for (AugFile file : files) {
				file.save();
			}
		}
	}

}
