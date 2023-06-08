/* Copyright (c) 2023  Egon Willighagen <egonw@users.sf.net>
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
import net.bioclipse.core.business.BioclipseException;

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

	public String getOAIPMHData(String doi) throws BioclipseException {
		if (doi.startsWith("10.5281/zenodo.")) {
			int recordID = Integer.valueOf(doi.substring(15));
			return this.bioclipse.download("https://zenodo.org/oai2d?verb=GetRecord&metadataPrefix=oai_datacite&identifier=oai:zenodo.org:" + recordID);
		}
		throw new BioclipseException("This does not seem to be a Zenodo DOI and does not start with '10.5281/zenodo'.");
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
