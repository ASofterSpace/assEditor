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

	private ConfigFile configuration;

	private List<AugFile> files;
	

	public AugFileCtrl (ConfigFile configuration) {
	
		this.configuration = configuration;

		files = new ArrayList<>();

		JSON jsonConfig = configuration.getAllContents();
		
		List<JSON> jsonFiles = jsonConfig.getArray("files");
		
		if (jsonFiles != null) {
			for (JSON jsonFile : jsonFiles) {
				File fileToOpen = new File(jsonFile.asString());
				loadAnotherFile(fileToOpen);
			}
		}
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
	
	public AugFile loadAnotherFile(File fileToLoad) {
		
		AugFile result = new AugFile(this, fileToLoad);
		
		files.add(result);
		
		updateConfigFileList();
		
		return result;
	}
	
	public List<AugFile> getFiles() {
		return files;
	}
	
	public void removeAllFiles() {
		files = new ArrayList<>();
		
		updateConfigFileList();
	}
	
	public void removeFile(AugFile fileToRemove) {
		files.remove(fileToRemove);
		
		updateConfigFileList();
	}
	
	public void updateConfigFileList() {
	
		// TODO :: ugly - fix me! (we are setting JSON via String, which works, but wäh... ^^)
		JSON jsonConfig = configuration.getAllContents();
		
		StringBuilder fileListBuilder = new StringBuilder();
		String sep = "";
		
		for (AugFile augFile : files) {
			fileListBuilder.append("\"" + augFile.getFilename() + "\"");
			fileListBuilder.append(sep);
			sep = ", ";
		}
		
		configuration.set("files", new JSON("[" + fileListBuilder.toString() + "]"));
	/*
		
		List<JSON> jsonFiles = jsonConfig.getArray("files");
		
		if (jsonFiles != null) {
			for (JSON jsonFile : jsonFiles) {
				File fileToOpen = new File(jsonFile.asString());
				loadAnotherFile(fileToOpen);
			}
		}
		*/
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
