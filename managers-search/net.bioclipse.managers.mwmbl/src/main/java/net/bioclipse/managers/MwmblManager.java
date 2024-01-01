/* Copyright (c) 2024  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;

/**
 * Bioclipse manager that provides functionality to search in the Mwmbl (mwmbl.org) search engine.
 */
public class MwmblManager implements IBactingManager {

	private String workspaceRoot;
	static BioclipseManager bioclipse;

	/**
     * Creates a new {@link MwmblManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public MwmblManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		bioclipse = new BioclipseManager(workspaceRoot);
	}

    /**
     * Method to search in Mwmbl.
     *
     * @param query The search string to find webpages for.
     * @return a list of webpages
     * @throws BioclipseException when the search string cannot be converted into an UTF_8 string
     */
    public List<String> search(String query) throws BioclipseException {
    	List<String> foundPages = new ArrayList<>();
		try {
			String url = "https://mwmbl.org/api/v1/search/?s=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
	    	String jsonContent = bioclipse.download(url);
	    	System.out.println(jsonContent);
	    	JSONArray json = new JSONArray(jsonContent);
	    	json.forEach(
	    		object -> {
	    			foundPages.add(((JSONObject)object).getString("url"));
	    		}
	    	);
		} catch (UnsupportedEncodingException e) {
			throw new BioclipseException(e.getMessage(), e);
		}
    	return  foundPages;
    }
    
    @Override
	public String getManagerName() {
		return "mwmbl";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}
}
