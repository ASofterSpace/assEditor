/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.configuration.ConfigFile;
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
	private final static String CONF_WORKSPACES = "workspaces";
	private final static String CONF_ARCHIVED_WORKSPACES = "workspacesArchived";
	private final static String CONF_ACTIVE_WORKSPACE = "activeWorkspace";
	private final static String CONF_WORKSPACE_NAME = "name";
	private final static String CONF_WORKSPACE_FILES = "files";
	private final static String STANDALONE_WORKSPACE_NAME = "Standalone Non-Persistent Workspace";

	private ConfigFile configuration;

	private List<AugFile> files = new ArrayList<>();

	private Record activeWorkspace;

	private Thread saveConfigThread;


	public AugFileCtrl(ConfigFile configuration, boolean standalone) {

		this.configuration = configuration;

		String activeWorkspaceName = configuration.getValue(CONF_ACTIVE_WORKSPACE);

		if (standalone) {
			activeWorkspaceName = STANDALONE_WORKSPACE_NAME;

			removeWorkspace(activeWorkspaceName);

			Record standaloneWorkspace = addWorkspace(activeWorkspaceName);

			List<Record> recFiles = standaloneWorkspace.getArray(CONF_WORKSPACE_FILES);

			Record recFile = new Record();

			recFile.set(CONF_FILENAME, "untitled");
			recFile.set(CONF_CARET_POS, 0);
			recFile.set(CONF_LANGUAGE, CodeLanguage.PLAINTEXT.toString());
			recFile.set(CONF_ACCESS_TIME, DateUtils.serializeDateTime(null));

			recFiles.add(recFile);
		}

		switchToWorkspace(activeWorkspaceName);
	}

	public String getWorkspaceName() {
		return activeWorkspace.getString(CONF_WORKSPACE_NAME);
	}

	public Record getWorkspace() {
		return activeWorkspace;
	}

	public List<String> getWorkspaces() {
		return getWorkspaces(CONF_WORKSPACES);
	}

	public List<String> getArchivedWorkspaces() {
		return getWorkspaces(CONF_ARCHIVED_WORKSPACES);
	}

	private List<String> getWorkspaces(String key) {

		List<String> workspaces = new ArrayList<>();

		List<Record> recWorkspaces = configuration.getAllContents().getArray(key);

		for (Record recWorkspace : recWorkspaces) {
			if (!STANDALONE_WORKSPACE_NAME.equals(recWorkspace.getString(CONF_WORKSPACE_NAME))) {
				workspaces.add(recWorkspace.getString(CONF_WORKSPACE_NAME));
			}
		}

		return workspaces;
	}

	public void removeWorkspace(String workspace) {

		Record recWorkspaceHolder = configuration.getAllContents().get(CONF_WORKSPACES);
		List<Record> recWorkspaces = recWorkspaceHolder.getValues();

		int i = 0;
		int foundAt = -1;
		for (Record recWorkspace : recWorkspaces) {
			if (workspace.equals(recWorkspace.getString(CONF_WORKSPACE_NAME))) {
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

		Record recWorkspaces = configuration.getAllContents().get(CONF_WORKSPACES);

		Record newWorkspace = new Record();

		Record fileRec = new Record();
		fileRec.makeArray();

		newWorkspace.set(CONF_WORKSPACE_FILES, fileRec);

		newWorkspace.setString(CONF_WORKSPACE_NAME, workspace);

		recWorkspaces.append(newWorkspace);

		configuration.create();

		return newWorkspace;
	}

	public void sortWorkspaces() {

		List<Record> recWorkspaces = configuration.getAllContents().getArray(CONF_WORKSPACES);
		sortWorkspaceList(recWorkspaces);
		configuration.getAllContents().setArray(CONF_WORKSPACES, recWorkspaces);

		configuration.create();
	}

	private void sortWorkspaceList(List<Record> recWorkspaces) {

		Collections.sort(recWorkspaces, new Comparator<Record>(){
			@Override
			public int compare(final Record a, final Record b) {
				return a.getString(CONF_WORKSPACE_NAME).compareToIgnoreCase(b.getString(CONF_WORKSPACE_NAME));
			}
		});
	}

	public void archiveWorkspace(String workspace) {
		archiveOrUnarchiveWorkspace(workspace, CONF_WORKSPACES, CONF_ARCHIVED_WORKSPACES, true);
	}

	public void unarchiveWorkspace(String workspace) {
		archiveOrUnarchiveWorkspace(workspace, CONF_ARCHIVED_WORKSPACES, CONF_WORKSPACES, false);
	}

	/**
	 * Move a workspace from the archive to the main list - or the other way around. :)
	 * (And optionally sort the target list afterwards.)
	 */
	private void archiveOrUnarchiveWorkspace(String workspace, String originKey, String targetKey, boolean sortTarget) {

		if (workspace == null) {
			return;
		}

		List<Record> recWorkspaces = configuration.getAllContents().getArray(originKey);
		List<Record> newWorkspaces = new ArrayList<>();
		Record recordToMove = null;

		// search for the record to be archived among the current workspace records
		for (Record cur : recWorkspaces) {
			if (workspace.equals(cur.getString(CONF_WORKSPACE_NAME))) {
				recordToMove = cur;
			} else {
				newWorkspaces.add(cur);
			}
		}

		// if there is nothing to do...
		if (recordToMove == null) {
			// ... do nothing!
			return;
		}

		configuration.getAllContents().setArray(originKey, newWorkspaces);

		List<Record> targetWorkspaces = configuration.getAllContents().getArray(targetKey);
		targetWorkspaces.add(recordToMove);
		if (sortTarget) {
			sortWorkspaceList(targetWorkspaces);
		}
		configuration.getAllContents().setArray(targetKey, targetWorkspaces);

		configuration.create();
	}

	public void switchToWorkspace(String workspace) {

		// save the current state of the previous workspace
		saveConfigFileList();

		List<Record> recWorkspaces = configuration.getAllContents().getArray(CONF_WORKSPACES);

		for (Record recWorkspace : recWorkspaces) {
			if ((workspace == null) ||
				workspace.equals(recWorkspace.getString(CONF_WORKSPACE_NAME))) {
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

	public String getActiveWorkspaceName() {
		return activeWorkspace.getString(CONF_WORKSPACE_NAME);
	}

	private void switchToJsonWorkspace(Record workspace) {

		activeWorkspace = workspace;

		if (!STANDALONE_WORKSPACE_NAME.equals(workspace.getString(CONF_WORKSPACE_NAME))) {
			configuration.getAllContents().setString(CONF_ACTIVE_WORKSPACE, workspace.getString(CONF_WORKSPACE_NAME));
		}

		files = new ArrayList<>();

		List<Record> recFiles = workspace.getArray(CONF_WORKSPACE_FILES);

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

	public void addTabsToWorkspace(List<AugFileTab> tabs, String workspaceName) {

		List<AugFile> augFiles = new ArrayList<>();

		for (AugFileTab tab : tabs) {
			augFiles.add(tab.getFile());
		}

		addFilesToWorkspace(augFiles, workspaceName);
	}

	public void addFilesToWorkspace(List<AugFile> augFiles, String workspaceName) {

		List<Record> recWorkspaces = configuration.getAllContents().getArray(CONF_WORKSPACES);

		for (Record recWorkspace : recWorkspaces) {
			if (workspaceName.equals(recWorkspace.getString(CONF_WORKSPACE_NAME))) {

				Record filesRec = recWorkspace.get(CONF_WORKSPACE_FILES);

				addFilesToConfig(augFiles, filesRec);

				return;
			}
		}
	}

	public void saveConfigFileList() {

		StringBuilder fileListBuilder = new StringBuilder();

		Record filesRec = new Record();

		if (activeWorkspace == null) {
			return;
		}

		activeWorkspace.set(CONF_WORKSPACE_FILES, filesRec);

		synchronized (files) {
			addFilesToConfig(files, filesRec);
		}
	}

	private void addFilesToConfig(List<AugFile> augFiles, Record parentElement) {

		for (AugFile augFile : augFiles) {

			Record curRec = new Record();

			curRec.setString(CONF_FILENAME, augFile.getFilename());
			curRec.setString(CONF_CARET_POS, augFile.getCaretPos());
			curRec.setString(CONF_LANGUAGE, augFile.getSourceLanguage());
			curRec.setString(CONF_ACCESS_TIME, DateUtils.serializeDateTime(augFile.getLastAccessTime()));

			parentElement.append(curRec);
		}

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
