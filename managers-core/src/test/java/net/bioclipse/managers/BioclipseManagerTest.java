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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;

public class BioclipseManagerTest {

	static BioclipseManager bioclipse;
	static String tmpPath;

	@BeforeAll
	static void setupManager() throws IOException {
		tmpPath = Files.createTempDirectory("bioclipsetestws").toString();
		System.out.println("tmpPath: " + tmpPath);
		bioclipse = new BioclipseManager(tmpPath);
		UIManager ui = new UIManager(tmpPath);
		ui.newProject("/Download/");
	}

	@Test
	public void testDOIs() {
		List<String> dois = bioclipse.doi();
		assertNotNull(dois);
		assertNotEquals(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("bioclipse", bioclipse.getManagerName());
	}

	@Test
	public void testIsOnline() {
		assertTrue(bioclipse.isOnline()); 
	}

	@Test
	public void testAssumeOnline() throws BioclipseException {
		bioclipse.assumeOnline();
	}

	@Test
	public void testFullPath() {
		String path = "/Project/test.txt";
		String fullPath = bioclipse.fullPath(path);
		assertTrue(fullPath.contains(path));
		assertTrue(fullPath.contains(tmpPath));
	}

	@Test
	public void testVersion() {
		assertSame("2.8.0", bioclipse.version());
	}

	@Test
	public void testRequireVersion() throws Exception {
		bioclipse.requireVersion("2.7.9.RC1");
		bioclipse.requireVersion("2.8.0");
		bioclipse.requireVersion("2.8");
	}

	@Test
	public void testCompare() {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{ bioclipse.requireVersion("a"); }
		);
		assertTrue(exception.getMessage().contains("Version numbers looks like these"));
	}

	@Test()
	public void testRequireVersion_TooNew() {
		String futureVersion = "3.0.0";
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
			    bioclipse.requireVersion(futureVersion);
			}
		);
		assertTrue(exception.getMessage().contains("requires at least version"));
		assertTrue(exception.getMessage().contains(futureVersion));
	}

	@Test
	public void testSparqlRemote() throws BioclipseException {
		byte[] results = bioclipse.sparqlRemote(
			"https://sparql.wikipathways.org/sparql",
			"SELECT * WHERE { ?s ?p ?o } LIMIT 1"
		);
		assertNotSame(0, results.length);
	}

	@Test
	public void testDownload() throws BioclipseException {
		String results = bioclipse.download(
			"https://wikipathways.org"
		);
		assertTrue(results.contains("WikiPathways"));
	}

	@Test
	public void testDownload_BadURL() {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				bioclipse.download("xxx://wikipathways.org");
			}
		);
		assertTrue(exception.getMessage().contains("Error while opening browser"));
	}

	@Test
	public void testDownload2() throws BioclipseException {
		String results = bioclipse.download(
			"https://wikidata.org/entity/Q5",
			"text/n3"
		);
		assertTrue(results.contains("Q5"));
		assertTrue(results.contains("rdfs:label"));
	}

	@Test
	public void testDownloadAsFile() throws BioclipseException {
		String results = bioclipse.downloadAsFile(
			"https://wikidata.org/wiki/Q5",
			"/Download/test.html"
		);
		assertTrue(results.equals("/Download/test.html"));
		// should also test the file content
	}

	@Test
	public void testDownloadAsFile2() throws BioclipseException {
		String results = bioclipse.downloadAsFile(
			"https://wikidata.org/wiki/Q5",
			"text/n3",
			"/Download/test.n3"
		);
		assertTrue(results.equals("/Download/test.n3"));
		// should also test the file content
	}
}
