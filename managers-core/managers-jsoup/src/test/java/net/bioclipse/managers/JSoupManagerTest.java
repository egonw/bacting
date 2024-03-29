/* Copyright (c) 2015-2023  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;

public class JSoupManagerTest {

	static JSoupManager jsoup;
	static UIManager ui;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("jsouptestws").toString();
		jsoup = new JSoupManager(workspaceRoot);
		ui = new UIManager(workspaceRoot);
		ui.newProject("/JSoupTests/");
	}

    @Test
    public void parseString() {
        Document doc = jsoup.parseString("<html />");
        assertNotNull(doc);
    };

    @Test
    public void parseFile() throws IOException, BioclipseException {
        String html = "<html><body><h3 id=\"foo\">Header</h3></body></html>";
        String newFile = ui.newFile("/JSoupTests/test1.txt", html);
        Document doc = jsoup.parse(newFile);
        assertNotNull(doc);
    }

	@Test
    public void removeHTMLTags() {
    	String html = "<html><body><h3 id=\"foo\">Header</h3></body></html>";
    	String text = jsoup.removeHTMLTags(html);
    	assertNotNull(text);
    	assertEquals("Header", text);
    }
	
	@Test
    public void select() {
    	Document doc = jsoup.parseString("<html><body><h3 id=\"foo\" /></body></html>");
    	Elements results = jsoup.select(doc, "#foo");
    	assertNotNull(results);
    	assertEquals("<h3 id=\"foo\"></h3>", "" + results);
    }
	
	@Test
	public void testDOIs() {
		List<String> dois = jsoup.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("jsoup", jsoup.getManagerName());
	}

}
