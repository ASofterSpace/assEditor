/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

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
	private CodeKind sourceLanguage;


	public AugFile(AugFileCtrl parent, File file) {

		this(parent, new SimpleFile(file));
	}

	public AugFile(AugFileCtrl parent, SimpleFile file) {

		this.file = file;

		this.parent = parent;

		filename = file.getCanonicalFilename();
	}

	public CodeKind getSourceLanguage() {

		if (sourceLanguage == null) {
			sourceLanguage = getInitialSourceLanguage();
		}

		return sourceLanguage;
	}

	public CodeKind getInitialSourceLanguage() {

		String lowfilename = filename.toLowerCase();

		if (lowfilename.endsWith(".java")) {
			return CodeKind.JAVA;
		}

		if (lowfilename.endsWith(".groovy")) {
			return CodeKind.GROOVY;
		}

		if (lowfilename.endsWith(".cs")) {
			return CodeKind.CSHARP;
		}

		if (lowfilename.endsWith(".md")) {
			return CodeKind.MARKDOWN;
		}

		if (lowfilename.endsWith(".pas")) {
			return CodeKind.DELPHI;
		}

		if (lowfilename.endsWith(".php")) {
			return CodeKind.PHP;
		}

		if (lowfilename.endsWith(".htm") || lowfilename.endsWith(".html")) {
 			return CodeKind.HTML;
 		}

		if (lowfilename.endsWith(".xml")) {
			return CodeKind.XML;
		}

		if (lowfilename.endsWith(".js")) {
			return CodeKind.JAVASCRIPT;
		}

		if (lowfilename.endsWith(".json")) {
			return CodeKind.JSON;
		}

		if (lowfilename.endsWith(".css")) {
			return CodeKind.CSS;
		}

		if (lowfilename.endsWith(".sh")) {
			return CodeKind.SHELL;
		}

		if (lowfilename.endsWith(".py")) {
			return CodeKind.PYTHON;
		}

		return CodeKind.PLAINTEXT;
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

	public void setSourceLanguage(CodeKind language) {
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
