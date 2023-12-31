/* Copyright (c) 2015-2023  Egon Willighagen <egonw@users.sf.net>
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
import net.bioclipse.core.business.BioclipseException;

/**
 * Manager for JSoup functionality to parse HTML content.
 */
public class JSoupManager implements IBactingManager {

	private String workspaceRoot;
	static UIManager ui;

	/**
     * Creates a new {@link JSoupManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public JSoupManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		ui = new UIManager(workspaceRoot);
	}

    /**
     * Parses a string with HTML content into the JSoup {@link org.jsoup.nodes.Document}.
     *
     * @param htmlString the HTML as {@link String}
     * @return           the HTML content as {@link org.jsoup.nodes.Document}
     */
    public Document parseString(String htmlString) {
    	Document doc = Jsoup.parse(htmlString);
    	return doc;
    };

    /**
     * Parses a file with HTML content from the workspace into the JSoup {@link org.jsoup.nodes.Document}.
     *
     * @param  htmlFile           the name of the HTML file in the workspace
     * @return                    the HTML content as {@link org.jsoup.nodes.Document}
     * @throws BioclipseException when the file could not be read
     */
    public Document parse(String htmlFile) throws BioclipseException {
        String htmlString = ui.readFile(htmlFile);
        return parseString(htmlString);
    };

    /**
     * Takes a HTML string and removes all tags.
     *
     * @param htmlString the HTML as {@link String}
     * @return           the text bits from the HTML
     */
    public String removeHTMLTags(String htmlString) {
    	return Jsoup.parse(htmlString).text();
    };

    /**
     * Selects a subsection of the {@link Document} and returns it as an {@link Elements} object.
     *
     * @param doc         JSoup document to select from as {@link Element}
     * @param cssSelector String with a Cascading Style Sheet selector instruction
     * @return            the selected content
     */
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
