/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.codeeditor.utils.CodeHighlighterFactory;
import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.codeeditor.utils.CodeSnippetWithLocation;
import com.asofterspace.toolbox.codeeditor.utils.LineNumbering;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CodeEditor;
import com.asofterspace.toolbox.gui.CodeEditorLineMemo;
import com.asofterspace.toolbox.gui.FileTab;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.guiImages.FancyCodeEditor;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.io.XML;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.utils.SortOrder;
import com.asofterspace.toolbox.utils.StringModifier;
import com.asofterspace.toolbox.utils.StrUtils;
import com.asofterspace.toolbox.utils.TextEncoding;

import java.awt.Color;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.FontMetrics;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;


public class AugFileTab implements FileTab {

	private JPanel parent;

	private JPanel visualPanel;

	private AugFile augFile;

	private AugFileCtrl augFileCtrl;

	private Code highlighter;
	private Code lineNumbers;

	private MainGUI mainGUI;

	private Callback onChangeCallback;

	// has the file been loaded from the drive?
	private boolean loaded = false;

	// has the file been changed since loading?
	private boolean changed = false;

	// has the file been highlighted? (we highlighted it when it appears in the search results
	// while searching through the workspace)
	private boolean highlighted = false;

	// graphical components
	private JPanel tab;
	private JPanel topHUD;
	private JLabel goBackLabel;
	private JLabel goForwardLabel;
	private boolean goBackEnabled = true;
	private boolean goForwardEnabled = true;
	private JLabel nameLabel;
	private JTextPane lineMemo;
	private CodeEditor fileContentMemo;
	private JScrollPane sourceCodeScroller;
	private FancyCodeEditor functionMemo;
	private JScrollPane sideScrollPane;

	// the original caret position - as global variable such that other functions
	// can more easily modify it cleverly
	private int origCaretPos;
	private int newCaretPos;

	private int selectionOrder = 0;
	private String defaultIndentationStr = "\t";


	public AugFileTab(JPanel parentPanel, AugFile augFile, final MainGUI mainGUI, AugFileCtrl augFileCtrl) {

		this.parent = parentPanel;

		this.augFile = augFile;
		augFile.setTab(this);

		this.augFileCtrl = augFileCtrl;

		this.mainGUI = mainGUI;

		this.onChangeCallback = new Callback() {
			public void call() {
				if (!changed) {
					changed = true;
					repaintAugFileListWhileWeAreVisible();
				}
			}
		};
	}

	private void repaintAugFileListWhileWeAreVisible() {

		// this tab is already visible, so we do not need to show it again,
		// therefore instead of calling the slow mainGUI.regenerateAugFileList();,
		// we call the following:
		boolean resize = true;
		mainGUI.highlightTabInLeftListOrTree(AugFileTab.this, resize);
	}

	private JPanel createVisualPanel() {

		tab = new JPanel();
		tab.setLayout(new GridBagLayout());

		topHUD = new JPanel();
		topHUD.setLayout(new GridBagLayout());

		goBackLabel = new JLabel("  < ");
		topHUD.add(goBackLabel, new Arrangement(0, 0, 0.0, 0.0));

		goBackLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mainGUI.goToPreviousTab();
			}
		});

		goForwardLabel = new JLabel(" > ");
		topHUD.add(goForwardLabel, new Arrangement(1, 0, 0.0, 0.0));

		goForwardLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mainGUI.goToNextTab();
			}
		});

		nameLabel = new JLabel(getFilePath());
		nameLabel.setPreferredSize(new Dimension(0, nameLabel.getPreferredSize().height*2));
		nameLabel.setHorizontalAlignment(JLabel.CENTER);
		topHUD.add(nameLabel, new Arrangement(2, 0, 1.0, 1.0));

		nameLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				StringSelection selection = new StringSelection(getFilePath());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
			}
		});

		tab.add(topHUD, new Arrangement(0, 0, 1.0, 0.0));

		JPanel mainPart = new JPanel();
		mainPart.setLayout(new GridBagLayout());

		JPanel scrolledPanel = new JPanel();
		scrolledPanel.setLayout(new GridBagLayout());

		fileContentMemo = new CodeEditor();
		fileContentMemo.enableStartLine(true);
		fileContentMemo.enableHorzLine(true);
		lineMemo = new CodeEditorLineMemo();
		lineNumbers = new LineNumbering(lineMemo, fileContentMemo);

		scrolledPanel.add(lineMemo, new Arrangement(0, 0, 0.0, 1.0));
		scrolledPanel.add(fileContentMemo, new Arrangement(1, 0, 1.0, 1.0));

		sourceCodeScroller = new JScrollPane(scrolledPanel);

		// scroll blazingly fast! :D
		sourceCodeScroller.getVerticalScrollBar().setUnitIncrement(99);
		sourceCodeScroller.getHorizontalScrollBar().setUnitIncrement(48);

		sourceCodeScroller.setPreferredSize(new Dimension(1, 1));
		sourceCodeScroller.setBorder(BorderFactory.createEmptyBorder());
		mainPart.add(sourceCodeScroller, new Arrangement(0, 1, 1.0, 0.8));


		functionMemo = new FancyCodeEditor();
		functionMemo.setGradientBackground(true);
		AssEditor.addStampTo(functionMemo);

		JPanel scrolledFunctionPanel = new JPanel();
		scrolledFunctionPanel.setLayout(new GridBagLayout());
		scrolledFunctionPanel.add(functionMemo, new Arrangement(0, 0, 1.0, 1.0));

		sideScrollPane = new JScrollPane(scrolledFunctionPanel);
		sideScrollPane.setBorder(BorderFactory.createEmptyBorder());
		sideScrollPane.setPreferredSize(new Dimension(1, 1));

		mainPart.add(sideScrollPane, new Arrangement(1, 1, 0.2, 1.0));

		functionMemo.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// scrollToFunction(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// scrollToFunction(e);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				scrollToFunction(e);
			}

			private void scrollToFunction(MouseEvent e) {

				ensureLoaded();

				CodeSnippetWithLocation curFunction = highlighter.getClickedFunction();

				if (curFunction == null) {
					return;
				}

				highlighter.stopFunctionHighlighting();

				final int targetCaretPos = curFunction.getCaretPos();

				// jump to the end...
				fileContentMemo.setCaretPosition(fileContentMemo.getText().length());
				new Thread(new Runnable() {
					public void run() {
						try {
							// ... and a couple milliseconds later...
							Thread.sleep(50);
						} catch (InterruptedException e) {
							// ... or earlier, if you insist...
						}
						// ... jump to the actual location (such that the
						// location is definitely at the TOP of the screen)
						fileContentMemo.setCaretPosition(targetCaretPos);

						highlighter.startFunctionHighlighting();
					}
				}).start();
			}
		});

		tab.add(mainPart, new Arrangement(0, 1, 1.0, 1.0));

		tab.setVisible(false);

		parent.add(tab);

		return tab;
	}

	private void resizeNameLabel() {

		String text = getFilePath();

		FontMetrics metrics = nameLabel.getFontMetrics(nameLabel.getFont());

		int textWidth = metrics.stringWidth(text);
		int labelWidth = nameLabel.getWidth();

		if (textWidth > labelWidth) {

			int len = text.length();
			String lastText = text;

			for (int i = 1; i <= len; i++) {

				String curText = "..." + text.substring(len - i, len);
				textWidth = metrics.stringWidth(curText);

				if (textWidth > labelWidth) {
					break;
				}

				lastText = curText;
			}

			text = lastText;
		}

		nameLabel.setText(text);
	}

	public AugFile getAugFile() {
		return augFile;
	}

	public boolean isItem(String item) {

		if (item == null) {
			return false;
		}

		if (augFile == null) {
			return false;
		}

		return item.equals(augFile.getName());
	}

	@Override
	public boolean isHighlighted() {

		return highlighted;
	}

	@Override
	public boolean isMissing() {

		return !augFile.exists();
	}

	@Override
	public boolean hasBeenChanged() {

		return changed;
	}

	public String getName() {

		return augFile.getName();
	}

	@Override
	public String getFilePath() {

		return augFile.getFilename();
	}

	@Override
	public String getDirectoryName() {

		return augFile.getParentDirectory().getAbsoluteDirname();
	}

	/*
	public void setName(String newName) {

		nameLabel.setText("Name: " + newName);

		this.onChangeCallback.call();

		augFile.setName(newName);
	}
	*/

	public void setChanged(boolean changed) {

		this.changed = changed;
	}

	public void show() {

		ensureLoaded();

		visualPanel.setVisible(true);

		resizeNameLabel();
	}

	public void hide() {

		if (visualPanel != null) {
			visualPanel.setVisible(false);
		}
	}

	public void setCodeLanguageAndCreateHighlighter(CodeLanguage codeKind) {

		// tell the associated file about this...
		augFile.setSourceLanguage(codeKind);

		setCodeLanguageAndCreateHighlighter();
	}

	public void setCodeLanguageAndCreateHighlighter() {

		// do nothing if this tab has not yet been initialized
		if (lineMemo == null) {
			return;
		}

		if (highlighter != null) {
			highlighter.discard();
		}

		// ... and get what the file made of it (e.g. transferring null to the initial default)
		CodeLanguage codeKind = augFile.getSourceLanguage();

		highlighter = CodeHighlighterFactory.getHighlighterForLanguage(codeKind, fileContentMemo);

		highlighter.setDefaultIndentation(defaultIndentationStr);

		if (highlighter.suppliesFunctions()) {

			sideScrollPane.setVisible(true);

			highlighter.setFunctionTextField(functionMemo);

		} else {

			sideScrollPane.setVisible(false);
		}

		highlighter.setOnChange(onChangeCallback);

		AugFileOpenCallback onOpenFileCallback = new AugFileOpenCallback(
			augFile.getParentDirectory(),
			mainGUI,
			augFileCtrl
		);
		highlighter.setOnOpenFile(onOpenFileCallback);

		highlighter.setCodeEditorLineMemo(lineMemo);

		highlighter.setFontSize(mainGUI.getFontSize());

		updateHighlighterConfig();
	}

	public void setComponentScheme(String scheme) {

		// a tab might be pre-loaded but not yet shown in which case it will receive a scheme
		// update... and just ignore it ^^
		if (tab == null) {
			return;
		}

		switch (scheme) {
			case GuiUtils.LIGHT_SCHEME:
				tab.setForeground(Color.black);
				tab.setBackground(new Color(235, 215, 255));
				nameLabel.setForeground(Color.black);
				nameLabel.setBackground(new Color(235, 215, 255));
				GuiUtils.setCornerColor(sourceCodeScroller, JScrollPane.LOWER_RIGHT_CORNER, new Color(235, 215, 255));
				GuiUtils.setCornerColor(sideScrollPane, JScrollPane.LOWER_RIGHT_CORNER, new Color(235, 215, 255));
				fileContentMemo.setStartLineColor(Color.lightGray);
				fileContentMemo.setHorzLineColor(Color.lightGray);
				break;
			case GuiUtils.DARK_SCHEME:
				tab.setForeground(new Color(255, 245, 255));
				tab.setBackground(Color.black);
				nameLabel.setForeground(new Color(255, 245, 255));
				nameLabel.setBackground(Color.black);
				GuiUtils.setCornerColor(sourceCodeScroller, JScrollPane.LOWER_RIGHT_CORNER, Color.black);
				GuiUtils.setCornerColor(sideScrollPane, JScrollPane.LOWER_RIGHT_CORNER, Color.black);
				fileContentMemo.setStartLineColor(Color.darkGray);
				fileContentMemo.setHorzLineColor(Color.darkGray);
				break;
		}

		topHUD.setForeground(tab.getForeground());
		topHUD.setBackground(tab.getBackground());

		showGoBack(goBackEnabled);
		goBackLabel.setBackground(nameLabel.getBackground());
		showGoForward(goForwardEnabled);
		goForwardLabel.setBackground(nameLabel.getBackground());

		MainGUI.setScheme(scheme, sideScrollPane);
		MainGUI.setScheme(scheme, sourceCodeScroller);
	}

	public void updateHighlighterConfig() {

		// update color scheme
		setComponentScheme(mainGUI.currentScheme);

		if (highlighter == null) {
			return;
		}

		switch (mainGUI.currentScheme) {
			case GuiUtils.LIGHT_SCHEME:
				highlighter.setLightScheme();
				lineNumbers.setLightScheme();
				break;
			case GuiUtils.DARK_SCHEME:
				highlighter.setDarkScheme();
				lineNumbers.setDarkScheme();
				break;
		}

		// also set the scroll pane color, as the scroll pane might be visible when the text is very short
		sideScrollPane.getViewport().setBackground(highlighter.getBackgroundColor());

		/*
		// update copy on enter behavior
		highlighter.setCopyOnCtrlEnter(mainGUI.copyOnEnter);
		*/

		// update block tab behavior
		highlighter.setTabEntireBlocks(mainGUI.tabEntireBlocks);

		// update proposed token autocompletion behavior
		highlighter.setProposeTokenAutoComplete(mainGUI.proposeTokenAutoComplete);
	}

	public void setFileContent(String newContent) {

		// set the new entry content (without saving it anywhere)
		fileContentMemo.setText(newContent);

		// scroll to the top
		fileContentMemo.setCaretPosition(0);
	}

	public AugFile getFile() {
		return augFile;
	}

	public Integer getCaretPos() {
		if (fileContentMemo == null) {
			return augFile.getInitialCaretPos();
		}
		return fileContentMemo.getCaretPosition();
	}

	public CodeLanguage getSourceLanguage() {

		return augFile.getSourceLanguage();
	}

	public void applyGit(boolean inverted) {

		ensureLoaded();

		String[] codeLines = fileContentMemo.getText().split("\n");

		StringBuilder sourceCode = new StringBuilder();

		for (String line : codeLines) {

			// ignore lines starting with - entirely (thus removing them)
			if ((!inverted) && line.startsWith("-")) {
				continue;
			}
			if (inverted && line.startsWith("+")) {
				continue;
			}

			// remove the + from lines starting with it,
			// and remove the space from lines starting with it, if then a tab follows
			// (if we have spacespacespace, then it is unclear if the space should be removed,
			// as we could just be indenting with spaces anyway...)
			if (line.startsWith("+") || line.startsWith("-") || line.startsWith(" \t")) {
				line = line.substring(1);
			}

			// treat all other lines (not starting with + or -) indifferently
			sourceCode.append(line);
			sourceCode.append("\n");
		}

		fileContentMemo.setText(sourceCode.toString());
	}

	public void removeDebugLines() {

		ensureLoaded();

		String[] codeLines = fileContentMemo.getText().split("\n");

		StringBuilder sourceCode = new StringBuilder();

		for (String line : codeLines) {

			// ignore Java-like DEBUG comments
			if (line.contains("// "+"DEBUG")) {
				continue;
			}

			// ignore bash-like DEBUG comments
			if (line.contains("# "+"DEBUG")) {
				continue;
			}

			// treat all other lines indifferently
			sourceCode.append(line);
			sourceCode.append("\n");
		}

		fileContentMemo.setText(sourceCode.toString());
	}

	public void removeCommentsAndStrings() {

		ensureLoaded();

		String newContent = highlighter.removeCommentsAndStrings(fileContentMemo.getText());

		fileContentMemo.setText(newContent);
	}

	public void removeXmlTags() {

		ensureLoaded();

		String newContent = XML.removeXmlTagsFromText(fileContentMemo.getText());

		fileContentMemo.setText(newContent);
	}

	public void writeLineNumbers() {

		ensureLoaded();

		String[] codeLines = fileContentMemo.getText().split("\n");

		StringBuilder sourceCode = new StringBuilder();

		int i = 0;

		for (String line : codeLines) {
			i++;
			sourceCode.append(i);
			sourceCode.append(": ");
			sourceCode.append(line);
			sourceCode.append("\n");
		}

		fileContentMemo.setText(sourceCode.toString());
	}

	public void removeLineNumbers() {

		ensureLoaded();

		String[] codeLines = fileContentMemo.getText().split("\n");

		StringBuilder sourceCode = new StringBuilder();

		String sep = "";

		for (String line : codeLines) {
			int index = line.indexOf(": ");
			if (index >= 0) {
				boolean cutBeginning = true;
				for (int i = 0; i < index; i++) {
					if (!Character.isDigit(line.charAt(i))) {
						cutBeginning = false;
						break;
					}
				}
				if (cutBeginning) {
					line = line.substring(index + 2);
				}
			}
			sourceCode.append(sep);
			sep = "\n";
			sourceCode.append(line);
		}

		fileContentMemo.setText(sourceCode.toString());
	}

	public void duplicateCurrentLine() {

		ensureLoaded();

		String sourceCode = fileContentMemo.getText();

		int carPos = fileContentMemo.getCaretPosition();

		int lineStart = StrUtils.getLineStartFromPosition(carPos, sourceCode);
		int lineEnd = StrUtils.getLineEndFromPosition(carPos, sourceCode);

		String insertStr = sourceCode.substring(lineStart, lineEnd);
		if (!insertStr.startsWith("\n")) {
			insertStr = "\n" + insertStr;
		}

		// if duplicating a package line in Java, actually duplicate as import line
		CodeLanguage lang = augFile.getSourceLanguage();
		if ((lang == CodeLanguage.JAVA) || (lang == CodeLanguage.GROOVY)) {
			if (insertStr.startsWith("\npackage ")) {
				insertStr = "\nimport " + insertStr.substring(9);
			}
		}

		sourceCode = sourceCode.substring(0, lineEnd) + insertStr + sourceCode.substring(lineEnd);

		fileContentMemo.setText(sourceCode);

		fileContentMemo.setCaretPosition(carPos + insertStr.length());
	}

	public void deleteCurrentLine() {

		ensureLoaded();

		String sourceCode = fileContentMemo.getText();

		int carPos = fileContentMemo.getCaretPosition();

		int lineStart = StrUtils.getLineStartFromPosition(carPos, sourceCode);
		int lineEnd = StrUtils.getLineEndFromPosition(carPos, sourceCode);

		// ignore trailing newline
		if (lineStart > 0) {
			lineStart--;
		}

		sourceCode = sourceCode.substring(0, lineStart) + sourceCode.substring(lineEnd);

		fileContentMemo.setText(sourceCode);

		if (carPos > sourceCode.length()) {
			carPos = sourceCode.length();
		}

		// set the caret to the next line (well, to where the next line will be after deleting...)
		int newPos = lineStart + 1;
		if (newPos > sourceCode.length()) {
			newPos = sourceCode.length();
		}
		fileContentMemo.setCaretPosition(newPos);
	}

	/**
	 * Modifies the selected text, if any is selected, or otherwise the entire text
	 */
	public void modifySelectedOrAllText(StringModifier modifier) {

		ensureLoaded();

		String sourceCode = fileContentMemo.getText();

		int carPos = fileContentMemo.getCaretPosition();
		int selStart = fileContentMemo.getSelectionStart();
		int selEnd = fileContentMemo.getSelectionEnd();

		String midStr = sourceCode.substring(selStart, selEnd);

		if (midStr.length() > 0) {

			midStr = modifier.modify(midStr);

			sourceCode = sourceCode.substring(0, selStart) + midStr + sourceCode.substring(selEnd);

			fileContentMemo.setText(sourceCode);
			if (carPos > selStart) {
				fileContentMemo.setCaretPosition(selStart + midStr.length());
			} else {
				fileContentMemo.setCaretPosition(carPos);
			}

		} else {

			sourceCode = modifier.modify(sourceCode);

			fileContentMemo.setText(sourceCode);
		}
	}

	/**
	 * Inserts text at the cursor position
	 */
	public void insertText(String text) {

		ensureLoaded();

		String sourceCode = fileContentMemo.getText();

		int carPos = fileContentMemo.getCaretPosition();
		int selStart = fileContentMemo.getSelectionStart();
		int selEnd = fileContentMemo.getSelectionEnd();

		String midStr = text;

		sourceCode = sourceCode.substring(0, selStart) + midStr + sourceCode.substring(selEnd);

		fileContentMemo.setText(sourceCode);
		if (carPos > selStart) {
			fileContentMemo.setCaretPosition(selStart + midStr.length());
		} else {
			fileContentMemo.setCaretPosition(carPos);
		}
	}

	public void lowCurSel() {

		ensureLoaded();

		String sourceCode = fileContentMemo.getText();

		int carPos = fileContentMemo.getCaretPosition();
		int selStart = fileContentMemo.getSelectionStart();
		int selEnd = fileContentMemo.getSelectionEnd();

		String lowStr = sourceCode.substring(selStart, selEnd);
		lowStr = lowStr.toLowerCase();

		sourceCode = sourceCode.substring(0, selStart) + lowStr + sourceCode.substring(selEnd);

		fileContentMemo.setText(sourceCode);
		fileContentMemo.setCaretPosition(carPos);
	}

	public void upLowCurSel() {

		ensureLoaded();

		String sourceCode = fileContentMemo.getText();

		int carPos = fileContentMemo.getCaretPosition();
		int selStart = fileContentMemo.getSelectionStart();
		int selEnd = fileContentMemo.getSelectionEnd();

		String upLowStr = sourceCode.substring(selStart, selEnd);
		if (upLowStr.length() > 0) {
			upLowStr = upLowStr.substring(0, 1).toUpperCase() + upLowStr.substring(1).toLowerCase();
		}

		sourceCode = sourceCode.substring(0, selStart) + upLowStr + sourceCode.substring(selEnd);

		fileContentMemo.setText(sourceCode);
		fileContentMemo.setCaretPosition(carPos);
	}

	public void upCurSel() {

		ensureLoaded();

		String sourceCode = fileContentMemo.getText();

		int carPos = fileContentMemo.getCaretPosition();
		int selStart = fileContentMemo.getSelectionStart();
		int selEnd = fileContentMemo.getSelectionEnd();

		String upStr = sourceCode.substring(selStart, selEnd);
		upStr = upStr.toUpperCase();

		sourceCode = sourceCode.substring(0, selStart) + upStr + sourceCode.substring(selEnd);

		fileContentMemo.setText(sourceCode);
		fileContentMemo.setCaretPosition(carPos);
	}

	public void lowCurWord() {

		ensureLoaded();

		String sourceCode = fileContentMemo.getText();

		int carPos = fileContentMemo.getCaretPosition();

		int selStart = StrUtils.getWordStartFromPosition(carPos, sourceCode, true);
		int selEnd = StrUtils.getWordEndFromPosition(carPos, sourceCode, true);

		String lowStr = sourceCode.substring(selStart, selEnd);
		lowStr = lowStr.toLowerCase();

		sourceCode = sourceCode.substring(0, selStart) + lowStr + sourceCode.substring(selEnd);

		fileContentMemo.setText(sourceCode);
		fileContentMemo.setCaretPosition(carPos);
	}

	public void upLowCurWord() {

		ensureLoaded();

		String sourceCode = fileContentMemo.getText();

		int carPos = fileContentMemo.getCaretPosition();

		int selStart = StrUtils.getWordStartFromPosition(carPos, sourceCode, true);
		int selEnd = StrUtils.getWordEndFromPosition(carPos, sourceCode, true);

		String upLowStr = sourceCode.substring(selStart, selEnd);
		if (upLowStr.length() > 0) {
			upLowStr = upLowStr.substring(0, 1).toUpperCase() + upLowStr.substring(1).toLowerCase();
		}

		sourceCode = sourceCode.substring(0, selStart) + upLowStr + sourceCode.substring(selEnd);

		fileContentMemo.setText(sourceCode);
		fileContentMemo.setCaretPosition(carPos);
	}

	public void upCurWord() {

		ensureLoaded();

		String sourceCode = fileContentMemo.getText();

		int carPos = fileContentMemo.getCaretPosition();

		int selStart = StrUtils.getWordStartFromPosition(carPos, sourceCode, true);
		int selEnd = StrUtils.getWordEndFromPosition(carPos, sourceCode, true);

		String upStr = sourceCode.substring(selStart, selEnd);
		upStr = upStr.toUpperCase();

		sourceCode = sourceCode.substring(0, selStart) + upStr + sourceCode.substring(selEnd);

		fileContentMemo.setText(sourceCode);
		fileContentMemo.setCaretPosition(carPos);
	}

	public void indentSelection(String indentWithWhat) {

		highlighter.indentSelection(indentWithWhat);
	}

	public void unindentSelection(int levelAmount, boolean forceUnindent) {

		highlighter.unindentSelection(levelAmount, forceUnindent);
	}

	private void setCaretPos(int newSelStart, int newSelEnd) {

		fileContentMemo.setCaretPosition(newSelStart);
		fileContentMemo.setSelectionStart(newSelStart);
		fileContentMemo.setSelectionEnd(newSelEnd);
	}

	public void selectAll() {

		ensureLoaded();

		int newSelStart = 0;
		int newSelEnd = fileContentMemo.getText().length();

		setCaretPos(newSelStart, newSelEnd);
	}

	public void selectFromHere() {

		ensureLoaded();

		int newSelStart = fileContentMemo.getCaretPosition();
		int newSelEnd = fileContentMemo.getText().length();

		setCaretPos(newSelStart, newSelEnd);
	}

	public void selectToHere() {

		ensureLoaded();

		int newSelStart = 0;
		int newSelEnd = fileContentMemo.getCaretPosition();

		setCaretPos(newSelStart, newSelEnd);
	}

	private String lastSearched = null;

	public void search(String searchFor) {

		ensureLoaded();

		String text = fileContentMemo.getText();

		int curpos = fileContentMemo.getCaretPosition();

		// if we already searched for the exact same string before, but it was shorter (or
		// longer, but definitely not the same), then continue searching at the same position,
		// so that we are not jumping around needlessly while someone is entering their search
		// query letter-by-letter
		if (lastSearched != null) {
			if (searchFor != null) {
				if (lastSearched.length() < searchFor.length()) {
					if (searchFor.startsWith(lastSearched)) {
						curpos -= searchFor.length();
					}
				}
				if (lastSearched.length() > searchFor.length()) {
					if (lastSearched.startsWith(searchFor)) {
						curpos -= lastSearched.length();
					}
				}
			}
		}

		lastSearched = searchFor;

		int nextpos = text.indexOf(searchFor, curpos);

		if (nextpos < 0) {
			nextpos = text.indexOf(searchFor);
		}

		if (nextpos >= 0) {
			setCaretPos(nextpos, nextpos + searchFor.length());
		}

		highlighter.setSearchStr(searchFor);
	}

	/**
	 * Searches for text within this tab and adds the resulting matches to the result builder
	 * Also outputs the amount of matches as returned int
	 */
	public int searchAndAddResultTo(String searchFor, StringBuilder result) {

		// do not ensure loaded, as it is enough for the file contents to be loaded,
		// while it is NOT necessary for this tab to actually load the file contents

		String text = augFile.getContent();

		if (fileContentMemo != null) {
			text = fileContentMemo.getText();
		}

		int matchAmount = 0;

		int nextpos = text.indexOf(searchFor);

		if (nextpos < 0) {
			// un-highlight, nothing was found
			this.highlighted = false;

			return matchAmount;
		}

		// highlight, as something was found in this file!
		this.highlighted = true;

		result.append(getFilePath());
		result.append(":\n\n");

		int oldfrom = -1;
		int oldto = -1;

		while (nextpos >= 0) {

			int line = StrUtils.getLineNumberFromPosition(nextpos, text);

			int newfrom = line - 1;
			int newto = line + 1;

			if (newfrom <= oldto) {
				// let's group matches
				oldto = newto;
			} else {
				addCodeToResultFromLineToLine(oldfrom, oldto, text, result);
				oldfrom = newfrom;
				oldto = newto;
			}

			nextpos = text.indexOf(searchFor, nextpos + 1);

			matchAmount++;
		}

		addCodeToResultFromLineToLine(oldfrom, oldto, text, result);

		result.append("\n");

		return matchAmount;
	}

	private void addCodeToResultFromLineToLine(int from, int to, String text, StringBuilder result) {

		if (from < 0) {
			from = 0;
		}

		for (int i = from; i < to + 1; i++) {
			result.append(i);
			result.append(": ");
			result.append(StrUtils.getLineFromNumber(i, text));
			result.append("\n");
		}

		// if we actually appended something, then append a newline in the end
		if (to >= 0) {
			result.append("\n");
		}
	}

	/**
	 * Searches for a text and replaces it with a different text
	 * Returns true if at least one match was found
	 */
	public boolean replaceAll(String searchFor, String replaceWith) {

		boolean foundIt;

		if (loaded) {

			String text = fileContentMemo.getText();

			foundIt = text.contains(searchFor);

			if (foundIt) {

				text = text.replace(searchFor, replaceWith);

				fileContentMemo.setText(text);

				highlighter.setSearchStr("");

				this.onChangeCallback.call();
			}

		} else {

			String text = augFile.getContent();

			foundIt = text.contains(searchFor);

			if (foundIt) {

				text = text.replace(searchFor, replaceWith);

				augFile.setContent(text);

				this.onChangeCallback.call();
			}
		}

		return foundIt;
	}

	public void backup(int backupNum) {

		ensureLoaded();

		// set the backup file location relative to the class path to always
		// get the same location, even when we are called from somewhere else
		SimpleFile backupFile = new SimpleFile(AssEditor.getBackupPath() + "content_" + StrUtils.leftPad0(backupNum, 4) + ".txt");

		backupFile.setContent(augFile.getFilename() + "\n\n" + fileContentMemo.getText());

		backupFile.create();
	}

	public void save() {

		if (loaded) {

			String contentText = fileContentMemo.getText();

			origCaretPos = fileContentMemo.getCaretPosition();

			if (mainGUI.addMissingImportsOnSave) {

				contentText = highlighter.addMissingImports(contentText);
			}

			if (mainGUI.removeUnusedImportsOnSave) {

				contentText = highlighter.removeUnusedImports(contentText);
			}

			if (mainGUI.reorganizeImportsOnSave) {

				contentText = highlighter.reorganizeImports(contentText);
			}

			if (mainGUI.reorganizeImportsOnSaveCompatible) {

				contentText = highlighter.reorganizeImportsCompatible(contentText);
			}

			if (mainGUI.replaceWhitespacesWithTabsOnSave) {

				contentText = replaceLeadingWhitespacesWithTabs(contentText);
			}

			if (mainGUI.replaceTabsWithWhitespacesOnSave) {

				contentText = replaceLeadingTabsWithWhitespaces(contentText);
			}

			if (mainGUI.removeTrailingWhitespaceOnSave) {

				contentText = removeTrailingWhitespace(contentText);
			}

			fileContentMemo.setText(contentText);

			if (origCaretPos > contentText.length()) {
				origCaretPos = contentText.length();
			}

			fileContentMemo.setCaretPosition(origCaretPos);

			augFile.setContent(contentText);
		}

		changed = false;

		augFile.ensureContents();

		augFile.save();

		repaintAugFileListWhileWeAreVisible();
	}

	public void addMissingImports() {

		highlighter.addMissingImports();
	}

	public void reorganizeImports() {

		highlighter.reorganizeImports();
	}

	public void reorganizeImportsCompatible() {

		highlighter.reorganizeImportsCompatible();
	}

	public void removeUnusedImports() {

		highlighter.removeUnusedImports();
	}

	public void sortDocument(SortOrder order) {

		highlighter.sortDocument(order);
	}

	public void sortSelectedLines(SortOrder order) {

		highlighter.sortSelectedLines(order);
	}

	public void sortSelectedStrings(SortOrder order) {

		highlighter.sortSelectedStrings(order);
	}

	public void replaceLeadingWhitespacesWithTabs() {

		ensureLoaded();

		String contentText = fileContentMemo.getText();

		origCaretPos = fileContentMemo.getCaretPosition();

		newCaretPos = origCaretPos;

		contentText = replaceLeadingWhitespacesWithTabs(contentText);

		fileContentMemo.setText(contentText);

		fileContentMemo.setCaretPosition(newCaretPos);
	}

	private String replaceLeadingWhitespacesWithTabs(String contentText) {

		StringBuilder result = new StringBuilder();

		boolean startOfLine = true;

		int curcAmount = 0;

		for (int i = 0; i < contentText.length(); i++) {

			char c = contentText.charAt(i);

			if ((c == '\n') || (c == '\r')) {
				if (curcAmount > 0) {
					for (int j = 0; j < curcAmount; j++) {
						result.append(' ');
					}
					curcAmount = 0;
				}
				result.append(c);
				startOfLine = true;
				continue;
			}

			if (startOfLine) {
				if (c == ' ') {
					curcAmount++;

					if (curcAmount > 3) {
						curcAmount -= 4;
						result.append('\t');

						if (i < origCaretPos) {
							newCaretPos -= 3;
						}

					}
					continue;
				}

				// in case of a tab, do not set startOfLine to false, as we want
				// newline + tab + space + space to transform also!
				if (c == '\t') {
					result.append('\t');
					continue;
				}
			}

			if (curcAmount > 0) {
				for (int j = 0; j < curcAmount; j++) {
					result.append(' ');
				}
				curcAmount = 0;
			}
			result.append(c);
			startOfLine = false;
		}

		return result.toString();
	}

	public void replaceLeadingTabsWithWhitespaces() {

		ensureLoaded();

		String contentText = fileContentMemo.getText();

		origCaretPos = fileContentMemo.getCaretPosition();

		newCaretPos = origCaretPos;

		contentText = replaceLeadingTabsWithWhitespaces(contentText);

		fileContentMemo.setText(contentText);

		fileContentMemo.setCaretPosition(newCaretPos);
	}

	private String replaceLeadingTabsWithWhitespaces(String contentText) {

		StringBuilder result = new StringBuilder();

		boolean startOfLine = true;

		for (int i = 0; i < contentText.length(); i++) {

			char c = contentText.charAt(i);

			if ((c == '\n') || (c == '\r')) {
				result.append(c);
				startOfLine = true;
				continue;
			}

			if (startOfLine) {
				if (c == '\t') {
					result.append("    ");
					continue;
				}

				// in case of a space, do not set startOfLine to false, as we want
				// newline + space + tab to transform also!
				if (c == ' ') {
					result.append(' ');
					continue;
				}
			}

			result.append(c);
			startOfLine = false;
		}

		return result.toString();
	}

	public void removeTrailingWhitespace() {

		ensureLoaded();

		String contentText = fileContentMemo.getText();

		origCaretPos = fileContentMemo.getCaretPosition();

		contentText = removeTrailingWhitespace(contentText);

		fileContentMemo.setText(contentText);

		fileContentMemo.setCaretPosition(origCaretPos);
	}

	private String removeTrailingWhitespace(String contentText) {

		StringBuilder newContent = new StringBuilder();

		int i = 0;
		int start = 0;

		// we always go until the last \n, so we append a \n here...
		contentText += "\n";

		for (; i < contentText.length(); i++) {

			char curChar = contentText.charAt(i);

			if (curChar == '\n') {
				if (i > 0) {
					int end = i-1; // we need to go one back, to go back from \n
					while ((contentText.charAt(end) == ' ') || (contentText.charAt(end) == '\t')) {
						end--;
						if (end < origCaretPos) {
							origCaretPos--;
						}
						if (end < 0) {
							end = 0;
							break;
						}
					}
					if (end+1 > start) {
						newContent.append(contentText.substring(start, end+1));
					}
				}
				newContent.append('\n');
				start = i+1;
			}
		}

		// ... and remove the appended \n here again
		newContent.setLength(newContent.length() - 1);

		return newContent.toString();
	}

	public void count100Up() {

		ensureLoaded();

		StringBuilder result = new StringBuilder();

		String contentText = fileContentMemo.getText();

		origCaretPos = fileContentMemo.getCaretPosition();

		String[] lines = contentText.split("\n");

		for (String line : lines) {
			result.append(line);
			result.append("\n");
		}

		String lastLine = "";
		int count = lines.length;
		if (count-1 >= 0) {
			lastLine = lines[count-1];
		}
		if (lastLine.equals("") && (count-2 >= 0)) {
			lastLine = lines[count-2];
		}
		int startNum = 0;
		try {
			startNum = Integer.valueOf(lastLine);
		} catch (NumberFormatException e) {
			// just start at 0
		}
		for (int i = startNum + 1; i <= startNum + 100; i++) {
			result.append(i);
			result.append("\n");
		}

		fileContentMemo.setText(result.toString());

		fileContentMemo.setCaretPosition(origCaretPos);
	}

	public void removeUntilFirstOccurrence(String needle) {

		ensureLoaded();

		String contentText = fileContentMemo.getText();

		String[] lines = contentText.split("\n");
		StringBuilder result = new StringBuilder();

		for (String line : lines) {
			int index = line.indexOf(needle);
			if (index >= 0) {
				line = line.substring(index + needle.length());
				result.append(line);
			}
			result.append("\n");
		}

		fileContentMemo.setText(result.toString());
	}

	public void removeAfterLastOccurrence(String needle) {

		ensureLoaded();

		String contentText = fileContentMemo.getText();

		String[] lines = contentText.split("\n");
		StringBuilder result = new StringBuilder();

		for (String line : lines) {
			int index = line.lastIndexOf(needle);
			if (index >= 0) {
				line = line.substring(0, index);
				result.append(line);
			}
			result.append("\n");
		}

		fileContentMemo.setText(result.toString());
	}

	public void deleteAllLinesContainingText(String needle, boolean invert) {

		ensureLoaded();

		String contentText = fileContentMemo.getText();

		origCaretPos = fileContentMemo.getCaretPosition();

		String[] lines = contentText.split("\n");
		StringBuilder result = new StringBuilder();

		for (String line : lines) {
			if (invert == line.contains(needle)) {
				result.append(line);
				result.append("\n");
			}
		}

		fileContentMemo.setText(result.toString());

		fileContentMemo.setCaretPosition(origCaretPos);
	}

	public void deleteAllLinesStartingWithText(String needle, boolean invert) {

		ensureLoaded();

		String contentText = fileContentMemo.getText();

		origCaretPos = fileContentMemo.getCaretPosition();

		String[] lines = contentText.split("\n");
		StringBuilder result = new StringBuilder();

		for (String line : lines) {
			if (invert == line.startsWith(needle)) {
				result.append(line);
				result.append("\n");
			}
		}

		fileContentMemo.setText(result.toString());

		fileContentMemo.setCaretPosition(origCaretPos);
	}

	public TextEncoding getEncoding() {
		return augFile.getEncoding();
	}

	public void setEncoding(TextEncoding encoding) {

		boolean changed = augFile.getEncoding() != encoding;

		augFile.setEncoding(encoding);

		if (changed) {
			this.onChangeCallback.call();
		}
	}

	public void undo() {

		highlighter.undo();
	}

	public void redo() {

		highlighter.redo();
	}

	public void saveIfChanged() {

		if (changed) {
			save();
		}
	}

	public void setFocus() {

		if (fileContentMemo == null) {
			return;
		}

		fileContentMemo.grabFocus();
		fileContentMemo.requestFocus();
	}

	public void remove() {
		if ((parent != null) && (visualPanel != null)) {
			parent.remove(visualPanel);
		}
	}

	public void delete() {

		augFile.delete();

		remove();
	}

	/**
	 * Ensure the tab (and the file content) has actually been loaded
	 */
	private void ensureLoaded() {

		if (!loaded) {

			visualPanel = createVisualPanel();

			String content = augFile.getContent();

			fileContentMemo.setText(content);

			loaded = true;

			Integer origCaretPos = augFile.getInitialCaretPos();

			if (origCaretPos == null) {
				// scroll to the top
				fileContentMemo.setCaretPosition(0);
			} else {
				// scroll to the last stored position
				origCaretPos = Math.min(origCaretPos, content.length());
				fileContentMemo.setCaretPosition(origCaretPos);
			}

			setCodeLanguageAndCreateHighlighter();
		}
	}

	public void showGoBack(boolean doShow) {

		goBackEnabled = doShow;

		if (doShow) {
			goBackLabel.setForeground(nameLabel.getForeground());
		} else {
			goBackLabel.setForeground(new Color(128, 128, 128));
		}
	}

	public void showGoForward(boolean doShow) {

		goForwardEnabled = doShow;

		if (doShow) {
			goForwardLabel.setForeground(nameLabel.getForeground());
		} else {
			goForwardLabel.setForeground(new Color(128, 128, 128));
		}
	}

	public void addConstructor() {
		highlighter.addConstructor();
	}

	public void addGetters() {
		highlighter.addGetters();
	}

	public void addSetters() {
		highlighter.addSetters();
	}

	public void addGettersAndSetters() {
		highlighter.addGettersAndSetters();
	}

	public void addEquals() {
		highlighter.addEquals();
	}

	public void addToString() {
		highlighter.addToString();
	}

	public void setDefaultIndent(String defaultIndentationStr) {
		this.defaultIndentationStr = defaultIndentationStr;
		if (highlighter != null) {
			highlighter.setDefaultIndentation(defaultIndentationStr);
		}
	}

	public void jumpToLine(int lineNum) {

		ensureLoaded();

		// we subtract 1, because humans start at 1, but internally we start counting lines at 0
		int lineStart = StrUtils.getLineStartFromNumber(lineNum - 1, fileContentMemo.getText());

		fileContentMemo.setCaretPosition(lineStart);
	}

	public String getContent() {

		ensureLoaded();

		return fileContentMemo.getText();
	}

	public void setContent(String newContent) {

		ensureLoaded();

		fileContentMemo.setText(newContent);
	}

	public void setSelectionOrder(int selOrder) {
		this.selectionOrder = selOrder;
	}

	@Override
	public int getSelectionOrder() {
		return selectionOrder;
	}

	@Override
	public String toString() {
		if (hasBeenChanged()) {
			return getName() + GuiUtils.CHANGE_INDICATOR;
		}
		return getName();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof AugFileTab) {
			AugFileTab otherTab = (AugFileTab) other;
			if (otherTab.getFilePath() != null) {
				if (otherTab.getFilePath().equals(this.getFilePath())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		String fullName = this.getFilePath();
		if (fullName == null) {
			return 0;
		}
		return fullName.hashCode();
	}

}
