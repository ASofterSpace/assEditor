/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.SimpleFile;


/**
 * This is an augmented file - a file that is augmented with metadata
 */
public class AugFile {

	private SimpleFile file;

	private String filename;

	private AugFileTab associatedTab;

	protected AugFileCtrl parent;

	// the following are only used when the file is re-opened at a later date,
	// and caret pos and source language come from remembered configuration rather
	// than from the frontend - basically, the frontend gets its old state back
	// from the backend :)
	private Integer initialCaretPos;
	private CodeLanguage sourceLanguage;


	public AugFile(AugFileCtrl parent, File file) {

		this(parent, new SimpleFile(file));
	}

	public AugFile(AugFileCtrl parent, SimpleFile file) {

		this.file = file;

		this.parent = parent;

		filename = file.getCanonicalFilename();
	}

	public CodeLanguage getSourceLanguage() {

		if (sourceLanguage == null) {
			sourceLanguage = getInitialSourceLanguage();
		}

		return sourceLanguage;
	}

	public CodeLanguage getInitialSourceLanguage() {

		return CodeLanguage.getFromFilename(filename);
	}

	public String getName() {
		return file.getLocalFilename();
	}

	public String getFilename() {
		return filename;
	}

	public String getContent() {
		return file.getContent();
	}

	// used by the frontend to ask the backend about the caret pos to init with
	public Integer getInitialCaretPos() {
		return initialCaretPos;
	}

	// used by the backend to ask the frontend about the current caret pos
	public Integer getCaretPos() {
		if (associatedTab == null) {
			return null;
		}
		return associatedTab.getCaretPos();
	}

	public void setContent(String content) {
		file.setContent(content);
	}

	public void setTab(AugFileTab tab) {
		associatedTab = tab;
	}

	public void setInitialCaretPos(Integer caretPos) {
		initialCaretPos = caretPos;
	}

	public void setSourceLanguage(CodeLanguage language) {
		sourceLanguage = language;
	}

	public void delete() {
		parent.removeFile(this);
		file.delete();
	}

	public void save() {
		file.save();
	}

}
