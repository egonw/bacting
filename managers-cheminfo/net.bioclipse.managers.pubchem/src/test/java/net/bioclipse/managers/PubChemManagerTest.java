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

import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.rdf.business.IRDFStore;

public class PubChemManagerTest {

	static PubChemManager pubchem;
	static UIManager ui;
	static CDKManager cdk;
	static RDFManager rdf;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("pubchemtestws").toString();
		pubchem = new PubChemManager(workspaceRoot);
		ui = new UIManager(workspaceRoot);
		cdk = new CDKManager(workspaceRoot);
		rdf = new RDFManager(workspaceRoot);
		ui.newProject("/PubChemFiles/");
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
		results = pubchem.search("acetic acid"); // query with a space
		assertNotNull(results);
		assertNotSame(0, results.size());
	}

	@Test
	public void testSearch_Brexitane() throws Exception {
		List<Integer> results = pubchem.search("brexitane");
		assertNotNull(results);
		assertSame(0, results.size());
	}

	@Test
	public void downloadAsString() throws Exception {
		String xml = pubchem.downloadAsString(71583);
		assertNotNull(xml);
		assertTrue(xml.contains("PC-Compounds"));
	}

	@Test
	public void download3d() throws Exception {
		IMolecule mol = pubchem.download3d(71583);
		assertNotNull(mol);
		assertNotSame(0, cdk.asCDKMolecule(mol).getAtomContainer().getAtomCount());
	}

	@Test
	public void download3dAsString() throws Exception {
		String mol = pubchem.download3dAsString(71583);
		assertNotNull(mol);
		assertTrue(mol.contains("PUBCHEM_COMPOUND_CID"));
	}

	@Test
	public void download() throws Exception {
		IMolecule mol = pubchem.download(71583);
		assertNotNull(mol);
		assertNotSame(0, cdk.asCDKMolecule(mol).getAtomContainer().getAtomCount());
	}

	@Test
	public void downloadRDF() throws Exception {
		IRDFStore store = rdf.createInMemoryStore();
	    pubchem.downloadRDF(71583, store);
		assertNotNull(store);
		assertNotSame(0, rdf.size(store));
	}

	@Test
    public void loadCompound() throws Exception {
		pubchem.loadCompound(71583, "/PubChemFiles/cid71583.mol");
		assertTrue(ui.fileExists("/PubChemFiles/cid71583.mol"));
    }

	@Test
    public void loadCompound3d() throws Exception {
		pubchem.loadCompound3d(71583, "/PubChemFiles/cid71583_3d.mol");
		assertTrue(ui.fileExists("/PubChemFiles/cid71583_3d.mol"));
	}

}
