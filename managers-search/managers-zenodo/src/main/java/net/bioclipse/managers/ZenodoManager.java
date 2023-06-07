/* Copyright (c) 2022  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.util.Collections;
import java.util.List;

import io.github.egonw.bacting.IBactingManager;

public class ZenodoManager implements IBactingManager {

	private String workspaceRoot;
	
	private BioclipseManager bioclipse;

	/**
     * Creates a new {@link ZenodoManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public ZenodoManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		this.bioclipse = new BioclipseManager(workspaceRoot);
	}

    @Override
	public String getManagerName() {
		return "zenodo";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}
}
