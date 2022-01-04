/* Copyright (c) 2021  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.wikidata.domain;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.domain.IMolecule;

public class WikidataMoleculeTest {

	@BeforeEach
	public void slowDown() throws InterruptedException {
		// keep the Wikidata Query Service happy
		Thread.sleep(150);
	}

	@Test
	public void testConstructor() {
		WikidataMolecule mol = new WikidataMolecule("http://www.wikidata.org/entity/Q37129");
		assertNotNull(mol);
		assertEquals("http://www.wikidata.org/entity/Q37129", mol.getId());
	}

	@Test
	public void testDefaultConstructor() throws Exception {
		// Spring requirement
		WikidataMolecule mol = new WikidataMolecule();
		assertNotNull(mol);
		assertNull(mol.getId());
		assertEquals(0, mol.toSMILES().length());
		assertEquals(0, mol.toSMILES().length());
	}

	@Test
	public void testToSMILES() throws Exception {
		// Spring requirement
		WikidataMolecule mol = new WikidataMolecule("http://www.wikidata.org/entity/Q37129");
		assertNotNull(mol);
		assertEquals("http://www.wikidata.org/entity/Q37129", mol.getId());
		assertNotSame(0, mol.toSMILES().length());
		assertNotSame(0, mol.toSMILES().length());
	}

	@Test
	public void testGetAdapter() {
		WikidataMolecule mol = new WikidataMolecule("http://www.wikidata.org/entity/Q37129");
		Object adapter = mol.getAdapter(ICDKMolecule.class);
		assertNotNull(adapter);
		assertTrue(adapter instanceof ICDKMolecule);
		adapter = mol.getAdapter(IWikidataMolecule.class);
		assertNotNull(adapter);
		assertTrue(adapter instanceof WikidataMolecule);
	}

	@Test
	public void testGetResource() {
		WikidataMolecule mol = new WikidataMolecule("http://www.wikidata.org/entity/Q37129");
		assertNull(mol.getResource());
	}

	@Test
	public void testGetConformers() {
		WikidataMolecule mol = new WikidataMolecule("http://www.wikidata.org/entity/Q37129");
		List<IMolecule> conformers = mol.getConformers();
		assertNotNull(conformers);
		assertEquals(0, conformers.size());
	}
}
