/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.io.File;


/**
 * This is an augmented file - a file that is augmented with metadata
 */
public class AugFile {

	private File file;

	protected AugFileCtrl parent;


	public AugFile(AugFileCtrl parent, File file) {

		this.file = file;

		this.parent = parent;
	}

	public String getName() {
		return file.getLocalFilename();
	}

	public String getFilename() {
		return file.getFilename();
	}

	public String getContent() {
		return file.getContent();
	}

	public void setContent(String content) {
		file.setContent(content);
	}

	public void delete() {
		parent.removeFile(this);
		file.delete();
	}

	public void save() {
		file.save();
	}

}
