/* Copyright (c) 2015-2022  Egon Willighagen <egonw@users.sf.net>
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.github.egonw.bacting.IBactingManager;

public class JSoupManager implements IBactingManager {

	private String workspaceRoot;

	/**
     * Creates a new {@link JSoupManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public JSoupManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

    public Document parseString(String htmlString) {
    	Document doc = Jsoup.parse(htmlString);
    	return doc;
    };

    public Elements select(Element doc, String cssSelector) {
    	Elements results = doc.select(cssSelector);
    	return results;    	
    }
    
    @Override
	public String getManagerName() {
		return "jsoup";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}
}
