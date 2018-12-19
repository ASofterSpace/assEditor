/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;


public enum CodeKind {

	CSHARP("C#"),
	DELPHI("Delphi"),
	GROOVY("Groovy"),
	JAVA("Java"),
	JAVASCRIPT("JavaScript"),
	MARKDOWN("Markdown"),
	PHP("PHP");
	
	
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