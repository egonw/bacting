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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;
import nu.xom.Document;

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
		ui.newFile("/XMLTests/notWellFormed.xml", "<xml>");
		ui.newFile("/XMLTests/doubleNamespace.xml",
			"<xml xmlns:ns=\"http://examples.org/\" xmlns:ns2=\"http://examples.org/\" />"
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
	public void testReadWellFormed() throws Exception {
		Document doc = xml.readWellFormed("/XMLTests/pom.xml");
		assertNotNull(doc);
		assertSame("project", doc.getRootElement().getLocalName());
	}

	@Test
	public void testReadValid() throws Exception {
		Document doc = xml.readValid("/XMLTests/pom.xml");
		assertNotNull(doc);
		assertSame("project", doc.getRootElement().getLocalName());
	}

	@Test
	public void testReadValidString() throws Exception {
		String xmlContent = bioclipse.download("https://raw.githubusercontent.com/egonw/bacting/master/pom.xml");
		Document doc = xml.readValidString(xmlContent);
		assertNotNull(doc);
		assertSame("project", doc.getRootElement().getLocalName());
	}

	@Test
	public void testReadString() throws Exception {
		String xmlContent = bioclipse.download("https://raw.githubusercontent.com/egonw/bacting/master/pom.xml");
		Document doc = xml.readString(xmlContent);
		assertNotNull(doc);
		assertSame("project", doc.getRootElement().getLocalName());
	}

	@Test
	public void testIsNotWellFormed() throws Exception {
		boolean isWellFormed = xml.isWellFormed("/XMLTests/notWellFormed.xml");
		assertFalse(isWellFormed);
	}

	@Test
	public void testIsWellFormed_NoFile() {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				xml.isWellFormed("/XMLTests/doesnotexist.xml");
			}
		);
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Error while opening file"));
	}

	@Test
	public void testReadWellFormed_NoFile() {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				xml.readWellFormed("/XMLTests/doesnotexist.xml");
			}
		);
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Error while reading file"));
	}

	@Test
	public void testIsValid() throws Exception {
		assertTrue(xml.isValid("/XMLTests/pom.xml"));
	}

	@Test
	public void testIsValid_NotFound() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				xml.isValid("/DoesNotExist/pom.xml");
			}
		);
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Error while opening file"));
	}

	@Test
	public void testReadValid_NotFound() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				xml.readValid("/DoesNotExist/pom.xml");
			}
		);
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Error while reading file"));
	}

	@Test
	public void testListNamespaces() throws Exception {
		List<String> namespaces = xml.listNamespaces("/XMLTests/pom.xml");
		assertNotNull(namespaces);
		assertNotEquals(0, namespaces.size());
		namespaces = xml.listNamespaces("/XMLTests/doubleNamespace.xml");
		assertNotNull(namespaces);
		assertSame(1, namespaces.size());
	}
}
