/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;


public class FileTreeFile extends FileTreeNode {

	private AugFileTab tab;


	public FileTreeFile(String name, AugFileTab tab) {
		super(name);

		this.tab = tab;
	}

	public AugFileTab getTab() {
		return tab;
	}

}
