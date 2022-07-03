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

public class PubmedManager implements IBactingManager {

	private String workspaceRoot;
	
	private BioclipseManager bioclipse;

	/**
     * Creates a new {@link PubmedManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public PubmedManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		this.bioclipse = new BioclipseManager(workspaceRoot);
	}

    public JSONObject fetchInfo(String pubmed) throws BioclipseException {
    	try {
    		Integer.parseInt(pubmed);
    	} catch (Exception exception) {
			throw new BioclipseException("Expected an integer but got " + pubmed);
		}
    	String url = "https://api.ncbi.nlm.nih.gov/lit/ctxp/v1/pubmed/?format=csl&id=" + pubmed;
		String jsonContent = bioclipse.download(url);
	    JSONObject json = new JSONObject(jsonContent);
	    return json;
    }
    
    @Override
	public String getManagerName() {
		return "pubmed";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}
}
