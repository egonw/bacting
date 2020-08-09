/* Copyright (c) 2020  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class XMLManagerTest {

	static XMLManager xml;
	static UIManager ui;
	static BioclipseManager bioclipse;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("uitestws").toString();
		System.out.println("tmpPath: " + workspaceRoot);
		xml = new XMLManager(workspaceRoot);
		ui = new UIManager(workspaceRoot);
		ui.newProject("/XMLTests/");
		bioclipse = new BioclipseManager(workspaceRoot);
		bioclipse.downloadAsFile(
			"https://raw.githubusercontent.com/egonw/bacting/master/pom.xml",
			"/XMLTests/pom.xml"
		);
	}

	@Test
	public void testDOIs() {
		List<String> dois = xml.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("xml", xml.getManagerName());
	}

	@Test
	public void testIsWellFormed() throws Exception {
		assertTrue(xml.isWellFormed("/XMLTests/pom.xml"));
	}

	@Test
	public void testIsValid() throws Exception {
		assertTrue(xml.isValid("/XMLTests/pom.xml"));
	}

	@Test
	public void testListNamespaces() throws Exception {
		List<String> namespaces = xml.listNamespaces("/XMLTests/pom.xml");
		System.out.println(namespaces);
		assertNotNull(namespaces);
		assertNotEquals(0, namespaces.size());
	}
}
