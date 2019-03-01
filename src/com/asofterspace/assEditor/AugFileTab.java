/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.Code;
import com.asofterspace.toolbox.codeeditor.CodeLocation;
import com.asofterspace.toolbox.codeeditor.CSharpCode;
import com.asofterspace.toolbox.codeeditor.CssCode;
import com.asofterspace.toolbox.codeeditor.GroovyCode;
import com.asofterspace.toolbox.codeeditor.HtmlCode;
import com.asofterspace.toolbox.codeeditor.JavaCode;
import com.asofterspace.toolbox.codeeditor.JavaScriptCode;
import com.asofterspace.toolbox.codeeditor.JsonCode;
import com.asofterspace.toolbox.codeeditor.LineNumbering;
import com.asofterspace.toolbox.codeeditor.MarkdownCode;
import com.asofterspace.toolbox.codeeditor.PhpCode;
import com.asofterspace.toolbox.codeeditor.PlainText;
import com.asofterspace.toolbox.codeeditor.PythonCode;
import com.asofterspace.toolbox.codeeditor.ShellCode;
import com.asofterspace.toolbox.codeeditor.XmlCode;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CodeEditor;
import com.asofterspace.toolbox.gui.CodeEditorLineMemo;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.Utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.FontMetrics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import javax.swing.border.CompoundBorder;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;

public class AugFileTab {

	private JPanel parent;

	private JPanel visualPanel;

	private AugFile augFile;

	private AugFileCtrl augFileCtrl;

	private Code highlighter;
	private Code lineNumbers;

	private GUI gui;

	private Callback onChangeCallback;

	private boolean changed = false;

	// graphical components
	private JLabel nameLabel;
	private JTextPane lineMemo;
	private JTextPane fileContentMemo;
	private JTextPane functionMemo;
	private JScrollPane sideScrollPane;


	public AugFileTab(JPanel parentPanel, AugFile augFile, final GUI gui, AugFileCtrl augFileCtrl) {

		this.parent = parentPanel;

		this.augFile = augFile;
		augFile.setTab(this);

		this.augFileCtrl = augFileCtrl;

		this.gui = gui;

		this.onChangeCallback = new Callback() {
			public void call() {
				if (!changed) {
					changed = true;
					gui.regenerateAugFileList();
				}
			}
		};

		visualPanel = createVisualPanel();

		setCodeKindAndCreateHighlighter();
	}

	private JPanel createVisualPanel() {

		JPanel tab = new JPanel();
		tab.setLayout(new GridBagLayout());

		nameLabel = new JLabel(getFullName());
		nameLabel.setPreferredSize(new Dimension(0, nameLabel.getPreferredSize().height*2));
		tab.add(nameLabel, new Arrangement(0, 0, 1.0, 0.0));

		nameLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				StringSelection selection = new StringSelection(nameLabel.getText());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
			}
		});

		JPanel scrolledPanel = new JPanel();
		scrolledPanel.setLayout(new GridBagLayout());

		fileContentMemo = new CodeEditor();
		lineMemo = new CodeEditorLineMemo();
		lineNumbers = new LineNumbering(lineMemo, fileContentMemo);

		String content = augFile.getContent();
		fileContentMemo.setText(content);

		scrolledPanel.add(lineMemo, new Arrangement(0, 0, 0.0, 1.0));
		scrolledPanel.add(fileContentMemo, new Arrangement(1, 0, 1.0, 1.0));

		JScrollPane sourceCodeScroller = new JScrollPane(scrolledPanel);
		sourceCodeScroller.setPreferredSize(new Dimension(1, 1));
		tab.add(sourceCodeScroller, new Arrangement(0, 1, 1.0, 0.8));

		Integer origCaretPos = augFile.getInitialCaretPos();

		if (origCaretPos == null) {
			// scroll to the top
			fileContentMemo.setCaretPosition(0);
		} else {
			// scroll to the last stored position
			origCaretPos = Math.min(origCaretPos, content.length());
			fileContentMemo.setCaretPosition(origCaretPos);
		}


		functionMemo = new CodeEditor();

		sideScrollPane = new JScrollPane(functionMemo);
		sideScrollPane.setPreferredSize(new Dimension(1, 1));
		sideScrollPane.getViewport().setBackground(Color.white);

		tab.add(sideScrollPane, new Arrangement(1, 1, 0.2, 1.0));

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

				List<CodeLocation> functions = highlighter.getFunctions();

				if ((functions == null) || (functions.size() < 1)) {
					return;
				}

				int pressedLine = -1;
				int caretPos = functionMemo.getCaretPosition();

				for (CodeLocation codeLoc : functions) {
					pressedLine++;
					caretPos -= codeLoc.getCode().length() + 1;
					if (caretPos < 0) {
						break;
					}
				}

				final int targetCaretPos = functions.get(pressedLine).getCaretPos();
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
					}
				}).start();
			}
		});

		tab.setVisible(false);

		parent.add(tab);

		return tab;
	}

	private void resizeNameLabel() {

		String text = getFullName();

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

	public boolean hasBeenChanged() {

		return changed;
	}

	public String getName() {

		return augFile.getName();
	}

		public String getFullName() {

				return augFile.getFilename();
		}

	/*
	public void setName(String newName) {

		nameLabel.setText("Name: " + newName);

		changed = true;

		augFile.setName(newName);
	}
	*/

	public void setChanged(boolean changed) {

		this.changed = changed;
	}

	public void show() {

		visualPanel.setVisible(true);

		resizeNameLabel();
	}

	public void hide() {

		visualPanel.setVisible(false);
	}

	public void setCodeKindAndCreateHighlighter(CodeKind codeKind) {

		// tell the associated file about this...
		augFile.setSourceLanguage(codeKind);

		setCodeKindAndCreateHighlighter();
	}

	public void setCodeKindAndCreateHighlighter() {

		// ... and get what the file made of it (e.g. transferring null to the initial default)
		CodeKind codeKind = augFile.getSourceLanguage();

		if (highlighter != null) {
			highlighter.discard();
		}

		if (codeKind == null) {
			highlighter = new PlainText(fileContentMemo);
		} else {
			switch (codeKind) {
				case JAVA:
					highlighter = new JavaCode(fileContentMemo);
					break;
				case GROOVY:
					highlighter = new GroovyCode(fileContentMemo);
					break;
				case CSHARP:
					highlighter = new CSharpCode(fileContentMemo);
					break;
				case MARKDOWN:
					highlighter = new MarkdownCode(fileContentMemo);
					break;
				case CSS:
					highlighter = new CssCode(fileContentMemo);
					break;
 				case HTML:
 					highlighter = new HtmlCode(fileContentMemo);
 					break;
				case XML:
					highlighter = new XmlCode(fileContentMemo);
					break;
				case PHP:
					highlighter = new PhpCode(fileContentMemo);
					break;
				case JAVASCRIPT:
					highlighter = new JavaScriptCode(fileContentMemo);
					break;
				case JSON:
					highlighter = new JsonCode(fileContentMemo);
					break;
				case PYTHON:
					highlighter = new PythonCode(fileContentMemo);
					break;
				case SHELL:
					highlighter = new ShellCode(fileContentMemo);
					break;
				default:
					highlighter = new PlainText(fileContentMemo);
			}
		}

		if (highlighter.suppliesFunctions()) {

			sideScrollPane.setVisible(true);

			highlighter.setFunctionTextField(functionMemo);

		} else {

			sideScrollPane.setVisible(false);
		}

		highlighter.setOnChange(onChangeCallback);

		highlighter.setCodeEditorLineMemo(lineMemo);

		updateHighlighterConfig();
	}

	public void updateHighlighterConfig() {

		// update color scheme
		switch (gui.currentScheme) {
			case GUI.LIGHT_SCHEME:
				highlighter.setLightScheme();
				lineNumbers.setLightScheme();
				break;
			case GUI.DARK_SCHEME:
				highlighter.setDarkScheme();
				lineNumbers.setDarkScheme();
				break;
		}

		// update copy on enter behavior
		highlighter.setCopyOnCtrlEnter(gui.copyOnEnter);

		// update block tab behavior
		highlighter.setTabEntireBlocks(gui.tabEntireBlocks);
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
		return fileContentMemo.getCaretPosition();
	}

	public CodeKind getSourceLanguage() {

		return augFile.getSourceLanguage();
	}

	public void applyGit() {

		String[] codeLines = fileContentMemo.getText().split("\n");

		StringBuilder sourceCode = new StringBuilder();

		for (String line : codeLines) {

			// ignore lines starting with - entirely (thus removing them)
			if (line.startsWith("-")) {
				continue;
			}

			// remove the + from lines starting with it
			if (line.startsWith("+")) {
				line = line.substring(1);
			}

			// treat all other lines (not starting with + or -) indifferently
			sourceCode.append(line);
			sourceCode.append("\n");
		}

		fileContentMemo.setText(sourceCode.toString());
	}

	private void setCaretPos(int newSelStart, int newSelEnd) {

		fileContentMemo.setCaretPosition(newSelStart);
		fileContentMemo.setSelectionStart(newSelStart);
		fileContentMemo.setSelectionEnd(newSelEnd);
	}

	public void selectAll() {

		int newSelStart = 0;
		int newSelEnd = fileContentMemo.getText().length();

		setCaretPos(newSelStart, newSelEnd);
	}

	public void selectFromHere() {

		int newSelStart = fileContentMemo.getCaretPosition();
		int newSelEnd = fileContentMemo.getText().length();

		setCaretPos(newSelStart, newSelEnd);
	}

	public void selectToHere() {

		int newSelStart = 0;
		int newSelEnd = fileContentMemo.getCaretPosition();

		setCaretPos(newSelStart, newSelEnd);
	}

	private String lastSearched = null;

	public void search(String searchFor) {

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

	public void replaceAll(String searchFor, String replaceWith) {

		String text = fileContentMemo.getText();

		text = text.replace(searchFor, replaceWith);

		fileContentMemo.setText(text);

		highlighter.setSearchStr("");
	}

	public void backup(int backupNum) {

		// set the backup file location relative to the class path to always
		// get the same location, even when we are called from somewhere else
		SimpleFile backupFile = new SimpleFile(System.getProperty("java.class.path") + "/../backup/" + Utils.leftPad0(backupNum, 4) + ".txt");

		backupFile.setContent(augFile.getFilename() + "\n\n" + fileContentMemo.getText());

		backupFile.create();
	}

	public void save() {

		String contentText = fileContentMemo.getText();

		if (gui.removeTrailingWhitespaceOnSave) {

			int origCaretPos = fileContentMemo.getCaretPosition();

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

			contentText = newContent.toString();

			fileContentMemo.setText(contentText);

			fileContentMemo.setCaretPosition(origCaretPos);
		}

		augFile.setContent(contentText);

		changed = false;

		augFile.save();

		gui.regenerateAugFileList();
	}

	public void reorganizeImports() {

		highlighter.reorganizeImports();
	}

	public void saveIfChanged() {

		if (changed) {
			save();
		}
	}

	public void remove() {

		parent.remove(visualPanel);
	}

	public void delete() {

		augFile.delete();

		remove();
	}

}
