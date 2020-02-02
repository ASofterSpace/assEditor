/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;

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
	private final static String STANDALONE_WORKSPACE_NAME = "Standalone Non-Persistent Workspace";

	private ConfigFile configuration;

	private List<AugFile> files = new ArrayList<>();

	private Record activeWorkspace;

	private Thread saveConfigThread;


	public AugFileCtrl(ConfigFile configuration, boolean standalone) {

		this.configuration = configuration;

		String activeWorkspaceName = configuration.getValue("activeWorkspace");

		if (standalone) {
			activeWorkspaceName = STANDALONE_WORKSPACE_NAME;

			removeWorkspace(activeWorkspaceName);

			Record standaloneWorkspace = addWorkspace(activeWorkspaceName);

			List<Record> recFiles = standaloneWorkspace.getArray("files");

			Record recFile = new Record();

			recFile.set(CONF_FILENAME, "untitled");
			recFile.set(CONF_CARET_POS, 0);
			recFile.set(CONF_LANGUAGE, CodeLanguage.PLAINTEXT.toString());
			recFile.set(CONF_ACCESS_TIME, DateUtils.serializeDateTime(null));

			recFiles.add(recFile);
		}

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

	public void removeWorkspace(String workspace) {

		Record recWorkspaceHolder = configuration.getAllContents().get("workspaces");
		List<Record> recWorkspaces = recWorkspaceHolder.getValues();

		int i = 0;
		int foundAt = -1;
		for (Record recWorkspace : recWorkspaces) {
			if (workspace.equals(recWorkspace.getString("name"))) {
				foundAt = i;
			}
			i++;
		}
		if (foundAt >= 0) {
			recWorkspaceHolder.removeIndex(foundAt);
		}
	}

	/**
	 * Tries to add a new workspace and returns the added workspace if one was added,
	 * or null if none was added as it already existed
	 */
	public Record addWorkspace(String workspace) {

		List<String> workspacesSoFar = getWorkspaces();
		if (workspacesSoFar != null) {
			for (String workspaceSoFar : workspacesSoFar) {
				if (workspace.equals(workspaceSoFar)) {
					// there is already a workspace with this name!
					return null;
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

		return newWorkspace;
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

		// if none are found, just use the first one...
		if (recWorkspaces.size() > 0) {
			switchToJsonWorkspace(recWorkspaces.get(0));
		}
	}

	public Record getActiveWorkspace() {
		return activeWorkspace;
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
