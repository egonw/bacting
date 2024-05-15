/* Copyright (c) 2024  Egon Willighagen <egon.willighagen@gmail.com>
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
public class GoslinManager implements IBactingManager {

	private String workspaceRoot;

    /**
     * Creates a new {@link GoslinManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public GoslinManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	@Override
	public String getManagerName() {
		return "goslin";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}

}
