/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.assEditor;

import com.asofterspace.toolbox.io.File;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


public class WorkspaceUtils {

	public static List<JMenuItem> createWorkspaceMenuEntries(JMenu parentElem, WorkspaceAction onClickAction, MainGUI mainGUI) {

		List<JMenuItem> workspaces = new ArrayList<>();

		// if we are in standalone mode, do nothing
		if (parentElem == null) {
			return workspaces;
		}

		parentElem.removeAll();

		List<String> workspaceNames = mainGUI.getAugFileCtrl().getWorkspaces();
		for (int i = 0; i < workspaceNames.size(); i++) {
			final String workspaceName = workspaceNames.get(i);
			if (i + 1 < workspaceNames.size()) {
				final String nextWorkspaceName = workspaceNames.get(i + 1);
				int wNIndex = workspaceName.indexOf(" ");
				if (wNIndex < 0) {
					wNIndex = workspaceName.length();
				}
				int nextWNIndex = nextWorkspaceName.indexOf(" ");
				if (nextWNIndex < 0) {
					nextWNIndex = nextWorkspaceName.length();
				}
				// we want to check that " " is not in the first position (therefore > 0)
				if ((wNIndex > 0) && (nextWNIndex > 0)) {
					String workspaceNamePrefix = workspaceName.substring(0, wNIndex);
					String nextWorkspaceNamePrefix = nextWorkspaceName.substring(0, nextWNIndex);
					if (workspaceNamePrefix.equals(nextWorkspaceNamePrefix)) {
						// actually group the workspaces together!
						JMenu submenu = new JMenu(workspaceNamePrefix);

						for (; i < workspaceNames.size(); i++) {
							final String innerWorkspaceName = workspaceNames.get(i);
							if (!(innerWorkspaceName + " ").startsWith(workspaceNamePrefix + " ")) {
								break;
							}
							JMenuItem workspace = createWorkspace(innerWorkspaceName, onClickAction, mainGUI);
							submenu.add(workspace);
							workspaces.add(workspace);
						}

						// we break the inner for, then continue the outer for,
						// so we do a ++ before the next outer for loop,
						// so we do a -- here to undo that
						// (or think about it this way: we have two nested loops,
						// EACH doing i++, but we only want one ++, so we do a --
						// to counterbalance one of the ++)
						i--;

						parentElem.add(submenu);
						continue;
					}
				}
			}
			JMenuItem workspace = createWorkspace(workspaceName, onClickAction, mainGUI);
			parentElem.add(workspace);
			workspaces.add(workspace);
		}

		return workspaces;
	}

	private static JMenuItem createWorkspace(final String workspaceName, WorkspaceAction onClickAction, final MainGUI mainGUI) {

		final JMenuItem workspace;

		switch (onClickAction) {

			case SWITCH_TO:
				workspace = new JCheckBoxMenuItem(workspaceName);
				workspace.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						((JCheckBoxMenuItem) workspace).setSelected(true);
						if (!workspaceName.equals(mainGUI.getAugFileCtrl().getActiveWorkspaceName())) {
							mainGUI.switchToWorkspace(workspaceName);
						}
					}
				});
				return workspace;

			case MOVE_FILES:
				workspace = new JMenuItem(workspaceName);
				workspace.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (!workspaceName.equals(mainGUI.getAugFileCtrl().getActiveWorkspaceName())) {
							List<AugFileTab> tabs = mainGUI.getHighlightedTabs();
							mainGUI.closeFiles(tabs);
							mainGUI.getAugFileCtrl().addTabsToWorkspace(tabs, workspaceName);
						}
					}
				});
				return workspace;

			case DUPLICATE_FILES:
				workspace = new JMenuItem(workspaceName);
				workspace.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (!workspaceName.equals(mainGUI.getAugFileCtrl().getActiveWorkspaceName())) {
							List<AugFileTab> tabs = mainGUI.getHighlightedTabs();
							mainGUI.getAugFileCtrl().addTabsToWorkspace(tabs, workspaceName);
						}
					}
				});
				return workspace;
		}

		return null;
	}
}
