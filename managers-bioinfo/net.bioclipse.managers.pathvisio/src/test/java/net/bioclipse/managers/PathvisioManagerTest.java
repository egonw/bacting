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

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("pvtestws").toString();
		pathvisio = new PathvisioManager(tmpPath);
	}

	@Test
	public void testGetGPML() throws BioclipseException {
		String gpmlFile = pathvisio.getGPML("WP4846");
		assertNotNull(gpmlFile);
		assertTrue(gpmlFile.contains("WP4846.gpml"));
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
