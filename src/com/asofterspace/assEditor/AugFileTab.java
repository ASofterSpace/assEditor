/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.codeeditor.Code;
import com.asofterspace.toolbox.codeeditor.GroovyCode;
import com.asofterspace.toolbox.codeeditor.JavaCode;
import com.asofterspace.toolbox.codeeditor.MarkdownCode;
import com.asofterspace.toolbox.codeeditor.PlainText;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.utils.Callback;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.border.CompoundBorder;
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
	
	private CodeKind codeKind;

	private GUI gui;

	private Callback onChangeCallback;

	private boolean changed = false;

	// graphical components
	private JLabel nameLabel;
	private JTextPane fileContentMemo;


	public AugFileTab(JPanel parentPanel, AugFile augFile, final GUI gui, AugFileCtrl augFileCtrl, CodeKind codeKind) {

		this.parent = parentPanel;

		this.augFile = augFile;

		this.augFileCtrl = augFileCtrl;
		
		this.codeKind = codeKind;
		
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
	}

	private JPanel createVisualPanel() {

		JPanel tab = new JPanel();
		tab.setLayout(new GridBagLayout());

		nameLabel = new JLabel(augFile.getFilename());
		nameLabel.setPreferredSize(new Dimension(0, nameLabel.getPreferredSize().height*2));
		tab.add(nameLabel, new Arrangement(0, 0, 1.0, 0.0));

		fileContentMemo = new JTextPane() {
			private final static long serialVersionUID = 1L;
			
			public boolean getScrollableTracksViewportWidth() {
				return getUI().getPreferredSize(this).width <= getParent().getSize().width;
			}
		};
		
		Code highlighter;
		switch (codeKind) {
			case JAVA:
				highlighter = new JavaCode(fileContentMemo);
				break;
			case GROOVY:
				highlighter = new GroovyCode(fileContentMemo);
				break;
			case MARKDOWN:
				highlighter = new MarkdownCode(fileContentMemo);
				break;
			default:
				highlighter = new PlainText(fileContentMemo);
		}
		switch (gui.currentScheme) {
			case GUI.LIGHT_SCHEME:
				highlighter.setLightScheme();
				break;
			case GUI.DARK_SCHEME:
				highlighter.setDarkScheme();
				break;
		}
		fileContentMemo.setText(augFile.getContent());
		highlighter.setOnChange(onChangeCallback);
		JScrollPane sourceCodeScroller = new JScrollPane(fileContentMemo);
		sourceCodeScroller.setPreferredSize(new Dimension(1, 1));
		tab.add(sourceCodeScroller, new Arrangement(0, 3, 1.0, 0.8));

		parent.add(tab);

		tab.setVisible(false);

		// scroll to the top
		fileContentMemo.setCaretPosition(0);

	    return tab;
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
	}
	
	public void hide() {

		visualPanel.setVisible(false);
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
	
	public void save() {

		changed = false;
		
		augFile.setContent(fileContentMemo.getText());
		
		augFile.save();

		gui.regenerateAugFileList();
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
