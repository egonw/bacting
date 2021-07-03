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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.inchi.InChI;

public class InChIManagerTest {

	static InChIManager inchi;
	static CDKManager cdk;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("inchitestws").toString();
		inchi = new InChIManager(workspaceRoot);
		cdk = new CDKManager(workspaceRoot);
		String result = inchi.load();
		assertNotNull(result);
		assertTrue(inchi.isLoaded());
		assertTrue(inchi.isAvailable());
	}

	@Test
	public void testManagerName() {
		assertSame("inchi", inchi.getManagerName());
	}

	@Test
	public void testDOIs() {
		List<String> dois = inchi.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testGenerate() throws Exception {
		IMolecule mol = cdk.fromSMILES("CC");
		InChI someInChI = inchi.generate(mol);
		assertNotNull(someInChI);
		assertTrue(someInChI.getValue().contains("InChI=1S/"));
	}

	@Test
	public void testGenerateFixedH() throws Exception {
		IMolecule mol = cdk.fromSMILES("C=CO");
		InChI someInChI = inchi.generate(mol, "FixedH");
		assertNotNull(someInChI);
		assertTrue(someInChI.getValue().contains("InChI=1/"));
	}

	@Test
	public void testCheck() throws Exception {
		assertFalse(inchi.check("InChI="));
		assertFalse(inchi.check("Foo="));
		assertTrue(inchi.check("InChI=1S/CH4/h1H4"));
	}
	
	@Test
	public void testOptions() throws Exception {
		List<String> options = inchi.options();
		assertNotNull(options);
		assertTrue(options.contains("FixedH"));
	}

	@Test
	public void testCheckStrict() throws Exception {
		assertFalse(inchi.checkStrict("InChI="));
		assertFalse(inchi.checkStrict("Foo="));
	}

	@Test
	public void testCheckKey() throws Exception {
		assertFalse(inchi.checkKey("VNWKTOKETHGBQD-UHFFFAOYSA-3"));
		assertTrue(inchi.checkKey("VNWKTOKETHGBQD-UHFFFAOYSA-N"));
		assertFalse(inchi.checkKey("FOO-FOO-N"));
	}

}
