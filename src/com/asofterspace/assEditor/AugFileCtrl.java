/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;

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

	private List<AugFile> files;
	

	public AugFileCtrl () {
	
		files = new ArrayList<>();
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
		
		return result;
	}
	
	public List<AugFile> getFiles() {
		return files;
	}
	
	public void removeFile(AugFile fileToRemove) {
		files.remove(fileToRemove);
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
