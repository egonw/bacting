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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.domain.IMolecule;

public class OscarManagerTest {

	static OscarManager oscar;
	static CDKManager cdk;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("oscartestws").toString();
		oscar = new OscarManager(workspaceRoot);
		cdk = new CDKManager(workspaceRoot);
	}

	@Test
	public void testFindNamedEntities() throws Exception {
		List<String> namedEntities = oscar.findNamedEntities("methane");
		assertNotNull(namedEntities);
		assertEquals(1, namedEntities.size());
		assertEquals("methane", namedEntities.get(0));
	}

	@Test
	public void testParseIUPACNameAsCML() throws Exception {
		List<IMolecule> namedEntities = oscar.findResolvedNamedEntities("methane");
		assertNotNull(namedEntities);
		assertEquals(1, namedEntities.size());
		ICDKMolecule methane = cdk.asCDKMolecule(namedEntities.get(0));
		assertNotNull(methane);
		assertEquals("CH4", cdk.molecularFormula(methane));
	}

	@Test
	public void testExtractText() throws Exception {
		String html = "<html><body>Benzene and toluene.</body></html>";
		String text = oscar.extractText(html);
		assertNotNull(text);
		assertEquals("Benzene and toluene.", text);
	}

	@Test
	public void testDOIs() {
		List<String> dois = oscar.doi();
		assertNotNull(dois);
		assertSame(1, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("oscar", oscar.getManagerName());
	}

}
