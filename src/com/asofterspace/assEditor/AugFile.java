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

	private AugFileTab associatedTab;

	protected AugFileCtrl parent;

	// the following are only used when the file is re-opened at a later date,
	// and caret pos and source language come from remembered configuration rather
	// than from the frontend - basically, the frontend gets its old state back
	// from the backend :)
	private Integer initialCaretPos;
	private String initialSourceLanguage;


	public AugFile(AugFileCtrl parent, File file) {

		this.file = file;

		this.parent = parent;

		setInitialSourceLanguage(CodeKind.PLAINTEXT);

		String filename = file.getFilename().toLowerCase();

		if (filename.endsWith(".java")) {
			setInitialSourceLanguage(CodeKind.JAVA);
		}

		if (filename.endsWith(".groovy")) {
			setInitialSourceLanguage(CodeKind.GROOVY);
		}

		if (filename.endsWith(".md")) {
			setInitialSourceLanguage(CodeKind.MARKDOWN);
		}

		if (filename.endsWith(".pas")) {
			setInitialSourceLanguage(CodeKind.DELPHI);
		}

		if (filename.endsWith(".php")) {
			setInitialSourceLanguage(CodeKind.PHP);
		}

		if (filename.endsWith(".js")) {
			setInitialSourceLanguage(CodeKind.JAVASCRIPT);
		}
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

	// used by the frontend to ask the backend about the source language to init with
	public String getInitialSourceLanguage() {
		return initialSourceLanguage;
	}

	// used by the backend to ask the frontend about the current source language
	public String getSourceLanguage() {
		if (associatedTab == null) {
			return null;
		}
		return associatedTab.getSourceLanguage();
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

	public void setInitialSourceLanguage(String language) {
		initialSourceLanguage = language;
	}

	public void setInitialSourceLanguage(CodeKind language) {
		if (language == null) {
			initialSourceLanguage = null;
		} else {
			initialSourceLanguage = language.toString();
		}
	}

	public void delete() {
		parent.removeFile(this);
		file.delete();
	}

	public void save() {
		file.save();
	}

}
