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

import org.json.JSONObject;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;

public class DOIManager implements IBactingManager {

	private String workspaceRoot;
	
	private BioclipseManager bioclipse;

	/**
     * Creates a new {@link DOIManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public DOIManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		this.bioclipse = new BioclipseManager(workspaceRoot);
	}

    public JSONObject fetchInfo(String doi) throws BioclipseException {
    	String url = "https://doi.org/" + doi;
		String jsonContent = bioclipse.download(url, "application/vnd.citationstyles.csl+json");
	    JSONObject json = new JSONObject(jsonContent);
	    return json;
    }
    
    @Override
	public String getManagerName() {
		return "doi";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}
}
