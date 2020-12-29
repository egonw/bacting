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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PubChemManagerTest {

	static PubChemManager pubchem;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("pubchemtestws").toString();
		pubchem = new PubChemManager(workspaceRoot);
	}

	@Test
	public void testManagerName() {
		assertSame("pubchem", pubchem.getManagerName());
	}

	@Test
	public void testDOIs() {
		List<String> dois = pubchem.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testSearch() throws Exception {
		List<Integer> results = pubchem.search("methane");
		assertNotNull(results);
		assertNotSame(0, results.size());
	}

	@Test
	public void downloadAsString() throws Exception {
		String xml = pubchem.downloadAsString(71583);
		assertNotNull(xml);
		assertTrue(xml.contains("PC-Compounds"));
	}

}
