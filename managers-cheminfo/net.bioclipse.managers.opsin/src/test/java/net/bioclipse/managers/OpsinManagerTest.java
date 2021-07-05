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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

public class OpsinManagerTest {

	static OpsinManager opsin;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("opsintestws").toString();
		opsin = new OpsinManager(workspaceRoot);
	}

	@Test
	public void testParseIUPACName() throws Exception {
		IMolecule molecule = opsin.parseIUPACName("methane");
		assertNotNull(molecule);
	}

	@Test
	public void testParseIUPACNameAsCML() throws Exception {
		String cmlMolecule = opsin.parseIUPACNameAsCML("methane");
		assertNotNull(cmlMolecule);
		assertTrue(cmlMolecule.contains("cml"));
	}

	@Test
	public void testParseIUPACNameAsCML_Bad() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				opsin.parseIUPACNameAsCML("brexit");
			}
		);
		assertTrue(exception.getMessage().contains("Could not parse"));
	}

	@Test
	public void testParseIUPACName_Bad() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				opsin.parseIUPACName("brexit");
			}
		);
		assertTrue(exception.getMessage().contains("Could not parse"));
	}

	@Test
	public void testDOIs() {
		List<String> dois = opsin.doi();
		assertNotNull(dois);
		assertSame(1, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("opsin", opsin.getManagerName());
	}

}
