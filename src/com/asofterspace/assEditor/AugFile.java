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
	private String initialSourceLanguage;


	public AugFile(AugFileCtrl parent, File file) {

		this(parent, new SimpleFile(file));
	}

	public AugFile(AugFileCtrl parent, SimpleFile file) {

		this.file = file;

		this.parent = parent;

		filename = file.getCanonicalFilename();

		resetInitialSourceLanguage();
	}

	public void resetInitialSourceLanguage() {

		String lowfilename = filename.toLowerCase();

		setInitialSourceLanguage(CodeKind.PLAINTEXT);

		if (lowfilename.endsWith(".java")) {
 			setInitialSourceLanguage(CodeKind.JAVA);
 		}

		if (lowfilename.endsWith(".groovy")) {
 			setInitialSourceLanguage(CodeKind.GROOVY);
 		}

		if (lowfilename.endsWith(".cs")) {
 			setInitialSourceLanguage(CodeKind.CSHARP);
 		}

		if (lowfilename.endsWith(".md")) {
 			setInitialSourceLanguage(CodeKind.MARKDOWN);
 		}

		if (lowfilename.endsWith(".pas")) {
 			setInitialSourceLanguage(CodeKind.DELPHI);
 		}

		if (lowfilename.endsWith(".php")) {
 			setInitialSourceLanguage(CodeKind.PHP);
 		}

		if (lowfilename.endsWith(".htm") || lowfilename.endsWith(".html")) {
 			setInitialSourceLanguage(CodeKind.HTML);
 		}

		if (lowfilename.endsWith(".js")) {
 			setInitialSourceLanguage(CodeKind.JAVASCRIPT);
 		}

		if (lowfilename.endsWith(".json")) {
 			setInitialSourceLanguage(CodeKind.JSON);
 		}

		if (lowfilename.endsWith(".css")) {
 			setInitialSourceLanguage(CodeKind.CSS);
 		}
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
