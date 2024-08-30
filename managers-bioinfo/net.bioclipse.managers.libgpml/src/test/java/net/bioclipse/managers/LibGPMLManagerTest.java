/* Copyright (c) 2020,2024  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pathvisio.libgpml.model.PathwayModel;

public class LibGPMLManagerTest {

	static LibGPMLManager gpml;
	static UIManager ui;

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("libgpmltestws").toString();
		gpml = new LibGPMLManager(tmpPath);
		ui = new UIManager(tmpPath);
		ui.newProject("/GPMLTests/");

		InputStream stream = LibGPMLManagerTest.class.getClassLoader().getResourceAsStream("WP1546.gpml");
		String newFile = ui.newFile("/GPMLTests/WP1546.gpml");
		ui.append(newFile, stream);
	}

	@Test
	public void testLoading() throws Exception {
		PathwayModel pathway = gpml.loadModel("/GPMLTests/WP1546.gpml");
		assertNotNull(pathway);
	}

	@Test
	public void testSaving() throws Exception {
		PathwayModel pathway = gpml.loadModel("/GPMLTests/WP1546.gpml");
		gpml.saveModelAsGPML2013a("/GPMLTests/test.gpml", pathway);
	}

	@Test
	public void testDOIs() {
		List<String> dois = gpml.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("gpml", gpml.getManagerName());
	}

}
