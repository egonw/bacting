/* Copyright (c) 2021  Egon Willighagen <egon.willighagen@gmail.com>
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pathvisio.model.PathwayModel;

import net.bioclipse.core.business.BioclipseException;

public class PathvisioManagerTest {

	static PathvisioManager pathvisio;
	static UIManager ui;

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("pvtestws").toString();
		pathvisio = new PathvisioManager(tmpPath);
		ui = new UIManager(tmpPath);
	}

	@Test
	public void testGetGPML() throws Exception {
		String gpmlFile = pathvisio.getGPML("WP4846");
		assertNotNull(gpmlFile);
		assertTrue(gpmlFile.contains("WP4846.gpml"));
		ui.renewFile(gpmlFile);
	}

	@Test
	public void testGetGPML_JustNumber() throws Exception {
		String gpmlFile = pathvisio.getGPML("4846");
		assertNotNull(gpmlFile);
		assertTrue(gpmlFile.contains("WP4846.gpml"));
		ui.renewFile(gpmlFile);
	}

	@Test
	public void testGetGPML_WithRevision() throws Exception {
		String gpmlFile = pathvisio.getGPML("WP4846_r114246");
		assertNotNull(gpmlFile);
		assertTrue(gpmlFile.contains("WP4846_r114246.gpml"));
		ui.renewFile(gpmlFile);
	}

	@Test
	public void loadGPML() throws Exception {
		String gpmlFile = pathvisio.getGPML("WP4846");
		PathwayModel model = pathvisio.loadGPML(gpmlFile);
		assertNotNull(model);
		assertTrue(model.getPathway().getTitle().contains("SARS-CoV-2"));
	}

	@Test
	public void queryWikipathways() throws Exception {
		Set<String> hits = pathvisio.queryWikipathways("orf1");
		assertNotNull(hits);
		assertNotEquals(0, hits.size());
	}

	@Test
	public void queryWikipathways_NotAGene() throws Exception {
		Set<String> hits = pathvisio.queryWikipathways("SARS-CoV-2");
		assertNotNull(hits);
		assertSame(0, hits.size());
	}

	@Test
	public void testWriteWrongFormat() throws Exception {
		String gpmlFile = pathvisio.getGPML("WP4846");
		PathwayModel model = pathvisio.loadGPML(gpmlFile);
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{ pathvisio.writeGPML("/Virtual/WP4846.2001.gpml", model, "GPML2009"); }
		);
		assertTrue(exception.getMessage().contains("No support for GPML version"));
	}

	@Test
	public void testWriteGPML2013a() throws Exception {
		String gpmlFile = pathvisio.getGPML("WP4846");
		PathwayModel model = pathvisio.loadGPML(gpmlFile);
		String gpml2013aFile = pathvisio.writeGPML2013a("/Virtual/WP4846.2013a.gpml", model);
		ui.fileExists(gpml2013aFile);
	}

	@Test
	public void testWriteGPML2021() throws Exception {
		String gpmlFile = pathvisio.getGPML("WP4846");
		PathwayModel model = pathvisio.loadGPML(gpmlFile);
		String gpml2021File = pathvisio.writeGPML2021("/Virtual/WP4846.2021.gpml", model);
		ui.fileExists(gpml2021File);
	}

	@Test
	public void testDOIs() {
		List<String> dois = pathvisio.doi();
		assertNotNull(dois);
		assertNotEquals(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("pathvisio", pathvisio.getManagerName());
	}

}
