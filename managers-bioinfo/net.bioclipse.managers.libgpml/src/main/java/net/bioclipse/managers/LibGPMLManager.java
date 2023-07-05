/* Copyright (c) 2011,2020  Egon Willighagen <egon.willighagen@gmail.com>
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

/**
 * Bioclipse manager that provides BioJava functionality.
 */
public class LibGPMLManager implements IBactingManager {

	private String workspaceRoot;

    /**
     * Creates a new {@link LibGPMLManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public LibGPMLManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	@Override
	public String getManagerName() {
		return "gpml";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}

}
