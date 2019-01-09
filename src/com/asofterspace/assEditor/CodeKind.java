/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;


public enum CodeKind {

	ASSEMBLER("Assembler"),
	C("C"),
	CPP("C++"),
	CSHARP("C#"),
	CSS("CSS"),
	DATEX("DaTeX"),
	DELPHI("Delphi"),
	GROOVY("Groovy"),
	HTML("HTML"),
	JAVA("Java"),
	JAVASCRIPT("JavaScript"),
	JSON("JSON"),
	MARKDOWN("Markdown"),
	PLAINTEXT("Plain Text"),
	PHP("PHP"),
	PYTHON("Python"),
	XML("XML");


	String kindStr;


	CodeKind (String kindStr) {
		this.kindStr = kindStr;
	}

	public static CodeKind getFromString(String kindStr) {
		for (CodeKind ck : CodeKind.values()) {
			if (ck.kindStr.equals(kindStr)) {
				return ck;
			}
		}
		return null;
	}

	public String toString() {
		return kindStr;
	}

	public String toLowerCase() {
		return toString().toLowerCase();
	}

}
