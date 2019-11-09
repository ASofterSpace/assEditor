/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.Record;
import com.asofterspace.toolbox.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * This class controls the opened files, which by the act of opening have become augmented files
 */
public class AugFileCtrl {

	private final static String CONF_FILENAME = "filename";
	private final static String CONF_CARET_POS = "caretPos";
	private final static String CONF_LANGUAGE = "language";
	private final static String CONF_ACCESS_TIME = "accessTime";

	private ConfigFile configuration;

	private List<AugFile> files;

	private Record activeWorkspace;

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

	public Record getWorkspace() {
		return activeWorkspace;
	}

	public List<String> getWorkspaces() {

		List<String> workspaces = new ArrayList<>();

		List<Record> recWorkspaces = configuration.getAllContents().getArray("workspaces");

		for (Record recWorkspace : recWorkspaces) {
			workspaces.add(recWorkspace.getString("name"));
		}

		return workspaces;
	}

	public void addWorkspace(String workspace) {

		List<String> workspacesSoFar = getWorkspaces();
		if (workspacesSoFar != null) {
			for (String workspaceSoFar : workspacesSoFar) {
				if (workspace.equals(workspaceSoFar)) {
					// there is already a workspace with this name!
					return;
				}
			}
		}

		Record recWorkspaces = configuration.getAllContents().get("workspaces");

		Record newWorkspace = new Record();

		Record fileRec = new Record();
		fileRec.makeArray();

		newWorkspace.set("files", fileRec);

		newWorkspace.setString("name", workspace);

		recWorkspaces.append(newWorkspace);

		configuration.create();
	}

	public void sortWorkspaces() {

		List<Record> recWorkspaces = configuration.getAllContents().getArray("workspaces");

		Collections.sort(recWorkspaces, new Comparator<Record>(){
			@Override
			public int compare(final Record a, final Record b) {
				return a.getString("name").compareToIgnoreCase(b.getString("name"));
			}
		});

		configuration.getAllContents().setArray("workspaces", recWorkspaces);

		configuration.create();
	}

	public void switchToWorkspace(String workspace) {

		List<Record> recWorkspaces = configuration.getAllContents().getArray("workspaces");

		for (Record recWorkspace : recWorkspaces) {
			if ((workspace == null) ||
				 workspace.equals(recWorkspace.getString("name"))) {
				switchToJsonWorkspace(recWorkspace);
				return;
			}
		}
	}

	private void switchToJsonWorkspace(Record workspace) {

		activeWorkspace = workspace;

		configuration.getAllContents().setString("activeWorkspace", workspace.getString("name"));

		files = new ArrayList<>();

		List<Record> recFiles = workspace.getArray("files");

		if (recFiles != null) {
			for (Record recFile : recFiles) {

				File fileToOpen = new File(recFile.getString(CONF_FILENAME));

				AugFile curFile = loadAnotherFileWithoutSaving(fileToOpen);

				if (curFile != null) {
					curFile.setInitialCaretPos(recFile.getInteger(CONF_CARET_POS));

					CodeLanguage sourceLang = CodeLanguage.getFromString(recFile.getString(CONF_LANGUAGE));

					curFile.setSourceLanguage(sourceLang);

					curFile.setLastAccessTime(DateUtils.parseDateTime(recFile.getString(CONF_ACCESS_TIME)));
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

		// System.out.println("Loading " + fileToLoad + "...");

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

		StringBuilder fileListBuilder = new StringBuilder();

		Record filesRec = new Record();

		synchronized (files) {

			for (AugFile augFile : files) {

				Record curRec = new Record();

				curRec.setString(CONF_FILENAME, augFile.getFilename());
				curRec.setString(CONF_CARET_POS, augFile.getCaretPos());
				curRec.setString(CONF_LANGUAGE, augFile.getSourceLanguage());
				curRec.setString(CONF_ACCESS_TIME, DateUtils.serializeDateTime(augFile.getLastAccessTime()));

				filesRec.append(curRec);
			}
		}

		activeWorkspace.set("files", filesRec);

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
