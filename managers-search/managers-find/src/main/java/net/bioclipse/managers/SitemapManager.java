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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * Bioclipse manager that helps work with <a href="https://www.sitemaps.org/">sitemaps</a>.
 */
public class SitemapManager implements IBactingManager {

	private String workspaceRoot;

	private BioclipseManager bioclipse;
	private XMLManager xml;

	/**
     * Creates a new {@link SitemapManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public SitemapManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		this.bioclipse = new BioclipseManager(workspaceRoot);
		this.xml = new XMLManager(workspaceRoot);
	}

    /**
     * Extracts the URLs in a sitemap, each as a String representation.
     * 
     * @param sitemapURL {@link URL} of the <code>sitemap.xml</code> to parse
     * @return a {@link List} of {@link String}s of URLs
     * @throws BioclipseException when the sitemap XML is not valid
     */
    public List<String> parse(String sitemapURL) throws BioclipseException {
    	List<String> urls = new ArrayList<>();
		String sitemapContent = bioclipse.download(sitemapURL);
		try {
			Document doc = xml.readString(sitemapContent);
			Iterator<Element> urlElems = doc.getRootElement().getChildElements().iterator();
			while (urlElems.hasNext()) {
				Elements locs = urlElems.next().getChildElements("loc", "http://www.sitemaps.org/schemas/sitemap/0.9");
				if (locs.size() > 0) urls.add(locs.get(0).getValue());
			}
		} catch (CoreException exception) {
			throw new BioclipseException("Sitemap validity problem: " + exception.getMessage(), exception);
		}
    	return urls;
    }
    
    @Override
	public String getManagerName() {
		return "sitemap";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}
}
