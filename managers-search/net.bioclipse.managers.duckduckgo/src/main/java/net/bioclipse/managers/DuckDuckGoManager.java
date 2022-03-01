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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;

/**
 * Bioclipse manager that provides functionality around the Resource
 * Description Framework standard. It allows creating in memory and
 * on disk triple stores, creating on content, and IO functionality.
 */
public class DuckDuckGoManager implements IBactingManager {

	private String workspaceRoot;
	static BioclipseManager bioclipse;
	static JSoupManager jsoup;

	/**
     * Creates a new {@link DuckDuckGoManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public DuckDuckGoManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		bioclipse = new BioclipseManager(workspaceRoot);
		jsoup = new JSoupManager(workspaceRoot);
	}

    public List<String> search(String query) throws BioclipseException {
    	List<String> foundPages = new ArrayList<>();
    	String results;
		try {
			String url = "https://duckduckgo.com/html/?ia=web&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
			Map<String,String> headers = new HashMap<>();
			headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
			results = bioclipse.download(url, "text/html", headers);
			Document jsoupDoc = jsoup.parseString(results);
	    	Elements finds = jsoup.select(jsoupDoc, ".result__extras__url .result__url");
	    	for (Element find : finds) {
	    		String findStr = find.attr("href");
	    		if (findStr != null) foundPages.add(findStr);
	    	}
		} catch (UnsupportedEncodingException e) {} // ignore, won't happen
    	return  foundPages;
    }
    
    @Override
	public String getManagerName() {
		return "duckduckgo";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}
}
