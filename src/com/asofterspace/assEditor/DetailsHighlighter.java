/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.utils.Callback;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.DocumentEvent;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;


public class DetailsHighlighter extends DefaultStyledDocument {

	// the end-of-line marker
	private static final String EOL = "\n";

	// indicates boldness
	private static final char BOLD_INDICATOR = '*';

	// the root element of the document, through which we can get the individual lines
	private Element root;

	// are we currently in a multiline bold area?
	private boolean curMultilineBold;
	
	// the callback to be called when something changes
	private Callback onChangeCallback;

	// styles for the different kinds of text in the document
	private static MutableAttributeSet attrBold;
	private static MutableAttributeSet attrRegular;

	// the editor that is to be decorated by us
	private final JTextPane decoratedEditor;
	
	// the list of all decorated editors
	private static List<DetailsHighlighter> instances = new ArrayList<>();
	
	// the font sizes, fonts and tab sets of all editors
	private static int fontSize = 15;
	private static String editorFontFamily;
	private static Font lastFont;
	private static TabSet lastTabSet;


	public DetailsHighlighter(JTextPane editor) {

		super();
		
		// keep track of the editor we are decorating (useful e.g. to get and set caret pos during insert operations)
		decoratedEditor = editor;

		// keep track of the root element
		root = this.getDefaultRootElement();

		// declare which end of line marker is to be used
		putProperty(DefaultEditorKit.EndOfLineStringProperty, EOL);
		
		// initialize the font size, lastFont etc. if they have not been initialized before
		if (lastFont == null) {
			setFontSize(fontSize);
		}

		// initialize all the attribute sets, if they have not been initialized before
		if (attrRegular == null) {
			attrBold = new SimpleAttributeSet();
			StyleConstants.setForeground(attrBold, new Color(0, 0, 0));
			StyleConstants.setBold(attrBold, true);

			attrRegular = new SimpleAttributeSet();
			StyleConstants.setForeground(attrRegular, new Color(0, 0, 0));
		}

		// actually style the editor with... us
		decoratedEditor.setDocument(this);
		// applySchemeAndFontToOurEditor();
		
		instances.add(this);
	}
	
	public static void setFontSize(int newSize) {
	
		/*
		fontSize = newSize;

		if (editorFontFamily == null) {
			lastFont = new Font("", Font.PLAIN, fontSize);
		} else {
			lastFont = new Font(editorFontFamily, Font.PLAIN, fontSize);
		}
		
		applySchemeAndFontToAllEditors();
		*/
	}

	public void setOnChange(Callback callback) {

		onChangeCallback = callback;
	}
	
	@Override
	public void insertString(int offset, String insertedString, AttributeSet attrs) {

		int origCaretPos = decoratedEditor.getCaretPosition();

		boolean overrideCaretPos = false;

		// automagically close brackets that are being opened
		switch (insertedString) {
			case "{":
				insertedString = "{}";
				overrideCaretPos = true;
				break;
			case "(":
				insertedString = "()";
				overrideCaretPos = true;
				break;
			case "[":
				insertedString = "[]";
				overrideCaretPos = true;
				break;
		}

		try {
			super.insertString(offset, insertedString, attrs);
		} catch (BadLocationException e) {
			// oops!
		}

		highlightText(offset, insertedString.length());

		if (overrideCaretPos) {
			decoratedEditor.setCaretPosition(origCaretPos + 1);
		}
		
		if (onChangeCallback != null) {
			onChangeCallback.call();
		}
	}

	@Override
	protected void fireInsertUpdate(DocumentEvent event) {

		super.fireInsertUpdate(event);

		highlightText(event.getOffset(), event.getLength());
	}

	@Override
	public void remove(int offset, int length) {

		try {
			super.remove(offset, length);
		} catch (BadLocationException e) {
			// oops!
		}

		highlightText(offset, 0);
		
		if (onChangeCallback != null) {
			onChangeCallback.call();
		}
	}

	@Override
	protected void fireRemoveUpdate(DocumentEvent event) {

		super.fireRemoveUpdate(event);

		highlightText(event.getOffset(), event.getLength());
	}
	
	private void highlightAllText() {
		highlightText(0, this.getLength());
	}

	// this is the main function that... well... hightlights our text :)
	private void highlightText(int start, int length) {

		try {
			int end = this.getLength();
			
			String content = this.getText(0, end);
			
			// set the entire document back to regular
			this.setCharacterAttributes(0, end, attrRegular, true);

			// TODO :: actually use the start and length passed in as arguments!
			// (currently, they are just being ignored...)
			start = 0;
			int cur = start;
			end -= 1;
			
			Integer boldStart = null;

			while (cur <= end) {

				if (BOLD_INDICATOR == content.charAt(cur)) {
					if (boldStart == null) {
						boldStart = cur;
					} else {
						this.setCharacterAttributes(boldStart, cur - boldStart + 1, attrBold, false);
						boldStart = null;
					}
				}
				
				cur++;
			}
			
		} catch (BadLocationException e) {
			// oops!
		}
	}
}
